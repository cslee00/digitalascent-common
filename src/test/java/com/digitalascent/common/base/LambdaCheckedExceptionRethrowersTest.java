package com.digitalascent.common.base;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import static com.digitalascent.common.base.LambdaCheckedExceptionRethrowers.rethrowingBiConsumer;
import static com.digitalascent.common.base.LambdaCheckedExceptionRethrowers.rethrowingBiFunction;
import static com.digitalascent.common.base.LambdaCheckedExceptionRethrowers.rethrowingBiPredicate;
import static com.digitalascent.common.base.LambdaCheckedExceptionRethrowers.rethrowingConsumer;
import static com.digitalascent.common.base.LambdaCheckedExceptionRethrowers.rethrowingFunction;
import static com.digitalascent.common.base.LambdaCheckedExceptionRethrowers.rethrowingPredicate;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LambdaCheckedExceptionRethrowersTest {

    private List<Integer> list;
    private Map<String, Integer> map;

    @BeforeEach
    void setup() {
        list = ImmutableList.of(1, 2, 3, 4, 5, 6);
        map = ImmutableMap.of("a", 1, "b", 2, "c", 3, "d", 4);
    }

    @Test
    void rethrowingCheckedConsumerThrowsException() {
        assertThatThrownBy(() -> list.forEach(rethrowingConsumer(this::doSomething)))
                .isInstanceOf(IOException.class).hasMessage("test exception");
    }

    @Test
    void rethrowingCheckedBiConsumerThrowsException() {
        assertThatThrownBy(() -> map.forEach(rethrowingBiConsumer((k, v) -> doSomething(v))))
                .isInstanceOf(IOException.class).hasMessage("test exception");
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void rethrowingCheckedPredicateThrowsException() {
        assertThatThrownBy(() -> list.stream().filter( rethrowingPredicate(this::checkIt))
                .collect(Collectors.toList()))
                .isInstanceOf(IOException.class).hasMessage("test exception");
    }

    @SuppressWarnings("ReturnValueIgnored")
    @Test
    void rethrowingCheckedBiPredicateThrowsException() {
        BiPredicate<Integer,Integer> biPredicate = rethrowingBiPredicate((a,b)->checkIt(a));
        assertThatThrownBy(() -> biPredicate.test(1,2))
                .isInstanceOf(IOException.class).hasMessage("test exception");
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void rethrowingCheckedFunctionThrowsException() {
        assertThatThrownBy(() -> list.stream().map(rethrowingFunction(this::multiply)).collect(Collectors.toList()))
                .isInstanceOf(IOException.class).hasMessage("test exception");
    }

    @SuppressWarnings("ReturnValueIgnored")
    @Test
    void rethrowingCheckedBiFunctionThrowsException() {
        BiFunction<Integer,Integer,Integer> biFunction = rethrowingBiFunction((a, b)->multiply(a));
        assertThatThrownBy(() -> biFunction.apply(1,2))
                .isInstanceOf(IOException.class).hasMessage("test exception");
    }

    boolean checkIt(int i) throws IOException {
        if (i > 0) {
            throw new IOException("test exception");
        }
        return false;
    }

    int multiply(int i) throws IOException {
        if (i > 0) {
            throw new IOException("test exception");
        }
        return i * 2;
    }

    void doSomething(int i) throws IOException {
        if (i > 0) {
            throw new IOException("test exception");
        }
    }
}