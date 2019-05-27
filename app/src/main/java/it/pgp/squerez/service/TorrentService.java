package it.pgp.squerez.service;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import org.gudy.azureus2.ui.console.ConsoleInputHelperFactory;

import it.pgp.squerez.MainActivity;
import it.pgp.squerez.enums.ForegroundServiceType;

public class TorrentService extends BaseBackgroundService {

    private static final int FOREGROUND_SERVICE_NOTIFICATION_ID = 0x4077E14;
    private static final String BROADCAST_ACTION = "http_download_service_broadcast_action";

    private String foreground_content_text;
    private String foreground_ticker;
    private String foreground_pause_action_label;
    private String foreground_stop_action_label;

    @Override
    public int getForegroundServiceNotificationId() {
        return FOREGROUND_SERVICE_NOTIFICATION_ID;
    }

    @Override
    public final ForegroundServiceType getForegroundServiceType() {
        return ForegroundServiceType.TORRENT_DOWNLOAD;
    }

    @Override
    protected void prepareLabels() {
        foreground_ticker="Squerez torrent manager is active";
        foreground_content_text="Download in progress...";
        foreground_pause_action_label="Pause all";
        foreground_stop_action_label="Shutdown";
    }

    @Override
    protected NotificationCompat.Builder getForegroundNotificationBuilder() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(BROADCAST_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Intent pauseIntent = new Intent(this, this.getClass());
        pauseIntent.setAction(PAUSE_ACTION);
        PendingIntent ppauseIntent = PendingIntent.getService(this, 0,
                pauseIntent, 0);

        Intent stopIntent = new Intent(this, this.getClass());
        stopIntent.setAction(CANCEL_ACTION);
        PendingIntent pstopIntent = PendingIntent.getService(this, 0,
                stopIntent, 0);

        Bitmap icon = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(
                        getResources(),
                        android.R.drawable.ic_dialog_info), // TODO replace icons
                128, 128, false);

        return new NotificationCompat.Builder(this,getPackageName())
                .setContentTitle("Squerez")
                .setTicker(foreground_ticker)
                .setContentText(foreground_content_text)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // TODO replace icons
                .setLargeIcon(icon)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .addAction(android.R.drawable.ic_media_pause, foreground_pause_action_label, // TODO replace icons
                        ppauseIntent)
                .addAction(android.R.drawable.ic_dialog_alert, foreground_stop_action_label, // TODO replace icons
                        pstopIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try { ConsoleInputHelperFactory.currentCommandReader.close(); } catch (Exception ignored) {}
    }

    @Override
    protected boolean onStartAction() {
        TorrentMonitor.startVuzeCLIEngine();
        return true;
    }
}
