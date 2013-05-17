package com.czxttkl.hugedata.analyze;

import java.io.UnsupportedEncodingException;
import java.util.ResourceBundle;

import com.czxttkl.hugedata.server.TaskListener.TaskListenerHandler;

public class TestAnalyzer {

	ResourceBundle resultBundle;
	TaskListenerHandler taskListenerHandler;

	/**
	 * Return keyvalue in ResultBundle.properties. By default, JDK reads
	 * keyvalue using ISO-8859
	 * 
	 * @param key
	 * @return
	 */
	public String getStringValue(String key) {
		String keyValue = null;
		try {
			keyValue = new String(resultBundle.getString(key).getBytes(
					"ISO-8859-1"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return keyValue;
	}

	public synchronized void waitForTestFinish() {
		try {
			wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public synchronized void notifyForTestFinish() {
		notify();
	}
}