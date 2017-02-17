package data;

public class SearchResult
{
	String displayText;
	String id;
	SearchType searchType;
}

enum SearchType
{
	Location, Provider
}