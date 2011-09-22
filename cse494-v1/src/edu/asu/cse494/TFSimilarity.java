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
	static String normFile = "tfnorm.txt";
	public static void main(String[] args) 
	{
		try
		{
			IndexReader reader = IndexReader.open("result3index");
			buildNorm(reader);
			System.out.println(norm[12036]);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
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
		norm = new Double[reader.numDocs()];
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
							if(norm[termDocs.doc()] != null)
							{
								norm[termDocs.doc()] += Math.pow(termDocs.freq(), 2);
							}
							else
							{
								norm[termDocs.doc()] = Math.pow(termDocs.freq(), 2);
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
