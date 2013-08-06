package gizmoe.capabilities;

import java.util.concurrent.ConcurrentHashMap;

public class TestCapability extends DemoBaseCapability{

	/**
	 * 
	 */
	ConcurrentHashMap<String, Object> ioMap;
	public TestCapability(ConcurrentHashMap<String, Object> inputs) {
		this.ioMap = inputs;
	}
	
	private static int counter = 0;
	public void run() {
		//System.out.println("From TestCapability: thread"+this.hashCode());
		if(ioMap.containsKey("out")){
			System.out.println("thread"+this.hashCode()+":: Got input:: "+ioMap.get("out"));
		}
		System.out.println("thread"+this.hashCode()+":: Sending output:: "+counter);
		ioMap.clear();
		ioMap.put("out",counter++);
	}

}
