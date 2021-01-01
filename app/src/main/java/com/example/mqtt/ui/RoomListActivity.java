package com.example.mqtt.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mqtt.Configuration;
import com.example.mqtt.R;
import com.example.mqtt.model.Room;
import com.example.mqtt.service.LoginRepository;
import com.example.mqtt.service.LogoutRepository;
import com.example.mqtt.service.Restarter;
import com.example.mqtt.service.SpecsService;
import com.example.mqtt.util.MyDatabaseHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Menu danh sách các phòng
 */

public class RoomListActivity extends AppCompatActivity {
    private ListView roomListView;
    private Button addRoomBtn;
    private TextView logoutBtn;

    private static final int MENU_ITEM_VIEW = 111;
    private static final int MENU_ITEM_EDIT = 222;
    private static final int MENU_ITEM_CREATE = 333;
    private static final int MENU_ITEM_DELETE = 444;

    private static final int MY_REQUEST_CODE = 1000;

    private List<Room> roomList;
    private ArrayAdapter<Room> listViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_list);

        this.addRoomBtn = findViewById(R.id.addRoomBtn);
        this.addRoomBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditRoomActivity.class);
            startActivityForResult(intent, MY_REQUEST_CODE);
        });
        this.logoutBtn = findViewById(R.id.logout_btn);

        // Get ListView object from xml
        this.roomListView = (ListView) findViewById(R.id.roomListView);
        Intent intent = this.getIntent();

        MyDatabaseHelper db = new MyDatabaseHelper(this);
        db.createDefaultRoomIfNeed();

        this.roomList = new ArrayList<>(db.getAllRooms());

        // Define a new Adapter
        // 1 - Context
        // 2 - Layout for the row
        // 3 - ID of the TextView to which the data is written
        // 4 - the List of data

        this.listViewAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, this.roomList);

        // Assign adapter to ListView
        this.roomListView.setAdapter(this.listViewAdapter);

        this.roomListView.setOnItemClickListener((parent, view, position, id) -> {
            Room selectedRoom = (Room) parent.getItemAtPosition(position);
            Intent deviceListIntent = new Intent(this, DeviceListActivity.class);
            deviceListIntent.putExtra("room", selectedRoom);

            startActivityForResult(deviceListIntent, MY_REQUEST_CODE);
        });

        this.logoutBtn.setOnClickListener(v -> {
            LogoutRepository logoutRepository = new LogoutRepository();
            logoutRepository.makeLogoutRequest();
            Configuration.TOKEN = null;
            SharedPreferences myPrefContainer = getSharedPreferences(Configuration.PREFNAME,
                    Activity.MODE_PRIVATE);
            SharedPreferences.Editor myPrefEditor = myPrefContainer.edit();
            myPrefEditor.remove("userToken");
            myPrefEditor.apply();

            Intent logInIntent = new Intent(this, LoginActivity.class);
            startActivity(logInIntent);
            RoomListActivity.this.finish();
        });

        // Register the ListView for Context menu
        registerForContextMenu(this.roomListView);

        Intent serviceIntent = new Intent(this, SpecsService.class);
        startService(serviceIntent);
    }

    @Override
    protected void onDestroy() {
        //stopService(mServiceIntent);
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("com.mqtt.ServiceStopped");
        broadcastIntent.setClass(this, Restarter.class);
        this.sendBroadcast(broadcastIntent);
        super.onDestroy();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view,
                                    ContextMenu.ContextMenuInfo menuInfo) {

        super.onCreateContextMenu(menu, view, menuInfo);
        menu.setHeaderTitle("Select The Action");

        // groupId, itemId, order, title
        menu.add(0, MENU_ITEM_VIEW, 0, "View Room");
        menu.add(0, MENU_ITEM_EDIT, 1, "Edit Room");
        menu.add(0, MENU_ITEM_DELETE, 2, "Delete Room");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo
                info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        final Room selectedRoom = (Room) this.roomListView.getItemAtPosition(info.position);

        if (item.getItemId() == MENU_ITEM_VIEW) {
            Intent intent = new Intent(this, DeviceListActivity.class);
            intent.putExtra("room", selectedRoom);
            // Start AddEditNoteActivity, (with feedback).
            this.startActivityForResult(intent, MY_REQUEST_CODE);

        } else if (item.getItemId() == MENU_ITEM_EDIT) {
            Intent intent = new Intent(this, AddEditRoomActivity.class);
            intent.putExtra("room", selectedRoom);

            // Start AddEditNoteActivity, (with feedback).
            this.startActivityForResult(intent, MY_REQUEST_CODE);

        } else if (item.getItemId() == MENU_ITEM_DELETE) {
            // Ask before deleting.
            new AlertDialog.Builder(this)
                    .setMessage(selectedRoom.getRoomId() + ":" + selectedRoom.getDescription() + ". Are you sure you want to delete?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", (dialog, id) -> deleteRoom(selectedRoom))
                    .setNegativeButton("No", null)
                    .show();

        } else {
            return false;
        }
        return true;
    }

    // Delete a record
    private void deleteRoom(Room room) {
        MyDatabaseHelper db = new MyDatabaseHelper(this);
        db.deleteRoom(room);
        this.roomList.remove(room);
        // Refresh ListView.
        this.listViewAdapter.notifyDataSetChanged();
    }

    // When AddEditRoomActivity completed, it sends feedback.
    // (If you start it using startActivityForResult ())

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == MY_REQUEST_CODE) {
            boolean needRefresh = data.getBooleanExtra("needRefresh", true);
            // Refresh ListView
            if (needRefresh) {
                this.roomList.clear();
                MyDatabaseHelper db = new MyDatabaseHelper(this);
                List<Room> list = db.getAllRooms();
                this.roomList.addAll(list);

                // Notify the data change (To refresh the ListView).
                this.listViewAdapter.notifyDataSetChanged();
            }
        }
    }
}