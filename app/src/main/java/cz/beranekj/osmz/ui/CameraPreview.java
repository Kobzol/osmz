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

import cz.beranekj.osmz.camera.CameraAnonymizer;
import cz.beranekj.osmz.camera.CameraFrameListener;
import cz.beranekj.osmz.renderscript.RenderscriptManager;
import cz.beranekj.osmz.util.SubscriptionManager;
import io.reactivex.android.schedulers.AndroidSchedulers;

import static android.content.Context.WINDOW_SERVICE;

public class CameraPreview implements SurfaceHolder.Callback
{
    private final ImageView image;
    private final Context context;
    private final Camera camera;
    private final CameraAnonymizer anonymizer;
    private final CameraFrameListener listener;

    public CameraPreview(Context context, SurfaceView surfaceView, ImageView image, RenderscriptManager manager, Camera camera)
    {
        this.context = context;
        this.image = image;
        this.camera = camera;

        SurfaceHolder holder = surfaceView.getHolder();
        holder.addCallback(this);

        this.anonymizer = new CameraAnonymizer(context, manager);
        this.listener = new CameraFrameListener(context, camera, false);
        this.listener.getFrameStream()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleImage);
    }

    public void startPreview()
    {
        this.listener.startPreview();
    }
    public void stopPreview()
    {
        this.listener.stopPreview();
    }
    public void dispose()
    {
        this.anonymizer.dispose();
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
        this.stopPreview();
    }

    private void handleImage(byte[] image)
    {
        if (!this.listener.isPreviewActive()) return;

        Camera.Size previewSize = this.listener.getPreviewSize();

        Bitmap anonymizedBitmap = this.anonymizer.anonymize(image, previewSize.width, previewSize.height);
        this.image.setImageDrawable(new BitmapDrawable(this.context.getResources(), anonymizedBitmap));
    }
}
