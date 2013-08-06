package gizmoe.capabilities;

import java.util.concurrent.ConcurrentHashMap;

public class BatteryMonitor extends DemoBaseCapability{

	private final String tag = "BatteryMonitor, thread"+this.hashCode()+":: ";
	private ConcurrentHashMap<String, Object> ioMap;
	@Override
	public void run() {
		int seconds = 0;
		if(ioMap.containsKey("time_in_seconds")){
			/*
			 * Input Section
			 */
			seconds = (Integer)(ioMap.get("time_in_seconds"));
//			System.out.println(tag+"Received input name = "+name);
			System.out.println(tag+"Battery will run out in "+seconds+" seconds!");
			try {
				Thread.sleep(seconds*1000);
				System.out.println(tag+"Battery is finished! Returning to home at Rm282");
				ioMap.clear();
				ioMap.put("home", "Rm282");
				ioMap.put("lowBattery", true);
			} catch (InterruptedException e) {
				//Assassinate silently
			}
			/*
			 * Operation Section
			 */
			//Go to location sim
		}else{
			System.err.println(tag+"Input time_in_seconds not found");
		}
//		System.out.println(tag+"Skyped with = "+name);
		
	}
	
	public BatteryMonitor(ConcurrentHashMap<String, Object> inputs) {
		this.ioMap = inputs;
	}

}
