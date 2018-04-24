/*
 * Copyright 2017-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.digitalascent.common.collect;


import com.digitalascent.common.base.StaticUtilityClass;
import com.digitalascent.common.concurrent.ExtraThreads;
import com.google.common.base.Throwables;
import com.google.common.base.Verify;
import com.google.common.util.concurrent.Uninterruptibles;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Verify.verify;

public final class ExtraStreams {

    /**
     * Create a stream that synchronously lazy-loads batches of elements from the provided supplier.
     * Example usage:
     *
     * <pre>
     * return ExtraStreams.batchLoadingStream(nextBatchToken -> {
     *      describeParametersRequest.setNextToken(nextBatchToken);
     *      DescribeParametersResult result = ssm.describeParameters(describeParametersRequest);
     *      return new Batch<>( result.getNextToken(),result.getParameters() );
     * });
     * </pre>
     *
     * @param batchSupplier the supplier that provides batches to expose in the stream
     * @param <T>           type of element
     * @return Stream of elements that are lazy-loaded in batches from the provided supplier
     */
    public static <T> Stream<T> batchLoadingStream(BatchSupplier<T> batchSupplier) {
        checkNotNull(batchSupplier, "batchSupplier is required");

        Stream<Iterable<T>> batchStream = StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(new BatchIterator<>(batchSupplier), Spliterator.ORDERED | Spliterator.IMMUTABLE),
                false);

        return batchStream.flatMap(i -> StreamSupport.stream(i.spliterator(), false));
    }

    /**
     * Create a stream that <i>asynchronously</i> lazy-loads batches of elements from the provided supplier.
     * Useful for suppliers that load batches from API calls to overlap producer network/processing latency
     * with consumption of batches.
     *
     * @param batchSupplier the supplier that provides batches to expose in the stream
     * @param <T>           type of element
     * @param queueSize     number of batches to allow to be queued before blocking the supplier from adding more batches
     * @return Stream of elements that are <i>asynchronously</i> lazy-loaded in batches from the provided supplier
     */
    public static <T> Stream<T> queuedBatchLoadingStream(BatchSupplier<T> batchSupplier, int queueSize) {
        checkNotNull(batchSupplier, "batchSupplier is required");
        checkArgument(queueSize > 0, "queueSize must be > 0 : %s", queueSize);

        BlockingQueue<Iterable<T>> queue = new ArrayBlockingQueue<>(queueSize);
        Iterable<T> poison = new ArrayList<>();

        // load batches in a separate thread, governed by the queue size (blocking when queue is full)
        // only use a single thread as batches are chained (the result of one batch has the token to load the next batch)
        ExecutorService executorService = Executors.newFixedThreadPool(1, ExtraThreads.defaultThreadFactory("ExtraStreams.batchLoadingStream:" + batchSupplier.getClass()));
        Future<?> batchProducerFuture = executorService.submit(() -> {
            try {
                Batch<T> currentBatch = Batch.emptyBatch();
                String lastToken = null;
                while (true) {
                    currentBatch = batchSupplier.nextBatch(currentBatch.getNextToken());

                    verify(currentBatch != null, "Null batch returned from %s", batchSupplier.getClass());
                    verify(lastToken == null || !Objects.equals(lastToken, currentBatch.getNextToken()), "Received the same batch token '%s' for two batches, aborting", lastToken);

                    Uninterruptibles.putUninterruptibly(queue, currentBatch.getIterable());
                    lastToken = currentBatch.getNextToken();
                    if (lastToken == null) {
                        break;
                    }
                }
            } finally {
                // always poison the queue, notifying consumer that this producer is finished, even in the event of an exception here (which will propagate to consumer)
                Uninterruptibles.putUninterruptibly(queue, poison);
            }
        });
        executorService.shutdown();

        // pull each batch off the queue and return a stream for it
        return queueStream(queue, poison, batchProducerFuture).flatMap(batch -> StreamSupport.stream(batch.spliterator(), false));
    }

    /**
     * Creates a stream supplying elements from the provided BlockingQueue, terminating when the <b>poison</b> element is encountered
     *
     * @param queue          the queue to extract elements from
     * @param poison         element that terminates the stream
     * @param producerFuture future for the producer of elements consumed by this queue, used to propagate exceptions
     */
    private static <T> Stream<T> queueStream(BlockingQueue<T> queue, T poison, Future<?> producerFuture) {
        return StreamSupport.stream(new QueueSpliterator<>(queue, poison, producerFuture), false);
    }

    private ExtraStreams() {
        StaticUtilityClass.cannotInstantiate( getClass() );
    }

    /**
     * Iterator over batches of elements.
     *
     * @param <T> type of elements to iterate over
     */
    private static class BatchIterator<T> implements Iterator<Iterable<T>> {
        private final BatchSupplier<T> batchSupplier;
        private Batch<T> currentBatch;
        private String lastToken;

        BatchIterator(BatchSupplier<T> batchSupplier) {
            this.batchSupplier = checkNotNull(batchSupplier);
        }

        @Override
        public boolean hasNext() {
            // if we haven't requested the first batch or there is a token for a subsequent batch
            return currentBatch == null || currentBatch.getNextToken() != null;
        }

        @Override
        public Iterable<T> next() {
            currentBatch = batchSupplier.nextBatch(currentBatch == null ? null : currentBatch.getNextToken());
            Verify.verify(currentBatch != null, "Null batch returned from %s", batchSupplier.getClass());
            if (lastToken != null && Objects.equals(lastToken, currentBatch.getNextToken())) {
                throw new IllegalStateException(String.format("Received the same batch token '%s' for two batches, aborting", lastToken));
            }

            lastToken = currentBatch.getNextToken();

            return currentBatch.getIterable();
        }
    }

    /**
     * Spliterator that pulls elements from the provided queue.  Blocks waiting for elements from the queue, exiting
     * when the provided 'poison' element is encountered.  Exceptions from the async producer are propagated.
     *
     * @param <T> type of elements in the queue
     */
    private static class QueueSpliterator<T> implements Spliterator<T> {
        private final BlockingQueue<T> queue;
        private final T poison;
        private final Future<?> producerFuture;

        QueueSpliterator(BlockingQueue<T> queue, T poison, Future<?> producerFuture) {
            this.queue = checkNotNull(queue, "queue is required");
            this.poison = checkNotNull(poison, "poison is required");
            this.producerFuture = checkNotNull(producerFuture, "producerFuture is required");
        }

        @Override
        public int characteristics() {
            return Spliterator.NONNULL | Spliterator.ORDERED | Spliterator.IMMUTABLE;
        }

        @Override
        public long estimateSize() {
            return Long.MAX_VALUE;
        }

        @Override
        public boolean tryAdvance(final Consumer<? super T> action) {
            final T next = Uninterruptibles.takeUninterruptibly(queue);
            if (next == poison) {
                try {
                    // obtain result from producer, used to propagate any producer exceptions
                    Uninterruptibles.getUninterruptibly(producerFuture);
                } catch (ExecutionException e) {
                    // propagate producer exception
                    Throwables.throwIfUnchecked(e.getCause());
                    throw new RuntimeException(e.getCause());
                }
                return false;
            }
            action.accept(next);
            return true;
        }

        @Override
        public Spliterator<T> trySplit() {
            return null;
        }

    }
}
