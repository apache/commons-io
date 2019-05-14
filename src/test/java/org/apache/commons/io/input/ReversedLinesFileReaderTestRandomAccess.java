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
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import org.junit.After;
import org.junit.Test;


public class ReversedLinesFileReaderTestRandomAccess {

  private ReversedLinesFileReader reversedLinesFileReader;
  private static final String testLine1 = "A Test Line. Special chars: \u00C4\u00E4\u00DC\u00FC\u00D6\u00F6\u00DF \u00C3\u00E1\u00E9\u00ED\u00EF\u00E7\u00F1\u00C2 \u00A9\u00B5\u00A5\u00A3";
  private static final String testLine2 = testLine1 +"\u00B1";

  
  @After
  public void closeReader() {
      try {
          reversedLinesFileReader.close();
      } catch(final Exception e) {
          // ignore
      }
  }
  
  @Test
  public void testSeekO() throws URISyntaxException, IOException {
      final File testFile= new File(this.getClass().getResource("/test-file-utf8-win-linebr.bin").toURI());
      reversedLinesFileReader = new ReversedLinesFileReader(testFile, StandardCharsets.UTF_8);
      reversedLinesFileReader.seek(0);      
      assertNull(reversedLinesFileReader.readLine());
  }

  @Test
  public void testSeekWithLineBR() throws URISyntaxException, IOException {
      final File testFile= new File(this.getClass().getResource("/test-file-utf8-win-linebr.bin").toURI());
      reversedLinesFileReader = new ReversedLinesFileReader(testFile, StandardCharsets.UTF_8);
      reversedLinesFileReader.seek(290);
      assertEqualsAndNoLineBreaks(testLine1, reversedLinesFileReader.readLine());
      assertEqualsAndNoLineBreaks(testLine2, reversedLinesFileReader.readLine());
  }
  
  @Test
  public void testSeekWithLineCR() throws URISyntaxException, IOException {
      final File testFile= new File(this.getClass().getResource("/test-file-utf8-cr-only.bin").toURI());
      reversedLinesFileReader = new ReversedLinesFileReader(testFile, StandardCharsets.UTF_8);
      reversedLinesFileReader.seek(287);
      assertEqualsAndNoLineBreaks(testLine1, reversedLinesFileReader.readLine());
      assertEqualsAndNoLineBreaks(testLine2, reversedLinesFileReader.readLine());
  }

}
