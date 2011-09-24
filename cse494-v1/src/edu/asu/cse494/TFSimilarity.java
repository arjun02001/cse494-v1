//Arjun Mukherji
package edu.asu.cse494;
import com.lucene.document.Document;
import com.lucene.document.Field;
import com.lucene.index.*;

import java.lang.*;
import java.io.*;
import java.util.*;

public class TFSimilarity 
{
	static Double[] norm;
	public static void main(String[] args) 
	{
		try
		{
			IndexReader reader = IndexReader.open("result3index");
			computeNorm(reader);
			while(true)
			{
				System.out.print("query: ");
				BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
				String input = in.readLine();
				if(input.length() == -1)
				{
					break;
				}
				long start = System.currentTimeMillis();				
				Hashtable<String, Integer> tokenizedQuery = getTokenizedQuery(input);
				Hashtable<Integer, Double> similarity = new Hashtable<Integer, Double>();
			
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
				long end = System.currentTimeMillis();
				System.out.println("------" + similarity.size() + " documents found------");
				System.out.println("------total time taken " + (end - start) + " ms------");
				sortedResult(similarity, reader);
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	//Call this method to print the sorted result
	private static void sortedResult(Hashtable<Integer, Double> similarity, IndexReader reader)
	{
		try
		{
			ArrayList myArrayList=new ArrayList(similarity.entrySet());
			Collections.sort(myArrayList, new MyComparator());
			Iterator itr=myArrayList.iterator();
			int count = 0, maxResult = 0;
			while(itr.hasNext())
			{
				Map.Entry<Integer, Double> e = (Map.Entry<Integer, Double>)itr.next();
				count++;
				System.out.println(count + ". " + reader.document(e.getKey()).get("url") + "  DocID-" + e.getKey());
				maxResult++;
				if(maxResult == 10)
				{
					System.out.print("more (y|n) ? ");
					BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
					String input = in.readLine();
					if(input.equals("n"))
					{
						break;
					}
					else
					{
						maxResult = 0;
					}
				}
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	//Call this method to normalize the similarity
	private static Hashtable<Integer, Double> normalizeSimilarity(Hashtable<Integer, Double> similarity, IndexReader reader)
	{
		try
		{
			Enumeration<Integer> similarityList = similarity.keys();
			while(similarityList.hasMoreElements())
			{
				Integer documentID = similarityList.nextElement();
				similarity.put(documentID, similarity.get(documentID) / Math.sqrt(norm[documentID]));
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return similarity;
	}
	
	//Call this method to split the query into terms and their frequencies
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
	
	//Call this method if you want to create in-memory norm array from scratch. Double norm[] will have the values
	private static void computeNorm(IndexReader reader)
	{
		System.out.println("computing norm only for the first time .. may take around 15-20 secs");
		norm = new Double[reader.numDocs()];
		Arrays.fill(norm, 0d);
		try
		{
			long start = System.currentTimeMillis();
			TermEnum termEnum = reader.terms();
			while(termEnum.next())
			{
				if(termEnum.term().field().equals("contents"))
				{
					TermDocs termDocs = reader.termDocs(termEnum.term());
					if(termDocs != null)
					{
						while(termDocs.next())
						{
							norm[termDocs.doc()] += Math.pow(termDocs.freq(), 2);
						}
					}
				}
			}
			long end = System.currentTimeMillis();
			System.out.println("computing norm took " + (end - start) + " ms");
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
}

class MyComparator implements Comparator
{
	public int compare(Object obj1, Object obj2)
	{
		int result=0;
		Map.Entry e1 = (Map.Entry)obj1 ;
		Map.Entry e2 = (Map.Entry)obj2 ;
		Double value1 = (Double)e1.getValue();
		Double value2 = (Double)e2.getValue();
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