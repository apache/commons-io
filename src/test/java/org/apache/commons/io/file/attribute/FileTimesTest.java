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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests {@link FileTimes}.
 */
public class FileTimesTest {

    public static Stream<Arguments> fileTimeNanoUnitsToNtfsProvider() {
        // @formatter:off
        return Stream.of(
            Arguments.of("1601-01-01T00:00:00.0000000Z", 0),
            Arguments.of("1601-01-01T00:00:00.0000001Z", 1),
            Arguments.of("1601-01-01T00:00:00.0000010Z", 10),
            Arguments.of("1601-01-01T00:00:00.0000100Z", 100),
            Arguments.of("1601-01-01T00:00:00.0001000Z", 1000),
            Arguments.of("1600-12-31T23:59:59.9999999Z", -1),
            Arguments.of("+30828-09-14T02:48:05.477580700Z", Long.MAX_VALUE),
            Arguments.of("+30828-09-14T02:48:05.477580600Z", Long.MAX_VALUE - 1),
            Arguments.of("+30828-09-14T02:48:05.477579700Z", Long.MAX_VALUE - 10),
            Arguments.of("+30828-09-14T02:48:05.477570700Z", Long.MAX_VALUE - 100),
            Arguments.of("+30828-09-14T02:48:05.477480700Z", Long.MAX_VALUE - 1000),
            Arguments.of("-27627-04-19T21:11:54.522419200Z", Long.MIN_VALUE),
            Arguments.of("-27627-04-19T21:11:54.522419300Z", Long.MIN_VALUE + 1),
            Arguments.of("-27627-04-19T21:11:54.522420200Z", Long.MIN_VALUE + 10),
            Arguments.of("-27627-04-19T21:11:54.522429200Z", Long.MIN_VALUE + 100),
            Arguments.of("-27627-04-19T21:11:54.522519200Z", Long.MIN_VALUE + 1000),
            Arguments.of("1601-01-01T00:00:00.0010000Z", FileTimes.HUNDRED_NANOS_PER_MILLISECOND),
            Arguments.of("1601-01-01T00:00:00.0010001Z", FileTimes.HUNDRED_NANOS_PER_MILLISECOND + 1),
            Arguments.of("1601-01-01T00:00:00.0009999Z", FileTimes.HUNDRED_NANOS_PER_MILLISECOND - 1),
            Arguments.of("1600-12-31T23:59:59.9990000Z", -FileTimes.HUNDRED_NANOS_PER_MILLISECOND),
            Arguments.of("1600-12-31T23:59:59.9990001Z", -FileTimes.HUNDRED_NANOS_PER_MILLISECOND + 1),
            Arguments.of("1600-12-31T23:59:59.9989999Z", -FileTimes.HUNDRED_NANOS_PER_MILLISECOND - 1),
            Arguments.of("1970-01-01T00:00:00.0000000Z", -FileTimes.UNIX_TO_NTFS_OFFSET),
            Arguments.of("1970-01-01T00:00:00.0000001Z", -FileTimes.UNIX_TO_NTFS_OFFSET + 1),
            Arguments.of("1970-01-01T00:00:00.0010000Z", -FileTimes.UNIX_TO_NTFS_OFFSET + FileTimes.HUNDRED_NANOS_PER_MILLISECOND),
            Arguments.of("1969-12-31T23:59:59.9999999Z", -FileTimes.UNIX_TO_NTFS_OFFSET - 1),
            Arguments.of("1969-12-31T23:59:59.9990000Z", -FileTimes.UNIX_TO_NTFS_OFFSET - FileTimes.HUNDRED_NANOS_PER_MILLISECOND));
        // @formatter:on
    }

    public static Stream<Arguments> fileTimeToNtfsProvider() {
        // @formatter:off
        return Stream.of(
            Arguments.of("1970-01-01T00:00:00Z", FileTime.from(Instant.EPOCH)),
            Arguments.of("1969-12-31T23:59:00Z", FileTime.from(Instant.EPOCH.minusSeconds(60))),
            Arguments.of("1970-01-01T00:01:00Z", FileTime.from(Instant.EPOCH.plusSeconds(60))));
        // @formatter:on
    }

    public static Stream<Arguments> isUnixFileTimeProvider() {
        // @formatter:off
        return Stream.of(
            Arguments.of("2022-12-27T12:45:22Z", true),
            Arguments.of("2038-01-19T03:14:07Z", true),
            Arguments.of("1901-12-13T23:14:08Z", true),
            Arguments.of("1901-12-13T03:14:08Z", false),
            Arguments.of("2038-01-19T03:14:08Z", false),
            Arguments.of("2099-06-30T12:31:42Z", false));
        // @formatter:on
    }

