package com.czxttkl.hugedata.test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.regex.Pattern;
import com.android.chimpchat.core.IChimpDevice;
import com.czxttkl.hugedata.analyze.PacketTestAnalyzer;
import com.czxttkl.hugedata.helper.DeviceInfo;
import com.czxttkl.hugedata.helper.ResultCollector;
import com.czxttkl.hugedata.helper.StreamTool;
import com.czxttkl.hugedata.server.RunnerServer;
import com.czxttkl.hugedata.server.TaskListener.TaskListenerHandler;

public class PacketTest extends Test implements Runnable {

	PacketTestAnalyzer packetTestAnalyzer = null;

	/**
	 * PacketTest should be constructed only by its builder.
	 * 
	 * @param builder
	 */
	private PacketTest(Builder builder) {
		// Mandatory parameters
		TEST_PACKAGE_NAME = builder.TEST_PACKAGE_NAME;
		DEVICE_INFO = builder.DEVICE_INFO;

		// Optional Parameters
		TEST_DURATION_THRESHOLD = builder.testDurationThres;
		APP_INSTALL_PATH = builder.appInstallPath;
		TEST_INSTALL_PATH = builder.testInstallPath;
		APP_PACKAGE_NAME = TEST_PACKAGE_NAME.substring(0,
				TEST_PACKAGE_NAME.length() - 5);
		CLEAR_HISTORY = builder.clearHistory;
		PRIORITY = builder.priority;
		PACKET_FILE_NAME = builder.packetFileName;
		TASK_LISTENER_HANDLER = builder.taskListenerHandler;

	}

	@Override
	public void run() {

		if (suspendDevice()) {
			IChimpDevice myDevice = getDevice();
			createResultDir();

			if (TASK_LISTENER_HANDLER != null) {
				ByteBuffer byteBuf = StreamTool.stringToByteBuffer("StartTest",
						"UTF-8");
				TASK_LISTENER_HANDLER.responseClient(byteBuf, true);

			}

			try {

				installPackage(myDevice, APP_INSTALL_PATH, "APP");
				installPackage(myDevice, TEST_INSTALL_PATH, "Test");
				startTcpdump();
				packetTestAnalyzer = ResultCollector.analyze(this, myDevice
						.startTestInstrumentation(TEST_PACKAGE_NAME,
								TEST_DURATION_THRESHOLD));

				logger.info("Test:" + resultDirStr + " succeeded.");

			} catch (Exception e) {
				logger.info("Test:" + resultDirStr + " failed. Caused by "
						+ e.getMessage());
			} finally {

				stopTcpdump();

				pullScreenshots(myDevice, getAdbName(), resultDirStr);

				/*
				 * Putting "PacketTestAnalyzer packetTestAnalyzer = new
				 * PacketTestAnalyzer( packetTest.resultDirStr,
				 * packetTest.TASK_LISTENER_HANDLER);" in
				 * resultCollector.anaylze() is better than in the finally block
				 * because I could use "if (packetTestAnalyzer != null)" to
				 * examine if the result has been collected successfully.
				 */
				if (packetTestAnalyzer != null)
					packetTestAnalyzer.notifyForTestFinish();

				if (CLEAR_HISTORY) {
					removePackage(myDevice, APP_PACKAGE_NAME, "APP");
					removePackage(myDevice, TEST_PACKAGE_NAME, "Test");
				} else
					logger.info("No need to remove package"
							+ myDevice.shell("am force-stop "
									+ APP_PACKAGE_NAME));

				releaseDevice();

			}// finally
		}// if suspenddevice
	}

	/**
	 * Start the tcpdump process
	 * 
	 * @throws IOException
	 */
	private void startTcpdump() throws IOException {
		String testCmd = constructTcpdumpCmd();
		Process tcpdump = Runtime.getRuntime().exec(testCmd);
	}

	/**
	 * Stop the tcpdump process
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void stopTcpdump() {
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
		} catch (Exception e) {
			logger.info("Stop Tcpdump failed. Caused by: " + e.getMessage());
		}
		// Remove the pcap file after pulling
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
		// By default, remove app completely after test
		private boolean clearHistory = true;
		// Default priority:1
		private int priority = 1;
		private String packetFileName = "capture.pcap";
		private TaskListenerHandler taskListenerHandler;

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

		public Builder taskListernerHandler(TaskListenerHandler tlh) {
			taskListenerHandler = tlh;
			return this;
		}

		public PacketTest build() {
			return new PacketTest(this);
		}

	}

}
