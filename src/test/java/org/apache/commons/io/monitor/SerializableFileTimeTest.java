/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.io.monitor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.file.PathUtils;
import org.apache.commons.io.file.attribute.FileTimes;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests {@link SerializableFileTime}.
 */
class SerializableFileTimeTest {

    @Test
    void testCompareTo_EarlierValue() {
        final SerializableFileTime sft = new SerializableFileTime(FileTime.fromMillis(2000L));
        final FileTime earlier = FileTime.fromMillis(1000L);
        assertTrue(sft.compareTo(earlier) > 0, "compareTo should return positive for later time");
    }

    @Test
    void testCompareTo_Epoch() {
        final SerializableFileTime sft = SerializableFileTime.EPOCH;
        assertEquals(0, sft.compareTo(FileTimes.EPOCH), "EPOCH compareTo EPOCH should be 0");
        assertTrue(SerializableFileTime.EPOCH.compareTo(FileTime.fromMillis(1000L)) < 0, "EPOCH should be less than positive time");
        assertTrue(SerializableFileTime.EPOCH.compareTo(FileTime.fromMillis(-1000L)) > 0, "EPOCH should be greater than negative time");
    }

    @Test
    void testCompareTo_LaterValue() {
        final SerializableFileTime sft = new SerializableFileTime(FileTime.fromMillis(1000L));
        final FileTime later = FileTime.fromMillis(2000L);
        assertTrue(sft.compareTo(later) < 0, "compareTo should return negative for earlier time");
    }

    @Test
    void testCompareTo_Null() {
        final SerializableFileTime sft = new SerializableFileTime(FileTimes.EPOCH);
        assertThrows(NullPointerException.class, () -> sft.compareTo(null));
    }

    @Test
    void testCompareTo_SameValue() {
        final FileTime fileTime = FileTime.fromMillis(1234567890L);
        final SerializableFileTime sft = new SerializableFileTime(fileTime);
        assertEquals(0, sft.compareTo(fileTime), "compareTo should return 0 for same value");
    }

    @Test
    void testConstructor() {
        final SerializableFileTime sft = new SerializableFileTime(FileTimes.EPOCH);
        assertEquals(FileTimes.EPOCH, sft.unwrap());
    }

    @Test
    void testConstructorNull() {
        assertThrows(NullPointerException.class, () -> new SerializableFileTime(null));
    }

    @Test
    void testEPOCH() {
        assertSame(FileTimes.EPOCH, SerializableFileTime.EPOCH.unwrap());
    }

    @Test
    void testEPOCH_MultipleAccess() {
        // Verify EPOCH constant is consistent across multiple accesses
        final SerializableFileTime sft1 = SerializableFileTime.EPOCH;
        final SerializableFileTime sft2 = SerializableFileTime.EPOCH;
        assertSame(sft1, sft2);
        assertEquals(0L, sft1.toMillis());
        assertEquals(Instant.EPOCH, sft1.toInstant());
    }

    @Test
    void testEquals_DifferentType() {
        final SerializableFileTime sft = new SerializableFileTime(FileTimes.EPOCH);
        assertNotEquals("not a SerializableFileTime", sft);
        assertNotEquals(FileTimes.EPOCH, sft);
    }

    @Test
    void testEquals_DifferentValue() {
        final SerializableFileTime sft1 = new SerializableFileTime(FileTime.fromMillis(1000L));
        final SerializableFileTime sft2 = new SerializableFileTime(FileTime.fromMillis(2000L));
        assertNotEquals(sft1, sft2);
    }

    @Test
    void testEquals_Epoch() {
        final SerializableFileTime sft1 = SerializableFileTime.EPOCH;
        final SerializableFileTime sft2 = new SerializableFileTime(FileTimes.EPOCH);
        assertEquals(sft1, sft2);
    }

    @Test
    void testEquals_Null() {
        final SerializableFileTime sft = new SerializableFileTime(FileTimes.EPOCH);
        assertNotEquals(null, sft);
    }

