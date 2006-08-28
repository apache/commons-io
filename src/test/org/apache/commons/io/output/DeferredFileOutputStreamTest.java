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

import junit.framework.TestCase;
//import org.apache.commons.fileupload.DeferredFileOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

/**
 * Unit tests for the <code>DeferredFileOutputStream</code> class.
 *
 * @author <a href="mailto:martinc@apache.org">Martin Cooper</a>
 *
 * @version $Id$
 */
public class DeferredFileOutputStreamTest extends TestCase
 {

    /**
     * The test data as a string (which is the simplest form).
     */
    private String testString = "0123456789";

    /**
     * The test data as a byte array, derived from the string.
     */
    private byte[] testBytes = testString.getBytes();

    /**
     * Standard JUnit test case constructor.
     *
     * @param name The name of the test case.
     */
    public DeferredFileOutputStreamTest(String name)
    {
        super(name);
    }

    /**
     * Tests the case where the amount of data falls below the threshold, and
     * is therefore confined to memory.
     */
    public void testBelowThreshold()
    {
        DeferredFileOutputStream dfos =
                new DeferredFileOutputStream(testBytes.length + 42, null);
        try
        {
            dfos.write(testBytes, 0, testBytes.length);
            dfos.close();
        }
        catch (IOException e) {
            fail("Unexpected IOException");
        }
        assertTrue(dfos.isInMemory());

        byte[] resultBytes = dfos.getData();
        assertTrue(resultBytes.length == testBytes.length);
        assertTrue(Arrays.equals(resultBytes, testBytes));
    }

    /**
     * Tests the case where the amount of data is exactly the same as the
     * threshold. The behavior should be the same as that for the amount of
     * data being below (i.e. not exceeding) the threshold.
     */
    public void testAtThreshold() {
        DeferredFileOutputStream dfos =
                new DeferredFileOutputStream(testBytes.length, null);
        try
        {
            dfos.write(testBytes, 0, testBytes.length);
            dfos.close();
        }
        catch (IOException e) {
            fail("Unexpected IOException");
        }
        assertTrue(dfos.isInMemory());

        byte[] resultBytes = dfos.getData();
        assertTrue(resultBytes.length == testBytes.length);
        assertTrue(Arrays.equals(resultBytes, testBytes));
    }

    /**
     * Tests the case where the amount of data exceeds the threshold, and is
     * therefore written to disk. The actual data written to disk is verified,
     * as is the file itself.
     */
    public void testAboveThreshold() {
        File testFile = new File("testAboveThreshold.dat");

        // Ensure that the test starts from a clean base.
        testFile.delete();

        DeferredFileOutputStream dfos =
                new DeferredFileOutputStream(testBytes.length - 5, testFile);
        try
        {
            dfos.write(testBytes, 0, testBytes.length);
            dfos.close();
        }
        catch (IOException e) {
            fail("Unexpected IOException");
        }
        assertFalse(dfos.isInMemory());
        assertNull(dfos.getData());

        verifyResultFile(testFile);

        // Ensure that the test starts from a clean base.
        testFile.delete();
    }

    /**
     * Tests the case where there are multiple writes beyond the threshold, to
     * ensure that the <code>thresholdReached()</code> method is only called
     * once, as the threshold is crossed for the first time.
     */
    public void testThresholdReached() {
        File testFile = new File("testThresholdReached.dat");

        // Ensure that the test starts from a clean base.
        testFile.delete();

        DeferredFileOutputStream dfos =
                new DeferredFileOutputStream(testBytes.length / 2, testFile);
        int chunkSize = testBytes.length / 3;

        try
        {
            dfos.write(testBytes, 0, chunkSize);
            dfos.write(testBytes, chunkSize, chunkSize);
            dfos.write(testBytes, chunkSize * 2,
                    testBytes.length - chunkSize * 2);
            dfos.close();
        }
        catch (IOException e) {
            fail("Unexpected IOException");
        }
        assertFalse(dfos.isInMemory());
        assertNull(dfos.getData());

        verifyResultFile(testFile);

        // Ensure that the test starts from a clean base.
        testFile.delete();
    }
    

    /**
     * Test wether writeTo() properly writes small content.
     */
    public void testWriteToSmall(){
        File testFile = new File("testWriteToMem.dat");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // Ensure that the test starts from a clean base.
        testFile.delete();

        DeferredFileOutputStream dfos =
                new DeferredFileOutputStream(testBytes.length *2, testFile);
        try{
            dfos.write(testBytes);

            assertFalse(testFile.exists());
            assertTrue(dfos.isInMemory());
            
            try {
                dfos.writeTo(baos);
                fail("Should not have been able to write before closing");
            } catch (IOException ioe) {
                // ok, as expected
            }
        
            dfos.close();
            dfos.writeTo(baos);
        } catch (IOException ioe) {
            fail("Unexpected IOException");
        }
        byte[] copiedBytes  = baos.toByteArray();
        assertTrue(Arrays.equals(testBytes, copiedBytes));

        testFile.delete();
    }

    /**
     * Test wether writeTo() properly writes large content.
     */
    public void testWriteToLarge(){
        File testFile = new File("testWriteToFile.dat");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // Ensure that the test starts from a clean base.
        testFile.delete();

        DeferredFileOutputStream dfos =
                new DeferredFileOutputStream(testBytes.length /2, testFile);
        try{
            dfos.write(testBytes);

            assertTrue(testFile.exists());
            assertFalse(dfos.isInMemory());
            
            try {
                dfos.writeTo(baos);
                fail("Should not have been able to write before closeing");
            } catch (IOException ioe) {
                // ok, as expected
            }
        
            dfos.close();
            dfos.writeTo(baos);
        } catch (IOException ioe) {
            fail("Unexpected IOException");
        }
        byte[] copiedBytes  = baos.toByteArray();
        assertTrue(Arrays.equals(testBytes, copiedBytes));
        verifyResultFile(testFile);
        testFile.delete();
    }

    /**
     * Verifies that the specified file contains the same data as the original
     * test data.
     *
     * @param testFile The file containing the test output.
     */
    private void verifyResultFile(File testFile) {
        try
        {
            FileInputStream fis = new FileInputStream(testFile);
            assertTrue(fis.available() == testBytes.length);

            byte[] resultBytes = new byte[testBytes.length];
            assertTrue(fis.read(resultBytes) == testBytes.length);

            assertTrue(Arrays.equals(resultBytes, testBytes));
            assertTrue(fis.read(resultBytes) == -1);

            try
            {
                fis.close();
            }
            catch (IOException e) {
                // Ignore an exception on close
            }
        }
        catch (FileNotFoundException e) {
            fail("Unexpected FileNotFoundException");
        }
        catch (IOException e) {
            fail("Unexpected IOException");
        }
    }
}
