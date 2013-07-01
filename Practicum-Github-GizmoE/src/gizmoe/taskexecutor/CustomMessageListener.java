package gizmoe.taskexecutor;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

public class CustomMessageListener implements MessageListener{

	int id;
	String queue;
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                System.out.println("TaskExecutor received message from "+queue+" :: "
                        + textMessage.getText() + "'");
            }
        } catch (JMSException e) {
            System.out.println("Caught:" + e);
            e.printStackTrace();
        }
    }
    public void setParams(int id, String queue){
    	this.id = id;
    	this.queue = queue;
    	
    }

}
