package com.czxttkl.hugedata.helper;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;
import java.util.logging.Logger;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Serializer;

import com.czxttkl.hugedata.test.PacketTest;

public class ResultAnalyzer {

	public static void analyze(PacketTest packetTest, String testResult) throws IOException {
		// TODO Auto-generated method stub
		Logger logger = packetTest.logger;
		logger.info("Test Instrumentation finished");

		int totalProcedure;
		double testTime = 0;

		Scanner resultScanner = new Scanner(testResult);
		while (resultScanner.hasNextLine()) {
			String line = resultScanner.nextLine();

			// filter unrelated lines
			if (line.length() < 4)
				continue;

			if (line.startsWith("Failure in testProcedure")) {
				int procedureNum = Integer.valueOf(line.substring(24)
						.split(":")[0]);
				System.out.println(procedureNum);

				while (resultScanner.hasNextLine()) {
					String inLine = resultScanner.nextLine();
					if (inLine.length() < 4)
						break;
					System.out.println(inLine);
				}
			}

			if (line.startsWith("Time: ")) {
				testTime = Double.valueOf(line.substring(6));
				System.out.println(testTime);
			}

			if (line.startsWith("OK (")) {
				totalProcedure = Integer
						.valueOf(line.substring(4).split(" ")[0]);
				System.out.println(totalProcedure);
			}

			if (line.startsWith("Tests run: ")) {
				totalProcedure = Integer
						.valueOf(line.split(",")[0].split(" ")[2]);
				System.out.println(totalProcedure);
			}

		}
		resultScanner.close();

		Element publicMetrics = new Element("publicMetrics");
		
		Element startTime = new Element("StartTime");
		startTime.appendChild(packetTest.TEST_START_TIME);
		Element duration = new Element("Duration");
		duration.appendChild(String.valueOf((int)(testTime * 1000)));
		publicMetrics.appendChild(startTime);
		publicMetrics.appendChild(duration);
		
		Document doc = new Document(publicMetrics);
		format(new BufferedOutputStream(new FileOutputStream(packetTest.resultDirStr + "/task.xml")),doc);
		logger.info(testResult);

	}
	
	public static void format(OutputStream os, Document doc) throws IOException{
		Serializer serializer = new Serializer(os,"utf-8");
		serializer.setIndent(4);
		serializer.setMaxLength(60);
		serializer.write(doc);
		serializer.flush();
	}
}
