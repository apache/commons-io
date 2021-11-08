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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.EOFException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link IOExceptionList}.
 */
public class IOExceptionListTest {

    @Test
    public void testCause() {
        final EOFException cause = new EOFException();
        final List<EOFException> list = Collections.singletonList(cause);
        final IOExceptionList sqlExceptionList = new IOExceptionList(list);
        assertEquals(cause, sqlExceptionList.getCause());
        assertEquals(cause, sqlExceptionList.getCause(0));
        assertEquals(list, sqlExceptionList.getCauseList());
        assertEquals(list, sqlExceptionList.getCauseList(EOFException.class));
        assertEquals(cause, sqlExceptionList.getCause(0, EOFException.class));
        // No CCE:
        final List<EOFException> causeList = sqlExceptionList.getCauseList();
        assertEquals(list, causeList);
    }

    @Test
    public void testCheckEmpty() throws IOExceptionList {
        IOExceptionList.checkEmpty(null, "");
        IOExceptionList.checkEmpty(null, null);
        IOExceptionList.checkEmpty(Collections.emptyList(), "");
        IOExceptionList.checkEmpty(Collections.emptyList(), null);
        assertThrows(IOExceptionList.class, () -> IOExceptionList.checkEmpty(Collections.singletonList(new Exception()), ""));
        assertThrows(IOExceptionList.class, () -> IOExceptionList.checkEmpty(Collections.singletonList(new Exception()), null));
    }

    @Test
    public void testEmptyList() {
        new IOExceptionList(Collections.emptyList());
        new IOExceptionList("foo", Collections.emptyList());
    }

    @Test
    public void testMessageCause() {
        final EOFException cause = new EOFException();
        final List<EOFException> list = Collections.singletonList(cause);
        final IOExceptionList sqlExceptionList = new IOExceptionList("Hello", list);
        assertEquals("Hello", sqlExceptionList.getMessage());
        //
        assertEquals(cause, sqlExceptionList.getCause());
        assertEquals(cause, sqlExceptionList.getCause(0));
        assertEquals(list, sqlExceptionList.getCauseList());
        assertEquals(list, sqlExceptionList.getCauseList(EOFException.class));
        assertEquals(cause, sqlExceptionList.getCause(0, EOFException.class));
        // No CCE:
        final List<EOFException> causeList = sqlExceptionList.getCauseList();
        assertEquals(list, causeList);
    }

    @Test
    public void testNullCause() {
        final IOExceptionList sqlExceptionList = new IOExceptionList(null);
        assertNull(sqlExceptionList.getCause());
        assertTrue(sqlExceptionList.getCauseList().isEmpty());
    }

    @Test
    public void testNullMessageArg() {
        assertNotNull(new IOExceptionList(null, Collections.emptyList()).getMessage());
        assertNotNull(new IOExceptionList(null, null).getMessage());
        assertEquals("A", new IOExceptionList("A", Collections.emptyList()).getMessage());
        assertEquals("A", new IOExceptionList("A", null).getMessage());
    }

    @Test
    public void testPrintStackTrace() {
        final EOFException cause = new EOFException();
        final List<EOFException> list = Collections.singletonList(cause);
        final IOExceptionList sqlExceptionList = new IOExceptionList(list);
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        sqlExceptionList.printStackTrace(pw);
        final String st = sw.toString();
        assertTrue(st.startsWith("org.apache.commons.io.IOExceptionList: 1 exception(s): [java.io.EOFException]"));
        assertTrue(st.contains("Caused by: java.io.EOFException"));
    }
}
