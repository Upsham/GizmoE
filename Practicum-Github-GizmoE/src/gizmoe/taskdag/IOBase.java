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
public abstract class IOBase {
	
	int id; // Unique identifier
	String name;// Name of Capability
	String type;// What data type, ie int, String, float etc
	String defaultValue;// For input only - is there a default value?
	String outputgroup;// For output only, is this a error output?

}
