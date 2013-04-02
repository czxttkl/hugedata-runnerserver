package com.czxttkl.hugedata.helper;

import com.android.chimpchat.core.IChimpDevice;

public class DeviceInfo {
	private String manufacturer;
	private String type;
	private String adbName;
	private IChimpDevice me;
	public boolean availability;

	public DeviceInfo(String manufacturer, String type, String adbName, IChimpDevice me) {
		this.manufacturer = manufacturer;
		this.type = type;
		this.adbName = adbName;
		if (me != null) {
			this.me = me;
			availability = true;
		}
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
}