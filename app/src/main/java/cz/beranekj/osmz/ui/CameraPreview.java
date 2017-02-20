package cz.beranekj.osmz.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.renderscript.Allocation;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.ImageView;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import cz.beranekj.osmz.renderscript.RenderscriptManager;

import static android.content.Context.WINDOW_SERVICE;

public class CameraPreview implements SurfaceHolder.Callback, Camera.PreviewCallback
{
    private final ImageView image;
    private final SurfaceView surfaceView;
    private final SurfaceHolder holder;
    private FaceDetector detector;
    private Camera camera;
    private final RenderscriptManager manager;
    private final Context context;
    private byte[] buffer = null;

    private boolean previewActive = false;

    public CameraPreview(Context context, SurfaceView surfaceView, ImageView image, RenderscriptManager manager, Camera camera)
    {
        this.context = context;
        this.camera = camera;
        this.image = image;
        this.manager = manager;
        this.surfaceView = surfaceView;
        this.holder = this.surfaceView.getHolder();
        this.holder.addCallback(this);
        this.holder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
        this.detector = new FaceDetector.Builder(context)
                .setTrackingEnabled(true)
                .setLandmarkType(FaceDetector.NO_LANDMARKS)
                .setClassificationType(FaceDetector.NO_CLASSIFICATIONS)
                .setMode(FaceDetector.FAST_MODE)
                .build();
        this.startPreview();
    }

    public void startPreview()
    {
        if (this.camera == null) return;

        Camera.Parameters camParams = this.camera.getParameters();
        Camera.Size size = camParams.getSupportedPreviewSizes().get(5);
        camParams.setPreviewSize(size.width, size.height);
        this.modifyCameraParams(this.camera, camParams);
        this.camera.setParameters(camParams);

        if (this.previewActive)
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

        this.buffer = new byte[3110400];//camParams.getPreviewSize().width * camParams.getPreviewSize().height * (ImageFormat.getBitsPerPixel(camParams.getPreviewFormat()) / 8)];
        this.camera.addCallbackBuffer(this.buffer);
        this.camera.setPreviewCallbackWithBuffer(this);
        this.camera.startPreview();

        this.previewActive = true;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        this.startPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        this.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        this.camera = null;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera)
    {
        if (this.camera == null) return;

        Camera.Parameters parameters = this.camera.getParameters();
        int width = parameters.getPreviewSize().width;
        int height = parameters.getPreviewSize().height;

        Allocation alloc = this.manager.renderScriptNV21ToRGBA888(width, height, data);

        Bitmap bitmap = this.manager.allocToBitmap(alloc, width, height);
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<Face> faces = detector.detect(frame);
        for (int i = 0; i < faces.size(); i++)
        {
            Face face = faces.valueAt(i);
            if (face != null)
            {
                this.manager.blurRegion(bitmap, face.getPosition(), face.getWidth(), face.getHeight());
                break;
            }
        }

        this.image.setImageDrawable(new BitmapDrawable(this.context.getResources(), bitmap));
        this.camera.addCallbackBuffer(this.buffer);
    }

    public void stopPreview()
    {
        this.camera = null;
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
}
