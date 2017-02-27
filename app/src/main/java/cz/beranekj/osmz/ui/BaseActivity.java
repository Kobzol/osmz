package cz.beranekj.osmz.ui;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import butterknife.ButterKnife;
import cz.beranekj.osmz.app.Application;

public abstract class BaseActivity extends Activity
{
    private static final String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.requestPermissions();
    }

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

    protected void requestPermissions()
    {
        for (String permission : BaseActivity.PERMISSIONS)
        {
            if (!this.hasPermission(permission))
            {
                ActivityCompat.requestPermissions(
                        this,
                        BaseActivity.PERMISSIONS,
                        1
                );
            }
        }
    }
}
