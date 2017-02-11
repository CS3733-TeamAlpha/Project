package data;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DatabaseTest
{
	Database database;
	@Before
	public void setUp() throws Exception
	{
		database = new Database("junit_testing_db");
	}

	@After
	public void tearDown() throws Exception
	{
		database.disconnect();
		java.lang.Runtime.getRuntime().exec("rm -rf junit_testing_db"); //Delete the testing database. TODO: Get this running on windows
	}

	@Test
	public void connectionTest()
	{
		assertTrue(database.isConnected());
	}

}