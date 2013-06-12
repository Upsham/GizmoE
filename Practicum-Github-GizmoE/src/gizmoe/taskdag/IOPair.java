/************************************************************
 * 
 * GizmoE Practicum
 * IOBase class which will contain packages used to store
 * inputs and outputs in DAG. This is the base class
 * 
 * Author: Upsham Dawra(ukd)
 * Version: 1.0
 * 
 * 
 ************************************************************/
package gizmoe.taskdag;

public class IOPair {
	public String mode;
	public int id;
	
	public IOPair(String mode, int id){
		this.mode = mode;
		this.id = id;
	}
}
