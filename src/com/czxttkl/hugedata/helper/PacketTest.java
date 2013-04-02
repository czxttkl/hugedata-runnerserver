package com.czxttkl.hugedata.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

	public static void setLogger(String logFilePath, boolean appendLog)
			throws SecurityException, IOException {
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
		String testCmd = constructTcpdumpCmd();
		try {
			/*
			 * BufferedReader dumpResult = new BufferedReader( new
			 * InputStreamReader(tcpdump.getInputStream())); String s; while ((s
			 * = dumpResult.readLine()) != null) System.out.println(s);
			 */
			installPackage(APP_INSTALL_PATH, "APP");
			installPackage(TEST_INSTALL_PATH, "Test");

			Process tcpdump = Runtime.getRuntime().exec(testCmd);
			analyzeResult(myDevice.startTestInstrumentation(TEST_PACKAGE_NAME,
					TEST_DURATION_THRESHOLD));

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
			releaseAvailability();
		}
	}

	private void analyzeResult(String testResult) {
		// TODO Auto-generated method stub
		logger.info("Test Instrumentation finished");
		int totalProcedure;
		double testTime;
		
		Scanner resultScanner = new Scanner(testResult);
		while (resultScanner.hasNextLine()) {
			String line = resultScanner.nextLine();
			
			//filter unrelated lines
			if(line.length()<2)
				continue;
			
			if (line.startsWith("Failure in testProcedure")) {
				int procedureNum = Integer.valueOf(line.substring(24).split(":")[0]);
				System.out.println(procedureNum);
				
				while (resultScanner.hasNextLine()){
					String inLine = resultScanner.nextLine();
					if(inLine.length()<2)
						break;
					System.out.println(inLine);
				}
			}

			if (line.startsWith("Time: ")) {
				testTime=Double.valueOf(line.substring(6));
				System.out.println(testTime);
			}
			
			if(line.startsWith("OK (")){
				totalProcedure = Integer.valueOf(line.substring(4).split(" ")[0]);
				System.out.println(totalProcedure);
			}
			
			if(line.startsWith("Tests run: ")){
				totalProcedure = Integer.valueOf(line.split(",")[0].split(" ")[2]);
				System.out.println(totalProcedure);
			}

		}
		resultScanner.close();
		logger.info(testResult);
	}

	
	private String constructTcpdumpCmd() {
		StringBuilder testCmd = new StringBuilder(ADB_LOCATION + " ");
		testCmd.append("-s ");
		testCmd.append(getDeviceName() + " ");
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
			Pattern p = Pattern.compile("(([a-zA-Z]+)\\.)+test");
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
