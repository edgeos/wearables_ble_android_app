package com.wearables.ge.wearables_ble_receiver.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException j) {
            return "{}";
        }
    }
}
