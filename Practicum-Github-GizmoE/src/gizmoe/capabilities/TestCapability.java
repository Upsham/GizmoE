package gizmoe.capabilities;

import java.util.concurrent.ConcurrentHashMap;

public class TestCapability extends CapabilityBase{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static int counter = 0;
	@Override
	public ConcurrentHashMap<String, Object> body(ConcurrentHashMap<String, Object> inputs) {
		//System.out.println("From TestCapability: thread"+this.hashCode());
		if(inputs.containsKey("out")){
			System.out.println("thread"+this.hashCode()+":: Got input:: "+inputs.get("out"));
		}
		ConcurrentHashMap<String, Object> outputs = new ConcurrentHashMap<String, Object>();
		System.out.println("thread"+this.hashCode()+":: Sending output:: "+counter);
		outputs.put("out",counter++);
		return outputs;
	}

}