    @Test
    void testEquals_SameObject() {
        final FileTime fileTime = FileTime.fromMillis(1234567890L);
        final SerializableFileTime sft = new SerializableFileTime(fileTime);
        assertEquals(sft, sft);
    }

    @Test
    void testEquals_SameValue() {
        final FileTime fileTime = FileTime.fromMillis(1234567890L);
        final SerializableFileTime sft1 = new SerializableFileTime(fileTime);
        final SerializableFileTime sft2 = new SerializableFileTime(fileTime);
        assertEquals(sft1, sft2);
    }

    @Test
    void testHashCode_DifferentValue() {
        final SerializableFileTime sft1 = new SerializableFileTime(FileTime.fromMillis(1000L));
        final SerializableFileTime sft2 = new SerializableFileTime(FileTime.fromMillis(2000L));
        assertNotEquals(sft1.hashCode(), sft2.hashCode());
    }

    @Test
    void testHashCode_Epoch() {
        final SerializableFileTime sft1 = SerializableFileTime.EPOCH;
        final SerializableFileTime sft2 = new SerializableFileTime(FileTimes.EPOCH);
        assertEquals(sft1.hashCode(), sft2.hashCode());
    }

    @Test
    void testHashCode_SameValue() {
        final FileTime fileTime = FileTime.fromMillis(1234567890L);
        final SerializableFileTime sft1 = new SerializableFileTime(fileTime);
        final SerializableFileTime sft2 = new SerializableFileTime(fileTime);
        assertEquals(sft1.hashCode(), sft2.hashCode());
    }

    @Test
    void testHashCodeContract() {
        // Verify that the hashCode contract is maintained: equal objects must have equal hashCodes
        final FileTime fileTime1 = FileTime.fromMillis(1234567890L);
        final FileTime fileTime2 = FileTime.fromMillis(1234567890L);
        final SerializableFileTime sft1 = new SerializableFileTime(fileTime1);
        final SerializableFileTime sft2 = new SerializableFileTime(fileTime2);
        // Equal objects should have equal hashCodes
        assertEquals(sft1, sft2);
        assertEquals(sft1.hashCode(), sft2.hashCode());
    }

