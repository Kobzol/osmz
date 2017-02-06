package cz.beranekj.osmz.server;

import java.util.HashMap;

public class Request
{
    private final HttpMethod method;
    private final HashMap<String, String> headers;
    private final String body;
    private final String path;

    public Request(HttpMethod method, HashMap<String, String> headers, String body, String path)
    {
        this.method = method;
        this.headers = headers;
        this.body = body;
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

    public String getBody()
    {
        return body;
    }

    public String getPath()
    {
        return path;
    }
}
