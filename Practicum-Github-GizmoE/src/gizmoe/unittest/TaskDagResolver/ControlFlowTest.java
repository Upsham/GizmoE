package gizmoe.unittest.TaskDagResolver;

import static org.junit.Assert.*;

import java.util.ArrayList;

import gizmoe.TaskDagResolver.ResolveDag;
import gizmoe.taskdag.MyDag;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ControlFlowTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
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
		/* Use NewCombo Task */
		MyDag testdag = ResolveDag.TaskDagResolver("NewCombo");
		ArrayList<Integer> testList = new ArrayList<Integer>();
		
		/* Start Capability Test */
		
		/* 16 is the start capability */
		testList.add(16); // Start capability for NewCombo is only capability 16
		assertEquals(testList, testdag.startCapabilities());
		
		
		
		/* Next Capability Test */
		
		/* Next for 16 is 17 */
		testList.clear();
		testList.add(17);
		assertEquals(testList, testdag.nextCapabilities(16));
		assertEquals(false,testdag.isJoin(16)); // Not a joining point

		/* After 17, we split into 10 and 21 */
		testList.clear();
		testList.add(10);
		testList.add(21);
		assertEquals(testList, testdag.nextCapabilities(17));
		assertEquals(false,testdag.isJoin(17)); // Not a joining point
		
		/* After 10, we have 12 */
		testList.clear();
		testList.add(12);
		assertEquals(testList, testdag.nextCapabilities(10));
		assertEquals(false,testdag.isJoin(10)); // Not a joining point
		
		/* After 21, we split again into 25 and 25 */
		testList.clear();
		testList.add(25);
		testList.add(26);
		assertEquals(testList, testdag.nextCapabilities(21));
		assertEquals(false,testdag.isJoin(21)); // Not a joining point
		
		/* 12 is the last capability, but is a join of 10, 25 and 26 */
		testList.clear();
		assertEquals(testList, testdag.nextCapabilities(12));
		assertEquals(true,testdag.isJoin(12)); // Is a joining point
		testList.add(10);
		testList.add(25);
		testList.add(26);
		assertEquals(testList,testdag.joinToBecome(12)); // Check what all joins to become cap 12

		/* After 25, we have 12 */
		testList.clear();
		testList.add(12);
		assertEquals(testList, testdag.nextCapabilities(25));
		assertEquals(false,testdag.isJoin(25)); // Not a joining point
		
		/* After 26, we have 12 */
		testList.clear();
		testList.add(12);
		assertEquals(testList, testdag.nextCapabilities(26));
		assertEquals(false,testdag.isJoin(26)); // Not a joining point
	}


}
