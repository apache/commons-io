package org.apache.commons.io;

import java.lang.ref.WeakReference;

/**
 * Provider for thread-local char-arrays.
 * <p>
 * This class provides static methods to access thread-local char-arrays of specified length, useful for method-internal buffering.
 * <p>
 * This utility should be used wherever there is need for a char-array to buffer data that is not used outside of a method, e.g. when
 * pushing raw data from one stream to another 
 * <p>
 * @version $Id$
 */
final class CharArrayThreadLocal extends ThreadLocal<WeakReference<char[]>>{

    public static final int BUFFER_ARRAY_SIZE = 2048;
    private static final CharArrayThreadLocal INSTANCE = new CharArrayThreadLocal();

    private CharArrayThreadLocal(){
    }

    @Override
    protected WeakReference<char[]> initialValue() {
        return new WeakReference<char[]>(new char[BUFFER_ARRAY_SIZE]);
    }

    /**
     * Returns a char-array of {@link #BUFFER_ARRAY_SIZE}-length, to use for buffering instead of allocating memory via new char[].
     * Please note that the content of the returned char-array is not guaranteed to be all-zero, it may
     * contain arbitrary data. It is enshured however, that the char-array returned is thread-local and cannot be accessed by
     * another thread as long as it is used method internal.
     * @since 2.5
     */
    public static char[] getThreadLocalCharArray(){
        WeakReference<char[]> weakReference = INSTANCE.get();

        char[] buffer = weakReference.get();

        if(buffer != null){
            return buffer;
        } else {
            INSTANCE.remove();
            return getThreadLocalCharArray();
        }
    }
}
