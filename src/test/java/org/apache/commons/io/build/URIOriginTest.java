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
package org.apache.commons.io.build;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.function.Supplier;

import org.apache.commons.io.build.AbstractOrigin.URIOrigin;
import org.apache.commons.io.build.AbstractOrigin.URIOrigin.URIOpenOption;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests {@link URIOrigin}.
 *
 * A URIOrigin can convert into all other aspects.
 *
 * @see URI
 */
class URIOriginTest extends AbstractOriginTest<URI, URIOrigin> {

    // @formatter:off
    private static final URIOpenOption URI_OPEN_OPTION = URIOpenOption.builder()
            .setConnectTimeout(Duration.ofSeconds(60))
            .setReadTimeout(Duration.ofSeconds(60))
            .get();
    // @formatter:on

    static String[] fixtures() {
        return new String[] { "http://1.1.1.1", // IP
                "http://google.com", // HTTP
                "https://apache.org" // HTTPS
        };
    }

    private void checkRead(final InputStream in) throws IOException {
        assertNotEquals(-1, in.read());
    }

    private void checkRead(final Channel in, final Supplier<String> message) throws IOException {
        if (in instanceof ReadableByteChannel) {
            final ReadableByteChannel rbc = (ReadableByteChannel) in;
            assertNotEquals(-1, rbc.read(ByteBuffer.allocate(1)), message);
        }
    }

    @Override
    protected URIOrigin newOriginRo() {
        return new URIOrigin(Paths.get(FILE_NAME_RO).toUri());
    }

    @Override
    protected URIOrigin newOriginRw() {
        return new URIOrigin(tempPath.resolve(FILE_NAME_RW).toUri());
    }

    @Override
    protected void resetOriginRw() throws IOException {
        // Reset the file
        final Path rwPath = tempPath.resolve(FILE_NAME_RW);
        Files.write(rwPath, ArrayUtils.EMPTY_BYTE_ARRAY, StandardOpenOption.CREATE);
    }

    @Test
    void testGetChannelFileURI() throws Exception {
        final AbstractOrigin.URIOrigin origin = getOriginRo().asThis();
        try (Channel in = origin.getChannel()) {
            checkRead(in, origin::toString);
        }
    }

    @ParameterizedTest
    @MethodSource("fixtures")
    void testGetInputStrea(final String uri) throws Exception {
        final AbstractOrigin.URIOrigin origin = new AbstractOrigin.URIOrigin(new URI(uri));
        try (Channel in = origin.getChannel(URI_OPEN_OPTION)) {
            checkRead(in, uri::toString);
        }
    }

    @ParameterizedTest
    @MethodSource("fixtures")
    void testGetInputStream(final String uri) throws Exception {
        final AbstractOrigin.URIOrigin origin = new AbstractOrigin.URIOrigin(new URI(uri));
        try (InputStream in = origin.getInputStream(URI_OPEN_OPTION)) {
            checkRead(in);
        }
    }
    @Test
    void testGetInputStreamFileURI() throws Exception {
        final AbstractOrigin.URIOrigin origin = getOriginRo().asThis();
        try (InputStream in = origin.getInputStream()) {
            checkRead(in);
        }
    }
}
