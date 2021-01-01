package com.example.mqtt.model;

import androidx.annotation.NonNull;

import com.example.mqtt.service.PublishRepository;

import java.io.Serializable;

public class Device implements Serializable {
    private int deviceId;
    private Room room;
    private String description;
    private boolean on;

    public Device() {
    }

    public Device(int deviceId, Room room, String description) {
        this.deviceId = deviceId;
        this.room = room;
        this.description = description;
        this.on = false;
    }

    public Device(Room room, String description) {
        this.room = room;
        this.description = description;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
        PublishRepository publishRepository = new PublishRepository();
        publishRepository.makePublishRequest(this.deviceId, this.on ? 1 : 0);
    }

    @NonNull
    @Override
    public String toString() {
        return this.description;
    }
}
