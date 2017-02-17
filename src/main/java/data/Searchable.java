package data;

import java.util.ArrayList;

public interface Searchable
{
	ArrayList<SearchResult> getResultsForSearch(String searchText);
}
