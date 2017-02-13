package data;

import java.io.*;
import java.util.HashMap;
import java.util.Scanner;

public class AdminFileStorage implements AdminStorage
{
	private static final String filePath = "admin_logins.txt";

	@Override
	public String getHashedPassword(String username)
	{
		HashMap<String, String> all = readAll();
		if(all.containsKey(username))
			return all.get(username);
		return null;
	}

	@Override
	public void storeHashedPassword(String username, String hash)
	{
		HashMap<String, String> all = readAll();
		all.put(username, hash);
		writeAll(all);
	}

	@Override
	public void deleteAccount(String username)
	{
		HashMap<String, String> all = readAll();
		if(all.containsKey(username))
		{
			all.remove(username);
		}
		writeAll(all);
	}

	private void writeAll(HashMap<String, String> all)
	{
		try
		{
			synchronized (filePath)
			{
				File f = new File(filePath);
				if(!f.exists())
					f.createNewFile();
				PrintWriter out = new PrintWriter(f);
				for(String key : all.keySet())
				{
					out.println(key);
					out.println(all.get(key));
				}
				out.flush();
				out.close();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	private HashMap<String, String> readAll()
	{
		synchronized (filePath)
		{
			HashMap<String, String> all = new HashMap<>();
			Scanner reader = null;
			try
			{
				reader = new Scanner(new FileReader(filePath));
				String line;
				String user = null;
				while((reader.hasNextLine()) && ((line = reader.nextLine()) != null))
				{
					if(user == null)
					{
						user = line;
					}
					else
					{
						all.put(user, line);
						user = null;
					}
				}
				reader.close();
			}
			catch (FileNotFoundException e)
			{
				try
				{
					new File(filePath).createNewFile();
					System.out.println("File created");
				} catch (IOException e1)
				{
					e1.printStackTrace();
				}
				try
				{
					reader.close();
				}
				catch (Exception e2)
				{
				}
			}
			return all;
		}
	}
}
