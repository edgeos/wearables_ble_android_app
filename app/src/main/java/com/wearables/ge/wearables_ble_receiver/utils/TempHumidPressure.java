package com.wearables.ge.wearables_ble_receiver.utils;

import android.util.Log;

import com.github.cliftonlabs.json_simple.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class TempHumidPressure extends Data {
    public String TAG = "TempHumidPressure";

    public String Type() { return "temphumidpressure"; }

    public double temp;
    public double humid;
    public double pres;

    public TempHumidPressure(String hexString){
        List<String> hexSplit = Arrays.asList(hexString.split("\\s+"));
        if(hexSplit.size() == 12){
            String tempString = hexSplit.get(3) + hexSplit.get(2) + hexSplit.get(1) + hexSplit.get(0);
            String humidString = hexSplit.get(7) + hexSplit.get(6) + hexSplit.get(5) + hexSplit.get(4);
            String pressureString = hexSplit.get(11) + hexSplit.get(10) + hexSplit.get(9) + hexSplit.get(8);
            Log.d(TAG, "temp: " + tempString + " humid: " + humidString + " pressure: "+ pressureString);

            try {
                int tempRaw = Integer.parseInt(tempString, 16);
                int humidRaw = Integer.parseInt(humidString, 16);
                int presRaw = Integer.parseInt(pressureString, 16);

                this.temp = (tempRaw * 0.01);
                this.humid = (humidRaw / 1024.0);
                this.pres = (presRaw / 100.0);
            } catch(NumberFormatException e) {}
        } else if(hexSplit.size() == 6) {
            String tempString = hexSplit.get(1) + hexSplit.get(0);
            String humidString = hexSplit.get(3) + hexSplit.get(2);
            String pressureString = hexSplit.get(5) + hexSplit.get(4);
            Log.d(TAG, "temp: " + tempString + " humid: " + humidString + " pressure: "+ pressureString);
            try{
                int tempRaw = Integer.parseInt(tempString, 16);
                int humidRaw = Integer.parseInt(humidString, 16);
                int presRaw = Integer.parseInt(pressureString, 16);

                this.temp = (tempRaw * 0.01);
                this.humid = (humidRaw / 1024.0);
                this.pres = (presRaw / 100.0);
            } catch(NumberFormatException e) {}
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

    public JsonObject toJson() {
        JsonObject msg = new JsonObject();

        msg.put("\"temp\"",this.getTemp());
        msg.put("\"humid\"",this.getHumid());
        msg.put("\"pressure\"",this.getPres());

        return msg;
    }

    public JSONObject DataToJSON() throws JSONException {
        JSONObject msg = new JSONObject();

        msg.put("temp",this.getTemp());
        msg.put("humid",this.getHumid());
        msg.put("pressure",this.getPres());

        return msg;
    }

}
