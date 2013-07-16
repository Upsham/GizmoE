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
import gizmoe.taskdag.IOPair;
import gizmoe.taskdag.Input;
import gizmoe.taskdag.MyDag;
import gizmoe.taskdag.Output;

public class TaskExecutor {

	static boolean startingCapabilities = true;
	static boolean exit = false;
	static MyDag taskdag;
	static MessageConsumer getQueue;
	static MessageProducer putQueue;
	static Session session;
	static Connection connection;
	static ConcurrentHashMap <Integer, MessageProducer> capabilityMessageMap = new ConcurrentHashMap<Integer, MessageProducer>();
	 static ConcurrentHashMap <Integer, ObjectMessage> preparedMessages = new ConcurrentHashMap<Integer, ObjectMessage>();
	 static ConcurrentHashMap <Integer, MessageConsumer> capabilityReplyMap = new ConcurrentHashMap<Integer, MessageConsumer>();
	 static ArrayList<Integer> capabilityExecuteQueue = new ArrayList<Integer>();
	 static ArrayList<Integer> capabilitiesFinished = new ArrayList<Integer>();
	 static ConcurrentHashMap <Integer, Object> outputCache = new ConcurrentHashMap<Integer, Object>();
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
			SpawnMessageListener listener = new SpawnMessageListener();
			getQueue.setMessageListener(listener);
			putQueue = session.createProducer(replydest);
		}catch (JMSException e) {
			e.printStackTrace();
		}
	}
	public static void execute(){
		setUpConnection();
		ArrayList<Input> overallInput = taskdag.getAllOverallInput();
		Scanner sysin = new Scanner(System.in);
		for(Input in : overallInput){
			//System.out.println("ID was:"+taskdag.isMappedTo(in.id).get(0).id);
			System.out.print("User input required, please enter '"+in.name+"' of type '"+in.type+"': ");
			String inputLine = sysin.nextLine();
			for(IOPair io : taskdag.isMappedTo(in.id)){
				if(in.type.equals("int")){
					outputCache.put(io.id, Integer.parseInt(inputLine));
				}else if(in.type.equals("string")){
					outputCache.put(io.id, sysin.nextLine());
				}else if(in.type.equals("boolean")){
					outputCache.put(io.id, Boolean.parseBoolean(inputLine));
				}else if(in.type.equals("double")){
					outputCache.put(io.id, Double.parseDouble(inputLine));
				}else if(in.type.equals("float")){
					outputCache.put(io.id, Float.parseFloat(inputLine));
				}else{
					System.err.println("Unrecognized input type!!");
				}
			}
		}
		ArrayList<Integer> startids = taskdag.startCapabilities();
		ConcurrentHashMap <Integer, String> map = new ConcurrentHashMap<Integer, String>();
		for(int id : startids){
			map.put(id, taskdag.getCapabilityName(id));
		}
		startCapabilities(map);
		//System.out.println("TaskExecutor:: Creating queues");
		//System.out.println("TaskExecutor:: Created queues!!");
		while(!exit){
		}
		System.out.println("Exiting Task Executor");
		//exitCleanly();
		
	}
	
	public static void registerOutputs(int id, ConcurrentHashMap<String, Object> outputMap){
		// get the outputs from actual capability using callback, store in local cache
		System.out.println("Back in TE!!");
		capabilitiesFinished.add(id);
		Output[] outputs = taskdag.getCapabilityOutputs(id);
		for(Output out : outputs){
			ArrayList<IOPair> mappings = taskdag.isMappedTo(out.id);
			for(IOPair mapping : mappings){
				if(!outputCache.containsKey(mapping.id)){
					outputCache.put(mapping.id, outputMap.get(out.name));
				}else{
					System.err.println("Task Executor's capability output cache already contains an entry with ID "+mapping.id);
				}
			}
			
		}
		tryNextCapability(id);
	}
	
	private static void tryNextCapability(int id){
		System.out.println("Back in trynextCap!!");
		boolean cannotExecute = false;
		if(taskdag.nextCapabilities(id) == null || taskdag.nextCapabilities(id).size()==0){
			System.out.println("TaskExecutor :: Exiting baby!!!");
			exit = true;
		}
		for(int toStart : taskdag.nextCapabilities(id)){
			if(!capabilityExecuteQueue.contains(toStart)){
				capabilityExecuteQueue.add(toStart);
			}
		}
		ArrayList<Integer> toRemove = new ArrayList<Integer>();
		for(int capID : capabilityExecuteQueue){
			if(taskdag.isJoin(capID)){
				ArrayList<Integer> joiners = taskdag.joinToBecome(capID);
				for(int join : joiners){
					if(!capabilitiesFinished.contains(join)){
						cannotExecute = true;
						break;
					}
				}
				if(!cannotExecute){
					ObjectMessage msg = createCapabilityInputMessage(capID);
					if(msg != null){
						preparedMessages.put(capID, msg);
						startValidCapability(capID);
						toRemove.add(capID);
					}
				}else{
					System.out.println("Reached a cannot execute state here!! ID: "+capID);
				}
			}else{
					ObjectMessage msg = createCapabilityInputMessage(capID);
					if(msg != null){
						preparedMessages.put(capID, msg);
						startValidCapability(capID);
						toRemove.add(capID);
					}
			}
		}
		capabilityExecuteQueue.removeAll(toRemove);

	}
	
	private static ObjectMessage createCapabilityInputMessage(int id){
		Input[] inputs = taskdag.getCapabilityInputs(id);
		ConcurrentHashMap<String, Object> input = new ConcurrentHashMap<String, Object>();
		for(Input in : inputs){
			if(!outputCache.containsKey(in.id)){
				System.err.println("Task Executor :: Input "+in.id+"has not been registered yet in TE. Will wait.");
				return null;
			}else{
				input.put(in.name, outputCache.get(in.id));
			}
		}
		ObjectMessage tmpMsg = null;
		try {
			tmpMsg = session.createObjectMessage(input);
		} catch (JMSException e) {
			e.printStackTrace();
		}
		return tmpMsg;
	}
	
	private static void startValidCapability(int id){
		System.out.println("Back in startValid!!");
		ConcurrentHashMap <Integer, String> map = new ConcurrentHashMap<Integer, String>();
		map.put(id, taskdag.getCapabilityName(id));
		startCapabilities(map);
	}
	public static void createCapabilityMessageQueues(ConcurrentHashMap <Integer, String> capabilityQueues){
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
					CapabilityMessageListener listener = new CapabilityMessageListener();
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
	public static void startCapabilities(ConcurrentHashMap<Integer, String> toStart){
		SpawnMessage spawnMsg = new SpawnMessage(toStart);
		ObjectMessage send;
		try {
			send = session.createObjectMessage(spawnMsg);
			putQueue.send(send);
		} catch (JMSException e) {
			e.printStackTrace();
		}
		return;
	}
	
	public static void capabilitySpawnerReplyRegister(ConcurrentHashMap<Integer, String> capabilityQueues){
		createCapabilityMessageQueues(capabilityQueues);
		if(!startingCapabilities){
			for(int id : capabilityQueues.keySet()){
				System.out.println("TaskExecutor:: Sending to testing "+capabilityQueues.get(id)+":: "+id);
				try {
					capabilityMessageMap.get(id).send(preparedMessages.get(id));
				} catch (JMSException e) {
					System.err.println("Could not send message to Capability!");
					e.printStackTrace();
				}
			}
		}else{
			startingCapabilities = false;
			for(int id : capabilityMessageMap.keySet()){
				System.out.println("TaskExecutor:: Sending to "+capabilityQueues.get(id)+":: "+id);
				ConcurrentHashMap<String, Object> input = new ConcurrentHashMap<String, Object>();
				Input[] inputs = taskdag.getCapabilityInputs(id);
				for(Input in : inputs){
					if(!outputCache.containsKey(in.id)){
						System.err.println("Task Executor :: Input "+in.id+"has not been registered yet in TE for start capabilities! Fatal!");
					}else{
						input.put(in.name, outputCache.get(in.id));
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
		}
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
