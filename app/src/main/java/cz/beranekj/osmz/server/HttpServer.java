package cz.beranekj.osmz.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpServer implements HttpHandler
{
    private final List<RequestHandler> handlers = new ArrayList<>();

    public void addHandler(RequestHandler handler)
    {
        this.handlers.add(handler);
    }

    @Override
    public void handleConnection(InputStream input, OutputStream output)
    {
        BufferedReader in = new BufferedReader(new InputStreamReader(input));

        Response response = new Response();
        try
        {
            Request request = this.parseRequest(in);

            boolean handled = false;
            for (RequestHandler handler : this.handlers)
            {
                if (handler.shouldHandle(request))
                {
                    handler.handle(request, response);
                    handled = true;
                }
            }

            if (!handled)
            {
                throw new ServerException(500, "Request not handled");
            }
        }
        catch (IOException e)
        {
            response.setCode(500);
        }
        catch (ServerException e)
        {
            response.setCode(e.getCode());
        }

        try
        {
            this.normalizeResponse(response);
            this.writeResponse(response, output);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        try
        {
            input.close();
            output.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void normalizeResponse(Response response)
    {
        if (!response.getHeaders().containsKey("Content-Type"))
        {
            response.getHeaders().put("Content-Type", "text/html; charset=utf-8");
        }
        else
        {
            String header = response.getHeaders().get("Content-Type");
            if (header.startsWith("text") && !header.contains("charset"))
            {
                response.getHeaders().put("Content-Type", header + "; charset=utf-8");
            }
        }

        if (!response.getHeaders().containsKey("Content-Length"))
        {
            response.getHeaders().put("Content-Length", String.valueOf(response.getBody().getBuffer().length));
        }
        if (!response.getHeaders().containsKey("Connection"))
        {
            response.getHeaders().put("Connection", "Close");
        }
        /*if (!response.getHeaders().containsKey("Content-Encoding"))
        {
            response.getHeaders().put("Content-Encoding", "gzip");
        }*/
    }

    private void writeResponse(Response response, OutputStream output) throws IOException
    {
        PrintStream stream = new PrintStream(output);
        stream.print("HTTP/1.0 ");
        stream.print(response.getCode());
        stream.println(" NA");

        for (String key : response.getHeaders().keySet())
        {
            stream.print(key);
            stream.print(": ");
            stream.println(response.getHeaders().get(key));
        }

        stream.println();
        stream.flush();

        output.write(response.getBody().getBuffer());
        output.flush();
    }

    private Request parseRequest(BufferedReader input) throws IOException, ServerException
    {
        String line = input.readLine();
        if (line == null)
        {
            throw new BadRequestException();
        }

        HttpMethod method;
        String path;
        Pattern firstLinePattern = Pattern.compile("(GET|POST) ([^ ]*) HTTP/1.(:?0|1)");
        Matcher matcher = firstLinePattern.matcher(line);
        if (matcher.matches())
        {
            method = matcher.group(1).equals("GET") ? HttpMethod.GET : HttpMethod.POST;
            path = matcher.group(2);
        }
        else throw new BadRequestException();

        HashMap<String, String> headers = new HashMap<>();

        while (true)
        {
            line = input.readLine();
            if (line == null || line.isEmpty())
            {
                break;
            }

            Pattern headerPattern = Pattern.compile("([A-Z][a-z]*(?:-[A-Z][a-z]*)*): (.*)");
            matcher = headerPattern.matcher(line);
            if (matcher.matches())
            {
                headers.put(matcher.group(1), matcher.group(2));
            }
            else throw new BadRequestException();
        }

        return new Request(method, headers, input, path);
    }
}
