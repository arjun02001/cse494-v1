package edu.asu.cse494;
import com.lucene.document.Document;
import com.lucene.document.Field;
import com.lucene.index.*;

import java.io.ObjectInputStream.GetField;
import java.util.Enumeration;
import java.util.Hashtable;

public class TFRanking 
{
	public static void main(String[] args) 
	{
		String input = "abandonados abandonar";
		//String input = "arjun";
		try
		{
			IndexReader reader = IndexReader.open("result3index");
			Hashtable<String, Integer> tokenizedQuery = getTokenizedQuery(input);
			Hashtable<Integer, Float> similarity = new Hashtable<Integer, Float>();
			
			Enumeration<String> queryKeywords = tokenizedQuery.keys();
			while(queryKeywords.hasMoreElements())
			{
				String queryKeyword = queryKeywords.nextElement();
				TermDocs termDocs = reader.termDocs(new Term("contents", queryKeyword));
				if(termDocs != null)
				{
					while(termDocs.next())
					{
						if(similarity.containsKey(termDocs.doc()))
						{
							similarity.put(termDocs.doc(), similarity.get(termDocs.doc()) + (tokenizedQuery.get(queryKeyword) * termDocs.freq()));
						}
						else
						{
							similarity.put(termDocs.doc(), (float) (tokenizedQuery.get(queryKeyword) * termDocs.freq()));
						}
					}
				}
			}
			System.out.println("done");
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	private static Hashtable<String, Integer> getTokenizedQuery(String input)
	{
		Hashtable<String, Integer> tokenizedQuery = new Hashtable<String, Integer>();
		try
		{
			String[] mainQuery = input.trim().toLowerCase().split("\\s+");
			for(String query:mainQuery)
			{
				if(tokenizedQuery.containsKey(query))
				{
					tokenizedQuery.put(query, tokenizedQuery.get(query) + 1);
				}
				else
				{
					tokenizedQuery.put(query, 1);
				}
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return tokenizedQuery;
	}
}
