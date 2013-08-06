package gizmoe.capabilities;

import java.util.concurrent.ConcurrentHashMap;

public class Capability2 extends DemoBaseCapability{

	/**
	 * 
	 */
	ConcurrentHashMap<String, Object> ioMap;
	public Capability2(ConcurrentHashMap<String, Object> inputs) {
		this.ioMap = inputs;
	}	private final String tag = "Capability2, thread"+this.hashCode()+":: ";
	//private static int counter = 0;
	public void run() {
		System.out.println("From Capability2, thread"+this.hashCode());
		int i1 = -1, i2 = -1, i3 = -1, o1, o2, o3;
		if(ioMap.containsKey("i1")){
			i1 = (Integer) ioMap.get("i1");
			System.out.println(tag+"Received input i1 = "+i1);
		}else{
			System.out.println(tag+"Input i1 not found");
		}
		if(ioMap.containsKey("i2")){
			i2 = (Integer) ioMap.get("i2");
			System.out.println(tag+"Received input i2 = "+i1);
		}else{
			System.out.println(tag+"Input i2 not found");
		}
		if(ioMap.containsKey("i3")){
			i3 = (Integer) ioMap.get("i3");
			System.out.println(tag+"Received input i3 = "+i3);
		}else{
			System.out.println(tag+"Input i3 not found");
		}
		ioMap.clear();
		o1 = i1+105;
		o2 = i2+105;
		o3 = i3+105;
		System.out.println(tag+"Sending output o1 = "+o1);
		System.out.println(tag+"Sending output o2 = "+o2);
		System.out.println(tag+"Sending output o3 = "+o3);
		ioMap.put("o1",o1);
		ioMap.put("o2",o2);
		ioMap.put("o3",o3);
	}

}
