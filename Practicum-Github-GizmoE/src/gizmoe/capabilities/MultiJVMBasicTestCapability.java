package gizmoe.capabilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

public class MultiJVMBasicTestCapability implements Runnable, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void run() {
		try {
			File file = new File("CS_MultiJVM_Test.txt");
			 
    		//if file doesn't exists, then create it
    		if(!file.exists()){
    			file.createNewFile();
    		}
 
    		//true = append file
    		FileWriter fileWriter = new FileWriter(file.getName(),true);
    	    BufferedWriter bufferWriter = new BufferedWriter(fileWriter);
    	    bufferWriter.write("thread"+this.hashCode()+"\n");
    	    bufferWriter.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
