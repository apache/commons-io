package org.apache.commons.io;

import java.lang.ref.WeakReference;

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
final class ByteArrayThreadLocal extends ThreadLocal<WeakReference<byte[]>>{

    public static final int BUFFER_ARRAY_SIZE = 2048;
    private static final ByteArrayThreadLocal INSTANCE = new ByteArrayThreadLocal();

    private ByteArrayThreadLocal(){
    }

    @Override
    protected WeakReference<byte[]> initialValue() {
        return new WeakReference<byte[]>(new byte[BUFFER_ARRAY_SIZE]);
    }

    /**
     * Returns a byte-array of {@link #BUFFER_ARRAY_SIZE}-length, to use for buffering instead of allocating memory via new byte[].
     * Please note that the content of the returned byte-array is not guaranteed to be all-zero, it may
     * contain arbitrary data. It is enshured however, that the char-array returned is thread-local and cannot be accessed by
     * another thread as long as it is used method internal.
     * @since 2.5
     */
    public static byte[] getThreadLocalByteArray(){
        WeakReference<byte[]> weakReference = INSTANCE.get();

        byte[] buffer = weakReference.get();

        if(buffer != null){
            return buffer;
        } else {
            INSTANCE.remove();
            return getThreadLocalByteArray();
        }
    }
}
