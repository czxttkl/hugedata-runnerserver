package com.czxttkl.hugedata.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.android.chimpchat.adb.AdbBackend;
import com.czxttkl.hugedata.helper.LogFormatter;
import com.czxttkl.hugedata.helper.NameDevicePair;
import com.czxttkl.hugedata.helper.Test;

public class RunnerServer {

	private static final String ADB_LOCATION = "c:/Android/platform-tools/adb.exe";
	private static final int ADB_CONNECTION_WAITTIME_THRESHOLD = 5000;
	private static HashMap<String, NameDevicePair> deviceInfoMap = new HashMap<String, NameDevicePair>();
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
			//e.printStackTrace();
			logger.severe("File Handler Initialization Failed. Caused by SecurityException.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			logger.severe("File Handler Initialization Failed. Caused by IOException.");
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			initRunnerServer();
			logger.info("Runner Server Initialization Completed.");
		} catch (IllegalArgumentException e) {
			logger.severe("Runner Server Initialization Failed. Caused by " + e.getMessage());
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			logger.severe("Runner Server Initialization Failed. Caused by SecurityException");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			logger.severe("Runner Server Initialization Failed. Caused by IOException");
		}

		System.out.println("Now Connecting Device:");
		for (String a : adbBackend.listAttachedDevice()) {
			System.out.println(a);
		}

		try {
			Test a = new Test.Builder("com.renren.mobile.android.test",
					deviceInfoMap.get("HTCT328W")).testDurationThres(999999)
					.appInstallPath("c:/Android/mytools/renren.apk")
					.clearHistory(true).build();
			new Thread(a).start();
			//Test.tryLock();

		} catch (IllegalArgumentException e) {
			System.out.println(e.getMessage());

			// e.printStackTrace();
		}

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

	private static void initRunnerServer() throws SecurityException, IOException {
		// TODO Auto-generated method stub
		adbBackend = new AdbBackend(ADB_LOCATION, false);
		deviceInfoMap.put(
				"HTCT328W",
				new NameDevicePair("HC29GPG09471", adbBackend
						.waitForConnection(ADB_CONNECTION_WAITTIME_THRESHOLD,
								"HC29GPG09471")));
		Test.setLogger("PacketTest.log", true);
		Test.setAdbLocation("c:/Android/platform-tools/adb");
	}

}
