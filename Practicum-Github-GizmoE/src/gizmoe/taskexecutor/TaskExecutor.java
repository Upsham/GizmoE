package gizmoe.taskexecutor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;


import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gizmoe.TaskDagResolver.ResolveDag;
import gizmoe.messages.SpawnMessage;
import gizmoe.taskdag.IOPair;
import gizmoe.taskdag.Input;
import gizmoe.taskdag.MyDag;
import gizmoe.taskdag.Output;

public class TaskExecutor {

	final static String logString = "Demo::";
	final static Logger log = LoggerFactory.getLogger(TaskExecutor.class);	
	static boolean startingCapabilities = true;
	volatile static boolean exit = false;
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
	static ArrayList<Integer> capabilitiesRunning = new ArrayList<Integer>();
	static ConcurrentHashMap <Integer, Object> outputCache = new ConcurrentHashMap<Integer, Object>();
	static ArrayList<Integer> endings = new ArrayList<Integer>();
	static int semaphore = 0;
	volatile static boolean alive = true;
	public static void main(String[] args) {
		System.out.println("/*******************************************");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Welcome to the Task Executor Demo Console");
		int i = 1;
		Boolean invalid;
		do{
			System.out.print("Please type 1 to run MeetAdvisor, or 2 to run TryMeetingAdvisors, " +
					"\n3 for MeetAdvisorWB and 4 for TryMeetingAdvisorsWB: ");
			invalid = false;
			try {
				i = Integer.parseInt(br.readLine());
			}catch (IOException e) {
				e.printStackTrace();
			}catch(NumberFormatException nfe){
				System.err.println("Invalid Format! Please enter 1 or 2 only!");
				invalid = true;
			}
			
			if(i!=1 && i!=2 && i!=3 && i!=4){
				System.err.println("Please only type 1 or 2!");
				invalid = true;
			}
		}while(invalid);
		System.out.println("*******************************************/");
		if(i==1){
			MyDag testdag = ResolveDag.TaskDagResolver("MeetAdvisor");
			TaskExecutor.callback(testdag);
		}else if(i==2){
			MyDag testdag = ResolveDag.TaskDagResolver("TryMeetingAdvisors");
			TaskExecutor.callback(testdag);
		}else if(i==3){
			MyDag testdag = ResolveDag.TaskDagResolver("MeetAdvisorWithBattery");
			TaskExecutor.callback(testdag);
		}else{
			MyDag testdag = ResolveDag.TaskDagResolver("TryMeetingAdvisorsWithBattery");
			TaskExecutor.callback(testdag);
		}
		Thread t1 = new Thread(new CapabilitySpawner());
		t1.start();
		TaskExecutor.execute();
	}
	
	public static void callback(MyDag dag){
		taskdag = dag;
	}
	
