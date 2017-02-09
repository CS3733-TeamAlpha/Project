package ui;

public class Accessibility
{
	public static final String HIGH_CONTRAST_CSS = "css/high-contrast.css";
	public static final String NORMAL_CSS = "css/normal.css";
	public static final String HIGH_CONTRAST_MAP_PATH = "images/floor3-hc.png";

	private static boolean highContrast = false;
	private static boolean largeText = false;

	public static boolean isHighContrast()
	{
		return highContrast;
	}

	public static void setHighContrast(boolean highContrast)
	{
		Accessibility.highContrast = highContrast;
	}

	public static boolean isLargeText()
	{
		return largeText;
	}

	public static void setLargeText(boolean largeText)
	{
		Accessibility.largeText = largeText;
	}

	public static void toggleHighContrast()
	{
		highContrast = !highContrast;
	}

	public static void toggleLargeText()
	{
		largeText = !largeText;
	}
}
