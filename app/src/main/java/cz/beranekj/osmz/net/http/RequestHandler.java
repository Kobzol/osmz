package cz.beranekj.osmz.net.http;

import java.io.IOException;

public interface RequestHandler
{
    boolean shouldHandle(Request request);
    void handle(Request request, Response response) throws IOException, ServerException;
}