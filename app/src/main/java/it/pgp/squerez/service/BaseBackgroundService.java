package it.pgp.squerez.service;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.Serializable;

import it.pgp.squerez.MainActivity;
import it.pgp.squerez.enums.ForegroundServiceType;

/**
onStartCommand's intent must contain at least:
 FOREGROUND_SERVICE_NOTIFICATION_ID (assigned from corresponding static field of subclasses)
 BROADCAST_ACTION (assigned from subclasses as well)
 sub-class dependent params
 */

// imported/adapted from XFiles

public abstract class BaseBackgroundService extends Service {

    public static final String START_ACTION = "Start";
    public static final String PAUSE_ACTION = "Pause"; // pause, on next activity open, show results found so far
    public static final String CANCEL_ACTION = "Cancel"; // cancel, on next activity open, show results found so far
	String currentAction;

	NotificationManager notificationManager;
    PowerManager mgr;
    PowerManager.WakeLock wakeLock;

    public Serializable params;

    public abstract int getForegroundServiceNotificationId();

    public abstract ForegroundServiceType getForegroundServiceType();
	
	@Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void abortServiceWithoutConfirmation() {
        stopForeground(true);
        stopSelf();
        try { MainActivity.mainActivity.finishAffinity(); }
        catch (NullPointerException ignored) {}
    }
	
	protected abstract void prepareLabels();
	protected abstract NotificationCompat.Builder getForegroundNotificationBuilder();

	protected static NotificationChannel notificationChannel;
	protected void createNotificationChannelForService() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            if (notificationChannel == null) {
                notificationChannel = new NotificationChannel(getPackageName(),"nch", NotificationManager.IMPORTANCE_LOW);
                notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                manager.createNotificationChannel(notificationChannel);
            }
        }
    }
	
	@Override
    public void onDestroy() {
        try { wakeLock.release(); Log.e("SERVICE","wakelock RELEASED"); } catch (Exception ignored) {}
        super.onDestroy();
    }
	
	@Override
    public void onCreate()
    {
        super.onCreate();
        mgr  = (PowerManager)getApplicationContext().getSystemService(Context.POWER_SERVICE);
        wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getPackageName()+":theWakeLock");
        notificationManager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
    }
	
	public void startAndShowNotificationBar() {
        wakeLock.acquire();
        Log.e("SERVICE","wakelock acquired");

        switch (currentAction) {
            case START_ACTION:
                if (!onStartAction()) {
                    Toast.makeText(getApplicationContext(), "Cannot start service, overlay is busy", Toast.LENGTH_SHORT).show();
                    wakeLock.release(); Log.e("SERVICE","wakelock RELEASED");
                    return;
                }
                break;
            // Forbidden zone
            case CANCEL_ACTION:
            case PAUSE_ACTION:
                Toast.makeText(getApplicationContext(),
                        "Service not running, pause/cancel command should not arrive here",
                        Toast.LENGTH_SHORT).show();
                return;
            default:
                // DEBUG Forbidden zone
                Toast.makeText(getApplicationContext(),
                        "Unknown action in onStartCommand",
                        Toast.LENGTH_SHORT).show();
                return;
        }

        /************************** build notification **************************/

        Notification notification = getForegroundNotificationBuilder().build();
        createNotificationChannelForService();
        startForeground(getForegroundServiceNotificationId(),notification);
    }

    protected abstract boolean onStartAction();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(currentAction == null) {
            currentAction = intent.getAction();

            if (!START_ACTION.equals(currentAction)) {
                Toast.makeText(this, "Service not yet started, expected start action", Toast.LENGTH_SHORT).show();
                abortServiceWithoutConfirmation();
                return START_NOT_STICKY;
            }

            prepareLabels();
            startAndShowNotificationBar();
        }
        else {
            // trying to abort?
            if (CANCEL_ACTION.equals(intent.getAction())) {
                abortServiceWithoutConfirmation();
            }
            // trying to start another concurrent task?
            else {
                Toast.makeText(getApplicationContext(),
                        "Service already running!",
                        Toast.LENGTH_SHORT).show();
            }
        }
        return START_NOT_STICKY;
    }
	
}