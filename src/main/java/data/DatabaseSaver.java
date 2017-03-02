package data;

import org.apache.derby.client.am.DateTime;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class DatabaseSaver
{
	public static void saveDatabase(String dbName)
	{
		PrintStream err = System.err;
		try (DSLContext ctx = DSL.using("jdbc:derby:" + dbName + ";create=true"))
		{
			ArrayList<String> inserts = new ArrayList<>();
			inserts.addAll(getTableInserts("Buildings", ctx));
			inserts.addAll(getTableInserts("Nodes", ctx));
			inserts.addAll(getTableInserts("Edges", ctx));
			inserts.addAll(getTableInserts("Providers", ctx));
			inserts.addAll(getTableInserts("ProviderOffices", ctx));
			inserts.addAll(getTableInserts("Services", ctx));

			String pattern = "MM-dd-yyyy hh:mm:ss";
			SimpleDateFormat format = new SimpleDateFormat(pattern);


			File f = new File("Save_Files");
			if(!f.exists())
			{
				f.mkdir();
			}

			f = new File("Save_Files/" + format.format(new Date()) + ".ddl");
			if(!f.exists())
			{
				f.createNewFile();
			}

			PrintWriter out = new PrintWriter(f);
			for(String s : inserts)
			{
				out.println(s);
			}
			out.flush();
			out.close();

			System.setErr(err);
		}
		catch (Exception e)
		{
			System.setErr(err);
			e.printStackTrace();
		}
	}

	private static List<String> getTableInserts(String tableName, DSLContext ctx)
	{
		System.setErr(new PrintStream(new OutputStream()
		{
			@Override
			public void write(int b) throws IOException
			{
				//Lol Jooq, stop carpet bombing stderr please
			}
		}));

		return ctx.meta()
				.getSchemas()
				.stream()

				// Filter out those schemas that you want to export
				//.filter(schema -> schema.getName().equals("APP"))

				// Get the tables for each schema...
				.flatMap(schema -> schema.getTables().stream())
				.filter(table -> table.getName().compareToIgnoreCase(tableName) == 0)
				.map(table -> ctx.fetch(table).formatInsert().replace("UNKNOWN_TABLE", table.getName()))
				.collect(Collectors.toList());

				// ... and format their content as INSERT statements.
				//.forEach(table -> System.out.println());
	}
}
