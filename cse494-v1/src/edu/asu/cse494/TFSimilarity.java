//Arjun Mukherji
package edu.asu.cse494;
import com.lucene.document.Document;
import com.lucene.document.Field;
import com.lucene.index.*;

import java.io.ObjectInputStream.GetField;
import java.util.*;

public class TFSimilarity 
{
	static Hashtable<Integer, Double> norm = new Hashtable<Integer, Double>();
	
	public static void main(String[] args) 
	{
		try
		{
			IndexReader reader = IndexReader.open("result3index");
			computeNorm(reader);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	private static void computeNorm(IndexReader reader)
	{
		try
		{
			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
}
