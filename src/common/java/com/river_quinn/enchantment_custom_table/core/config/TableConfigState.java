package com.river_quinn.enchantment_custom_table.core.config;

import java.util.Map;

/** Local/server data and a connection-scoped client view must never share mutable configuration. */
public final class TableConfigState {
    public static final TableConfigSnapshot UNAVAILABLE = new TableConfigSnapshot(Map.of(), false, false, false, false);
    private volatile TableConfigSnapshot local = UNAVAILABLE;
    private volatile TableConfigSnapshot remote;
    private volatile boolean connected;
    private long revision = -1;

    public TableConfigSnapshot local() { return local; }
    public TableConfigSnapshot forSide(boolean clientSide) {
        if (!clientSide || !connected) return local;
        TableConfigSnapshot value = remote;
        return value == null ? UNAVAILABLE : value;
    }
    public void setLocal(TableConfigSnapshot value) { local = value; }
    public synchronized void beginConnection() { connected = true; remote = null; revision = -1; }
    public synchronized void disconnect() { remote = null; revision = -1; connected = false; }
    public synchronized void receive(long newRevision, TableConfigSnapshot value) {
        if (connected && newRevision >= revision) { remote = value; revision = newRevision; }
    }
    public boolean connected() { return connected; }
}
