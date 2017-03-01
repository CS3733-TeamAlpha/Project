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
	STAIRWAY(20),
	PARKINGLOT(21);

	private int val;
	NodeTypes(int val) { this.val = val;}
	public int val() { return val; }

	//Returns true if a given node is special, since we don't actually store an enumeration for that.
	public boolean isSpecial() { return val >= 6 || val <= 19; }
}
