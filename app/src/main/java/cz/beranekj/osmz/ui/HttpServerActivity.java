package cz.beranekj.osmz.ui;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.OnClick;
import cz.beranekj.osmz.R;
import cz.beranekj.osmz.handler.ServeSDHandler;
import cz.beranekj.osmz.server.HttpServer;
import cz.beranekj.osmz.server.SocketServer;


public class HttpServerActivity extends Activity implements OnClickListener
{
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

	private SocketServer socketServer = null;
	private HttpServer server = new HttpServer();
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_http_server);

        ButterKnife.bind(this);

        this.verifyStoragePermissions();

        this.server.addHandler(new ServeSDHandler(this.getApplicationContext()));
    }

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     */
    public void verifyStoragePermissions()
    {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED)
        {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

	@Override
    @OnClick({R.id.button1, R.id.button2})
	public void onClick(View v)
    {
		if (v.getId() == R.id.button1)
        {
            if (this.socketServer == null || !this.socketServer.isRunning())
            {
                this.socketServer = new SocketServer(this.server);
                this.socketServer.start();
                Toast.makeText(this, "Server started", Toast.LENGTH_SHORT).show();
            }
		}
		if (v.getId() == R.id.button2)
        {
            if (this.socketServer != null && this.socketServer.isRunning())
            {
                this.socketServer.close();
                try
                {
                    this.socketServer.join();
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }

                this.socketServer = null;

                Toast.makeText(this, "Server stopped", Toast.LENGTH_SHORT).show();
            }
		}
	}
}
