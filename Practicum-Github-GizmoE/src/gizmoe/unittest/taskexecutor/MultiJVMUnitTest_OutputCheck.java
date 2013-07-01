package gizmoe.unittest.taskexecutor;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MultiJVMUnitTest_OutputCheck {

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
	public void test() {
		System.out.println("This test assumes that you have just run MultiJVMTest_CapSpawner and MultiJVMTest_TaskExecutor");
		try {
			assertTrue("The files differ!", FileUtils.contentEquals(new File("CS_MultiJVM_Test.txt"), new File("TE_MultiJVM_Test.txt")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
