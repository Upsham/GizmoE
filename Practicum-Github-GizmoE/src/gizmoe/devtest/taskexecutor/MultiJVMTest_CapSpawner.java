package gizmoe.devtest.taskexecutor;

import gizmoe.taskexecutor.CapabilitySpawner;

public class MultiJVMTest_CapSpawner {
	
	public static void main(String[] args) {
		Thread t1 = new Thread(new CapabilitySpawner());
		t1.start();
	}
	
}
