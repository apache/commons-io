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

import java.math.BigInteger;
import java.util.Objects;

/**
 * Provides counters for files, directories, and sizes, as a visit proceeds.
 *
 * @since 2.7
 */
public class Counters {

    /**
     * Counts files, directories, and sizes, as a visit proceeds.
     */
    private static class AbstractPathCounters implements PathCounters {

        private final Counter byteCounter;
        private final Counter directoryCounter;
        private final Counter fileCounter;

        /**
         * Constructs a new instance.
         *
         * @param byteCounter the byte counter.
         * @param directoryCounter the directory counter.
         * @param fileCounter the file counter.
         */
        protected AbstractPathCounters(final Counter byteCounter, final Counter directoryCounter,
            final Counter fileCounter) {
            this.byteCounter = byteCounter;
            this.directoryCounter = directoryCounter;
            this.fileCounter = fileCounter;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof AbstractPathCounters)) {
                return false;
            }
            final AbstractPathCounters other = (AbstractPathCounters) obj;
            return Objects.equals(byteCounter, other.byteCounter)
                && Objects.equals(directoryCounter, other.directoryCounter)
                && Objects.equals(fileCounter, other.fileCounter);
        }

        @Override
        public Counter getByteCounter() {
            return byteCounter;
        }

        @Override
        public Counter getDirectoryCounter() {
            return directoryCounter;
        }

        /**
         * Gets the count of visited files.
         *
         * @return the byte count of visited files.
         */
        @Override
        public Counter getFileCounter() {
            return this.fileCounter;
        }

        @Override
        public int hashCode() {
            return Objects.hash(byteCounter, directoryCounter, fileCounter);
        }

        @Override
        public void reset() {
            byteCounter.reset();
            directoryCounter.reset();
            fileCounter.reset();
        }

        @Override
        public String toString() {
            return String.format("%,d files, %,d directories, %,d bytes", Long.valueOf(fileCounter.get()),
                Long.valueOf(directoryCounter.get()), Long.valueOf(byteCounter.get()));
        }

    }

    /**
     * Counts using a BigInteger number.
     */
    private static final class BigIntegerCounter implements Counter {

        private BigInteger value = BigInteger.ZERO;

        @Override
        public void add(final long val) {
            value = value.add(BigInteger.valueOf(val));

        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Counter)) {
                return false;
            }
            final Counter other = (Counter) obj;
            return Objects.equals(value, other.getBigInteger());
        }

        @Override
        public long get() {
            return value.longValueExact();
        }

        @Override
        public BigInteger getBigInteger() {
            return value;
        }

        @Override
        public Long getLong() {
            return Long.valueOf(value.longValueExact());
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public void increment() {
            value = value.add(BigInteger.ONE);
        }

        @Override
        public String toString() {
            return value.toString();
        }

        @Override
        public void reset() {
            value = BigInteger.ZERO;
        }
    }

    /**
     * Counts files, directories, and sizes, as a visit proceeds, using BigInteger numbers.
     */
    private final static class BigIntegerPathCounters extends AbstractPathCounters {

        /**
         * Constructs a new initialized instance.
         */
        protected BigIntegerPathCounters() {
            super(Counters.bigIntegerCounter(), Counters.bigIntegerCounter(), Counters.bigIntegerCounter());
        }

    }

    /**
     * Counts using a number.
     */
    public interface Counter {

        /**
         * Adds the given number to this counter.
         *
         * @param val the value to add.
         */
        void add(long val);

        /**
         * Gets the counter as a long.
         *
         * @return the counter as a long.
         */
        long get();

        /**
         * Gets the counter as a BigInteger.
         *
         * @return the counter as a BigInteger.
         */
        BigInteger getBigInteger();

        /**
         * Gets the counter as a Long.
         *
         * @return the counter as a Long.
         */
        Long getLong();

        /**
         * Adds one to this counter.
         */
        void increment();

        /**
         * Resets this count to 0.
         */
        default void reset() {
            // binary compat, do nothing
        }

    }

    /**
     * Counts using a long number.
     */
    private final static class LongCounter implements Counter {

        private long value;

        @Override
        public void add(final long add) {
            value += add;

        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Counter)) {
                return false;
            }
            final Counter other = (Counter) obj;
            return value == other.get();
        }

        @Override
        public long get() {
            return value;
        }

        @Override
        public BigInteger getBigInteger() {
            return BigInteger.valueOf(value);
        }

        @Override
        public Long getLong() {
            return Long.valueOf(value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public void increment() {
            value++;
        }

        @Override
        public String toString() {
            return Long.toString(value);
        }

        @Override
        public void reset() {
            value = 0L;
        }
    }

    /**
     * Counts files, directories, and sizes, as a visit proceeds, using long numbers.
     */
    private final static class LongPathCounters extends AbstractPathCounters {

        /**
         * Constructs a new initialized instance.
         */
        protected LongPathCounters() {
            super(Counters.longCounter(), Counters.longCounter(), Counters.longCounter());
        }

    }

    /**
     * Counts nothing.
     */
    private final static class NoopCounter implements Counter {

        static final NoopCounter INSTANCE = new NoopCounter();

        @Override
        public void add(final long add) {
            // noop
        }

        @Override
        public long get() {
            return 0;
        }

        @Override
        public BigInteger getBigInteger() {
            return BigInteger.ZERO;
        }

        @Override
        public Long getLong() {
            return 0L;
        }

        @Override
        public void increment() {
            // noop
        }

    }

    /**
     * Counts nothing.
     */
    private static final class NoopPathCounters extends AbstractPathCounters {

        static final NoopPathCounters INSTANCE = new NoopPathCounters();

        /**
         * Constructs a new initialized instance.
         */
        private NoopPathCounters() {
            super(Counters.noopCounter(), Counters.noopCounter(), Counters.noopCounter());
        }

    }

    /**
     * Counts files, directories, and sizes, as a visit proceeds.
     */
    public interface PathCounters {

        /**
         * Gets the byte counter.
         *
         * @return the byte counter.
         */
        Counter getByteCounter();

        /**
         * Gets the directory counter.
         *
         * @return the directory counter.
         */
        Counter getDirectoryCounter();

        /**
         * Gets the file counter.
         *
         * @return the file counter.
         */
        Counter getFileCounter();

        /**
         * Resets the counts to 0.
         */
        default void reset() {
            // binary compat, do nothing
        }

    }

    /**
     * Returns a new BigInteger Counter.
     *
     * @return a new BigInteger Counter.
     */
    public static Counter bigIntegerCounter() {
        return new BigIntegerCounter();
    }

    /**
     * Returns a new BigInteger PathCounters.
     *
     * @return a new BigInteger PathCounters.
     */
    public static PathCounters bigIntegerPathCounters() {
        return new BigIntegerPathCounters();
    }

    /**
     * Returns a new long Counter.
     *
     * @return a new long Counter.
     */
    public static Counter longCounter() {
        return new LongCounter();
    }

    /**
     * Returns a new BigInteger PathCounters.
     *
     * @return a new BigInteger PathCounters.
     */
    public static PathCounters longPathCounters() {
        return new LongPathCounters();
    }

    /**
     * Returns the NOOP Counter.
     *
     * @return the NOOP Counter.
     * @since 2.9.0
     */
    public static Counter noopCounter() {
        return NoopCounter.INSTANCE;
    }

    /**
     * Returns the NOOP PathCounters.
     *
     * @return the NOOP PathCounters.
     * @since 2.9.0
     */
    public static PathCounters noopPathCounters() {
        return NoopPathCounters.INSTANCE;
    }
}
