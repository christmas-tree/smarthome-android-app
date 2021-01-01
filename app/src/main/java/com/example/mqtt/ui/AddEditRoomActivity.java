package com.example.mqtt.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mqtt.R;
import com.example.mqtt.model.Room;
import com.example.mqtt.util.MyDatabaseHelper;

public class AddEditRoomActivity extends AppCompatActivity {

    private static final int MODE_CREATE = 1;
    private static final int MODE_EDIT = 2;

    private EditText textTitle;
    private EditText textContent;
    private Button buttonSave;
    private ImageView backBtn;


    private Room room;
    private boolean needRefresh;
    private int mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_room);

        this.textTitle = this.findViewById(R.id.newRoomIdEditText);
        this.textContent = this.findViewById(R.id.newRoomNameEditText);

        this.buttonSave = findViewById(R.id.newRoomSaveBtn);
        this.backBtn = findViewById(R.id.backBtn);

        this.buttonSave.setOnClickListener(v -> buttonSaveClicked());

        this.backBtn.setOnClickListener(v -> this.onBackPressed());

        Intent intent = this.getIntent();
        this.room = (Room) intent.getSerializableExtra("room");
        if(room== null)  {
            this.mode = MODE_CREATE;
        } else  {
            this.mode = MODE_EDIT;
            this.textTitle.setText(Integer.toString(room.getRoomId()));
            this.textContent.setText(room.getDescription());
        }
    }

    // User Click on the Save button.
    public void buttonSaveClicked()  {
        MyDatabaseHelper db = new MyDatabaseHelper(this);

        String title = this.textTitle.getText().toString();
        String content = this.textContent.getText().toString();

        if(title.equals("") || content.equals("")) {
            Toast.makeText(getApplicationContext(),
                    "Please enter title & content", Toast.LENGTH_LONG).show();
            return;
        }

        if(mode == MODE_CREATE ) {
            this.room= new Room( Integer.parseInt(title),content);
            db.addRoom(room);
        } else  {
            this.room.setDescription(content);
            db.updateRoom(room);
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