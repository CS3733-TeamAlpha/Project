package data;

public interface AdminStorage
{
	String getHashedPassword(String username);
	void storeHashedPassword(String username, String hash);
	void deleteAccount(String username);
}
