package com.wearables.ge.wearables_ble_receiver.utils;

import android.location.Location;

import com.wearables.ge.wearables_ble_receiver.services.LocationService;

import java.util.Calendar;

public class VoltageEvent {

    int voltage;
    Long time;
    Location location;
    Long duration;

    public VoltageEvent(int voltage, Long duration){
        this.voltage = voltage;
        this.time = Calendar.getInstance().getTimeInMillis();
        if(LocationService.locations.isEmpty()){
            this.location = null;
        } else {
            this.location = LocationService.locations.get(LocationService.locations.size() - 1);
        }
        this.duration = duration;
    }

    public int getVoltage() {
        return voltage;
    }

    public void setVoltage(int voltage) {
        this.voltage = voltage;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }
}
