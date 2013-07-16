/************************************************************
 * 
 * GizmoE Practicum
 * Interface for in-memory representation of the task DAG
 * This will contain a list of capabilities (nested capability class container)
 * It will use 2 adjacency matrices for execution and io
 * The matrices can employ ids directly, as ids are unique
 * 
 * Author: Upsham Dawra(ukd)
 * Version: 1.0
 * 
 * Do not change this interface
 * 
 ************************************************************/
package gizmoe.taskdag;

import java.util.ArrayList;

public interface MyDagInterface { 
	/**
	 * Add a capability vertex to graph
	 * @param capability name of the capability in the DB
	 * @param id unique identifier assigned by TTR
	 * @param inputs an array of all inputs
	 * @param outputs an array of all outputs
	 * @return success/failure
	 */
	boolean addCapability(String capability, int id, Input[] inputs, Output[] outputs);
	
	/**
	 * Connect capability vertices for execution
	 * @param firstCapabilityid id of the capability
	 * @param nextCapabilityid id of next capability
	 * @return success/failure
	 */
	boolean connect(int firstCapabilityid, int nextCapabilityid);
	
	/**
	 * Map the IO of the capability to each other
	 * @param firstIoID The IO (port) to be connected (eg output)
	 * @param nextIoID The IO (port) that firstIoID is connected to (eg input)
	 * @return success/failure
	 */
	boolean mapIO(int firstIoID, int nextIoID, String mode);
	
	/**
	 * Add the Collected inputs and outputs
	 * @param overallInputs Array of collected inputs
	 * @param overallOutputs Array of collected outputs
	 * @return success/failure
	 */
	void addOverallIO(Input[] overallInputs, Output[] overallOutputs);
	
	/**
	 * Spits out a capability name given the id
	 * @param id identifier of the capability
	 * @return String containing the name of the ID
	 */
	String getCapabilityName(int id);
	
	/**
	 * Gets all inputs of a capability stored in the data structure
	 * @param id identifier of the capability
	 * @return Array of all inputs
	 */
	Object[] getCapabilityInputs(int id);
	
	/**
	 * Gets all outputs of a capability stored in the data structure
	 * @param id identifier of the capability
	 * @return Array of all outputs
	 */
	Object[] getCapabilityOutputs(int id);
	
	/**
	 * Gets the io mappings for a certain input/output
	 * @param ioID identifier of the io
	 * @return arraylist containing the ids of what this maps to
	 */
	ArrayList<IOPair> isMappedTo(int ioID);
	
	/**
	 * Gets the set of next capabilities to execute
	 * @param id identifier of the capability just finished
	 * @return arraylist containing the ids of the next capabilities to execute
	 */
	ArrayList<Integer> nextCapabilities(int id);
	
	/**
	 * Finds which capabilities (in parallel) join at this capability
	 * This helps TaskExecutor to know which capabilities to 'wait' for!
	 * @param id identifier of the capability that is the joining point
	 * @return arraylist containing the capabilities ids that join at 'id'
	 */
	ArrayList<Integer> joinToBecome(int id);
	
	/**
	 * Simple checker which tells TaskExecutor is a capability is a 
	 * joining point or not.
	 * @param id identifier of the capability that is to be checked
	 * @return true/false
	 */
	boolean isJoin(int id);
	
	/**
	 * Gets what maps to (like the 'domain' of a function/mapping) 
	 * of the specific io
	 * @param ioID identifier of the io
	 * @return arraylist containing the ids of the 'domain' io
	 */
	ArrayList<IOPair> isMappingOf(int ioID);
	
	
	/**
	 * Set a certain capability as the starting capability
	 * @param id the id of the capability which is the starting capability
	 * @return success/failure
	 */
	public boolean setStartCapability(int id);
	
	/**
	 * Returns the starting capabilities 
	 * @return An arraylist containing IDs of starting capabilities
	 */
	public ArrayList<Integer> startCapabilities();
	
	/**
	 * Efficiently returns all the end capabilities 
	 * @return An arraylist containing IDs of end capabilities
	 */
	public ArrayList<Integer> endCapabilities();
	
	/**
	 * Since finding end capabilities is computationally intensive,
	 * precompute them so that endCapabilities is efficient. This
	 * is done only once, and we recommend that you call this method
	 * yourself right after the dag is created. Otherwise, the endCapabilities
	 * method will call it automatically.
	 */
	public void preComputeEndCapabilities();
	
	/**
	 * Clear all the data in the data structures
	 * @return success/failure
	 */
	void emptyDAG();
	
}

