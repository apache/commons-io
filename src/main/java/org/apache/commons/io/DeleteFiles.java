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

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Provides different strategies to delete files and directories.
 */
public final class DeleteFiles {

    private static final DeleteFiles DEFAULT = newConfig().build();

    /**
     * Gets the default DeleteFiles strategy.
     */
    public static DeleteFiles getDefault() {
        return DEFAULT;
    }

    /**
     * Creates a new Config builder object to override options in DeleteFiles.
     */
    public static Config newConfig() {
        return new Config();
    }

    private final int maxRetries;
    private final long waitBetweenRetriesMillis;
    private final double backoffMultiplier;
    private final boolean retryOverridingFileAttributes;
    private final boolean overrideAllAttributes;

    private DeleteFiles(final int maxRetries, final long waitBetweenRetriesMillis, final double backoffMultiplier,
                        final boolean retryOverridingFileAttributes, final boolean overrideAllAttributes) {
        this.maxRetries = maxRetries;
        this.waitBetweenRetriesMillis = waitBetweenRetriesMillis;
        this.backoffMultiplier = backoffMultiplier;
        this.retryOverridingFileAttributes = retryOverridingFileAttributes;
        this.overrideAllAttributes = overrideAllAttributes;
    }

    /**
     * Deletes a file or directory and its contents recursively.
     *
     * @param path path to delete
     * @throws IOException if there is an error deleting anything
     */
    public void forceDelete(final Path path) throws IOException {
        final List<IOException> accumulatedErrors = new ArrayList<>();
        for (int retriesAttempted = 0; retriesAttempted <= maxRetries; retriesAttempted++) {
            final List<IOException> errors = tryDeleteRecursive(path);
            if (errors.isEmpty()) {
                return;
            }
            accumulatedErrors.addAll(errors);
            if (waitToRetry(retriesAttempted)) {
                throw new CompositeIOException(failureMessage(path, retriesAttempted, true), accumulatedErrors);
            }
        }
        throw new CompositeIOException(failureMessage(path, maxRetries, false), accumulatedErrors);
    }

