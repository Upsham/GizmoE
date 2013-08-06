package gizmoe.capabilities;

import java.io.Serializable;
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

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CapabilityBase implements Runnable, Serializable {
    /**
	 * 
	 */
	//final static Logger log = LoggerFactory.getLogger(getClass());	
	private static final long serialVersionUID = 1L;
	protected static String url = ActiveMQConnection.DEFAULT_BROKER_URL;
    protected MessageConsumer getQueue;
    protected MessageProducer putQueue;
    protected Connection connection;
    protected Session session;
    private String name;
	public CapabilityBase(String capabilityName){
		this.name = capabilityName;
		final Logger log = LoggerFactory.getLogger(getClass());	
		log.info("From capabilitybase");
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
        Destination destination = session.createQueue("thread"+this.hashCode());
        Destination replydest = session.createQueue("thread"+this.hashCode()+"reply");

        // MessageConsumer is used for receiving (consuming) messages
        getQueue = session.createConsumer(destination);
        putQueue = session.createProducer(replydest);
		}catch (JMSException e) {
			e.printStackTrace();
		}
	}
	
	public void exitCleanly(){
		try {
			connection.stop();
			connection.close();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
	public void run() {
		try {
			Message inMsg = getQueue.receive();
			if(inMsg instanceof ObjectMessage){
				//TODO what message do we want? For now, serialized hashmap
				if(((ObjectMessage) inMsg).getObject() instanceof ConcurrentHashMap<?, ?>){
					@SuppressWarnings("unchecked")
					ConcurrentHashMap<String, Object> inputs = (ConcurrentHashMap<String, Object>) ((ObjectMessage) inMsg).getObject();
					
					Class<?> c = Class.forName("gizmoe.capabilities."+name);
					Constructor<?> constructor = c.getConstructor(ConcurrentHashMap.class);
					Object o = constructor.newInstance(inputs);
					Thread t1 = new Thread((Runnable) o);
					t1.start();
					
					while(t1.isAlive()){
						Message kill = getQueue.receiveNoWait();
						if(kill!=null){
							System.out.println("CapabilityBase "+this.hashCode()+"::Killing the thread for "+name);
							try{
							t1.interrupt();
							this.exitCleanly();
							return;
							}catch(Exception e){
								e.printStackTrace();							
							}
						}
					}
					ObjectMessage outMsg = session.createObjectMessage(inputs);
					putQueue.send(outMsg);
					
				}
			}else{
				return;
			}
		} catch (JMSException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InvocationTargetException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SecurityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (NoSuchMethodException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		this.exitCleanly();
		return;
	}
	
}
