package cz.beranekj.osmz.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.LargestFaceFocusingProcessor;

import java.io.IOException;

import javax.inject.Inject;

import butterknife.BindView;
import cz.beranekj.osmz.R;
import cz.beranekj.osmz.renderscript.RenderscriptManager;

public class FaceBlurActivity extends BaseActivity
{
    @BindView(R.id.container) ViewGroup container;
    @BindView(R.id.surface) SurfaceView surface;
    @BindView(R.id.img) ImageView img;

    @Inject RenderscriptManager renderscriptManager;

    private Camera camera = null;
    private CameraPreview preview;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_blur);

        this.bind();
        this.getApp().getInjector().inject(this);

        this.camera = this.openCamera();
        this.preview = new CameraPreview(this, this.surface, img, this.renderscriptManager, this.camera);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        this.preview.stopPreview();
        this.camera.stopPreview();
        this.camera.release();
        this.camera = null;
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (this.camera == null)
        {
            this.camera = this.openCamera();
            this.preview = new CameraPreview(this, this.surface, img, this.renderscriptManager, this.camera);
        }
    }

    private Camera openCamera()
    {
        return Camera.open(1);
    }

    private void faceDetection()
    {
        try
        {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
            {
                FaceDetector detector = new FaceDetector.Builder(this)
                        .setTrackingEnabled(true)
                        .setMode(FaceDetector.FAST_MODE)
                        .build();

                detector.setProcessor(new LargestFaceFocusingProcessor(detector, new FaceTracker()));

                CameraSource mCameraSource = new CameraSource.Builder(this, detector)
                        .setRequestedPreviewSize(640, 480)
                        .setFacing(CameraSource.CAMERA_FACING_BACK)
                        .setRequestedFps(30.0f)
                        .build();
                        //.start(this.surface.getHolder());
            }
        }
        catch (Exception ignored)
        {

        }
    }

    private class FaceTracker extends Tracker<Face>
    {
        @Override
        public void onUpdate(Detector.Detections<Face> detections, Face face)
        {
            super.onUpdate(detections, face);
            Log.d("OSMZ", "Face tracked: " + String.valueOf(face.getIsSmilingProbability()));
        }
    }
}
