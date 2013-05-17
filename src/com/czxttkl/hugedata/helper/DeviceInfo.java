package com.czxttkl.hugedata.helper;

import java.net.InetAddress;
import java.util.Scanner;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.android.chimpchat.core.IChimpDevice;
import com.czxttkl.hugedata.server.RunnerServer;
import com.czxttkl.hugedata.server.TaskListener.TaskListenerHandler;
import com.czxttkl.hugedata.test.Test;

public class DeviceInfo implements Runnable {

	public static Logger logger = Logger
			.getLogger(RunnerServer.class.getName());

	private final String manufacturer;
	private final String type;
	private final String network;
	private final String adbName;
	private final IChimpDevice me;
	private final String platformName;
	private final String platformVer;

	private volatile boolean availability;
	public PriorityBlockingQueue<Test> testQueue = new PriorityBlockingQueue<Test>();

	public DeviceInfo(String manufacturer, String type, String network,
			String adbName, IChimpDevice me) {
		this.manufacturer = manufacturer;
		this.type = type;
		this.network = network;
		this.adbName = adbName;
		this.me = me;
		this.availability = true;
		this.platformName = "AND";
		this.platformVer = setPlatformVer();
	}

	private String setPlatformVer() {
		// TODO Auto-generated method stub
		// Scanner ver = new
		// Scanner(device.shell("getprop ro.build.version.release"));
		// return ver.nextLine();
		return me.shell("getprop ro.build.version.release").trim();
	}

	public String getManufacturer() {
		return this.manufacturer;
	}

	public String getType() {
		return this.type;
	}

	public String getNetwork() {
		return this.network;
	}

	public String getAdbName() {
		return this.adbName;
	}

	public IChimpDevice getDevice() {
		return this.me;
	}

	public String getPlatformName() {
		return this.platformName;
	}

	public String getPlatformVer() {
		return this.platformVer;
	}

	public synchronized boolean suspendDevice(Test test) {
		if (availability) {
			availability = false;
			logger.info("Test:" + test.resultDirStr + " with priority"
					+ test.PRIORITY + " starts. " + this + " device suspended.");
			return true;
		} else {
			logger.info("Test:" + test.resultDirStr + " with priority"
					+ test.PRIORITY + " can't start. " + this
					+ " device is not released.");
			return false;
		}
	}

	public synchronized boolean releaseDevice(Test test) {
		if (!availability) {
			availability = true;
			logger.info("Test:" + Test.LOCATION_NUM + getManufacturer()
					+ getType() + getNetwork() + test.TEST_START_TIME
					+ test.getClass().getSimpleName() + " with priority"
					+ test.PRIORITY + " ends. " + this + " device released.");
			return true;
		} else {
			logger.info("Test:" + Test.LOCATION_NUM + getManufacturer()
					+ getType() + getNetwork() + test.TEST_START_TIME
					+ test.getClass().getSimpleName() + " with priority"
					+ test.PRIORITY + " ends. " + this
					+ " device is suspended.");
			return false;
		}
	}

	// Read operation doesn't need synchronized keyword
	public boolean isAvailable() {
		return availability;
	}

	public String getIpAddress() {
		Matcher m = Pattern.compile("([0-9]{1,3}.){3}[0-9]{1,3}").matcher(
				me.shell("ifconfig rmnet0"));
		if (m.find())
			return m.group().trim();
		else
			return null;
	}

	public String getPrimeDns() {
		return me.shell("getprop net.dns1").trim();
	}

	public String getSecondaryDns() {
		return me.shell("getprop net.dns2").trim();
	}

	public void addToTestQueue(Test test) {
		testQueue.add(test);
		logger.info("Test:" + Test.LOCATION_NUM + getManufacturer() + getType()
				+ getNetwork() + " with priority" + test.PRIORITY + " added. ");
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return manufacturer + type + network;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (!Thread.interrupted())
			try {
				testQueue.take().run();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

	}
}