package gizmoe.capabilities;

import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class FindBusy extends CapabilityBase{

	private static final long serialVersionUID = 1L;
	private final String tag = "FindBusy, thread"+this.hashCode()+":: ";
	@Override
	public ConcurrentHashMap<String, Object> body(ConcurrentHashMap<String, Object> inputs) {
		ConcurrentHashMap<String, Object> outputs = new ConcurrentHashMap<String, Object>();
		String handle = null, status = null;
		if(inputs.containsKey("queryHandle")){
			/*
			 * Input Section
			 */
			handle = (String) inputs.get("queryHandle");
//			System.out.println(tag+"Received input queryHandle = "+handle);
			
			/*
			 * Operation Section
			 */
			InputStream stream = FindBusy.class.getResourceAsStream("calendar.txt");
			Scanner in = new Scanner(stream);
			while(in.hasNext()){
//				System.out.println(tag+"In Loop");
				String[] line = in.nextLine().split(";");
				String candidate = line[0];
				if(candidate.equalsIgnoreCase(handle)){
					status = line[1];
					
					/*
					 * Output Section
					 */
					
					if(status.equalsIgnoreCase("y")){
//						System.out.println(tag+"Sending output available = "+line[2]);
						System.out.println(tag+handle+" is currently available at "+line[2]);
						outputs.put("available", line[2]);
					}else{
//						System.out.println(tag+"Sending output notAvailable = "+true);
						outputs.put("notAvailable", true);
					}
					return outputs;
				}
			}
		}else{
			System.err.println(tag+"Input queryHandle not found");
		}
//		System.out.println(tag+"Sending empty output");
		return outputs;
	}

}