	public static void setUpConnection(){
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
		endings.addAll(taskdag.endCapabilities());
//		for(int id : endings){
//			System.out.println("Endings IDs: "+id);
//		}
		for(Input in : overallInput){
			//System.out.println("ID was:"+taskdag.isMappedTo(in.id).get(0).id);
//			System.out.print("User input required, please enter '"+in.name+"' of type '"+in.type+"': ");
			String inputLine = userInputTaker(in.name, true, in.type);
			for(IOPair io : taskdag.isMappedTo(in.id)){
				if(in.type.equals("int")){
					outputCache.put(io.id, Integer.parseInt(inputLine));
				}else if(in.type.equals("String")){
					outputCache.put(io.id, inputLine);
				}else if(in.type.equals("Boolean")){
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
		Collections.sort(startids);
		ConcurrentHashMap <Integer, String> map = new ConcurrentHashMap<Integer, String>();
		Boolean cannotStart;
		for(int id : startids){
			cannotStart = false;
			for(Input inp : taskdag.getCapabilityInputs(id)){
				if(!outputCache.containsKey(inp.id)){
					cannotStart = true;
				}
			}
			if(cannotStart){
				capabilityExecuteQueue.add(id);
//				System.err.println("Did not start capability "+taskdag.getCapabilityName(id)+", will wait");
			}else{
				map.put(id, taskdag.getCapabilityName(id));
			}
		}
		startCapabilities(map);
		//System.out.println("TaskExecutor:: Creating queues");
		//System.out.println("TaskExecutor:: Created queues!!");
		while(!exit){
		}
		log.warn(logString+"Cleaning up before exiting!");
		System.out.println("Cleaning up before exiting!");
		exitCleanly();
		log.warn(logString+"Bye!");
		System.out.println("Bye!");
		
	}
	
	private static void killAllThreads(){
		alive = false;
		capabilityExecuteQueue.clear();
		int seconds = 1;
		long start = System.currentTimeMillis();
		long end = start + seconds*1000; // seconds * 1000 ms/sec
		while (System.currentTimeMillis() < end)
		{
			// wait for any lagging capability spawns first!
		}
		for(int id : capabilitiesRunning){
			try {
				capabilityMessageMap.get(id).send(session.createTextMessage("Kill message"));
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
		alive = true;
	}
	
	private static void handleErrorOutput(String error){
		//method used for printing stuff for demo purposes. This will only print 
		//for meet advisor. Note that this is only used for demo, but the underlying
		//error handling functionality is correct. 
		if(error.equals("emailNotFound")){
			System.err.println("Task Executor :: Person was not listed in advisor directory (email not found)");
		}else if(error.equals("notAvailable")){
			System.err.println("Task Executor :: Advisor is currently listed as busy in the calendar");
		}else if(error.equals("photoNotFound")){
			System.err.println("Task Executor :: Advisor's photo was not found!");
		}	
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void registerOutputs(int id, ConcurrentHashMap<String, Object> outputMap){
		// get the outputs from actual capability using callback, store in local cache
//		System.out.println("Back in TE!!");
		int index = capabilitiesRunning.indexOf(id);
		if(index!=-1){
			capabilitiesRunning.remove(index);
		}
		capabilitiesFinished.add(id);
		semaphore--;
		Output[] outputs = taskdag.getCapabilityOutputs(id);
		for(Output out : outputs){
			if(outputMap.containsKey(out.name)){
				ArrayList<IOPair> mappings = taskdag.isMappedTo(out.id);
				for(IOPair mapping : mappings){
					if(mapping.mode.equals("stop")){
						semaphore = 0;
						killAllThreads();
					}
				}
				for(IOPair mapping : mappings){
					//for(Output finalOut : taskdag.getAllOverallOutput()){
						if(mapping.mode.equals("error")){
//							System.err.println("TaskExecutor:: Found an ERROR output!");
//							System.err.println("TaskExecutor:: Output "+out.name+" = "+outputMap.get(out.name));
							handleErrorOutput(out.name);
							id = -1;
						}
					//}
//					System.out.println("TaskExecutor :: ID: "+out.id+" name: "+out.name+" mapping ID: "+mapping.id);
					if(!outputCache.containsKey(mapping.id)){
						outputCache.put(mapping.id, outputMap.get(out.name));
					}else{
						System.err.println("Task Executor's capability output cache already contains an entry with ID "+mapping.id);
					}
				}
			}
		}
		tryNextCapability(id);
	}
	
	private static void tryNextCapability(int id){
//		System.out.println("Back in trynextCap, trying to execute "+id);
		boolean allDone = true;
		boolean cannotExecute = false;
		if(id>0){
			if(taskdag.nextCapabilities(id) == null || taskdag.nextCapabilities(id).size()==0){
				if(endings.contains(id)){
					//System.out.println("TaskExecutor :: Found an end capability");
					endings.remove(endings.indexOf(id));
				}
				if(endings.size() == 0){
					exit = true;
					return;
				}
			}
			for(int toStart : taskdag.nextCapabilities(id)){
				if(!capabilityExecuteQueue.contains(toStart)){
					capabilityExecuteQueue.add(toStart);
				}
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
						allDone = false;
					}else{
//						System.err.println("Did not start capability "+taskdag.getCapabilityName(capID)+", will wait");
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
						allDone = false;
					}else{
//						System.err.println("Did not start capability "+taskdag.getCapabilityName(capID)+" with ID "+capID+", will wait");
					}
			}
		}
		capabilityExecuteQueue.removeAll(toRemove);
		if(allDone && semaphore<=0){
			exit = true;
		}

	}
	
	private static ObjectMessage createCapabilityInputMessage(int id){
		Input[] inputs = taskdag.getCapabilityInputs(id);
		Boolean onlyUserLeft = true;
		for(Input in : inputs){
			if(!outputCache.containsKey(in.id)){
				for(IOPair io : taskdag.isMappingOf(in.id)){
					if(io.id!=0 && io.id!=1){
						onlyUserLeft = false;
					}
				}
			}
		}
		if(onlyUserLeft){
			for(Input in : inputs){
				for(IOPair io : taskdag.isMappingOf(in.id)){
//					System.out.println("TaskExecutor:: "+taskdag.getCapabilityName(id)+" has input "+in.name+" mapped with mode "+io.mode);
					if(io.id == 1 || io.id == 0){
//						System.out.println("TaskExecutor::USER INPUT REQUIRED:: Capability "
//								+taskdag.getCapabilityName(id)+" needs input "+in.name+
//								" of type "+in.type+">>");
						String inputLine = userInputTaker(io.mode.split("::")[1], false, in.type);
						if(in.type.equals("int")){
							outputCache.put(in.id, Integer.parseInt(inputLine));
						}else if(in.type.equals("String")){
							outputCache.put(in.id, inputLine);
						}else if(in.type.equals("Boolean")){
							outputCache.put(in.id, Boolean.parseBoolean(inputLine));
						}else if(in.type.equals("double")){
							outputCache.put(in.id, Double.parseDouble(inputLine));
						}else if(in.type.equals("float")){
							outputCache.put(in.id, Float.parseFloat(inputLine));
						}else{
							System.err.println("Unrecognized input type!!");
						}
					}
				}
			}
		}
		ConcurrentHashMap<String, Object> input = new ConcurrentHashMap<String, Object>();
		for(Input in : inputs){
			if(!outputCache.containsKey(in.id)){
//				System.err.println("Task Executor :: Input "+in.id+" has not been registered yet in TE. Will wait.");
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
//		System.out.println("Back in startValid!!");
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
		if(alive){
			try {
				semaphore++;
				send = session.createObjectMessage(spawnMsg);
				putQueue.send(send);
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
		return;
	}
	
	public static String userInputTaker(String prompt, Boolean startingCap, String type){
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		if(startingCap){
			System.out.print("Overall Task Input is required.\nPlease enter input '"+prompt+"' of type '"+type+"': ");
		}else{
			System.out.println(prompt);
		}
		String line = null;
		int i = 1;
		Boolean invalid;
		do{
			invalid = false;
			try {
				line = br.readLine();
				if(line == null){
					System.out.println("You entered a null! Please enter a valid input.");
					System.out.println("Please try again: ");
					invalid = true;
				}else if(line == "" || line == "\n" || line == "\t" || line.trim().isEmpty()){
					System.out.println("You entered an invalid character. Please enter valid input only.");
					System.out.println("Please try again: ");
					invalid = true;
				}else if(type.equals("int")){
					i = Integer.parseInt(line);
					if(i < 0){
						System.out.println("Invalid input! Please enter a positive integer only!");
						System.out.println("Please try again: ");
						invalid = true;
					}
				}
			}catch (IOException e) {
				invalid = true;
				e.printStackTrace();
			}catch(NumberFormatException nfe){
				System.err.println("Invalid input! Please enter an integer only!");
				System.out.println("Please try again: ");
				invalid = true;
			}
		}while(invalid);
		
		return line;
	}
	
	public static void capabilitySpawnerReplyRegister(ConcurrentHashMap<Integer, String> capabilityQueues){
		createCapabilityMessageQueues(capabilityQueues);
		if(!startingCapabilities){
			for(int id : capabilityQueues.keySet()){
//				System.out.println("TaskExecutor:: Sending to testing "+capabilityQueues.get(id)+":: "+id);
				try {
					capabilityMessageMap.get(id).send(preparedMessages.get(id));
					capabilitiesRunning.add(id);
				} catch (JMSException e) {
					System.err.println("Could not send message to Capability!");
					e.printStackTrace();
				}
			}
		}else{
			startingCapabilities = false;
			
			for(int id : capabilityMessageMap.keySet()){
//				System.out.println("TaskExecutor:: Sending to "+capabilityQueues.get(id)+":: "+id);
				ConcurrentHashMap<String, Object> input = new ConcurrentHashMap<String, Object>();
				Input[] inputs = taskdag.getCapabilityInputs(id);
				Boolean onlyUserLeft = true;
				for(Input in : inputs){
					if(!outputCache.containsKey(in.id)){
						for(IOPair io : taskdag.isMappingOf(in.id)){
							if(io.id!=0 && io.id!=1){
								onlyUserLeft = false;
							}
							if(io.mode.equals("stop")){
								semaphore--;
							}
						}
					}
				}
				if(onlyUserLeft){
					for(Input in : inputs){
						for(IOPair io : taskdag.isMappingOf(in.id)){
							//System.out.println("TaskExecutor:: "+taskdag.getCapabilityName(id)+" has input "+in.name+" mapped with mode "+io.mode);
							if(io.id == 1 || io.id == 0){
								//System.out.println("TaskExecutor::USER INPUT REQUIRED:: Capability "
								//+taskdag.getCapabilityName(id)+" needs input "+in.name+
								//" of type "+in.type+">>");
								String inputLine = userInputTaker(io.mode.split("::")[1], false, in.type);
								if(in.type.equals("int")){
									outputCache.put(in.id, Integer.parseInt(inputLine));
								}else if(in.type.equals("String")){
									outputCache.put(in.id, inputLine);
								}else if(in.type.equals("Boolean")){
									outputCache.put(in.id, Boolean.parseBoolean(inputLine));
								}else if(in.type.equals("double")){
									outputCache.put(in.id, Double.parseDouble(inputLine));
								}else if(in.type.equals("float")){
									outputCache.put(in.id, Float.parseFloat(inputLine));
								}else{
									System.err.println("Unrecognized input type!!");
								}
							}
						}
					}

					for(Input in : inputs){
						if(!outputCache.containsKey(in.id)){
							//System.err.println("Task Executor :: Input "+in.id+"has not been registered yet in TE for start capabilities! Fatal!");
						}else{
							input.put(in.name, outputCache.get(in.id));
						}
					}
				}
				ObjectMessage tmpMsg;
				try {
					tmpMsg = session.createObjectMessage(input);
					capabilityMessageMap.get(id).send(tmpMsg);
					capabilitiesRunning.add(id);
				} catch (JMSException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void exitCleanly(){
		killAllThreads();
        try {
			putQueue.send(session.createTextMessage("Kill message"));
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
//			System.out.println(o.hashCode());
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
