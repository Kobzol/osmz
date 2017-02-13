package cz.beranekj.osmz.net.handler;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.beranekj.osmz.net.http.HttpMethod;
import cz.beranekj.osmz.net.http.Request;
import cz.beranekj.osmz.net.http.RequestHandler;
import cz.beranekj.osmz.net.http.Response;
import cz.beranekj.osmz.net.http.ServerException;
import cz.beranekj.osmz.util.ArrayIterator;
import cz.beranekj.osmz.util.IOUtil;

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
    public void handle(Request request, Response response) throws IOException, ServerException
    {
        String filePath = "";
        Pattern contentPattern = Pattern.compile("Content-Disposition: .* filename=\"(.*)\"");

        byte[] body = this.readRequestBody(request);
        ArrayIterator iterator = new ArrayIterator(body, 0);

        boolean fileFound = false;
        while (true)
        {
            String line = IOUtil.readLine(iterator);
            if (line == null) break;

            Matcher matcher = contentPattern.matcher(line);
            if (matcher.matches())
            {
                filePath = matcher.group(1);
            }

            if (line.startsWith("Content-Type"))
            {
                fileFound = true;
                IOUtil.readLine(iterator);
                break;
            }
        }

        // read content
        if (!fileFound)
        {
            throw new ServerException(400, "Bad request body");
        }

        byte[] content = this.readUploadContent(request, iterator);
        this.writeFile(content, filePath);
        response.setCode(200);
    }

    private byte[] readUploadContent(Request request, ArrayIterator iterator)
    {
        String boundary = this.parseBoundary(request);
        byte[] content = new byte[iterator.getArray().length - (iterator.getOffset() + boundary.length() + 4 + 2)];
        for (int i = 0; i < content.length; i++)
        {
            content[i] = (byte) iterator.read();
        }

        return content;
    }

    private byte[] readRequestBody(Request request) throws ServerException, IOException
    {
        int contentLength = Integer.parseInt(request.getHeaders().get("Content-Length"));
        byte[] data = new byte[contentLength];
        int readCount = request.getInputStream().read(data);

        if (readCount != contentLength)
        {
            throw new ServerException(500, "Bad content length, expected " + String.valueOf(contentLength) + ", got " + String.valueOf(readCount));
        }

        return data;
    }

    private void writeFile(byte[] content, String filePath) throws IOException
    {
        File file = new File(Environment.getExternalStorageDirectory(), filePath);

        try (FileOutputStream stream = new FileOutputStream(file))
        {
            stream.write(content);
        }
    }

    private String parseBoundary(Request request)
    {
        Pattern boundaryPattern = Pattern.compile("multipart/form-data; boundary=(.*)");
        String typeHeader = request.getHeaders().get("Content-Type");
        Matcher boundaryMatcher = boundaryPattern.matcher(typeHeader);

        if (boundaryMatcher.matches())
        {
            return boundaryMatcher.group(1);
        }
        else return "";
    }
}
