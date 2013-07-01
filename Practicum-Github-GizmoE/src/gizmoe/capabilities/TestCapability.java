package gizmoe.capabilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ConcurrentHashMap;

public class TestCapability extends CapabilityBase{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public ConcurrentHashMap<String, Object> body(ConcurrentHashMap<String, Object> inputs) {
		System.out.println("From TestCapability: thread"+this.hashCode());
		try {
			File file = new File("CS_MultiJVM_Test.txt");
			 
    		//if file doesn't exists, then create it
    		if(!file.exists()){
    			file.createNewFile();
    		}
 
    		//true = append file
    		FileWriter fileWriter = new FileWriter(file.getName(),true);
    	    BufferedWriter bufferWriter = new BufferedWriter(fileWriter);
    	    bufferWriter.write("thread"+this.hashCode());
    	    bufferWriter.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
