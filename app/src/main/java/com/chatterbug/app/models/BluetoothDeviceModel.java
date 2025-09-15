package com.chatterbug.app.models;

import java.util.Objects;

public class BluetoothDeviceModel {
    private String name;
    private String address;
    private boolean isPaired;

    public BluetoothDeviceModel(String name, String address, boolean isPaired) {
        this.name = name;
        this.address = address;
        this.isPaired = isPaired;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isPaired() {
        return isPaired;
    }

    public void setPaired(boolean paired) {
        isPaired = paired;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BluetoothDeviceModel that = (BluetoothDeviceModel) o;
        return Objects.equals(address, that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address);
    }

    @Override
    public String toString() {
        return "BluetoothDeviceModel{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", isPaired=" + isPaired +
                '}';
    }
}
