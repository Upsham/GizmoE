/************************************************************
 * 
 * GizmoE Practicum
 * Implementation of MyDagInterface
 * 
 * Author: Upsham Dawra(ukd)
 * Version: 1.0
 * 
 * 
 ************************************************************/
package gizmoe.taskdag;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Class MyDag implements interface MyDagInterface
 * @author ukd (implementation)
 * @author sindhusatish (documentation)
 * @see MyDagInterface document
 */
public class MyDag implements MyDagInterface{

  /**
	 * capabilityMap has been designed to store the values
	 * of the type "capability", each corresponding to a 
	 * unique key, an internal capability id of type "Integer"
	 */
	HashMap<Integer, Capability> capabilityMap;
	/**
	 * overallInputMap has been designed to store values
	 * of the type "Input", each corresponding to a 
	 * unique key, an internal input id of type "Integer"
	 * <p>
	 * The overall inputs to the task are stored
	 * @see Input.java
	 */
	HashMap<Integer, Input> overallInputMap;
	/**
	 * overallOutputMap has been designed to store values
	 * of the type "Output", each corresponding to a 
	 * unique key, an internal output id of type "Integer"
	 * <p>
	 * The overall outputs of the task are stored
	 * @see Output.java
	 */
	HashMap<Integer, Output> overallOutputMap;
	/**
	 * connectMap has been designed to store values
	 * of the type "Boolean" 
	 * <p>
	 * connectMap is a 2D array that shows the connections
	 * between the capabilities that are added to the MyDag
	 * data structure. Imagine a 2D array representation of
	 * a directed acylic graph of capabilities. 1 corresponds 
	 * to a presence of connection between two cells and 0 
	 * corresponds to a lack of connection. 1 in the cell with
	 * same index corresponds to a start capability also known
	 * as first capability. 
	 */
	ArrayList<ArrayList<Boolean>> connectMap;
	/**
	 * ioMap has been designed to store values
	 * of the type "IOMapping" 
	 * <p>
	 * ioMap is a 2D array that shows the connections
	 * between the io for the capabilities that are added to MyDag
	 * data structure. Imagine a 2D array representation of
	 * that shows mappings of inputs to outputs, inputs to inputs
	 * outputs to outputs, outputs to inputs. 
	 * @see Class IOMapping in MyDag.java
	 */
	ArrayList<ArrayList<IOMapping>> ioMap;
	/**
	 * internalID has been designed to store values
	 * of the type "Integer" which is the original id
	 * of the capability from the Task XML, each corresponding
	 *  to a unique key, an internal capability id of type "Integer"
	 * <p>
	 * internalID is used to retain a mapping of internal ids and 
	 * original ids of the capabilities. Every capability is assumed 
	 * to have a unique id.
	 * @see AssumptionsOnTaskLanguage document
	 */
	HashMap<Integer, Integer> internalID;
	/**
	 * internalIOID has been designed to store values
	 * of the type "Integer" which is the original id
	 * of the io from the Task XML, each corresponding
	 *  to a unique key, an internal io id of type "Integer"
	 * <p>
	 * internalIOID is used to retain a mapping of internal ids and 
	 * original ids of the io. 
	 * @see AssumptionsOnTaskLanguage document
	 */
	HashMap<Integer, Integer> internalIOID;
	/**
	 * originalIOID has been designed to store values
	 * of the type "Integer" which is the original id
	 * of the io from the Task XML
	 * <p>
	 * originalIOID is used to maintain a list of original ids.
	 * An arraylist has been used to reduce the performance 
	 * overhead while lookup. The internal ids assigned to the io
	 * are continuous and hence an arraylist whose index is used for
	 * lookup makes search for orginal ids faster than a hashmap.
	 * IO mappings can be very large and hence the decision. 
	 * @see AssumptionsOnTaskLanguage document
	 */
	ArrayList<Integer> originalIOID;
	/**
	 * capabilityID is set to 0 and incremented when a new
	 * capability is added.
	 * <p>
	 * The incremented value is assigned as the internal 
	 * capability id for the newly added capability.
	 * @since original capability ids are stored in the
	 * capabilityMap as type "Capability" which contains 
	 * original ids, there is no need of an originalCapabilityID.
	 * In addition, there is no overhead on lookup.
	 */
	private int capabilityID = 0;
	/**
	 * ioID is set to 0 and incremented when a new io
	 * mapping is added.
	 * <p>
	 * The incremented value is assigned as the internal 
	 * io id for the newly added capability's io mapping.
	 */
	private int ioID = 0;

	
	/**
	 * Constructor MyDag, allocates memory to the data structures
	 */
	public MyDag(){
		internalID = new HashMap<Integer, Integer>();
		internalIOID = new HashMap<Integer, Integer>();
		capabilityMap = new HashMap<Integer, Capability>();
		connectMap = new ArrayList<ArrayList<Boolean>>();
		ioMap = new ArrayList<ArrayList<IOMapping>>();
		originalIOID = new ArrayList<Integer>();
		overallInputMap = new HashMap<Integer, Input>();
		overallOutputMap = new HashMap<Integer, Output>();

	}

