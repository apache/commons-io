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

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.commons.io.file.Counters.PathCounters;

/**
 * A Path that deletes its delegate on close.
 *
 * @since 2.12.0
 */
public class DeletablePath extends AbstractPathWrapper implements Closeable {

    /**
     * Constructs a new instance wrapping the given delegate.
     *
     * @param path The delegate.
     */
    public DeletablePath(final Path path) {
        super(path);
    }

    @Override
    public void close() throws IOException {
        delete();
    }

    /**
     * Deletes the delegate path.
     *
     * @return The visitor used to delete the given directory.
     * @throws IOException if an I/O error is thrown by a visitor method or if an I/O error occurs.
     */
    public PathCounters delete() throws IOException {
        return delete((DeleteOption[]) null);
    }

    /**
     * Deletes the delegate path.
     * @param deleteOptions How to handle deletion.
     * @return The visitor used to delete the given directory.
     * @throws IOException if an I/O error is thrown by a visitor method or if an I/O error occurs.
     */
    public PathCounters delete(final DeleteOption... deleteOptions) throws IOException {
        return PathUtils.delete(get(), deleteOptions);
    }

}
