package com.wearables.ge.wearables_ble_receiver.utils;

import android.util.Log;

import com.github.cliftonlabs.json_simple.JsonObject;
import org.json.*;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class AccelerometerData extends Data {
    public String TAG = "AccelerometerData";

    public String Type() { return "accelerometer"; }

    public int xValue;
    public int yValue;
    public int zValue;

    public AccelerometerData(String hexString){
        List<String> hexSplit = Arrays.asList(hexString.split("\\s+"));
        if(hexSplit.size() >= 6){
            //switch values for little endian
            String xValueString = hexSplit.get(1) + hexSplit.get(0);
            String yValueString = hexSplit.get(3) + hexSplit.get(2);
            String zValueString = hexSplit.get(5) + hexSplit.get(4);
//            Log.d(TAG,  "xValueString: " + xValueString + " yValueString: " + yValueString + " zValueString: " + zValueString);
            this.xValue = (short) Integer.parseInt(xValueString, 16);
            this.yValue = (short) Integer.parseInt(yValueString, 16);
            this.zValue = (short) Integer.parseInt(zValueString, 16);
            Log.d(TAG, "xValue: " + xValue + " yValue: " + yValue + " zValue: " + zValue);
        } else {
            Log.d(TAG, "HexSplit list size: " + hexSplit.size() + " size of 6 was expected");
        }
    }

    public int getxValue() {
        return xValue;
    }

    public void setxValue(int xValue) {
        this.xValue = xValue;
    }

    public int getyValue() {
        return yValue;
    }

    public void setyValue(int yValue) {
        this.yValue = yValue;
    }

    public int getzValue() {
        return zValue;
    }

    public void setzValue(int zValue) {
        this.zValue = zValue;
    }

    public JsonObject toJson() {
        JsonObject msg = new JsonObject();
        msg.put("\"x\"", this.getxValue());
        msg.put("\"y\"", this.getyValue());
        msg.put("\"z\"", this.getzValue());
        return msg;
    }

    public JSONObject DataToJSON() throws JSONException {
        JSONObject msg = new JSONObject();
        msg.put("x", this.getxValue());
        msg.put("y", this.getyValue());
        msg.put("z", this.getzValue());
        return msg;
    }
}
