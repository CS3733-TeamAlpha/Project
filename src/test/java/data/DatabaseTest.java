package data;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pathfinding.*;
import java.io.File;
import java.util.UUID;

import static org.junit.Assert.*;

public class DatabaseTest
{
	private static final String TEST_DB = "junit_testing_db";

	Database database;

	@Before
	public void setUp() throws Exception
	{
		database = new Database(TEST_DB);
	}

	@After
	public void tearDown() throws Exception
	{
		database.disconnect();

		//Delete the test database, stored in a folder $TEST_DB
		//deleteFolder(TEST_DB);
	}

	/**
	 * Recursively deletes a folder, because java is stupid and `rm -rf $fname` isn't platform independent.
	 * @param fname
	 */
	private void deleteFolder(String fname)
	{
		File folder = new File(fname);
		for (String s : folder.list())
		{
			File curFile = new File(folder.getPath(), s);
			if (curFile.isDirectory())
				deleteFolder(curFile.getPath());
			curFile.delete();
		}
		folder.delete();
	}

	@Test
	public void connectionTest()
	{
		assertTrue(database.isConnected());
	}

	@Test
	public void testInsertAndRetrieval()
	{
		Node testNode = new ConcreteNode("00000000-0000-0000-0000-000000000000",
				"Test Node", "SFHeadquarters", 1, 2, 3, 1701);

		database.insertNode(testNode); //Insert the node...
		Node retNode = database.getNodeByUUID(testNode.getID());
		assertTrue(testNode.equals(retNode));
	}

	@Test
	public void testDelete()
	{
		ConcreteNode testNode = new ConcreteNode();
		database.insertNode(testNode);
		assertNotNull(database.getNodeByUUID(testNode.getID()));
		database.deleteNodeByUUID(testNode.getID());
		assertNull(database.getNodeByUUID(testNode.getID()));
	}
}