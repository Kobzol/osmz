package cz.beranekj.osmz.net.http;

import java.util.HashMap;

import cz.beranekj.osmz.util.ByteBuffer;

public class Response
{
    private int code = 200;
    private HashMap<String, String> headers = new HashMap<>();
    private ByteBuffer body = new ByteBuffer();

    public int getCode()
    {
        return code;
    }

    public void setCode(int code)
    {
        this.code = code;
    }

    public HashMap<String, String> getHeaders()
    {
        return headers;
    }

    public ByteBuffer getBody()
    {
        return body;
    }
}
