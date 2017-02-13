package cz.beranekj.osmz.util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ByteBuffer
{
    private byte[] buffer = new byte[1024];
    private int offset = 0;

    public void write(byte[] buffer, int offset, int length)
    {
        this.reallocate(length);
        System.arraycopy(buffer, offset, this.buffer, this.offset, length);
        this.offset += length;
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
        return this.buffer;
    }

    private void reallocate(int length)
    {
        if (length + this.offset > this.buffer.length)
        {
            byte[] newBuffer = new byte[Math.max(this.buffer.length * 2, length + this.offset)];
            System.arraycopy(this.buffer, 0, newBuffer, 0, this.offset);
            this.buffer = newBuffer;
        }
    }
}
