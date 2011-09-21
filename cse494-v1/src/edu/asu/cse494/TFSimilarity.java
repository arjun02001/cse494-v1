//Arjun Mukherji
package edu.asu.cse494;
import com.lucene.document.Document;
import com.lucene.document.Field;
import com.lucene.index.*;
import java.lang.*;
import java.io.ObjectInputStream.GetField;
import java.util.*;

public class TFSimilarity 
{
	static Double[] norm;
	public static void main(String[] args) 
	{
		try
		{
			long start = System.currentTimeMillis();
			IndexReader reader = IndexReader.open("result3index");
			computeNorm(reader);
			long end = System.currentTimeMillis();
			System.out.println(end - start);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	private static void computeNorm(IndexReader reader)
	{
		norm = new Double[reader.numDocs()];
		try
		{
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
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
}
