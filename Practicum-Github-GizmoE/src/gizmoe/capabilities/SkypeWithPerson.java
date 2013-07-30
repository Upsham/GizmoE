package gizmoe.capabilities;

import java.util.concurrent.ConcurrentHashMap;

public class SkypeWithPerson extends DemoBaseCapability{

	private final String tag = "SkypeWithPerson, thread"+this.hashCode()+":: ";
	ConcurrentHashMap<String, Object> ioMap;
	public SkypeWithPerson(ConcurrentHashMap<String, Object> inputs) {
		this.ioMap = inputs;
	}
	public void run() {
		String name = null;
		if(ioMap.containsKey("name")){
			/*
			 * Input Section
			 */
			name = (String) ioMap.get("name");
//			System.out.println(tag+"Received input name = "+name);
			System.out.println(tag+"Now Skyping with "+name);
			//Simulate operation
			if(seconds > 0){
				try {
					Thread.sleep(seconds * 1000);
				} catch (Exception e) {
					System.out.println(tag+"Killing Skype Session with "+name);
					return;
				}
			}else{
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					System.out.println(tag+"Killing Skype Session with "+name);
					return;
				}
			}
			/*
			 * Operation Section
			 */
			//Go to location sim
		}else{
			System.err.println(tag+"Input name not found");
		}
//		System.out.println(tag+"Skyped with = "+name);
		System.out.println(tag+"Finished skyping with "+name);
		ioMap.clear();
	}

}
