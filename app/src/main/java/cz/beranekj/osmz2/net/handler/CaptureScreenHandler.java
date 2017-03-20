package cz.beranekj.osmz2.net.handler;

import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Date;

import cz.beranekj.osmz2.net.http.HttpMethod;
import cz.beranekj.osmz2.net.http.Request;
import cz.beranekj.osmz2.net.http.RequestHandler;
import cz.beranekj.osmz2.net.http.Response;
import cz.beranekj.osmz2.net.http.ServerException;
import cz.beranekj.osmz2.net.server.ServerLog;
import cz.beranekj.osmz2.ui.HttpServerActivity;

public class CaptureScreenHandler implements RequestHandler
{
    private Bitmap bitmap = null;

    @Override
    public boolean shouldHandle(Request request)
    {
        return request.getMethod() == HttpMethod.GET && request.getPath().startsWith("/capture");
    }

    @Override
    public void handle(Request request, Response response, ServerLog log) throws IOException, ServerException
    {
        String boundary = "mjpeg--boundary--mjpeg";

        cz.beranekj.osmz2.util.ByteBuffer header = new cz.beranekj.osmz2.util.ByteBuffer();
        header.write("HTTP/1.0 200 OK\r\n");
        header.write("Content-Type: multipart/x-mixed-replace;boundary=" + boundary + "\r\n\r\n");
        response.getOutputStream().write(header.getBuffer());
        response.getOutputStream().flush();

        ImageReader reader = ImageReader.newInstance(640, 480, PixelFormat.RGBA_8888, 20);
        reader.setOnImageAvailableListener(r ->
        {
            Image image = reader.acquireLatestImage();
            Image.Plane[] planes = image.getPlanes();
            ByteBuffer buffer = planes[0].getBuffer();
            int pixelStride = planes[0].getPixelStride();
            int rowStride = planes[0].getRowStride();
            int rowPadding = rowStride - pixelStride * reader.getWidth();
            this.bitmap = Bitmap.createBitmap(reader.getWidth() + rowPadding / pixelStride,
                    reader.getHeight(), Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(buffer);
        }, HttpServerActivity.handler);

        this.capture(reader);

        Date start = new Date();
        int timelimit = 20000;
        while (new Date().getTime() - start.getTime() < timelimit)
        {
            try
            {
                if (this.bitmap != null)
                {
                    this.writeImage(this.bitmap, response.getOutputStream(), boundary);
                    this.bitmap = null;

                    this.capture(reader);
                }
                else Thread.sleep(500);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    private VirtualDisplay capture(ImageReader reader)
    {
        return HttpServerActivity.projection.createVirtualDisplay(
                "ScreenCapture",
                reader.getWidth(),
                reader.getHeight(),
                HttpServerActivity.density,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                reader.getSurface(),
                null,
                HttpServerActivity.handler
        );
    }

    private void writeImage(Bitmap bitmap, OutputStream stream, String boundary) throws IOException
    {
        this.writeImage(this.compressBitmap(bitmap), stream, boundary);
    }
    private void writeImage(byte[] data, OutputStream stream, String boundary) throws IOException
    {
        cz.beranekj.osmz2.util.ByteBuffer buffer = new cz.beranekj.osmz2.util.ByteBuffer();
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
