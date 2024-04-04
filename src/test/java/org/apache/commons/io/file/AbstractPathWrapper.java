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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Wraps and delegates to a Path for subclasses.
 *
 * @since 2.12.0
 */
public abstract class AbstractPathWrapper implements Path {

    /**
     * The path delegate.
     */
    private final Path path;

    /**
     * Constructs a new instance.
     *
     * @param path The path to wrap.
     */
    protected AbstractPathWrapper(final Path path) {
        this.path = Objects.requireNonNull(path, "path");
    }

    @Override
    public int compareTo(final Path other) {
        return path.compareTo(other);
    }

    @Override
    public boolean endsWith(final Path other) {
        return path.endsWith(other);
    }

    @Override
    public boolean endsWith(final String other) {
        return path.endsWith(other);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AbstractPathWrapper)) {
            return false;
        }
        final AbstractPathWrapper other = (AbstractPathWrapper) obj;
        return Objects.equals(path, other.path);
    }

    /**
     * Delegates to {@link Files#exists(Path, LinkOption...)}.
     *
     * @param options See {@link Files#exists(Path, LinkOption...)}.
     * @return See {@link Files#exists(Path, LinkOption...)}.
     */
    public boolean exists(final LinkOption... options) {
        return Files.exists(path, options);
    }

    @Override
    public void forEach(final Consumer<? super Path> action) {
        path.forEach(action);
    }

    /**
     * Gets the delegate Path.
     *
     * @return the delegate Path.
     */
    public Path get() {
        return path;
    }

    @Override
    public Path getFileName() {
        return path.getFileName();
    }

    @Override
    public FileSystem getFileSystem() {
        return path.getFileSystem();
    }

    @Override
    public Path getName(final int index) {
        return path.getName(index);
    }

    @Override
    public int getNameCount() {
        return path.getNameCount();
    }

    @Override
    public Path getParent() {
        return path.getParent();
    }

    @Override
    public Path getRoot() {
        return path.getRoot();
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

    @Override
    public boolean isAbsolute() {
        return path.isAbsolute();
    }

    @Override
    public Iterator<Path> iterator() {
        return path.iterator();
    }

    @Override
    public Path normalize() {
        return path.normalize();
    }

    @Override
    public WatchKey register(final WatchService watcher, final Kind<?>... events) throws IOException {
        return path.register(watcher, events);
    }

    @Override
    public WatchKey register(final WatchService watcher, final Kind<?>[] events, final Modifier... modifiers) throws IOException {
        return path.register(watcher, events, modifiers);
    }

    @Override
    public Path relativize(final Path other) {
        return path.relativize(other);
    }

    @Override
    public Path resolve(final Path other) {
        return path.resolve(other);
    }

    @Override
    public Path resolve(final String other) {
        return path.resolve(other);
    }

    @Override
    public Path resolveSibling(final Path other) {
        return path.resolveSibling(other);
    }

    @Override
    public Path resolveSibling(final String other) {
        return path.resolveSibling(other);
    }

    @Override
    public Spliterator<Path> spliterator() {
        return path.spliterator();
    }

    @Override
    public boolean startsWith(final Path other) {
        return path.startsWith(other);
    }

    @Override
    public boolean startsWith(final String other) {
        return path.startsWith(other);
    }

    @Override
    public Path subpath(final int beginIndex, final int endIndex) {
        return path.subpath(beginIndex, endIndex);
    }

    @Override
    public Path toAbsolutePath() {
        return path.toAbsolutePath();
    }

    @Override
    public File toFile() {
        return path.toFile();
    }

    @Override
    public Path toRealPath(final LinkOption... options) throws IOException {
        return path.toRealPath(options);
    }

    @Override
    public String toString() {
        return path.toString();
    }

    @Override
    public URI toUri() {
        return path.toUri();
    }

}
