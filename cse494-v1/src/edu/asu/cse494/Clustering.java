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


public class Clustering 
{
	Hashtable<Integer, Hashtable<String, Integer>> forwardIndex = new Hashtable<Integer, Hashtable<String,Integer>>();
	
	public static void main(String[] args) 
	{
		Clustering clustering = new Clustering();
		//clustering.saveForwardIndex();
		clustering.loadForwardIndex();
		
	}
	
	private void loadForwardIndex()
	{
		try
		{
			long start = System.currentTimeMillis();
			FileReader fr = new FileReader("forwardindex.txt");
			BufferedReader br = new BufferedReader(fr);
			String line = "";
			while((line = br.readLine()) != null)
			{
				String[] content = line.split(";");
				int doc = Integer.parseInt(content[0]);
				System.out.println(doc);
				if(content.length > 1 && content[1].length() > 0)
				{
					Hashtable<String, Integer> innerMap = new Hashtable<String, Integer>();
					for(String wordFreq : content[1].split(","))
					{
						innerMap.put(wordFreq.split(" ")[0], Integer.parseInt(wordFreq.split(" ")[1]));
					}
					forwardIndex.put(doc, innerMap);
				}
			}
			br.close();
			fr.close();
			long end = System.currentTimeMillis();
			System.out.println(end - start);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	private void saveForwardIndex()
	{
		try
		{
			buildForwardIndex();
			writeForwardIndex();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	private void writeForwardIndex()
	{
		try
		{
			FileWriter fw = new FileWriter("forwardindex_temp.txt");
			PrintWriter pw = new PrintWriter(fw);
			for(Map.Entry<Integer, Hashtable<String, Integer>> entry : forwardIndex.entrySet())
			{
				System.out.println(entry.getKey());
				pw.print(entry.getKey() + ";");
				for(Map.Entry<String, Integer> innerEntry : entry.getValue().entrySet())
				{
					pw.print(innerEntry.getKey() + " " + innerEntry.getValue() + ",");
				}
				pw.println();
			}
			pw.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	private void buildForwardIndex()
	{
		try
		{
			IndexReader reader = IndexReader.open("result3index");
			TermEnum termEnum = reader.terms();
			while(termEnum.next())
			{
				System.out.println("Term = " + termEnum.term().text());
				if(termEnum.term().field().equals("contents"))
				{
					TermDocs termDocs = reader.termDocs(termEnum.term());
					if(termDocs != null)
					{
						while(termDocs.next())
						{
							System.out.println("               " + "DocNo = " + termDocs.doc() + "  Freq = " + termDocs.freq());
							if(forwardIndex.containsKey(termDocs.doc()))
							{
								Hashtable<String, Integer> innerMap = forwardIndex.get(termDocs.doc());
								innerMap.put(termEnum.term().text(), termDocs.freq());
								forwardIndex.put(termDocs.doc(), innerMap);
							}
							else
							{
								Hashtable<String, Integer> innerMap = new Hashtable<String, Integer>();
								innerMap.put(termEnum.term().text(), termDocs.freq());
								forwardIndex.put(termDocs.doc(), innerMap);
							}
						}
					}
				}
			}
			reader.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
}
