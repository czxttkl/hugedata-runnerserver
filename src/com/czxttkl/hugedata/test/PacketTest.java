package com.czxttkl.hugedata.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.android.chimpchat.core.IChimpDevice;
import com.android.chimpchat.core.PhysicalButton;
import com.android.chimpchat.core.TouchPressType;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.czxttkl.hugedata.helper.LogFormatter;
import com.czxttkl.hugedata.helper.DeviceInfo;
import com.czxttkl.hugedata.helper.ResultAnalyzer;

public class PacketTest extends Test implements Runnable {
	// Parameters set in the static methods
	public static Logger logger = Logger.getLogger(PacketTest.class.getName());

	// Mandatory Parameters
	public final String TEST_PACKAGE_NAME;
	public final DeviceInfo DEVICE_INFO;
	public final String TEST_START_TIME;
	public final String NETWORK;

	// Optional Parameters
	private final String APP_PACKAGE_NAME;
	private final int TEST_DURATION_THRESHOLD;
	private final String APP_INSTALL_PATH;
	private final String TEST_INSTALL_PATH;
	private final boolean CLEAR_HISTORY;

	public String resultDirStr;
	private File resultDir;

	private PacketTest(Builder builder) {
		TEST_PACKAGE_NAME = builder.TEST_PACKAGE_NAME;
		DEVICE_INFO = builder.DEVICE_INFO;
		TEST_START_TIME = builder.TEST_START_TIME;
		NETWORK = builder.NETWORK;

		TEST_DURATION_THRESHOLD = builder.testDurationThres;
		APP_INSTALL_PATH = builder.appInstallPath;
		TEST_INSTALL_PATH = builder.testInstallPath;
		APP_PACKAGE_NAME = TEST_PACKAGE_NAME.substring(0,
				TEST_PACKAGE_NAME.length() - 5);
		CLEAR_HISTORY = builder.clearHistory;

		resultDirStr = LOCATION_NUM + DEVICE_INFO.getManufacturer()
				+ DEVICE_INFO.getType() + NETWORK + TEST_START_TIME;
		resultDir = new File(resultDirStr);
		resultDir.mkdir();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

		logger.info("Test starts. Device suspended.");
		IChimpDevice myDevice = getDevice();
		String testCmd = constructTcpdumpCmd();
		try {
			installPackage(APP_INSTALL_PATH, "APP");
			installPackage(TEST_INSTALL_PATH, "Test");

			Process tcpdump = Runtime.getRuntime().exec(testCmd);

			ResultAnalyzer.analyze(this, myDevice.startTestInstrumentation(
					TEST_PACKAGE_NAME, TEST_DURATION_THRESHOLD));

			logger.info("Tcpdump has been killed."
					+ myDevice.shell("busybox pkill -SIGINT tcpdump"));

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ShellCommandUnresponsiveException e) {
			logger.info("Test timed out. ");
		} catch (IllegalArgumentException e) {
			logger.info("Install failed. Caused by " + e.getMessage());
		} finally {
			if (CLEAR_HISTORY) {
				removePackage(APP_PACKAGE_NAME, "APP");
				removePackage(TEST_PACKAGE_NAME, "Test");
			} else
				logger.info("No need to remove package"
						+ myDevice.shell("am force-stop " + APP_PACKAGE_NAME));
			if (releaseDevice())
				logger.info("Test ends. Device released.");
		}
	}

	private String constructTcpdumpCmd() {
		StringBuilder testCmd = new StringBuilder(ADB_LOCATION + " ");
		testCmd.append("-s ");
		testCmd.append(getAdbName() + " ");
		testCmd.append("shell tcpdump ");
		testCmd.append("-p -vv -s 0 -w ");
		testCmd.append("/sdcard/capture.pcap");
		return testCmd.toString();
	}

	/**
	 * @param installPath
	 *            the install path of the package
	 * @param installType
	 *            the type for install(APP or Test)
	 */
	private void installPackage(String installPath, String installType) {
		if (installPath != null) {
			if (getDevice().installPackage(installPath))
				logger.info("Install " + installType + " package successfully");
			else
				throw new IllegalArgumentException(installType
						+ " Install Path Parameter Illegal");
		} else
			logger.info("No need to install " + installType + " package");
	}

	/**
	 * @param packageName
	 *            the name of the package to be removed
	 * @param removeType
	 *            the type for the removing package(APP or Test)
	 */
	private void removePackage(String packageName, String removeType) {
		if (getDevice().removePackage(packageName))
			logger.info("Remove " + removeType + " package successfully.");
		else
			logger.info("Remove " + removeType + " package failed.");
	}

	private IChimpDevice getDevice() {
		// TODO Auto-generated method stub
		return DEVICE_INFO.getDevice();
	}

	private String getAdbName() {
		// TODO Auto-generated method stub
		return DEVICE_INFO.getAdbName();
	}

	private boolean suspendDevice() {
		return DEVICE_INFO.suspendDevice();
	}

	private boolean releaseDevice() {
		return DEVICE_INFO.releaseDevice();
	}

	public static class Builder {
		// Mandatory Parameters
		private final String TEST_PACKAGE_NAME;
		private final DeviceInfo DEVICE_INFO;
		private final String TEST_START_TIME;
		private final String NETWORK;

		// Optional Parameters
		private int testDurationThres = 999999;
		private String appInstallPath;
		private String testInstallPath;
		private boolean clearHistory = true;

		public Builder(String TEST_PACKAGE_NAME, DeviceInfo deviceinfo,
				String network) {
			// Validate Test Package Name
			Pattern p = Pattern.compile("(([a-zA-Z]+)\\.)+test");
			if (p.matcher(TEST_PACKAGE_NAME).matches())
				this.TEST_PACKAGE_NAME = TEST_PACKAGE_NAME;
			else
				throw new IllegalArgumentException(
						"TEST PACKAGE NAME Parameter Illegal");
			// The device instance
			this.DEVICE_INFO = deviceinfo;
			// Initialize the test start time for logger
			this.TEST_START_TIME = new SimpleDateFormat("yyyyMMddHHmmss")
					.format(new Date()).toString();
			// The network the device uses
			this.NETWORK = network;
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

		public Builder testInstallPath(String tipa) {
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
