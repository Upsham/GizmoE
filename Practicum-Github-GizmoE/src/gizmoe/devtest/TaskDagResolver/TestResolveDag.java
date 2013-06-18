package gizmoe.devtest.TaskDagResolver;
import java.util.ArrayList;

import gizmoe.TaskDagResolver.*;
import gizmoe.taskdag.MyDag;
public class TestResolveDag {

	public static void main(String[] args) {
		
		MyDag testdag = ResolveDag.TaskDagResolver("NewCombo");
		System.out.println("This task starts with capabilities: ");
		for(int id : testdag.startCapabilities()){
			System.out.println(id);
		}
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
