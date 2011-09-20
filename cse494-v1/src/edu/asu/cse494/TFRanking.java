package edu.asu.cse494;
import com.lucene.document.Document;
import com.lucene.document.Field;
import com.lucene.index.*;

import java.io.ObjectInputStream.GetField;
import java.util.Hashtable;

public class TFRanking 
{
	public static void main(String[] args) 
	{
		String input = "abandonados abandonar";
		try
		{
			IndexReader reader = IndexReader.open("result3index");
			Hashtable<String, Integer> tokenizedQuery = getTokenizedQuery(input);
			
			
			System.out.println("");
			/*TermDocs termDocs = reader.termDocs(new Term("contents", query));
			while(termDocs.next())
			{
				System.out.println(termDocs.doc() + "   " + termDocs.freq() + "    " + reader.norms("contents")[termDocs.doc()]);
				
			}*/
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
