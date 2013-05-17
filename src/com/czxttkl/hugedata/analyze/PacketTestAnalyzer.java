package com.czxttkl.hugedata.analyze;

import com.czxttkl.hugedata.server.TaskListener.TaskListenerHandler;

public class PacketTestAnalyzer implements Runnable{
	
	TaskListenerHandler taskListenerHandler;

	public PacketTestAnalyzer(TaskListenerHandler taskListenerHandler) {
		this.taskListenerHandler = taskListenerHandler;
	}
	
	public void run() {
		
	}
	
	public synchronized void waitForTestFinish() {
		try {
			wait();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
