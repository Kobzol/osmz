package cz.beranekj.osmz2.di;

import android.content.Context;

import cz.beranekj.osmz2.renderscript.RenderscriptManager;
import dagger.Module;
import dagger.Provides;

@Module
public class AppModule
{
    private final Context context;

    public AppModule(Context context)
    {
        this.context = context;
    }

    @Provides
    RenderscriptManager provideRenderscriptManager()
    {
        return new RenderscriptManager(this.context);
    }
}
