package com.czxttkl.hugedata.analyze;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.jsoup.nodes.Element;

import com.att.aro.main.ApplicationResourceOptimizer;
import com.czxttkl.hugedata.helper.StreamTool;
import com.czxttkl.hugedata.server.TaskListener;
import com.czxttkl.hugedata.server.TaskListener.TaskListenerHandler;

public class PacketTestAnalyzer extends TestAnalyzer implements Runnable {

	int totalProcedure;
	List<Integer> failureProcedureNums;

	public PacketTestAnalyzer(String resultDirStr,
			TaskListenerHandler taskListenerHandler,
			HashMap<String, String> publicMetrics, int totalProcedure,
			List<Integer> failureProcedureNums) throws IOException {
		super(resultDirStr, taskListenerHandler, publicMetrics);
		this.totalProcedure = totalProcedure;
		this.failureProcedureNums = failureProcedureNums;
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

		String[] procedureNames = { "输入账号密码并登陆人人网", "首次载入应用刷新新鲜事",
				"发送一条Hello World新鲜事" };
		String[] procedureDetails = {
				"登陆成功后将出现人人网提示新功能的界面（而非直接出现新鲜事列表）。设置阈值时长为10秒。",
				"刷新新鲜事列表，判定标准为刷新图标由动态滚动变为静态。设定阈值时长为10秒。",
				"点击桌面菜单的发状态图标后，进入发状态界面。输入Hello World后点击发送。设定阈值时间为15秒。" };
		
		File dir = new File(resultDirStr);
		String[] screenshots = dir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if (name.startsWith("test_"))
					return true;
				else
					return false;
			}
		});
		
		
		final ApplicationResourceOptimizer aro = new ApplicationResourceOptimizer();
		File pcapFile = new File(resultDirStr + "/capture.pcap");
		aro.openPcap(pcapFile);

		Element body = doc.body();
		body.appendElement("h2").attr("id", "subtitle")
				.text(getStringValue("html.subtitle.packettest"));
		Element accordion = body.appendElement("ul").attr("class", "accordion");
		Element accordionLi = accordion.appendElement("li");

		String procedureLevel1;
		if (failureProcedureNums.size() == 0)
			procedureLevel1 = "passlevel1";
		else if (failureProcedureNums.size() < totalProcedure)
			procedureLevel1 = "partlypasslevel1";
		else
			procedureLevel1 = "faillevel1";
		accordionLi
				.appendElement("a")
				.attr("class", procedureLevel1)
				.text(getStringValue("html.title.procedure.completion") + "  ("
						+ getStringValue("html.title.procedure.completion1")
						+ totalProcedure
						+ getStringValue("html.title.procedure.completion2")
						+ (totalProcedure - failureProcedureNums.size())
						+ getStringValue("html.title.procedure.completion3") + ")");

		Element accordionLiUl = accordionLi.appendElement("ul");

		for (int i = 0; i < totalProcedure; i++) {
			Element accordionLiUlLi = accordionLiUl.appendElement("li");
			if (i == 0)
				accordionLiUlLi.attr("class", "current");

			Element a = accordionLiUlLi.appendElement("a").text(procedureNames[i]);
			if (failureProcedureNums.contains(i + 1))
				a.attr("class", "faillevel2");
			else
				a.attr("class", "passlevel2");

			Element frame = accordionLiUlLi.appendElement("div").attr("class",
					"frame");
			
			Element procedure = frame.appendElement("div").attr("class",
					"procedure");

			procedure.appendElement("b").text(
					getStringValue("html.procedure.name"));
			procedure.appendText(procedureNames[i]);
			procedure.appendElement("br");
			procedure.appendElement("br");

			procedure.appendElement("b").text(
					getStringValue("html.procedure.succeess.rate"));
			procedure.appendText("N/A");
			procedure.appendElement("br");
			procedure.appendElement("br");

			procedure.appendElement("b").text(
					getStringValue("html.procedure.detail"));
			procedure.appendText(procedureDetails[i]);
			procedure.appendElement("br");
			procedure.appendElement("br");
			
			frame.appendElement("img").attr("class","screenshot").attr("src","../" + screenshots[i]);

		}//for

	}
}
