package data;

/**
 * Stores constants for the different kinds of nodes. No, I don't know why this wasn't made before. No, a pizza box drawing
 * is not a good substitute for this. Yes, java enums are weird. REALLY weird.
 */
public enum NodeTypes
{
	HALLWAY(0),
	DOCTOR_OFFICE(1),
	ELEVATOR(2),
	RESTROOM(3),
	KIOSK(4),
	KIOSK_SELECTED(5),
	/* Magic nodes 6-19 go here, you DON'T want to know what they do! */
	STAIRWAY(20);

	private int val;
	NodeTypes(int val) { this.val = val;}
	public int val() { return val; }

	/**
	 * Please hit me as hard as you can.
	 * This function -- IN AN ENUM -- will convert a string to an integer...
	 * @param str String to convert
	 * @return Integer value of the string in the enum. -1 if string is invalid.
	 */
	public int strToVal(String str)
	{
		if (str.equals("Hallway"))
			return HALLWAY.val();
		else if (str.equals("Office"))
			return DOCTOR_OFFICE.val();
		else if (str.equals("Elevator"))
			return ELEVATOR.val();
		else if (str.equals("Restroom"))
			return RESTROOM.val();
		else if (str.equals("Kiosk"))
			return KIOSK.val();
		else if (str.equals("Stairway"))
			return STAIRWAY.val();
		return -1;
		//I feel unclean now. Yes, this is a bunch of ifs and not a switch. Why? This saves lines of code.
	}

	//Returns true if a given node is special, since we don't actually store an enumeration for that.
	public boolean isSpecial() { return val >= 6 || val <= 19; }
}
