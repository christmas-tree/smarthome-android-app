package com.example.mqtt.service;

import android.util.Log;

import com.example.mqtt.Configuration;
import com.example.mqtt.util.JSONHandler;
import com.example.mqtt.util.ThreadPool;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class PublishRepository {

    public PublishRepository() {
    }

    public void makePublishRequest(int id, int voltage) {
        ThreadPool.executorService.execute(() -> {
            makeSynchronousLoginRequest(id, voltage);
        });
    }

    public void makeSynchronousLoginRequest(int id, int voltage) {
        try {
            URL url = new URL(Configuration.SERVER_IP + Configuration.PUBLISH_API);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            String auth = String.format("Token %s", Configuration.TOKEN);
            conn.setRequestProperty("Authorization", auth);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JSONObject json = new JSONObject();
            json.put("home", 1);
            json.put("room", 1);
            json.put("device", id);
            json.put("vol", voltage);

            String params = json.toString();

//            String params = "home=1&room=1&device=" + id + "&vol=" + voltage;

            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(params);
            wr.flush();
            wr.close();

            int responseCode = conn.getResponseCode();
            Log.v("TAG", "Sending 'POST' request to URL : " + url.toString());
            Log.v("TAG", "Response Code : " + responseCode);
            Log.d("STR", params);

            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}