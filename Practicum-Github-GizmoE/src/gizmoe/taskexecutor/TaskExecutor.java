package gizmoe.taskexecutor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import messages.SpawnMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import gizmoe.taskdag.MyDag;

public class TaskExecutor {

	static MyDag taskdag;
	static MessageConsumer getQueue;
	static MessageProducer putQueue;
	static Session session;
	static Connection connection;
	private static ConcurrentHashMap <Integer, MessageProducer> capabilityMessageMap = new ConcurrentHashMap<Integer, MessageProducer>();
	private static ConcurrentHashMap <Integer, MessageConsumer> capabilityReplyMap = new ConcurrentHashMap<Integer, MessageConsumer>();
	public static void main(String[] args) {
		Thread t1 = new Thread(new CapabilitySpawner());
		t1.start();
		execute();
	}
	
	public static void callback(MyDag dag){
		taskdag = dag;
	}
	
	public static void setUpConnection(){
		Logger.getRootLogger().setLevel(Level.OFF);
		String url = ActiveMQConnection.DEFAULT_BROKER_URL;

		// Getting JMS connection from the server
		ConnectionFactory connectionFactory
		= new ActiveMQConnectionFactory(url);
		try {
			connection = connectionFactory.createConnection();

			connection.start();

			// Creating session for sending messages
			session = connection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);

			// Getting the queue 'InvokeQueue'
			Destination destination = session.createQueue("SpawnReply");
			Destination replydest = session.createQueue("Spawn");

			// MessageConsumer is used for receiving (consuming) messages
			getQueue = session.createConsumer(destination);
			putQueue = session.createProducer(replydest);
		}catch (JMSException e) {
			e.printStackTrace();
		}
	}
	public static void execute(){
		setUpConnection();
		
		
		ConcurrentHashMap <Integer, String> map = new ConcurrentHashMap<Integer, String>();
		map.put(100, "TestCapability");
		map.put(200, "TestCapability");
		
		ConcurrentHashMap <Integer, String> capabilityQueues = startCapabilities(map);
		System.out.println("TaskExecutor:: Creating queues");
		createCapabilityMessageQueues(capabilityQueues);
		System.out.println("TaskExecutor:: Created queues!!");
		for(int id : capabilityMessageMap.keySet()){
			System.out.println("TaskExecutor:: Sending to "+capabilityQueues.get(id)+" - "+id);
			ConcurrentHashMap<String, Object> input = new ConcurrentHashMap<String, Object>();
			input.put("out", id);
			ObjectMessage tmpMsg;
			try {
				tmpMsg = session.createObjectMessage(input);
				capabilityMessageMap.get(id).send(tmpMsg);
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
		try {
			Thread.sleep(100);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		exitCleanly();
		
	}
	
	private static void createCapabilityMessageQueues(ConcurrentHashMap <Integer, String> capabilityQueues){
		Destination destination;
		for(int id : capabilityQueues.keySet()){
			String queueName = capabilityQueues.get(id);
			if(!capabilityMessageMap.containsKey(id)){
				try {
					destination = session.createQueue(queueName);
					MessageProducer queue = session.createProducer(destination);
					capabilityMessageMap.put(id, queue);
				} catch (JMSException e) {
					e.printStackTrace();
				}
			}else{
				System.err.println("ID collision detected in task executor!");
			}
			
			if(!capabilityReplyMap.containsKey(id)){
				try {
					destination = session.createQueue(queueName+"reply");
					MessageConsumer queue = session.createConsumer(destination);
					CustomMessageListener listener = new CustomMessageListener();
			        listener.setParams(id, queueName);
			        queue.setMessageListener(listener);
					capabilityReplyMap.put(id, queue);
				} catch (JMSException e) {
					e.printStackTrace();
				}
			}else{
				System.err.println("ID collision detected in task executor!");
			}
		}
	}
	public static ConcurrentHashMap<Integer, String> startCapabilities(ConcurrentHashMap<Integer, String> toStart){
		SpawnMessage spawnMsg = new SpawnMessage(toStart);
		ObjectMessage send;
		try {
			send = session.createObjectMessage(spawnMsg);
			putQueue.send(send);
			Message inMsg = getQueue.receive();
			if(inMsg instanceof ObjectMessage){
				ObjectMessage tmp = (ObjectMessage) inMsg;
				if(tmp.getObject() instanceof SpawnMessage){
					spawnMsg = (SpawnMessage) tmp.getObject();
					return spawnMsg.getCapabilities();
				}
			}
		} catch (JMSException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void exitCleanly(){
        try {
			connection.stop();
	        connection.close();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
	@SuppressWarnings("unused")
	private static void searchAndExecuteCapability(String name){
		try {
			Class<?> c = Class.forName(name);
			Constructor<?> constructor = c.getConstructor();
			Object o = constructor.newInstance();
			System.out.println(o.hashCode());
			Thread t1 = new Thread((Runnable) o);
			t1.run();
			System.out.println("magic!");
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
	}

}
