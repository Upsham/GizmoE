package gizmoe.capabilities;

import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class LookUpPhoto extends CapabilityBase{

	private static final long serialVersionUID = 1L;
	private final String tag = "LookUpPhoto, thread"+this.hashCode()+":: ";
	@Override
	public ConcurrentHashMap<String, Object> body(ConcurrentHashMap<String, Object> inputs) {
		ConcurrentHashMap<String, Object> outputs = new ConcurrentHashMap<String, Object>();
		String name = null, photo = null;
		Boolean found = false;
		if(inputs.containsKey("name")){
			/*
			 * Input Section
			 */
			name = (String) inputs.get("name");
			System.out.println(tag+"Received input name = "+name);
			
			/*
			 * Operation Section
			 */
			InputStream stream = LookUpPhoto.class.getResourceAsStream("photo.txt");
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
		
		/*
		 * Output Section
		 */
		if(found == false){
			System.out.println(tag+"Sending output photoNotFound = "+found);
			outputs.put("photoNotFound",found);		
		}else{
			System.out.println(tag+"Sending output photo = "+photo);
			outputs.put("photo",photo);		
		}
		return outputs;
	}

}
