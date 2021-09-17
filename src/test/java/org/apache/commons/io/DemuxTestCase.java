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

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Random;

import org.apache.commons.io.input.DemuxInputStream;
import org.apache.commons.io.input.StringInputStream;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.io.output.DemuxOutputStream;
import org.apache.commons.io.test.TestUtils;
import org.junit.jupiter.api.Test;

/**
 * Basic unit tests for the multiplexing streams.
 */
public class DemuxTestCase {
    private static class ReaderThread
            extends Thread {
        private final StringBuffer stringBuffer = new StringBuffer();
        private final InputStream inputStream;
        private final DemuxInputStream demuxInputStream;

        ReaderThread(final String name,
                     final InputStream input,
                     final DemuxInputStream demux) {
            super(name);
            inputStream = input;
            demuxInputStream = demux;
        }

        public String getData() {
            return stringBuffer.toString();
        }

        @Override
        public void run() {
            demuxInputStream.bindStream(inputStream);

            try {
                int ch = demuxInputStream.read();
                while (-1 != ch) {
                    //System.out.println( "Reading: " + (char)ch );
                    stringBuffer.append((char) ch);

                    final int sleepTime = Math.abs(c_random.nextInt() % 10);
                    TestUtils.sleep(sleepTime);
                    ch = demuxInputStream.read();
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }
    private static class WriterThread
            extends Thread {
        private final byte[] byteArray;
        private final OutputStream outputStream;
        private final DemuxOutputStream demuxOutputStream;

        WriterThread(final String name,
                     final String data,
                     final OutputStream output,
                     final DemuxOutputStream demux) {
            super(name);
            outputStream = output;
            demuxOutputStream = demux;
            byteArray = data.getBytes();
        }

        @Override
        public void run() {
            demuxOutputStream.bindStream(outputStream);
            for (final byte element : byteArray) {
                try {
                    //System.out.println( "Writing: " + (char)byteArray[ i ] );
                    demuxOutputStream.write(element);
                    final int sleepTime = Math.abs(c_random.nextInt() % 10);
                    TestUtils.sleep(sleepTime);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private static final String T1 = "Thread1";
    private static final String T2 = "Thread2";

    private static final String T3 = "Thread3";
    private static final String T4 = "Thread4";
    private static final String DATA1 = "Data for thread1";
    private static final String DATA2 = "Data for thread2";

    private static final String DATA3 = "Data for thread3";
    private static final String DATA4 = "Data for thread4";
    private static final Random c_random = new Random();

    private final HashMap<String, ByteArrayOutputStream> outputMap = new HashMap<>();

    private final HashMap<String, Thread> threadMap = new HashMap<>();

    private void doJoin()
            throws Exception {
        for (final String name : threadMap.keySet()) {
            final Thread thread = threadMap.get(name);
            thread.join();
        }
    }

    private void doStart() {
        for (final String name : threadMap.keySet()) {
            final Thread thread = threadMap.get(name);
            thread.start();
        }
    }

    private String getInput(final String threadName) {
        final ReaderThread thread = (ReaderThread) threadMap.get(threadName);
        assertNotNull(thread, "getInput()");

        return thread.getData();
    }

    private String getOutput(final String threadName) {
        final ByteArrayOutputStream output =
                outputMap.get(threadName);
        assertNotNull(output, "getOutput()");

        return output.toString(StandardCharsets.UTF_8);
    }

    private void startReader(final String name,
                             final String data,
                             final DemuxInputStream demux) {
        final InputStream input = new StringInputStream(data);
        final ReaderThread thread = new ReaderThread(name, input, demux);
        threadMap.put(name, thread);
    }

    private void startWriter(final String name,
                             final String data,
                             final DemuxOutputStream demux) {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        outputMap.put(name, output);
        final WriterThread thread =
                new WriterThread(name, data, output, demux);
        threadMap.put(name, thread);
    }

    @Test
    public void testInputStream()
            throws Exception {
        final DemuxInputStream input = new DemuxInputStream();
        startReader(T1, DATA1, input);
        startReader(T2, DATA2, input);
        startReader(T3, DATA3, input);
        startReader(T4, DATA4, input);

        doStart();
        doJoin();

        assertEquals(DATA1, getInput(T1), "Data1");
        assertEquals(DATA2, getInput(T2), "Data2");
        assertEquals(DATA3, getInput(T3), "Data3");
        assertEquals(DATA4, getInput(T4), "Data4");
    }

    @Test
    public void testOutputStream()
            throws Exception {
        final DemuxOutputStream output = new DemuxOutputStream();
        startWriter(T1, DATA1, output);
        startWriter(T2, DATA2, output);
        startWriter(T3, DATA3, output);
        startWriter(T4, DATA4, output);

        doStart();
        doJoin();

        assertEquals(DATA1, getOutput(T1), "Data1");
        assertEquals(DATA2, getOutput(T2), "Data2");
        assertEquals(DATA3, getOutput(T3), "Data3");
        assertEquals(DATA4, getOutput(T4), "Data4");
    }
}

