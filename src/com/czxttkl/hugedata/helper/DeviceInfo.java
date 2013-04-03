package com.czxttkl.hugedata.helper;

import java.util.Scanner;

import com.android.chimpchat.core.IChimpDevice;

public class DeviceInfo {
	private String manufacturer;
	private String type;
	private String adbName;
	private IChimpDevice me;
	private String platformName;
	private String platformVer;
	
	public boolean availability;
	
	public DeviceInfo(String manufacturer, String type, String adbName, IChimpDevice me) {
		this.manufacturer = manufacturer;
		this.type = type;
		this.adbName = adbName;
		if (me != null) {
			this.me = me;
			availability = true;
		}
		platformName = "AND";
		platformVer = setPlatformVer(me);
	}

	private String setPlatformVer(IChimpDevice device) {
		// TODO Auto-generated method stub
		//Scanner ver = new Scanner(device.shell("getprop ro.build.version.release"));
		//return ver.nextLine();
		return device.shell("getprop ro.build.version.release").trim();
	}

	public String getManufacturer() {
		return this.manufacturer;
	}
	
	public String getType(){
		return this.type;
	}
	
	public String getAdbName() {
		return this.adbName;
	}

	public IChimpDevice getDevice() {
		return this.me;
	}
	
	public String getPlatformName(){
		return this.platformName;
	}
	
	public String getPlatformVer(){
		return this.platformVer;
	}
}