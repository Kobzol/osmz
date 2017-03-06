package cz.beranekj.osmz2.util;

public class ArrayIterator
{
    private final byte[] array;
    private int offset;

    public ArrayIterator(byte[] array, int offset)
    {
        this.array = array;
        this.offset = offset;
    }

    public byte[] getArray()
    {
        return this.array;
    }

    public int getOffset()
    {
        return this.offset;
    }

    public void setOffset(int offset)
    {
        this.offset = offset;
    }

    public int read()
    {
        if (this.offset >= this.array.length) return -1;

        byte b = this.array[this.offset];
        this.offset++;
        return b;
    }
}
