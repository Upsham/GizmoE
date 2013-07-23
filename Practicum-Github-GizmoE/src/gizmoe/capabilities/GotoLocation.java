package gizmoe.capabilities;

import java.util.concurrent.ConcurrentHashMap;

public class GotoLocation extends CapabilityBase{

	private static final long serialVersionUID = 1L;
	private final String tag = "GotoLocation, thread"+this.hashCode()+":: ";
	@Override
	public ConcurrentHashMap<String, Object> body(ConcurrentHashMap<String, Object> inputs) {
		ConcurrentHashMap<String, Object> outputs = new ConcurrentHashMap<String, Object>();
		String location = null;
		if(inputs.containsKey("location")){
			/*
			 * Input Section
			 */
			location = (String) inputs.get("location");
			System.out.println(tag+"Received input location = "+location);
			
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
			System.err.println(tag+"Input location not found");
		}
		System.out.println(tag+"At location = "+location);
		return outputs;
	}

}
