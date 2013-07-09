package gizmoe.taskexecutor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Scanner;
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


import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import gizmoe.TaskDagResolver.ResolveDag;
import gizmoe.messages.SpawnMessage;
import gizmoe.taskdag.Input;
import gizmoe.taskdag.MyDag;
import gizmoe.taskdag.Output;

public class TaskExecutor {

	static MyDag taskdag;
	static MessageConsumer getQueue;
	static MessageProducer putQueue;
	static Session session;
	static Connection connection;
	private static ConcurrentHashMap <Integer, MessageProducer> capabilityMessageMap = new ConcurrentHashMap<Integer, MessageProducer>();
	private static ConcurrentHashMap <Integer, MessageConsumer> capabilityReplyMap = new ConcurrentHashMap<Integer, MessageConsumer>();
	
	private static ConcurrentHashMap <Integer, Object> outputCache = new ConcurrentHashMap<Integer, Object>();
	public static void main(String[] args) {
		MyDag testdag = ResolveDag.TaskDagResolver("NewCombo");
		callback(testdag);
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

			// Creating session for sending gizmoe.messages
			session = connection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);

			// Getting the queue 'InvokeQueue'
			Destination destination = session.createQueue("SpawnReply");
			Destination replydest = session.createQueue("Spawn");

			// MessageConsumer is used for receiving (consuming) gizmoe.messages
			getQueue = session.createConsumer(destination);
			putQueue = session.createProducer(replydest);
		}catch (JMSException e) {
			e.printStackTrace();
		}
	}
	public static void execute(){
		setUpConnection();
		
		ArrayList<Integer> startids = taskdag.startCapabilities();
		ConcurrentHashMap <Integer, String> map = new ConcurrentHashMap<Integer, String>();
		for(int id : startids){
			map.put(id, taskdag.getCapabilityName(id));
		}
		ConcurrentHashMap <Integer, String> capabilityQueues = startCapabilities(map);
		//System.out.println("TaskExecutor:: Creating queues");
		createCapabilityMessageQueues(capabilityQueues);
		//System.out.println("TaskExecutor:: Created queues!!");
		Scanner sysin = new Scanner(System.in);
		for(int id : capabilityMessageMap.keySet()){
			System.out.println("TaskExecutor:: Sending to "+capabilityQueues.get(id)+":: "+id);
			ConcurrentHashMap<String, Object> input = new ConcurrentHashMap<String, Object>();
			Input[] inputs = taskdag.getCapabilityInputs(id);
			for(Input in : inputs){
				System.out.print("User input required, please enter '"+in.name+"' of type '"+in.type+"': ");
				if(in.type == "int"){
					input.put(in.name, Integer.parseInt(sysin.nextLine()));
				}else if(in.type == "string"){
					input.put(in.name, sysin.nextLine());
				}else if(in.type == "boolean"){
					input.put(in.name, Boolean.parseBoolean(sysin.nextLine()));
				}else if(in.type == "double"){
					input.put(in.name, Double.parseDouble(sysin.nextLine()));
				}else if(in.type == "float"){
					input.put(in.name, Float.parseFloat(sysin.nextLine()));
				}else{
					System.err.println("Unrecognized input type!!");
				}
			}
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
	
	public synchronized static void registerOutputs(int id, ConcurrentHashMap<String, Object> outputMap){
		Output[] outputs = taskdag.getCapabilityOutputs(id);
		for(Output out : outputs){
			if(!outputCache.containsKey(out.id)){
				outputCache.put(out.id, outputMap.get(out.name));
			}else{
				System.err.println("Task Executor's capability output cache already contains an enrry with ID "+out.id);
			}
		}
		// get the outputs from actual capability using callback, store in local cache
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
