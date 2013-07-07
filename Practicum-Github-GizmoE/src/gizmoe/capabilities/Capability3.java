package gizmoe.capabilities;

import java.util.concurrent.ConcurrentHashMap;

public class Capability3 extends CapabilityBase{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String tag = "Capability3, thread"+this.hashCode()+":: ";
	//private static int counter = 0;
	@Override
	public ConcurrentHashMap<String, Object> body(ConcurrentHashMap<String, Object> inputs) {
		System.out.println("From Capability3, thread"+this.hashCode());
		int i1 = -1, o1;
		if(inputs.containsKey("i1")){
			i1 = (Integer) inputs.get("i1");
			System.out.println(tag+"Received input i1 = "+i1);
		}else{
			System.out.println(tag+"Input i1 not found");
		}
		o1 = i1+102;
		ConcurrentHashMap<String, Object> outputs = new ConcurrentHashMap<String, Object>();
		System.out.println(tag+"Sending output o1 = "+o1);
		outputs.put("o1",o1);
		return outputs;
	}

}
