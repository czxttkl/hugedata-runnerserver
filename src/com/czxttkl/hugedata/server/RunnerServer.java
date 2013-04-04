package com.czxttkl.hugedata.server;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
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

	private static final String ADB_LOCATION = "c:/Android/platform-tools/adb.exe";
	private static final int ADB_CONNECTION_WAITTIME_THRESHOLD = 5000;
	private static HashMap<String, DeviceInfo> deviceInfoMap = new HashMap<String, DeviceInfo>();

	private static AdbBackend adbBackend;
	public static LogFormatter logFormatter = new LogFormatter();
	public static Logger logger;
	static {
		logger = Logger.getLogger(RunnerServer.class.getName());
		logger.setLevel(Level.FINEST);
		FileHandler fileHandler;
		try {
			fileHandler = new FileHandler("RunnerServer.log", true);
			fileHandler.setFormatter(logFormatter);
			logger.addHandler(fileHandler);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			logger.severe("File Handler Initialization Failed. Caused by SecurityException.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			logger.severe("File Handler Initialization Failed. Caused by IOException.");
		}
	}

	public static void main(String[] args) throws InterruptedException,
			IOException {
		// TODO Auto-generated method stub
		try {
			initRunnerServer();
			logger.info("Runner Server Initialization Completed. Server Starts.");
		} catch (InterruptedException e) {
			logger.severe("Runner Server Initialization Failed. Caused by "
					+ e.getMessage());
			throw e;
		} catch (IllegalArgumentException e) {
			logger.severe("Runner Server Initialization Failed. Caused by "
					+ e.getMessage());
			throw e;
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			logger.severe("Runner Server Initialization Failed in Logger Setup. Caused by SecurityException");
			throw e;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			logger.severe("Runner Server Initialization Failed in Logger Setup. Caused by IOException");
			throw e;
		}

		PacketTest a = new PacketTest.Builder("com.renren.mobile.android.test",
				deviceInfoMap.get("HTCT328W"), new SimpleDateFormat(
						"yyyyMMddHHmmss").format(new Date()).toString(), "TEL")
				.testInstallPath("c:/Android/mytools/RenrenTestProject1.apk")
				.appInstallPath("c:/Android/mytools/renren.apk")
				.testDurationThres(999999).build();
		new Thread(a).start();

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

	private static void initRunnerServer() throws SecurityException,
			IOException, InterruptedException {
		// TODO Auto-generated method stub
		
		adbBackend = new AdbBackend(ADB_LOCATION, false);
		Thread.sleep(5000);
		/*
		 * deviceInfoMap.put( "HTCT328W", new DeviceInfo("HTC", "T328W",
		 * "HC29GPG09471", adbBackend
		 * .waitForConnection(ADB_CONNECTION_WAITTIME_THRESHOLD,
		 * "HC29GPG09471")));
		 */

		System.out.println("Now Connecting Device:");
		for (String deviceAdbName : adbBackend.listAttachedDevice()) {
			System.out.println(deviceAdbName);
			IChimpDevice device = adbBackend.waitForConnection(
					ADB_CONNECTION_WAITTIME_THRESHOLD, deviceAdbName);
			if (device != null) {
				device.startActivity(null, null, null, null, null, null,
						"com.czxttkl.hugedata/.activity.MainActivity", 0);
				Thread.sleep(5000);
				String raw = device.shell("cat /sdcard/hugedata/deviceinfo")
						.trim();
				String manufacturer = raw.split(":")[0];
				String type = raw.split(":")[1];
				DeviceInfo deviceInfo = new DeviceInfo(manufacturer, type,
						deviceAdbName, device);
				deviceInfoMap.put(manufacturer + type, deviceInfo);
				logger.info("Device Added, Manufacturer:" + manufacturer
						+ ", Type:" + type + ", ADB Name:" + deviceAdbName);
			}
		}

		Class[] testClasses = { PacketTest.class };
		Test.setLogger(testClasses, true);
		Test.setAdbLocation("c:/Android/platform-tools/adb");
		Test.setTestLocation(101010);
		logger.info("Test Configured Done.");
	}

}
