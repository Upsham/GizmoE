package gizmoe.run;

import gizmoe.TaskDagResolver.ResolveDag;
import gizmoe.taskdag.MyDag;
import gizmoe.taskexecutor.CapabilitySpawner;
import gizmoe.taskexecutor.TaskExecutor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class RunSystem {

	/**
	 * Run the system via console!
	 */
	public static void main(String[] args) {
		System.out.println("/*******************************************");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Welcome to the GizmoE Demo Console\nPlease make sure that ActiveMQ is running!");
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

}
