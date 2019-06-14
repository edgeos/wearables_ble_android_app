package com.wearables.ge.wearables_ble_receiver.utils;

import com.github.cliftonlabs.json_simple.JsonObject;

import java.util.Calendar;

public class TempHumidPressureJsonObject {
    private TempHumidPressure tempHumidPressureData;
    private String deviceId;
    private String userId;

    public void setTempHumidPressureData(TempHumidPressure tempHumidPressureData) {
        this.tempHumidPressureData = tempHumidPressureData;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public TempHumidPressure getTempHumidPressureData() {
        return tempHumidPressureData;
    }

    public String toJson() {
        JsonObject msg = new JsonObject();

        msg.put("\"timestamp\"", Calendar.getInstance().getTimeInMillis());
        msg.put("\"deviceId\"", "\"" + getDeviceId() + "\"");
        msg.put("\"userId\"", "\"" + getUserId() + "\"");
        msg.put("\"type\"", "\"temphumidpressure\"");
        msg.put("\"subtype\"","\"none\"");
        msg.put("\"data\"", this.tempHumidPressureData.toJson());

        return msg.toString().replace("=",":");
    }
}
