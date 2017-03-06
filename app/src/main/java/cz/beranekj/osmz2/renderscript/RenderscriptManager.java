package cz.beranekj.osmz2.renderscript;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.Script;
import android.renderscript.ScriptIntrinsicBlur;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;

import cz.beranekj.osmz2.renderscript.ScriptC_invert;

public class RenderscriptManager
{
    private final RenderScript context;

    public RenderscriptManager(Context context)
    {
        this.context = RenderScript.create(context);
    }

    public void invert(Bitmap bitmap)
    {
        Allocation imageAlloc = Allocation.createFromBitmap(this.context, bitmap, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
        Allocation imageAllocOut = Allocation.createTyped(this.context, imageAlloc.getType());

        ScriptC_invert invert = new ScriptC_invert(this.context);
        invert.forEach_invert(imageAlloc, imageAllocOut);
        imageAllocOut.copyTo(bitmap);
    }

    public Allocation renderScriptNV21ToRGBA888(int width, int height, byte[] nv21)
    {
        ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(this.context, Element.U8_4(this.context));

        Type.Builder yuvType = new Type.Builder(this.context, Element.U8(this.context)).setX(nv21.length);
        Allocation in = Allocation.createTyped(this.context, yuvType.create(), Allocation.USAGE_SCRIPT);

        Type.Builder rgbaType = new Type.Builder(this.context, Element.RGBA_8888(this.context)).setX(width).setY(height);
        Allocation out = Allocation.createTyped(this.context, rgbaType.create(), Allocation.USAGE_SCRIPT);

        in.copyFrom(nv21);

        yuvToRgbIntrinsic.setInput(in);
        yuvToRgbIntrinsic.forEach(out);

        return out;
    }

    public Bitmap allocToBitmap(Allocation alloc, int width, int height)
    {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        alloc.copyTo(bitmap);
        return bitmap;
    }

    public Bitmap invertFromAlloc(Allocation alloc, int width, int height)
    {
        Allocation imageAllocOut = Allocation.createTyped(this.context, alloc.getType());

        ScriptC_invert invert = new ScriptC_invert(this.context);
        invert.forEach_invert(alloc, imageAllocOut);

        return this.allocToBitmap(imageAllocOut, width, height);
    }

    public void blurRegion(Bitmap bitmap, PointF position, float regionWidth, float regionHeight)
    {
        position.x = Math.max(position.x, 0.0f);
        position.y = Math.max(position.y, 0.0f);

        Allocation imageAlloc = Allocation.createFromBitmap(this.context, bitmap, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
        Allocation imageAllocOut = Allocation.createFromBitmap(this.context, bitmap, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);

        ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(this.context, Element.U8_4(this.context));
        blur.setInput(imageAlloc);
        blur.setRadius(15.0f);

        Script.LaunchOptions options = new Script.LaunchOptions();
        options.setX((int) position.x, (int) (position.x + regionWidth));
        options.setY((int) position.y, (int) (position.y + regionHeight));

        blur.forEach(imageAllocOut, options);

        imageAllocOut.copyTo(bitmap);
    }
}
