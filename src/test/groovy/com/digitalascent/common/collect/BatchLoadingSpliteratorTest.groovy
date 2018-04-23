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

package com.digitalascent.common.collect

import com.google.common.base.VerifyException
import com.google.common.collect.Lists
import spock.lang.Specification

import java.util.stream.Collectors


class BatchLoadingSpliteratorTest extends Specification {

    def "iteration over multiple batches works"() {
        when:
        def lists = Lists.partition(1..100, 6)
        int idx = 0
        def stream = ExtraStreams.batchLoadingStream({ nextToken ->
            return new Batch<>(idx == lists.size() - 1 ? null : idx.toString(), lists.get(idx++))
        })
        def finalList = stream.collect(Collectors.toList())

        then:
        finalList.size() == 100

    }

    def "iteration over single batch works"() {
        when:
        def list = 1..100
        def stream = ExtraStreams.batchLoadingStream({ nextToken ->
            return new Batch<>(null , list)
        })
        def finalList = stream.collect(Collectors.toList())

        then:
        finalList.size() == 100

    }

    def "iteration over batches fails when token repeated"() {
        when:

        def stream = ExtraStreams.batchLoadingStream({ nextToken ->
            return new Batch<>("abc", [])
        })
        stream.collect(Collectors.toList())

        then:
        thrown(IllegalStateException.class)

    }


    def "iteration over multiple batches works async"() {
        when:
        def lists = Lists.partition(1..100, 6)
        int idx = 0
        def stream = ExtraStreams.queuedBatchLoadingStream({ nextToken ->
            return new Batch<>(idx == lists.size() - 1 ? null : idx.toString(), lists.get(idx++))
        },5)
        def finalList = stream.collect(Collectors.toList())

        then:
        finalList.size() == 100

    }

    def "iteration over single batch works async"() {
        when:
        def list = 1..100
        def stream = ExtraStreams.queuedBatchLoadingStream({ nextToken ->
            return new Batch<>(null , list)
        },5)
        def finalList = stream.collect(Collectors.toList())

        then:
        finalList.size() == 100

    }

    def "iteration over batches fails when token repeated async"() {
        when:

        def stream = ExtraStreams.queuedBatchLoadingStream({ nextToken ->
            return new Batch<>("abc", [])
        },5)
        stream.collect(Collectors.toList())

        then:
        thrown(VerifyException.class)

    }
}
