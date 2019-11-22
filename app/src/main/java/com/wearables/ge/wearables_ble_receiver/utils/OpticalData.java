package com.wearables.ge.wearables_ble_receiver.utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class OpticalData extends Data {
    public String TAG = "OpticalData";

    public String Type() { return "optical"; }

    public boolean m_bProximity;

    public OpticalData(String hexString){
        List<String> hexSplit = Arrays.asList(hexString.split("\\s+"));
        if(hexSplit.size() > 0)
            m_bProximity = Integer.parseInt(hexSplit.get(0)) != 0;
        else m_bProximity = false;
    }

    public boolean getProximity() { return m_bProximity; }
    public void setProximity(boolean b) { m_bProximity = b; }

    public JSONObject DataToJSON() throws JSONException {
        JSONObject msg = new JSONObject();
        msg.put("proximity", this.getProximity());
        return msg;
    }
}
