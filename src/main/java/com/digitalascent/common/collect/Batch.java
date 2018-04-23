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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;


/**
 * Represents a batch of elements that can be iterated over.  Contains the 'next' token from the batch provider for loading
 * the next batch; may be null if there is no next batch.
 *
 * @param <T> type of elements in the batch
 */
public final class Batch<T> {
    private final String nextToken;
    private final Iterable<T> iterable;

    public static <T> Batch<T> emptyBatch() {
        return new Batch<>(null, ImmutableList.of());
    }

    public Batch(@Nullable String nextToken, Iterable<T> iterable) {
        this.nextToken = nextToken;
        this.iterable = Iterables.unmodifiableIterable(iterable);
    }

    String getNextToken() {
        return nextToken;
    }

    Iterable<T> getIterable() {
        return iterable;
    }
}
