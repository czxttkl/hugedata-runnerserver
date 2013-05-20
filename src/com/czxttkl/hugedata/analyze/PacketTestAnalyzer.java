package com.czxttkl.hugedata.analyze;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.ResourceBundle;

import com.att.aro.main.ApplicationResourceOptimizer;
import com.czxttkl.hugedata.helper.StreamTool;
import com.czxttkl.hugedata.server.TaskListener.TaskListenerHandler;

public class PacketTestAnalyzer extends TestAnalyzer implements Runnable {

	public PacketTestAnalyzer(String resultDirStr,
			TaskListenerHandler taskListenerHandler) {
		this.resultDirStr = resultDirStr;
		this.taskListenerHandler = taskListenerHandler;
		// System.out.println(supportedLocales[0].getLanguage());

	}

	public void run() {

		try {
			appendPublicMetrics();
			waitForTestFinish();
			appendPrivateMetrics();
			generateHtml();
			logger.info("Generate Html Successfully for Test:" + resultDirStr);
		} catch (Exception e) {
			logger.info("Generate Html Failed. Caused by:" + e.getCause());
			e.printStackTrace();
		}

	}

	private void generateHtml() throws UnsatisfiedLinkError, IOException {

		// StreamTool.copyFolder("html/template", resultDirStr + "/html/");
		// StreamTool.copyFile("html/template/index.html", resultDirStr +
		// "/html/index.html");
		if (doc != null) {
			FileOutputStream fos = new FileOutputStream(resultDirStr
					+ "/html/index.html", false);
			fos.write(doc.html().getBytes());
			fos.close();
		}
		
		if (taskListenerHandler != null) {
			ByteBuffer byteBuf = StreamTool.stringToByteBuffer("EndTest:"
					+ resultDirStr, "UTF-8");
			taskListenerHandler.responseClient(byteBuf, true);
		}

	}

	@Override
	public void appendPrivateMetrics() throws UnsatisfiedLinkError, IOException {

		final ApplicationResourceOptimizer aro = new ApplicationResourceOptimizer();
		File pcapFile = new File(resultDirStr + "/capture.pcap");
		aro.openPcap(pcapFile);



	}

}
