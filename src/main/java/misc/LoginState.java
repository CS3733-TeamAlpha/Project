package misc;

import ui.Paths;

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

	/**
	 * Stores the currently logged in account. It will also detect if the account is admin, and store than information too.
	 * @param account The user name to store.
	 */
	public static void login(String account)
	{
		adminLoggedIn = account.equals(Paths.ADMIN_NAME);
		loggedIn = true;
		loggedInAccount = account;
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
