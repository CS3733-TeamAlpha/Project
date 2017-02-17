package data;

public class SearchResult
{
	public String displayText;
	public String id;
	public SearchType searchType;
}

enum SearchType
{
	Location, Provider
}