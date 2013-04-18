package com.czxttkl.hugedata.helper;

import java.net.InetAddress;
import java.util.Scanner;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.android.chimpchat.core.IChimpDevice;
import com.czxttkl.hugedata.test.Test;

public class DeviceInfo implements Runnable {
	private String manufacturer;
	private String type;
	private String network;
	private String adbName;
	private IChimpDevice me;
	private String platformName;
	private String platformVer;

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

	public synchronized boolean suspendDevice() {
		if (availability) {
			availability = false;
			return true;
		} else
			return false;
	}

	public synchronized boolean releaseDevice() {
		if (!availability) {
			availability = true;
			return true;
		} else
			return false;
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
		System.out.println("test added");
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (!Thread.interrupted())
			try {
				testQueue.take().run();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	}
}