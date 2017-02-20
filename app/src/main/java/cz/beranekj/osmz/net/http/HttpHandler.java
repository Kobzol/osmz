package cz.beranekj.osmz.net.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import cz.beranekj.osmz.net.server.ServerLog;

public interface HttpHandler
{
    void handleConnection(InputStream input, OutputStream output, ServerLog log) throws IOException;
}
