package com.czxttkl.hugedata.analyze;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.czxttkl.hugedata.server.RunnerServer;
import com.czxttkl.hugedata.server.TaskListener.TaskListenerHandler;

public abstract class TestAnalyzer {

	public static Logger logger = Logger
			.getLogger(RunnerServer.class.getName());

	public String resultDirStr;
	public static final ResourceBundle resultBundle = ResourceBundle.getBundle(
			"ResultBundle", Locale.CHINA);
	public TaskListenerHandler taskListenerHandler;
	public Document doc;

	public volatile boolean notified = false;

	/**
	 * Return keyvalue in ResultBundle.properties. By default, JDK reads
	 * keyvalue using ISO-8859-1
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
			while(!notified)
				wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public synchronized void notifyForTestFinish() {
		notified = true;
		notify();
		
	}

	public void appendPublicMetrics() throws IOException {
		System.out.println("appendPublicMetrics");
		File html = new File("html/template/index.html");
		doc = Jsoup.parse(html, "UTF-8");

		Element title = doc.getElementById("title");
		System.out.println(title.text() + getStringValue("html.title"));

		// title.html(getStringValue("html.title"));
		// title.text(getStringValue("html.title"));
		// title.append(getStringValue("html.title"));
		// title.appendText(getStringValue("html.title"));
		title.appendText(getStringValue("html.title"));

		Element publicinfo = doc.getElementById("publicinfo");
		publicinfo.appendText(resultBundle.getString("html.title"));
		// title.prependText(getStringValue("html.title"));
	}

	public abstract void appendPrivateMetrics() throws UnsatisfiedLinkError,
			IOException;

}
