package com.czxttkl.hugedata.helper;

import com.android.chimpchat.core.IChimpDevice;

public class NameDevicePair {

	private String name;
	private IChimpDevice me;
	public boolean availability;

	public NameDevicePair() {

	}

	public NameDevicePair(String name, IChimpDevice me) {
		this.name = name;
		if (me != null) {
			this.me = me;
			availability = true;
		}
	}

	public String getDeviceName() {
		return this.name;
	}

	public IChimpDevice getDevice() {
		return this.me;
	}
}