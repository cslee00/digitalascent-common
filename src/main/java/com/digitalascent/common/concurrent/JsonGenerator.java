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
import com.google.common.base.Joiner;
import com.google.common.escape.CharEscaperBuilder;
import com.google.common.escape.Escaper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Internal utility class to output map as a json representation; light-weight to avoid pulling in an external dependency
 * on a JSON generator.
 */
final class JsonGenerator {

    private static final Escaper JSON_ESCAPER = new CharEscaperBuilder().addEscape('"', "\\\"").addEscape('\\', "\\\\").toEscaper();

    static String mapToJson(Map<String, Object> map) {
        Map<String, String> jsonProperties = new LinkedHashMap<>();

        map.forEach((key, value) -> {
            String escapedKey = '"' + JSON_ESCAPER.escape(key) + '"';
            String escapedValue = '"' + Optional.ofNullable(value).map(v -> JSON_ESCAPER.escape(v.toString())).orElse("null") + '"';
            jsonProperties.put(escapedKey, escapedValue);
        });
        Joiner.MapJoiner joiner = Joiner.on(',').withKeyValueSeparator(':');
        return '{' + joiner.join(jsonProperties) + '}';
    }

    private JsonGenerator() {
        StaticUtilityClass.throwCannotInstantiateError(getClass());
    }
}
