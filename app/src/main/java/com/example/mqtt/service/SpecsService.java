package com.example.mqtt.service;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.mqtt.Configuration;
import com.example.mqtt.R;
import com.example.mqtt.model.Spec;
import com.example.mqtt.util.JSONHandler;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

public class SpecsService extends Service {

    private static final int ID_SERVICE = 101;
    Timer timer = null;
    Spec spec;
    String channelId;
    NotificationManager notificationManager;
    private IBinder mBinder = new MyBinder();
    final int NOTI_ID = 12512;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (Configuration.TOKEN == null) {
            loadKey();
        }

        spec = makeSpecsRequest();
        if (timer == null) {
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (Configuration.TOKEN != null) {
                        spec = makeSpecsRequest();
                        if (!spec.getSafe().equalsIgnoreCase("safe")) {
                            showWarningNotification();
                        }
                    }
                }
            }, 0, 5000);
        }
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Create the Foreground Service
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel(notificationManager) : "";
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build();

        startForeground(ID_SERVICE, notification);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(NotificationManager notificationManager) {
        String channelId = "my_service_channelid";
        String channelName = "My Foreground Service";
        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
        channel.setImportance(NotificationManager.IMPORTANCE_NONE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        notificationManager.createNotificationChannel(channel);
        return channelId;
    }

    public void showWarningNotification() {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId);
        Notification notification = notificationBuilder
                .setSmallIcon(R.mipmap.ic_launcher)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setContentText("Gas level is beyond normal. Please check!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();

        notificationManager.notify(NOTI_ID, notification);
    }


    @Override
    public void onDestroy() {
        Log.i("serviceI", "Destroyed service");
        timer.cancel();

        Intent intent = new Intent("com.mqtt.ServiceStopped");
        intent.setClass(this, Restarter.class);
        sendBroadcast(intent);

        super.onDestroy();
    }

    public Spec makeSpecsRequest() {
        try {
            URL url = new URL(Configuration.SERVER_IP + Configuration.SPEC_API);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            String auth = String.format("Token %s", Configuration.TOKEN);
            conn.setRequestProperty("Authorization", auth);

            int responseCode = conn.getResponseCode();
            Log.v("TAG", "Sending 'GET' request to URL : " + url.toString());
            Log.v("TAG", "Response Code : " + responseCode);

            BufferedReader in;
            if (responseCode == HttpURLConnection.HTTP_OK) {
                in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }

            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            conn.disconnect();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                return parseResponse(response.toString());
            } else {
                System.out.println(response.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Spec parseResponse(String response) throws Exception {
        System.out.println(response);
        JSONObject jsonResponse = new JSONObject(response);
        return new Spec(
                jsonResponse.getDouble("temperature"),
                jsonResponse.getInt("gas"),
                jsonResponse.getString("status"),
                jsonResponse.getDouble("humidity")
        );
    }

    private void loadKey() {
        SharedPreferences myPrefContainer =
                getSharedPreferences(Configuration.PREFNAME, Activity.MODE_PRIVATE);
        if (myPrefContainer != null) {
            if (myPrefContainer.contains("userToken")) {
                Configuration.TOKEN = myPrefContainer.getString("userToken", null);
            }
        }
    }

    public class MyBinder extends Binder {
        public SpecsService getService() {
            return SpecsService.this;
        }
    }

    public Spec getSpec() {
        return spec;
    }
}
