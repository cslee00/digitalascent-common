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

package com.digitalascent.common

import com.digitalascent.common.base.LambdaCheckedExceptionRethrowers
import spock.lang.Shared
import spock.lang.Specification

import java.lang.reflect.UndeclaredThrowableException
import java.util.function.BiFunction
import java.util.function.BiPredicate
import java.util.stream.Collectors

class LambdaCheckedExceptionRethrowersTest extends Specification {

    @Shared
    def list = [1, 2, 3, 4, 5, 6]
    def map = [a:1,b:2,c:3,d:4]

    def "unhandled checked consumer throws exception"() {
        when:
        list.forEach({ i -> doSomething(i) })

        then:
        UndeclaredThrowableException exception = thrown()
        exception.cause instanceof IOException
    }

    def "rethrowing checked consumer throws exception"() {
        when:
        list.forEach(LambdaCheckedExceptionRethrowers.rethrowingConsumer({ i -> doSomething(i as int) }))

        then:
        thrown IOException
    }

    def "unhandled checked biconsumer throws exception"() {
        when:
        map.forEach({ k,v -> doSomething(v) })

        then:
        UndeclaredThrowableException exception = thrown()
        exception.cause instanceof IOException
    }

    def "rethrowing checked biconsumer throws exception"() {
        when:
        map.forEach(LambdaCheckedExceptionRethrowers.rethrowingBiConsumer({ k, v -> doSomething(v as int) }))

        then:
        thrown IOException
    }

    def "unhandled checked predicate throws exception"() {
        when:
        list.stream().filter( {i -> checkIt(i)}).collect(Collectors.toList())

        then:
        UndeclaredThrowableException exception = thrown()
        exception.cause instanceof IOException
    }

    def "rethrowing checked predicate throws exception"() {
        when:
        list.stream().filter( LambdaCheckedExceptionRethrowers.rethrowingPredicate({ i -> checkIt(i)})).collect(Collectors.toList())

        then:
        thrown IOException
    }

    def "unhandled checked bipredicate throws exception"() {
        when:
        BiPredicate biPredicate = { a, b ->  checkIt(a) }
        biPredicate.test(1,2)

        then:
        UndeclaredThrowableException exception = thrown()
        exception.cause instanceof IOException
    }

    def "rethrowing checked bipredicate throws exception"() {
        when:
        BiPredicate biPredicate = LambdaCheckedExceptionRethrowers.rethrowingBiPredicate({ a, b ->  checkIt(a) })
        biPredicate.test(1,2)

        then:
        thrown IOException
    }

    def "unhandled checked function throws exception"() {
        when:
        list.stream().map( {a -> multiply(a)}).collect(Collectors.toList())

        then:
        UndeclaredThrowableException exception = thrown()
        exception.cause instanceof IOException
    }

    def "rethrowing checked function throws exception"() {
        when:
        list.stream().map( LambdaCheckedExceptionRethrowers.rethrowingFunction({ a -> multiply(a)})).collect(Collectors.toList())

        then:
        thrown IOException
    }

    def "unhandled checked bifunction throws exception"() {
        when:
        BiFunction biFunction = { a, b -> multiply(a)}
        biFunction.apply(1,2)

        then:
        UndeclaredThrowableException exception = thrown()
        exception.cause instanceof IOException
    }

    def "rethrowing checked bifunction throws exception"() {
        when:
        BiFunction biFunction = LambdaCheckedExceptionRethrowers.rethrowingBiFunction({ a, b -> multiply(a)})
        biFunction.apply(1,2)

        then:
        thrown IOException
    }


    boolean checkIt( int i ) throws IOException {
        if (i > 0) {
            throw new IOException()
        }
        return false
    }

    int multiply( int i ) throws IOException {
        if( i > 0 ) {
            throw new IOException()
        }
        return i * 2
    }

    void doSomething(int i) throws IOException {
        if (i > 0) {
            throw new IOException()
        }
    }
}
