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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Helps use {@link FileTime} and interoperate Date and NTFS times.
 *
 * @since 2.12.0
 */
public final class FileTimes {

    /**
     * Constant for the {@code 1970-01-01T00:00:00Z} {@link Instant#EPOCH epoch} as a time stamp attribute.
     *
     * @see Instant#EPOCH
     */
    public static final FileTime EPOCH = FileTime.from(Instant.EPOCH);

    /**
     * The offset of Windows time 0 to UNIX epoch in 100-nanosecond intervals.
     *
     * <a href="https://msdn.microsoft.com/en-us/library/windows/desktop/ms724290%28v=vs.85%29.aspx">Windows File Times</a>
     * <p>
     * A file time is a 64-bit value that represents the number of 100-nanosecond intervals that have elapsed since 12:00
     * A.M. January 1, 1601 Coordinated Universal Time (UTC). This is the offset of Windows time 0 to UNIX epoch in
     * 100-nanosecond intervals.
     * </p>
     */
    static final long WINDOWS_EPOCH_OFFSET = -116444736000000000L;

    /**
     * The amount of 100-nanosecond intervals in one second.
     */
    private static final long HUNDRED_NANOS_PER_SECOND = TimeUnit.SECONDS.toNanos(1) / 100;

    /**
     * The amount of 100-nanosecond intervals in one millisecond.
     */
    static final long HUNDRED_NANOS_PER_MILLISECOND = TimeUnit.MILLISECONDS.toNanos(1) / 100;

    /**
     * Converts standard UNIX time (in seconds, UTC/GMT) to {@link FileTime}.
     *
     * @param time UNIX timestamp (seconds).
     * @return the corresponding FileTime.
     * @since 2.16.0
     */
    public static FileTime fromUnixTime(final long time) {
        return FileTime.from(time, TimeUnit.SECONDS);
    }

    /**
     * Tests whether a FileTime can be safely represented in the standard UNIX time.
     * <p>
     * If the FileTime is null, this method returns true.
     * </p>
     *
     * @param time the FileTime to evaluate, can be null.
     * @return true if the time exceeds the minimum or maximum UNIX time, false otherwise.
     * @since 2.16.0
     */
    public static boolean isUnixTime(final FileTime time) {
        return isUnixTime(toUnixTime(time));
    }

    /**
     * Tests whether a given number of seconds (since Epoch) can be safely represented in the standard UNIX time.
     *
     * @param seconds the number of seconds (since Epoch) to evaluate.
     * @return true if the time can be represented in the standard UNIX time, false otherwise.
     * @since 2.16.0
     */
    public static boolean isUnixTime(final long seconds) {
        return Integer.MIN_VALUE <= seconds && seconds <= Integer.MAX_VALUE;
    }


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
     * Obtains the current instant FileTime from the system clock.
     *
     * @return the current instant FileTime from the system clock.
     */
    public static FileTime now() {
        return FileTime.from(Instant.now());
    }

    /**
     * Converts NTFS time (100 nanosecond units since 1 January 1601) to Java time.
     *
     * @param ntfsTime the NTFS time in 100 nanosecond units
     * @return the Date
     */
    public static Date ntfsTimeToDate(final long ntfsTime) {
        final long javaHundredNanos = Math.addExact(ntfsTime, WINDOWS_EPOCH_OFFSET);
        final long javaMillis = Math.floorDiv(javaHundredNanos, HUNDRED_NANOS_PER_MILLISECOND);
        return new Date(javaMillis);
    }

    /**
     * Converts NTFS time (100-nanosecond units since 1 January 1601) to a FileTime.
     *
     * @param ntfsTime the NTFS time in 100-nanosecond units
     * @return the FileTime
     *
     * @see #toNtfsTime(FileTime)
     */
    public static FileTime ntfsTimeToFileTime(final long ntfsTime) {
        final long javaHundredsNanos = Math.addExact(ntfsTime, WINDOWS_EPOCH_OFFSET);
        final long javaSeconds = Math.floorDiv(javaHundredsNanos, HUNDRED_NANOS_PER_SECOND);
        final long javaNanos = Math.floorMod(javaHundredsNanos, HUNDRED_NANOS_PER_SECOND) * 100;
        return FileTime.from(Instant.ofEpochSecond(javaSeconds, javaNanos));
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

    /**
     * Sets the last modified time of the given file path to now.
     *
     * @param path The file path to set.
     * @throws IOException if an I/O error occurs.
     */
    public static void setLastModifiedTime(final Path path) throws IOException {
        Files.setLastModifiedTime(path, now());
    }

    /**
     * Converts {@link FileTime} to a {@link Date}. If the provided FileTime is {@code null}, the returned Date is also
     * {@code null}.
     *
     * @param fileTime the file time to be converted.
     * @return a {@link Date} which corresponds to the supplied time, or {@code null} if the time is {@code null}.
     * @see #toFileTime(Date)
     */
    public static Date toDate(final FileTime fileTime) {
        return fileTime != null ? new Date(fileTime.toMillis()) : null;
    }

    /**
     * Converts {@link Date} to a {@link FileTime}. If the provided Date is {@code null}, the returned FileTime is also
     * {@code null}.
     *
     * @param date the date to be converted.
     * @return a {@link FileTime} which corresponds to the supplied date, or {@code null} if the date is {@code null}.
     * @see #toDate(FileTime)
     */
    public static FileTime toFileTime(final Date date) {
        return date != null ? FileTime.fromMillis(date.getTime()) : null;
    }

    /**
     * Converts a {@link Date} to NTFS time.
     *
     * @param date the Date
     * @return the NTFS time
     */
    public static long toNtfsTime(final Date date) {
        final long javaHundredNanos = date.getTime() * HUNDRED_NANOS_PER_MILLISECOND;
        return Math.subtractExact(javaHundredNanos, WINDOWS_EPOCH_OFFSET);
    }

    /**
     * Converts a {@link FileTime} to NTFS time (100-nanosecond units since 1 January 1601).
     *
     * @param fileTime the FileTime
     * @return the NTFS time in 100-nanosecond units
     */
    public static long toNtfsTime(final FileTime fileTime) {
        final Instant instant = fileTime.toInstant();
        final long javaHundredNanos = instant.getEpochSecond() * HUNDRED_NANOS_PER_SECOND + instant.getNano() / 100;
        return Math.subtractExact(javaHundredNanos, WINDOWS_EPOCH_OFFSET);
    }

    /**
     * Converts Java time (milliseconds since Epoch) to NTFS time.
     *
     * @param javaTime the Java time
     * @return the NTFS time
     * @since 2.16.0
     */
    public static long toNtfsTime(final long javaTime) {
        final long javaHundredNanos = javaTime * HUNDRED_NANOS_PER_MILLISECOND;
        return Math.subtractExact(javaHundredNanos, WINDOWS_EPOCH_OFFSET);
    }

    /**
     * Converts {@link FileTime} to standard UNIX time in seconds.
     * <p>
     * The returned seconds value may lie out of bounds of UNIX time. Check with {@link FileTimes#isUnixTime(long)}.
     * </p>
     *
     * @param fileTime the original FileTime.
     * @return the UNIX timestamp or 0 if the input is null.
     * @see #isUnixTime(long)
     * @since 2.16.0
     */
    public static long toUnixTime(final FileTime fileTime) {
        return fileTime != null ? fileTime.to(TimeUnit.SECONDS) : 0;
    }

    private FileTimes() {
        // No instances.
    }
}
