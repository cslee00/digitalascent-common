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

package com.digitalascent.common.concurrent;

import com.digitalascent.common.base.StaticUtilityClass;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;

public final class ExtraThreads {

    public static ThreadFactoryBuilder defaultThreadFactoryBuilder() {
        return defaultThreadFactoryBuilder("pool");
    }

    private static ThreadFactoryBuilder defaultThreadFactoryBuilder(String threadPoolPrefix) {
        checkArgument(!isNullOrEmpty(threadPoolPrefix), "threadPoolPrefix is required to be non-null & not empty: %s", threadPoolPrefix);

        ThreadFactoryBuilder builder = new ThreadFactoryBuilder();
        builder.setUncaughtExceptionHandler(DEFAULT_UNCAUGHT_EXCEPTION_HANDLER);
        builder.setNameFormat(String.format("%s-%d-%%d", threadPoolPrefix, threadFactoryCounter.incrementAndGet()));
        return builder;
    }

    /**
     * Provides a default thread factory for use in thread pools.  ExtraThreads are named 'prefix-#-#' to uniquely identify them,
     * with the pool # incrementing for each thread factory.
     * The default uncaught exception handler is used, logging all uncaught exceptions
     *
     * @param threadPoolPrefix prefix for naming threads
     * @return ThreadFactory
     */
    public static ThreadFactory defaultThreadFactory(String threadPoolPrefix) {
        return defaultThreadFactoryBuilder(threadPoolPrefix).build();
    }

    /**
     * Throws InterruptedException if the current thread has been interrupted; useful to place in looping constructs
     * to abort processing if thread has been externally interrupted.
     *
     * @throws InterruptedException
     */
    public static void abortIfInterrupted() throws InterruptedException {
        if( Thread.currentThread().isInterrupted() ) {
            throw new InterruptedException("Thread Interrupted");
        }
    }

    public static Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler() {
        return DEFAULT_UNCAUGHT_EXCEPTION_HANDLER;
    }

    public static <T> T invokeWithThreadName(String threadName, Callable<T> callable) {
        return invokeWithThreadContext(ImmutableMap.of("threadName",threadName), callable);
    }

    /**
     * Invoke the provided callable (in the calling thread), using the supplied thread context to
     * modify the calling thread name for the duration of the call (reverting to original name when
     * call is completed).
     *
     * The following defaults are added to the context:
     *  "timestamp": current timestamp in UTC, ISO8601 (from Instant.now())
     *  "originalThreadName": contains the original name of the thread prior to invocation
     *
     * The thread name is JSON-encoded.
     *
     * @param threadContext
     * @param callable
     * @param <T>
     * @return
     */
    public static <T> T invokeWithThreadContext(Map<String, Object> threadContext, Callable<T> callable) {
        String originalThreadName = Thread.currentThread().getName();

        Map<String, Object> finalThreadContext = new LinkedHashMap<>();
        finalThreadContext.put("timestamp", Instant.now());
        finalThreadContext.putAll(threadContext);
        finalThreadContext.put("originalThreadName", originalThreadName);

        String threadContextJson = JsonGenerator.mapToJson(finalThreadContext);

        try {
            Thread.currentThread().setName(threadContextJson);
            return callable.call();
        } catch (Exception e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        } finally {
            Thread.currentThread().setName(originalThreadName);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static <T> void invokeWithThreadContext(Map<String, Object> threadContext, Runnable runnable ) {
        invokeWithThreadContext(threadContext, () -> {
            runnable.run();
            return null;
        });
    }


    private ExtraThreads() {
        StaticUtilityClass.cannotInstantiate( getClass() );
    }

    private static AtomicLong threadFactoryCounter = new AtomicLong();
    private static final Thread.UncaughtExceptionHandler DEFAULT_UNCAUGHT_EXCEPTION_HANDLER = new LoggingUncaughtExceptionHandler();

}
