package org.apache.commons.io;

import java.util.HashMap;
import java.util.Map;

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

    public static byte[] ofSize(int bufferSize){
        ThreadLocalByteArray instanceForBufferSize = instancesByBufferSize.get(bufferSize);

        if(instanceForBufferSize == null){
            instanceForBufferSize = new ThreadLocalByteArray(bufferSize);

            instancesByBufferSize.put(bufferSize, instanceForBufferSize);
        }

        return instanceForBufferSize.get();
    }
}
