package gizmoe.taskexecutor;

import java.util.concurrent.ConcurrentHashMap;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

public class CustomMessageListener implements MessageListener{

	int id;
	String queue;
    public void onMessage(Message message) {
        try {
            if (message instanceof ObjectMessage) {
                ObjectMessage inMsg = (ObjectMessage) message;
                if(inMsg.getObject() instanceof ConcurrentHashMap<?, ?>){
                	@SuppressWarnings("unchecked")
					ConcurrentHashMap<String, Object> output = (ConcurrentHashMap<String, Object>) inMsg.getObject();
                	int toPrint = (Integer) output.get("out");
                	System.out.println("TaskExecutor:: "+queue+" gave me - "+toPrint);
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
