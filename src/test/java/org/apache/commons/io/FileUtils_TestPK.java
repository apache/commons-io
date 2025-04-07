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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;



public class FileUtils_TestPK {
    //test with null input 
    @Test
    void testDeleteQuietlyWithNull(){
        File file =null;
        boolean result = FileUtils.deleteQuietly(file);
        assertFalse(result); //expecting false since file is null

    } 
    //Test with a non-existent file
    @Test
    void testDeleteQuietlyWithNonExistentFiles(){
        File file = new File("nonExistentFile.txt");
        boolean result = FileUtils.deleteQuietly(file);
        assertFalse(result); //expecting false since file is null

    } 
   
    //Test with an empty directory (boundary value)
    @Test
    void testDeleteQuietlyWithEmptyDirectory() throws IOException{
        File dir= new File("EmptyDir");
        
        dir.mkdir();// creates an emtpy dir
        boolean result = FileUtils.deleteQuietly(dir);
        assertTrue(result); //expecting true since dir is empty

    } 
   
    //Test with a non-empty directory
    @Test
    void testDeleteQuietlyWithNonEmptyDirectory() throws IOException{
        File dir= new File("nonEmptyDir");
        
        dir.mkdir();// creates a non emtpy dir
        new File(dir,"file1.txt").createNewFile();
        boolean result = FileUtils.deleteQuietly(dir);
        assertTrue(result); //expecting true since dir is empty

    } 
   
    
}
