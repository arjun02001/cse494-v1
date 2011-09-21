//Arjun Mukherji

package edu.asu.cse494;
import com.lucene.document.Document;
import com.lucene.document.Field;
import com.lucene.index.*;
import java.io.ObjectInputStream.GetField;
import java.util.*;

public class TFRanking 
{
	public static void main(String[] args) 
	{
		//String input = "abandonados abandonar";
		String input = "aboard";
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
			similarity = normalizeSimilarity(similarity, reader);
			sortedResult(similarity, reader);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	private static void sortedResult(Hashtable<Integer, Float> similarity, IndexReader reader)
	{
		try
		{
			ArrayList myArrayList=new ArrayList(similarity.entrySet());
			Collections.sort(myArrayList, new MyComparator());
			Iterator itr=myArrayList.iterator();
			int count = 0;
			while(itr.hasNext())
			{
				Map.Entry<Integer, Float> e = (Map.Entry<Integer, Float>)itr.next();
				count++;
				System.out.println(count + ". " + reader.document(e.getKey()).get("url") + "  DocID-" + e.getKey());
				//System.out.println(e.getKey() + " " + e.getValue());
			}
			System.out.println(count + " " + "documents found");
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	private static Hashtable<Integer, Float> normalizeSimilarity(Hashtable<Integer, Float> similarity, IndexReader reader)
	{
		try
		{
			Enumeration<Integer> similarityList = similarity.keys();
			while(similarityList.hasMoreElements())
			{
				Integer documentID = similarityList.nextElement();
				similarity.put(documentID, similarity.get(documentID) / reader.norms("contents")[documentID]);
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return similarity;
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

class MyComparator implements Comparator
{
	public int compare(Object obj1, Object obj2)
	{
		int result=0;
		Map.Entry e1 = (Map.Entry)obj1 ;
		Map.Entry e2 = (Map.Entry)obj2 ;
		Float value1 = (Float)e1.getValue();
		Float value2 = (Float)e2.getValue();
		if(value1.compareTo(value2) == 0)
		{
			Integer word1=(Integer)e1.getKey();
			Integer word2=(Integer)e2.getKey();
			result = word1.compareTo(word2);
		} 
		else
		{
			result = value2.compareTo(value1);
		}
		return result;
	 }
}