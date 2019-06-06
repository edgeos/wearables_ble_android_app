package com.wearables.ge.wearables_ble_receiver.utils;

import android.util.Log;

import com.github.cliftonlabs.json_simple.JsonObject;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class TempHumidPressure {
    public String TAG = "TempHumidPressure";

    public Long date;

    public double temp;
    public double humid;
    public double pres;

    public TempHumidPressure(String hexString){

        this.date = Calendar.getInstance().getTimeInMillis();

        List<String> hexSplit = Arrays.asList(hexString.split("\\s+"));
        if(hexSplit.size() == 12){
            String tempString = hexSplit.get(3) + hexSplit.get(2) + hexSplit.get(1) + hexSplit.get(0);
            String humidString = hexSplit.get(7) + hexSplit.get(6) + hexSplit.get(5) + hexSplit.get(4);
            String pressureString = hexSplit.get(11) + hexSplit.get(10) + hexSplit.get(9) + hexSplit.get(8);

            int tempRaw = Integer.parseInt(tempString, 16);
            int humidRaw = Integer.parseInt(humidString, 16);
            int presRaw = Integer.parseInt(pressureString, 16);

            this.temp = (tempRaw * 0.01);
            this.humid = (humidRaw / 1024);
            this.pres = (presRaw / 256);
            Log.d(TAG, "temp: " + tempString + " humid: " + humidString + " pressure: "+ pressureString);
        } else if(hexSplit.size() == 6) {
            String tempString = hexSplit.get(1) + hexSplit.get(0);
            String humidString = hexSplit.get(3) + hexSplit.get(2);
            String pressureString = hexSplit.get(5) + hexSplit.get(4);
            int tempRaw = Integer.parseInt(tempString, 16);
            int humidRaw = Integer.parseInt(humidString, 16);
            int presRaw = Integer.parseInt(pressureString, 16);

            this.temp = (tempRaw * 0.01);
            this.humid = (humidRaw / 1024);
            this.pres = (presRaw / 256);
            Log.d(TAG, "temp: " + tempString + " humid: " + humidString + " pressure: "+ pressureString);
        } else {
            Log.d(TAG, "Temp/Pressure/Humid hex string malformed: " + hexString);
        }
    }

    public double getTemp() {
        return temp;
    }

    public void setTemp(double temp) {
        this.temp = temp;
    }

    public double getHumid() {
        return humid;
    }

    public void setHumid(double humid) {
        this.humid = humid;
    }

    public double getPres() {
        return pres;
    }

    public void setPres(double pres) {
        this.pres = pres;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public JsonObject toJson() {
        JsonObject msg = new JsonObject();

        msg.put("\"temp\"",this.getTemp());
        msg.put("\"humid\"",this.getHumid());
        msg.put("\"pressure\"",this.getPres());

        return msg;
    }

}
