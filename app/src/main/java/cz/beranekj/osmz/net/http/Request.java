package cz.beranekj.osmz.net.http;

import android.net.Uri;

import java.io.InputStream;
import java.util.HashMap;

public class Request
{
    private final HttpMethod method;
    private final HashMap<String, String> headers;
    private final String path;
    private final InputStream inputStream;
    private final Uri uri;

    public Request(HttpMethod method, HashMap<String, String> headers, InputStream inputStream, String path)
    {
        this.method = method;
        this.headers = headers;
        this.inputStream = inputStream;
        this.path = path;
        this.uri = Uri.parse(path);
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

    public Uri getUri()
    {
        return this.uri;
    }
}
