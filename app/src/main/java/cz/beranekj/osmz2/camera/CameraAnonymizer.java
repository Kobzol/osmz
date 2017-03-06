package cz.beranekj.osmz2.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import cz.beranekj.osmz2.renderscript.RenderscriptManager;

public class CameraAnonymizer
{
    private final RenderscriptManager manager;
    private final FaceDetector detector;

    public CameraAnonymizer(Context context, RenderscriptManager manager)
    {
        this.manager = manager;
        this.detector = new FaceDetector.Builder(context)
                .setTrackingEnabled(true)
                .setLandmarkType(FaceDetector.NO_LANDMARKS)
                .setClassificationType(FaceDetector.NO_CLASSIFICATIONS)
                .setMode(FaceDetector.FAST_MODE)
                .build();
    }

    public Bitmap anonymize(byte[] image, int width, int height)
    {
        Allocation alloc = this.manager.renderScriptNV21ToRGBA888(width, height, image);

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

        return bitmap;
    }

    public void dispose()
    {
        this.detector.release();
    }
}
