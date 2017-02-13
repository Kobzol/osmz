package cz.beranekj.osmz.net.server;

import java.io.IOException;

import cz.beranekj.osmz.net.http.HttpHandler;

public abstract class NetServer
{
    protected boolean isRunning = false;
    protected final HttpHandler handler;

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
}
