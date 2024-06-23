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
package org.apache.commons.io.monitor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.file.attribute.FileTimes;

/**
 * Wraps a {@link FileTime} and allows it to be Serializable.
 *
 * <h2>Deprecating Serialization</h2>
 * <p>
 * <em>Serialization is deprecated and will be removed in 3.0.</em>
 * </p>
 */
final class SerializableFileTime implements Serializable {

    static final SerializableFileTime EPOCH = new SerializableFileTime(FileTimes.EPOCH);

    private static final long serialVersionUID = 1L;

    private FileTime fileTime;

    public SerializableFileTime(final FileTime fileTime) {
        this.fileTime = Objects.requireNonNull(fileTime);
    }

    public int compareTo(final FileTime other) {
        return fileTime.compareTo(other);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SerializableFileTime)) {
            return false;
        }
        final SerializableFileTime other = (SerializableFileTime) obj;
        return Objects.equals(fileTime, other.fileTime);
    }

    @Override
    public int hashCode() {
        return fileTime.hashCode();
    }

    /**
     * Deserializes an instance from an ObjectInputStream.
     *
     * @param in The source ObjectInputStream.
     * @throws IOException            Any of the usual Input/Output related exceptions.
     * @throws ClassNotFoundException A class of a serialized object cannot be found.
     */
    private void readObject(final ObjectInputStream in) throws ClassNotFoundException, IOException {
        this.fileTime = FileTime.from((Instant) in.readObject());
    }

    long to(final TimeUnit unit) {
        return fileTime.to(unit);
    }

    Instant toInstant() {
        return fileTime.toInstant();
    }

    long toMillis() {
        return fileTime.toMillis();
    }

    @Override
    public String toString() {
        return fileTime.toString();
    }

    FileTime unwrap() {
        return fileTime;
    }

    private void writeObject(final ObjectOutputStream oos) throws IOException {
        oos.writeObject(fileTime.toInstant());
    }
}
