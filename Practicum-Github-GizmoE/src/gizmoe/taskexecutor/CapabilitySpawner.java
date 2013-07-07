package gizmoe.taskexecutor;

import gizmoe.messages.SpawnMessage;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.*;


import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class CapabilitySpawner implements Runnable {
	// URL of the JMS server. DEFAULT_BROKER_URL will just mean
    // that JMS server is on localhost
    private static String url = ActiveMQConnection.DEFAULT_BROKER_URL;

    // Name of the queue we will be sending gizmoe.messages to
    private static String subject = "Spawn";
    private static String replysubject = "SpawnReply";
    
	public void run() {
		Logger.getRootLogger().setLevel(Level.OFF);
		 // Getting JMS connection from the server
        ConnectionFactory connectionFactory
            = new ActiveMQConnectionFactory(url);
        Connection connection;
		try {
			connection = connectionFactory.createConnection();

        connection.start();
        // Creating session for sending gizmoe.messages
        Session session = connection.createSession(false,
            Session.AUTO_ACKNOWLEDGE);
        // Getting the queue 'InvokeQueue'
        Destination destination = session.createQueue(subject);
        Destination replydest = session.createQueue(replysubject);

        // MessageConsumer is used for receiving (consuming) gizmoe.messages
        MessageConsumer consumer = session.createConsumer(destination);
        MessageProducer producer = session.createProducer(replydest);
        
        // Here we receive the message.
        // By default this call is blocking, which means it will wait
        // for a message to arrive on the queue.
        Message message = consumer.receive();
        // There are many types of Message and TextMessage
        // is just one of them. Producer sent us a TextMessage
        // so we must cast to it to get access to its .getText()
        // method.
        
        ConcurrentHashMap <Integer, String> reply = new ConcurrentHashMap<Integer, String>();
        if (message instanceof ObjectMessage) {
        	SpawnMessage spawnMessage = (SpawnMessage) ((ObjectMessage) (message)).getObject();
            for(int id : spawnMessage.getCapabilities().keySet()){
            	int hash = searchAndExecuteCapability(spawnMessage.getCapabilities().get(id));
            	if(hash!=-1){
            		reply.put(id, "thread"+hash);
            	}else{
            		System.err.println("CapabilitySpawner was unable to create a thread for capability "+spawnMessage.getCapabilities().get(id));
            	}
            }
        }
        SpawnMessage replyMessage = new SpawnMessage(reply);
        ObjectMessage replyM = session.createObjectMessage(replyMessage);
        producer.send((Message) replyM);
        connection.stop();
        connection.close();	
		}catch (JMSException e) {
			e.printStackTrace();
		}
	}
	private static int searchAndExecuteCapability(String name){
		try {
			Class<?> c = Class.forName("gizmoe.capabilities."+name);
			Constructor<?> constructor = c.getConstructor();
			Object o = constructor.newInstance();
			Thread t1 = new Thread((Runnable) o);
			t1.start();
			return o.hashCode();
		} catch (ClassNotFoundException e) {
			System.out.print("The class does not exist in the capability package!");
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return -1;

	}


}