    @ParameterizedTest
    @MethodSource("fileTimeNanoUnitsToNtfsProvider")
    public void testDateToFileTime(final String instant, final long ignored) {
        final Instant parsedInstant = Instant.parse(instant);
        final FileTime parsedFileTime = FileTime.from(parsedInstant);
        final Date parsedDate = Date.from(parsedInstant);
        assertEquals(parsedFileTime.toMillis(), FileTimes.toFileTime(parsedDate).toMillis());
    }

    @ParameterizedTest
    @MethodSource("fileTimeNanoUnitsToNtfsProvider")
    public void testDateToNtfsTime(final String instantStr, final long ntfsTime) {
        final long ntfsMillis = Math.floorDiv(ntfsTime, FileTimes.HUNDRED_NANOS_PER_MILLISECOND) * FileTimes.HUNDRED_NANOS_PER_MILLISECOND;
        final Instant instant = Instant.parse(instantStr);
        final Date parsed = Date.from(instant);
        final long ntfsTime2 = FileTimes.toNtfsTime(parsed);
        if (ntfsTime2 == Long.MIN_VALUE || ntfsTime2 == Long.MAX_VALUE) {
            // toNtfsTime returns max long instead of overflowing
        } else {
            assertEquals(ntfsMillis, ntfsTime2);
            assertEquals(ntfsMillis, FileTimes.toNtfsTime(parsed.getTime()));
            assertEquals(ntfsMillis, FileTimes.toNtfsTime(FileTimes.ntfsTimeToInstant(ntfsTime).toEpochMilli()));
        }
        assertEquals(ntfsTime, FileTimes.toNtfsTime(FileTimes.ntfsTimeToInstant(ntfsTime)));
    }

    @Test
    public void testEpoch() {
        assertEquals(0, FileTimes.EPOCH.toMillis());
    }

    @ParameterizedTest
    @MethodSource("fileTimeNanoUnitsToNtfsProvider")
    public void testFileTimeToDate(final String instant, final long ignored) {
        final Instant parsedInstant = Instant.parse(instant);
        final FileTime parsedFileTime = FileTime.from(parsedInstant);
        final Date parsedDate = Date.from(parsedInstant);
        assertEquals(parsedDate, FileTimes.toDate(parsedFileTime));
    }

    //@Disabled
    @ParameterizedTest
    @MethodSource("fileTimeToNtfsProvider")
    public void testFileTimeToNtfsTime(final String instantStr, final FileTime fileTime) {
        final Instant instant = Instant.parse(instantStr);
        final FileTime parsed = FileTime.from(instant);
        assertEquals(instant, parsed.toInstant());
        assertEquals(fileTime, FileTimes.ntfsTimeToFileTime(FileTimes.toNtfsTime(parsed)));
    }

    @ParameterizedTest
    @MethodSource("fileTimeNanoUnitsToNtfsProvider")
    public void testFileTimeToNtfsTime(final String instant, final long ntfsTime) {
        final FileTime parsed = FileTime.from(Instant.parse(instant));
        assertEquals(ntfsTime, FileTimes.toNtfsTime(parsed));
    }

    @ParameterizedTest
    @MethodSource("fileTimeNanoUnitsToNtfsProvider")
    public void testFromUnixTime(final String instant, final long ntfsTime) {
        final long epochSecond = Instant.parse(instant).getEpochSecond();
        assertEquals(epochSecond, FileTimes.fromUnixTime(epochSecond).to(TimeUnit.SECONDS));
    }

    @ParameterizedTest
    @MethodSource("isUnixFileTimeProvider")
    public void testIsUnixTime(final String instant, final boolean isUnixTime) {
        assertEquals(isUnixTime, FileTimes.isUnixTime(FileTime.from(Instant.parse(instant))));
    }

    public void testIsUnixTimeFileTimeNull() {
        assertTrue(FileTimes.isUnixTime(null));
    }

    @ParameterizedTest
    @MethodSource("isUnixFileTimeProvider")
    public void testIsUnixTimeLong(final String instant, final boolean isUnixTime) {
        assertEquals(isUnixTime, FileTimes.isUnixTime(Instant.parse(instant).getEpochSecond()));
    }

    @Test
    public void testMaxJavaTime() {
        final long javaTime = Long.MAX_VALUE;
        final Instant instant = Instant.ofEpochMilli(javaTime);
        assertEquals(javaTime, instant.toEpochMilli()); // sanity check
        final long ntfsTime = FileTimes.toNtfsTime(javaTime);
        final Instant instant2 = FileTimes.ntfsTimeToInstant(ntfsTime);
        if (ntfsTime == Long.MAX_VALUE) {
            // toNtfsTime returns max long instead of overflowing
        } else {
            assertEquals(javaTime, instant2.toEpochMilli());
        }
    }

