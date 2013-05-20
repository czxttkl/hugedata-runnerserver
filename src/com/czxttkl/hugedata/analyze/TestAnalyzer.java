package com.czxttkl.hugedata.analyze;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
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
    public HashMap<String, String> publicMetrics;
	
	public volatile boolean notified = false;
	
	public TestAnalyzer(String resultDirStr,
			TaskListenerHandler taskListenerHandler, HashMap<String, String> publicMetrics) throws IOException {
		
		this.resultDirStr = resultDirStr;
		this.taskListenerHandler = taskListenerHandler;
		this.publicMetrics = publicMetrics;
		
		File html = new File("html/template/index.html");
		doc = Jsoup.parse(html, "UTF-8");
		setTitle();
	}

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
		
		Element testType= doc.getElementById("testType");
		testType.text(getStringValue("html.test.type.packettest"));
		
		Element startTime = doc.getElementById("startTime");
		startTime.text(publicMetrics.get("StartTime"));
		
		Element duration= doc.getElementById("duration");
		duration.text(publicMetrics.get("Duration"));
		
		Element location= doc.getElementById("location");
		location.text(publicMetrics.get("Location"));
		
		Element phoneManufacturer = doc.getElementById("phoneManufacturer");
		phoneManufacturer.text(publicMetrics.get("PhoneManufacturer"));
		
		Element phoneType= doc.getElementById("phoneType");
		phoneType.text(publicMetrics.get("PhoneType"));
		
		Element platformName= doc.getElementById("platformName");
		platformName.text(publicMetrics.get("PlatformName"));
		
		Element platformVer = doc.getElementById("platformVer");
		platformVer.text(publicMetrics.get("PlatformVer"));
		
		Element network = doc.getElementById("network");
		network.text(publicMetrics.get("Network"));
		
		Element ipAddress = doc.getElementById("ipAddress");
		ipAddress.text(publicMetrics.get("IpAddress"));
		
		Element primeDns = doc.getElementById("primeDns");
		primeDns.text(publicMetrics.get("PrimeDns"));
		
		Element secondaryDns = doc.getElementById("secondaryDns");
		secondaryDns.text(publicMetrics.get("SecondaryDns"));
		
	}

	public void setTitle() {

		Element title = doc.getElementById("title");
		title.text(getStringValue("html.title"));
		
		Element testTypeTitle = doc.getElementById("testTypeTitle");
		testTypeTitle.text(getStringValue("html.test.type.title"));
		
		Element startTimeTile = doc.getElementById("startTimeTitle");
		startTimeTile.text(getStringValue("html.start.time.title"));
		
		Element durationTitle= doc.getElementById("durationTitle");
		durationTitle.text(getStringValue("html.duration.title"));
		
		Element locationTitle= doc.getElementById("locationTitle");
		locationTitle.text(getStringValue("html.location.title"));
		
		Element phoneManufacturerTitle = doc.getElementById("phoneManufacturerTitle");
		phoneManufacturerTitle.text(getStringValue("html.phone.manufacturer.title"));
		
		Element phoneTypeTitle= doc.getElementById("phoneTypeTitle");
		phoneTypeTitle.text(getStringValue("html.phone.type.tile"));
		
		Element networkTitle= doc.getElementById("networkTitle");
		networkTitle.text(getStringValue("html.network.title"));
		
		Element platformNameTitle= doc.getElementById("platformNameTitle");
		platformNameTitle.text(getStringValue("html.platform.name.title"));
		
		Element platformVerTitle= doc.getElementById("platformVerTitle");
		platformVerTitle.text(getStringValue("html.platform.ver.title"));
		
		Element ipAddressTitle= doc.getElementById("ipAddressTitle");
		ipAddressTitle.text(getStringValue("html.ip.address.title"));
		
		Element primeDnsTitle= doc.getElementById("primeDnsTitle");
		primeDnsTitle.text(getStringValue("html.prime.dns.title"));
		
		Element secondaryDnsTitle= doc.getElementById("secondaryDnsTitle");
		secondaryDnsTitle.text(getStringValue("html.secondary.dns.title"));
		
	}
	
	public abstract void appendPrivateMetrics() throws UnsatisfiedLinkError,
			IOException;

}
