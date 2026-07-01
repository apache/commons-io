/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */

package org.apache.commons.io.file;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * A Path fence guards against using paths outside of a "fence" of made of root paths.
 * <p>
 * For example, to prevent an application from using paths outside of its configuration folder:
 * </p>
 * <pre>
 * Path config = Paths.get("path/to/config");
 * PathFence fence = PathFence.builder().setRoots(config).get();
 * </pre>
 * <p>
 * You call one of the {@code apply} methods to validate paths in your application:
 * </p>
 * <pre>
 * // Gets a Path or throws IllegalArgumentException
 * Path file1 = fence.{@link #apply(String) apply}("someName");
 * Path file2 = fence.{@link #apply(Path) apply}(somePath);
 * </pre>
 * <p>
 * You can also use multiple roots as the path fence:
 * </p>
 * <pre>
 * Path globalConfig = Paths.get("path1/to/global-config");
 * Path userConfig = Paths.get("path2/to/user-config");
 * Path localConfig = Paths.get("path3/to/sys-config");
 * PathFence fence = PathFence.builder().setRoots(globalConfig, userConfig, localConfig).get();
 * </pre>
 * <p>
 * To use the current directory as the path fence:
 * </p>
 * <pre>
 * PathFence fence = PathFence.builder().setRoots(PathUtils.current()).get();
 * </pre>
 *
 * @since 2.21.0
 */
// Cannot implement both UnaryOperator<Path> and Function<String, Path>, so don't pick one over the other
public final class PathFence {

    /**
     * Builds {@link PathFence} instances.
     */
    public static final class Builder implements Supplier<PathFence> {

        /** The empty Path array. */
        private static final Path[] EMPTY = {};

        /**
         * A fence is made of root Paths.
         */
        private Path[] roots = EMPTY;

        /**
         * Constructs a new instance.
         */
        private Builder() {
            // empty
        }

        @Override
        public PathFence get() {
            return new PathFence(this);
        }

        /**
         * Sets the paths that delineate this fence.
         *
         * @param roots the paths that delineate this fence.
         * @return {@code this} instance.
         */
        Builder setRoots(final Path... roots) {
            this.roots = roots != null ? roots.clone() : EMPTY;
            return this;
        }
    }

    /**
     * Creates a new builder.
     *
     * @return a new builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * A fence is made of Paths guarding Path resolution.
     */
    private final List<Path> roots;

    /**
     * Constructs a new instance.
     *
     * @param builder A builder.
     */
    private PathFence(final Builder builder) {
        this.roots = Arrays.stream(builder.roots).map(this::absoluteNormalize).collect(Collectors.toList());
    }

    /**
     * Converts the given path to a normalized absolute path.
     *
     * @param path The source path.
     * @return The result path.
     */
    private Path absoluteNormalize(final Path path) {
        return path.toAbsolutePath().normalize();
    }

    /**
     * Checks that that a Path is within our fence.
     *
     * @param path The path to test.
     * @return The given path.
     * @throws IllegalArgumentException Thrown if the path is not within our fence.
     */
    // Cannot implement both UnaryOperator<Path> and Function<String, Path>
    public Path apply(final Path path) {
        return getPath(path.toString(), path);
    }

    /**
     * Gets a Path for the given file name, checking that it is within our fence.
     *
     * @param fileName the file name to test.
     * @return The given path.
     * @throws IllegalArgumentException Thrown if the file name is not within our fence.
     */
    // Cannot implement both UnaryOperator<Path> and Function<String, Path>
    public Path apply(final String fileName) {
        return getPath(fileName, Paths.get(fileName));
    }

    private Path getPath(final String fileName, final Path path) {
        if (roots.isEmpty()) {
            return path;
        }
        final Path pathAbs = absoluteNormalize(path);
        final Optional<Path> first = roots.stream().filter(pathAbs::startsWith).findFirst();
        if (first.isPresent()) {
            return path;
        }
        throw new IllegalArgumentException(String.format("[%s] -> [%s] not in the fence %s", fileName, pathAbs, roots));
    }
}
