package com.example.mqtt.service;

public interface RepositoryCallback<T> {
    void onComplete(T result);
}
