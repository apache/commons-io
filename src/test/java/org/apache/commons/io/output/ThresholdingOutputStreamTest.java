package org.apache.commons.io.output;
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
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

public class ThresholdingOutputStreamTest {

    @Test
    public void testSetByteCount() throws Exception {
        final AtomicBoolean reached = new AtomicBoolean(false);
        ThresholdingOutputStream tos = new ThresholdingOutputStream(3) {
            {
                setByteCount(2);
            }
            @Override
            protected OutputStream getStream() throws IOException {
                return new ByteArrayOutputStream(4);
            }

            @Override
            protected void thresholdReached() throws IOException {
                reached.set( true);
            }
        };

        tos.write(12);
        assertFalse( reached.get());
        tos.write(12);
        assertTrue(reached.get());
        tos.close();
    }
}