package com.czxttkl.hugedata.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.android.chimpchat.core.IChimpDevice;
import com.android.ddmlib.ShellCommandUnresponsiveException;

public class PacketTest implements Runnable {

	public static String ADB_LOCATION;
	public static Logger logger;
	// Mandatory Parameters
	private final String TEST_PACKAGE_NAME;
	private final NameDevicePair PAIR;

	// Optional Parameters
	private final String APP_PACKAGE_NAME;
	private final int TEST_DURATION_THRESHOLD;
	private final String APP_INSTALL_PATH;
	private final String TEST_INSTALL_PATH;
	private final boolean CLEAR_HISTORY;

	private PacketTest(Builder builder) {
		TEST_PACKAGE_NAME = builder.TEST_PACKAGE_NAME;
		PAIR = builder.PAIR;

		TEST_DURATION_THRESHOLD = builder.testDurationThres;
		APP_INSTALL_PATH = builder.appInstallPath;
		TEST_INSTALL_PATH = builder.testInstallPath;
		APP_PACKAGE_NAME = TEST_PACKAGE_NAME.substring(0,
				TEST_PACKAGE_NAME.length() - 5);
		CLEAR_HISTORY = builder.clearHistory;
	}

	public static void setAdbLocation(String adbLocation) {
		// windows c:/adb or linux ~/adb or ../adb or a-bc/adb or /adb
		// No need to append ".exe"
		Pattern p = Pattern
				.compile("(([a-zA-Z]:)|~|(\\.\\.)|(\\w|-)*)/((\\w|-)+/)*adb");
		if (p.matcher(adbLocation).matches()) {
			PacketTest.ADB_LOCATION = adbLocation;
			logger.info("Adb Location set successfully");
		} else
			throw new IllegalArgumentException("Adb Location Parameter Illegal");
	}

	public static void setLogger(String logFilePath, boolean appendLog) throws SecurityException, IOException {
		LogFormatter logFormatter = new LogFormatter();
		logger = Logger.getLogger(PacketTest.class.getName());
		logger.setLevel(Level.FINEST);

		FileHandler fileHandler = new FileHandler(logFilePath, appendLog);
		fileHandler.setFormatter(logFormatter);
		logger.addHandler(fileHandler);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		suspendAvailability();

		IChimpDevice myDevice = getDevice();
		String testCmd = constructCmd();

		try {
			Process tcpdump = Runtime.getRuntime().exec(testCmd);
			/*
			 * BufferedReader dumpResult = new BufferedReader( new
			 * InputStreamReader(tcpdump.getInputStream())); String s; while ((s
			 * = dumpResult.readLine()) != null) System.out.println(s);
			 */

			if (APP_INSTALL_PATH != null)
				logger.info("Install package:"
						+ myDevice.installPackage(APP_INSTALL_PATH));
			else
				logger.info("No need to install app package");

			if (TEST_INSTALL_PATH != null)
				logger.info("Install test package:"
						+ myDevice.installPackage(TEST_INSTALL_PATH));
			else
				logger.info("No need to install test package");
			
			logger.info(myDevice.startTestInstrumentation(TEST_PACKAGE_NAME,
					TEST_DURATION_THRESHOLD));

			logger.info("Test Instrumentation finished");

			logger.info("Tcpdump has been killed."
					+ myDevice.shell("busybox pkill -SIGINT tcpdump"));

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ShellCommandUnresponsiveException e) {
			System.out.println("Test timed out. ");
		} finally {
			if (CLEAR_HISTORY){
				logger.info("Remove app package:"
						+ myDevice.removePackage(APP_PACKAGE_NAME));
				logger.info("Remove test package:"
						+ myDevice.removePackage(TEST_PACKAGE_NAME));
			} else
				logger.info("No need to remove package"
						+ myDevice.shell("am force-stop " + APP_PACKAGE_NAME));
			releaseAvailability();
		}
	}

	private String constructCmd() {
		StringBuilder testCmd = new StringBuilder(ADB_LOCATION + " ");
		testCmd.append("-s ");
		testCmd.append(getDeviceName() + " ");
		testCmd.append("shell tcpdump ");
		testCmd.append("-p -vv -s 0 -w ");
		testCmd.append("/sdcard/capture.pcap");
		return testCmd.toString();
	}

	private IChimpDevice getDevice() {
		// TODO Auto-generated method stub
		return PAIR.getDevice();
	}

	private String getDeviceName() {
		// TODO Auto-generated method stub
		return PAIR.getDeviceName();
	}

	private void suspendAvailability() {
		logger.info("Test starts. Device suspended.");
		PAIR.availability = false;
	}

	private void releaseAvailability() {
		logger.info("Test ends. Device released.");
		PAIR.availability = true;
	}

	public static class Builder {
		// Mandatory Parameters
		private final String TEST_PACKAGE_NAME;
		private final NameDevicePair PAIR;

		// Optional Parameters
		private int testDurationThres = 999999;
		private String appInstallPath;
		private String testInstallPath;
		private boolean clearHistory = true;

		public Builder(String TEST_PACKAGE_NAME, NameDevicePair pair) {
			Pattern p = Pattern.compile("((\\w+)\\.)+test");
			if (p.matcher(TEST_PACKAGE_NAME).matches())
				this.TEST_PACKAGE_NAME = TEST_PACKAGE_NAME;
			else
				throw new IllegalArgumentException(
						"TEST PACKAGE NAME Parameter Illegal");
			if (pair.availability)
				this.PAIR = pair;
			else
				throw new IllegalArgumentException("Test Device is in use.");
		}

		public Builder testDurationThres(int durthr) {
			if (durthr > 0) {
				this.testDurationThres = durthr;
				return this;
			} else
				throw new IllegalArgumentException(
						"Test Duration Threshold Parameter Illegal");
		}

		public Builder clearHistory(boolean clhis) {
			this.clearHistory = clhis;
			return this;
		}

		public Builder appInstallPath(String apinpa) {
			// windows c:/test.apk or linux ~/test.apk or ../test.apk or
			// abc/test.apk or /test.apk
			Pattern p = Pattern
					.compile("(([a-zA-Z]:)|~|(\\.\\.)|(\\w|-)*)/((\\w|-)+/)*(\\w|-)+\\.apk");
			if (p.matcher(apinpa).matches()) {
				this.appInstallPath = apinpa;
				return this;
			} else
				throw new IllegalArgumentException(
						"App Install Path Parameter Illegal");
		}
		
		public Builder testInstallPath(String tipa){
			// windows c:/test.apk or linux ~/test.apk or ../test.apk or
			// abc/test.apk or /test.apk
			Pattern p = Pattern
					.compile("(([a-zA-Z]:)|~|(\\.\\.)|(\\w|-)*)/((\\w|-)+/)*(\\w|-)+\\.apk");
			if (p.matcher(tipa).matches()) {
				this.testInstallPath = tipa;
				return this;
			} else
				throw new IllegalArgumentException(
						"Test Install Path Parameter Illegal");
		}
		
		public PacketTest build() {
			return new PacketTest(this);
		}

	}

}
