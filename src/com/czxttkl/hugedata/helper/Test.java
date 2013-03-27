package com.czxttkl.hugedata.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.android.chimpchat.core.IChimpDevice;
import com.android.ddmlib.ShellCommandUnresponsiveException;

public class Test implements Runnable {

	public static String ADB_LOCATION;

	// Mandatory Parameters
	private final String TEST_PACKAGE_NAME;
	private final NameDevicePair PAIR;

	// Optional Parameters
	private final String APP_PACKAGE_NAME;
	private final int TEST_DURATION_THRESHOLD;
	private final String APP_INSTALL_PATH;
	private final boolean CLEAR_HISTORY;

	private Test(Builder builder) {
		TEST_PACKAGE_NAME = builder.TEST_PACKAGE_NAME;
		PAIR = builder.PAIR;

		TEST_DURATION_THRESHOLD = builder.testDurationThres;
		APP_INSTALL_PATH = builder.appInstallPath;
		APP_PACKAGE_NAME = TEST_PACKAGE_NAME.substring(0,
				TEST_PACKAGE_NAME.length() - 5);
		CLEAR_HISTORY = builder.clearHistory;
	}

	public static void setAdbLocation(String adbLocation) {
		// windows c:/adb or linux ~/adb or ../adb or a-bc/adb or /adb
		Pattern p = Pattern
				.compile("(([a-zA-Z]:)|~|(\\.\\.)|(\\w|-)*)/((\\w|-)+/)*adb");
		if (p.matcher(adbLocation).matches())
			Test.ADB_LOCATION = adbLocation;
		else
			throw new IllegalArgumentException();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		suspendAvailability();

		IChimpDevice myDevice = getDevice();
		String testCmd = constructCmd();

		try {
			Process tcpdump = Runtime.getRuntime().exec(testCmd);
			BufferedReader dumpResult = new BufferedReader(new InputStreamReader(tcpdump.getInputStream()));
			
			
			if (APP_INSTALL_PATH != null)
				System.out.println("Install package:"
						+ myDevice.installPackage(APP_INSTALL_PATH));
			else
				System.out.println("No need to install package");

			System.out.println(myDevice.startTestInstrumentation(
					TEST_PACKAGE_NAME, TEST_DURATION_THRESHOLD));

			System.out.println("Test finished");

			System.out.println("Tcpdump killed:"
					+ myDevice.shell("busybox pkill -SIGINT tcpdump"));

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ShellCommandUnresponsiveException e) {
			System.out.println("Test timed out. ");
		} finally {
			if (CLEAR_HISTORY)
				System.out.println("Remove package:"
						+ myDevice.removePackage(APP_PACKAGE_NAME));
			else
				System.out.println("No need to remove package"
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
		System.out.println("Test starts. Device suspended.");
		PAIR.availability = false;
	}

	private void releaseAvailability() {
		System.out.println("Test ends. Device released.");
		PAIR.availability = true;
	}

	public static class Builder {
		// Mandatory Parameters
		private final String TEST_PACKAGE_NAME;
		private final NameDevicePair PAIR;

		// Optional Parameters
		private int testDurationThres = 999999;
		private String appInstallPath;
		private boolean clearHistory;

		public Builder(String TEST_PACKAGE_NAME, NameDevicePair pair) {
			Pattern p = Pattern.compile("((\\w+)\\.)+test");
			if (p.matcher(TEST_PACKAGE_NAME).matches())
				this.TEST_PACKAGE_NAME = TEST_PACKAGE_NAME;
			else
				throw new IllegalArgumentException();
			this.PAIR = pair;
		}

		public Builder testDurationThres(int durthr) {
			if (durthr > 0) {
				this.testDurationThres = durthr;
				return this;
			} else
				throw new IllegalArgumentException();
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
				throw new IllegalArgumentException();
		}

		public Test build() {
			return new Test(this);
		}

	}

}
