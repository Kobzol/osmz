package cz.beranekj.osmz.di;

import android.content.Context;

import cz.beranekj.osmz.renderscript.RenderscriptManager;
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
