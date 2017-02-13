package ui;

import data.Database;

/**
 * AKA 'JFXController'
 * This class exists to provide global access to the database without having multiple connections open.
 */
public abstract class AbstractController
{
	protected static Database database;

	static
	{
		database = null;
	}

	public AbstractController()
	{
		if (database == null)
			database = new Database("FHAlpha");
	}
}
