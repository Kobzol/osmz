package cz.beranekj.osmz2.di;

import javax.inject.Singleton;

import cz.beranekj.osmz2.net.handler.MotionJpegHandler;
import cz.beranekj.osmz2.ui.FaceBlurActivity;
import cz.beranekj.osmz2.ui.HttpServerActivity;
import dagger.Component;

@Singleton
@Component(modules={AppModule.class})
public interface AppComponent
{
    void inject(HttpServerActivity activity);
    void inject(FaceBlurActivity activity);

    void inject(MotionJpegHandler motionJpegHandler);
}
