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
package org.apache.commons.io.build;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Paths;

import org.apache.commons.io.build.AbstractOrigin.URIOrigin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests {@link URIOrigin}.
 *
 * A URIOrigin can convert into all other aspects.
 *
 * @see URI
 */
public class URIOriginTest extends AbstractOriginTest<URI, URIOrigin> {

    @Override
    protected URIOrigin newOriginRo() {
        return new URIOrigin(Paths.get(FILE_NAME_RO).toUri());
    }

    @Override
    protected URIOrigin newOriginRw() {
        return new URIOrigin(Paths.get(FILE_NAME_RW).toUri());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "http://example.com",
            "https://example.com"
    })
    void testGetInputStream(final String uri) throws Exception {
        final AbstractOrigin.URIOrigin origin = new AbstractOrigin.URIOrigin(new URI(uri));
        try (InputStream in = origin.getInputStream()) {
            assertNotEquals(-1, in.read());
        }
    }

    @Test
    void testGetInputStreamFileURI() throws Exception {
        final AbstractOrigin.URIOrigin origin = getOriginRo().asThis();
        try (InputStream in = origin.getInputStream()) {
            assertNotEquals(-1, in.read());
        }
    }
}
