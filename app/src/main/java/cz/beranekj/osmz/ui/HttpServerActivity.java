package cz.beranekj.osmz.ui;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cz.beranekj.osmz.R;
import cz.beranekj.osmz.net.handler.ServeSDHandler;
import cz.beranekj.osmz.net.handler.UploadFileHandler;
import cz.beranekj.osmz.net.server.HttpServer;
import cz.beranekj.osmz.net.server.MultiThreadServer;
import cz.beranekj.osmz.net.server.NetServer;
import cz.beranekj.osmz.renderscript.RenderscriptManager;


public class HttpServerActivity extends Activity implements OnClickListener
{
    @BindView(R.id.log) TextView logView;
    @BindView(R.id.reset_log) Button resetLogButton;
    @BindView(R.id.img) ImageView img;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

	private NetServer netServer = null;
	private HttpServer httpServer = new HttpServer();
    private final RenderscriptManager rsManager = new RenderscriptManager();
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_http_server);

        ButterKnife.bind(this);

        this.logView.setMovementMethod(new ScrollingMovementMethod());

        this.verifyStoragePermissions();

        this.httpServer.addHandler(new ServeSDHandler(this.getApplicationContext()));
        this.httpServer.addHandler(new UploadFileHandler(this.getApplicationContext()));

        this.rsManager.init(this);
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
            if (this.netServer == null || !this.netServer.isRunning())
            {
                this.netServer = this.createServer();
                this.netServer.start();
                Toast.makeText(this, "NetServer started", Toast.LENGTH_SHORT).show();
            }
		}
		if (v.getId() == R.id.button2)
        {
            if (this.netServer != null && this.netServer.isRunning())
            {
                try
                {
                    this.netServer.stop();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                this.netServer = null;
                Toast.makeText(this, "NetServer stopped", Toast.LENGTH_SHORT).show();
            }
		}
	}

    @OnClick(R.id.reset_log)
    public void onResetLogClicked()
    {
        this.logView.setText("");
    }

    private NetServer createServer()
    {
        NetServer server = new MultiThreadServer(this.httpServer, 8080);
        server.getLog().addListener(message -> runOnUiThread(() -> this.logMessage(message)));

        return server;
    }

    private void logMessage(String message)
    {
        message = this.formatDate(new GregorianCalendar(), "hh:mm:ss") + ": " + message;
        this.logView.setText(this.logView.getText().toString() + "\n" + message);
    }

    private String formatDate(Calendar date, String format)
    {
        return new SimpleDateFormat(format).format(date.getTime());
    }

    private Bitmap loadBitmap(int resource)
    {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeResource(getResources(), resource, options);
    }
}
