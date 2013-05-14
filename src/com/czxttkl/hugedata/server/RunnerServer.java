package com.czxttkl.hugedata.server;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashMap;
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
 * See reference on: https://code.google.com/p/hugedata-runner-server/
 */
public class RunnerServer {

	private static String ADB_LOCATION;
	private static int ADB_CONNECTION_WAITTIME_THRESHOLD;
	private static HashMap<String, DeviceInfo> deviceInfoMap = new HashMap<String, DeviceInfo>();
	private static ExecutorService exec = Executors.newCachedThreadPool();
	public static int locationNum;

	private static AdbBackend adbBackend;
	public static LogFormatter logFormatter;
	public static Logger logger = Logger
			.getLogger(RunnerServer.class.getName());

	public static void main(String[] args) throws InterruptedException,
			IOException {
		// TODO Auto-generated method stub

		initRunnerServer();

		// Judge if the device is suspended
		// suspend the device
		
		PacketTest a = new PacketTest.Builder("com.renren.mobile.android.test",
				deviceInfoMap.get("HTCT328WUNI"))
				.testInstallPath("c:/Android/mytools/RenrenTestProject1.apk")
				.appInstallPath("c:/Android/mytools/renren.apk")
				.testDurationThres(999999).build();
		Thread.sleep(5000);
		PacketTest b = new PacketTest.Builder("com.renren.mobile.android.test",
				deviceInfoMap.get("HTCT328WUNI"))
				.testInstallPath("c:/Android/mytools/RenrenTestProject1.apk")
				.appInstallPath("c:/Android/mytools/renren.apk")
				.testDurationThres(999999).priority(5).build();
		Thread.sleep(5000);
		PacketTest c = new PacketTest.Builder("com.renren.mobile.android.test",
				deviceInfoMap.get("HTCT328WUNI"))
				.testInstallPath("c:/Android/mytools/RenrenTestProject1.apk")
				.appInstallPath("c:/Android/mytools/renren.apk")
				.testDurationThres(999999).priority(6).build();
		// new Thread(a).start();
		deviceInfoMap.get("HTCT328WUNI").addToTestQueue(a);
		deviceInfoMap.get("HTCT328WUNI").addToTestQueue(b);
		deviceInfoMap.get("HTCT328WUNI").addToTestQueue(c);
		// Test.tryLock();

		/*
		 * Collection<String> viewList = me.getViewIdList(); for(String a :
		 * viewList){ System.out.println(a); }
		 */

		// ArrayList<String> cat = new ArrayList<String>();
		// cat.add("android.intent.category.LAUNCHER");

		// me.startActivity(null,null,null,null,null,null,"com.czxttkl.hugedata/.MainActivity",
		// 0);
		// Thread.sleep(3000);
		// me.press(PhysicalButton.HOME,TouchPressType.DOWN_AND_UP);

		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				e.printStackTrace();
				if (e instanceof OutOfMemoryError) 
					logger.log(Level.SEVERE, "Out of memory error", e);
				 else 
					logger.log(Level.SEVERE, "Uncaught exception", e);
			}
		});
	}

	/**
	 * Initialize the runner server
	 */
	private static void initRunnerServer() {
		//Set the Log 
		logFormatter = new LogFormatter();
		setServerLog();
		logger.info("----------------------------------------------------------------");
		logger.info("Runner Server Initialization Starts");
		//Initialize the basic parameters
		ADB_LOCATION = "c:/Android/platform-tools/adb.exe";
		ADB_CONNECTION_WAITTIME_THRESHOLD = 5000;
		locationNum = 101010;
		Test.setAdbLocation("c:/Android/platform-tools/adb");
		Test.setTestLocation(locationNum);
		adbBackend = new AdbBackend(ADB_LOCATION, false);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("AdbBackend Established and Connected. ");
		//Check out currently connected devices 
		checkOutDevice();
		logger.info("Runner Server Initialization Completed.");
		logger.info("----------------------------------------------------------------");
	}

	/**
	 * Set the log
	 */
	private static void setServerLog() {
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
	private static void checkOutDevice() {
		System.out.println("Now Connecting Device:");
		
		for (String deviceAdbName : adbBackend.listAttachedDevice()) {
			System.out.println(deviceAdbName);
			IChimpDevice device = adbBackend.waitForConnection(
					ADB_CONNECTION_WAITTIME_THRESHOLD, deviceAdbName);
			if (device != null) {
				device.startActivity(null, null, null, null, null, null,
						"com.czxttkl.hugedata/.activity.MainActivity", 0);
				// waiting for hugedata setting up
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				String raw = device.shell("cat /sdcard/hugedata/deviceinfo")
						.trim();
				String[] metrics = raw.split(":");
				String manufacturer = metrics[0];
				String type = metrics[1];
				String network = metrics[2];

				DeviceInfo deviceInfo = new DeviceInfo(manufacturer, type,
						network, deviceAdbName, device);
				exec.execute(deviceInfo);
				deviceInfoMap.put(manufacturer + type + network, deviceInfo);
				logger.info("Device Added, Manufacturer:" + manufacturer
						+ ", Type:" + type + ", Network:" + network
						+ ", ADB Name:" + deviceAdbName);
			}
		}

	}

}
