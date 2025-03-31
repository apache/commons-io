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
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Helps use {@link FileTime} and interoperate Date and NTFS times.
 * <p>
 * An NTFS file time is a 64-bit value that represents the number of 100-nanosecond intervals that have elapsed since 12:00 A.M. January 1, 1601 Coordinated
 * Universal Time (UTC). This is the offset of Windows time 0 to Unix epoch in 100-nanosecond intervals.
 * </p>
 *
 * @since 2.12.0
 * @see <a href="https://learn.microsoft.com/en-us/windows/win32/sysinfo/file-times">NTFS File Times</a>
 */
public final class FileTimes {

    private static final BigDecimal LONG_MIN_VALUE_BD = BigDecimal.valueOf(Long.MIN_VALUE);

    private static final BigDecimal LONG_MAX_VALUE_BD = BigDecimal.valueOf(Long.MAX_VALUE);

    private static final MathContext MATH_CONTEXT = new MathContext(0, RoundingMode.FLOOR);

    /**
     * Constant for the {@code 1970-01-01T00:00:00Z} {@link Instant#EPOCH epoch} as a time stamp attribute.
     *
     * @see Instant#EPOCH
     */
    public static final FileTime EPOCH = FileTime.from(Instant.EPOCH);

    /**
     * The offset of Windows time 0 to Unix epoch in 100-nanosecond intervals.
     *
     * <a href="https://msdn.microsoft.com/en-us/library/windows/desktop/ms724290%28v=vs.85%29.aspx">Windows File Times</a>
     * <p>
     * A file time is a 64-bit value that represents the number of 100-nanosecond intervals that have elapsed since 12:00 A.M. January 1, 1601 Coordinated
     * Universal Time (UTC). This is the offset of Windows time 0 to Unix epoch in 100-nanosecond intervals.
     * </p>
     */
    static final long UNIX_TO_NTFS_OFFSET = -116444736000000000L;

    private static final BigDecimal UNIX_TO_NTFS_OFFSET_BD = BigDecimal.valueOf(UNIX_TO_NTFS_OFFSET);

    /**
     * The amount of 100-nanosecond intervals in one second.
     */
    private static final long HUNDRED_NANOS_PER_SECOND = TimeUnit.SECONDS.toNanos(1) / 100;

    private static final BigDecimal HUNDRED_NANOS_PER_SECOND_BD = BigDecimal.valueOf(HUNDRED_NANOS_PER_SECOND);

    /**
     * The amount of 100-nanosecond intervals in one millisecond.
     */
    static final long HUNDRED_NANOS_PER_MILLISECOND = TimeUnit.MILLISECONDS.toNanos(1) / 100;

    static final BigDecimal HUNDRED_NANOS_PER_MILLISECOND_BD = BigDecimal.valueOf(HUNDRED_NANOS_PER_MILLISECOND);

    private static final long HUNDRED = 100L;

    private static final BigDecimal HUNDRED_BD = BigDecimal.valueOf(HUNDRED);

    /**
     * Converts standard Unix time (in seconds, UTC/GMT) to {@link FileTime}.
     *
     * @param time Unix timestamp (seconds).
     * @return the corresponding FileTime.
     * @since 2.16.0
     */
    public static FileTime fromUnixTime(final long time) {
        return FileTime.from(time, TimeUnit.SECONDS);
    }

    /**
     * Tests whether a FileTime can be safely represented in the standard Unix time.
     * <p>
     * If the FileTime is null, this method returns true.
     * </p>
     *
     * @param time the FileTime to evaluate, can be null.
     * @return true if the time exceeds the minimum or maximum Unix time, false otherwise.
     * @since 2.16.0
     */
    public static boolean isUnixTime(final FileTime time) {
        return isUnixTime(toUnixTime(time));
    }

