package cz.beranekj.osmz.camera;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.google.android.gms.common.SupportErrorDialogFragment;

import java.io.ByteArrayOutputStream;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

import static android.content.Context.WINDOW_SERVICE;

public class CameraFrameListener implements Camera.PreviewCallback
{
    private Camera camera;
    private final Context context;
    private final boolean convertToJpeg;

    private byte[] buffer = null;

    private boolean previewActive = false;
    private byte[] lastFrame = null;
    private Subject<byte[]> frameStream = PublishSubject.create();

    private Camera.Size previewSize = null;

    public CameraFrameListener(Context context, Camera camera, boolean convertToJpeg)
    {
        this.context = context;
        this.camera = camera;
        this.convertToJpeg = convertToJpeg;
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

        this.previewSize = this.camera.getParameters().getPreviewSize();

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
    public Camera.Size getPreviewSize()
    {
        return this.previewSize;
    }
    public boolean isPreviewActive()
    {
        return this.previewActive;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera)
    {
        if (this.convertToJpeg)
        {
            data = this.convertToJpeg(data);
        }

        this.lastFrame = data;
        this.frameStream.onNext(data);
        this.camera.addCallbackBuffer(this.buffer);
    }

    private void modifyCameraParams(Camera camera, Camera.Parameters parameters)
    {
        Display display = ((WindowManager) this.context.getSystemService(WINDOW_SERVICE)).getDefaultDisplay();

        if (display.getRotation() == Surface.ROTATION_0)
        {
            parameters.set("orientation", "portrait");
            camera.setDisplayOrientation(90);
        }

        if (display.getRotation() == Surface.ROTATION_270)
        {
            camera.setDisplayOrientation(180);
        }
    }

    private byte[] convertToJpeg(byte[] data)
    {
        Camera.Size size = this.previewSize;

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
