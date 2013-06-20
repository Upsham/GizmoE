package gizmoe.devtest.TaskDagResolver;
import java.util.ArrayList;

import gizmoe.TaskDagResolver.*;
import gizmoe.taskdag.IOPair;
import gizmoe.taskdag.Input;
import gizmoe.taskdag.MyDag;
import gizmoe.taskdag.Output;
public class TestResolveDag {

	public static void main(String[] args) {
		ArrayList<Integer> testarr = new ArrayList<Integer>();
		testarr.add(16);
		testarr.add(17);
		testarr.add(10);
		testarr.add(21);
		testarr.add(12);
		testarr.add(25);
		testarr.add(26);

		MyDag testdag = ResolveDag.TaskDagResolver("NewCombo");
		System.out.println("Overall input IDs: ");
		for(Input in : testdag.getAllOverallInput()){
			System.out.println(in.id);
		}
		System.out.println("Overall Output IDs: ");
		for(Output out : testdag.getAllOverallOutput()){
			System.out.println(out.id);
		}
		System.out.println("All Inputs: ");
		for(int i : testarr){
			for(Input io : testdag.getCapabilityInputs(i)){
				System.out.println(i+" -> "+io.id+ " : "+io.name);
			}
			System.out.println();
		}

		System.out.println("Outputs with full dataflow:");
		for(Output io : testdag.getCapabilityOutputs(16)){
			System.out.println("16 -> "+io.id + " : "+io.name);
			System.out.println("This maps to:");
			for(IOPair id : testdag.isMappedTo(io.id)){
				System.out.println(id.id+" with mode "+id.mode);;
			}			
		}
		System.out.println();

		
		for(Output io : testdag.getCapabilityOutputs(17)){
			System.out.println("17 -> "+io.id + " : "+io.name);
			System.out.println("This maps to:");
			for(IOPair id : testdag.isMappedTo(io.id)){
				System.out.println(id.id+" with mode "+id.mode);;
			}	
			
		}
		System.out.println();

		for(Output io : testdag.getCapabilityOutputs(10)){
			System.out.println("10 -> "+io.id + " : "+io.name);
			System.out.println("This maps to:");
			for(IOPair id : testdag.isMappedTo(io.id)){
				System.out.println(id.id+" with mode "+id.mode);;
			}	
		}
		System.out.println();

		for(Output io : testdag.getCapabilityOutputs(21)){
			System.out.println("21 -> "+io.id + " : "+io.name);
			System.out.println("This maps to:");
			for(IOPair id : testdag.isMappedTo(io.id)){
				System.out.println(id.id+" with mode "+id.mode);;
			}	
		}
		System.out.println();

		for(Output io : testdag.getCapabilityOutputs(12)){
			System.out.println("12 -> "+io.id + " : "+io.name);
			System.out.println("This maps to:");
			for(IOPair id : testdag.isMappedTo(io.id)){
				System.out.println(id.id+" with mode "+id.mode);;
			}	
		}
		System.out.println();

		for(Output io : testdag.getCapabilityOutputs(25)){
			System.out.println("25 -> "+io.id + " : "+io.name);
			System.out.println("This maps to:");
			for(IOPair id : testdag.isMappedTo(io.id)){
				System.out.println(id.id+" with mode "+id.mode);;
			}	
		}
		System.out.println();

		for(Output io : testdag.getCapabilityOutputs(26)){
			System.out.println("26 -> "+io.id + " : "+io.name);
			System.out.println("This maps to:");
			for(IOPair id : testdag.isMappedTo(io.id)){
				System.out.println(id.id+" with mode "+id.mode);;
			}	
		}
		System.out.println("\n\n\nControl Flow");
		printNextCap(16,testdag);
		printNextCap(17,testdag);
		printNextCap(10,testdag);
		printNextCap(21,testdag);
		printNextCap(12,testdag);
		printNextCap(25,testdag);
		printNextCap(26,testdag);
	}
	
	private static void printNextCap(int id, MyDag testdag){
		ArrayList<Integer> nextCap = testdag.nextCapabilities(id);
		ArrayList<Integer> joinCap = testdag.nextCapabilities(id);
		if(nextCap.size()==0){
			System.out.println(id+" is the last capability!");

		}else{
			System.out.println("The nextcapability for "+id+" is:");
			for(int i : nextCap){
				System.out.println(i);
			}
		}
		if(testdag.isJoin(id)){
			System.out.println("This is also a joining point! The following capabilities join at this point:");
			joinCap = testdag.joinToBecome(id);
			for(int i : joinCap){
				System.out.println(i);
			}
		}
		System.out.println();
	}

}
