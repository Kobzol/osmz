package cz.beranekj.osmz.renderscript;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;

public class RenderscriptManager
{
    private RenderScript context;

    public void init(Context context)
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

    public void destroy()
    {
        this.context.destroy();
        this.context = null;
    }
}
