package cz.beranekj.osmz.util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class ByteBuffer
{
    private final List<Byte> buffer = new ArrayList<>();

    public void write(byte[] buffer, int offset, int length)
    {
        for (int i = offset; i < offset + length; i++)
        {
            this.buffer.add(buffer[i]);
        }
    }
    public void write(byte[] buffer)
    {
        this.write(buffer, 0, buffer.length);
    }
    public void write(String data, String encoding)
    {
        try
        {
            this.write(data.getBytes(encoding));
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
    }
    public void writeln(String data, String encoding)
    {
        this.write(data, encoding);
        this.write("\n", encoding);
    }
    public void write(String data)
    {
        this.write(data, "UTF-8");
    }
    public void writeln(String data)
    {
        this.writeln(data, "UTF-8");
    }

    public byte[] getBuffer()
    {
        byte[] buffer = new byte[this.buffer.size()];
        int i = 0;
        for (Byte b : this.buffer)
        {
            buffer[i++] = b;
        }

        return buffer;
    }
}
