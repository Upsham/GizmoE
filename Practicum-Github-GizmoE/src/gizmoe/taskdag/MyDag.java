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
	HashMap<Integer, Input> overallInputMap;
	HashMap<Integer, Output> overallOutputMap;	
	ArrayList<ArrayList<Boolean>> connectMap;
	ArrayList<ArrayList<IOMapping>> ioMap;
	HashMap<Integer, Integer> internalID;
	HashMap<Integer, Integer> internalIOID;
	ArrayList<Integer> originalIOID;
	private int capabilityID = 0;
	private int ioID = 0;
	
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
	
	private class Capability {
		ArrayList<Input> inputs;
		ArrayList<Output> outputs;
		private String name;
		
		public Capability(String capability, Input[] inputs,
				Output[] outputs){
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
		Capability newCap = new Capability(capability, inputs, outputs);
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
		if(row != null && row.size() > internalID.get(nextIoID)){
			row.set(internalID.get(nextIoID), notFalse);
			return true;
		}else{
			return false;
		}
	}

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

	@Override
	public String getCapabilityName(int id) {
		return null;
	}

	@Override
	public Input[] getCapabilityInputs(int id) {
		return null;
	}

	@Override
	public Output[] getCapabilityOutputs(int id) {
		return null;
	}

	@Override
	public ArrayList<IOPair> isMappedTo(int ioID) {
		return null;
	}
	
	@Override
	public ArrayList<IOPair> isMappingOf(int ioID) {
		return null;
	}

	@Override
	public ArrayList<Integer> nextCapabilities(int id) {
		return null;
	}
	
	@Override
	public ArrayList<Integer> joinToBecome(int id){
		return null;
	}
	
	@Override
	public boolean isJoin(int id){
		return false;
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
