package com.wearables.ge.wearables_ble_receiver.utils;

import android.util.Log;

import com.github.cliftonlabs.json_simple.JsonObject;

import org.json.*;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class GasSensorData extends Data {
    public String TAG = "GasSensorData";

    public String Type() { return "gas"; }

    public int m_nEquivCO2;
    public int m_nTotalVOC;
    public int m_nError;
    public String m_sError;

    public final static String sm_sErrors[] = {
            "Invalid Register Write",
            "Invalid Register Read",
            "Invalid Measure Mode",
            "Max Resistance",
            "Heater Fault",
            "Voltage Fault"
    };

    public GasSensorData(String hexString){
        List<String> hexSplit = Arrays.asList(hexString.split("\\s+"));
        if(hexSplit.size() >= 5){
            //switch values for little endian
            String co2ValueString = hexSplit.get(1) + hexSplit.get(0);
            String vocValueString = hexSplit.get(3) + hexSplit.get(2);
            String errValueString = hexSplit.get(4);
            Log.d(TAG,  "co2String: " + co2ValueString + " vocString: " + vocValueString + " errString: " + errValueString);
            this.m_nEquivCO2 = Integer.parseInt(co2ValueString, 16);
            this.m_nTotalVOC= Integer.parseInt(vocValueString, 16);
            this.m_nError = Integer.parseInt(errValueString, 16);
            if(this.m_nError != 0)
                for(int i = 0, m = 1; i < sm_sErrors.length; ++i, m <<= 1)
                    if(m == (this.m_nError & m)) {
                        if (this.m_sError.isEmpty()) this.m_sError = sm_sErrors[i];
                        else this.m_sError += "," + sm_sErrors[i];
                    }
            Log.d(TAG, "eCO2: " + m_nEquivCO2 + " TVOC: " + m_nTotalVOC + " Error: " + m_sError);
        } else {
            Log.d(TAG, "HexSplit list size: " + hexSplit.size() + " size of 5 was expected");
        }
    }

    public int getEquivCO2() {
        return m_nEquivCO2;
    }

    public void setEquivCO2(int xValue) {
        this.m_nEquivCO2 = xValue;
    }

    public int getTotalVOC() {
        return m_nTotalVOC;
    }

    public void setTotalVOC(int yValue) {
        this.m_nTotalVOC = yValue;
    }

    public int getError() {
        return m_nError;
    }

    public String getErrorString() {
        return m_sError;
    }

    public JsonObject toJson() {
        JsonObject msg = new JsonObject();
        msg.put("\"eCO2\"", this.getEquivCO2());
        msg.put("\"TVOC\"", this.getTotalVOC());
        return msg;
    }

    public JSONObject DataToJSON() throws JSONException {
        JSONObject msg = new JSONObject();
        msg.put("eCO2", this.getEquivCO2());
        msg.put("TVOC", this.getTotalVOC());
        return msg;
    }
}