    @ParameterizedTest
    @MethodSource("fileTimeNanoUnitsToNtfsProvider")
    public void testMaxJavaTimeParam(final String instantStr, final long javaTime) {
        // final long javaTime = Long.MAX_VALUE;
        final Instant instant = Instant.ofEpochMilli(javaTime);
        assertEquals(javaTime, instant.toEpochMilli()); // sanity check
        final long ntfsTime = FileTimes.toNtfsTime(javaTime);
        final Instant instant2 = FileTimes.ntfsTimeToInstant(ntfsTime);
        if (ntfsTime == Long.MIN_VALUE || ntfsTime == Long.MAX_VALUE) {
            // toNtfsTime returns min or max long instead of overflowing
        } else {
            assertEquals(javaTime, instant2.toEpochMilli());
        }
    }

    @Test
    public void testMinusMillis() {
        final int millis = 2;
        assertEquals(Instant.EPOCH.minusMillis(millis), FileTimes.minusMillis(FileTimes.EPOCH, millis).toInstant());
        assertEquals(Instant.EPOCH, FileTimes.minusMillis(FileTimes.EPOCH, 0).toInstant());
    }

    @Test
    public void testMinusNanos() {
        final int millis = 2;
        assertEquals(Instant.EPOCH.minusNanos(millis), FileTimes.minusNanos(FileTimes.EPOCH, millis).toInstant());
        assertEquals(Instant.EPOCH, FileTimes.minusNanos(FileTimes.EPOCH, 0).toInstant());
    }

    @Test
    public void testMinusSeconds() {
        final int seconds = 2;
        assertEquals(Instant.EPOCH.minusSeconds(seconds), FileTimes.minusSeconds(FileTimes.EPOCH, seconds).toInstant());
        assertEquals(Instant.EPOCH, FileTimes.minusSeconds(FileTimes.EPOCH, 0).toInstant());
    }

    @ParameterizedTest
    @MethodSource("fileTimeNanoUnitsToNtfsProvider")
    public void testNtfsTimeToDate(final String instant, final long ntfsTime) {
        assertEquals(Instant.parse(instant).toEpochMilli(), FileTimes.ntfsTimeToDate(ntfsTime).toInstant().toEpochMilli());
    }

    @ParameterizedTest
    @MethodSource("fileTimeNanoUnitsToNtfsProvider")
    public void testNtfsTimeToFileTime(final String instantStr, final long ntfsTime) {
        final Instant instant = Instant.parse(instantStr);
        final FileTime fileTime = FileTime.from(instant);
        assertEquals(instant, fileTime.toInstant()); // sanity check
        assertEquals(instant, FileTimes.ntfsTimeToInstant(ntfsTime));
        assertEquals(fileTime, FileTimes.ntfsTimeToFileTime(ntfsTime));
    }

    @Test
    public void testNullDateToNullFileTime() {
        assertNull(FileTimes.toFileTime(null));
    }

    @Test
    public void testNullFileTimeToNullDate() {
        assertNull(FileTimes.toDate(null));
    }

    @Test
    public void testPlusMinusMillis() {
        final int millis = 2;
        assertEquals(Instant.EPOCH.plusMillis(millis), FileTimes.plusMillis(FileTimes.EPOCH, millis).toInstant());
        assertEquals(Instant.EPOCH, FileTimes.plusMillis(FileTimes.EPOCH, 0).toInstant());
    }

    @Test
    public void testPlusNanos() {
        final int millis = 2;
        assertEquals(Instant.EPOCH.plusNanos(millis), FileTimes.plusNanos(FileTimes.EPOCH, millis).toInstant());
        assertEquals(Instant.EPOCH, FileTimes.plusNanos(FileTimes.EPOCH, 0).toInstant());
    }

    @Test
    public void testPlusSeconds() {
        final int seconds = 2;
        assertEquals(Instant.EPOCH.plusSeconds(seconds), FileTimes.plusSeconds(FileTimes.EPOCH, seconds).toInstant());
        assertEquals(Instant.EPOCH, FileTimes.plusSeconds(FileTimes.EPOCH, 0).toInstant());
    }

    @ParameterizedTest
    @MethodSource("isUnixFileTimeProvider")
    public void testToUnixTime(final String instant, final boolean isUnixTime) {
        assertEquals(isUnixTime, FileTimes.isUnixTime(FileTimes.toUnixTime(FileTime.from(Instant.parse(instant)))));
    }
}
