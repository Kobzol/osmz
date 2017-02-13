package cz.beranekj.osmz.handler;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.beranekj.osmz.server.HttpMethod;
import cz.beranekj.osmz.server.Request;
import cz.beranekj.osmz.server.RequestHandler;
import cz.beranekj.osmz.server.Response;

public class UploadFileHandler implements RequestHandler
{
    private final Context context;

    public UploadFileHandler(Context context)
    {
        this.context = context;
    }

    @Override
    public boolean shouldHandle(Request request)
    {
        return request.getMethod() == HttpMethod.POST;
    }

    @Override
    public void handle(Request request, Response response) throws IOException
    {
        StringBuilder body = new StringBuilder();
        BufferedReader input = request.getInputStream();

        String filePath = "";
        Pattern pattern = Pattern.compile("Content-Disposition: .* filename=\"(.*)\"");

        int contentType = 0;
        while (true)
        {
            String line = input.readLine();
            if (line == null || line.isEmpty())
            {
                break;
            }

            if (line.startsWith("Content-Type"))
            {
                contentType++;
            }
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches())
            {
                filePath = matcher.group(1);
            }

            if (contentType == 1)
            {
                input.readLine();
                break;
            }
        }

        if (contentType == 1)
        {
            String line = input.readLine();
            body.append(line);
        }

        File file = new File(Environment.getExternalStorageDirectory(), filePath);
        FileOutputStream stream = new FileOutputStream(file);

        try
        {
            stream.write(body.toString().getBytes());
        }
        finally
        {
            stream.close();
        }

        response.setCode(200);
    }
}
