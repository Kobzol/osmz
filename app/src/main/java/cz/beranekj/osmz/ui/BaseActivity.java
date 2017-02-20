package cz.beranekj.osmz.ui;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

import butterknife.ButterKnife;
import cz.beranekj.osmz.app.Application;

public abstract class BaseActivity extends Activity
{
    protected Application getApp()
    {
        return (Application) this.getApplication();
    }

    protected void bind()
    {
        ButterKnife.bind(this);
    }

    protected boolean hasPermission(String permission)
    {
        return ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }
}
