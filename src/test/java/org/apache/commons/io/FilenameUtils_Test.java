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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FilenameUtils_Test {
   // Test Cases for getBaseName()

     String fileName ;
     // run before each test
    @BeforeEach
    void setUp(){
     fileName = "";
    }
    
    //test to check an empty file name
    @Test
    void testEmptyFileName(){
        fileName = "";
        String baseName = FilenameUtils.getBaseName(fileName);
        assertEquals("", baseName , "Base name should be empty for a empty input");
    }

    //Test to check single character file

    @Test
    void testSingleCharacterFileName(){
        fileName = "p";
        String baseName = FilenameUtils.getBaseName(fileName);
        assertEquals("p", baseName, "Base name should be the same for a single character file name");

    }

    //Test to check file name with extension: typical case
    @Test
    void testFileNameWithExtension(){
        fileName = "document.text";
        String baseName = FilenameUtils.getBaseName(fileName);
        assertEquals("document", baseName, " Base name should be 'document' when file name has an extension.");
    }
    //Test with no extension 
    @Test
    void testFileNameWithNoExtension(){
        fileName = "document";
        String baseName = FilenameUtils.getBaseName(fileName);
        assertEquals("document", baseName, " Base name should be the same as'document' when file name has no extension.");
    }
    // Test with special Character
    @Test
    void testFileNameWithSpecialCharacters(){
        fileName = "doc@file#name.txt";
        String baseName = FilenameUtils.getBaseName(fileName);
        assertEquals("doc@file#name", baseName, " Base name should correctly handle special characters");
    }
    // Test with numbers in filename 
    @Test
    void testFileNameWithNumbers(){
        fileName = "doc1234.txt";
        String baseName = FilenameUtils.getBaseName(fileName);
        assertEquals("doc1234", baseName, " Base name should correctly handle numbers");
    }
    // Test with multiple dots
    @Test
    void testFileNameWithMultipleDots(){
        fileName = "document.excel.dot.txt";
        String baseName = FilenameUtils.getBaseName(fileName);
        assertEquals("document.excel.dot", baseName, " Base name should remove only one extension");
    }
    //Test involving full path of file
    @Test
    void testFileNameWithFullPath(){
        fileName = "C:\\Users\\14197\\Documents\\MyCode.txt";
        String baseName = FilenameUtils.getBaseName(fileName);
        assertEquals("MyCode", baseName, " Base name should give only file name");
    }
    //Test with different formats
    @Test
    void testFileNameWithDiffFormat(){
        fileName = "Document.pdf";
        String baseName = FilenameUtils.getBaseName(fileName);
        assertEquals("Document", baseName, " Base name work perfectly with different formats.");
    }

 
    // Test for getExtension() starts here

    //Test to get extension of a valid filename
    @Test 
    void testGetExtensionForValidFileName(){
     fileName="file.txt";
   
    assertEquals("txt", FilenameUtils.getExtension(fileName));
    }
}
