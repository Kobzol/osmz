package cz.beranekj.osmz.handler;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import cz.beranekj.osmz.server.Request;
import cz.beranekj.osmz.server.RequestHandler;
import cz.beranekj.osmz.server.Response;

public class ServeSDHandler implements RequestHandler
{
    private final Context context;

    public ServeSDHandler(Context context)
    {
        this.context = context;
    }

    @Override
    public void handle(Request request, Response response)
    {
        File dir = Environment.getExternalStorageDirectory();
        File file = new File(dir, request.getPath());

        if (request.getPath().equals("/"))
        {
            this.serveIndex(response);
            return;
        }

        try
        {
            InputStream fileStream = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fileStream.read(data);
            fileStream.close();

            response.getBody().write(data);

            response.getHeaders().put("Content-Type", "application/octet-stream");
            response.getHeaders().put("Content-Disposition", "attachment; filename=" + file.getName());
            response.getHeaders().put("Content-Length", String.valueOf(file.length()));
        }
        catch (FileNotFoundException e)
        {
            response.setCode(404);
        }
        catch (IOException e)
        {
            response.setCode(500);
        }
    }

    private void serveIndex(Response response)
    {
        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader(
                    new InputStreamReader(this.context.getAssets().open("index.html")));

            while (true)
            {
                String line = reader.readLine();
                if (line == null) break;

                response.getBody().write("line");
                response.getBody().write("\n");
            }
        }
        catch (IOException ignored)
        {

        }
        finally
        {
            if (reader != null)
            {
                try
                {
                    reader.close();
                }
                catch (IOException ignored)
                {

                }
            }
        }
    }
}
