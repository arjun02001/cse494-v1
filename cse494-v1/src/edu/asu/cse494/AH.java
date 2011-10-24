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
	Hashtable<Integer, Integer> baseSet = new Hashtable<Integer, Integer>();
	Hashtable<Integer, Integer> inverseBaseSet = new Hashtable<Integer, Integer>();
	ArrayList<Integer> docs = new ArrayList<Integer>();
	TFIDFSimilarity sim = null;
	int topKDocs = 5, count = 0, numDocs = 25053;
	double threshold = 0.00000000001;
	double[][] mat;
	double[] hub, authority;
	
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
				computeAH(baseSet);
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
			System.out.println("how many documents from TF/IDF results do you want to fetch for AH computation?");
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			topKDocs = Integer.parseInt(in.readLine());
		
			docs = sim.getTopKResults(sim.computeSimilarity(), topKDocs);
			count = 0;
			for(int doc:docs)
			{
				baseSet.put(doc, count);
				inverseBaseSet.put(count, doc);
				count++;
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
						baseSet.put(link, count);
						inverseBaseSet.put(count, link);
						count++;
					}
				}
				int[] citations = la.getCitations(doc);
				for(int citation:citations)
				{
					if(!baseSet.containsKey(citation))
					{
						baseSet.put(citation, count);
						inverseBaseSet.put(count, citation);
						count++;
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
	
	public void computeAH(Hashtable<Integer, Integer> baseSet)
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
			powerIterate(mat);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public void powerIterate(double[][] mat)
	{
		try
		{
			double[][] matT = getTranspose(mat);
			double[] previousHub = new double[mat.length];
			double[] previousAuthority = new double[mat.length];
			int iterationCount = 0;
			hub = new double[mat.length];
			authority = new double[mat.length];
			for(int i = 0; i < mat.length; i++)
			{
				hub[i] = 1;
			}
			for(int i = 0; i < mat.length; i++)
			{
				previousAuthority[i] = previousHub[i] = 0;
			}
			for(;;)
			{
				authority = matrixMultiply(matT, hub);
				hub = matrixMultiply(mat, authority);
				authority = normalizeVector(authority);
				hub = normalizeVector(hub);
				if(computeDistance(authority, previousAuthority) < threshold && computeDistance(hub, previousHub) < threshold)
				{
					break;
				}
				previousAuthority = authority;
				previousHub = hub;
				iterationCount++;
			}
			System.out.println("\nno. of iterations = " + iterationCount);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	private double computeDistance(double[] a, double[] b)
	{
		double norm = 0;
		try
		{
			for(int i = 0; i < a.length; i++)
			{
				norm += Math.pow(a[i] - b[i], 2);
			}
			norm = Math.sqrt(norm);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return norm;
	}
	
	public double[] normalizeVector(double[] v)
	{
		try
		{
			double norm = 0;
			for(double d:v)
			{
				norm += Math.pow(d, 2);
			}
			norm = Math.sqrt(norm);
			for(int i = 0; i < v.length; i++)
			{
				v[i] /= norm;
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return v;
	}
	
	public double[] matrixMultiply(double[][] a, double[] b)
	{
		double[] c = new double[a.length];
		try
		{
			for(int i = 0; i < a.length; i++)
			{
				for(int j = 0; j < a.length; j++)
				{
					c[i] += a[i][j] * b[j];
				}
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return c;
	}
	
	public double[][] getTranspose(double[][] mat)
	{
		double[][] matT = new double[mat.length][mat.length];
		try
		{
			for(int i = 0; i < mat.length; i++)
			{
				for(int j = 0; j < mat.length; j++)
				{
					matT[j][i] = mat[i][j];
				}
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return matT;
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