package org.apache.commons.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

// A Proxy stream which acts as expected, that is it passes the method 
// calls on to the proxied stream and doesn't change which methods are 
// being called. It is a Filter stream to increase reusability.
public abstract class ProxyInputStream extends FilterInputStream {

    private InputStream proxy;

    public ProxyInputStream(InputStream proxy) {
        super(proxy);
        this.proxy = proxy;
    }

    public int read() throws IOException {
        return this.proxy.read();
    }

    public int read(byte[] bts) throws IOException {
        return this.proxy.read(bts);
    }

    public int read(byte[] bts, int st, int end) throws IOException {
        return this.proxy.read(bts, st, end);
    }

    public long skip(long ln) throws IOException {
        return this.proxy.skip(ln);
    }

    public int available() throws IOException {
        return this.proxy.available();
    }

    public void close() throws IOException {
        this.proxy.close();
    }

    public synchronized void mark(int idx) {
        this.proxy.mark(idx);
    }

    public synchronized void reset() throws IOException {
        this.proxy.reset();
    }

    public boolean markSupported() {
        return this.proxy.markSupported();
    }

}
