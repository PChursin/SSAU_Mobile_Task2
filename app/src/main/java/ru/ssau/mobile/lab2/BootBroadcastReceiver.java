package ru.ssau.mobile.lab2;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

public class BootBroadcastReceiver extends BroadcastReceiver {

    private static final int PERIOD=5000;
    @Override
    public void onReceive(Context ctxt, Intent i) {
        scheduleAlarms(ctxt);
    }

    static void scheduleAlarms(Context ctxt) {
        AlarmManager mgr=
                (AlarmManager)ctxt.getSystemService(Context.ALARM_SERVICE);
        Intent i=new Intent(ctxt, ScheduledService.class);
        PendingIntent pi=PendingIntent.getService(ctxt, 0, i, 0);

        mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + PERIOD, PERIOD, pi);
        Log.d("BootBroadcastReceiver", "Scheduled task");
    }

    static void cancelAlarms(Context ctxt) {
        AlarmManager mgr=
                (AlarmManager)ctxt.getSystemService(Context.ALARM_SERVICE);
        Intent i=new Intent(ctxt, ScheduledService.class);
        PendingIntent pi=PendingIntent.getService(ctxt, 0, i, 0);
        mgr.cancel(pi);
        Log.d("BootBroadcastReceiver", "Cancelled task");
    }
}