	/**
	 * Class Capability defines a Capability.
	 * <p>
	 * This class defines a capability, which consists of a set
	 * of inputs, a set of outputs, a name, and an id.
	 */
	private class Capability {
		ArrayList<Input> inputs = new ArrayList<Input>();
		ArrayList<Output> outputs = new ArrayList<Output>();
		public String name;
		public int id;
		/**
		 * Capability constructor that creates Capability objects.
		 * <p>
		 * Assigns id, capability name and the list of inputs and outputs
		 * that comprise a capability.
		 * @param inputs, It is an arraylist of type "Input"
	     * @param outputs, It is an arraylist of type "Output"
	     * @param capability, It is the name of a capability of type "String"
	     * @param id, It is the id of the capability of type "int"
	     * @see MyDagInterface document
		 */
		public Capability(String capability, Input[] inputs,
				Output[] outputs, int id){
			this.id = id;
			this.name = capability;
			for(Input i : inputs){
				this.inputs.add(i);
			}
			for(Output i : outputs){
				this.outputs.add(i);
			}
		}
	}

	/**
	 * Class IOMapping defines an IO mapping.
	 * <p>
	 * This class defines an IO mapping, which consists of
	 * a mode and connection. The mode defines the way in which
	 * IO is connected. IO can have copy, pipe, user, etc. as modes.
	 * When an IO is connected, it is set to true else false.
	 * @see MyDagInterface document, AssumptionsOntaskLanguage document
	 */
	private class IOMapping {
		String mode;
		boolean connected;

		/**
		 * IOMapping Constructor that creats IOMapping objects.
		 * <p>
		 * Assigns a mode and connection value that comprises an
		 * IOMapping.
		 * @param mode, It is the mode in which the IO is connected of type "String"
	     * @param conn, It is a "boolean"; true for a connection, false for no connection
		 */
		public IOMapping(String mode, boolean conn){
			this.mode = mode;
			this.connected = conn;
		}
	}
	
	
	/** 
	 * addCapability method adds a capability vertex to graph; Mydag data structure.
	 * @see gizmoe.taskdag.MyDagInterface#addCapability(java.lang.String, int, gizmoe.taskdag.Input[], gizmoe.taskdag.Output[])
	 *<p>
	 *This method adds a capability vertex to the graph, i.e. MyDag data structure.
	 * It does this by first checking if the id of the capability being added is already present in the internalID, 
	 * if it is not present, it adds the id to the internalID. Then it creates a capability node by instantiating 
	 * an object of class Capability. Then it adds this new capability into capabilityMap and calls Grow2DArray, 
	 * which creates a two dimensional array with this capability’s index. Finally it adds the inputs and outputs
	 * for the capability and calls Grow2DArrayIO, which creates a two dimensional array with these IO ids.
	 */
	public boolean addCapability(String capability, int id, Input[] inputs,
			Output[] outputs) {
		if(internalID.containsKey(id))
		{
			return false;
		}else{
			internalID.put(id, capabilityID);
		}
		Capability newCap = new Capability(capability, inputs, outputs, id);
		capabilityMap.put(capabilityID++, newCap);
		Grow2DArray(connectMap);

		for(Input in : inputs){
			internalIOID.put(in.id, ioID++);
			originalIOID.add(in.id);
			Grow2DArrayIO(ioMap);
		}
		for(Output out : outputs){
			internalIOID.put(out.id, ioID++);
			originalIOID.add(out.id);
			Grow2DArrayIO(ioMap);
		}
		return true;
	}

	private void Grow2DArray(ArrayList<ArrayList<Boolean>> anyMap){

		int rows = 0;
		if(anyMap.isEmpty()){
			anyMap.add(new ArrayList<Boolean>());
			anyMap.get(0).add(false);
		}else{

			for(ArrayList<Boolean> row : anyMap){
				row.add(false);
				rows++;
			}
			ArrayList<Boolean> toAdd = new ArrayList<Boolean>();
			while(rows>=0){
				toAdd.add(false);
				rows--;
			}
			anyMap.add(toAdd);
		}

	}