    /**
     * Deletes a file or directory and its contents recursively without throwing an exception.
     *
     * @param path path to delete
     * @return whether or not the path was deleted successfully
     */
    public boolean deleteQuietly(final Path path) {
        try {
            forceDelete(path);
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    /**
     * Deletes a directory's contents recursively.
     *
     * @param directory path to delete
     * @throws IOException              if any descendants cannot be deleted
     * @throws IllegalArgumentException if the path is not a directory
     */
    public void cleanDirectory(final Path directory) throws IOException {
        if (!Files.isDirectory(directory, LinkOption.NOFOLLOW_LINKS)) {
            throw new IllegalArgumentException(directory + " is not a directory.");
        }
        final List<IOException> accumulatedErrors = new ArrayList<>();
        for (int retriesAttempted = 0; retriesAttempted <= maxRetries; retriesAttempted++) {
            final List<IOException> errors = tryCleanDirectory(directory);
            if (errors.isEmpty()) {
                return;
            }
            accumulatedErrors.addAll(errors);
            if (waitToRetry(retriesAttempted)) {
                throw new CompositeIOException(failureMessage(directory, retriesAttempted, true), accumulatedErrors);
            }
        }
        throw new CompositeIOException(failureMessage(directory, maxRetries, false));
    }

    private IOException tryDeleteFile(final Path file) {
        try {
            if (retryOverridingFileAttributes) {
                deleteOrMakeDeletableThenRetry(file);
            } else {
                Files.delete(file);
            }
            return null;
        } catch (final IOException e) {
            return e;
        }
    }

    private List<IOException> tryDeleteRecursive(final Path path) {
        final List<IOException> accumulatedErrors;
        if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
            accumulatedErrors = tryCleanDirectory(path);
        } else {
            accumulatedErrors = new ArrayList<>();
        }
        final IOException e = tryDeleteFile(path);
        if (e != null) {
            accumulatedErrors.add(e);
        }
        return accumulatedErrors;
    }

    private List<IOException> tryCleanDirectory(final Path directory) {
        final List<IOException> accumulatedErrors = new ArrayList<>();
        if (!Files.isDirectory(directory, LinkOption.NOFOLLOW_LINKS)) {
            return accumulatedErrors;
        }
        try (final DirectoryStream<Path> children = Files.newDirectoryStream(directory)) {
            for (final Path child : children) {
                accumulatedErrors.addAll(tryDeleteRecursive(child));
            }
        } catch (final IOException e) {
            accumulatedErrors.add(e);
        }
        return accumulatedErrors;
    }

    private boolean waitToRetry(final int retriesAttempted) {
        if (waitBetweenRetriesMillis <= 0 || retriesAttempted >= maxRetries) return Thread.interrupted();
        final long waitToRetryMillis = getWaitToRetryMillis(retriesAttempted);
        try {
            Thread.sleep(waitToRetryMillis);
            return false;
        } catch (final InterruptedException ignored) {
            return true;
        }
    }

    private long getWaitToRetryMillis(final int retriesAttempted) {
        return backoffMultiplier > 1.0
                ? (long) (Math.pow(backoffMultiplier, retriesAttempted) * waitBetweenRetriesMillis)
                : waitBetweenRetriesMillis;
    }

    private String failureMessage(final Path pathToDelete, final int retriesAttempted, final boolean wasInterrupted) {
        final StringBuilder sb = new StringBuilder("Unable to delete '")
                .append(pathToDelete)
                .append("'. Tried ")
                .append(retriesAttempted + 1)
                .append(" time");
        if (retriesAttempted > 0) {
            sb.append('s');
        }
        if (maxRetries > 0) {
            sb.append(" (of a maximum of ")
                    .append(maxRetries + 1)
                    .append(')');
            if (waitBetweenRetriesMillis > 0) {
                sb.append(" waiting ").append(waitBetweenRetriesMillis);
                if (backoffMultiplier > 1.0) {
                    sb.append('-').append(getWaitToRetryMillis(retriesAttempted));
                }
                sb.append(" millis between attempts");
            }
        }
        if (wasInterrupted) {
            sb.append(". The delete operation was interrupted before it completed successfully");
        }
        return sb.append('.').toString();
    }

    private void deleteOrMakeDeletableThenRetry(final Path path) throws IOException {
        try {
            Files.delete(path);
        } catch (final IOException e1) {
            try {
                makeDeletable(path);
                Files.delete(path);
            } catch (final IOException e2) {
                if (Files.isDirectory(path)) {
                    final List<String> entries = new ArrayList<>();
                    try (final DirectoryStream<Path> children = Files.newDirectoryStream(path)) {
                        for (final Path child : children) {
                            entries.add(child.toString());
                        }
                    } catch (final IOException e3) {
                        throw new CompositeIOException("Cannot delete directory " + path, e1, e2, e3);
                    }
                    throw new CompositeIOException("Cannot delete directory " + path + " with directory contents: " + entries, e1, e2);
                }
                throw new CompositeIOException("Cannot delete directory " + path, e1, e2);
            }
        }
    }

    private void makeDeletable(final Path path) throws IOException {
        if (!Files.isWritable(path)) {
            makeWritable(path);
        }
        // in POSIX environments, the parent directory must also be writable in order to delete its entries
        final Path parent = path.getParent();
        if (parent != null && !Files.isWritable(parent)) {
            makeWritable(parent);
        }
    }

    private void makeWritable(final Path path) throws IOException {
        try {
            final PosixFileAttributeView fileAttributeView = Files.getFileAttributeView(path, PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
            if (overrideAllAttributes) {
                fileAttributeView.setPermissions(EnumSet.allOf(PosixFilePermission.class));
            } else {
                final Set<PosixFilePermission> permissions = fileAttributeView.readAttributes().permissions();
                permissions.add(PosixFilePermission.OWNER_WRITE);
                fileAttributeView.setPermissions(permissions);
            }
        } catch (UnsupportedOperationException ignored) {
            // retry with old File API
        }
        //noinspection ResultOfMethodCallIgnored the subsequent call to Files.delete() will have a more useful exception message
        path.toFile().setWritable(true, !overrideAllAttributes);
    }

    /**
     * Configures an instance of {@link DeleteFiles}.
     */
    public static final class Config {
        private int maxRetries;
        private long waitBetweenRetriesMillis;
        private double backoffMultiplier = 1.0;
        private boolean retryOverridingFileAttributes;
        private boolean overrideAllAttributes;

        private Config() {
        }

        /**
         * Sets the maximum number of retries to attempt in case of exceptions.
         * Setting this to 0 means that deletion operations are not retried.
         */
        public Config setMaxRetries(final int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        /**
         * Sets the time to wait between retries in milliseconds.
         */
        public Config setWaitBetweenRetriesMillis(final long waitBetweenRetriesMillis) {
            this.waitBetweenRetriesMillis = waitBetweenRetriesMillis;
            return this;
        }

        /**
         * Sets the time to wait between retries.
         */
        public Config setWaitBetweenRetries(final long waitBetweenRetries, final TimeUnit timeUnit) {
            return setWaitBetweenRetriesMillis(timeUnit.toMillis(waitBetweenRetries));
        }

        /**
         * Sets the backoff multiplier to apply to retries. Using a larger value than 1.0 will cause each retry to
         * take longer, while using a value of 1.0 will cause each retry to take {@link #setWaitBetweenRetriesMillis(long)}
         * milliseconds.
         */
        public Config setBackoffMultiplier(final double backoffMultiplier) {
            this.backoffMultiplier = backoffMultiplier;
            return this;
        }

        /**
         * Sets the option to retry deletes with file attributes being overridden. When enabled, this will
         * attempt to make files and their parent directories writable if they cannot be deleted.
         */
        public Config setRetryOverridingFileAttributes(final boolean retryOverridingFileAttributes) {
            this.retryOverridingFileAttributes = retryOverridingFileAttributes;
            return this;
        }

        /**
         * Sets the option to override all file attributes when a file or directory cannot be deleted.
         */
        public Config setOverrideAllAttributes(final boolean overrideAllAttributes) {
            this.overrideAllAttributes = overrideAllAttributes;
            return this;
        }

        /**
         * Builds a new DeleteFiles instance using the configured options.
         */
        public DeleteFiles build() {
            return new DeleteFiles(maxRetries, waitBetweenRetriesMillis, backoffMultiplier, retryOverridingFileAttributes, overrideAllAttributes);
        }
    }
}
