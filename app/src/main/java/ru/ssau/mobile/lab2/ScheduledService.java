/***
 * Copyright (c) 2012 CommonsWare, LLC
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
 * by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 * <p>
 * From _The Busy Coder's Guide to Android Development_
 * https://commonsware.com/Android
 */

package ru.ssau.mobile.lab2;


import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;

import ru.ssau.mobile.lab2.models.Meeting;

public class ScheduledService extends IntentService {

    private static final int NOTIFY_ID = 101;
    private static final String TAG = "ScheduledService";

    public ScheduledService() {
        super("ScheduledService");
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onHandleIntent(Intent intent) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null)
            return;
        Log.d(getClass().getSimpleName(), "I ran!");
        ArrayList<String> ids = null;
        ArrayList<Meeting> meetings = null;
        try {
            FileInputStream fis = getApplicationContext().openFileInput(
                    getResources().getString(R.string.meetings_serialize));
            ObjectInputStream is = new ObjectInputStream(fis);
            meetings = (ArrayList<Meeting>) is.readObject();
            is.close();
            fis.close();
            fis = getApplicationContext().openFileInput(
                    getResources().getString(R.string.meetings_ids_serialize));
            is = new ObjectInputStream(fis);
            ids = (ArrayList<String>) is.readObject();
            is.close();
            fis.close();
        } catch (ClassNotFoundException | IOException e) {
            Log.e(TAG, "Error during read: ", e);
            return;
        }
        final HashMap<String, Meeting> meetingHashMap = new HashMap<>();
        for (int i = 0; i < meetings.size(); i++) {
            meetingHashMap.put(ids.get(i), meetings.get(i));
        }
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("meetings");
        /*ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot part : dataSnapshot.getChildren()) {
                    Meeting m = meetingHashMap.get(part.getKey());
                    Meeting fromPart = part.getValue(Meeting.class);
                    if (m == null || !m.equalsTo(fromPart)) {
                        showNotification(fromPart.getSubject());
                        return;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showNotification(String subject) {
        Context context = getApplicationContext();

        Intent notificationIntent = new Intent(context, MeetingsActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context,
                0, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        Resources res = context.getResources();
        Notification.Builder builder = new Notification.Builder(context);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        builder.setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.ic_small_icon)
                // большая картинка
                .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.ic_small_icon))
                //.setTicker(res.getString(R.string.warning)) // текст в строке состояния
                .setTicker("New meetings")
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                //.setContentTitle(res.getString(R.string.notifytitle)) // Заголовок уведомления
                .setContentTitle("Meetings: " + subject)
                //.setContentText(res.getString(R.string.notifytext))
                .setContentText("New meetings arrived. Click to check") // Текст уведомления
                .setSound(alarmSound);

        Notification notification = builder.build();

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFY_ID, notification);
    }
}
