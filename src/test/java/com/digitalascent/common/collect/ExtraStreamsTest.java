package com.digitalascent.common.collect;


import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import org.junit.jupiter.api.Test;


import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExtraStreamsTest {

    @Test
    void streamForReturnsEmptyStream() {
        assertThat(ExtraStreams.streamFor(null)).isEmpty();
    }

    @Test
    void batchIterationMultipleBatches() {
        List<Integer> list = ImmutableList.copyOf(ContiguousSet.create(
                Range.closed(1, 500), DiscreteDomain.integers()));

        List<List<Integer>> lists = Lists.partition(list, 6);
        final int[] idx = {0};
        Stream<Integer> stream = ExtraStreams.batchLoadingStream(
                nextToken -> new Batch<>(idx[0] == lists.size() - 1 ? null : String.valueOf(idx[0]), lists.get(idx[0]++))
        );
        List<Integer> finalList = stream.collect(Collectors.toList());

        assertThat(finalList).isEqualTo(list);
    }

    @Test
    void batchIterationSingleBatch() {
        List<Integer> list = ImmutableList.copyOf(ContiguousSet.create(
                Range.closed(1, 500), DiscreteDomain.integers()));

        final int[] idx = {0};
        Stream<Integer> stream = ExtraStreams.batchLoadingStream(
                nextToken -> new Batch<>(null, list)
        );
        List<Integer> finalList = stream.collect(Collectors.toList());

        assertThat(finalList).isEqualTo(list);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void batchIterationFailsOnRepeatedToken() {

        assertThatThrownBy(() -> {
            Stream<Integer> stream = ExtraStreams.batchLoadingStream(
                    nextToken -> new Batch<>("abc", ImmutableList.of())
            );
            stream.collect(Collectors.toList());
        }).isInstanceOf(IllegalStateException.class).hasMessageContaining("abc");
    }

    @Test
    void asyncBatchIterationOnMultipleBatches() {
        List<Integer> list = ImmutableList.copyOf(ContiguousSet.create(
                Range.closed(1, 500), DiscreteDomain.integers()));

        List<List<Integer>> lists = Lists.partition(list, 6);
        final int[] idx = {0};
        Stream<Integer> stream = ExtraStreams.queuedBatchLoadingStream(
                nextToken -> new Batch<>(idx[0] == lists.size() - 1 ? null : String.valueOf(idx[0]), lists.get(idx[0]++))
        , 5);
        List<Integer> finalList = stream.collect(Collectors.toList());

        assertThat(finalList).isEqualTo(list);
    }

    @Test
    void asyncBatchIterationSingleBatch() {
        List<Integer> list = ImmutableList.copyOf(ContiguousSet.create(
                Range.closed(1, 500), DiscreteDomain.integers()));

        final int[] idx = {0};
        Stream<Integer> stream = ExtraStreams.queuedBatchLoadingStream(
                nextToken -> new Batch<>(null, list)
        , 5);
        List<Integer> finalList = stream.collect(Collectors.toList());

        assertThat(finalList).isEqualTo(list);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void asyncBatchIterationFailsOnRepeatedToken() {

        assertThatThrownBy(() -> {
            Stream<Integer> stream = ExtraStreams.queuedBatchLoadingStream(
                    nextToken -> new Batch<>("abc", ImmutableList.of())
            , 5);
            stream.collect(Collectors.toList());
        }).isInstanceOf(IllegalStateException.class).hasMessageContaining("abc");
    }
}