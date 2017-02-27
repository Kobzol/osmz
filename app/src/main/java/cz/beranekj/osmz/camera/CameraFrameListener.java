package cz.beranekj.osmz.camera;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import java.io.ByteArrayOutputStream;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

import static android.content.Context.WINDOW_SERVICE;

public class CameraFrameListener implements Camera.PreviewCallback
{
    private final Camera camera;
    private final Context context;

    private byte[] buffer = null;
    private boolean previewActive = false;

    private byte[] lastFrame = null;
    private Subject<byte[]> frameStream = PublishSubject.create();

    public CameraFrameListener(Context context, Camera camera)
    {
        this.context = context;
        this.camera = camera;
    }

    public Observable<byte[]> getFrameStream()
    {
        return this.frameStream;
    }

    public void startPreview()
    {
        Camera.Parameters camParams = this.camera.getParameters();
        Camera.Size size = camParams.getSupportedPreviewSizes().get(5);
        camParams.setPreviewSize(size.width, size.height);
        this.modifyCameraParams(this.camera, camParams);
        this.camera.setParameters(camParams);

        if (this.previewActive)
        {
            this.stopPreview();
        }

        this.buffer = new byte[3110400];//camParams.getPreviewSize().width * camParams.getPreviewSize().height * (ImageFormat.getBitsPerPixel(camParams.getPreviewFormat()) / 8)];
        this.camera.addCallbackBuffer(this.buffer);
        this.camera.setPreviewCallbackWithBuffer(this);
        this.camera.startPreview();

        this.previewActive = true;
    }

    public void stopPreview()
    {
        this.previewActive = false;
        try
        {
            this.camera.stopPreview();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public byte[] getLastFrame()
    {
        return this.lastFrame;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera)
    {
        data = this.convertToJpeg(data);
        this.lastFrame = data;
        this.frameStream.onNext(data);
        this.camera.addCallbackBuffer(this.buffer);
    }

    private void modifyCameraParams(Camera camera, Camera.Parameters parameters)
    {
        int width = parameters.getPreviewSize().width;
        int height = parameters.getPreviewSize().height;

        Display display = ((WindowManager) this.context.getSystemService(WINDOW_SERVICE)).getDefaultDisplay();

        if (display.getRotation() == Surface.ROTATION_90)
        {
            parameters.setPreviewSize(width, height);
        }

        if (display.getRotation() == Surface.ROTATION_270)
        {
            parameters.setPreviewSize(width, height);
            camera.setDisplayOrientation(180);
        }
    }

    private byte[] convertToJpeg(byte[] data)
    {
        Camera.Parameters parameters = this.camera.getParameters();
        Camera.Size size = parameters.getPreviewSize();

        YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
        Rect rectangle = new Rect();
        rectangle.bottom = size.height;
        rectangle.top = 0;
        rectangle.left = 0;
        rectangle.right = size.width;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        image.compressToJpeg(rectangle, 100, out);

        return out.toByteArray();
    }
}
