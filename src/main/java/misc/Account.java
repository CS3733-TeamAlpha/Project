package misc;

public class Account
{
	private String userName;

	public Account(String userName)
	{
		this.userName = userName;
	}

	public String getUserName()
	{
		return userName;
	}

	public boolean isAdmin()
	{
		return userName.equals("admin");
	}
}
