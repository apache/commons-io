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

package org.apache.commons.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

import org.apache.commons.io.function.IOConsumer;
import org.apache.commons.io.function.IOFunction;

/**
 * Enumerates access modes for {@link RandomAccessFile} with factory methods.
 *
 * @see RandomAccessFile#RandomAccessFile(File, String)
 * @see RandomAccessFile#RandomAccessFile(String, String)
 * @see Enum
 * @since 2.12.0
 */
public enum RandomAccessFileMode {

    /**
     * Defines mode {@value #R} to open a {@link RandomAccessFile} for reading only.
     *
     * @see RandomAccessFile#RandomAccessFile(File, String)
     * @see RandomAccessFile#RandomAccessFile(String, String)
     */
    READ_ONLY(RandomAccessFileMode.R, 1), // NOPMD bug https://github.com/pmd/pmd/issues/5263

    /**
     * Defines mode {@value #RW} to open a {@link RandomAccessFile} for reading and writing.
     *
     * @see RandomAccessFile#RandomAccessFile(File, String)
     * @see RandomAccessFile#RandomAccessFile(String, String)
     */
    READ_WRITE(RandomAccessFileMode.RW, 2), // NOPMD bug https://github.com/pmd/pmd/issues/5263

    /**
     * Defines mode {@value #RWS} to open a {@link RandomAccessFile} for reading and writing, as with {@value #RW}, and also require that every update to the
     * file's content or metadata be written synchronously to the underlying storage device.
     *
     * @see RandomAccessFile#RandomAccessFile(File, String)
     * @see RandomAccessFile#RandomAccessFile(String, String)
     * @see StandardOpenOption#SYNC
     */
    READ_WRITE_SYNC_ALL(RandomAccessFileMode.RWS, 4), // NOPMD bug https://github.com/pmd/pmd/issues/5263

    /**
     * Defines mode {@value #RWD} to open a {@link RandomAccessFile} for reading and writing, as with {@value #RW}, and also require that every update to the
     * file's content be written synchronously to the underlying storage device.
     *
     * @see RandomAccessFile#RandomAccessFile(File, String)
     * @see RandomAccessFile#RandomAccessFile(String, String)
     * @see StandardOpenOption#DSYNC
     */
    READ_WRITE_SYNC_CONTENT(RandomAccessFileMode.RWD, 3); // NOPMD bug https://github.com/pmd/pmd/issues/5263

    private static final String R = "r";
    private static final String RW = "rw";
    private static final String RWD = "rwd";
    private static final String RWS = "rws";

    /**
     * Gets the enum value that best fits the given {@link OpenOption}s.
     * <p>
     * The input must be a legal and working combination for NIO.
     * </p>
     *
     * @param openOption options like {@link StandardOpenOption}.
     * @return best fit, by default {@link #READ_ONLY}.
     * @see StandardOpenOption
     * @since 2.18.0
     */
    public static RandomAccessFileMode valueOf(final OpenOption... openOption) {
        RandomAccessFileMode bestFit = READ_ONLY;
        for (final OpenOption option : openOption) {
            if (option instanceof StandardOpenOption) {
                switch ((StandardOpenOption) option) {
                case WRITE:
                    if (!bestFit.implies(READ_WRITE)) {
                        bestFit = READ_WRITE;
                    }
                    break;
                case DSYNC:
                    if (!bestFit.implies(READ_WRITE_SYNC_CONTENT)) {
                        bestFit = READ_WRITE_SYNC_CONTENT;
                    }
                    break;
                case SYNC:
                    if (!bestFit.implies(READ_WRITE_SYNC_ALL)) {
                        bestFit = READ_WRITE_SYNC_ALL;
                    }
                    break;
                default:
                    // explicit case skip (spotbugs)
                    continue;
                }
            }
        }
        return bestFit;
    }

    /**
     * Gets the {@link RandomAccessFileMode} value for the given mode, one of {@value #R}, {@value #RW}, {@value #RWD}, or {@value #RWS}.
     *
     * @param mode one of {@value #R}, {@value #RW}, {@value #RWD}, or {@value #RWS}.
     * @return A RandomAccessFileMode.
     * @throws IllegalArgumentException Thrown when mode is not one of {@value #R}, {@value #RW}, {@value #RWD}, or {@value #RWS}.
     * @since 2.18.0
     */
    public static RandomAccessFileMode valueOfMode(final String mode) {
        switch (mode) {
        case R:
            return READ_ONLY;
        case RW:
            return READ_WRITE;
        case RWD:
            return READ_WRITE_SYNC_CONTENT;
        case RWS:
            return READ_WRITE_SYNC_ALL;
        }
        throw new IllegalArgumentException(mode);
    }

    private final int level;

    private final String mode;

    RandomAccessFileMode(final String mode, final int level) {
        this.mode = mode;
        this.level = level;
    }

