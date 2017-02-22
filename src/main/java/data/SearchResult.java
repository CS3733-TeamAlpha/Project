package data;

public class SearchResult
{
	public String displayText;
	public String id;
	public SearchType searchType;

	public SearchResult(String displayText, String id, SearchType searchType)
	{
		this.displayText = displayText;
		this.id = id;
		this.searchType = searchType;
	}

	public SearchResult()
	{
	}
}

enum SearchType
{
	Location, Provider
}