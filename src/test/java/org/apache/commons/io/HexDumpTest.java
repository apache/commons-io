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
package org.apache.commons.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.jupiter.api.Test;


/**
 *
 */
public class HexDumpTest {

    private char toHex(final int n) {
        final char[] hexChars =
                {
                    '0', '1', '2', '3', '4', '5', '6', '7',
                    '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
                };

        return hexChars[n % 16];
    }

    @Test
    public void testDump()
            throws IOException {
        final byte[] testArray = new byte[256];

        for (int j = 0; j < 256; j++) {
            testArray[j] = (byte) j;
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        HexDump.dump(testArray, 0, stream, 0);
        byte[] outputArray = new byte[16 * (73 + HexDump.EOL.length())];

        for (int j = 0; j < 16; j++) {
            int offset = (73 + HexDump.EOL.length()) * j;

            outputArray[offset++] = (byte) '0';
            outputArray[offset++] = (byte) '0';
            outputArray[offset++] = (byte) '0';
            outputArray[offset++] = (byte) '0';
            outputArray[offset++] = (byte) '0';
            outputArray[offset++] = (byte) '0';
            outputArray[offset++] = (byte) toHex(j);
            outputArray[offset++] = (byte) '0';
            outputArray[offset++] = (byte) ' ';
            for (int k = 0; k < 16; k++) {
                outputArray[offset++] = (byte) toHex(j);
                outputArray[offset++] = (byte) toHex(k);
                outputArray[offset++] = (byte) ' ';
            }
            for (int k = 0; k < 16; k++) {
                outputArray[offset++] = (byte) toAscii((j * 16) + k);
            }
            System.arraycopy(HexDump.EOL.getBytes(), 0, outputArray, offset,
                    HexDump.EOL.getBytes().length);
        }
        byte[] actualOutput = stream.toByteArray();

        assertEquals(outputArray.length, actualOutput.length, "array size mismatch");
        for (int j = 0; j < outputArray.length; j++) {
            assertEquals(outputArray[j], actualOutput[j], "array[ " + j + "] mismatch");
        }

        // verify proper behavior with non-zero offset
        stream = new ByteArrayOutputStream();
        HexDump.dump(testArray, 0x10000000, stream, 0);
        outputArray = new byte[16 * (73 + HexDump.EOL.length())];
        for (int j = 0; j < 16; j++) {
            int offset = (73 + HexDump.EOL.length()) * j;

            outputArray[offset++] = (byte) '1';
            outputArray[offset++] = (byte) '0';
            outputArray[offset++] = (byte) '0';
            outputArray[offset++] = (byte) '0';
            outputArray[offset++] = (byte) '0';
            outputArray[offset++] = (byte) '0';
            outputArray[offset++] = (byte) toHex(j);
            outputArray[offset++] = (byte) '0';
            outputArray[offset++] = (byte) ' ';
            for (int k = 0; k < 16; k++) {
                outputArray[offset++] = (byte) toHex(j);
                outputArray[offset++] = (byte) toHex(k);
                outputArray[offset++] = (byte) ' ';
            }
            for (int k = 0; k < 16; k++) {
                outputArray[offset++] = (byte) toAscii((j * 16) + k);
            }
            System.arraycopy(HexDump.EOL.getBytes(), 0, outputArray, offset,
                    HexDump.EOL.getBytes().length);
        }
        actualOutput = stream.toByteArray();
        assertEquals(outputArray.length, actualOutput.length, "array size mismatch");
        for (int j = 0; j < outputArray.length; j++) {
            assertEquals(outputArray[j], actualOutput[j], "array[ " + j + "] mismatch");
        }

        // verify proper behavior with negative offset
        stream = new ByteArrayOutputStream();
        HexDump.dump(testArray, 0xFF000000, stream, 0);
        outputArray = new byte[16 * (73 + HexDump.EOL.length())];
        for (int j = 0; j < 16; j++) {
            int offset = (73 + HexDump.EOL.length()) * j;

            outputArray[offset++] = (byte) 'F';
            outputArray[offset++] = (byte) 'F';
            outputArray[offset++] = (byte) '0';
            outputArray[offset++] = (byte) '0';
            outputArray[offset++] = (byte) '0';
            outputArray[offset++] = (byte) '0';
            outputArray[offset++] = (byte) toHex(j);
            outputArray[offset++] = (byte) '0';
            outputArray[offset++] = (byte) ' ';
            for (int k = 0; k < 16; k++) {
                outputArray[offset++] = (byte) toHex(j);
                outputArray[offset++] = (byte) toHex(k);
                outputArray[offset++] = (byte) ' ';
            }
            for (int k = 0; k < 16; k++) {
                outputArray[offset++] = (byte) toAscii((j * 16) + k);
            }
            System.arraycopy(HexDump.EOL.getBytes(), 0, outputArray, offset,
                    HexDump.EOL.getBytes().length);
        }
        actualOutput = stream.toByteArray();
        assertEquals(outputArray.length, actualOutput.length, "array size mismatch");
        for (int j = 0; j < outputArray.length; j++) {
            assertEquals(outputArray[j], actualOutput[j], "array[ " + j + "] mismatch");
        }

        // verify proper behavior with non-zero index
        stream = new ByteArrayOutputStream();
        HexDump.dump(testArray, 0x10000000, stream, 0x81);
        outputArray = new byte[(8 * (73 + HexDump.EOL.length())) - 1];
        for (int j = 0; j < 8; j++) {
            int offset = (73 + HexDump.EOL.length()) * j;

            outputArray[offset++] = (byte) '1';
            outputArray[offset++] = (byte) '0';
            outputArray[offset++] = (byte) '0';
            outputArray[offset++] = (byte) '0';
            outputArray[offset++] = (byte) '0';
            outputArray[offset++] = (byte) '0';
            outputArray[offset++] = (byte) toHex(j + 8);
            outputArray[offset++] = (byte) '1';
            outputArray[offset++] = (byte) ' ';
            for (int k = 0; k < 16; k++) {
                final int index = 0x81 + (j * 16) + k;

                if (index < 0x100) {
                    outputArray[offset++] = (byte) toHex(index / 16);
                    outputArray[offset++] = (byte) toHex(index);
                } else {
                    outputArray[offset++] = (byte) ' ';
                    outputArray[offset++] = (byte) ' ';
                }
                outputArray[offset++] = (byte) ' ';
            }
            for (int k = 0; k < 16; k++) {
                final int index = 0x81 + (j * 16) + k;

                if (index < 0x100) {
                    outputArray[offset++] = (byte) toAscii(index);
                }
            }
            System.arraycopy(HexDump.EOL.getBytes(), 0, outputArray, offset,
                    HexDump.EOL.getBytes().length);
        }
        actualOutput = stream.toByteArray();
        assertEquals(outputArray.length, actualOutput.length, "array size mismatch");
        for (int j = 0; j < outputArray.length; j++) {
            assertEquals(outputArray[j], actualOutput[j], "array[ " + j + "] mismatch");
        }

        // verify proper behavior with negative index
        try {
            HexDump.dump(testArray, 0x10000000, new ByteArrayOutputStream(),
                    -1);
            fail("should have caught ArrayIndexOutOfBoundsException on negative index");
        } catch (final ArrayIndexOutOfBoundsException ignored_exception) {

            // as expected
        }

        // verify proper behavior with index that is too large
        try {
            HexDump.dump(testArray, 0x10000000, new ByteArrayOutputStream(),
                    testArray.length);
            fail("should have caught ArrayIndexOutOfBoundsException on large index");
        } catch (final ArrayIndexOutOfBoundsException ignored_exception) {

            // as expected
        }

        // verify proper behavior with null stream
        try {
            HexDump.dump(testArray, 0x10000000, null, 0);
            fail("should have caught IllegalArgumentException on negative index");
        } catch (final IllegalArgumentException ignored_exception) {

            // as expected
        }
    }

    private char toAscii(final int c) {
        char rval = '.';

        if ((c >= 32) && (c <= 126)) {
            rval = (char) c;
        }
        return rval;
    }
}
