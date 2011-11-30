//Arjun Mukherji
package edu.asu.cse494;

import com.lucene.document.Document;
import com.lucene.document.Field;
import com.lucene.index.*;

import java.io.*;
import java.io.ObjectInputStream.GetField;
import java.lang.*;
import java.sql.Savepoint;
import java.util.*;
import java.util.Map.Entry;

import org.omg.CORBA.DoubleSeqHolder;

public class Snippet 
{
	Hashtable<Integer, Hashtable<Integer, String>> positionInfo = new Hashtable<Integer, Hashtable<Integer,String>>();
	Hashtable<Integer, Double> docs = new Hashtable<Integer, Double>();
	TFIDFSimilarity sim = null;
	int topKDocs = 10;
	
	public static void main(String[] args) 
	{
		Snippet snippet = new Snippet();
		//snippet.createPositionInfo();
		//snippet.savePositionInfo();
		snippet.loadPositionInfo();
		snippet.startCalculation();
	}
	
	public Snippet()
	{
		sim = new TFIDFSimilarity();
	}
	
	private void startCalculation()
	{
		try
		{
			while(true)
			{
				getRootSet();
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	private void getRootSet()
	{
		try
		{
			int count = 0;
			IndexReader reader = IndexReader.open("result3index");
			Hashtable<Integer, Double> similarity = sim.computeSimilarity();
			docs = sim.getTopKResults(similarity, topKDocs);
			ArrayList myArrayList=new ArrayList(docs.entrySet());
			Collections.sort(myArrayList, new MyComparator1());
			Iterator itr=myArrayList.iterator();
			while(itr.hasNext())
			{
				Map.Entry<Integer, Double> entry = (Map.Entry<Integer, Double>)itr.next();
				System.out.println((count + 1) + ". docid = " + entry.getKey() + "  " + reader.document(entry.getKey()).get("url"));
				System.out.println(getSnippet(entry.getKey()));
				count++;
			}
			reader.close();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	private String getSnippet(int doc)
	{
		String snippet = "";
		int termPos = -1;
		try
		{
			for(Entry<String, Double> entry : sim.tokenizedQuery.entrySet())
			{
				termPos = getTermPosition(doc, entry.getKey());
				if(termPos != -1)
				{
					int pos = termPos - 10;
					while(pos <= termPos + 10)
					{
						snippet += (positionInfo.get(doc).get(pos) == null) ? " " : positionInfo.get(doc).get(pos) + " ";
						pos++;
					}
				}
				snippet += "\n";
			}
			return snippet;
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return "";
	}
	
	private int getTermPosition(int doc, String term)
	{
		try
		{
			if(positionInfo.containsKey(doc))
			{
				for(Entry<Integer, String> entry : positionInfo.get(doc).entrySet())
				{
					if(term.equalsIgnoreCase(entry.getValue()))
					{
						return entry.getKey();
					}
				}
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return -1;
	}
	
	private void loadPositionInfo()
	{
		try
		{
			System.out.println("loading position index. may take 15-20 secs");
			long start = System.currentTimeMillis();
			FileReader fr = new FileReader("positioninfo.txt");
			BufferedReader br = new BufferedReader(fr);
			String line = "";
			while((line = br.readLine()) != null)
			{
				String[] content = line.split(";");
				int doc = Integer.parseInt(content[0]);
				Hashtable<Integer, String> innerMap = new Hashtable<Integer, String>();
				String[] posTerms = content[1].split(",");
				for(String posTerm : posTerms)
				{
					String[] text = posTerm.split(" ");
					innerMap.put(Integer.parseInt(text[0]), text[1]);
				}
				positionInfo.put(doc, innerMap);
			}
			br.close();
			fr.close();
			long end = System.currentTimeMillis();
			//System.out.println("loading position info took " + (end - start) + " ms");
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	private void createPositionInfo()
	{
		try
		{
			IndexReader reader = IndexReader.open("result3index");
			TermEnum termEnum = reader.terms();
			while(termEnum.next())
			{
				if(termEnum.term().field().equals("contents"))
				{
					System.out.println(termEnum.term().text());
					TermPositions termPositions = reader.termPositions(termEnum.term());
					while(termPositions.next())
					{
						if(positionInfo.containsKey(termPositions.doc()))
						{
							Hashtable<Integer, String> posTerm = positionInfo.get(termPositions.doc());
							for(int i = 0; i < termPositions.freq(); i++)
							{
								posTerm.put(termPositions.nextPosition(), termEnum.term().text());
							}
							positionInfo.put(termPositions.doc(), posTerm);
						}
						else
						{
							Hashtable<Integer, String> posTerm = new Hashtable<Integer, String>();
							for(int i = 0; i < termPositions.freq(); i++)
							{
								posTerm.put(termPositions.nextPosition(), termEnum.term().text());
							}
							positionInfo.put(termPositions.doc(), posTerm);
						}
					}
				}
			}
			reader.close();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	private void savePositionInfo()
	{
		try
		{
			FileWriter fw = new FileWriter("positioninfo.txt");
			PrintWriter pw = new PrintWriter(fw);
			for(Map.Entry<Integer, Hashtable<Integer, String>> entry : positionInfo.entrySet())
			{
				//System.out.println(entry.getKey());
				pw.print(entry.getKey() + ";");
				for(Map.Entry<Integer, String> innerEntry : entry.getValue().entrySet())
				{
					pw.print(innerEntry.getKey() + " " + innerEntry.getValue() + ",");
				}
				pw.println();
			}
			pw.close();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
}
