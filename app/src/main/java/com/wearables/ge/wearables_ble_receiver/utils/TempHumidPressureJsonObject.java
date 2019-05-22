package com.wearables.ge.wearables_ble_receiver.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TempHumidPressureJsonObject {
//    private VoltageAlarmStateChar voltageAlarmData;
//    private AccelerometerData accelerometerData;
    private TempHumidPressure tempHumidPressureData;
    private String deviceId;

//    public void setVoltageAlarmData(VoltageAlarmStateChar voltageAlarmData) {
//        this.voltageAlarmData = voltageAlarmData;
//    }

//    public void setAccelerometerData(AccelerometerData accelerometerData) {
//        this.accelerometerData = accelerometerData;
//    }

    public void setTempHumidPressureData(TempHumidPressure tempHumidPressureData) {
        this.tempHumidPressureData = tempHumidPressureData;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

//    public VoltageAlarmStateChar getVoltageAlarmData() {
//        return voltageAlarmData;
//    }

//    public AccelerometerData getAccelerometerData() {
//        return accelerometerData;
//    }

    public TempHumidPressure getTempHumidPressureData() {
        return tempHumidPressureData;
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
