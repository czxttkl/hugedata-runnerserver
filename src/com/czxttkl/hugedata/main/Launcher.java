package com.czxttkl.hugedata.main;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.att.aro.main.ApplicationResourceOptimizer;
import com.czxttkl.hugedata.server.RunnerServer;
import com.czxttkl.hugedata.server.TaskListener;

public class Launcher {
	public static final ResourceBundle InitBundle = ResourceBundle.getBundle("InitBundle");
	
	public static void main(String... args) {
		
		final RunnerServer runnerServer = new RunnerServer();
		
		int webSocketPort = Integer.valueOf(InitBundle.getString("WebSocket.Server.Port"));
		final TaskListener taskListener = new TaskListener(webSocketPort);
		taskListener.startListen();
		
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				e.printStackTrace();
				Logger logger = Logger.getLogger(RunnerServer.class.getName());
				if (e instanceof OutOfMemoryError) 
					logger.log(Level.SEVERE, "Out of memory error", e);
				 else 
					logger.log(Level.SEVERE, "Uncaught exception", e);
			}
		});
	}
}
