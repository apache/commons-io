/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import org.apache.commons.io.filefilter.FileFilterTestCase;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * A basic test suite that tests all the IO package.
 * 
 * @author Matthew Hawthorne
 * @see org.apache.commons.io
 */
public class IOTestSuite
{
    
    public static void main(String[] args) {
        TestRunner.run(suite());
    }
    
    public static Test suite()
    {
        TestSuite suite = new TestSuite( "IO Utilities" );
        suite.addTest( new TestSuite( CopyUtilsTest.class ) );
        suite.addTest( new TestSuite( IOUtilsTestCase.class ) );
        suite.addTest( new TestSuite( FileUtilsTestCase.class ) );
        suite.addTest( new TestSuite( FileFilterTestCase.class ) );
        suite.addTest( new TestSuite( DemuxTestCase.class ) );
        suite.addTest( new TestSuite( HexDumpTest.class ) );
        return suite;
    }
}