    /**
     * Performs an operation on the {@link RandomAccessFile} specified at the given {@link Path}.
     * <p>
     * This method allocates and releases the {@link RandomAccessFile} given to the consumer.
     * </p>
     *
     * @param file the file specifying the {@link RandomAccessFile} to open.
     * @param consumer the function to apply.
     * @throws FileNotFoundException See {@link IORandomAccessFile#IORandomAccessFile(File, String)}.
     * @throws IOException Thrown by the given function.
     * @since 2.18.0
     */
    public void accept(final Path file, final IOConsumer<RandomAccessFile> consumer) throws IOException {
        try (RandomAccessFile raf = create(file)) {
            consumer.accept(raf);
        }
    }

    /**
     * Applies the given function for a {@link RandomAccessFile} specified at the given {@link Path}.
     * <p>
     * This method allocates and releases the {@link RandomAccessFile} given to the function.
     * </p>
     *
     * @param <T> the return type of the function.
     * @param file the file specifying the {@link RandomAccessFile} to open.
     * @param function the function to apply.
     * @return the function's result value.
     * @throws FileNotFoundException See {@link IORandomAccessFile#IORandomAccessFile(File, String)}.
     * @throws IOException Thrown by the given function.
     * @since 2.18.0
     */
    public <T> T apply(final Path file, final IOFunction<RandomAccessFile, T> function) throws IOException {
        try (RandomAccessFile raf = create(file)) {
            return function.apply(raf);
        }
    }

    /**
     * Constructs a random access file to read from, and optionally to write to, the file specified by the {@link File} argument.
     * <p>
     * Prefer {@link #create(Path)} over this.
     * </p>
     *
     * @param file the file object
     * @return a random access file
     * @throws FileNotFoundException See {@link IORandomAccessFile#IORandomAccessFile(File, String)}.
     */
    public RandomAccessFile create(final File file) throws FileNotFoundException {
        return new IORandomAccessFile(file, mode);
    }

    /**
     * Constructs a random access file to read from, and optionally to write to, the file specified by the {@link File} argument.
     *
     * @param file the file object
     * @return a random access file
     * @throws FileNotFoundException See {@link IORandomAccessFile#IORandomAccessFile(File, String)}.
     */
    public RandomAccessFile create(final Path file) throws FileNotFoundException {
        return create(Objects.requireNonNull(file.toFile(), "file"));
    }

    /**
     * Constructs a random access file to read from, and optionally to write to, the file specified by the {@link File} argument.
     * <p>
     * Prefer {@link #create(Path)} over this.
     * </p>
     *
     * @param name the file object
     * @return a random access file
     * @throws FileNotFoundException See {@link IORandomAccessFile#IORandomAccessFile(File, String)}.
     */
    public RandomAccessFile create(final String name) throws FileNotFoundException {
        return new IORandomAccessFile(name, mode);
    }

    /**
     * A level for relative comparison of access mode rights, the larger, the more access.
     * <p>
     * The relative order from lowest to highest access rights is:
     * </p>
     * <ol>
     * <li>{@link #READ_ONLY}</li>
     * <li>{@link #READ_WRITE}</li>
     * <li>{@link #READ_WRITE_SYNC_CONTENT}</li>
     * <li>{@link #READ_WRITE_SYNC_ALL}</li>
     * </ol>
     * <p>
     * This is unrelated to {@link #ordinal()}.
     * </p>
     *
     * @return A level for relative comparison.
     */
    private int getLevel() {
        return level;
    }

    /**
     * Gets the access mode, one of {@value #R}, {@value #RW}, {@value #RWD}, or {@value #RWS}.
     *
     * @return one of {@value #R}, {@value #RW}, {@value #RWD}, or {@value #RWS}.
     * @since 2.18.0
     */
    public String getMode() {
        return mode;
    }

    /**
     * Tests whether this mode implies the given {@code other} mode.
     * <p>
     * For example:
     * </p>
     * <ol>
     * <li>{@link RandomAccessFileMode#READ_WRITE_SYNC_ALL} implies {{@link RandomAccessFileMode#READ_WRITE_SYNC_CONTENT}}.</li>
     * <li>{@link RandomAccessFileMode#READ_WRITE_SYNC_CONTENT} implies {{@link RandomAccessFileMode#READ_WRITE}}.</li>
     * <li>{@link RandomAccessFileMode#READ_WRITE} implies {{@link RandomAccessFileMode#READ_ONLY}}.</li>
     * </ol>
     *
     * @param other the non-null mode to test against.
     * @return whether this mode implies the given {@code other} mode.
     * @since 2.18.0
     */
    public boolean implies(final RandomAccessFileMode other) {
        // Note: The method name "implies" is inspired by java.security.Permission.implies(Permission)
        return getLevel() >= other.getLevel();
    }

    /**
     * Constructs a random access file to read from, and optionally to write to, the file specified by the {@link File} argument.
     *
     * @param name the file object
     * @return a random access file
     * @throws FileNotFoundException See {@link IORandomAccessFile#IORandomAccessFile(File, String)}.
     * @since 2.18.0
     */
    public IORandomAccessFile io(final String name) throws FileNotFoundException {
        return new IORandomAccessFile(name, mode);
    }

}
