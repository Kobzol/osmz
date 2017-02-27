package cz.beranekj.osmz.net.handler;

import android.hardware.Camera;
import android.net.Uri;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.beranekj.osmz.app.Application;
import cz.beranekj.osmz.camera.CameraFrameListener;
import cz.beranekj.osmz.net.http.HttpMethod;
import cz.beranekj.osmz.net.http.Request;
import cz.beranekj.osmz.net.http.RequestHandler;
import cz.beranekj.osmz.net.http.Response;
import cz.beranekj.osmz.net.http.ServerException;
import cz.beranekj.osmz.net.server.ServerLog;
import cz.beranekj.osmz.util.ByteBuffer;

public class MotionJpegHandler implements RequestHandler
{
    private final static int DEFAULT_TIMELIMIT = 10000;

    private final Application application;

    public MotionJpegHandler(Application application)
    {
        this.application = application;
    }

    @Override
    public boolean shouldHandle(Request request)
    {
        return request.getMethod() == HttpMethod.GET && request.getPath().startsWith("/camera");
    }

    @Override
    public void handle(Request request, Response response, ServerLog log) throws IOException, ServerException
    {
        Camera camera = this.openCamera(request);
        CameraFrameListener listener = new CameraFrameListener(this.application.getApplicationContext(), camera, true);
        listener.startPreview();

        String boundary = "mjpeg--boundary--mjpeg";

        ByteBuffer header = new ByteBuffer();
        header.write("HTTP/1.0 200 OK\r\n");
        header.write("Content-Type: multipart/x-mixed-replace;boundary=" + boundary + "\r\n\r\n");
        response.getOutputStream().write(header.getBuffer());
        response.getOutputStream().flush();

        Date start = new Date();
        int timelimit = this.getTimelimit(request);
        try
        {
            while (new Date().getTime() - start.getTime() < timelimit)
            {
                byte[] data = listener.getLastFrame();
                if (data != null)
                {
                    this.writeImage(data, response.getOutputStream(), boundary);
                }

                try
                {
                    Thread.sleep(5);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }
        finally
        {
            listener.stopPreview();
            camera.release();
        }
    }

    private Camera openCamera(Request request)
    {
        String cameraIdQuery = request.getUri().getQueryParameter("camera");
        if (cameraIdQuery != null)
        {
            int cameraId = Integer.parseInt(cameraIdQuery);
            if (cameraId >= 0 && cameraId < Camera.getNumberOfCameras())
            {
                return Camera.open(cameraId);
            }
        }

        return Camera.open();
    }
    private int getTimelimit(Request request)
    {
        String timelimitQuery = request.getUri().getQueryParameter("timelimit");

        if (timelimitQuery != null)
        {
            try
            {
                int timelimit = Integer.parseInt(timelimitQuery);
                if (timelimit >= 0 && timelimit < MotionJpegHandler.DEFAULT_TIMELIMIT * 3)
                {
                    return timelimit;
                }
            }
            catch (Exception ignored)
            {

            }
        }

        return MotionJpegHandler.DEFAULT_TIMELIMIT;
    }

    private void writeImage(byte[] data, OutputStream stream, String boundary) throws IOException
    {
        ByteBuffer buffer = new ByteBuffer();
        buffer.write("--" + boundary + "\r\n");
        buffer.write("Content-Type: image/jpeg\r\n");
        buffer.write("Content-Length: " + String.valueOf(data.length) + "\r\n");
        buffer.write("\r\n");
        buffer.write(data);
        buffer.write("--" + boundary + "\r\n");

        stream.write(buffer.getBuffer());
        stream.flush();
    }
}
