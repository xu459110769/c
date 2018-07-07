package com.example.control3.bluetooth;

/**
 * Created by Saber on 2018/3/19.
 */

public class BluetoothInformation {
    private String name;
    private String address;

    public BluetoothInformation(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }
}
