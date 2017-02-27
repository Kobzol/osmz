package cz.beranekj.osmz.ui;

import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.ImageView;

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
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        this.stopPreview();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        this.startPreview();
    }

    private void startPreview()
    {
        this.camera = Camera.open(1);
        this.preview = new CameraPreview(this, this.surface, img, this.renderscriptManager, this.camera);
        this.preview.startPreview();
    }
    private void stopPreview()
    {
        this.preview.stopPreview();
        this.preview.dispose();
        this.preview = null;
        this.camera.release();
        this.camera = null;
    }
}
