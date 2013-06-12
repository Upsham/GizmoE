/************************************************************
 * 
 * GizmoE Practicum
 * Input class which will contain packages used to store
 * inputs in DAG
 * 
 * Author: Upsham Dawra(ukd)
 * Version: 1.0
 * 
 * 
 ************************************************************/
package gizmoe.taskdag;
public class Input extends IOBase {
	
	public String defaultValue;// For input only - is there a default value?
	public Input(String name, int id, String type){
		this.name = name;
		this.id = id;
		this.type = type;
	}

	public Input(String name, int id, String type, String defaul){
		this.name = name;
		this.id = id;
		this.type = type;
		this.defaultValue = defaul;
	}
}
