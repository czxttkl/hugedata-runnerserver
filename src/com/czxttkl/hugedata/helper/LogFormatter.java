package com.czxttkl.hugedata.helper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import com.czxttkl.hugedata.server.RunnerServer;

public class LogFormatter extends Formatter{

	@Override
	public String format(LogRecord record) {
		// TODO Auto-generated method stub
		StringBuilder stb = new StringBuilder();
		stb.append(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()).toString());
		stb.append("->");
		stb.append(RunnerServer.locationNum);
		stb.append("->");
		stb.append(record.getLevel());
		stb.append("->");
		stb.append(record.getMessage());
		stb.append("\r\n");
		return stb.toString();
	}

}
