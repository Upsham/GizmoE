/************************************************************
 * 
 * GizmoE Practicum
 * The test class for the Dag! This is a dev-test.
 * 
 * Author: Upsham Dawra(ukd)
 * Version: 1.0
 * 
 * 
 ************************************************************/
package gizmoe.devtest.taskdag;

import java.util.ArrayList;

import gizmoe.taskdag.IOPair;
import gizmoe.taskdag.Input;
import gizmoe.taskdag.MyDag;
import gizmoe.taskdag.Output;

public class TestMyDag {

	static int ioid=3, capabilityid=1;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MyDag testdag = new MyDag();
		testdag.addOverallIO(createInputArr(1), createOutputArr(2));
		testdag.addCapability("First capability", capabilityid++ , createInputArr(1), createOutputArr(2));
		testdag.mapIO(3, 6, "copy");
		testdag.mapIO(4, 7, "copy");
		testdag.mapIO(5, 8, "copy");
		
		System.out.println("The capability is called: "+testdag.getCapabilityName(1));
		
		System.out.println("The capability inputs are:\n");
		printInputArr(testdag.getCapabilityInputs(1));
		System.out.println("The capability outputs are:\n");
		printOutputArr(testdag.getCapabilityOutputs(1));
		mapIOPrint(testdag, false, 3);
		mapIOPrint(testdag, false, 5);
		mapIOPrint(testdag, true, 6);
		mapIOPrint(testdag, true, 8);

		testdag.emptyDAG();
		ioid = 1;
		capabilityid = 1;
		testdag.addOverallIO(createInputArr(2), createOutputArr(2));
		testdag.addCapability("Start Seq capability", capabilityid++ , createInputArr(1), createOutputArr(2));
		testdag.addCapability("Parallel capability", capabilityid++ , createInputArr(1), createOutputArr(1));
		testdag.addCapability("Parallel capability", capabilityid++ , createInputArr(1), createOutputArr(1));
		testdag.addCapability("Parallel capability", capabilityid++ , createInputArr(1), createOutputArr(1));
		testdag.addCapability("Join capability", capabilityid++ , createInputArr(2), createOutputArr(1));
		testdag.connect(1, 2);
		testdag.connect(1, 3);
		testdag.connect(1, 4);
		testdag.connect(2, 5);
		testdag.connect(3, 5);
		testdag.connect(4, 5);
		
		testdag.mapIO(1, 5, "copy");//Overall -> start seq cap
		testdag.mapIO(2, 12, "copy");//Overall -> parallel cap 3
		testdag.mapIO(6, 8, "pipe");//start seq -> parallel 1
		testdag.mapIO(7, 10, "pipe");//start seq -> parallel 2
		testdag.mapIO(9, 14, "pipe");//parallel 1 -> join
		testdag.mapIO(13, 15, "pipe");//parallel 3 -> join
		testdag.mapIO(11, 3, "copy");//parallel 2 -> Overall
		testdag.mapIO(16, 4, "copy");//join -> Overall

		printNextCap(1, testdag);
		printNextCap(2, testdag);
		printNextCap(3, testdag);
		printNextCap(4, testdag);
		printNextCap(5, testdag);

	}
	
	private static void printNextCap(int id, MyDag testdag){
		ArrayList<Integer> nextCap = testdag.nextCapabilities(id);
		System.out.println("The nextcapability for "+id+" is:");
		for(int i : nextCap){
			System.out.println(i);
		}
		if(testdag.isJoin(id)){
			System.out.println("This is also a joining point! The following capabilities join at this point:");
			nextCap = testdag.joinToBecome(id);
			for(int i : nextCap){
				System.out.println(i);
			}
		}
		System.out.println();
	}
	private static void mapIOPrint(MyDag dag, boolean pointedTo, int id){
		ArrayList<IOPair> arr;
		if(!pointedTo){
			arr = dag.isMappedTo(id);
			for(IOPair io : arr){
				System.out.println("IO "+id+" connects to "+io.id+" with mode "+io.mode);
			}
		}else{
			arr = dag.isMappingOf(id);
			for(IOPair io : arr){
				System.out.println("IO "+id+" is pointed to by "+io.id+" with mode "+io.mode);
			}
		}
	}
	private static void printInputArr(Input[] arr){
		for(int i = 0; i<arr.length; i++){
			System.out.println("ID: "+arr[i].id);
			System.out.println("Name: "+arr[i].name);
			System.out.println("Type: "+arr[i].type+"\n");
		}
	}
	private static void printOutputArr(Output[] arr){
		for(int i = 0; i<arr.length; i++){
			System.out.println("ID: "+arr[i].id);
			System.out.println("Name: "+arr[i].name);
			System.out.println("Type: "+arr[i].type+"\n");
		}
	}
	private static Input[] createInputArr(int size){
		Input[] inputarr = new Input[size];
		for(int i=0; i<size; i++){
			inputarr[i] = new Input("Input"+i,ioid++,"int");
		}
		return inputarr;
	}
	
	private static Output[] createOutputArr(int size){
		Output[] outputarr = new Output[size];
		for(int i=0; i<size; i++){
			outputarr[i] = new Output("Output"+i,ioid++,"int");

		}
		return outputarr;
	}

}