	private void Grow2DArrayIO(ArrayList<ArrayList<IOMapping>> anyMap){

		int rows = 0;
		IOMapping untrue = new IOMapping("N/A",false);
		if(anyMap.isEmpty()){
			anyMap.add(new ArrayList<IOMapping>());
			anyMap.get(0).add(untrue);
		}else{

			for(ArrayList<IOMapping> row : anyMap){
				row.add(untrue);
				rows++;
			}
			ArrayList<IOMapping> toAdd = new ArrayList<IOMapping>();
			while(rows>=0){
				toAdd.add(untrue);
				rows--;
			}
			anyMap.add(toAdd);
		}
	}

	/** 
	 * connect method connects a capability vertex to another capability vertex
	 *<p>
	 *This method sets the particular cell in the 2D array to true for the two capabilities
	 * being passed to it. It does this by accessing the row whose index is the firstcapability
	 * ID in the connectMap. Then it checks if the row doesn’t contain null and that its size 
	 * is greater than the id of the nextCapability, and then sets the nextCapability cell true
	 *  within the row of the firstcapability.
	 */
	@Override
	public boolean connect(int firstCapabilityid, int nextCapabilityid) {
		ArrayList<Boolean> row = connectMap.get(internalID.get(firstCapabilityid));
		if(row != null && row.size() > internalID.get(nextCapabilityid)){
			row.set(internalID.get(nextCapabilityid), true);
			return true;
		}else{
			return false;
		}
	}
	/** 
	 * mapIO method maps IO to the capabilities added
	 *<p>
	 *This method follows a very similar logic as connect(). 
	 *Here the cells of the 2D array do not contain Boolean, instead, they 
	 *contain IOMappings which has a mode and a boolean connected. Initially connected is
	 * set to false in the IOMapping Class. 
	 */
	@Override
	public boolean mapIO(int firstIoID, int nextIoID, String mode) {
		IOMapping notFalse = new IOMapping(mode, true);
		ArrayList<IOMapping> row = ioMap.get(internalIOID.get(firstIoID));
		if(row != null && row.size() > internalIOID.get(nextIoID)){
			row.set(internalIOID.get(nextIoID), notFalse);
			return true;
		}else{
			return false;
		}
	}
	/** 
	 * addOverallIO method adds the overall IO for the task
	 *<p>
	 *This method adds the overall inputs and outputs of a task into the DAG.
	 *It does this by again creating new internal ids for these and added them in to the 2D IO array. 
	 */
	@Override
	public void addOverallIO(Input[] overallInputs, Output[] overallOutputs) {
		for(Input in : overallInputs){
			internalIOID.put(in.id, ioID++);
			originalIOID.add(in.id);
			Grow2DArrayIO(ioMap);
		}
		for(Output out : overallOutputs){
			internalIOID.put(out.id, ioID++);
			originalIOID.add(out.id);
			Grow2DArrayIO(ioMap);
		}		
	}
	/** 
	 * getCapabilityName retrieves the name of the capability whose ID is passed as parameter
	 *<p>
	 *This method gets the name of the capability whose id has been passed. It retrieves this from
	 *the capabilityMap which is a mapping between internal ids and capabilities (which contains original ids and names)
	 */
	@Override
	public String getCapabilityName(int id) {
		Capability cap = capabilityMap.get(internalID.get(id));
		return cap.name;
	}
	/** 
	 * getCapabilityInputs retrieves the inputs of the capability whose ID is passed as parameter
	 *<p>
	 *This method gets the inputs to the capability whose id has been passed. It does this by first 
	 *retrieving the capability, then creates a array of Input objects and copies the inputs of the
	 *capability into it and returns it.
	 */
	@Override
	public Input[] getCapabilityInputs(int id) {
		Capability cap = capabilityMap.get(internalID.get(id));
		Input[] in = new Input[cap.inputs.size()];
		cap.inputs.toArray(in);
		return in;
	}
	/** 
	 * getCapabilityOutputs retrieves the outputs of the capability whose ID is passed as parameter
	 *<p>
	 *Same as getCapabilityInputs(), instead it creates an array of Output objects.
	 */
	@Override
	public Output[] getCapabilityOutputs(int id) {
		Capability cap = capabilityMap.get(internalID.get(id));
		Output[] out = new Output[cap.outputs.size()];
		cap.outputs.toArray(out);
		return out;
	}
	/** 
	 *isMappedTo retrieves the IO pairs of ioID passed as a parameter
	 *<p>
	 *This method gets the IOMappings of a certain input or output by
	 *checking the ioMap data structure (it contains IOPair for the capabilities).
	 */
	@Override
	public ArrayList<IOPair> isMappedTo(int ioID) {
		ioID = internalIOID.get(ioID);
		ArrayList<IOPair> mappings = new ArrayList<IOPair>(10);
		for(int i = 0; i<ioMap.get(ioID).size(); i++){
			if(ioMap.get(ioID).get(i).connected){
				mappings.add(new IOPair(ioMap.get(ioID).get(i).mode,originalIOID.get(i)));
			}
		}
		return mappings;
	}
	/** 
	 *isMappingOf retrieves the IO pairs of ioID to which is a mapping of
	 *<p>
	 *This method gets the domain of an IO mapping, i.e it retrieves 
	 *the IOPair that is mapped to the ioID passed as a parameter by checking the ioMap data structure.
	 */
	@Override
	public ArrayList<IOPair> isMappingOf(int ioID) {
		ArrayList<IOPair> mappings = new ArrayList<IOPair>(10);
		ioID = internalIOID.get(ioID);
		for(int i = 0; i<ioMap.size(); i++){
			if(ioMap.get(i).get(ioID).connected){
				mappings.add(new IOPair(ioMap.get(i).get(ioID).mode,originalIOID.get(i)));
			}
		}
		return mappings;
	}

