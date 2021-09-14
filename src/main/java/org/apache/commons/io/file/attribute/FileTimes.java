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

package org.apache.commons.io.file.attribute;

import java.nio.file.attribute.FileTime;
import java.time.Instant;

/**
 * Helps use {@link FileTime}.
 *
 * @since 2.12.0
 */
public class FileTimes {

    /**
     * Constant for the {@code 1970-01-01T00:00:00Z} epoch time stamp attribute.
     *
     * @see Instant#EPOCH
     */
    public static final FileTime EPOCH = FileTime.from(Instant.EPOCH);

    /**
     * Subtracts milliseconds from a source FileTime.
     *
     * @param fileTime The source FileTime.
     * @param millisToSubtract The milliseconds to subtract.
     * @return The resulting FileTime.
     */
    public static FileTime minusMillis(final FileTime fileTime, final long millisToSubtract) {
        return FileTime.from(fileTime.toInstant().minusMillis(millisToSubtract));
    }

    /**
     * Subtracts nanoseconds from a source FileTime.
     *
     * @param fileTime The source FileTime.
     * @param nanosToSubtract The nanoseconds to subtract.
     * @return The resulting FileTime.
     */
    public static FileTime minusNanos(final FileTime fileTime, final long nanosToSubtract) {
        return FileTime.from(fileTime.toInstant().minusNanos(nanosToSubtract));
    }

    /**
     * Subtracts seconds from a source FileTime.
     *
     * @param fileTime The source FileTime.
     * @param secondsToSubtract The seconds to subtract.
     * @return The resulting FileTime.
     */
    public static FileTime minusSeconds(final FileTime fileTime, final long secondsToSubtract) {
        return FileTime.from(fileTime.toInstant().minusSeconds(secondsToSubtract));
    }

    /**
     * Adds milliseconds to a source FileTime.
     *
     * @param fileTime The source FileTime.
     * @param millisToAdd The milliseconds to add.
     * @return The resulting FileTime.
     */
    public static FileTime plusMillis(final FileTime fileTime, final long millisToAdd) {
        return FileTime.from(fileTime.toInstant().plusMillis(millisToAdd));
    }

    /**
     * Adds nanoseconds from a source FileTime.
     *
     * @param fileTime The source FileTime.
     * @param nanosToSubtract The nanoseconds to subtract.
     * @return The resulting FileTime.
     */
    public static FileTime plusNanos(final FileTime fileTime, final long nanosToSubtract) {
        return FileTime.from(fileTime.toInstant().plusNanos(nanosToSubtract));
    }

    /**
     * Adds seconds to a source FileTime.
     *
     * @param fileTime The source FileTime.
     * @param secondsToAdd The seconds to add.
     * @return The resulting FileTime.
     */
    public static FileTime plusSeconds(final FileTime fileTime, final long secondsToAdd) {
        return FileTime.from(fileTime.toInstant().plusSeconds(secondsToAdd));
    }

    private FileTimes() {
        // No instances.
    }
}
