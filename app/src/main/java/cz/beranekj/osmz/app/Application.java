package cz.beranekj.osmz.app;

import cz.beranekj.osmz.di.AppComponent;
import cz.beranekj.osmz.di.AppModule;
import cz.beranekj.osmz.di.DaggerAppComponent;

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
