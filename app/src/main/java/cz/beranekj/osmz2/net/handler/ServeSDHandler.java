package cz.beranekj.osmz2.net.handler;

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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import cz.beranekj.osmz2.net.http.HttpMethod;
import cz.beranekj.osmz2.net.http.Request;
import cz.beranekj.osmz2.net.http.RequestHandler;
import cz.beranekj.osmz2.net.http.Response;
import cz.beranekj.osmz2.net.server.ServerLog;

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
    public void handle(Request request, Response response, ServerLog log)
    {
        File dir = Environment.getExternalStorageDirectory();
        File file = new File(dir, request.getPath());

        if (file.isFile())
        {
            this.serveFile(context, response, file, log);
        }
        else if (file.isDirectory())
        {
            log.log("Serving directory " + file.getAbsolutePath());
            this.serveDirectory(response, file);
        }
        else
        {
            log.log("Path not found: " + request.getPath());
            response.setCode(404);
        }
    }

    private void serveFile(Context context, Response response, File file, ServerLog log)
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

            log.log("Serving file " + file.getAbsolutePath());

            response.getHeaders().put("Content-Length", String.valueOf(file.length()));
        }
        catch (FileNotFoundException e)
        {
            log.log("File not found " + file.getAbsolutePath());
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

        List<File> files = Arrays.asList(directory.listFiles());
        Collections.sort(files, (l, r) -> {
            if (l.isDirectory() && !r.isDirectory()) return -1;
            if (!l.isDirectory() && r.isDirectory()) return 1;

            return l.getAbsolutePath().compareToIgnoreCase(r.getAbsolutePath());
        });

        for (File file : files)
        {
            String path = this.stripRoot(root, file.getAbsolutePath());
            String name = path;

            if (file.isFile())
            {
                name += " (" + file.length() + " bytes)";
            }

            html += "<li><a href='" + path + "'>" + name + "</a></li>";
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
