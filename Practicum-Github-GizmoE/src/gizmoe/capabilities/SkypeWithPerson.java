package gizmoe.capabilities;

import java.util.concurrent.ConcurrentHashMap;

public class SkypeWithPerson extends CapabilityBase{

	private static final long serialVersionUID = 1L;
	private final String tag = "SkypeWithPerson, thread"+this.hashCode()+":: ";
	@Override
	public ConcurrentHashMap<String, Object> body(ConcurrentHashMap<String, Object> inputs) {
		ConcurrentHashMap<String, Object> outputs = new ConcurrentHashMap<String, Object>();
		String name = null;
		if(inputs.containsKey("name")){
			/*
			 * Input Section
			 */
			name = (String) inputs.get("name");
			System.out.println(tag+"Received input name = "+name);
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			/*
			 * Operation Section
			 */
			//Go to location sim
		}else{
			System.err.println(tag+"Input name not found");
		}
		System.out.println(tag+"Skyped with = "+name);
		return outputs;
	}

}