	/** 
	 *nextCapabilities retrieves the capability ids which come after the id being passed
	 *<p>
	 *This method gets the id’s of the capabilities that are next in line for execution
	 *after the capability whose id is passed as a parameter by checking the connectMap 
	 *for cells with the same row index as id being passed as parameter.
	 */
	@Override
	public ArrayList<Integer> nextCapabilities(int id) {
		int oldid = id;
		id = internalID.get(id);
		ArrayList<Integer> next = new ArrayList<Integer>();
		for(int i = 0; i<connectMap.get(id).size(); i++){
			if(connectMap.get(id).get(i)){
				if(oldid!=capabilityMap.get(i).id){
					next.add(capabilityMap.get(i).id);
				}
			}
		}
		return next;
	}
	/** 
	 *joinToBecome retrieves the capability ids which joins at the id being passed 
	 *<p>
	 *This method gets the id’s of the capabilities that join at the capability whose id is passed as a parameter.
	 */
	@Override
	public ArrayList<Integer> joinToBecome(int id){
		id = internalID.get(id);
		ArrayList<Integer> joiners = new ArrayList<Integer>();
		for(int i = 0; i<connectMap.size(); i++){
			if(connectMap.get(i).get(id) && id != i){
				joiners.add(capabilityMap.get(i).id);
			}
		}
		return joiners;
	}
	/** 
	 *isJoin checks whether the id being passed is a join capability or not
	 *<p>
	 *This method checks whether the capability whose id is passed as a parameter 
	 *is a joining point or not by checking the connectMap data structure. A capability 
	 *is joining point if any cell in the connectMap is set to true whose column index 
	 *is same as the id being checked for. It returns a Boolean.
	 */
	@Override
	public boolean isJoin(int id){
		id = internalID.get(id);
		int connectsWithID = 0;
		for(int i = 0; i<connectMap.size(); i++){
			if(connectMap.get(i).get(id) && id!=i){
				connectsWithID++;
				if(connectsWithID >= 2){
					return true;
				}
			}
		}
		return false;
	}
	/** 
	 *startCapabilities retrieves the capability ids of the starting capabilities
	 *<p>
	 *This method returns the set of id’s of the start capabilities which are 
	 *identified by a true in the cells of the connectMap data structure 
	 *(2D array that stores the connections between capabilities) for the same row and column index.
	 */
	public ArrayList<Integer> startCapabilities(){
		ArrayList<Integer> starters = new ArrayList<Integer>();
		for(int i = 0; i<connectMap.size(); i++){
			if(connectMap.get(i).get(i)){
				starters.add(capabilityMap.get(i).id);
			}
		}
		return starters;
	}
	/** 
	 *setStartCapabilities sets the start capability
	 *<p>
	 *This method sets the start capability by setting 
	 *the cell in connectMap (2D array that stores the 
	 *connections between capabilities) to true with 
	 *the same row and column index as that of the id being passed.
	 */
	public boolean setStartCapability(int id){
		if(internalID.containsKey(id)){
			connect(id,id);
			return true;
		}else{
			return false;
		}
	}
	/** 
	 *emptyDAG empties the MyDAG data structure
	 *<p>
	 *This method empties all the data structures 
	 *used to in MyDAG, i.e. the data structures 
	 *used to store the capabilities, capability connections, 
	 *IO, IO mappings (see the descriptions of the data structures above). 
	 */
	@Override
	public void emptyDAG() {
		internalID.clear();
		internalIOID.clear();
		capabilityMap.clear();
		connectMap.clear();
		ioMap.clear();
		originalIOID.clear();
		overallInputMap.clear();
		overallOutputMap.clear();
		capabilityID = 0;
		ioID = 0;
	}


}
