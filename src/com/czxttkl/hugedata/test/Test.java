package com.czxttkl.hugedata.test;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.czxttkl.hugedata.helper.LogFormatter;

public abstract class Test {
	public static String ADB_LOCATION;
	public static int LOCATION_NUM;
	public static Logger logger;

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

/*	public static void setLogger(Class[] testClasses, boolean appendLog)
			throws SecurityException, IOException {
		for (Class test : testClasses) {
			String logFilePath = test.getSimpleName() + ".log";
			LogFormatter logFormatter = new LogFormatter();
			Logger logger = Logger.getLogger(test.getName());
			logger.setLevel(Level.FINEST);
			FileHandler fileHandler = new FileHandler(logFilePath, appendLog);
			fileHandler.setFormatter(logFormatter);
			logger.addHandler(fileHandler);
		}
		FileHandler fileHandler = new FileHandler("RunnerServer.log", true);
		LogFormatter logFormatter = new LogFormatter();
		fileHandler.setFormatter(logFormatter);
		logger.addHandler(fileHandler);
		
		Logger logger = Logger.getLogger(test.getName());
		logger.setLevel(Level.FINEST);
		FileHandler fileHandler = new FileHandler(logFilePath, appendLog);
	}*/
	
	public static void setLogger(Logger lg){
		logger = lg;
	}

}
