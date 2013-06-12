/************************************************************
 * 
 * GizmoE Practicum
 * Output class which will contain packages used to store
 * outputs in DAG
 * 
 * Author: Upsham Dawra(ukd)
 * Version: 1.0
 * 
 * 
 ************************************************************/
package gizmoe.taskdag;
public class Output extends IOBase {
	
	public String outputgroup;// For output only, is this a error output?
	
	public Output(String name, int id, String type){
		this.name = name;
		this.id = id;
		this.type = type;
	}

	public Output(String name, int id, String type, String group){
		this.name = name;
		this.id = id;
		this.type = type;
		this.outputgroup = group;
	}
	
}
