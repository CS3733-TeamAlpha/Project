package ui;

public class Accessibility
{
	public static final String HIGH_CONTRAST_CSS = "css/high-contrast.css";
	public static final String NORMAL_CSS = "css/normal.css";
	public static final String HIGH_CONTRAST_MAP_PATH = "images/floor3-hc.png";

	private static boolean highContrast = false;

	public static boolean isHighContrast()
	{
		return highContrast;
	}

	public static void setHighContrast(boolean highContrast)
	{
		Accessibility.highContrast = highContrast;
		Main.updateCSS();
	}

	public static void toggleHighContrast()
	{
		highContrast = !highContrast;
		Main.updateCSS();
	}

}