    /**
     * Tests whether a given number of seconds (since Epoch) can be safely represented in the standard Unix time.
     *
     * @param seconds the number of seconds (since Epoch) to evaluate.
     * @return true if the time can be represented in the standard Unix time, false otherwise.
     * @since 2.16.0
     */
    public static boolean isUnixTime(final long seconds) {
        return Integer.MIN_VALUE <= seconds && seconds <= Integer.MAX_VALUE;
    }

    /**
     * Subtracts milliseconds from a source FileTime.
     *
     * @param fileTime         The source FileTime.
     * @param millisToSubtract The milliseconds to subtract.
     * @return The resulting FileTime.
     */
    public static FileTime minusMillis(final FileTime fileTime, final long millisToSubtract) {
        return FileTime.from(fileTime.toInstant().minusMillis(millisToSubtract));
    }

    /**
     * Subtracts nanoseconds from a source FileTime.
     *
     * @param fileTime        The source FileTime.
     * @param nanosToSubtract The nanoseconds to subtract.
     * @return The resulting FileTime.
     */
    public static FileTime minusNanos(final FileTime fileTime, final long nanosToSubtract) {
        return FileTime.from(fileTime.toInstant().minusNanos(nanosToSubtract));
    }

    /**
     * Subtracts seconds from a source FileTime.
     *
     * @param fileTime          The source FileTime.
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

    static Date ntfsTimeToDate(final BigDecimal ntfsTime) {
        return new Date(ntfsTimeToInstant(ntfsTime).toEpochMilli());
    }

    /**
     * Converts an NTFS time (100 nanosecond units since 1 January 1601) to a {@link Date}.
     * <p>
     * An NTFS file time is a 64-bit value for the number of 100-nanosecond intervals since 12:00 A.M. January 1, 1601 Coordinated Universal Time (UTC).
     * </p>
     *
     * @param ntfsTime the NTFS time, 100-nanosecond units since 1 January 1601.
     * @return the Date input.
     * @see <a href="https://learn.microsoft.com/en-us/windows/win32/sysinfo/file-times">NTFS File Times</a>
     */
    public static Date ntfsTimeToDate(final long ntfsTime) {
        return ntfsTimeToDate(BigDecimal.valueOf(ntfsTime));
    }

    /**
     * Converts an NTFS time (100-nanosecond units since 1 January 1601) to a {@link FileTime}.
     * <p>
     * An NTFS file time is a 64-bit value for the number of 100-nanosecond intervals since 12:00 A.M. January 1, 1601 Coordinated Universal Time (UTC).
     * </p>
     *
     * @param ntfsTime the NTFS time, 100-nanosecond units since 1 January 1601.
     * @return the FileTime input.
     * @see #toNtfsTime(FileTime)
     * @see <a href="https://learn.microsoft.com/en-us/windows/win32/sysinfo/file-times">NTFS File Times</a>
     */
    public static FileTime ntfsTimeToFileTime(final long ntfsTime) {
        return FileTime.from(ntfsTimeToInstant(ntfsTime));
    }

    static Instant ntfsTimeToInstant(final BigDecimal ntfsTime) {
        final BigDecimal javaHundredsNanos = ntfsTime.add(UNIX_TO_NTFS_OFFSET_BD);
        final BigDecimal[] dar = javaHundredsNanos.divideAndRemainder(HUNDRED_NANOS_PER_SECOND_BD, MATH_CONTEXT);
        return Instant.ofEpochSecond(dar[0].longValueExact(), dar[1].multiply(HUNDRED_BD).longValueExact());
    }

    static Instant ntfsTimeToInstant(final long ntfsTime) {
        return ntfsTimeToInstant(BigDecimal.valueOf(ntfsTime));
    }

    /**
     * Adds milliseconds to a {@link FileTime}.
     *
     * @param fileTime    The source FileTime.
     * @param millisToAdd The milliseconds to add.
     * @return The resulting FileTime.
     */
    public static FileTime plusMillis(final FileTime fileTime, final long millisToAdd) {
        return FileTime.from(fileTime.toInstant().plusMillis(millisToAdd));
    }

