package gizmoe.taskexecutor;

import gizmoe.messages.SpawnMessage;

import java.util.concurrent.ConcurrentHashMap;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

public class SpawnMessageListener implements MessageListener{

	int id;
	String queue;
    public void onMessage(Message message) {
        try {
        	if(message instanceof ObjectMessage){
				ObjectMessage tmp = (ObjectMessage) message;
				if(tmp.getObject() instanceof SpawnMessage){
					SpawnMessage spawnMsg = (SpawnMessage) tmp.getObject();
					TaskExecutor.capabilitySpawnerReplyRegister(spawnMsg.getCapabilities());
				}
			}
            if (message instanceof SpawnMessage) {
                ObjectMessage inMsg = (ObjectMessage) message;
                if(inMsg.getObject() instanceof ConcurrentHashMap<?, ?>){
                	@SuppressWarnings("unchecked")
					ConcurrentHashMap<String, Object> output = (ConcurrentHashMap<String, Object>) inMsg.getObject();
                	//int toPrint = (Integer) output.get("out");
//                	for(Object toPrint : output.values()){
//                		System.out.println("TaskExecutor:: "+queue+" gave me:: "+(Integer)toPrint);
//                	}
                	TaskExecutor.registerOutputs(id, output);
                }
            }
        } catch (Exception e) {
            System.out.println("Caught:" + e);
            e.printStackTrace();
        }
    }
    public void setParams(int id, String queue){
    	this.id = id;
    	this.queue = queue;
    	
    }

}
