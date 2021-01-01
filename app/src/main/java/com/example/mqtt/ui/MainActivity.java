//package com.example.mqtt.ui;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//
//import android.Manifest;
//import android.annotation.SuppressLint;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.os.Bundle;
//import android.speech.RecognitionListener;
//import android.speech.RecognizerIntent;
//import android.speech.SpeechRecognizer;
//import android.util.Log;
//import android.view.MotionEvent;
//import android.view.View;
//import android.widget.EditText;
//import android.widget.ImageView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.example.mqtt.Configuration;
//import com.example.mqtt.HomeComponent;
//import com.example.mqtt.R;
//import com.google.android.material.switchmaterial.SwitchMaterial;
//
//import org.eclipse.paho.android.service.MqttAndroidClient;
//import org.eclipse.paho.client.mqttv3.IMqttActionListener;
//import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
//import org.eclipse.paho.client.mqttv3.IMqttToken;
//import org.eclipse.paho.client.mqttv3.MqttCallback;
//import org.eclipse.paho.client.mqttv3.MqttClient;
//import org.eclipse.paho.client.mqttv3.MqttException;
//import org.eclipse.paho.client.mqttv3.MqttMessage;
//
//import java.io.UnsupportedEncodingException;
//import java.util.ArrayList;
//import java.util.Locale;
//
//public class MainActivity extends AppCompatActivity {
//    private SwitchMaterial relayTglBtn;
//    private SwitchMaterial mainRoomTglBtn;
//    private SwitchMaterial bedroomTglBtn;
//    private EditText speakEditText;
//    private ImageView micButton;
//    private ImageView sendButton;
//    private TextView configTextView;
//    private TextView logOutTextView;
//
//    MqttAndroidClient client;
//    private SpeechRecognizer speechRecognizer;
//    private Intent speechRecognizerIntent;
//    final int PERMISSION_REQUEST_CODE = 1;
//
//
//    private boolean isListening = false;
//
//
//    @SuppressLint("ClickableViewAccessibility")
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        relayTglBtn = findViewById(R.id.relay);
//        mainRoomTglBtn = findViewById(R.id.mainroom);
//        bedroomTglBtn = findViewById(R.id.bedroom);
//        speakEditText = findViewById(R.id.speak_text);
//        micButton = findViewById(R.id.mic_button);
//        sendButton = findViewById(R.id.send_btn);
//        configTextView = findViewById(R.id.configuration_btn);
//        logOutTextView = findViewById(R.id.logout_btn);
//
//        relayTglBtn.setOnCheckedChangeListener((buttonView, isChecked) -> {
//            if(isChecked) {
//                publishMessage(HomeComponent.RELAY,1);
//            } else {
//                publishMessage(HomeComponent.RELAY,0);
//            }
//        });
//
//        mainRoomTglBtn.setOnCheckedChangeListener((buttonView, isChecked) -> {
//            if(isChecked) {
//                publishMessage(HomeComponent.MAIN_ROOM,1);
//            } else {
//                publishMessage(HomeComponent.MAIN_ROOM,0);
//            }
//        });
//
//        bedroomTglBtn.setOnCheckedChangeListener((buttonView, isChecked) -> {
//            if(isChecked) {
//                publishMessage(HomeComponent.BED_ROOM,1);
//            } else {
//                publishMessage(HomeComponent.BED_ROOM,0);
//            }
//        });
//
//        configTextView.setOnClickListener(e -> {
//            startActivity(new Intent(this, ConfigActivity.class));
//        });
//
//        logOutTextView.setOnClickListener(e -> {
//            startActivity(new Intent(this, LoginActivity.class));
//            finish();
//        });
//
//        sendButton.setOnClickListener(e -> {
//            if (!speakEditText.getText().toString().isEmpty()) {
//                Toast.makeText(getApplicationContext(), "Command received", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        initMQTT();
//        initSpeechRecognizer();
//
//        micButton.setOnClickListener((view) -> {
//            if (isListening){
//                Toast.makeText(getApplicationContext(), "Stopped", Toast.LENGTH_SHORT).show();
//                speechRecognizer.stopListening();
//            } else {
//                Toast.makeText(getApplicationContext(), "Started", Toast.LENGTH_SHORT).show();
//                speechRecognizer.startListening(speechRecognizerIntent);
//                micButton.setImageResource(R.drawable.ic_voice_on);
//                isListening = true;
//            }
//        });
//
//    }
//
//    void initMQTT() {
//        //Phần kết nối với broker bắt đầu từ đây
//
//        //Nếu broker yêu cầu mật khẩu
//        //MqttConnectOptions options = new MqttConnectOptions();
//        //options.setUserName("dxhoan");
//        //options.setPassword("dxhoan".toCharArray());
//
//        String clientId = MqttClient.generateClientId();
//
//        //Đặt địa chỉ của broker vào đây
//        client = new MqttAndroidClient(this.getApplicationContext(), Configuration.MQTT_IP, clientId);
//
//        //Đặt Callback để chờ nhận gói tin từ broker
//        client.setCallback(new MqttCallback() {
//            //Trong trường hợp mất kết nối
//            @Override
//            public void connectionLost(Throwable cause) {
//                Log.d("mqtt", "Lost connection: " + cause.toString());
//            }
//
//            //Nhận được gói tin thành công
//            @Override
//            public void messageArrived(String topic, MqttMessage message) throws Exception {
//                Log.d("mqtt", message.toString());
//            }
//
//            @Override
//            public void deliveryComplete(IMqttDeliveryToken token) {
//
//            }
//        });
//
//        try {
//            IMqttToken token = client.connect();
//            //Nếu sử dụng option
//            //IMqttToken token = client.connect(options);
//            token.setActionCallback(new IMqttActionListener() {
//                @Override
//                public void onSuccess(IMqttToken asyncActionToken) {
//                    //Thực hiện code trong trường hợp kết nối lần đầu
//                    Log.d("mqtt", "onSuccess");
////                    publishMessage("hello");
////                    String topic = "foo/bar";
////                    int qos = 1;
////                    try {
////                        IMqttToken subToken = client.subscribe(topic, qos);
////                        subToken.setActionCallback(new IMqttActionListener() {
////                            @Override
////                            public void onSuccess(IMqttToken asyncActionToken) {
////                                Log.d("mqtt", "onSuccess: subscribe");
////                            }
////
////                            @Override
////                            public void onFailure(IMqttToken asyncActionToken,
////                                                  Throwable exception) {
////                                // The subscription could not be performed, maybe the user was not
////                                // authorized to subscribe on the specified topic e.g. using wildcards
////
////                            }
////                        });
////                    } catch (MqttException e) {
////                        e.printStackTrace();
////                    }
//                }
//
//                @Override
//                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
//                    Log.d("mqtt", "onFailure");
//
//                }
//            });
//        } catch (MqttException e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    void initSpeechRecognizer() {
//        //Kiểm tra quyền truy cập vào record
//        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_CODE);
//        }
//
//        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
//        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
//        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
//
//        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
//        speechRecognizer.setRecognitionListener(new RecognitionListener() {
//            @Override
//            public void onReadyForSpeech(Bundle bundle) {
//            }
//
//            @Override
//            public void onBeginningOfSpeech() {
//                speakEditText.setText("");
//                speakEditText.setHint("Listening...");
//            }
//
//            @Override
//            public void onRmsChanged(float v) {
//            }
//
//            @Override
//            public void onBufferReceived(byte[] bytes) {
//            }
//
//            @Override
//            public void onEndOfSpeech() {
//                speakEditText.setHint("Thinking...");
//                stopListening();
//            }
//
//            @Override
//            public void onError(int i) {
//                Log.e("speech", "Error recognizing");
//                stopListening();
//            }
//
//            @Override
//            public void onResults(Bundle bundle) {
//                stopListening();
//                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
//                speakEditText.setText(data.get(0));
//            }
//
//            @Override
//            public void onPartialResults(Bundle bundle) {
//                stopListening();
//                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
//                speakEditText.setText(data.get(0));
//            }
//
//            @Override
//            public void onEvent(int i, Bundle bundle) {
//            }
//
//
//        });
//    }
//
//    public void stopListening() {
//        isListening = false;
//        micButton.setImageResource(R.drawable.ic_voice);
//        speakEditText.setHint("Enter command");
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
//                                           @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == PERMISSION_REQUEST_CODE) {
//            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
//                micButton.setVisibility(View.INVISIBLE);
//            }
//        }
//    }
//
//    //Hàm gửi gói tin cho broker
//    void publishMessage(HomeComponent component, int status) {
//        String topic = Configuration.MQTT_COMPONENT_TOPIC;
//        String payload = "{\"id\":" + String.valueOf(component) + ",\"vol\":" + String.valueOf(status) + "}";
//        byte[] encodedPayload;
//        try {
//            encodedPayload = payload.getBytes("UTF-8");
//            MqttMessage message = new MqttMessage(encodedPayload);
//            client.publish(topic, message);
//        } catch (UnsupportedEncodingException | MqttException e) {
//            e.printStackTrace();
//        }
//    }
//
//}