package org.apache.commons.io;

import java.util.HashMap;
import java.util.Map;

/**
 * Provider for thread-local char-arrays.
 * <p>
 * This class provides static methods to access thread-local char-arrays of specified length, usefull for method-internal buffering.
 * <p>
 * This utility should be used wherever there is need for a char-array to buffer data that is not used outside of a method, e.g. when
 * pushing raw data from one stream to another 
 * <p>
 * @version $Id$
 */
public final class ThreadLocalCharArray extends ThreadLocal<char[]>{
    private static final Map<Integer, ThreadLocalCharArray> instancesByBufferSize = new HashMap<Integer, ThreadLocalCharArray>();

    private final int bufferSize;

    private ThreadLocalCharArray(int bufferSize){
        this.bufferSize = bufferSize;
    }

    @Override
    protected char[] initialValue() {
        return new char[bufferSize];
    }

    /**
     * Returns a char[] of specified length, to use for buffering instead of allocating memory via new char[].
     * Please not that the content of the returned char-array is not guaranteed to be all-zero, it may
     * contain arbitrary data. It is enshured however, that the char-array returned is threadlocal and cannot be accessed by 
     * another thread as long as it is used method internal. 
     * @param bufferSize the size of the array to be returned, must be larger than zero
     * @since 2.5
     */
    public static char[] ofSize(int bufferSize){
        if(bufferSize <= 0){
            throw new IllegalArgumentException("bufferSize must be larger than 0");
        }
        
        ThreadLocalCharArray instanceForBufferSize = instancesByBufferSize.get(bufferSize);

        if(instanceForBufferSize == null){
            instanceForBufferSize = new ThreadLocalCharArray(bufferSize);

            instancesByBufferSize.put(bufferSize, instanceForBufferSize);
        }

        return instanceForBufferSize.get();
    }
}
