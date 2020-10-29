package org.apache.commons.io.input;

import static org.apache.commons.io.IOUtils.EOF;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;

/**
 * JUnit Test Case for {@link ReadAheadInputStream}.
 */
public class ReadAheadInputStreamTest {


    private static final String EXPECTED = "expected";
    private static final int READ_BUFFER_SIZE = 2;

    private ByteArrayInputStream innerStream;

    @Before
    public void setup() {
        innerStream = new ByteArrayInputStream(EXPECTED.getBytes(Charset.defaultCharset()));
    }

    @Test
    public void testReadSingleChars() {
        ReadAheadInputStream readAheadInputStream = createStreamWithSize(1);
        try {
            for (int counter = 0; counter < EXPECTED.length(); counter++) {
                int r = readAheadInputStream.read();
                assertEquals(EXPECTED.charAt(counter), (char)r);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testReadToArrayWhereInternalBufferIsSmallerThanArray() {

        ReadAheadInputStream readAheadInputStream = createStreamWithSize(READ_BUFFER_SIZE / 2);
        try {
            for (int counter = 0; counter < EXPECTED.length(); counter++) {
                byte[] buff = new byte[READ_BUFFER_SIZE];
                int r = readAheadInputStream.read(buff);
                assertEquals(r, READ_BUFFER_SIZE / 2);
                assertEquals(EXPECTED.charAt(counter), (char)buff[0]);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    public void testReadToArrayWhereInternalBufferIsBiggerThanArray() {

        ReadAheadInputStream readAheadInputStream = createStreamWithSize(READ_BUFFER_SIZE * 5);
        try {
            for (int counter = 0; counter < EXPECTED.length() / 2; counter++) {
                byte[] buff = new byte[READ_BUFFER_SIZE];
                int r = readAheadInputStream.read(buff);
                assertEquals(r, READ_BUFFER_SIZE);
                assertEquals(EXPECTED.charAt(counter * 2),(char)buff[0]);
                assertEquals(EXPECTED.charAt(counter * 2 + 1),(char)buff[1]);

            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testReadUntilEnfOfInternalBuff() {
        boolean foundEOF = false;
        ReadAheadInputStream readAheadInputStream = new ReadAheadInputStream(innerStream, READ_BUFFER_SIZE * 2);
        try {
            for (int counter = 0; counter < EXPECTED.length() + 1 ; counter++) {
                int r = readAheadInputStream.read();
                if (r == EOF) {
                    foundEOF = true;
                }
            }
            assertTrue(foundEOF);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testReadFromStreamToANonZeroIndexInTheTargetArray() throws Exception {
        ReadAheadInputStream readAheadInputStream = new ReadAheadInputStream(innerStream, EXPECTED.length());
        byte[] buff = new byte[100];
        readAheadInputStream.read(buff, 50, 50 + EXPECTED.length());
        byte[] buffForString = new byte[EXPECTED.length()];
        System.arraycopy(buff, 50,  buffForString, 0, EXPECTED.length());
        System.out.println(new String(buffForString));
        assertEquals(EXPECTED, new String(buffForString, Charset.defaultCharset()));
    }

    @Test
    public void testInnerStreamClosingBasedOnCloseInternalField() {
        checkInternalCloseWhenFlagIs(true);
        checkInternalCloseWhenFlagIs(false);
    }

    @Test
    public void testUsingCustomExecutor() throws IOException {


        ReadAheadInputStream readAheadInputStream = new ReadAheadInputStream(innerStream, Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                usingCustomExecutor = true;
                return new Thread(r);
            }
        }));

        readAheadInputStream.read();
        assertEquals(true, usingCustomExecutor);
    }

    @Test
    public void testStreamCreationWithOnlyInnerStreamAsArgument() {
        ReadAheadInputStream readAheadInputStream = new ReadAheadInputStream(innerStream);
        assertEquals(true, readAheadInputStream.isCloseInternal());
    }

    @Test
    public void testStreamCreationWithInnerStreamBufferSizeAndExecutorService() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        ReadAheadInputStream readAheadInputStream = new ReadAheadInputStream(innerStream, READ_BUFFER_SIZE, executorService);
        assertEquals(READ_BUFFER_SIZE, readAheadInputStream.getReadBufferSize());
        assertEquals( executorService, readAheadInputStream.getExecutorService());
    }

    private void checkInternalCloseWhenFlagIs(boolean internalClose) {
        InputStream innerStream = generateInnerStreamToCheckInternalClosesFlag(internalClose);
        ReadAheadInputStream readAheadInputStream = new ReadAheadInputStream(innerStream, internalClose);
        readAheadInputStream.close();
    }

    private ByteArrayInputStream generateInnerStreamToCheckInternalClosesFlag(final boolean internalClose) {
        return new ByteArrayInputStream(EXPECTED.getBytes(Charset.defaultCharset())) {

            @Override
            public void close() throws IOException {
                assertTrue(internalClose);
            }
        };
    }

    private ReadAheadInputStream createStreamWithSize(int size) {
        return new ReadAheadInputStream(innerStream, size);
    }

    private boolean usingCustomExecutor = false;

}