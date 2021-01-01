package com.example.mqtt.model;

public class Spec {
    private double temperature;
    private int gas;
    private String safe;
    private double humidity;

    public Spec(double temperature, int gas, String safe, double humidity) {
        this.temperature = temperature;
        this.gas = gas;
        this.safe = safe;
        this.humidity = humidity;
    }

    public Spec() {
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getGas() {
        return gas;
    }

    public void setGas(int gas) {
        this.gas = gas;
    }

    public String getSafe() {
        return safe;
    }

    public void setSafe(String safe) {
        this.safe = safe;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }
}
