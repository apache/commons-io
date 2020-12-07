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
package org.apache.commons.io.output;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.Writer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * JUnit Test Case for {@link CloseShieldWriter}.
 */
public class CloseShieldWriterTest {

    private StringBuilderWriter original;

    private Writer shielded;

    @BeforeEach
    public void setUp() {
        original = spy(new StringBuilderWriter());
        shielded = CloseShieldWriter.wrap(original);
    }

    @Test
    public void testClose() throws IOException {
        shielded.close();
        verify(original, never()).close();
        assertThrows(IOException.class, () -> shielded.write('x'), "write(c)");
        original.write('y');
        assertEquals(1, original.getBuilder().length());
        assertEquals('y', original.toString().charAt(0));
    }

}
