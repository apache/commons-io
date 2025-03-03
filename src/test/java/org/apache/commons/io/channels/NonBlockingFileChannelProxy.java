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

package org.apache.commons.io.channels;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

/**
 * Simulates a non-blocking file channel by returning 0 from reads every other call as allowed by {@link ReadableByteChannel} and {@link FileChannel}.
 */
class NonBlockingFileChannelProxy extends FileChannelProxy {

    boolean toggleRead0;

    NonBlockingFileChannelProxy(final FileChannel fileChannel) {
        super(fileChannel);
    }

    private boolean flipState() {
        return toggleRead0 = !toggleRead0;
    }

    @Override
    public int read(final ByteBuffer dst) throws IOException {
        return flipState() ? 0 : super.read(dst);
    }

    @Override
    public int read(final ByteBuffer dst, final long position) throws IOException {
        flipState();
        return flipState() ? 0 : super.read(dst, position);
    }

    @Override
    public long read(final ByteBuffer[] dsts, final int offset, final int length) throws IOException {
        return flipState() ? 0 : super.read(dsts, offset, length);
    }
}
