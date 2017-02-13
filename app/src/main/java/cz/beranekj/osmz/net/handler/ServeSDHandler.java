package cz.beranekj.osmz.net.handler;

import android.content.Context;
import android.os.Environment;
import android.webkit.MimeTypeMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cz.beranekj.osmz.net.http.HttpMethod;
import cz.beranekj.osmz.net.http.Request;
import cz.beranekj.osmz.net.http.RequestHandler;
import cz.beranekj.osmz.net.http.Response;

public class ServeSDHandler implements RequestHandler
{
    private final Context context;

    public ServeSDHandler(Context context)
    {
        this.context = context;
    }

    @Override
    public boolean shouldHandle(Request request)
    {
        return request.getMethod() == HttpMethod.GET;
    }

    @Override
    public void handle(Request request, Response response)
    {
        File dir = Environment.getExternalStorageDirectory();
        File file = new File(dir, request.getPath());

        if (file.isFile())
        {
            this.serveFile(context, response, file);
        }
        else if (file.isDirectory())
        {
            this.serveDirectory(response, file);
        }
        else response.setCode(404);
    }

    private void serveFile(Context context, Response response, File file)
    {
        try
        {
            InputStream fileStream = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fileStream.read(data);
            fileStream.close();

            response.getBody().write(data);

            String mimeType = getMimeType(file.getAbsolutePath());
            if (mimeType == null || mimeType.isEmpty())
            {
                response.getHeaders().put("Content-Type", "application/octet-stream");
                response.getHeaders().put("Content-Disposition", "attachment; filename=" + file.getName());
            }
            else response.getHeaders().put("Content-Type", mimeType);

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
    private void serveDirectory(Response response, File directory)
    {
        String root = new File(Environment.getExternalStorageDirectory(), "/").getAbsolutePath();
        String html = "<html><head><title>" + this.stripRoot(root, directory.getAbsolutePath()) + "</title>";
        html += "</head><body>";
        html += "<h1>" + this.stripRoot(root, directory.getAbsolutePath()) + "</h1>";
        html += "<ul>";

        File[] files = directory.listFiles();
        List<String> filePaths = new ArrayList<>();
        for (File file : files)
        {
            String path = this.stripRoot(root, file.getAbsolutePath());
            filePaths.add(path);
        }

        Collections.sort(filePaths);

        for (String path : filePaths)
        {
            html += "<li><a href='" + path + "'>" + path + "</a></li>";
        }

        html += "</ul></body></html>";
        response.getBody().write(html);
    }

    private String stripRoot(String root, String path)
    {
        return path.substring(root.length());
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

    private static String getMimeType(String url)
    {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null)
        {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
        }
        return type;
    }
}
