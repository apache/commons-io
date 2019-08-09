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

import java.io.EOFException;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class IOExceptionListTestCase {

    @Test
    public void testCause() {
        final EOFException cause = new EOFException();
        final List<EOFException> list = Collections.singletonList(cause);
        final IOExceptionList sqlExceptionList = new IOExceptionList(list);
        Assert.assertEquals(cause, sqlExceptionList.getCause());
        Assert.assertEquals(cause, sqlExceptionList.getCause(0));
        Assert.assertEquals(list, sqlExceptionList.getCauseList());
        Assert.assertEquals(list, sqlExceptionList.getCauseList(EOFException.class));
        Assert.assertEquals(cause, sqlExceptionList.getCause(0, EOFException.class));
        // No CCE:
        final List<EOFException> causeList = sqlExceptionList.getCauseList();
        Assert.assertEquals(list, causeList);
    }

    @Test
    public void testNullCause() {
        final IOExceptionList sqlExceptionList = new IOExceptionList(null);
        Assert.assertNull(sqlExceptionList.getCause());
        Assert.assertTrue(sqlExceptionList.getCauseList().isEmpty());
    }

    @Test
    public void testPrintStackTrace() {
        final EOFException cause = new EOFException();
        final List<EOFException> list = Collections.singletonList(cause);
        final IOExceptionList sqlExceptionList = new IOExceptionList(list);
        sqlExceptionList.printStackTrace();
    }
}
