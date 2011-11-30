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

import org.omg.CORBA.DoubleSeqHolder;

public class Snippet 
{
	Hashtable<Integer, Hashtable<Integer, String>> positionInfo = new Hashtable<Integer, Hashtable<Integer,String>>();
	
	public static void main(String[] args) 
	{
		Snippet snippet = new Snippet();
		//snippet.createPositionInfo();
		//snippet.savePositionInfo();
		snippet.loadPositionInfo();
	}
	
	public Snippet()
	{
		
	}
	
	private void loadPositionInfo()
	{
		try
		{
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
			System.out.println("loading position info took " + (end - start) + " ms");
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
				System.out.println(entry.getKey());
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
