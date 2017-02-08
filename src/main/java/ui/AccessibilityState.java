package ui;

public class AccessibilityState
{
	private static boolean highContrast = false;
	private static boolean largeText = false;

	public static boolean isHighContrast()
	{
		return highContrast;
	}

	public static void setHighContrast(boolean highContrast)
	{
		AccessibilityState.highContrast = highContrast;
	}

	public static boolean isLargeText()
	{
		return largeText;
	}

	public static void setLargeText(boolean largeText)
	{
		AccessibilityState.largeText = largeText;
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
