package data;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pathfinding.*;
import java.io.File;

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
		database.deleteNodeByUUID(retNode.getID());
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

	@Test
	public void testLinkage()
	{
		//Stolen from pathfinding integration test
		ConcreteNode[][] gridNodes = new ConcreteNode[10][10];
		for (int i = 0; i < 10; i++)
			for (int j = 0; j < 10; j++)
				gridNodes[i][j] = new ConcreteNode();

		for (int i = 0; i < 10; i++)
		{
			for (int j = 0; j < 10; j++)
			{
				if (i > 0)
					gridNodes[i][j].addNeighbor(gridNodes[i-1][j]);
				if (i < 9)
					gridNodes[i][j].addNeighbor(gridNodes[i+1][j]);
				if (j > 0)
					gridNodes[i][j].addNeighbor(gridNodes[i][j-1]);
				if (j < 9)
					gridNodes[i][j].addNeighbor(gridNodes[i][j+1]);
			}
		}

		for (int i = 0; i < 10; i++)
			for (int j = 0; j < 10; j++)
				database.insertNode(gridNodes[i][j]);

		//Now get all those nodes back out again
		Node[][] testNodes = new ConcreteNode[10][10];
		for (int i = 0; i < 10; i++)
		{
			for (int j = 0; j < 10; j++)
			{
				testNodes[i][j] = database.getNodeByUUID(gridNodes[i][j].getID());
				assertNotNull(testNodes[i][j]);
			}
		}

		//Verify the integrity of the node grid just retrieved
		for (int i = 0; i < 10; i++)
		{
			for (int j = 0; j < 10; j++)
			{
				if (i > 0)
					assertTrue(gridNodes[i][j].getNeighbors().contains(gridNodes[i-1][j]));
				if (i < 9)
					assertTrue(gridNodes[i][j].getNeighbors().contains(gridNodes[i+1][j]));
				if (j > 0)
					assertTrue(gridNodes[i][j].getNeighbors().contains(gridNodes[i][j-1]));
				if (j < 9)
					assertTrue(gridNodes[i][j].getNeighbors().contains(gridNodes[i][j+1]));
			}
		}

		//And finally clean up, we don't want a ton of spare nodes hanging around in the database
		for (int i = 0; i < 10; i++)
			for (int j = 0; j < 10; j++)
				database.deleteNodeByUUID(testNodes[i][j].getID());
	}
}