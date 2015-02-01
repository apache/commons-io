package org.apache.commons.io;

import java.util.HashMap;
import java.util.Map;

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

    public static ThreadLocalCharArray ofSize(int bufferSize){
        ThreadLocalCharArray instanceForBufferSize = instancesByBufferSize.get(bufferSize);

        if(instanceForBufferSize == null){
            instanceForBufferSize = new ThreadLocalCharArray(bufferSize);

            instancesByBufferSize.put(bufferSize, instanceForBufferSize);
        }

        return instanceForBufferSize;
    }
}
