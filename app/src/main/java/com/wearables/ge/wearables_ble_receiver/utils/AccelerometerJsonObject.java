package com.wearables.ge.wearables_ble_receiver.utils;

import com.github.cliftonlabs.json_simple.JsonObject;

import java.util.Calendar;

public class AccelerometerJsonObject {
    private AccelerometerData accelerometerData;
    private String deviceId;

    public void setAccelerometerData(AccelerometerData accelerometerData) {
        this.accelerometerData = accelerometerData;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public AccelerometerData getAccelerometerData() {
        return accelerometerData;
    }

    public String toJson() {
        JsonObject msg = new JsonObject();

        msg.put("\"timestamp\"", Calendar.getInstance().getTimeInMillis());
        msg.put("\"deviceId\"", "\"" + getDeviceId() + "\"");
        msg.put("\"type\"", "\"accelerometer\"");
        msg.put("\"subtype\"","\"none\"");
        msg.put("\"data\"", this.accelerometerData.toJson());

        return msg.toString().replace("=",":");
    }
}
