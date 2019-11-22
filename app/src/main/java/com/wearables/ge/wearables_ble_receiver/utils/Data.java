package com.wearables.ge.wearables_ble_receiver.utils;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.Calendar;

public abstract class Data {
    public static String sm_sDeviceId = "Wedge XFF";
    public static String sm_sUserId = "user_1";
    public static int sm_nIndent = 0;
    public abstract JSONObject DataToJSON() throws JSONException;
    public abstract String Type();
    public String SubType() { return "raw"; }

    public Long date;

    public Data() {
        this.date = Calendar.getInstance().getTimeInMillis();
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }


    public JSONObject toJSON() throws JSONException {
        JSONObject msg = new JSONObject();
        msg.put("timestamp", Calendar.getInstance().getTimeInMillis());
        msg.put("deviceId", sm_sDeviceId.replace(':', '_'));
        msg.put("userId", sm_sUserId);
        msg.put("type", Type());
        msg.put("subtype", SubType());
        msg.put("data", DataToJSON());
        return msg;
    }

    public String toJSONString() {
        try {
            JSONObject msg = toJSON();
            return sm_nIndent > 0 ? msg.toString(sm_nIndent) : msg.toString();
        } catch(JSONException e) {
            return "{\"error\" : \"" + e.toString() + "\"";
        }
    }

    public static String parseHex(String sz, int nStart, int nBytes) {
        String szRet = "";
        for(int i = 0; i < nBytes && nStart * 3 + 2 < sz.length() ; ++i, ++nStart)
            szRet = sz.substring(nStart * 3, nStart * 3 + 2) + szRet;
        return szRet;
    }

    public static int parseHexInt(String sz, int nStart) {
        return Integer.parseInt(parseHex(sz, nStart, 4), 16);
    }
}
