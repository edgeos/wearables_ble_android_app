# Volt Sense

## Description

This is the android mobile application developed by GE Global Research Center to work with the high voltage sensor wristband device also develpoed by GRC.
Volt Sense requires an Amazon Cognito login for app-to-cloud data streaming and provides an interface for new users to create an account.
Once a user has logged into their account they will be redirected to the device connection page where they can connect to nearby bluetooth devices.
While Volt Sense can connect to any bluetooth device that will allow it, the fatures in the app will only work with a Bluetooth Low Energy (BLE) voltage wristband.
Once a device is connceted via BLE, you will see a message at the botom of the screen that says "Device Connected!" this indicated that Volt Sense has not only connected to the device but also discovered some services on the device that broadcast data.
At this point, the device will start streaming live data to the application, move to the "Device" dab to see the incoming data.

## Device Tab

![alt text](https://github.com/edgeos/wearables_ble_android_app/blob/master/images/device_tab.jpg)

## To build

Make sure Android Studio and the Android SDK are installed, and open this project in Android Studio.
Android studio will automatically install required packages and libraries.
Let the project build and accept any download prompts from Android Studio.

## To run

This project only supports devices with an Android SDK of 23 or higher, which means Android version 6.0 (Released 2015) or above.
Attach an adroid device of appropriate software version to your dev machine and enable dev-mode and then debugging on the Android device.
See [Androids guide on enabling Dev-mode](https://developer.android.com/studio/debug/dev-options) for more information on configuring developer options.
Once debugging is enabled on the phone/tablet, press "Run App" and Android studio should detect the connected device and display it as an option to run on.
Select that device and press "OK" and Android Studio will likely prompt to download the necessary SDK for  that phone, do this and allow Android Studio to run code on the connected device.
