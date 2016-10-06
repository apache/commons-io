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
package org.apache.commons.io.comparator;

import org.junit.Before;

import java.io.File;

/**
 * Test case for {@link DefaultFileComparator}.
 */
public class DefaultFileComparatorTest extends ComparatorAbstractTestCase {

    @Before
    public void setUp() throws Exception {
        comparator = (AbstractFileComparator) DefaultFileComparator.DEFAULT_COMPARATOR;
        reverse = DefaultFileComparator.DEFAULT_REVERSE;
        equalFile1 = new File("foo");
        equalFile2 = new File("foo");
        lessFile   = new File("abc");
        moreFile   = new File("xyz");
    }
}
