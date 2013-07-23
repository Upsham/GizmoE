package gizmoe.capabilities;

import java.util.concurrent.ConcurrentHashMap;

public class ErrorHandler extends CapabilityBase{

	private static final long serialVersionUID = 1L;
	//private final String tag = "ErrorHandler, thread"+this.hashCode()+":: ";
	@Override
	public ConcurrentHashMap<String, Object> body(ConcurrentHashMap<String, Object> inputs) {
		ConcurrentHashMap<String, Object> outputs = new ConcurrentHashMap<String, Object>();
		String pass = null;
		if(inputs.containsKey("ErrorInput")){
			/*
			 * Input Section
			 */
//			System.out.println(tag+"Received input errorInput");
			
		}else{
//			System.err.println(tag+"Input errorInput not found");
		}
		if(inputs.containsKey("UserInput")){
			pass = (String) inputs.get("UserInput");
//			System.out.println(tag+"Received input UserInput = "+pass);
		}else{
//			System.err.println(tag+"Input UserInput not found");
		}
		
		/*
		 * Output Section
		 */
		outputs.put("Passthrough", pass);
//		System.out.println(tag+"Sending output Passthrough = "+pass);
		return outputs;
	}

}
