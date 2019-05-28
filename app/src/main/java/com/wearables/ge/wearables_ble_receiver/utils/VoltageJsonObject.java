package com.wearables.ge.wearables_ble_receiver.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

@JsonIgnoreProperties(value = { "full_message_ms", "abbreviated_message_ms", "last_abbreviated_message_timestamp", "last_message_timestamp" })
public class VoltageJsonObject {
    private VoltageAlarmStateChar voltageAlarmData;
    private String deviceId;

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

    public VoltageAlarmStateChar getVoltageAlarmData() {
        return voltageAlarmData;
    }

    public String toJson(Boolean force_full_message) {
        SimpleBeanPropertyFilter theFilter;
        if (this.abbreviate_message && !force_full_message){
            theFilter = SimpleBeanPropertyFilter.serializeAllExcept("ch1_fft_results", "ch2_fft_results", "ch3_fft_results");
        } else {
            theFilter = SimpleBeanPropertyFilter.serializeAllExcept("ch1_50HZ", "ch1_60HZ","ch2_50HZ", "ch2_60HZ","ch3_50HZ", "ch3_60HZ");
        }
        FilterProvider filters = new SimpleFilterProvider()
                .addFilter("myFilter", theFilter);

        final ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        try {
            return mapper.writer(filters).writeValueAsString(this);
        } catch (JsonProcessingException j) {
            return "{}";
        }
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
