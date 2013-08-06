package gizmoe.capabilities;

import java.util.concurrent.ConcurrentHashMap;

public class Capability1 extends DemoBaseCapability{

	/**
	 * 
	 */
	ConcurrentHashMap<String, Object> ioMap;
	public Capability1(ConcurrentHashMap<String, Object> inputs) {
		this.ioMap = inputs;
	}
	private final String tag = "Capability1, thread"+this.hashCode()+":: ";
	//private static int counter = 0;
	public void run() {
		System.out.println("From Capability1, thread"+this.hashCode());
		int i1 = -1, o1, o2;
		if(ioMap.containsKey("i1")){
			i1 = (Integer) ioMap.get("i1");
			System.out.println(tag+"Received input i1 = "+i1);
		}else{
			System.out.println(tag+"Input i1 not found");
		}
		ioMap.clear();
		o1 = i1+100;
		o2 = i1+100;
		System.out.println(tag+"Sending output o1 = "+o1);
		System.out.println(tag+"Sending output o2 = "+o2);
		ioMap.put("o1",o1);
		ioMap.put("o2",o2);
	}

}
