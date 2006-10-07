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

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * A basic test suite that tests all the IO package.
 * 
 * @author Matthew Hawthorne
 * @author Stephen Colebourne
 * @see org.apache.commons.io
 */
public class PackageTestSuite {

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("IO Utilities");
        suite.addTest(new TestSuite(CopyUtilsTest.class));
        suite.addTest(new TestSuite(DemuxTestCase.class));
        suite.addTest(new TestSuite(DirectoryWalkerTestCase.class));
        suite.addTest(new TestSuite(EndianUtilsTest.class));
        suite.addTest(new TestSuite(FileCleanerTestCase.class));
        suite.addTest(new TestSuite(FileDeleteStrategyTestCase.class));
        suite.addTest(new TestSuite(FilenameUtilsTestCase.class));
        suite.addTest(new TestSuite(FilenameUtilsWildcardTestCase.class));
        suite.addTest(new TestSuite(FileSystemUtilsTestCase.class));
        suite.addTest(new TestSuite(FileUtilsFileNewerTestCase.class));
        suite.addTest(new TestSuite(FileUtilsListFilesTestCase.class));
        suite.addTest(new TestSuite(FileUtilsCleanDirectoryTestCase.class));
        suite.addTest(new TestSuite(FileUtilsTestCase.class));
        suite.addTest(new TestSuite(HexDumpTest.class));
        suite.addTest(new TestSuite(IOCaseTestCase.class));
        suite.addTest(new TestSuite(IOUtilsCopyTestCase.class));
        suite.addTest(new TestSuite(IOUtilsTestCase.class));
        suite.addTest(new TestSuite(IOUtilsWriteTestCase.class));
        suite.addTest(new TestSuite(LineIteratorTestCase.class));
        suite.addTest(new TestSuite(FileUtilsWaitForTestCase.class));
        return suite;
    }
}
