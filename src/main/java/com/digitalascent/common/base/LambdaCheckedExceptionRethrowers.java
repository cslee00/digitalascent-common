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

package com.digitalascent.common.base;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Utility methods to wrap functional interfaces used in Streams, rethrowing checked exceptions
 */
public final class LambdaCheckedExceptionRethrowers {

    @FunctionalInterface
    public interface CheckedPredicate<T, E extends Exception> {
        boolean test(T t) throws E;
    }

    @FunctionalInterface
    public interface CheckedBiPredicate<T, U, E extends Exception> {
        boolean test(T var1, U var2) throws E;
    }

    @FunctionalInterface
    public interface CheckedConsumer<T, E extends Exception> {
        void accept(T var1) throws E;
    }

    @FunctionalInterface
    public interface CheckedBiConsumer<T, U, E extends Exception> {
        void accept(T var1, U var2) throws E;
    }

    @FunctionalInterface
    public interface CheckedFunction<T, R, E extends Exception> {
        R apply(T var1) throws E;
    }

    @FunctionalInterface
    public interface CheckedBiFunction<T, U, R, E extends Exception> {
        R apply(T var1, U var2) throws E;
    }

    @FunctionalInterface
    public interface CheckedSupplier<T, E extends Exception> {
        T get() throws E;
    }

    @FunctionalInterface
    public interface CheckedRunnable<E extends Exception> {
        void run() throws E;
    }

    public static <T, E extends Exception> Predicate<T> rethrowingPredicate(CheckedPredicate<T, E> predicate) {
        return t -> {
            try {
                return predicate.test(t);
            } catch (Exception exception) {
                throwAsUnchecked(exception);
                return false;
            }
        };
    }

    public static <T, U, E extends Exception> BiPredicate<T, U> rethrowingBiPredicate(CheckedBiPredicate<T, U, E> predicate) {
        return (var1, var2) -> {
            try {
                return predicate.test(var1, var2);
            } catch (Exception exception) {
                throwAsUnchecked(exception);
                return false;
            }
        };
    }

    public static <T, E extends Exception> Consumer<T> rethrowingConsumer(CheckedConsumer<T, E> consumer) {
        return var1 -> {
            try {
                consumer.accept(var1);
            } catch (Exception exception) {
                throwAsUnchecked(exception);
            }
        };
    }

    public static <T, U, E extends Exception> BiConsumer<T, U> rethrowingBiConsumer(CheckedBiConsumer<T, U, E> biConsumer) {
        return (var1, var2) -> {
            try {
                biConsumer.accept(var1, var2);
            } catch (Exception exception) {
                throwAsUnchecked(exception);
            }
        };
    }

    public static <T, R, E extends Exception> Function<T, R> rethrowingFunction(CheckedFunction<T, R, E> function) {
        return t -> {
            try {
                return function.apply(t);
            } catch (Exception exception) {
                throwAsUnchecked(exception);
                return null;
            }
        };
    }

    public static <T, U, R, E extends Exception> BiFunction<T, U, R> rethrowingBiFunction(CheckedBiFunction<T, U, R, E> function) {
        return (var1, var2) -> {
            try {
                return function.apply(var1, var2);
            } catch (Exception exception) {
                throwAsUnchecked(exception);
                return null;
            }
        };
    }

    public static <T, E extends Exception> Supplier<T> rethrowingSupplier(CheckedSupplier<T, E> function) {
        return () -> {
            try {
                return function.get();
            } catch (Exception exception) {
                throwAsUnchecked(exception);
                return null;
            }
        };
    }

    public static <E extends Exception> Runnable rethrowingRunnable(CheckedRunnable<E> runnable) {
        return () -> {
            try {
                runnable.run();
            } catch (Exception exception) {
                throwAsUnchecked(exception);
            }
        };
    }

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    private static <E extends Throwable> void throwAsUnchecked(Exception exception) throws E {
        throw (E) exception;
    }

    private LambdaCheckedExceptionRethrowers() {
        StaticUtilityClass.throwCannotInstantiateError(getClass());
    }

}
