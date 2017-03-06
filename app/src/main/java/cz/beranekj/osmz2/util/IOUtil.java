package cz.beranekj.osmz2.util;

import java.io.IOException;
import java.io.InputStream;

public class IOUtil
{
    public static String readLine(InputStream input) throws IOException
    {
        StringBuilder line = new StringBuilder();

        while (true)
        {
            int ch = input.read();
            if (ch == -1) return line.toString();

            char c = (char) ch;

            if (c == '\r')
            {
                input.read();
                break;
            }
            else line.append(c);
        }

        return line.toString();
    }

    public static String readLine(ArrayIterator iterator)
    {
        StringBuilder line = new StringBuilder();
        while (true)
        {
            int ch = iterator.read();
            if (ch == -1)
            {
                String data = line.toString();
                if (data.isEmpty()) return null;
                else return data;
            }

            char c = (char) ch;

            if (c == '\r')
            {
                iterator.read();
                break;
            }
            else line.append(c);
        }

        return line.toString();
    }
}
