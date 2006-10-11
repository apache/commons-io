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

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * A basic test suite that tests all the IO output package.
 * 
 * @author Stephen Colebourne
 */
public class PackageTestSuite {

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("IO Utilities - output");
        suite.addTest(new TestSuite(ByteArrayOutputStreamTestCase.class));
        suite.addTest(new TestSuite(CountingOutputStreamTest.class));
        suite.addTest(new TestSuite(DeferredFileOutputStreamTest.class));
        suite.addTest(new TestSuite(LockableFileWriterTest.class));
        suite.addTest(new TestSuite(NullOutputStreamTest.class));
        suite.addTest(new TestSuite(NullWriterTest.class));
        suite.addTest(new TestSuite(TeeOutputStreamTest.class));
        return suite;
    }
}
