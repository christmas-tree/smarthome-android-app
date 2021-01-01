package com.example.mqtt.service;

import android.util.Log;

import com.example.mqtt.Configuration;
import com.example.mqtt.util.ThreadPool;

import java.net.HttpURLConnection;
import java.net.URL;

public class LogoutRepository {

    public LogoutRepository() {
    }

    public void makeLogoutRequest() {
        ThreadPool.executorService.execute(this::makeSynchronousLogoutRequest);
    }

    public void makeSynchronousLogoutRequest() {
        try {
            URL url = new URL(Configuration.SERVER_IP + Configuration.LOGOUT_API);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            String auth = String.format("Token %s", Configuration.TOKEN);
            conn.setRequestProperty("Authorization", auth);

            int responseCode = conn.getResponseCode();
            Log.v("TAG", "Sending 'POST' request to URL : " + url.toString());
            Log.v("TAG", "Response Code : " + responseCode);

            conn.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}