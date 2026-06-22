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
package org.febit.common.jooq.foo;

import lombok.experimental.UtilityClass;

@UtilityClass
class DDL {

    static final String CREATE_TABLE = """
            CREATE SCHEMA IF NOT EXISTS "test_h2";
            CREATE TABLE "test_h2"."foo"
            (
              "id"                     BIGINT AUTO_INCREMENT,
              "enabled"                BOOLEAN NULL,
              "name"                   VARCHAR(128)                             NOT NULL,
              "status"                 VARCHAR(32)                              NOT NULL,
              "description"            VARCHAR(255)                             NULL,
              "date"                   DATE                                     NULL,
              "time"                   TIME(3)                                  NULL,
              "timestamp"              TIMESTAMP(3)                             NULL,
              "json_varchar"           JSON                                     NULL,
              "json_text"              JSON                                     NULL,
              "string_json_bean"       VARCHAR(255)                             NULL,
              "string_json_bean_array" VARCHAR(255)                             NULL,
              "string_json_bean_list"  VARCHAR(255)                             NULL,
              "string_json_bean_map"   VARCHAR(255)                             NULL,
              "string_json_map"        VARCHAR(255)                             NULL,
              "created_by"             VARCHAR(128)                             NULL,
              "updated_by"             VARCHAR(128)                             NULL,
              "created_at"             TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
              "updated_at"             TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
              CONSTRAINT "pk_foo" PRIMARY KEY ("id")
            );
            CREATE INDEX IF NOT EXISTS "idx_foo_status" ON "test_h2"."foo" ("status");
            """;

    static final String DROP_TABLE = """
            DROP TABLE IF EXISTS "test_h2"."foo";
            """;
}
