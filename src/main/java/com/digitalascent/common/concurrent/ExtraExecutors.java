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
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Wraps an executor, storing futures for executed tasks
 */
public final class ExtraExecutors {

    public static <T> ListenableFuture<T> execute(ListeningExecutorService executorService, List<ListenableFuture<T>> futures,  Callable<T> callable) {
        ListenableFuture<T> future = executorService.submit(callable);
        futures.add(future);
        return future;
    }

    public static void execute(ListeningExecutorService executorService, List<ListenableFuture<?>> futures, Runnable runnable) {
        ListenableFuture<?> future = executorService.submit(runnable);
        futures.add(future);
    }

    private ExtraExecutors() {
        StaticUtilityClass.cannotInstantiate( getClass() );
    }
}
