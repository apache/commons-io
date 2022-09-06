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

import static org.apache.commons.io.input.ReversedLinesFileReaderTestParamBlockSize.assertEqualsAndNoLineBreaks;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.TestResources;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class ReversedLinesFileReaderTestRandomAccess {

  private ReversedLinesFileReader reversedLinesFileReader;
  private static final String testLine1 =
          "A Test Line. Special chars: "
                  .concat("\u00C4\u00E4\u00DC\u00FC\u00D6\u00F6\u00DF ")
                  .concat("\u00C3\u00E1\u00E9\u00ED\u00EF\u00E7\u00F1\u00C2 ")
                  .concat("\u00A9\u00B5\u00A5\u00A3");
  private static final String testLine2 = testLine1 +"\u00B1";
  private static final String testLine3 = testLine2 +"\u00B2";
  private static final String testLine4 = testLine3 +"\u00AE";

  @AfterEach
  public void closeReader() {
      try {
          reversedLinesFileReader.close();
      } catch(final Exception e) {
          // ignore
      }
  }

  @Test
  public void testSeekO() throws URISyntaxException, IOException {
      final File testFile = TestResources.getFile("/test-file-utf8-win-linebr.bin");
      reversedLinesFileReader = new ReversedLinesFileReader(testFile, StandardCharsets.UTF_8);
      reversedLinesFileReader.seek(0);
      assertEquals(0, reversedLinesFileReader.getFilePointer());
      assertNull(reversedLinesFileReader.readLine());
  }

  @Test
  public void testSeekWithLineBR() throws URISyntaxException, IOException {
      final File testFile= TestResources.getFile("/test-file-utf8-win-linebr.bin");
      reversedLinesFileReader = new ReversedLinesFileReader(testFile, StandardCharsets.UTF_8);
      reversedLinesFileReader.seek(290);
      assertEquals(290, reversedLinesFileReader.getFilePointer());
      assertEqualsAndNoLineBreaks(testLine1, reversedLinesFileReader.readLine());
      assertEqualsAndNoLineBreaks(testLine2, reversedLinesFileReader.readLine());
      assertEqualsAndNoLineBreaks(testLine3, reversedLinesFileReader.readLine());
      assertEqualsAndNoLineBreaks(testLine4, reversedLinesFileReader.readLine());
      assertNull(reversedLinesFileReader.readLine());
  }

  @Test
    public void testSeekMiddleLineBR() throws URISyntaxException, IOException {
        final File testFile= TestResources.getFile("/test-file-utf8-win-linebr.bin");
        reversedLinesFileReader = new ReversedLinesFileReader(testFile, StandardCharsets.UTF_8);
        reversedLinesFileReader.seek(288);
        assertEquals(288, reversedLinesFileReader.getFilePointer());
        assertEqualsAndNoLineBreaks(testLine1.substring(0, testLine1.length() - 1), reversedLinesFileReader.readLine());
        assertEqualsAndNoLineBreaks(testLine2, reversedLinesFileReader.readLine());
        assertEqualsAndNoLineBreaks(testLine3, reversedLinesFileReader.readLine());
        assertEqualsAndNoLineBreaks(testLine4, reversedLinesFileReader.readLine());
        assertNull(reversedLinesFileReader.readLine());
  }

    @Test
    public void testSeekEndWithLineBR() throws URISyntaxException, IOException {
        final File testFile= TestResources.getFile("/test-file-utf8-win-linebr.bin");
        reversedLinesFileReader = new ReversedLinesFileReader(testFile, StandardCharsets.UTF_8);
        reversedLinesFileReader.seek(1757);
        assertEquals(1757, reversedLinesFileReader.getFilePointer());
        assertEqualsAndNoLineBreaks("A", reversedLinesFileReader.readLine());
        assertEqualsAndNoLineBreaks("A ", reversedLinesFileReader.readLine());
        assertEqualsAndNoLineBreaks("A T", reversedLinesFileReader.readLine());
    }

  @Test
  public void testSeekWithLineCR() throws URISyntaxException, IOException {
      final File testFile= TestResources.getFile("/test-file-utf8-cr-only.bin");
      reversedLinesFileReader = new ReversedLinesFileReader(testFile, StandardCharsets.UTF_8);
      reversedLinesFileReader.seek(287);
      assertEquals(287, reversedLinesFileReader.getFilePointer());
      assertEqualsAndNoLineBreaks(testLine1, reversedLinesFileReader.readLine());
      assertEqualsAndNoLineBreaks(testLine2, reversedLinesFileReader.readLine());
  }

    @Test
    public void testSeekMiddleLineCR() throws URISyntaxException, IOException {
        final File testFile= TestResources.getFile("/test-file-utf8-cr-only.bin");
        reversedLinesFileReader = new ReversedLinesFileReader(testFile, StandardCharsets.UTF_8);
        reversedLinesFileReader.seek(285);
        assertEquals(285, reversedLinesFileReader.getFilePointer());
        assertEqualsAndNoLineBreaks(testLine1.substring(0, testLine1.length() - 1), reversedLinesFileReader.readLine());
        assertEqualsAndNoLineBreaks(testLine2, reversedLinesFileReader.readLine());
        assertEqualsAndNoLineBreaks(testLine3, reversedLinesFileReader.readLine());
        assertEqualsAndNoLineBreaks(testLine4, reversedLinesFileReader.readLine());
        assertNull(reversedLinesFileReader.readLine());
    }

    @Test
    public void testSeekEndWithLineCR() throws URISyntaxException, IOException {
        final File testFile= TestResources.getFile("/test-file-utf8-cr-only.bin");
        reversedLinesFileReader = new ReversedLinesFileReader(testFile, StandardCharsets.UTF_8);
        reversedLinesFileReader.seek(1705);
        assertEquals(1705, reversedLinesFileReader.getFilePointer());
        assertEqualsAndNoLineBreaks("A", reversedLinesFileReader.readLine());
        assertEqualsAndNoLineBreaks("A ", reversedLinesFileReader.readLine());
        assertEqualsAndNoLineBreaks("A T", reversedLinesFileReader.readLine());
    }

    @Test
    public void testSeekEndAfterEOF() throws URISyntaxException, IOException {
        final File testFile= TestResources.getFile("/test-file-utf8-cr-only.bin");
        reversedLinesFileReader = new ReversedLinesFileReader(testFile, StandardCharsets.UTF_8);
        assertThrows(IllegalStateException.class, () -> {
            reversedLinesFileReader.seek(2730);
        });
    }

    @Test
    public void testSeekBelowZero() throws URISyntaxException, IOException {
        final File testFile= TestResources.getFile("/test-file-utf8-cr-only.bin");
        reversedLinesFileReader = new ReversedLinesFileReader(testFile, StandardCharsets.UTF_8);
        assertThrows(IllegalArgumentException.class, () -> {
            reversedLinesFileReader.seek(-1);
        });
    }
}
