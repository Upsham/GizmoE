package gizmoe.capabilities;

import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class FindBusy extends DemoBaseCapability{

	private final String tag = logTag + "FindBusy, thread"+this.hashCode()+":: ";
	private ConcurrentHashMap<String, Object> ioMap;
	@Override
	public void run() {
		String handle = null, status = null;
		if(ioMap.containsKey("queryHandle")){
			/*
			 * Input Section
			 */
			handle = (String) ioMap.get("queryHandle");
			log.warn(tag+"Querying "+handle+"'s calendar to check availability.");
			System.out.println(tag+"Querying "+handle+"'s calendar to check availability.");
//			System.out.println(tag+"Received input queryHandle = "+handle);
			
			/*
			 * Operation Section
			 */
			InputStream stream = FindBusy.class.getResourceAsStream("/gizmoe/mockdatabase/calendar.txt");
			Scanner in = new Scanner(stream);
			while(in.hasNext()){
//				System.out.println(tag+"In Loop");
				String[] line = in.nextLine().split(";");
				String candidate = line[0];
				if(candidate.equalsIgnoreCase(handle)){
					status = line[1];
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
						if(status.equalsIgnoreCase("y")){
//							System.out.println(tag+"Sending output available = "+line[2]);
							log.warn(tag+handle+" is currently available at "+line[2]);
							System.out.println(tag+handle+" is currently available at "+line[2]);
							ioMap.clear();
							ioMap.put("available", line[2]);
						}else{
//							System.out.println(tag+"Sending output notAvailable = "+true);
							ioMap.clear();
							ioMap.put("notAvailable", true);
						}
					
				}
			}
		}else{
			System.err.println(tag+"Input queryHandle not found");
		}
//		System.out.println(tag+"Sending empty output");
	}
	
	public FindBusy(ConcurrentHashMap<String, Object> inputs) {
		this.ioMap = inputs;
	}
	

}
