package cz.beranekj.osmz2.net.handler.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

class ThreadReader extends Thread
{
    private final InputStream stream;
    private final StringBuilder builder = new StringBuilder();

    ThreadReader(InputStream stream)
    {
        this.stream = stream;
    }

    @Override
    public void run()
    {
        InputStreamReader reader = new InputStreamReader(this.stream);
        BufferedReader bufferedReader = new BufferedReader(reader);

        try
        {
            while (true)
            {
                String line = bufferedReader.readLine();
                if (line == null) return;
                this.builder.append(line);
                this.builder.append("\n");
            }
        }
        catch (IOException e)
        {

        }
        finally
        {
            try
            {
                this.stream.close();
            }
            catch (IOException e1)
            {
                e1.printStackTrace();
            }
        }
    }

    String getOutput()
    {
        return this.builder.toString();
    }
}
