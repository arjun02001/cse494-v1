package edu.asu.cse494;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import com.lucene.index.IndexReader;
import com.lucene.index.Term;
import com.lucene.index.TermDocs;
import com.lucene.index.TermEnum;

public class AH 
{
	Hashtable<Integer, Double> similarity = new Hashtable<Integer, Double>();
	ArrayList<Integer> docs = new ArrayList<Integer>();
	TFIDFSimilarity sim = null;
	int topKDocs = 5;
	
	public static void main(String[] args) 
	{
		try
		{
			AH ah = new AH();
			ah.startCalculation();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public AH()
	{
		try
		{
			sim = new TFIDFSimilarity();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	private void startCalculation()
	{
		try
		{
			while(true)
			{
				docs = getBaseSet();
				for(int i:docs)
				{
					System.out.println(i);
				}
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public ArrayList<Integer> getBaseSet()
	{
		try
		{
			similarity = sim.computeSimilarity();
			
			System.out.println("how many documents from TF/IDF results do you want to fetch ?");
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			topKDocs = Integer.parseInt(in.readLine());
		
			docs = sim.getTopKResults(similarity, topKDocs);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return docs;
	}
}