package cz.beranekj.osmz2.ui;

import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.OnClick;
import cz.beranekj.osmz2.R;
import cz.beranekj.osmz2.service.ServerService;


public class HttpServerActivity extends BaseActivity implements OnClickListener
{
    @BindView(R.id.log) TextView logView;
    @BindView(R.id.reset_log) Button resetLogButton;

    public static int density;
    private MediaProjectionManager projectionManager;
    public static MediaProjection projection;
    public static Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_http_server);

        this.bind();
        this.getApp().getInjector().inject(this);

        this.logView.setMovementMethod(new ScrollingMovementMethod());

        DisplayMetrics metrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        HttpServerActivity.density = metrics.densityDpi;

        this.projectionManager = (MediaProjectionManager) this.getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        Intent intent = this.projectionManager.createScreenCaptureIntent();
        this.startActivityForResult(intent, 0);

        HttpServerActivity.handler = new Handler();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        HttpServerActivity.projection = this.projectionManager.getMediaProjection(resultCode, data);
    }

    @Override
    @OnClick({R.id.button1, R.id.button2})
	public void onClick(View v)
    {
		if (v.getId() == R.id.button1)
        {
            ServerService.startServerAction(this);
		}
		if (v.getId() == R.id.button2)
        {
            ServerService.stopServerAction(this);
		}
	}

    @OnClick(R.id.reset_log)
    public void onResetLogClicked()
    {
        this.logView.setText("");
    }

    private String formatDate(Calendar date, String format)
    {
        return new SimpleDateFormat(format).format(date.getTime());
    }
}
