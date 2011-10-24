package edu.asu.cse494;

import java.awt.image.ConvolveOp;
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
	Hashtable<Integer, Integer> baseSet = new Hashtable<Integer, Integer>();
	ArrayList<Integer> docs = new ArrayList<Integer>();
	TFIDFSimilarity sim = null;
	int topKDocs = 5, count = 0, numDocs = 25053;
	double[][] mat;
	
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
				baseSet = getRootSet();
				baseSet = getBaseSet();
				mat = computeAH(baseSet);
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public Hashtable<Integer, Integer> getRootSet()
	{
		try
		{
			similarity = sim.computeSimilarity();
			
			System.out.println("how many documents from TF/IDF results do you want to fetch ?");
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			topKDocs = Integer.parseInt(in.readLine());
		
			docs = sim.getTopKResults(similarity, topKDocs);
			count = 0;
			for(int doc:docs)
			{
				baseSet.put(doc, count++);
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return baseSet;
	}
	
	public Hashtable<Integer, Integer> getBaseSet()
	{
		try
		{
			LinkAnalysis.numDocs = numDocs;
			LinkAnalysis la = new LinkAnalysis();
			for(int doc:docs)
			{
				int[] links = la.getLinks(doc);
				for(int link:links)
				{
					if(!baseSet.containsKey(link))
					{
						baseSet.put(link, count++);
					}
				}
				int[] citations = la.getCitations(doc);
				for(int citation:citations)
				{
					if(!baseSet.containsKey(citation))
					{
						baseSet.put(citation, count++);
					}
				}
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return baseSet;
	}
	
	public double[][] computeAH(Hashtable<Integer, Integer> baseSet)
	{
		try
		{
			LinkAnalysis.numDocs = numDocs;
			LinkAnalysis la = new LinkAnalysis();
			mat = new double[baseSet.size()][baseSet.size()];
			for(int docid:baseSet.keySet())
			{
				for(int link:la.getLinks(docid))
				{
					if(baseSet.containsKey(link))
					{
						mat[baseSet.get(docid)][baseSet.get(link)] = 1;
					}
				}
			}
			printMatrix(mat, baseSet.size());
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return mat;
	}
	
	public void printMatrix(double[][] mat, int size)
	{
		try
		{
			System.out.println(size);
			for(int i = 0; i < size; i++)
			{
				for(int j = 0; j < size; j++)
				{
					System.out.print((int)mat[i][j] + " ");
				}
				System.out.println();
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
}