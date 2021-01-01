package com.example.mqtt.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mqtt.R;
import com.example.mqtt.model.Device;
import com.example.mqtt.model.Room;
import com.example.mqtt.util.MyDatabaseHelper;

public class AddEditDeviceActivity extends AppCompatActivity {

    private static final String TAG = "AddDevice";
    private static final int MODE_CREATE = 1;
    private static final int MODE_EDIT = 2;

    private EditText newDeviceIdEditText;
    private EditText newDeviceNameEditText;
    private Button buttonSave;
    private ImageView backBtn;

    private Room room;
    private Device device;
    private boolean needRefresh;
    private int mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_device);

        this.newDeviceIdEditText = findViewById(R.id.newDeviceIdEditText);
        this.newDeviceNameEditText = findViewById(R.id.newDeviceNameEditText);
        this.buttonSave = findViewById(R.id.newDeviceSaveBtn);
        this.backBtn = findViewById(R.id.backBtn);

        this.buttonSave.setOnClickListener(v -> buttonSaveClicked());
        this.backBtn.setOnClickListener(v -> this.onBackPressed());

        Intent intent = this.getIntent();
        this.device = (Device) intent.getSerializableExtra("device");

        if (device == null) {
            this.room = (Room) intent.getSerializableExtra("room");
            this.mode = MODE_CREATE;
        } else {
            Log.i(TAG, "onCreate ... ");
            this.mode = MODE_EDIT;
            this.newDeviceIdEditText.setText(Integer.toString(device.getDeviceId()));
            this.newDeviceNameEditText.setText(device.getDescription());
        }
    }

    // User Click on the Save button.
    public void buttonSaveClicked() {
        MyDatabaseHelper db = new MyDatabaseHelper(this);

        String id = this.newDeviceIdEditText.getText().toString();
        String name = this.newDeviceNameEditText.getText().toString();

        if (id.equals("") || name.equals("")) {
            Toast.makeText(getApplicationContext(),
                    "Please enter ID & name", Toast.LENGTH_LONG).show();
            return;
        }

        if (mode == MODE_CREATE) {
            this.device = new Device(Integer.parseInt(id), this.room, name);
            db.addDevice(this.device);
        } else {
            this.device.setDescription(name);
            db.updateDevice(this.device);
        }

        this.needRefresh = true;

        // Back to MainActivity.
        this.onBackPressed();
    }

    // When completed this Activity,
    // Send feedback to the Activity called it.
    @Override
    public void finish() {

        // Create Intent
        Intent data = new Intent();

        // Request MainActivity refresh its ListView (or not).
        data.putExtra("needRefresh", needRefresh);

        // Set Result
        this.setResult(Activity.RESULT_OK, data);
        super.finish();
    }

}