/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. */
package org.apache.commons.io;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.testtools.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;


/**
 * This is used to test FileUtils for correctness.
 */
public class FileUtilsCopyToFileTestCase {

    @TempDir
    public File temporaryFolder;

    private File testFile;

    private byte[] testData;

    @BeforeEach
    public void setUp() throws Exception {
        testFile = new File(temporaryFolder, "file1-test.txt");
        if(!testFile.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testFile +
                " as the parent directory does not exist");
        }

        testData = TestUtils.generateTestData(1024);
    }

    /**
     * Tests that <code>copyToFile(InputStream, File)</code> does not close the input stream.
     *
     * @throws IOException
     * @see FileUtils#copyToFile(InputStream, File)
     * @see FileUtils#copyInputStreamToFile(InputStream, File)
     */
    @Test
    public void testCopyToFile() throws IOException {
        try(CheckingInputStream inputStream = new CheckingInputStream(testData)) {
            FileUtils.copyToFile(inputStream, testFile);
            assertFalse(inputStream.isClosed(), "inputStream should NOT be closed");
        }
    }

    /**
     * Tests that <code>copyInputStreamToFile(InputStream, File)</code> closes the input stream.
     *
     * @throws IOException
     * @see FileUtils#copyInputStreamToFile(InputStream, File)
     * @see FileUtils#copyToFile(InputStream, File)
     */
    @Test
    public void testCopyInputStreamToFile() throws IOException {
        try(CheckingInputStream inputStream = new CheckingInputStream(testData)) {
            FileUtils.copyInputStreamToFile(inputStream, testFile);
            assertTrue(inputStream.isClosed(), "inputStream should be closed");
        }
    }

    private class CheckingInputStream extends ByteArrayInputStream {
        private boolean closed;

        public CheckingInputStream(final byte[] data) {
            super(data);
            closed = false;
        }

        @Override
        public void close() throws IOException {
            super.close();
            closed = true;
        }

        public boolean isClosed() {
            return closed;
        }
    }
}
