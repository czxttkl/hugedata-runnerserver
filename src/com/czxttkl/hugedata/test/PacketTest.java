package com.czxttkl.hugedata.test;

import java.io.IOException;
import java.util.regex.Pattern;
import com.android.chimpchat.core.IChimpDevice;
import com.czxttkl.hugedata.helper.DeviceInfo;
import com.czxttkl.hugedata.helper.ResultAnalyzer;

public class PacketTest extends Test implements Runnable {

	private PacketTest(Builder builder) {
		TEST_PACKAGE_NAME = builder.TEST_PACKAGE_NAME;
		DEVICE_INFO = builder.DEVICE_INFO;

		TEST_DURATION_THRESHOLD = builder.testDurationThres;
		APP_INSTALL_PATH = builder.appInstallPath;
		TEST_INSTALL_PATH = builder.testInstallPath;
		APP_PACKAGE_NAME = TEST_PACKAGE_NAME.substring(0,
				TEST_PACKAGE_NAME.length() - 5);
		CLEAR_HISTORY = builder.clearHistory;
		PRIORITY = builder.priority;
		PACKET_FILE_NAME = builder.packetFileName;
	}

	@Override
	public void run() {

		if (suspendDevice()) {

			createResultDir();
			IChimpDevice myDevice = getDevice();

			try {
				installPackage(myDevice, APP_INSTALL_PATH, "APP");
				installPackage(myDevice, TEST_INSTALL_PATH, "Test");
				startTcpdump();
				ResultAnalyzer.analyze(this, myDevice.startTestInstrumentation(
						TEST_PACKAGE_NAME, TEST_DURATION_THRESHOLD));

			} catch (Exception e) {
				logger.info("Test:" + resultDirStr + " failed. Caused by "
						+ e.getMessage());
			} finally {

				stopTcpdump();
				pullScreenshots(myDevice, getAdbName(), resultDirStr);

				if (CLEAR_HISTORY) {
					removePackage(myDevice, APP_PACKAGE_NAME, "APP");
					removePackage(myDevice, TEST_PACKAGE_NAME, "Test");
				} else
					logger.info("No need to remove package"
							+ myDevice.shell("am force-stop "
									+ APP_PACKAGE_NAME));

				releaseDevice();

			}
		}
	}

	private void startTcpdump() throws IOException {
		// TODO Auto-generated method stub
		String testCmd = constructTcpdumpCmd();
		Process tcpdump = Runtime.getRuntime().exec(testCmd);
	}

	private void stopTcpdump() {
		// TODO Auto-generated method stub
		getDevice().shell("busybox pkill -SIGINT tcpdump");
		StringBuilder cmd = new StringBuilder(ADB_LOCATION + " ");
		cmd.append("-s ");
		cmd.append(getAdbName() + " ");
		cmd.append("pull ");
		cmd.append("/sdcard/hugedata/");
		cmd.append(PACKET_FILE_NAME + " ");
		cmd.append(resultDirStr);
		Process p;
		try {
			p = Runtime.getRuntime().exec(cmd.toString());
			p.waitFor();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.info("IOException" + e.toString());
		}
		// wait for pulling images out
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			logger.info("InterruptedException" + e.toString());
		}

		getDevice().shell("rm /sdcard/hugedata/" + PACKET_FILE_NAME);
		logger.info("Tcpdump has been killed.");

	}

	/**
	 * Construct the specified String command for starting tcpdump
	 * 
	 * @return the String command for starting tcpdump
	 */
	private String constructTcpdumpCmd() {
		StringBuilder cmd = new StringBuilder(ADB_LOCATION + " ");
		cmd.append("-s ");
		cmd.append(getAdbName() + " ");
		cmd.append("shell tcpdump ");
		cmd.append("-p -s 0 -w ");
		/*
		 * testCmd.append("-p -vv -s 0 "); -p: Not in Promiscuous Mode. So
		 * tcpdump will only capture packets that intends to be received -w :
		 * Write raw packets to file rather than printing them
		 * testCmd.append("-p -w "); -s 0 set snaplength 65535
		 */
		cmd.append("/sdcard/hugedata/");
		cmd.append(PACKET_FILE_NAME);
		return cmd.toString();
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
		return DEVICE_INFO.suspendDevice(this);
	}

	private boolean releaseDevice() {
		return DEVICE_INFO.releaseDevice(this);
	}

	public static class Builder {
		// Mandatory Parameters
		private final String TEST_PACKAGE_NAME;
		private final DeviceInfo DEVICE_INFO;

		// Optional Parameters
		private int testDurationThres = 999999;
		private String appInstallPath;
		private String testInstallPath;
		private boolean clearHistory = true;
		// default priority:1
		private int priority = 1;
		private String packetFileName = "capture.pcap";

		public Builder(String TEST_PACKAGE_NAME, DeviceInfo deviceinfo) {
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

		public Builder priority(int pr) {
			priority = pr;
			return this;
		}

		public Builder packetFileName(String name) {
			packetFileName = name;
			return this;
		}

		public PacketTest build() {
			return new PacketTest(this);
		}

	}

}
