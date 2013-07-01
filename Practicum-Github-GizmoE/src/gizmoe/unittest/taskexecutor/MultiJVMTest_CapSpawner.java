package gizmoe.unittest.taskexecutor;

import java.io.File;

import gizmoe.taskexecutor.CapabilitySpawner;

public class MultiJVMTest_CapSpawner {
	
	public static void main(String[] args) {
		File file = new File("CS_MultiJVM_Test.txt");
		file.delete();
		Thread t1 = new Thread(new CapabilitySpawner());
		t1.start();
		System.out.println("Now please run MultiJVMTest_TaskExecutor");
	}
	
}
