/*
 * Copyright  The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.io.compress.tar;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * A basic test suite that tests all the tar package.
 */
public class TarTestSuite
{
    public static Test suite()
    {
        final TestSuite suite = new TestSuite( "Tar Utilities" );
        return suite;
    }
}
