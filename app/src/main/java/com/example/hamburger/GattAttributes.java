package com.example.hamburger;

import java.util.HashMap;

public class GattAttributes {
    private static HashMap<String, String> attributes = new HashMap<String, String>();
    public static String ACCTIMER = "6E400003-B5A3-F393-E0A9-E50E24DCCA9E";

    public final static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    static {
        // Sample Services.
        attributes.put("00002901-0000-1000-8000-00805f9b34fb", "Accelerometer Service");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        // Sample Characteristics.
        attributes.put(ACCTIMER, "Timer");
        attributes.put("00002902-0000-1000-8000-00805f9b34fb", "Timer1");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
