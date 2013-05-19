package com.czxttkl.hugedata.analyze;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.ResourceBundle;

import com.att.aro.main.ApplicationResourceOptimizer;
import com.czxttkl.hugedata.helper.StreamTool;
import com.czxttkl.hugedata.server.TaskListener.TaskListenerHandler;

public class PacketTestAnalyzer extends TestAnalyzer implements Runnable {

	public PacketTestAnalyzer(String resultDirStr, TaskListenerHandler taskListenerHandler) {
		this.resultDirStr = resultDirStr;
		this.taskListenerHandler = taskListenerHandler;
		// System.out.println(supportedLocales[0].getLanguage());
		
	}

	public void run() {
		System.out.println("generatePdf");
		try {
			generateHtml();
		} catch (Exception e) {
			logger.info("Generate Pdf Failed. Caused by:" + e.getCause());
		} 

	}

	private void generateHtml() throws UnsatisfiedLinkError, IOException {
		
		final ApplicationResourceOptimizer aro = new ApplicationResourceOptimizer();
		File pcapFile = new File(resultDirStr + "/capture.pcap");
		aro.openPcap(pcapFile);
		
		String keyValue = getStringValue("cached");
		System.out.println(keyValue);
		if (taskListenerHandler != null) {
			ByteBuffer byteBuf = StreamTool.stringToByteBuffer(
					"EndTest:" + resultDirStr, "UTF-8");
			taskListenerHandler.responseClient(byteBuf, true);
		}
		
		StreamTool.copyFile("html/index.html", resultDirStr + "/html/result.html");
		
	}



}
