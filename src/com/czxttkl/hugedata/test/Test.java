package com.czxttkl.hugedata.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.android.chimpchat.core.IChimpDevice;
import com.czxttkl.hugedata.helper.DeviceInfo;
import com.czxttkl.hugedata.helper.LogFormatter;

public class Test {
	//Paramters set in static methods
	public static String ADB_LOCATION;
	public static int LOCATION_NUM;
	public static Logger logger;

	// Mandatory Parameters
	public String TEST_PACKAGE_NAME;
	public DeviceInfo DEVICE_INFO;
	public String TEST_START_TIME;
	
	public static void setAdbLocation(String adbLocation) {
		// windows c:/adb or linux ~/adb or ../adb or a-bc/adb or /adb
		// No need to append ".exe"
		Pattern p = Pattern
				.compile("(([a-zA-Z]:)|~|(\\.\\.)|(\\w|-)*)/((\\w|-)+/)*adb");
		if (p.matcher(adbLocation).matches()) {
			ADB_LOCATION = adbLocation;
		} else
			throw new IllegalArgumentException("Adb Location Parameter Illegal");
	}

	public static void setTestLocation(int locationNum) {
		if (locationNum >= 100000 && locationNum <= 999999)
			LOCATION_NUM = locationNum;
		else
			throw new IllegalArgumentException(
					"Location Number Parameter Illegal");
	}
	
	public static void setLogger(Logger lg){
		logger = lg;
	}

	
	/**
	 * Install the package to the phone.
	 * @param installPath
	 *            the install path of the package
	 * @param installType
	 *            the type for install(APP or Test)
	 */
	public static void installPackage(IChimpDevice me, String installPath, String installType) {
		if (installPath != null) {
			if (me.installPackage(installPath))
				logger.info(installType + " install successfully:" + installPath);
			else
				logger.info(installType + " install failed:" + installPath);
		} else
			logger.info("No need to install " + installType + " package");
	}
	
	/**
	 * Remove the package from the phone
	 * @param packageName
	 *            the name of the package to be removed
	 * @param removeType
	 *            the type for the removing package(APP or Test)
	 */
	public static void removePackage(IChimpDevice me, String packageName, String removeType) {
		if (me.removePackage(packageName))
			logger.info("Remove " + removeType + " package successfully.");
		else
			logger.info("Remove " + removeType + " package failed.");
	}
	
	/**
	 * Pull Screenshots Images from /sdcard/Robotium-Screenshots and then
	 * delete the whole folder
	 */
	public static void pullScreenshots(IChimpDevice me, String adbName, String dir) {
		// TODO Auto-generated method stub
		StringBuilder cmd = new StringBuilder(ADB_LOCATION + " ");
		cmd.append("-s ");
		cmd.append(adbName + " ");
		cmd.append("pull ");
		cmd.append("/sdcard/Robotium-Screenshots ");
		cmd.append(dir);
		
		try {
			 Process p = Runtime.getRuntime().exec(cmd.toString());
			 //wait for pulling images out
			 p.waitFor();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.info("IOException" + e.toString());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			logger.info("InterruptedException" + e.toString());
		}
		
		me.shell("rm -r /sdcard/Robotium-Screenshots");
		logger.info("Pull Screenshots Successfully.");
	}
}
