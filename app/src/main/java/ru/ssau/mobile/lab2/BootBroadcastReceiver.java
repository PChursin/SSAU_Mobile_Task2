package ru.ssau.mobile.lab2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class BootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
            Intent serviceIntent = new Intent(context, MeetingUpdater.class);
            context.startService(serviceIntent);
            Toast toast = Toast.makeText(context, "BroadcastReceiver: trying to start service!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
