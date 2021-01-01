package com.example.mqtt.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mqtt.Configuration;
import com.example.mqtt.R;
import com.example.mqtt.service.LoginRepository;

public class LoginActivity extends AppCompatActivity {

    private Button loginButton;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private TextView configBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        loginButton = findViewById(R.id.login_btn);
        usernameEditText = findViewById(R.id.username_edittext);
        passwordEditText = findViewById(R.id.password_edittext);
        configBtn = findViewById(R.id.configBtn);

        configBtn.setOnClickListener(v -> {
            Intent configIntent = new Intent(this, ConfigActivity.class);
            startActivity(configIntent);
        });

        loadConfig();
        if (Configuration.TOKEN != null) {
            startActivity(new Intent(this, RoomListActivity.class));
            this.finish();
        }

        loginButton.setOnClickListener(view -> {
            String email = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            LoginRepository loginRepository = new LoginRepository();
            loginRepository.makeLoginRequest(email, password, key -> {
                if (key == null) {
                    this.runOnUiThread(() -> {
                        Toast.makeText(this, "Log in failed!", Toast.LENGTH_LONG).show();
                    });
                } else {
                    Configuration.TOKEN = key;
                    saveKey();
                    startActivity(new Intent(this, RoomListActivity.class));
                    this.finish();
                }
            });
        });

    }

    private void saveKey() {
        SharedPreferences myPrefContainer = getSharedPreferences(Configuration.PREFNAME,
                Activity.MODE_PRIVATE);
        SharedPreferences.Editor myPrefEditor = myPrefContainer.edit();
        myPrefEditor.putString("userToken", Configuration.TOKEN);
        myPrefEditor.apply();
    }


    private void loadConfig() {
        SharedPreferences myPrefContainer =
                getSharedPreferences(Configuration.PREFNAME, Activity.MODE_PRIVATE);
        if (myPrefContainer != null) {
            if (myPrefContainer.contains("userToken")) {
                Configuration.TOKEN = myPrefContainer.getString("userToken", null);
            }
            if (myPrefContainer.contains("serverIP")) {
                Configuration.SERVER_IP = myPrefContainer.getString("serverIP", "");
            }
            if (myPrefContainer.contains("mqttIP")) {
                Configuration.MQTT_IP = myPrefContainer.getString("mqttIP", "");
            }
        }
    }

}
