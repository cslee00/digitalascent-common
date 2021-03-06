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


import javax.annotation.Nullable;

@FunctionalInterface
public interface BatchSupplier<T> {
    /**
     * Retrieve next batch of items
     * <p>
     * Example usage:
     * <pre>
     * return ExtraStreams.batchLoadingStream(nextBatchToken -> {
     *      describeParametersRequest.setNextToken(nextBatchToken);
     *      DescribeParametersResult result = ssm.describeParameters(describeParametersRequest);
     *      return new Batch<>( result.getNextToken(),result.getParameters() );
     * });
     * </pre>
     *
     * @param nextBatchToken Token to request next batch of data (may be null)
     * @return Batch object representing retrieved batch; use Batch.emptyBatch() to represent empty batch; must not be null
     */
    Batch<T> nextBatch(@Nullable String nextBatchToken);
}
