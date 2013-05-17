package com.czxttkl.hugedata.analyze;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.ResourceBundle;

import com.czxttkl.hugedata.helper.StreamTool;
import com.czxttkl.hugedata.server.TaskListener.TaskListenerHandler;

public class PacketTestAnalyzer implements Runnable {

	ResourceBundle resultBundle;
	TaskListenerHandler taskListenerHandler;

	public PacketTestAnalyzer(TaskListenerHandler taskListenerHandler) {
		this.taskListenerHandler = taskListenerHandler;
		// System.out.println(supportedLocales[0].getLanguage());
		resultBundle = ResourceBundle.getBundle("ResultBundle", Locale.CHINA);
	}

	public void run() {
		waitForTestFinish();
		System.out.println("generatePdf");
		generatePdf();

	}

	private void generatePdf() {
		taskListenerHandler.responseClient(
				StreamTool.stringToByteBuffer("PDF", "UTF-8"), true);
		String keyValue = null;
		try {
			keyValue = new String(resultBundle.getString("cached").getBytes(
					"ISO-8859-1"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(keyValue);
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
