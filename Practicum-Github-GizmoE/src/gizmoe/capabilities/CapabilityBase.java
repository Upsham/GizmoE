package gizmoe.capabilities;

import java.io.Serializable;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

public abstract class CapabilityBase implements Runnable, Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static String url = ActiveMQConnection.DEFAULT_BROKER_URL;
    protected MessageConsumer getQueue;
    protected MessageProducer putQueue;
    protected Connection connection;
    protected  Session session;
	public void setUp(){
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void run() {
		body();
	}
	
	public abstract void body();
}
