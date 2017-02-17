package data;

import java.util.ArrayList;

public interface Searchable
{
	ArrayList<SearchResult> getResultsForSeach(String searchText);
}
