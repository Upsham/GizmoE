package gizmoe.unittest.taskexecutor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import gizmoe.taskexecutor.TaskExecutor;

public class MultiJVMTest_TaskExecutor {

	/**
	 * @param args
	 */
	@Deprecated
	public static void main(String[] args) {
		System.out.println("This test assumes that MultiJVMTest_CapSpawner is running in a different process");
		System.out.println("If MultiJVMTest_CapSpawner is running and you are ready to begin, type 'y'");
		System.out.println("Else, hit enter to exit");
		System.out.print("\n\n>>> ");
		Scanner in = new Scanner(System.in);
		String input = in.nextLine();
		if(input.equalsIgnoreCase("y")){
			TaskExecutor.setUpConnection();
			ConcurrentHashMap <Integer, String> map = new ConcurrentHashMap<Integer, String>();
			map.put(1, "MultiJVMBasicTestCapability");
			map.put(2, "MultiJVMBasicTestCapability");

			//ConcurrentHashMap <Integer, String> capabilityQueues = TaskExecutor.startCapabilities(map);
			// Note : This test WILL NOT work anymore, due to aync changes in TE. However, it is STILL MultiVM
			// and this has been verified separately
			try {
				File file = new File("TE_MultiJVM_Test.txt");
				 
	    		//if file doesn't exists, then create it
	    		if(!file.exists()){
	    			file.createNewFile();
	    		}
	 
	    		FileWriter fileWriter = new FileWriter(file.getName(), false);
	    	    BufferedWriter bufferWriter = new BufferedWriter(fileWriter);
//				for(String str : capabilityQueues.values()){
//		    	    bufferWriter.write(str+"\n");
//				}
	    	    bufferWriter.close();

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			TaskExecutor.exitCleanly();	
			
			System.out.println("Now run MultiJVMUnitTest_OutputCheck to verify output!");
		}
		
	}

}
