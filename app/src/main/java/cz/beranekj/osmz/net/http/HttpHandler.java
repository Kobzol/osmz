package cz.beranekj.osmz.net.http;

import java.io.InputStream;
import java.io.OutputStream;

public interface HttpHandler
{
    void handleConnection(InputStream input, OutputStream output);
}
