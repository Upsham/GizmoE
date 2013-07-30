package gizmoe.capabilities;

import java.util.concurrent.ConcurrentHashMap;

public class ErrorHandler extends DemoBaseCapability{

	//private final String tag = "ErrorHandler, thread"+this.hashCode()+":: ";
	ConcurrentHashMap<String, Object> ioMap;
	public void run() {
		String pass = null;
		if(ioMap.containsKey("ErrorInput")){
			/*
			 * Input Section
			 */
//			System.out.println(tag+"Received input errorInput");
			
		}else{
//			System.err.println(tag+"Input errorInput not found");
		}
		if(ioMap.containsKey("UserInput")){
			pass = (String) ioMap.get("UserInput");
//			System.out.println(tag+"Received input UserInput = "+pass);
		}else{
//			System.err.println(tag+"Input UserInput not found");
		}
		
		//Simulate operation
		if(seconds > 0){
			try {
				Thread.sleep(seconds * 1000);
			} catch (Exception e) {
				return;
			}
		}
		
		/*
		 * Output Section
		 */
		ioMap.clear();
		ioMap.put("Passthrough", pass);
//		System.out.println(tag+"Sending output Passthrough = "+pass);
	}
	
	public ErrorHandler(ConcurrentHashMap<String, Object> inputs){
		this.ioMap = inputs;
	}

}
