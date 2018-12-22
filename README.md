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

---

### Voltage Peak detection

The bar graph on the Device tab shows the voltage peaks at the 60Hz bin of channel one's FFT data.
Volt Sense does this programmatically by simply finding all the voltage levels between the 50 and 70Hz bins and grabbing the maximumm of those value.
This is what is displayed in real-time on the "Voltage Level" section above and as well as on the graph. 
The result is generally the value around the 60Hz bin, but can vary when the device is away from a voltage source and Volt Sense is just reading general jitter.
If any of these peaks reach above the alarm threshold, they will be logged on the "events" tab.

---

## Events

The Events tab shows all voltage peaks that have crossed the alarm theshold.
Volt Sense logs the date, time, location (of the android device), and duration of each peak.
The bar graph on this page is similar to the one on the Device tab in that it shows voltage peaks, but this one only shows peaks that have crossed the threshold, which we will rever to as "Events".

![alt text](https://github.com/edgeos/wearables_ble_android_app/blob/master/images/events_tab.jpg)

From this page the user has the option to either save the log file on the SD card of the device, or upload the file to the cloud thourgh Amazons AWS IoT.
The "Save To Device" button will save the current log file to the SD card in a custom folder in the downloads directory.
As mentioned, the "Save To Cloud" button will upload the log file to an AWS S3 bucket for the users organization.
Next, the aptly named "Clear Log" button will clear the current log.
To bring up old files on the device, press the "Find Saved Files" button, which will list all files in the VoltSense folder of the downloads directory of the device.
When viewinng an old file, the user can upload it to the clous with the "Save To Cloud" button wich will delete that file from the device after a successful upload.

## History

# Development

## Project Structure

This project is structured like most basic android aplications, it follows the documented guidelines for Android development structure found [here](https://developer.android.com/studio/projects/).
Within /app/src/main/res/ is all the XML code responsible for the UI. Deeper down, /res/layout/ holds all the code for the layout of the main pages and fragments, these are the most commonly used UI elements of the application.
Moving to the business logic of the app in /app/src/main/java/com/wearables/ge/wearables_ble_receiver/  -  /activities/main/AuthenticatorActivity.java is where that app opens up to, giving the user an Amazon Cognito sign-in page.
Amazons Cognito user sign in tool rises a small problem becasue it is hardly custumizeable, with only a few settings that the developer can change to make the sign on page a little more user friendly.

Once the user has successfully logged on, the app switches activities to MainTabbedActivity.java which does most of the work. 
After setting up the necessary UI elements, such as the top action bar, menu button, and tabulated structure, the main activity will then bind iteself to the custom bluetooth service.
Then it will begin the Location Service which, unlike the Bluetooth Service, is not by definition a "service" in respect to [Androids documented definition](https://developer.android.com/guide/components/services) of a service. 
The location service runs asynchronously in the background, logging location changes to provide GPS coordinates for the Alarm Event logging.

Volt Sense works with four main fragments which are all childs of the MainTabbedActivity class, the apps fragment design follows the model documented on [Androids Developers Guide](https://developer.android.com/guide/components/fragments).
The pairing fragment is the landing page of the app, after the user logs in. 
Pairing is a misleading term for now, as Volt Sense only connects to Bluetooth device and doesn't pair, however there are plans to add this feature.
The pairing fragment will kick off a bluetooth scan automatically, which is not part of the Bluetooth Service, but a method of the Pairing Fragment.

---

### BLE Scans

The Bluetooth scans on this app will find any nearby bluetooth devices and display their boradcasted name ("Unkown" if no device name is provided) and their MAC addresses.
The scan is initiated in the startScan() method of the PairingFragment class, as seen here:

```java
if (!hasPermissions() || mScanning) {
	return;
}

List<ScanFilter> filters = new ArrayList<>();
ScanSettings settings = new ScanSettings.Builder()
		.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
		.build();

mScanCallback = new BtleScanCallback();
mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
mBluetoothLeScanner.startScan(filters, settings, mScanCallback);
mScanning = true;
mHandler = new Handler();
mHandler.postDelayed(this::stopScan, SCAN_PERIOD);
```

First Volt Sense checks if it has Bluetooth permissions and the method returns if it does not. It will first ask for permissions before returning.
A [filter is added](https://developer.android.com/reference/android/bluetooth/le/ScanSettings) to run the scan in low power mode, which returns results in quick batches rather than realtime scan results, this consumes less battery on the device.
The BtleScanCallback is a callback method for the scan, which asynchronously handles the scan results as they are returned.

---

```java
private class BtleScanCallback extends ScanCallback {

	private Map<String, BluetoothDevice> scanResults;

	BtleScanCallback() {
		scanResults = new HashMap<>();
	}

	@Override
	public void onScanResult(int callbackType, ScanResult result) {
		addScanResult(result);
	}

	@Override
	public void onBatchScanResults(List<ScanResult> results) {
		for (ScanResult result : results) {
			addScanResult(result);
		}
	}

	@Override
	public void onScanFailed(int errorCode) {
		Log.e(TAG, "BLE Scan Failed with code " + errorCode);
	}

	Boolean grey = true;
	private void addScanResult(ScanResult result) {
		BluetoothDevice obj = result.getDevice();
		String deviceAddress = obj.getAddress();
		String objName = obj.getName() == null ? deviceAddress : obj.getName();
		//Logic for adding device to the UI
	}
}
```
---



---

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
