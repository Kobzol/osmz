package cz.beranekj.osmz2.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;

import cz.beranekj.osmz2.camera.CameraAnonymizer;
import cz.beranekj.osmz2.camera.CameraFrameListener;
import cz.beranekj.osmz2.renderscript.RenderscriptManager;
import io.reactivex.android.schedulers.AndroidSchedulers;

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
