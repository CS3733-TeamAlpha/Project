package misc;

public class LoginState
{
	private static boolean loggedIn = false;
	private static boolean adminLoggedIn = false;
	private static String loggedInAccount = null;

	public static void logout()
	{
		loggedIn = false;
		adminLoggedIn = false;
		loggedInAccount = null;
	}

	public static void login(Account account)
	{
		if(account.isAdmin())
		{
			adminLoggedIn = true;
		}
		loggedIn = true;
		loggedInAccount = account.getUserName();
	}

	public static boolean isLoggedIn()
	{
		return loggedIn;
	}

	public static String getLoggedInAccount()
	{
		return loggedInAccount;
	}

	public static boolean isAdminLoggedIn()
	{
		return adminLoggedIn;
	}
}
