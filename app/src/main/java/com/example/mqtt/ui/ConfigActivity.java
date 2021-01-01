package com.example.mqtt.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mqtt.Configuration;
import com.example.mqtt.R;

public class ConfigActivity extends AppCompatActivity {

    private EditText serverURLEditText;
    private EditText mqttURLEditText;
    private Button saveButton;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        serverURLEditText = findViewById(R.id.server_ip_edittext);
        mqttURLEditText = findViewById(R.id.mqtt_ip_edittext);
        saveButton = findViewById(R.id.save_btn);
        backButton = findViewById(R.id.config_back_btn);

        saveButton.setOnClickListener(v -> {
            Configuration.SERVER_IP = serverURLEditText.getText().toString();
            Configuration.MQTT_IP = mqttURLEditText.getText().toString();
        });

        backButton.setOnClickListener(v -> {
            this.finish();
        });
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onStart() {
        super.onStart();
        loadConfigData();
        serverURLEditText.setText(Configuration.SERVER_IP);
        mqttURLEditText.setText(Configuration.MQTT_IP);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveConfigData();
    }

    private void saveConfigData() {
        SharedPreferences myPrefContainer = getSharedPreferences(Configuration.PREFNAME,
                        Activity.MODE_PRIVATE);
        SharedPreferences.Editor myPrefEditor = myPrefContainer.edit();
        myPrefEditor.putString("serverIP", Configuration.SERVER_IP);
        myPrefEditor.putString("mqttIP", Configuration.MQTT_IP);
        myPrefEditor.apply();
    }

    private void loadConfigData() {
        SharedPreferences myPrefContainer =
                getSharedPreferences(Configuration.PREFNAME, Activity.MODE_PRIVATE);
        if (myPrefContainer != null) {
            if (myPrefContainer.contains("serverIP")) {
                Configuration.SERVER_IP = myPrefContainer.getString("serverIP", "");
            }
            if (myPrefContainer.contains("mqttIP")) {
                Configuration.MQTT_IP = myPrefContainer.getString("mqttIP", "");
            }
        }
    }
}
