package org.apache.commons.io.watcher;

import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;

public class WatcherProperties {
    private WatchEvent.Kind<?>[] listenEvents;
    private boolean endless;

    public WatcherProperties() {
        listenEvents = new WatchEvent.Kind[]{StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE};
        endless = true;
    }

    public WatchEvent.Kind<?>[] getListenEvents() {
        return listenEvents;
    }

    public void setListenEvents(WatchEvent.Kind<?>[] listenEvents) {
        this.listenEvents = listenEvents;
    }

    public boolean isEndless() {
        return endless;
    }

    public void setEndless(boolean endless) {
        this.endless = endless;
    }
}
