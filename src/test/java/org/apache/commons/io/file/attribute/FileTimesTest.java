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

    public static Stream<Arguments> dateToNtfsProvider() {
        // @formatter:off
        return Stream.of(
            Arguments.of("1601-01-01T00:00:00.000Z", 0),
            Arguments.of("1601-01-01T00:00:00.000Z", 1),
            Arguments.of("1600-12-31T23:59:59.999Z", -1),
            Arguments.of("1601-01-01T00:00:00.001Z", FileTimes.HUNDRED_NANOS_PER_MILLISECOND),
            Arguments.of("1601-01-01T00:00:00.001Z", FileTimes.HUNDRED_NANOS_PER_MILLISECOND + 1),
            Arguments.of("1601-01-01T00:00:00.000Z", FileTimes.HUNDRED_NANOS_PER_MILLISECOND - 1),
            Arguments.of("1600-12-31T23:59:59.999Z", -FileTimes.HUNDRED_NANOS_PER_MILLISECOND),
            Arguments.of("1600-12-31T23:59:59.999Z", -FileTimes.HUNDRED_NANOS_PER_MILLISECOND + 1),
            Arguments.of("1600-12-31T23:59:59.998Z", -FileTimes.HUNDRED_NANOS_PER_MILLISECOND - 1),
            Arguments.of("1970-01-01T00:00:00.000Z", -FileTimes.WINDOWS_EPOCH_OFFSET),
            Arguments.of("1970-01-01T00:00:00.000Z", -FileTimes.WINDOWS_EPOCH_OFFSET + 1),
            Arguments.of("1970-01-01T00:00:00.001Z", -FileTimes.WINDOWS_EPOCH_OFFSET + FileTimes.HUNDRED_NANOS_PER_MILLISECOND),
            Arguments.of("1969-12-31T23:59:59.999Z", -FileTimes.WINDOWS_EPOCH_OFFSET - 1),
            Arguments.of("1969-12-31T23:59:59.999Z", -FileTimes.WINDOWS_EPOCH_OFFSET - FileTimes.HUNDRED_NANOS_PER_MILLISECOND));
        // @formatter:on
    }

    public static Stream<Arguments> fileTimeToNtfsProvider() {
        // @formatter:off
        return Stream.of(
            Arguments.of("1601-01-01T00:00:00.0000000Z", 0),
            Arguments.of("1601-01-01T00:00:00.0000001Z", 1),
            Arguments.of("1600-12-31T23:59:59.9999999Z", -1),
            Arguments.of("1601-01-01T00:00:00.0010000Z", FileTimes.HUNDRED_NANOS_PER_MILLISECOND),
            Arguments.of("1601-01-01T00:00:00.0010001Z", FileTimes.HUNDRED_NANOS_PER_MILLISECOND + 1),
            Arguments.of("1601-01-01T00:00:00.0009999Z", FileTimes.HUNDRED_NANOS_PER_MILLISECOND - 1),
            Arguments.of("1600-12-31T23:59:59.9990000Z", -FileTimes.HUNDRED_NANOS_PER_MILLISECOND),
            Arguments.of("1600-12-31T23:59:59.9990001Z", -FileTimes.HUNDRED_NANOS_PER_MILLISECOND + 1),
            Arguments.of("1600-12-31T23:59:59.9989999Z", -FileTimes.HUNDRED_NANOS_PER_MILLISECOND - 1),
            Arguments.of("1970-01-01T00:00:00.0000000Z", -FileTimes.WINDOWS_EPOCH_OFFSET),
            Arguments.of("1970-01-01T00:00:00.0000001Z", -FileTimes.WINDOWS_EPOCH_OFFSET + 1),
            Arguments.of("1970-01-01T00:00:00.0010000Z", -FileTimes.WINDOWS_EPOCH_OFFSET + FileTimes.HUNDRED_NANOS_PER_MILLISECOND),
            Arguments.of("1969-12-31T23:59:59.9999999Z", -FileTimes.WINDOWS_EPOCH_OFFSET - 1),
            Arguments.of("1969-12-31T23:59:59.9990000Z", -FileTimes.WINDOWS_EPOCH_OFFSET - FileTimes.HUNDRED_NANOS_PER_MILLISECOND));
        // @formatter:on
    }

    @ParameterizedTest
    @MethodSource("dateToNtfsProvider")
    public void testDateToFileTime(final String instant, final long ignored) {
        final Instant parsedInstant = Instant.parse(instant);
        final FileTime parsedFileTime = FileTime.from(parsedInstant);
        final Date parsedDate = Date.from(parsedInstant);
        assertEquals(parsedFileTime, FileTimes.toFileTime(parsedDate));
    }

    @ParameterizedTest
    @MethodSource("dateToNtfsProvider")
    public void testDateToNtfsTime(final String instant, final long ntfsTime) {
        final long ntfsMillis = Math.floorDiv(ntfsTime, FileTimes.HUNDRED_NANOS_PER_MILLISECOND) * FileTimes.HUNDRED_NANOS_PER_MILLISECOND;
        final Date parsed = Date.from(Instant.parse(instant));
        assertEquals(ntfsMillis, FileTimes.toNtfsTime(parsed));
        assertEquals(ntfsMillis, FileTimes.toNtfsTime(parsed.getTime()));
    }

    @Test
    public void testEpoch() {
        assertEquals(0, FileTimes.EPOCH.toMillis());
    }

    @ParameterizedTest
    @MethodSource("fileTimeToNtfsProvider")
    public void testFileTimeToDate(final String instant, final long ignored) {
        final Instant parsedInstant = Instant.parse(instant);
        final FileTime parsedFileTime = FileTime.from(parsedInstant);
        final Date parsedDate = Date.from(parsedInstant);
        assertEquals(parsedDate, FileTimes.toDate(parsedFileTime));
    }

    @ParameterizedTest
    @MethodSource("fileTimeToNtfsProvider")
    public void testFileTimeToNtfsTime(final String instant, final long ntfsTime) {
        final FileTime parsed = FileTime.from(Instant.parse(instant));
        assertEquals(ntfsTime, FileTimes.toNtfsTime(parsed));
    }

    //

    @ParameterizedTest
    @MethodSource("dateToNtfsProvider")
    public void testFromUnixTime(final String instant, final long ntfsTime) {
        final long epochSecond = Instant.parse(instant).getEpochSecond();
        assertEquals(epochSecond, FileTimes.fromUnixTime(epochSecond).to(TimeUnit.SECONDS));
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
    @MethodSource("dateToNtfsProvider")
    public void testNtfsTimeToDate(final String instant, final long ntfsTime) {
        assertEquals(Instant.parse(instant), FileTimes.ntfsTimeToDate(ntfsTime).toInstant());
    }

    @ParameterizedTest
    @MethodSource("fileTimeToNtfsProvider")
    public void testNtfsTimeToFileTime(final String instant, final long ntfsTime) {
        final FileTime parsed = FileTime.from(Instant.parse(instant));
        assertEquals(parsed, FileTimes.ntfsTimeToFileTime(ntfsTime));
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
}
