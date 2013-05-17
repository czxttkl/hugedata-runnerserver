package com.czxttkl.hugedata.server;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.android.chimpchat.adb.AdbBackend;
import com.android.chimpchat.core.IChimpDevice;
import com.android.chimpchat.core.PhysicalButton;
import com.android.chimpchat.core.TouchPressType;
import com.czxttkl.hugedata.helper.LogFormatter;
import com.czxttkl.hugedata.helper.DeviceInfo;
import com.czxttkl.hugedata.test.PacketTest;
import com.czxttkl.hugedata.test.Test;

/**
 * @author Zhengxing Chen
 * 
 *         See reference on: https://code.google.com/p/hugedata-runner-server/
 */
public class RunnerServer {

	public static final ResourceBundle InitBundle = ResourceBundle.getBundle("InitBundle");

	private String ADB_LOCATION;
	private int ADB_CONNECTION_WAITTIME_THRESHOLD;
	public static int locationNum;

	public static HashMap<String, ArrayList<DeviceInfo>> deviceInfoMap = new HashMap<String, ArrayList<DeviceInfo>>();
	public static ExecutorService executor = Executors.newCachedThreadPool();

	private AdbBackend adbBackend;
	public static LogFormatter logFormatter;
	public static Logger logger = Logger
			.getLogger(RunnerServer.class.getName());

	public RunnerServer() {
		initRunnerServer();
	}

	/**
	 * Initialize the runner server
	 */
	private void initRunnerServer() {
		// Set the Log
		logFormatter = new LogFormatter();
		setServerLog();

		logger.info("----------------------------------------------------------------");
		logger.info("Runner Server Initialization Starts");

		// Initialize the basic parameters
		ADB_LOCATION = InitBundle.getString("Adb.Location");
		ADB_CONNECTION_WAITTIME_THRESHOLD = Integer.valueOf(InitBundle
				.getString("Adb.Connection.Wait.Threshold"));
		locationNum = Integer.valueOf(InitBundle
				.getString("Location.Number"));
		
		Test.setAdbLocation(ADB_LOCATION);
		Test.setTestLocation(locationNum);

		// Initialize AdbBackend
		adbBackend = new AdbBackend(ADB_LOCATION, false);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		logger.info("AdbBackend Established and Connected. ");

		// Check out currently connected devices
		checkOutDevice();

		logger.info("Runner Server Initialization Completed.");
		logger.info("----------------------------------------------------------------");
	}

	/**
	 * Set and format the log
	 */
	private void setServerLog() {
		logger.setLevel(Level.FINEST);
		FileHandler fileHandler;
		try {
			fileHandler = new FileHandler("RunnerServer.log", true);
			fileHandler.setFormatter(logFormatter);
			logger.addHandler(fileHandler);
		} catch (Exception e) {
			logger.severe("File Handler Initialization Failed. Caused by "
					+ e.getMessage());
		}
	}

	/**
	 * Check out currently connected devices
	 */
	private void checkOutDevice() {
		for (String deviceAdbName : adbBackend.listAttachedDevice()) {
			// deviceAdbName is the serialno in build.prop on the phone
			// Ensure that different phones have different SerialNo in
			// build.prop
			System.out.println(deviceAdbName);

			IChimpDevice device = adbBackend.waitForConnection(
					ADB_CONNECTION_WAITTIME_THRESHOLD, deviceAdbName);

			if (device != null) {

				// Start Maintenance Helper on the device
				device.startActivity(null, null, null, null, null, null,
						"com.czxttkl.hugedata/.activity.MainActivity", 0);

				// waiting for Maintenance Helper launching
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				// Extract device info from Maintenance Helper
				String deviceInfoLocation = InitBundle.getString("Device.Info.Location");
				String raw = device.shell("cat " + deviceInfoLocation)
						.trim();
				String[] metrics = raw.split(":");
				String manufacturer = metrics[0];
				String type = metrics[1];
				String network = metrics[2];

				// Create DeviceInfo instance
				// DeviceInfo implements Runnable interface
				DeviceInfo deviceInfo = new DeviceInfo(manufacturer, type,
						network, deviceAdbName, device);
				executor.execute(deviceInfo);
				
				String key = manufacturer + type + network;
				if (!deviceInfoMap.containsKey(key)) {
					ArrayList<DeviceInfo> arr = new ArrayList<DeviceInfo>();
					arr.add(deviceInfo);
					deviceInfoMap.put(key, arr);
				} else {
					ArrayList<DeviceInfo> arr = deviceInfoMap.get(key);
					arr.add(deviceInfo);
				}

				logger.info("Device Added, Manufacturer:" + manufacturer
						+ ", Type:" + type + ", Network:" + network
						+ ", ADB Name:" + deviceAdbName);
			}
		}

	}

}
