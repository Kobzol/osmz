package cz.beranekj.osmz2.net.handler;

import android.graphics.Bitmap;
import android.hardware.Camera;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import javax.inject.Inject;

import cz.beranekj.osmz2.app.Application;
import cz.beranekj.osmz2.camera.CameraAnonymizer;
import cz.beranekj.osmz2.camera.CameraFrameListener;
import cz.beranekj.osmz2.net.http.HttpMethod;
import cz.beranekj.osmz2.net.http.Request;
import cz.beranekj.osmz2.net.http.RequestHandler;
import cz.beranekj.osmz2.net.http.Response;
import cz.beranekj.osmz2.net.http.ServerException;
import cz.beranekj.osmz2.net.server.ServerLog;
import cz.beranekj.osmz2.renderscript.RenderscriptManager;
import cz.beranekj.osmz2.util.ByteBuffer;

public class MotionJpegHandler implements RequestHandler
{
    private final static int DEFAULT_TIMELIMIT = 10000;

    @Inject RenderscriptManager renderscriptManager;

    private final Application application;
    private final CameraAnonymizer anonymizer;

    public MotionJpegHandler(Application application)
    {
        application.getInjector().inject(this);

        this.application = application;
        this.anonymizer = new CameraAnonymizer(application.getApplicationContext(), this.renderscriptManager);
    }

    @Override
    public boolean shouldHandle(Request request)
    {
        return request.getMethod() == HttpMethod.GET && request.getPath().startsWith("/camera");
    }

    @Override
    public void handle(Request request, Response response, ServerLog log) throws IOException, ServerException
    {
        try
        {
            Camera camera = this.openCamera(request);
            CameraFrameListener listener = new CameraFrameListener(this.application.getApplicationContext(), camera, false);
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
                        Bitmap bitmap = this.anonymizer.anonymize(data, listener.getPreviewSize().width, listener.getPreviewSize().height);
                        this.writeImage(bitmap, response.getOutputStream(), boundary);
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
        catch (RuntimeException e)
        {
            response.setCode(500);
            response.getBody().write("Couldn't open the camera");
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

    private void writeImage(Bitmap bitmap, OutputStream stream, String boundary) throws IOException
    {
       this.writeImage(this.compressBitmap(bitmap), stream, boundary);
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
    private byte[] compressBitmap(Bitmap bitmap)
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }
}
