package org.apache.commons.io;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.Set;

/**
 * Extends {@link Selector} with no-ops for testing.
 *  
 * @version $Id$
 */
public class SelectorAdapter extends Selector {

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public SelectorProvider provider() {
        return null;
    }

    @Override
    public Set<SelectionKey> keys() {
        return null;
    }

    @Override
    public Set<SelectionKey> selectedKeys() {
        return null;
    }

    @Override
    public int selectNow() throws IOException {
        return 0;
    }

    @Override
    public int select(long timeout) throws IOException {
        return 0;
    }

    @Override
    public int select() throws IOException {
        return 0;
    }

    @Override
    public Selector wakeup() {
        return null;
    }

    @Override
    public void close() throws IOException {
    }

}
