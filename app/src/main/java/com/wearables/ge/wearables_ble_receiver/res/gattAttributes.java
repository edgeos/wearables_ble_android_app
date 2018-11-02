/*
 * Copyright (c) 2017 General Electric Company. All rights reserved.
 *
 * The copyright to the computer software herein is the property of
 * General Electric Company. The software may be used and/or copied only
 * with the written permission of General Electric Company or in accordance
 * with the terms and conditions stipulated in the agreement/contract
 * under which the software has been supplied.
 */

package com.wearables.ge.wearables_ble_receiver.res;

import java.util.UUID;

public class gattAttributes {

    public static UUID BATT_LEVEL_CHAR_UUID = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");
    public static UUID BATT_SERVICE_UUID = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
    public static UUID VOLTAGE_WRISTBAND_SERVICE_UUID = UUID.fromString("40010001-4c0b-954d-8451-a0d4a5d77036");
    public static UUID VOLTAGE_ALARM_STATE_CHARACTERISTIC_UUID = UUID.fromString("40010002-4c0b-954d-8451-a0d4a5d77036");
    public static UUID VOLTAGE_ALARM_CONFIG_CHARACTERISTIC_UUID = UUID.fromString("40010003-4c0b-954d-8451-a0d4a5d77036");
    public static UUID ACCELEROMETER_DATA_CHARACTERISTIC_UUID = UUID.fromString("40010004-4c0b-954d-8451-a0d4a5d77036");
    public static UUID TEMP_HUMIDITY_PRESSURE_DATA_CHARACTERISTIC_UUID = UUID.fromString("40010005-4c0b-954d-8451-a0d4a5d77036");
    public static UUID GAS_SENSOR_DATA_CHARACTERISTIC_UUID = UUID.fromString("40010006-4c0b-954d-8451-a0d4a5d77036");
    public static UUID OPTICAL_SENSOR_DATA_CHARACTERISTIC_UUID = UUID.fromString("40010007-4c0b-954d-8451-a0d4a5d77036");
    public static UUID STREAMING_DATA_CHARACTERISTIC_UUID = UUID.fromString("40010008-4c0b-954d-8451-a0d4a5d77036");

}
