//Arjun Mukherji
package edu.asu.cse494;
import com.lucene.document.Document;
import com.lucene.document.Field;
import com.lucene.index.*;

import java.io.*;
import java.io.ObjectInputStream.GetField;
import java.lang.*;
import java.util.*;

public class TFIDFSimilarity 
{
	static Double[] norm;
	static String normFile = "tfIdfNorm.txt";
	public static void main(String[] args) 
	{
		String input = "theta grades";
		TermDocs termDocs = null;
		try
		{
			long start = System.currentTimeMillis();
			IndexReader reader = IndexReader.open("result3index");
			Hashtable<String, Integer> tokenizedQuery = getTokenizedQuery(input);
			Hashtable<Integer, Double> similarity = new Hashtable<Integer, Double>();
			buildNorm(reader);
			
			Enumeration<String> queryKeywords = tokenizedQuery.keys();
			int corpusCount = reader.numDocs();
			while(queryKeywords.hasMoreElements())
			{
				String queryKeyword = queryKeywords.nextElement();
				termDocs = reader.termDocs(new Term("contents", queryKeyword));
				if(termDocs != null)
				{
					int count = 0;
					while(termDocs.next())
					{
						count++;
					}
					double idf = Math.log((double)(corpusCount / count));
					termDocs = reader.termDocs(new Term("contents", queryKeyword));
					while(termDocs.next())
					{
						if(similarity.containsKey(termDocs.doc()))
						{
							similarity.put(termDocs.doc(), similarity.get(termDocs.doc()) + (tokenizedQuery.get(queryKeyword) * termDocs.freq() * idf));
						}
						else
						{
							similarity.put(termDocs.doc(), (double)(tokenizedQuery.get(queryKeyword) * termDocs.freq() * idf));
						}
					}
				}
			}
			similarity = normalizeSimilarity(similarity, reader);
			sortedResult(similarity, reader);
			reader.close();
			long end = System.currentTimeMillis();
			System.out.println("total time taken " + (end - start) + " ms");
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
				similarity.put(documentID, similarity.get(documentID) / Math.sqrt(norm[documentID]));
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
	
	//Call this method to create the in-memory norm array Double norm[] from a file. FASTEST WAY
	private static void buildNorm(IndexReader reader)
	{
		if(!new File(normFile).exists())
		{
			computeNorm(reader);
			writeToFile();
			return;
		}
		norm = new Double[reader.numDocs()];
		try
		{
			long start = System.currentTimeMillis();
			FileInputStream fis = new FileInputStream(normFile);
			DataInputStream dis = new DataInputStream(fis);
			BufferedReader br = new BufferedReader(new InputStreamReader(dis));
			String line;
			while((line = br.readLine()) != null)
			{
				String value[] = line.split("\\s+");
				norm[Integer.parseInt(value[0])] = value[1].equals("null") ? null : Double.parseDouble(value[1]);
			}
			long end = System.currentTimeMillis();
			fis.close();
			dis.close();
			br.close();
			System.out.println("building norm took " + (end - start) + " ms");
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	//Call this method if you want to write the in-memory norm array 'Double norm[]' to a file
	private static void writeToFile()
	{
		try
		{
			long start = System.currentTimeMillis();
			FileWriter fw = new FileWriter(normFile);
			PrintWriter pw = new PrintWriter(fw);
			for(int i = 0; i < norm.length; i++)
			{
				pw.print(i);
				pw.print(" ");
				pw.println(norm[i]);
				pw.flush();
			}
			fw.close();
			pw.close();
			long end = System.currentTimeMillis();
			System.out.println("writing norm to file took " + (end - start) + " ms");
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	//Call this method if you want to create in-memory norm array from scratch. Double norm[] will have the values
	private static void computeNorm(IndexReader reader)
	{
		System.out.println("computing norm only for the first time ..");
		norm = new Double[reader.numDocs()];
		int corpusCount = reader.numDocs();
		TermDocs termDocs = null;
		try
		{
			long start = System.currentTimeMillis();
			TermEnum termEnum = reader.terms();
			while(termEnum.next())
			{
				if(termEnum.term().field().equals("contents"))
				{
					termDocs = reader.termDocs(termEnum.term());
					if(termDocs != null)
					{
						int count = 0;
						while(termDocs.next())
						{
							count++;
						}
						double idf = Math.log((double)(corpusCount / count));
						termDocs = reader.termDocs(termEnum.term());
						while(termDocs.next())
						{
							if(norm[termDocs.doc()] != null)
							{
								norm[termDocs.doc()] += Math.pow((termDocs.freq() * idf), 2);
							}
							else
							{
								norm[termDocs.doc()] = Math.pow((termDocs.freq() * idf), 2);
							}
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
