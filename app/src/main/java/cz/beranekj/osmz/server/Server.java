package cz.beranekj.osmz.server;

public abstract class Server
{
    protected boolean isRunning = false;
    protected final HttpHandler handler;

    public Server(HttpHandler handler)
    {
        this.handler = handler;
    }

    public abstract void start();
    public abstract void stop();

    public boolean isRunning()
    {
        return this.isRunning;
    }
}
