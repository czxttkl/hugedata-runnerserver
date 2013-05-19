package com.czxttkl.hugedata.helper;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Serializer;

import com.czxttkl.hugedata.analyze.PacketTestAnalyzer;
import com.czxttkl.hugedata.server.RunnerServer;
import com.czxttkl.hugedata.test.PacketTest;
import com.czxttkl.hugedata.test.Test;

public class ResultCollector {

	public static Logger logger = Logger
			.getLogger(RunnerServer.class.getName());

	public static PacketTestAnalyzer analyze(PacketTest packetTest,
			String testResult) throws IOException {
		// TODO Auto-generated method stub
		logger.info("Test Instrumentation finished");

		int totalProcedure = 0;
		double testTime = 0;
		List<Integer> failureProcedureNums = new ArrayList<Integer>();

		Scanner resultScanner = new Scanner(testResult);
		while (resultScanner.hasNextLine()) {
			String line = resultScanner.nextLine();

			// filter unrelated lines
			if (line.length() < 4)
				continue;

			if (line.startsWith("Failure in testProcedure")) {
				int procedureNum = Integer.valueOf(line.substring(24)
						.split(":")[0]);
				failureProcedureNums.add(procedureNum);

				while (resultScanner.hasNextLine()) {
					String exception = resultScanner.nextLine();
					if (exception.length() < 4)
						break;
					// System.out.println(exception);
				}
			}

			if (line.startsWith("Time: ")) {
				testTime = Double.valueOf(line.substring(6));
				// System.out.println(testTime);
			}

			if (line.startsWith("OK (")) {
				totalProcedure = Integer
						.valueOf(line.substring(4).split(" ")[0]);
				// System.out.println(totalProcedure);
			}

			if (line.startsWith("Tests run: ")) {
				totalProcedure = Integer
						.valueOf(line.split(",")[0].split(" ")[2]);
				// System.out.println(totalProcedure);
			}

		}
		resultScanner.close();

		Element root = new Element("Result");
		appendPublicMetrics(root, packetTest, testTime);
		appendPacketTestMetrics(root, packetTest, totalProcedure,
				failureProcedureNums);

		Document doc = new Document(root);
		format(new BufferedOutputStream(new FileOutputStream(
				packetTest.resultDirStr + "/result.xml")), doc);
		
		logger.info("The result of " + packetTest.resultDirStr
				+ " with priority" + packetTest.PRIORITY
				+ " has been analyzed.");
		
		PacketTestAnalyzer packetTestAnalyzer = new PacketTestAnalyzer(
				packetTest.resultDirStr, packetTest.TASK_LISTENER_HANDLER);
		RunnerServer.executor.execute(packetTestAnalyzer);
		return packetTestAnalyzer;
	}

	private static void appendPacketTestMetrics(Element root,
			PacketTest packetTest, int totalProcedure,
			List<Integer> failureProcedureNums) {
		
		Element test = new Element("Test");
		Element packetFileName = new Element("PacketFileName");
		packetFileName.appendChild(packetTest.PACKET_FILE_NAME);
		test.appendChild(packetFileName);

		for (int i = 0; i < totalProcedure; i++) {
			Element procedure = new Element("Procedure");
			Element completed = new Element("Completed");
			if (failureProcedureNums.contains(i + 1))
				completed.appendChild("False");
			else
				completed.appendChild("True");
			procedure.appendChild(completed);
			test.appendChild(procedure);
		}

		root.appendChild(test);
	}

	private static void appendPublicMetrics(Element root, Test test,
			double testTime) {
		Element startTime = new Element("StartTime");
		startTime.appendChild(test.TEST_START_TIME);
		Element duration = new Element("Duration");
		duration.appendChild(String.valueOf((int) (testTime * 1000)));
		Element location = new Element("Location");
		location.appendChild(String.valueOf(PacketTest.LOCATION_NUM));
		Element phoneManufacturer = new Element("PhoneManufacturer");
		phoneManufacturer.appendChild(test.DEVICE_INFO.getManufacturer());
		Element phoneType = new Element("PhoneType");
		phoneType.appendChild(test.DEVICE_INFO.getType());
		Element network = new Element("Network");
		network.appendChild(test.DEVICE_INFO.getNetwork());
		Element platformName = new Element("PlatformName");
		platformName.appendChild(test.DEVICE_INFO.getPlatformName());
		Element platformVer = new Element("PlatformVer");
		platformVer.appendChild(test.DEVICE_INFO.getPlatformVer());
		Element ipAddress = new Element("IpAddress");
		ipAddress.appendChild(test.DEVICE_INFO.getIpAddress());
		Element primeDns = new Element("PrimeDns");
		primeDns.appendChild(test.DEVICE_INFO.getPrimeDns());
		Element secondaryDns = new Element("SecondaryDns");
		secondaryDns.appendChild(test.DEVICE_INFO.getSecondaryDns());

		root.appendChild(startTime);
		root.appendChild(duration);
		root.appendChild(location);
		root.appendChild(phoneType);
		root.appendChild(network);
		root.appendChild(platformName);
		root.appendChild(platformVer);
		root.appendChild(ipAddress);
		root.appendChild(primeDns);
		root.appendChild(secondaryDns);
	}

	public static void format(OutputStream os, Document doc) throws IOException {
		Serializer serializer = new Serializer(os, "utf-8");
		serializer.setIndent(4);
		serializer.setMaxLength(600);
		serializer.write(doc);
		serializer.flush();
	}
}
