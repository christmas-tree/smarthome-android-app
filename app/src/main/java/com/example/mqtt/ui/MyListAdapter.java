package com.example.mqtt.ui;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.example.mqtt.R;
import com.example.mqtt.model.Device;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;

public class MyListAdapter extends ArrayAdapter<Device> {

    private final Activity context;
    private final List<Device> devices;

    public MyListAdapter(Activity context, List<Device> devices) {
        super(context, R.layout.device_list, devices);

        this.context = context;
        this.devices = devices;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.device_list, null, true);

        TextView deviceName = rowView.findViewById(R.id.deviceName);
        SwitchMaterial deviceSwitch = rowView.findViewById(R.id.deviceSwitch);

        Device device = devices.get(position);
        deviceName.setText(device.getDescription());
        deviceSwitch.setChecked(device.isOn());

        deviceSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                device.setOn(true);
            } else {
                device.setOn(false);
            }
        });

        return rowView;
    }
}