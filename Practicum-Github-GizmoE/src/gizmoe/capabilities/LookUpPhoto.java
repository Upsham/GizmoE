package gizmoe.capabilities;

import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class LookUpPhoto extends DemoBaseCapability{

	private final String tag = "LookUpPhoto, thread"+this.hashCode()+":: ";
	private ConcurrentHashMap<String, Object> ioMap;
	public void run() {
		String name = null, photo = null;
		Boolean found = false;
		if(ioMap.containsKey("name")){
			/*
			 * Input Section
			 */
			name = (String) ioMap.get("name");
			System.out.println(tag+"Trying to get "+name+"'s photo from database.");

//			System.out.println(tag+"Received input name = "+name);
			
			/*
			 * Operation Section
			 */
			InputStream stream = LookUpPhoto.class.getResourceAsStream("/gizmoe/mockdatabase/photo.txt");
			Scanner in = new Scanner(stream);
			while(in.hasNext()){
				String[] line = in.nextLine().split(";");
				String candidate = line[0];
				if(candidate.equalsIgnoreCase(name)){
					photo = line[1];
					found = true;
					break;
				}
			}
		}else{
			System.err.println(tag+"Input name not found");
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
		if(found == false){
//			System.out.println(tag+"Sending output photoNotFound = "+found);
			ioMap.put("photoNotFound",found);		
		}else{
//			System.out.println(tag+"Sending output photo = "+photo);
			System.out.println(tag+name+"'s photo was found! It is at "+photo+" and is ready for display!");
			ioMap.put("photo",photo);		
		}
	}
	
	public LookUpPhoto(ConcurrentHashMap<String, Object> inputs) {
		this.ioMap = inputs;
	}

}
