package cz.beranekj.osmz2.net.server;

import java.io.IOException;

import cz.beranekj.osmz2.net.http.HttpHandler;

public abstract class NetServer
{
    protected boolean isRunning = false;
    protected final HttpHandler handler;
    protected final ServerLog log = new ServerLog();

    public NetServer(HttpHandler handler)
    {
        this.handler = handler;
    }

    public abstract void start();
    public abstract void stop() throws IOException;

    public boolean isRunning()
    {
        return this.isRunning;
    }

    public ServerLog getLog()
    {
        return this.log;
    }
}
