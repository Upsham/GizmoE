/************************************************************
 * 
 * GizmoE Practicum
 * Actual DAG implementation
 * 
 * Author: Upsham Dawra(ukd)
 * Version: 1.0
 * 
 * 
 ************************************************************/
package gizmoe.taskdag;

import java.util.ArrayList;
import java.util.HashMap;

public class MyDag implements MyDagInterface{

	HashMap<Integer, Capability> capabilityMap;
	ArrayList <Input> overallInputMap;
	ArrayList<Output> overallOutputMap;	
	ArrayList<ArrayList<Boolean>> connectMap;
	ArrayList<ArrayList<IOMapping>> ioMap;
	HashMap<Integer, Integer> internalID;
	HashMap<Integer, Integer> internalIOID;
	ArrayList<Integer> originalIOID;
	private int capabilityID = 0;
	private int ioID = 0;
	ArrayList<Integer> endCapabList;
	
	public MyDag(){
		internalID = new HashMap<Integer, Integer>();
		internalIOID = new HashMap<Integer, Integer>();
		capabilityMap = new HashMap<Integer, Capability>();
		connectMap = new ArrayList<ArrayList<Boolean>>();
		ioMap = new ArrayList<ArrayList<IOMapping>>();
		originalIOID = new ArrayList<Integer>();
		overallInputMap = new ArrayList <Input>();
		overallOutputMap = new ArrayList <Output>();
		endCapabList = new ArrayList<Integer>();

	}
	
	private class Capability {
		ArrayList<Input> inputs = new ArrayList<Input>();
		ArrayList<Output> outputs = new ArrayList<Output>();
		public String name;
		public int id;
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
	
	private class IOMapping {
		String mode;
		boolean connected;
		
		public IOMapping(String mode, boolean conn){
			this.mode = mode;
			this.connected = conn;
		}
	}
	
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

	@Override
	public void addOverallIO(Input[] overallInputs, Output[] overallOutputs) {
		for(Input in : overallInputs){
			overallInputMap.add(in);
			internalIOID.put(in.id, ioID++);
			originalIOID.add(in.id);
			Grow2DArrayIO(ioMap);
		}
		for(Output out : overallOutputs){
			overallOutputMap.add(out);
			internalIOID.put(out.id, ioID++);
			originalIOID.add(out.id);
			Grow2DArrayIO(ioMap);
		}		
	}

	public ArrayList<Input> getAllOverallInput(){
		return overallInputMap;
	}
	
	public ArrayList<Output> getAllOverallOutput(){
		return overallOutputMap;
	}
	
	
	@Override
	public String getCapabilityName(int id) {
		Capability cap = capabilityMap.get(internalID.get(id));
		return cap.name;
	}

	@Override
	public Input[] getCapabilityInputs(int id) {
		Capability cap = capabilityMap.get(internalID.get(id));
		Input[] in = new Input[cap.inputs.size()];
		cap.inputs.toArray(in);
		return in;
	}

	@Override
	public Output[] getCapabilityOutputs(int id) {
		Capability cap = capabilityMap.get(internalID.get(id));
		Output[] out = new Output[cap.outputs.size()];
		cap.outputs.toArray(out);
		return out;
	}

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
	
	public void preComputeEndCapabilities(){
		if(!endCapabList.isEmpty()){
			return;
		}
		Boolean isEnd = true;
		for(int i = 1; i<connectMap.size(); i++){
			isEnd = true;
			for(int j = 0; j<connectMap.get(i).size(); j++ ){
				if(connectMap.get(i).get(j)){
					isEnd = false;
					break;
				}
			}
			if(isEnd){
				endCapabList.add(capabilityMap.get(i).id);
			}
		}
	}
	
	public ArrayList<Integer> endCapabilities(){
		if(!endCapabList.isEmpty()){
			this.preComputeEndCapabilities();
		}
		return endCapabList;
	}
	
	
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
	
	public ArrayList<Integer> startCapabilities(){
		ArrayList<Integer> starters = new ArrayList<Integer>();
		for(int i = 0; i<connectMap.size(); i++){
			if(connectMap.get(i).get(i)){
				starters.add(capabilityMap.get(i).id);
			}
		}
		return starters;
	}
	
	public boolean setStartCapability(int id){
		if(internalID.containsKey(id)){
			connect(id,id);
			return true;
		}else{
			return false;
		}
	}

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
