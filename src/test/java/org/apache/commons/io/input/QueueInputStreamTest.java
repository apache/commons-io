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
package org.apache.commons.io.input;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.QueueOutputStream;
import org.apache.commons.io.output.QueueOutputStreamTest;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test {@link QueueInputStream}.
 * 
 * See more tests in {@link QueueOutputStreamTest}
 */
public class QueueInputStreamTest {

    @Test
    public void readString() throws Exception {
        try (final QueueInputStream inputStream = new QueueInputStream();
                final QueueOutputStream outputStream = inputStream.newQueueOutputStream()) {
            outputStream.write("ABC".getBytes(StandardCharsets.UTF_8));
            final String value = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            assertEquals("ABC", value);
        }
    }
}

