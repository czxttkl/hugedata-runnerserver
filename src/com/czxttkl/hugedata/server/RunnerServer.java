package com.czxttkl.hugedata.server;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
				.testDurationThres(999999).build();
		Thread.sleep(5000);
		PacketTest c = new PacketTest.Builder("com.renren.mobile.android.test",
				deviceInfoMap.get("HTCT328WUNI"))
				.testInstallPath("c:/Android/mytools/RenrenTestProject1.apk")
				.appInstallPath("c:/Android/mytools/renren.apk")
				.testDurationThres(999999).build();
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

	}

	private static void initRunnerServer() {
		// TODO Auto-generated method stub
		ADB_LOCATION = "c:/Android/platform-tools/adb.exe";
		ADB_CONNECTION_WAITTIME_THRESHOLD = 5000;
		locationNum = 101010;
		logFormatter = new LogFormatter();
		setServerLog();
		
		adbBackend = new AdbBackend(ADB_LOCATION, false);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		checkOutDevice();
		// Class[] testClasses = { PacketTest.class };
		// Test.setLogger(testClasses, true);
		Test.setAdbLocation("c:/Android/platform-tools/adb");
		Test.setTestLocation(locationNum);
		logger.info("Test Configured Done.");
		logger.info("Runner Server Initialization Completed. Server Starts.");

	}

	private static void setServerLog() {
		// TODO Auto-generated method stub
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

	private static void checkOutDevice() {
		// TODO Auto-generated method stub
		System.out.println("Now Connecting Device:");
		
		for (String deviceAdbName : adbBackend.listAttachedDevice()) {
			System.out.println(deviceAdbName);
			IChimpDevice device = adbBackend.waitForConnection(
					ADB_CONNECTION_WAITTIME_THRESHOLD, deviceAdbName);
			if (device != null) {
				device.startActivity(null, null, null, null, null, null,
						"com.czxttkl.hugedata/.activity.MainActivity", 0);
				//waiting for hugedata setting up
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
