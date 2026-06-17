/*
 * Copyright 2013-present febit.org (support@febit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.febit.common.etcd.support;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.DockerClientFactory;

public final class DockerAvailableCondition implements ExecutionCondition {

    @Override
    public @NonNull ConditionEvaluationResult evaluateExecutionCondition(@NonNull ExtensionContext context) {
        try {
            return DockerClientFactory.instance().isDockerAvailable()
                    ? ConditionEvaluationResult.enabled("Docker is available")
                    : ConditionEvaluationResult.disabled("Docker is not available for jetcd-test");
        } catch (Throwable ex) {
            return ConditionEvaluationResult.disabled("Docker is not available for jetcd-test: " + ex.getMessage());
        }
    }
}
