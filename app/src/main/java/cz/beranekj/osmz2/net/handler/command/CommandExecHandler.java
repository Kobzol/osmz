package cz.beranekj.osmz2.net.handler.command;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import cz.beranekj.osmz2.app.Application;
import cz.beranekj.osmz2.net.http.HttpMethod;
import cz.beranekj.osmz2.net.http.Request;
import cz.beranekj.osmz2.net.http.RequestHandler;
import cz.beranekj.osmz2.net.http.Response;
import cz.beranekj.osmz2.net.http.ServerException;
import cz.beranekj.osmz2.net.server.ServerLog;

public class CommandExecHandler implements RequestHandler
{
    private final static int TIMEOUT = 5000;

    private final Application application;

    public CommandExecHandler(Application application)
    {
        this.application = application;
    }

    @Override
    public boolean shouldHandle(Request request)
    {
        return request.getMethod() == HttpMethod.GET && request.getPath().startsWith("/cgi-bin");
    }

    @Override
    public void handle(Request request, Response response, ServerLog log) throws IOException, ServerException
    {
        String command = request.getUri().getQueryParameter("command");
        String stdin = request.getUri().getQueryParameter("stdin");

        if (command == null || command.isEmpty())
        {
            throw new ServerException(400);
        }

        RuntimeResult result = new RuntimeResult("", "", -1, -1);

        try
        {
            Process process = Runtime.getRuntime().exec(command);
            result = this.communicate(process, stdin);
        }
        catch (IOException e)
        {
            result.status = -2;
        }

        this.writeResult(response, result);
    }

    private RuntimeResult communicate(Process process, String stdin)
    {
        String stdout = "";
        String stderr = "";
        int state = -1;
        int returnValue = -1;

        try
        {
            if (stdin != null && !stdin.isEmpty())
            {
                PrintWriter writer = new PrintWriter(process.getOutputStream());
                writer.write(stdin);
                writer.close();
            }

            ThreadReader stdoutReader = new ThreadReader(process.getInputStream());
            stdoutReader.start();
            ThreadReader stderrReader = new ThreadReader(process.getErrorStream());
            stderrReader.start();

            Date start = new Date();
            while (true)
            {
                try
                {
                    returnValue = process.exitValue();
                    stdoutReader.join();
                    stderrReader.join();

                    state = 0;
                    stdout = stdoutReader.getOutput();
                    stderr = stderrReader.getOutput();
                    break;
                }
                catch (IllegalThreadStateException e)
                {
                    if (new Date().getTime() - start.getTime() >= CommandExecHandler.TIMEOUT) break;
                    Thread.sleep(1000);
                }
            }
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        return new RuntimeResult(stdout, stderr, returnValue, state);
    }

    private void writeResult(Response response, RuntimeResult result) throws ServerException
    {
        JSONObject obj = new JSONObject();
        try
        {
            obj.put("state", result.getStatus());
            obj.put("result", result.getExitCode());
            obj.put("stdout", result.getStdout());
            obj.put("stderr", result.getStderr());
            response.getBody().write(obj.toString(4));
        }
        catch (JSONException e)
        {
            throw new ServerException(500, e.getMessage());
        }

        response.getHeaders().put("Content-Type", "application/json; charset=utf-8");
    }

    private class RuntimeResult
    {
        private String stdout;
        private String stderr;
        private int exitCode;
        private int status;

        private RuntimeResult(String stdout, String stderr, int exitCode, int status)
        {
            this.stdout = stdout;
            this.stderr = stderr;
            this.exitCode = exitCode;
            this.status = status;
        }

        String getStdout()
        {
            return this.stdout;
        }

        String getStderr()
        {
            return this.stderr;
        }

        int getExitCode()
        {
            return this.exitCode;
        }

        int getStatus()
        {
            return this.status;
        }
    }
}
