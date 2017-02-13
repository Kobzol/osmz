package cz.beranekj.osmz.net.http;

import java.io.InputStream;
import java.util.HashMap;

public class Request
{
    private final HttpMethod method;
    private final HashMap<String, String> headers;
    private final String path;
    private final InputStream inputStream;

    public Request(HttpMethod method, HashMap<String, String> headers, InputStream inputStream, String path)
    {
        this.method = method;
        this.headers = headers;
        this.inputStream = inputStream;
        this.path = path;
    }

    public HttpMethod getMethod()
    {
        return method;
    }

    public HashMap<String, String> getHeaders()
    {
        return headers;
    }

    public String getPath()
    {
        return path;
    }

    public InputStream getInputStream()
    {
        return this.inputStream;
    }
}
