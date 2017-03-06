package cz.beranekj.osmz2.app;

import cz.beranekj.osmz2.di.AppComponent;
import cz.beranekj.osmz2.di.AppModule;
import cz.beranekj.osmz2.di.DaggerAppComponent;

public class Application extends android.app.Application
{
    private AppComponent injector;

    @Override
    public void onCreate()
    {
        super.onCreate();

        this.injector = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();
    }

    public AppComponent getInjector()
    {
        return this.injector;
    }
}
