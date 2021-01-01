package com.example.mqtt.model;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Room implements Serializable {
    private int roomId;
    private String description;

    public Room() {
    }

    public Room( String description) {
        this.description = description;
    }

    public Room(int roomId, String description) {
        this.roomId = roomId;
        this.description = description;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @NonNull
    @Override
    public String toString() {
        return this.description;
    }
}
