package cz.beranekj.osmz.server;

public interface RequestHandler
{
    void handle(Request request, Response response);
}
