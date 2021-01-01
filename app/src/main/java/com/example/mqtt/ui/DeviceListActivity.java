package com.example.mqtt.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.mqtt.R;
import com.example.mqtt.model.Device;
import com.example.mqtt.model.Room;
import com.example.mqtt.model.Spec;
import com.example.mqtt.service.PublishRepository;
import com.example.mqtt.service.SpecsService;
import com.example.mqtt.util.MyDatabaseHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Danh sách các thiết bị trong phòng
 */

public class DeviceListActivity extends AppCompatActivity {

    private ListView deviceListView;
    private Button addDeviceBtn;
    private Room room;
    private TextView roomTitle;
    private ImageView backBtn;
    private EditText speakEditText;
    private ImageView micButton;
    private ImageView sendButton;
    private TextView temperatureText;
    private TextView humidText;
    private TextView gasText;
    private TextView statusText;
    private ImageView statusBg;
    private ImageView gasBg;


    private Timer timer;

    SpecsService specsService;
    boolean serviceBound = false;

    private static final int MENU_ITEM_EDIT = 222;
    private static final int MENU_ITEM_DELETE = 444;

    private static final int MY_REQUEST_CODE = 1001;
    private static final int MY_REQUEST_PLAYING_CODE = 1002;

    private final List<Device> deviceList = new ArrayList<>();
    private ArrayAdapter<Device> listViewAdapter;

    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    final int PERMISSION_REQUEST_CODE = 1;
    private boolean isListening = false;

    PublishRepository publishRepository = new PublishRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        this.roomTitle = findViewById(R.id.deviceListTitle);
        this.addDeviceBtn = findViewById(R.id.addDeviceBtn);
        this.backBtn = findViewById(R.id.backBtn);
        this.speakEditText = findViewById(R.id.speak_text);
        this.micButton = findViewById(R.id.mic_button);
        this.sendButton = findViewById(R.id.send_btn);
        this.deviceListView = findViewById(R.id.deviceListView);

        this.temperatureText = findViewById(R.id.temperatureText);
        this.gasText = findViewById(R.id.gasText);
        this.humidText = findViewById(R.id.humidText);
        this.statusText = findViewById(R.id.statusText);

        this.gasBg = findViewById(R.id.gasBg);
        this.statusBg = findViewById(R.id.statusBg);

        // Add device listener and back btn
        this.addDeviceBtn.setOnClickListener(v -> addDeviceForRoom());
        this.backBtn.setOnClickListener(v -> this.onBackPressed());

        MyDatabaseHelper db = new MyDatabaseHelper(this);

        // Get room info from intent and init devices
        Intent intent = this.getIntent();
        this.room = (Room) intent.getSerializableExtra("room");
        this.roomTitle.setText(this.room.getDescription());
        this.deviceList.addAll(db.getAllDevByRoom(this.room));

        // Define a new Adapter
        this.listViewAdapter = new MyListAdapter(this, deviceList);
        this.deviceListView.setAdapter(listViewAdapter);
        this.deviceListView.setOnItemClickListener((parent, view, position, id) -> {
            Device clicked = (Device) parent.getItemAtPosition(position);
            clicked.setOn(!clicked.isOn());
            this.listViewAdapter.notifyDataSetChanged();
        });

        // Register the ListView for Context menu
        registerForContextMenu(this.deviceListView);

        // Speech recognizer init
        sendButton.setOnClickListener(e -> {
            if (!speakEditText.getText().toString().isEmpty()) {
                analyzeSpeech(speakEditText.getText().toString());
            }
        });

        initSpeechRecognizer();

