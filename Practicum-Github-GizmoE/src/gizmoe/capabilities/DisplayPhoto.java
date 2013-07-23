package gizmoe.capabilities;

import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class DisplayPhoto extends CapabilityBase{

	private static final long serialVersionUID = 1L;
	private final String tag = "DisplayPhoto, thread"+this.hashCode()+":: ";
	@Override
	public ConcurrentHashMap<String, Object> body(ConcurrentHashMap<String, Object> inputs) {
		ConcurrentHashMap<String, Object> outputs = new ConcurrentHashMap<String, Object>();
		String handle = null;
		if(inputs.containsKey("queryHandle")){
			/*
			 * Input Section
			 */
			handle = (String) inputs.get("queryHandle");
			System.out.println(tag+"Received input queryHandle = "+handle);
			
			/*
			 * Operation Section
			 */
			InputStream stream = DisplayPhoto.class.getResourceAsStream("photo.txt");
			Scanner in = new Scanner(stream);
			while(in.hasNext()){
				String[] line = in.nextLine().split(";");
				String candidate = line[0];
				if(candidate.equalsIgnoreCase(handle)){					
					/*
					 * Output Section
					 */
					
					//register photo
					System.out.println(tag+"Photo Found!");
					return outputs;
				}
			}
		}else{
			System.err.println(tag+"Input queryHandle not found");
		}
		return outputs;
	}

}
