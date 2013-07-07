package gizmoe.messages;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

public class SpawnMessage implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8126342327923671492L;
	ConcurrentHashMap<Integer, String> capabilityQueues = new ConcurrentHashMap<Integer, String>();
	
	public SpawnMessage(ConcurrentHashMap<Integer, String> capabilityQueues){
		this.capabilityQueues = capabilityQueues;
	}
	
	public ConcurrentHashMap<Integer, String> getCapabilities(){
		return capabilityQueues;
	}
	public void getCapabilityNames(){
		if(capabilityQueues==null || capabilityQueues.isEmpty()){
			System.out.println("Empty Message");
			return;
		}
		for(int cap : capabilityQueues.keySet()){
			System.out.println(cap);
		}
	}
}
