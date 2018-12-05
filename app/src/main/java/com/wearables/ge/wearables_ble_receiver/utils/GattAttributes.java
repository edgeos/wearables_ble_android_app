/*
 * Copyright (c) 2017 General Electric Company. All rights reserved.
 *
 * The copyright to the computer software herein is the property of
 * General Electric Company. The software may be used and/or copied only
 * with the written permission of General Electric Company or in accordance
 * with the terms and conditions stipulated in the agreement/contract
 * under which the software has been supplied.
 */

package com.wearables.ge.wearables_ble_receiver.utils;

import java.util.UUID;

@SuppressWarnings("unused")
public class GattAttributes {
    private static final String UUID_MASK = "0000%s-0000-1000-8000-00805f9b34fb";
    private static final String VOLTAGE_UUID_MASK = "58da000%s-f287-4b46-8e75-9e6dcfa567c1";


    public static UUID BATT_LEVEL_CHAR_UUID = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");
    public static UUID BATT_SERVICE_UUID = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");

    public static UUID VOLTAGE_WRISTBAND_SERVICE_UUID = UUID.fromString(String.format(VOLTAGE_UUID_MASK, "1"));
    public static UUID VOLTAGE_ALARM_STATE_CHARACTERISTIC_UUID = UUID.fromString(String.format(VOLTAGE_UUID_MASK, "2"));
    public static UUID VOLTAGE_ALARM_CONFIG_CHARACTERISTIC_UUID = UUID.fromString(String.format(VOLTAGE_UUID_MASK, "3"));
    public static UUID ACCELEROMETER_DATA_CHARACTERISTIC_UUID = UUID.fromString(String.format(VOLTAGE_UUID_MASK, "4"));
    public static UUID TEMP_HUMIDITY_PRESSURE_DATA_CHARACTERISTIC_UUID = UUID.fromString(String.format(VOLTAGE_UUID_MASK, "5"));
    public static UUID GAS_SENSOR_DATA_CHARACTERISTIC_UUID = UUID.fromString(String.format(VOLTAGE_UUID_MASK, "6"));
    public static UUID OPTICAL_SENSOR_DATA_CHARACTERISTIC_UUID = UUID.fromString(String.format(VOLTAGE_UUID_MASK, "7"));
    public static UUID STREAMING_DATA_CHARACTERISTIC_UUID = UUID.fromString(String.format(VOLTAGE_UUID_MASK, "8"));

    public static UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString(String.format(UUID_MASK, "2902"));

    public static int MESSAGE_TYPE_RENAME = 1;
    public static int MESSAGE_TYPE_ALARM_THRESHOLD = 2;
    public static int MESSAGE_TYPE_MODE = 3;

}
