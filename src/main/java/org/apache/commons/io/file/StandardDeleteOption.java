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

import org.apache.commons.io.IOUtils;

/**
 * Defines the standard delete options.
 *
 * @since 2.8.0
 */
public enum StandardDeleteOption implements DeleteOption {

    /**
     * Overrides the read-only attribute to allow deletion, on POSIX, this means Write and Execute on the parent.
     */
    OVERRIDE_READ_ONLY;

    /**
     * Returns true if the given options contain {@link StandardDeleteOption#OVERRIDE_READ_ONLY}.
     *
     * For now, assume the array is not sorted.
     *
     * @param options the array to test
     * @return true if the given options contain {@link StandardDeleteOption#OVERRIDE_READ_ONLY}.
     */
    public static boolean overrideReadOnly(final DeleteOption[] options) {
        if (IOUtils.length(options) == 0) {
            return false;
        }
        return Stream.of(options).anyMatch(e -> OVERRIDE_READ_ONLY == e);
    }

}
