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

import java.util.concurrent.atomic.AtomicLong;

/**
 * Counts files, directories, and sizes, as a visit proceeds.
 *
 * @since 2.7
 */
public class PathCounts {

    final AtomicLong byteCount = new AtomicLong();
    final AtomicLong directoryCount = new AtomicLong();
    final AtomicLong fileCount = new AtomicLong();

    /**
     * Gets the byte count of visited files.
     *
     * @return the byte count of visited files.
     */
    public long getByteCount() {
        return this.byteCount.get();
    }

    /**
     * Gets the count of visited directories.
     *
     * @return the count of visited directories.
     */
    public long getDirectoryCount() {
        return this.directoryCount.get();
    }

    /**
     * Gets the count of visited files.
     *
     * @return the byte count of visited files.
     */
    public long getFileCount() {
        return this.fileCount.get();
    }

    @Override
    public String toString() {
        return String.format("%,d files, %,d directories, %,d bytes", Long.valueOf(fileCount.longValue()),
                Long.valueOf(directoryCount.longValue()), Long.valueOf(byteCount.longValue()));
    }

}
