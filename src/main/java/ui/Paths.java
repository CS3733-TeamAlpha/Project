package ui;

public class Paths
{
	//FXML files
	public static final String LOGIN_FXML = "/fxml/Login.fxml";
	public static final String DIRECTORY_EDITOR_FXML = "/fxml/DirectoryEditor.fxml";
	public static final String MAP_FXML = "/fxml/Map.fxml";
	public static final String MAP_EDITOR_FXML = "/fxml/MapEditorTool.fxml";
	public static final String PROVIDER_BOX_FXML = "/fxml/ProviderBox.fxml";
	public static final String STARTUP_FXML = "/fxml/Startup.fxml";
	public static final String ADMIN_PAGE_FXML = "/fxml/AdminPage.fxml";
	public static final String USER_DIRECTORY_FXML = "/fxml/UserDirectory.fxml";
	public static final String ACCOUNT_BOX_FXML = "/fxml/AccountBox.fxml";
	public static final String MANAGE_ACCOUNTS_FXML = "/fxml/ManageAccounts.fxml";

	//CSS files
	public static final String NORMAL_CSS = "css/normal.css";

	//Floor images
	public static final String FLOOR1_NORMAL = "/floors/hospital1.png";
	public static final String FLOOR2_NORMAL = "/floors/hospital2.png";
	public static final String FLOOR3_NORMAL = "/floors/hospital3.png";
	public static final String FLOOR4_NORMAL = "/floors/hospital4.png";
	public static final String FLOOR5_NORMAL = "/floors/hospital5.png";
	public static final String FLOOR6_NORMAL = "/floors/hospital6.png";
	public static final String FLOOR7_NORMAL = "/floors/hospital7.png";
	public static final String FLOOR1_BELKIN = "/floors/belkin1Double.png";
	public static final String FLOOR2_BELKIN = "/floors/belkin2Double.png";
	public static final String FLOOR3_BELKIN = "/floors/belkin3Double.png";
	public static final String FLOOR4_BELKIN = "/floors/belkin4Double.png";
	public static final String OUTDOORS = "/floors/outsideFloor.png";

	//GUI element icons
	public static final String HALLWAYICON = "/images/nodeIcon.png";
	public static final String ELEVATORICON = "/images/ElevatorIcon.png";
	public static final String DOCTORICON = "/images/DoctorIcon2.png";
	public static final String RESTROOMICON = "/images/UniRestroomIcon.png";
	public static final String KIOSKICON = "/images/KioskIcon.png";
	public static final String SELECTEDKIOSKICON = "/images/selectedKiosk2.png";
	public static final String STAIRWAYICON = "/images/StairsIcon.png";
	public static final String PARKINGLOTICON = "/images/parkingIcon.png";
	public static final String ADDNODE = "/images/addNodeSmall.png";
	public static final String REMOVENEIGHBOR = "/images/RemoveConnectionSmall.png";
	public static final String REMOVENODE = "/images/removeNodeSmall.png";
	public static final String CHAINNODES = "/images/chainNodeSmall.png";
	public static final String ICON = "/images/icon.png";
	public static final String DOOR = "/images/doorIcon.png";
	public static final String YOUAREHEREICON = "/images/hereIcon1.png";


	//Floor image proxies
	public static ProxyImage f1ImageProxy = new ProxyImage(Paths.FLOOR1_NORMAL);
	public static ProxyImage f2ImageProxy = new ProxyImage(Paths.FLOOR2_NORMAL);
	public static ProxyImage f3ImageProxy = new ProxyImage(Paths.FLOOR3_NORMAL);
	public static ProxyImage f4ImageProxy = new ProxyImage(Paths.FLOOR4_NORMAL);
	public static ProxyImage f5ImageProxy = new ProxyImage(Paths.FLOOR5_NORMAL);
	public static ProxyImage f6ImageProxy = new ProxyImage(Paths.FLOOR6_NORMAL);
	public static ProxyImage f7ImageProxy = new ProxyImage(Paths.FLOOR7_NORMAL);
	public static ProxyImage f1BelkinProxy = new ProxyImage(Paths.FLOOR1_BELKIN);
	public static ProxyImage f2BelkinProxy = new ProxyImage(Paths.FLOOR2_BELKIN);
	public static ProxyImage f3BelkinProxy = new ProxyImage(Paths.FLOOR3_BELKIN);
	public static ProxyImage f4BelkinProxy = new ProxyImage(Paths.FLOOR4_BELKIN);
	public static ProxyImage[] regularFloorImages = {f1ImageProxy, f2ImageProxy, f3ImageProxy, f4ImageProxy, f5ImageProxy, f6ImageProxy, f7ImageProxy};
	public static ProxyImage[] belkinFloorImages = {f1BelkinProxy, f2BelkinProxy, f3BelkinProxy, f4BelkinProxy};

	//GUI element image proxies
	public static ProxyImage removeNodeImageProxy = new ProxyImage(Paths.REMOVENODE);
	public static ProxyImage chainImageProxy = new ProxyImage(Paths.CHAINNODES);
	public static ProxyImage addNeighborImageProxy = new ProxyImage(Paths.ADDNODE);
	public static ProxyImage removeNeighborImageProxy = new ProxyImage(Paths.REMOVENEIGHBOR);
	public static ProxyImage doctorImageProxy = new ProxyImage(Paths.DOCTORICON);
	public static ProxyImage restroomImageProxy = new ProxyImage(Paths.RESTROOMICON);
	public static ProxyImage elevatorImageProxy = new ProxyImage(Paths.ELEVATORICON);
	public static ProxyImage hallwayImageProxy = new ProxyImage(Paths.HALLWAYICON);
	public static ProxyImage kioskImageProxy = new ProxyImage(Paths.KIOSKICON);
	public static ProxyImage skioskImageProxy = new ProxyImage(Paths.SELECTEDKIOSKICON);
	public static ProxyImage stairwayImageProxy = new ProxyImage(Paths.STAIRWAYICON);
	public static ProxyImage parkinglotImageProxy = new ProxyImage(Paths.PARKINGLOTICON);
	public static ProxyImage outdoorImageProxy = new ProxyImage(Paths.OUTDOORS);
	public static ProxyImage doorImageProxy = new ProxyImage(Paths.DOOR);
	public static ProxyImage yahImageProxy = new ProxyImage(Paths.YOUAREHEREICON);

	public static boolean isAdmin(String userName)
	{
		return userName.equals("admin");
	}
}
