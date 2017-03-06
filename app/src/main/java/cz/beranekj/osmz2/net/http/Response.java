package cz.beranekj.osmz2.net.http;

import java.io.OutputStream;
import java.util.HashMap;

import cz.beranekj.osmz2.util.ByteBuffer;

public class Response
{
    private int code = 200;
    private HashMap<String, String> headers = new HashMap<>();
    private ByteBuffer body = new ByteBuffer();
    private final OutputStream outputStream;

    public Response(OutputStream outputStream)
    {
        this.outputStream = outputStream;
    }

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

    public OutputStream getOutputStream()
    {
        return this.outputStream;
    }
}
