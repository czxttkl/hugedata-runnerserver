package com.czxttkl.hugedata.test;

import java.io.IOException;
import java.util.HashMap;
import com.android.chimpchat.adb.AdbBackend;
import com.czxttkl.hugedata.helper.NameDevicePair;
import com.czxttkl.hugedata.helper.Test;

public class SampleMonkey {

	private static final String ANDROID_SDK_HOME = "c:/Android";
	private static final String ADB_LOCATION = "c:/Android/platform-tools/adb.exe";
	private static final String APP_PACKAGE_NAME = "com.renren.mobile.android";
	private static final int ADB_CONNECTION_WAITTIME_THRESHOLD = 5000;
	private static HashMap<String, NameDevicePair> deviceInfoMap = new HashMap<String, NameDevicePair>();
	private static AdbBackend adbBackend;

	public static void main(String[] args) throws InterruptedException,
			IOException {
		// TODO Auto-generated method stub

		init();

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

		} catch (IllegalArgumentException e) {
			e.printStackTrace();
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

	private static void init() {
		// TODO Auto-generated method stub
		adbBackend = new AdbBackend(ADB_LOCATION, false);
		deviceInfoMap.put(
				"HTCT328W",
				new NameDevicePair("HC29GPG09471", adbBackend
						.waitForConnection(ADB_CONNECTION_WAITTIME_THRESHOLD,
								"HC29GPG09471")));
		Test.setAdbLocation("c:/Android/platform-tools/adb");

	}

}
