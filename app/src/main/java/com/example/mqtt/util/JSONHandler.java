package com.example.mqtt.util;

import org.json.JSONObject;

public class JSONHandler {

    JSONObject jsonObject;

    public JSONHandler(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    public String getString(String key, String defValue) {
        try {
            return jsonObject.getString(key);
        }
        catch (Exception ex) {
            return defValue;
        }
    }

    public int getInt(String key, int defValue) {
        try {
            return jsonObject.getInt(key);
        }
        catch (Exception ex) {
            return defValue;
        }
    }
}