    @Test
    void testMaxValue() {
        // Test with maximum FileTime value
        final FileTime maxTime = FileTime.from(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        final SerializableFileTime sft = new SerializableFileTime(maxTime);
        final FileTime unwrap = sft.unwrap();
        assertEquals(maxTime, unwrap);
        assertEquals(unwrap.toMillis(), sft.toMillis());
    }

    @Test
    void testMinValue() {
        // Test with minimum FileTime value (negative)
        final FileTime minTime = FileTime.from(Long.MIN_VALUE, TimeUnit.NANOSECONDS);
        final SerializableFileTime sft = new SerializableFileTime(minTime);
        final FileTime unwrap = sft.unwrap();
        assertEquals(minTime, unwrap);
        assertEquals(unwrap.toMillis(), sft.toMillis());
    }

    @Test
    void testMultipleInstancesIndependent() {
        final SerializableFileTime sft1 = new SerializableFileTime(FileTime.fromMillis(1000L));
        final SerializableFileTime sft2 = new SerializableFileTime(FileTime.fromMillis(2000L));
        // Verify they are independent
        assertNotEquals(sft1, sft2);
        assertNotEquals(sft1.hashCode(), sft2.hashCode());
        assertEquals(1000L, sft1.toMillis());
        assertEquals(2000L, sft2.toMillis());
    }

    @Test
    void testNullFileTimeInCompareTo() {
        assertThrows(NullPointerException.class, () -> new SerializableFileTime(FileTimes.EPOCH).compareTo(null));
    }

    @Test
    void testNullFileTimeInConstructor() {
        final NullPointerException exception = assertThrows(NullPointerException.class, () -> new SerializableFileTime(null));
        assertEquals("fileTime", exception.getMessage());
    }

    @Test
    void testSerializable() throws IOException {
        final SerializableFileTime expected = new SerializableFileTime(Files.getLastModifiedTime(PathUtils.current()));
        final SerializableFileTime actual = SerializationUtils.roundtrip(expected);
        assertEquals(expected, actual);
        final FileTime expectedFt = expected.unwrap();
        assertEquals(expectedFt, actual.unwrap());
        assertEquals(0, actual.compareTo(expectedFt));
        assertEquals(expectedFt.hashCode(), actual.hashCode());
        assertEquals(expectedFt.toInstant(), actual.toInstant());
        assertEquals(expectedFt.toMillis(), actual.toMillis());
        assertEquals(expectedFt.toString(), actual.toString());
    }

    @Test
    void testSerializable_CompareTo() {
        final SerializableFileTime expected = new SerializableFileTime(FileTime.fromMillis(1234567890L));
        final SerializableFileTime actual = SerializationUtils.roundtrip(expected);
        final FileTime fileTime = expected.unwrap();
        assertEquals(0, actual.compareTo(fileTime));
        final FileTime test1 = FileTime.fromMillis(1000L);
        assertEquals(expected.compareTo(test1), actual.compareTo(test1));
        final FileTime test2 = FileTime.fromMillis(2000L);
        assertEquals(expected.compareTo(test2), actual.compareTo(test2));
    }

    @Test
    void testSerializable_Epoch() {
        final SerializableFileTime expected = SerializableFileTime.EPOCH;
        final SerializableFileTime actual = SerializationUtils.roundtrip(expected);
        assertEquals(expected, actual);
        assertEquals(expected.unwrap(), actual.unwrap());
        assertEquals(expected.toInstant(), actual.toInstant());
        assertEquals(expected.toMillis(), actual.toMillis());
        assertEquals(expected.toString(), actual.toString());
    }

    @Test
    void testSerializable_EqualsAndHashCode() {
        final SerializableFileTime expected = new SerializableFileTime(FileTime.fromMillis(1234567890L));
        final SerializableFileTime actual = SerializationUtils.roundtrip(expected);
        assertEquals(expected, actual);
        assertEquals(expected.hashCode(), actual.hashCode());
    }

    @Test
    void testSerializable_FutureTime() {
        final Instant future = Instant.now().plusSeconds(86400L);
        final SerializableFileTime expected = new SerializableFileTime(FileTime.from(future));
        final SerializableFileTime actual = SerializationUtils.roundtrip(expected);
        assertEquals(expected, actual);
        assertEquals(expected.unwrap(), actual.unwrap());
        assertEquals(expected.toInstant(), actual.toInstant());
        assertEquals(expected.toMillis(), actual.toMillis());
    }

    @Test
    void testSerializable_NanosPrecision() {
        // Test that nanosecond precision is preserved through serialization
        final Instant instant = Instant.EPOCH.plusNanos(123456789);
        final SerializableFileTime expected = new SerializableFileTime(FileTime.from(instant));
        final SerializableFileTime actual = SerializationUtils.roundtrip(expected);
        assertEquals(expected, actual);
        assertEquals(expected.unwrap(), actual.unwrap());
        assertEquals(expected.toInstant(), actual.toInstant());
        assertEquals(expected.toMillis(), actual.toMillis());
        assertEquals(expected.to(TimeUnit.NANOSECONDS), actual.to(TimeUnit.NANOSECONDS));
    }

    @Test
    void testSerializable_NegativeTime() {
        final SerializableFileTime expected = new SerializableFileTime(FileTime.fromMillis(-1234567890L));
        final SerializableFileTime actual = SerializationUtils.roundtrip(expected);
        assertEquals(expected, actual);
        assertEquals(expected.unwrap(), actual.unwrap());
        assertEquals(expected.toInstant(), actual.toInstant());
        assertEquals(expected.toMillis(), actual.toMillis());
    }

    @Test
    void testSerializable_SelfEquality() {
        final SerializableFileTime sft = new SerializableFileTime(FileTime.fromMillis(1234567890L));
        final SerializableFileTime actual = SerializationUtils.roundtrip(sft);
        assertNotSame(sft, actual, "Deserialized instance should not be same reference");
    }

    @Test
    void testSerializable_StringValue() {
        final SerializableFileTime expected = new SerializableFileTime(FileTime.fromMillis(1234567890L));
        final SerializableFileTime actual = SerializationUtils.roundtrip(expected);
        assertEquals(expected.toString(), actual.toString());
    }

    @Test
    void testSerializable_ToTimeUnits() {
        final SerializableFileTime expected = new SerializableFileTime(FileTime.fromMillis(1234567890L));
        final SerializableFileTime actual = SerializationUtils.roundtrip(expected);
        assertEquals(expected.to(TimeUnit.NANOSECONDS), actual.to(TimeUnit.NANOSECONDS));
        assertEquals(expected.to(TimeUnit.MICROSECONDS), actual.to(TimeUnit.MICROSECONDS));
        assertEquals(expected.to(TimeUnit.MILLISECONDS), actual.to(TimeUnit.MILLISECONDS));
        assertEquals(expected.to(TimeUnit.SECONDS), actual.to(TimeUnit.SECONDS));
        assertEquals(expected.to(TimeUnit.MINUTES), actual.to(TimeUnit.MINUTES));
        assertEquals(expected.to(TimeUnit.HOURS), actual.to(TimeUnit.HOURS));
        assertEquals(expected.to(TimeUnit.DAYS), actual.to(TimeUnit.DAYS));
    }

    @Test
    void testSerializable_TransitiveEquality() {
        final SerializableFileTime sft1 = new SerializableFileTime(FileTime.fromMillis(1234567890L));
        final SerializableFileTime sft2 = SerializationUtils.roundtrip(sft1);
        final SerializableFileTime sft3 = SerializationUtils.roundtrip(sft2);
        assertEquals(sft1, sft2);
        assertEquals(sft2, sft3);
        assertEquals(sft1, sft3);
    }

    @Test
    void testSerializable_Unwrap() {
        final SerializableFileTime expected = new SerializableFileTime(FileTime.fromMillis(1234567890L));
        final SerializableFileTime actual = SerializationUtils.roundtrip(expected);
        assertEquals(expected.unwrap(), actual.unwrap());
    }

    @Test
    void testSerializationPreservesNanosecondPrecision() {
        // FileTime can have nanosecond precision, verify serialization preserves it
        final Instant instant = Instant.EPOCH.plusSeconds(100).plusNanos(123456789);
        final SerializableFileTime expected = new SerializableFileTime(FileTime.from(instant));
        final SerializableFileTime actual = SerializationUtils.roundtrip(expected);
        assertEquals(expected.to(TimeUnit.NANOSECONDS), actual.to(TimeUnit.NANOSECONDS));
        assertEquals(expected.to(TimeUnit.MICROSECONDS), actual.to(TimeUnit.MICROSECONDS));
        assertEquals(expected.toMillis(), actual.toMillis());
    }

    @Test
    void testSerializationWithDifferentFileTimeConstructors() {
        // Test serialization with FileTime created via different constructors
        final FileTime fromMillis = FileTime.fromMillis(1234567890L);
        final FileTime fromSeconds = FileTime.from(1234567890L, TimeUnit.SECONDS);
        final FileTime fromNanos = FileTime.from(1234567890000000000L, TimeUnit.NANOSECONDS);
        for (final FileTime fileTime : new FileTime[] { fromMillis, fromSeconds, fromNanos }) {
            final SerializableFileTime expected = new SerializableFileTime(fileTime);
            final SerializableFileTime actual = SerializationUtils.roundtrip(expected);
            assertEquals(expected, actual);
            assertEquals(expected.unwrap(), actual.unwrap());
        }
    }

    @Test
    void testTo_TimeUnit_Epoch() {
        final SerializableFileTime sft = SerializableFileTime.EPOCH;
        assertEquals(0L, sft.to(TimeUnit.NANOSECONDS));
        assertEquals(0L, sft.to(TimeUnit.MICROSECONDS));
        assertEquals(0L, sft.to(TimeUnit.MILLISECONDS));
        assertEquals(0L, sft.to(TimeUnit.SECONDS));
        assertEquals(0L, sft.to(TimeUnit.MINUTES));
        assertEquals(0L, sft.to(TimeUnit.HOURS));
        assertEquals(0L, sft.to(TimeUnit.DAYS));
    }

    @Test
    void testTo_TimeUnit_NegativeTime() {
        // Test with a time before epoch (negative milliseconds)
        final SerializableFileTime sft = new SerializableFileTime(FileTime.fromMillis(-500L));
        assertEquals(-500L, sft.to(TimeUnit.MILLISECONDS));
        assertEquals(-500_000L, sft.to(TimeUnit.MICROSECONDS));
        assertEquals(-500_000_000L, sft.to(TimeUnit.NANOSECONDS));
    }

    @Test
    void testTo_TimeUnitDays() {
        final SerializableFileTime sft = new SerializableFileTime(FileTime.from(86_400L, TimeUnit.SECONDS));
        assertEquals(1L, sft.to(TimeUnit.DAYS), "to(DAYS) should return days");
    }

    @Test
    void testTo_TimeUnitHours() {
        final SerializableFileTime sft = new SerializableFileTime(FileTime.from(7_200L, TimeUnit.SECONDS));
        assertEquals(2L, sft.to(TimeUnit.HOURS), "to(HOURS) should return hours");
    }

    @Test
    void testTo_TimeUnitMicros() {
        final SerializableFileTime sft = new SerializableFileTime(FileTime.fromMillis(1000L));
        assertEquals(1_000_000L, sft.to(TimeUnit.MICROSECONDS), "to(MICROSECONDS) should return microseconds");
    }

    @Test
    void testTo_TimeUnitMillis() {
        final SerializableFileTime sft = new SerializableFileTime(FileTime.fromMillis(1000L));
        assertEquals(1000L, sft.to(TimeUnit.MILLISECONDS), "to(MILLISECONDS) should return milliseconds");
    }

    @Test
    void testTo_TimeUnitMinutes() {
        final SerializableFileTime sft = new SerializableFileTime(FileTime.from(120L, TimeUnit.SECONDS));
        assertEquals(2L, sft.to(TimeUnit.MINUTES), "to(MINUTES) should return minutes");
    }

    @Test
    void testTo_TimeUnitNanos() {
        final SerializableFileTime sft = new SerializableFileTime(FileTime.fromMillis(1000L));
        assertEquals(1_000_000_000L, sft.to(TimeUnit.NANOSECONDS), "to(NANOSECONDS) should return nanoseconds");
    }

    @Test
    void testTo_TimeUnitSeconds() {
        final SerializableFileTime sft = new SerializableFileTime(FileTime.from(100L, TimeUnit.SECONDS));
        assertEquals(100L, sft.to(TimeUnit.SECONDS), "to(SECONDS) should return seconds");
    }

    @Test
    void testToInstant() {
        final Instant expected = Instant.EPOCH;
        final SerializableFileTime sft = new SerializableFileTime(FileTime.from(expected));
        assertEquals(expected, sft.toInstant());
    }

    @Test
    void testToInstant_Epoch() {
        assertEquals(Instant.EPOCH, SerializableFileTime.EPOCH.toInstant());
    }

    @Test
    void testToInstant_FutureTime() {
        final Instant future = Instant.now().plusSeconds(3600L);
        final SerializableFileTime sft = new SerializableFileTime(FileTime.from(future));
        assertEquals(future, sft.toInstant());
    }

    @Test
    void testToMillis() {
        final SerializableFileTime sft = new SerializableFileTime(FileTime.fromMillis(1234567890L));
        assertEquals(1234567890L, sft.toMillis());
    }

    @Test
    void testToMillis_Epoch() {
        assertEquals(0L, SerializableFileTime.EPOCH.toMillis());
    }

    @Test
    void testToMillis_Negative() {
        final SerializableFileTime sft = new SerializableFileTime(FileTime.fromMillis(-1000L));
        assertEquals(-1000L, sft.toMillis());
    }

    @Test
    void testToString() {
        final SerializableFileTime sft = new SerializableFileTime(FileTime.fromMillis(1234567890L));
        final String expected = FileTime.fromMillis(1234567890L).toString();
        assertEquals(expected, sft.toString(), "toString() should match FileTime.toString()");
    }

    @Test
    void testToString_Epoch() {
        assertEquals(FileTimes.EPOCH.toString(), SerializableFileTime.EPOCH.toString());
    }

    @Test
    void testUnwrap() {
        final FileTime fileTime = FileTime.fromMillis(1234567890L);
        final SerializableFileTime sft = new SerializableFileTime(fileTime);
        assertEquals(fileTime, sft.unwrap());
    }

    @Test
    void testUnwrap_Epoch() {
        assertSame(FileTimes.EPOCH, SerializableFileTime.EPOCH.unwrap());
    }

    @Test
    void testValueOfFileTimeFrom() {
        final FileTime fileTime = FileTime.from(1234567890L, TimeUnit.MILLISECONDS);
        final SerializableFileTime sft = new SerializableFileTime(fileTime);
        assertEquals(fileTime, sft.unwrap());
        assertEquals(1234567890L, sft.toMillis());
    }

    @ParameterizedTest
    @ValueSource(longs = { 0L, 1L, -1L, 1234567890L, Long.MAX_VALUE, Long.MIN_VALUE })
    void testValueOfFileTimeFromMillis(final long millis) {
        final FileTime fileTime = FileTime.fromMillis(millis);
        final SerializableFileTime sft = new SerializableFileTime(fileTime);
        assertEquals(fileTime, sft.unwrap());
        assertEquals(millis, sft.toMillis());
    }

    @ParameterizedTest
    @ValueSource(longs = { 0L, 1L, -1L, 1234567890L, Long.MAX_VALUE, Long.MIN_VALUE })
    void testValueOfFileTimeFromNanos(final long nanos) {
        final FileTime fileTime = FileTime.from(nanos, TimeUnit.NANOSECONDS);
        final SerializableFileTime sft = new SerializableFileTime(fileTime);
        assertEquals(fileTime, sft.unwrap());
        assertEquals(nanos, sft.to(TimeUnit.NANOSECONDS));
    }

    @ParameterizedTest
    @ValueSource(longs = { 0L, 1L, -1L, 1234567890L, Long.MAX_VALUE, Long.MIN_VALUE })
    void testValueOfFileTimeFromSeconds(final long seconds) {
        final FileTime fileTime = FileTime.from(seconds, TimeUnit.SECONDS);
        final SerializableFileTime sft = new SerializableFileTime(fileTime);
        assertEquals(fileTime, sft.unwrap());
        assertEquals(seconds, sft.to(TimeUnit.SECONDS));
    }

    @Test
    void testValueOfInstant() {
        // @formatter:off
        final Instant[] instants = {
                Instant.EPOCH,
                Instant.now(),
                Instant.ofEpochMilli(0L),
                Instant.ofEpochMilli(1L),
                Instant.ofEpochMilli(-1L),
                Instant.ofEpochMilli(Long.MAX_VALUE),
                Instant.ofEpochMilli(Long.MIN_VALUE)
        };
        // @formatter:on
        for (final Instant instant : instants) {
            final SerializableFileTime sft = new SerializableFileTime(FileTime.from(instant));
            assertEquals(instant, sft.toInstant(), "toInstant() should match for Instant: " + instant);
        }
    }
}
