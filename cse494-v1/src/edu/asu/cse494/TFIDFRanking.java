//Arjun Mukherji

package edu.asu.cse494;
import com.lucene.document.Document;
import com.lucene.document.Field;
import com.lucene.index.*;
import java.io.ObjectInputStream.GetField;
import java.lang.*;
import java.util.*;

public class TFIDFRanking 
{
	static Hashtable<String, Double> idfs = new Hashtable<String, Double>(); 
	
	public static void main(String[] args) 
	{
		String input = "aaa";
		try
		{
			IndexReader reader = IndexReader.open("result3index");
			Hashtable<String, Integer> tokenizedQuery = getTokenizedQuery(input);
			Hashtable<Integer, Double> similarity = new Hashtable<Integer, Double>();
			
			computeIDF(reader);
			
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
							similarity.put(termDocs.doc(), (double) (tokenizedQuery.get(queryKeyword) * termDocs.freq()));
						}
					}
				}
			}
			similarity = normalizeSimilarity(similarity, reader);
			sortedResult(similarity, reader);
			System.out.println("done");
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	private static void sortedResult(Hashtable<Integer, Double> similarity, IndexReader reader)
	{
		try
		{
			ArrayList myArrayList=new ArrayList(similarity.entrySet());
			Collections.sort(myArrayList, new MyComparator());
			Iterator itr=myArrayList.iterator();
			int count = 0;
			while(itr.hasNext())
			{
				Map.Entry<Integer, Double> e = (Map.Entry<Integer, Double>)itr.next();
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
	
	private static Hashtable<Integer, Double> normalizeSimilarity(Hashtable<Integer, Double> similarity, IndexReader reader)
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
	
	private static void computeIDF(IndexReader reader)
	{
		try
		{
			int corpusCount = reader.numDocs();
			TermEnum termEnum = reader.terms();
			while(termEnum.next())
			{
				TermDocs termDocs = reader.termDocs(termEnum.term());
				int termDocsCount = 0;
				while(termDocs.next())
				{
					termDocsCount++;
				}
				if(!idfs.containsKey(termEnum.term().text()))
				{
					idfs.put(termEnum.term().text(), Math.log((double)(corpusCount / termDocsCount)));
				}
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
}
