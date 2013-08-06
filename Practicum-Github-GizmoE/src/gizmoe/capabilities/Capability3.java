package gizmoe.capabilities;

import java.util.concurrent.ConcurrentHashMap;

public class Capability3 extends DemoBaseCapability{

	/**
	 * 
	 */
	ConcurrentHashMap<String, Object> ioMap;
	public Capability3(ConcurrentHashMap<String, Object> inputs) {
		this.ioMap = inputs;
	}	private final String tag = "Capability3, thread"+this.hashCode()+":: ";
	//private static int counter = 0;
	public void run() {
		System.out.println("From Capability3, thread"+this.hashCode());
		int i1 = -1, o1;
		if(ioMap.containsKey("i1")){
			i1 = (Integer) ioMap.get("i1");
			System.out.println(tag+"Received input i1 = "+i1);
		}else{
			System.out.println(tag+"Input i1 not found");
		}
		ioMap.clear();
		o1 = i1+102;
		System.out.println(tag+"Sending output o1 = "+o1);
		ioMap.put("o1",o1);
	}

}
