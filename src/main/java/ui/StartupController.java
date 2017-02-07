package ui;

/**
 * Created by Ari on 2/6/17.
 */
public class StartupController
{

	public StartupController(){}

	public void initialize()
	{

	}

	public void showMap()
	{
		Main.loadFXML("/fxml/Map.fxml");
	}

	public void showDirectory()
	{
		Main.loadFXML("/fxml/Directory.fxml");
	}

	public void showLogin()
	{
		Main.loadFXML("/fxml/Login.fxml");
	}
}
