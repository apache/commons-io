package org.apache.commons.io;

import java.util.HashMap;
import java.util.Map;

/**
 * Provider for thread-local bytearrays.
 * <p>
 * This class provides static methods to access thread-local bytearrays of specified length, usefull for method-internal buffering.
 * <p>
 * This utility should be used wherever there is need for a bytearray to buffer data that is not used outside of a method, e.g. when
 * pushing raw data from one stream to another
 * <p>
 * @version $Id$
 */
public final class ThreadLocalByteArray extends ThreadLocal<byte[]>{
    private static final Map<Integer, ThreadLocalByteArray> instancesByBufferSize = new HashMap<Integer, ThreadLocalByteArray>();

    private final int bufferSize;

    private ThreadLocalByteArray(int bufferSize){
        this.bufferSize = bufferSize;
    }

    @Override
    protected byte[] initialValue() {
        return new byte[bufferSize];
    }

    /**
     * Returns a byte[] of specified length, to use for buffering instead of allocating memory via new byte[].
     * Please not that the content of the returned byte-array is not guaranteed to be all-zero, it may
     * contain arbitrary data. It is enshured however, that the bytearray returned is threadlocal and cannot be accessed by
     * another thread as long as the byte-array is used method internal.
     * @param bufferSize the size of the array to be returned, must be larger than zero
     * @since 2.5
     */
    public static byte[] ofSize(int bufferSize){
        if(bufferSize <= 0){
            throw new IllegalArgumentException("bufferSize must be larger than 0");
        }

        ThreadLocalByteArray instanceForBufferSize = instancesByBufferSize.get(bufferSize);

        if(instanceForBufferSize == null){
            instanceForBufferSize = new ThreadLocalByteArray(bufferSize);

            instancesByBufferSize.put(bufferSize, instanceForBufferSize);
        }

        return instanceForBufferSize.get();
    }
}
