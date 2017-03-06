package cz.beranekj.osmz2.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import java.io.IOException;

import cz.beranekj.osmz2.R;
import cz.beranekj.osmz2.app.Application;
import cz.beranekj.osmz2.net.handler.MotionJpegHandler;
import cz.beranekj.osmz2.net.handler.ServeSDHandler;
import cz.beranekj.osmz2.net.handler.UploadFileHandler;
import cz.beranekj.osmz2.net.http.HttpServer;
import cz.beranekj.osmz2.net.server.MultiThreadServer;
import cz.beranekj.osmz2.net.server.NetServer;
import cz.beranekj.osmz2.ui.HttpServerActivity;
import cz.beranekj.osmz2.util.SubscriptionManager;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class ServerService extends Service
{
    private static final int NOTIFICATION_ID = 1;

    private static final String ACTION_START_SERVER = "cz.beranekj.osmz2.action.START_SERVER";
    private static final String ACTION_STOP_SERVER = "cz.beranekj.osmz2.action.STOP_SERVER";

    private NetServer netServer = null;
    private HttpServer httpServer = new HttpServer();
    private SubscriptionManager subscriptionManager = new SubscriptionManager();

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    public static void startServerAction(Context context)
    {
        Intent intent = new Intent(context, ServerService.class);
        intent.setAction(ACTION_START_SERVER);
        context.startService(intent);
    }
    public static void stopServerAction(Context context)
    {
        Intent intent = new Intent(context, ServerService.class);
        intent.setAction(ACTION_STOP_SERVER);
        context.startService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);
        this.onHandleIntent(intent);

        return START_STICKY;
    }

    protected void onHandleIntent(Intent intent)
    {
        if (intent != null)
        {
            final String action = intent.getAction();
            if (ACTION_START_SERVER.equals(action))
            {
                this.handleServerStart();
            } else if (ACTION_STOP_SERVER.equals(action))
            {
                this.handleServerStop();
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleServerStart()
    {
        if (this.netServer != null && this.netServer.isRunning())
        {
            return;
        }

        this.httpServer = new HttpServer();
        this.httpServer.addHandler(new MotionJpegHandler((Application) this.getApplication()));
        this.httpServer.addHandler(new ServeSDHandler(this.getApplicationContext()));
        this.httpServer.addHandler(new UploadFileHandler(this.getApplicationContext()));

        this.netServer = new MultiThreadServer(this.httpServer, 8080, 15);
        this.netServer.start();
        this.subscriptionManager.add(this.netServer.getLog().onMessageLogged().subscribe(this::updateNotification));

        this.startForeground(NOTIFICATION_ID, this.buildNotification("Server running"));
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleServerStop()
    {
        if (this.netServer == null) return;

        try
        {
            this.netServer.stop();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            this.httpServer = null;
            this.netServer = null;
            this.subscriptionManager.unsubscribe();
            this.stopSelf();
        }
    }

    private Notification buildNotification(String text)
    {
        Intent notificationIntent = new Intent(this, HttpServerActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        return new Notification.Builder(this)
                .setContentTitle("OSMZ Server")
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();
    }

    private void updateNotification(String text)
    {
        NotificationManager manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, this.buildNotification(text));
    }
}
