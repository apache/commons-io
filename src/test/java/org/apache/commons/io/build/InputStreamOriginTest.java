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

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.commons.io.build.AbstractOrigin.InputStreamOrigin;
import org.apache.commons.io.input.CharSequenceInputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link InputStreamOrigin}.
 *
 * A InputStreamOrigin can convert into some of the other aspects.
 */
public class InputStreamOriginTest extends AbstractOriginTest<InputStream, InputStreamOrigin> {

    @SuppressWarnings("resource")
    @BeforeEach
    public void beforeEach() throws FileNotFoundException {
        setOriginRo(new InputStreamOrigin(new FileInputStream(FILE_NAME_RO)));
        setOriginRw(new InputStreamOrigin(CharSequenceInputStream.builder().setCharSequence("World").get()));
    }

    @Override
    @Test
    public void testGetFile() {
        // Cannot convert a InputStream to a File.
        assertThrows(UnsupportedOperationException.class, super::testGetFile);
    }

    @Override
    @Test
    public void testGetOutputStream() {
        // Cannot convert a InputStream to an OutputStream.
        assertThrows(UnsupportedOperationException.class, super::testGetOutputStream);
    }

    @Override
    @Test
    public void testGetPath() {
        // Cannot convert a InputStream to a Path.
        assertThrows(UnsupportedOperationException.class, super::testGetPath);
    }

    @Override
    @Test
    public void testGetWriter() {
        // Cannot convert a InputStream to a Writer.
        assertThrows(UnsupportedOperationException.class, super::testGetWriter);
    }

}
