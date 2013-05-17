package com.czxttkl.hugedata.analyze;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.ResourceBundle;

import com.czxttkl.hugedata.helper.StreamTool;
import com.czxttkl.hugedata.server.TaskListener.TaskListenerHandler;

public class PacketTestAnalyzer extends TestAnalyzer implements Runnable {

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
		
		String keyValue = getStringValue("cached");
		System.out.println(keyValue);
		if (taskListenerHandler != null) {
			ByteBuffer byteBuf = StreamTool.stringToByteBuffer(
					"EndTest", "UTF-8");
			taskListenerHandler.responseClient(byteBuf, true);
		}
	}



}
