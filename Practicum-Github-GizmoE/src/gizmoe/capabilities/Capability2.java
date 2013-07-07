package gizmoe.capabilities;

import java.util.concurrent.ConcurrentHashMap;

public class Capability2 extends CapabilityBase{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String tag = "Capability2, thread"+this.hashCode()+":: ";
	//private static int counter = 0;
	@Override
	public ConcurrentHashMap<String, Object> body(ConcurrentHashMap<String, Object> inputs) {
		System.out.println("From Capability2, thread"+this.hashCode());
		int i1 = -1, i2 = -1, i3 = -1, o1, o2, o3;
		if(inputs.containsKey("i1")){
			i1 = (Integer) inputs.get("i1");
			System.out.println(tag+"Received input i1 = "+i1);
		}else{
			System.out.println(tag+"Input i1 not found");
		}
		if(inputs.containsKey("i2")){
			i2 = (Integer) inputs.get("i2");
			System.out.println(tag+"Received input i2 = "+i1);
		}else{
			System.out.println(tag+"Input i2 not found");
		}
		if(inputs.containsKey("i3")){
			i3 = (Integer) inputs.get("i3");
			System.out.println(tag+"Received input i3 = "+i3);
		}else{
			System.out.println(tag+"Input i3 not found");
		}
		o1 = i1+105;
		o2 = i2+105;
		o3 = i3+105;
		ConcurrentHashMap<String, Object> outputs = new ConcurrentHashMap<String, Object>();
		System.out.println(tag+"Sending output o1 = "+o1);
		System.out.println(tag+"Sending output o2 = "+o2);
		System.out.println(tag+"Sending output o3 = "+o3);
		outputs.put("o1",o1);
		outputs.put("o2",o2);
		outputs.put("o3",o3);
		return outputs;
	}

}
