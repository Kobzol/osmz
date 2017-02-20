package cz.beranekj.osmz.net.http;

import java.io.IOException;

import cz.beranekj.osmz.net.server.ServerLog;

public interface RequestHandler
{
    boolean shouldHandle(Request request);
    void handle(Request request, Response response, ServerLog log) throws IOException, ServerException;
}
