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

package com.digitalascent.common.concurrent

import groovy.json.JsonSlurper
import spock.lang.Specification

import java.util.concurrent.Callable

class ThreadsTest extends Specification {

    def "invokeWithThreadContext renames thread properly"() {
        when:
        def threadName = ExtraThreads.invokeWithThreadContext(["a":"b"], (Callable){ -> return Thread.currentThread().getName()})
        def response = new JsonSlurper().parseText(threadName)

        then:
        threadName != null
        response["timestamp"]  != null
        response["originalThreadName"] == "Test worker"
        response["a"] == "b"
        response.keySet() == ["timestamp","a","originalThreadName"] as Set
    }
}
