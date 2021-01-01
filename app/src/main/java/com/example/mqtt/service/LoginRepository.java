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

public class LoginRepository {

    public LoginRepository() {
    }

    public void makeLoginRequest(String email, String password, RepositoryCallback<String> callback) {
        ThreadPool.executorService.execute(() -> {
            String key = makeSynchronousLoginRequest(email, password);
            callback.onComplete(key);
        });
    }

    public String makeSynchronousLoginRequest(String email, String password) {
        try {
            URL url = new URL(Configuration.SERVER_IP + Configuration.LOGIN_API);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            String params = "email=" + email + "&password=" + password;

            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(params);
            wr.flush();
            wr.close();

            int responseCode = conn.getResponseCode();
            Log.v("TAG", "Sending 'POST' request to URL : " + url.toString());
            Log.v("TAG", "Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            conn.disconnect();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                return parseResponse(response.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String parseResponse(String response) throws Exception {
        JSONObject jsonResponse = new JSONObject(response);
        JSONHandler handler = new JSONHandler(jsonResponse);
        String key = handler.getString("key", null);
        return key;
    }
}