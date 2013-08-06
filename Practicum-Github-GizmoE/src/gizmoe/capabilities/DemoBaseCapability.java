package gizmoe.capabilities;

import gizmoe.taskexecutor.TaskExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DemoBaseCapability implements Runnable{
	//This class is only used for demo. If not demo-ing, change all capabilities to simply
	//implement Runnable interface :)
	protected long seconds = 5;//The amount of delay that you want in each capability
	protected static Logger log = LoggerFactory.getLogger(TaskExecutor.class);
	protected final String logTag = "";
}
