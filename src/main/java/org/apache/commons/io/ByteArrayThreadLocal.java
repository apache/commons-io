package org.apache.commons.io;

/**
 * Provider for thread-local byte-arrays.
 * <p>
 * This class provides static methods to access thread-local byte-arrays of specified length, useful for method-internal buffering.
 * <p>
 * This utility should be used wherever there is need for a byte-array to buffer data that is not used outside of a method, e.g. when
 * pushing raw data from one stream to another
 * <p>
 * @version $Id$
 */
final class ByteArrayThreadLocal extends ThreadLocal<byte[]>{

    public static final int BUFFER_ARRAY_SIZE = 4096;
    private static final ByteArrayThreadLocal INSTANCE = new ByteArrayThreadLocal();

    private ByteArrayThreadLocal(){
    }

    @Override
    protected byte[] initialValue() {
        return new byte[BUFFER_ARRAY_SIZE];
    }

    /**
     * Returns a byte-array of {@link #BUFFER_ARRAY_SIZE}-length, to use for buffering instead of allocating memory via new byte[].
     * Please not that the content of the returned byte-array is not guaranteed to be all-zero, it may
     * contain arbitrary data. It is enshured however, that the char-array returned is thread-local and cannot be accessed by
     * another thread as long as it is used method internal.
     * @since 2.5
     */
    public static byte[] getThreadLocalByteArray(){
        return INSTANCE.get();
    }
}
