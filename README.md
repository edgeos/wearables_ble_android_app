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

From here you can see live data streaming from the BLE deice.
The broadcasted name of the device will appear in the upper left corner of the screen.
The data you see on this page is all real-time data from the device and is updated as it streams from sensor to app.

On this page, the user can set the alarm threshold of the voltage device.
This is the threshold at which the sensor will use to alarm the wearer of dangerous voltages.
Scroll the threshold slider bar to the desired number and a dialog will appear to accept the changes.

![alt text](https://github.com/edgeos/wearables_ble_android_app/blob/master/images/alarm_thresh_change.jpg)

Once you have accepted the new threshold, Volt Sense will write data to the voltage band to make those changes on the BLE device.
The changes should be applied immediately and, if the threshold is below any of the peaks of the device chatter, you will hear the alarm.
The new threshold line will appear on the voltage peak graph below.

### Voltage Peak detection

The bar graph on the Device tab shows the voltage peaks at the 60Hz bin of channel one's FFT data.
Volt Sense does this programmatically by simply finding all the voltage levels between the 50 and 70Hz bins and grabbing the maximumm of those value.
This is what is displayed in real-time on the "Voltage Level" section above and as well as on the graph. 
The result is generally the value around the 60Hz bin, but can vary when the device is away from a voltage source and Volt Sense is just reading general jitter.
If any of these peaks reach above the alarm threshold, they will be logged on the "events" tab.

## Events

The Events tab shows all voltage peaks that have crossed the alarm theshold.
Volt Sense logs the date, time, location (of the android device), and duration of each peak.
The bar graph on this page is similar to the one on the Device tab in that it shows voltage peaks, but this one only shows peaks that have crossed the threshold, which we will rever to as "Events".

![alt text](https://github.com/edgeos/wearables_ble_android_app/blob/master/images/events_tab.jpg)

From this page the user has the option to either save the log file on the SD card of the device, or upload the file to the cloud thourgh Amazons AWS IoT.
The "Save To Device" button will save the current log file to the SD card in a custom folder in the downloads directory.
As mentioned, the "Save To Cloud" button will upload the log file to an AWS S3 button for the users organization.
Next, the aptly named "Clear Log" button will clear the current log.
To bring up old files on the device, press the "Find Saved Files" button, which will list all files in the VoltSense folder of the downloads directory of the device.
When viewinng an old file, the user can upload it to the clous with the "Save To Cloud" button wich will delete that file from the device after a successful upload.

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
