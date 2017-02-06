package cz.beranekj.osmz.server;

import java.util.HashMap;

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
