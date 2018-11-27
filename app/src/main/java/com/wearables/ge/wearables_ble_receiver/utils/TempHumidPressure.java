package com.wearables.ge.wearables_ble_receiver.utils;

import android.util.Log;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class TempHumidPressure {
    public String TAG = "TempHumidPressure";

    public int temp;
    public int humid;
    public int pres;

    public Long date;

    public TempHumidPressure(String hexString){
        List<String> hexSplit = Arrays.asList(hexString.split("\\s+"));
        if(hexSplit.size() == 6){
            String tempString = hexSplit.get(1) + hexSplit.get(0);
            String humidString = hexSplit.get(3) + hexSplit.get(2);
            String pressureString = hexSplit.get(5) + hexSplit.get(4);
            this.temp = Integer.parseInt(tempString, 16);
            this.humid = Integer.parseInt(humidString, 16);
            this.pres = Integer.parseInt(pressureString, 16);
            this.date = Calendar.getInstance().getTimeInMillis();
        } else {
            Log.d(TAG, "Temp/Pressure/Humid hex string malformed: " + hexString);
        }
    }

    public int getTemp() {
        return temp;
    }

    public void setTemp(int temp) {
        this.temp = temp;
    }

    public int getHumid() {
        return humid;
    }

    public void setHumid(int humid) {
        this.humid = humid;
    }

    public int getPres() {
        return pres;
    }

    public void setPres(int pres) {
        this.pres = pres;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }
}
