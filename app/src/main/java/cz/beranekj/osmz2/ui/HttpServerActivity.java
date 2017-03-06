package cz.beranekj.osmz2.ui;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import butterknife.BindView;
import butterknife.OnClick;
import cz.beranekj.osmz2.R;
import cz.beranekj.osmz2.net.handler.MotionJpegHandler;
import cz.beranekj.osmz2.net.handler.ServeSDHandler;
import cz.beranekj.osmz2.net.handler.UploadFileHandler;
import cz.beranekj.osmz2.net.http.HttpServer;
import cz.beranekj.osmz2.net.server.MultiThreadServer;
import cz.beranekj.osmz2.net.server.NetServer;
import cz.beranekj.osmz2.service.ServerService;
import cz.beranekj.osmz2.util.SubscriptionManager;
import io.reactivex.android.schedulers.AndroidSchedulers;


public class HttpServerActivity extends BaseActivity implements OnClickListener
{
    @BindView(R.id.log) TextView logView;
    @BindView(R.id.reset_log) Button resetLogButton;

	private NetServer netServer = null;
	private HttpServer httpServer = new HttpServer();
    private final SubscriptionManager subscriptionManager = new SubscriptionManager();
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_http_server);

        this.bind();
        this.getApp().getInjector().inject(this);

        this.logView.setMovementMethod(new ScrollingMovementMethod());

        /*this.httpServer.addHandler(new MotionJpegHandler(this.getApp()));
        this.httpServer.addHandler(new ServeSDHandler(this.getApplicationContext()));
        this.httpServer.addHandler(new UploadFileHandler(this.getApplicationContext()));*/
    }

	@Override
    @OnClick({R.id.button1, R.id.button2})
	public void onClick(View v)
    {
		if (v.getId() == R.id.button1)
        {
            ServerService.startServerAction(this);
            /*if (this.netServer == null || !this.netServer.isRunning())
            {
                this.netServer = this.createServer();
                this.netServer.start();
                Toast.makeText(this, "NetServer started", Toast.LENGTH_SHORT).show();
            }*/
		}
		if (v.getId() == R.id.button2)
        {
            ServerService.stopServerAction(this);
            /*if (this.netServer != null)
            {
                try
                {
                    this.netServer.stop();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

                this.logMessage("Server stopped");

                this.netServer = null;
                Toast.makeText(this, "NetServer stopped", Toast.LENGTH_SHORT).show();
                this.subscriptionManager.unsubscribe();
            }*/
		}
	}

    @OnClick(R.id.reset_log)
    public void onResetLogClicked()
    {
        this.logView.setText("");
    }

    private NetServer createServer()
    {
        NetServer server = new MultiThreadServer(this.httpServer, 8080, 15);
        this.subscriptionManager.add(server.getLog().onMessageLogged()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::logMessage));

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
}
