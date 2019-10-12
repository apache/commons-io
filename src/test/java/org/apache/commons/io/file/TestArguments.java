/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.io.file;

import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;

class TestArguments {

    static Stream<Arguments> cleaningPathVisitors() {
        // @formatter:off
        return Stream.of(
          Arguments.of(CleaningPathVisitor.withBigIntegerCounters()),
          Arguments.of(CleaningPathVisitor.withLongCounters()));
        // @formatter:on
    }

    static Stream<Arguments> countingPathVisitors() {
        // @formatter:off
        return Stream.of(
          Arguments.of(CountingPathVisitor.withBigIntegerCounters()),
          Arguments.of(CountingPathVisitor.withLongCounters()));
        // @formatter:on
    }

    static Stream<Arguments> deletingPathVisitors() {
        // @formatter:off
        return Stream.of(
          Arguments.of(DeletingPathVisitor.withBigIntegerCounters()),
          Arguments.of(DeletingPathVisitor.withLongCounters()));
        // @formatter:on
    }

    static Stream<Arguments> numberCounters() {
        // @formatter:off
        return Stream.of(
          Arguments.of(Counters.longCounter()),
          Arguments.of(Counters.bigIntegerCounter()));
        // @formatter:on
    }

    static Stream<Arguments> pathCounters() {
        // @formatter:off
        return Stream.of(
          Arguments.of(Counters.longPathCounters()),
          Arguments.of(Counters.bigIntegerPathCounters()));
        // @formatter:on
    }
}
