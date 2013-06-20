package gizmoe.unittest.TaskDagResolver;

import static org.junit.Assert.*;

import java.util.ArrayList;

import gizmoe.TaskDagResolver.ResolveDag;
import gizmoe.taskdag.Input;
import gizmoe.taskdag.MyDag;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class DataFlowTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ArrayList<Integer> overallInput = new ArrayList<Integer>();
		ArrayList<Integer> overallOutput = new ArrayList<Integer>();

		overallInput.add(23);
		overallInput.add(24);
		overallInput.add(25);
		overallOutput.add(26);
		overallOutput.add(27);
		overallOutput.add(28);
		overallOutput.add(29);
		overallOutput.add(30);
		overallOutput.add(31);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testTaskDagResolver() {

		/* Expected Values */
		
		ArrayList<Input> overallInput = new ArrayList<Input>();
		ArrayList<Integer> overallOutput = new ArrayList<Integer>();

		overallInput.add(new Input("i1",23,"int"));
		overallInput.add(new Input("i2",24,"int"));
		overallInput.add(new Input("i3",25,"int","1"));

		overallOutput.add(26);
		overallOutput.add(27);
		overallOutput.add(28);
		overallOutput.add(29);
		overallOutput.add(30);
		overallOutput.add(31);
		MyDag testdag = ResolveDag.TaskDagResolver("NewCombo");
		
		/*  Fully Check Overall Input  */
		for(int i = 0; i<overallInput.size(); i++){
			int id = testdag.getAllOverallInput().get(i).id;
			assertEquals(id, overallInput.get(i).id);
			assertEquals(testdag.getAllOverallInput().get(i).name,overallInput.get(i).name);
		}
		/*	Fully Check Overall Output	*/
		for(int i = 0; i<overallOutput.size(); i++){
			assertEquals(testdag.getAllOverallOutput().get(i).id == overallOutput.get(i), true);
		}
	}

}
