package com.wearables.ge.wearables_ble_receiver.utils;

import com.github.cliftonlabs.json_simple.JsonObject;

import java.util.Calendar;


public class VoltageJsonObject {
    private VoltageAlarmStateChar voltageAlarmData;
    private String deviceId;
    private String userId;

    private long full_message_ms;
    private long abbreviated_message_ms;
    private long last_abbreviated_message_timestamp;
    private long last_message_timestamp;
    private Boolean abbreviate_message;


    public void setVoltageAlarmData(VoltageAlarmStateChar voltageAlarmData) {
        this.voltageAlarmData = voltageAlarmData;
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

    public VoltageAlarmStateChar getVoltageAlarmData() {
        return voltageAlarmData;
    }

    public String toJson(Boolean full_message){
        JsonObject msg = new JsonObject();

        msg.put("\"timestamp\"", Calendar.getInstance().getTimeInMillis());
        msg.put("\"deviceId\"", "\"" + getDeviceId() + "\"");
        msg.put("\"userId\"", "\"" + getUserId() + "\"");
        msg.put("\"type\"", "\"voltage\"");
        msg.put("\"subtype\"", (full_message || !this.abbreviate_message) ? "\"full_message\"" : "\"abbreviated_message\"");
        msg.put("\"data\"", this.voltageAlarmData.toJson(full_message || !this.abbreviate_message));

        return msg.toString().replace("=", ":");
    }

    public VoltageJsonObject(long abbreviated_message_ms, long full_message_ms){
        this.abbreviated_message_ms = abbreviated_message_ms;
        this.full_message_ms = full_message_ms;
        this.last_abbreviated_message_timestamp = 0;
        this.last_message_timestamp = 0;
    }

    public Boolean timerCheck(){
        Boolean encode_message;
        long current_time = System.currentTimeMillis();
        long time_since_last_full_message = current_time - last_message_timestamp;
        long time_since_last_abbreviated_message = current_time - last_abbreviated_message_timestamp;
        if (time_since_last_full_message > full_message_ms && time_since_last_abbreviated_message > abbreviated_message_ms) {
            encode_message = Boolean.TRUE;
            this.abbreviate_message = Boolean.FALSE;
            last_message_timestamp = current_time;
            last_abbreviated_message_timestamp = current_time;
        }else if(time_since_last_full_message > full_message_ms){
            encode_message = Boolean.TRUE;
            this.abbreviate_message = Boolean.FALSE;
            last_message_timestamp = current_time;
        } else if (time_since_last_abbreviated_message > abbreviated_message_ms){
            encode_message = Boolean.TRUE;
            this.abbreviate_message = Boolean.TRUE;
            last_abbreviated_message_timestamp = current_time;
        } else {
            encode_message = Boolean.FALSE;
        }
        return encode_message;
    }
}