    /**
     * Adds nanoseconds to a {@link FileTime}.
     *
     * @param fileTime        The source FileTime.
     * @param nanosToSubtract The nanoseconds to subtract.
     * @return The resulting FileTime.
     */
    public static FileTime plusNanos(final FileTime fileTime, final long nanosToSubtract) {
        return FileTime.from(fileTime.toInstant().plusNanos(nanosToSubtract));
    }

    /**
     * Adds seconds to a {@link FileTime}.
     *
     * @param fileTime     The source FileTime.
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
     * Converts a {@link FileTime} to a {@link Date}. If the provided FileTime is {@code null}, the returned Date is also {@code null}.
     *
     * @param fileTime the file time to be converted.
     * @return a {@link Date} which corresponds to the supplied time, or {@code null} if the time is {@code null}.
     * @see #toFileTime(Date)
     */
    public static Date toDate(final FileTime fileTime) {
        return fileTime != null ? new Date(fileTime.toMillis()) : null;
    }

    /**
     * Converts a {@link Date} to a {@link FileTime}. If the provided Date is {@code null}, the returned FileTime is also {@code null}.
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
     * <p>
     * An NTFS file time is a 64-bit value for the number of 100-nanosecond intervals since 12:00 A.M. January 1, 1601 Coordinated Universal Time (UTC).
     * </p>
     *
     * @param date the Date input.
     * @return the NTFS time, 100-nanosecond units since 1 January 1601.
     */
    public static long toNtfsTime(final Date date) {
        return toNtfsTime(date.getTime());
    }

    /**
     * Converts a {@link FileTime} to NTFS time (100-nanosecond units since 1 January 1601).
     * <p>
     * An NTFS file time is a 64-bit value for the number of 100-nanosecond intervals since 12:00 A.M. January 1, 1601 Coordinated Universal Time (UTC).
     * </p>
     *
     * @param fileTime the FileTime input.
     * @return the NTFS time, 100-nanosecond units since 1 January 1601.
     */
    public static long toNtfsTime(final FileTime fileTime) {
        return toNtfsTime(fileTime.toInstant());
    }

    static long toNtfsTime(final Instant instant) {
        final BigDecimal javaHundredNanos = BigDecimal.valueOf(instant.getEpochSecond()).multiply(HUNDRED_NANOS_PER_SECOND_BD)
                .add(BigDecimal.valueOf(instant.getNano() / 100));
        return javaHundredNanos.subtract(UNIX_TO_NTFS_OFFSET_BD).longValueExact();
    }

    /**
     * Converts a Java time (milliseconds since Epoch) to NTFS time.
     * <p>
     * An NTFS file time is a 64-bit value for the number of 100-nanosecond intervals since 12:00 A.M. January 1, 1601 Coordinated Universal Time (UTC).
     * </p>
     *
     * @param javaTime the Java time
     * @return the NTFS time, 100-nanosecond units since 1 January 1601.
     * @since 2.16.0
     */
    public static long toNtfsTime(final long javaTime) {
        final BigDecimal javaHundredNanos = BigDecimal.valueOf(javaTime).multiply(HUNDRED_NANOS_PER_MILLISECOND_BD);
        final BigDecimal ntfsTime = javaHundredNanos.subtract(UNIX_TO_NTFS_OFFSET_BD);
        if (ntfsTime.compareTo(LONG_MAX_VALUE_BD) >= 0) {
            return Long.MAX_VALUE;
        }
        if (ntfsTime.compareTo(LONG_MIN_VALUE_BD) <= 0) {
            return Long.MIN_VALUE;
        }
        return ntfsTime.longValue();
    }

    /**
     * Converts a {@link FileTime} to standard Unix time in seconds.
     * <p>
     * The returned seconds value may lie out of bounds of Unix time. Check with {@link FileTimes#isUnixTime(long)}.
     * </p>
     *
     * @param fileTime the original FileTime.
     * @return the Unix timestamp or 0 if the input is null.
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
