package org.apache.commons.io;

import java.io.IOException;
import java.io.FilterOutputStream;
import java.io.OutputStream;

// A Proxy stream which acts as expected, that is it passes the method 
// calls on to the proxied stream and doesn't change which methods are 
// being called. It is a Filter stream to increase reusability.
public class ProxyOutputStream extends FilterOutputStream {

    private OutputStream proxy;

    public ProxyOutputStream(OutputStream proxy) {
        super(proxy);
        this.proxy = proxy;
    }

    public void write(int idx) throws IOException {
        this.proxy.write(idx);
    }

    public void write(byte[] bts) throws IOException {
        this.proxy.write(bts);
    }

    public void write(byte[] bts, int st, int end) throws IOException {
        this.proxy.write(bts, st, end);
    }

    public void flush() throws IOException {
        this.proxy.flush();
    }

    public void close() throws IOException {
        this.proxy.close();
    }

}