        micButton.setOnClickListener((view) -> {
            if (isListening) {
                Toast.makeText(getApplicationContext(), "Stopped", Toast.LENGTH_SHORT).show();
                speechRecognizer.stopListening();
            } else {
                Toast.makeText(getApplicationContext(), "Started", Toast.LENGTH_SHORT).show();
                speechRecognizer.startListening(speechRecognizerIntent);
                micButton.setImageResource(R.drawable.ic_voice_on);
                isListening = true;
            }
        });

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (serviceBound) {
                    Spec spec = specsService.getSpec();
                    if (spec != null) {
                        runOnUiThread(() -> updateUI(spec));
                    }
                }
            }
        }, 0, 5000);
    }

    private void updateUI(Spec spec) {
        temperatureText.setText(String.valueOf(spec.getTemperature() + "°C"));
        humidText.setText(String.valueOf(spec.getHumidity()));
        gasText.setText(String.valueOf(spec.getGas()));
        statusText.setText(spec.getSafe());

        if (spec.getSafe().toLowerCase().equals("safe")) {
            gasBg.setImageResource(R.drawable.gradient_1);
            statusBg.setImageResource(R.drawable.gradient_blue);
        } else {
            gasBg.setImageResource(R.drawable.gradient_warning);
            statusBg.setImageResource(R.drawable.gradient_warning);
        }
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SpecsService.MyBinder myBinder = (SpecsService.MyBinder) service;
            specsService = myBinder.getService();
            serviceBound = true;

            Spec spec = specsService.getSpec();
            if (spec != null) {
                updateUI(spec);
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, SpecsService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    protected void onStop() {
        super.onStop();
        if (serviceBound) {
            unbindService(mServiceConnection);
            serviceBound = false;
        }
    }

    public void addDeviceForRoom() {
        Intent intent = new Intent(this, AddEditDeviceActivity.class);
        intent.putExtra("room", this.room);
        // Start AddEditNoteActivity, (with feedback).
        this.startActivityForResult(intent, MY_REQUEST_CODE);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view,
                                    ContextMenu.ContextMenuInfo menuInfo) {

        super.onCreateContextMenu(menu, view, menuInfo);
        menu.setHeaderTitle("Select The Action");

        // groupId, itemId, order, title
        menu.add(0, MENU_ITEM_EDIT, 0, "Edit Device");
        menu.add(0, MENU_ITEM_DELETE, 1, "Delete Device");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo
                info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        final Device selectedDevice = (Device) this.deviceListView.getItemAtPosition(info.position);

        if (item.getItemId() == MENU_ITEM_EDIT) {
            Intent intent = new Intent(this, AddEditDeviceActivity.class);
            intent.putExtra("device", selectedDevice);
            this.startActivityForResult(intent, MY_REQUEST_CODE);

        } else if (item.getItemId() == MENU_ITEM_DELETE) {
            // Ask before deleting.
            new AlertDialog.Builder(this)
                    .setMessage("Are you sure you want to delete " + selectedDevice.getDescription() + "?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", (dialog, id) -> deleteDevice(selectedDevice))
                    .setNegativeButton("No", null)
                    .show();
        } else {
            return false;
        }
        return true;
    }

    // Delete a record
    private void deleteDevice(Device device) {
        MyDatabaseHelper db = new MyDatabaseHelper(this);
        db.deleteDevice(device);
        this.deviceList.remove(device);
        // Refresh ListView.
        this.listViewAdapter.notifyDataSetChanged();
    }

    // When AddEditNoteActivity completed, it sends feedback.
    // (If you start it using startActivityForResult ())
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == MY_REQUEST_CODE) {
            boolean needRefresh = data.getBooleanExtra("needRefresh", true);
            // Refresh ListView
            if (needRefresh) {
                this.deviceList.clear();
                MyDatabaseHelper db = new MyDatabaseHelper(this);
                List<Device> list = db.getAllDevByRoom(this.room);
                this.deviceList.addAll(list);
                // Notify the data change (To refresh the ListView).
                this.listViewAdapter.notifyDataSetChanged();
            }
        }
    }

    void initSpeechRecognizer() {
        //Kiểm tra quyền truy cập vào record
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_CODE);
        }

        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {
            }

            @Override
            public void onBeginningOfSpeech() {
                speakEditText.setText("");
                speakEditText.setHint("Listening...");
            }

            @Override
            public void onRmsChanged(float v) {
            }

            @Override
            public void onBufferReceived(byte[] bytes) {
            }

            @Override
            public void onEndOfSpeech() {
                speakEditText.setHint("Thinking...");
                stopListening();
            }

            @Override
            public void onError(int i) {
                Log.e("speech", "Error recognizing");
                stopListening();
            }

            @Override
            public void onResults(Bundle bundle) {
                stopListening();
                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                speakEditText.setText(data.get(0));
                analyzeSpeech(data.get(0));
            }

            @Override
            public void onPartialResults(Bundle bundle) {
                stopListening();
                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                speakEditText.setText(data.get(0));
            }

            @Override
            public void onEvent(int i, Bundle bundle) {
            }


        });
    }

    public void analyzeSpeech(String input) {
        String noSpace = input.replaceAll(" ", "").toLowerCase();
        Device recognizedDevice = null;
        for (Device device : deviceList) {
            if (noSpace.contains(device.getDescription().replaceAll(" ", "").toLowerCase())) {
                recognizedDevice = device;
                break;
            }
        }

        if (recognizedDevice != null) {
            if (noSpace.contains("on")) {
                recognizedDevice.setOn(true);
            } else if (noSpace.contains("off")) {
                recognizedDevice.setOn(false);
            }
            this.listViewAdapter.notifyDataSetChanged();
            this.speakEditText.getText().clear();
        }
    }

    public void stopListening() {
        isListening = false;
        micButton.setImageResource(R.drawable.ic_voice);
        speakEditText.setHint("Enter command");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                micButton.setVisibility(View.INVISIBLE);
            }
        }
    }
}