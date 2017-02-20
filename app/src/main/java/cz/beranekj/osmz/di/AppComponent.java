package cz.beranekj.osmz.di;

import javax.inject.Singleton;

import cz.beranekj.osmz.ui.FaceBlurActivity;
import cz.beranekj.osmz.ui.HttpServerActivity;
import dagger.Component;

@Singleton
@Component(modules={AppModule.class})
public interface AppComponent
{
    void inject(HttpServerActivity activity);
    void inject(FaceBlurActivity activity);
}
