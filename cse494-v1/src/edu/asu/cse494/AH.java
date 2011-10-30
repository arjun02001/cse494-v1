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
	//hashtable to hold the docid and their corresponding index in the adjacency matrix
	Hashtable<Integer, Integer> baseSet = new Hashtable<Integer, Integer>();
	//hashtable to hold the docid and their similarity score
	Hashtable<Integer, Double> docs = new Hashtable<Integer, Double>();
	TFIDFSimilarity sim = null;
	int topKDocs = 10, count = 0, numDocs = 25053;
	double threshold = 0.00000000001;
	//2d array to hold the adjacency matrix
	double[][] mat;
	//arrays to hold the authority and hub score
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
			//create a new instance of the tf-idf similarity class. this will take user query
			//and compute the similarity
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
				getRootSet();
				getBaseSet();
				computeAH();
				displayAH();
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	//method to calculate the tf-idf similarity and get the top 10 results
	//this will constitute our rootset
	public void getRootSet()
	{
		try
		{
			Hashtable<Integer, Double> similarity = sim.computeSimilarity();
			//System.out.println("how many documents from TF/IDF results do you want to fetch for AH computation?");
			//BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			//topKDocs = Integer.parseInt(in.readLine());
			docs = sim.getTopKResults(similarity, topKDocs);
			System.out.println("top " + topKDocs + " tf-idf results are");
			System.out.println("-----------------------------------------");
			count = 0;
			IndexReader reader = IndexReader.open("result3index");
			for(int doc:docs.keySet())
			{
				System.out.println((count + 1) + ". docid = " + doc + "  " + reader.document(doc).get("url"));
				count++;
			}
			reader.close();
			count = 0;
			baseSet.clear();
			for(int doc:docs.keySet())
			{
				baseSet.put(doc, count++);
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	//method to construct the baseset by utilizing linkanalysis.java.
	public void getBaseSet()
	{
		try
		{
			LinkAnalysis.numDocs = numDocs;
			LinkAnalysis la = new LinkAnalysis();
			for(int doc:docs.keySet())
			{
				//get all links for a doc
				int[] links = la.getLinks(doc);
				for(int link:links)
				{
					if(!baseSet.containsKey(link))
					{
						baseSet.put(link, count++);
					}
				}
				//get all citations for a doc
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
	}
	
	//method to calculate the authority hub score
	public void computeAH()
	{
		try
		{
			LinkAnalysis.numDocs = numDocs;
			LinkAnalysis la = new LinkAnalysis();
			mat = new double[baseSet.size()][baseSet.size()];
			//construct the adjacency matrix
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
			//power iterate on the matrix
			powerIterate(mat);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	//method to perform power iteration
	public void powerIterate(double[][] mat)
	{
		try
		{
			double[][] matT = getTranspose(mat);
			//to keep track of the hub values for the previous iteration
			double[] previousHub = new double[mat.length];
			//to keep track of the authority values for the previous iteration
			double[] previousAuthority = new double[mat.length];
			int iterationCount = 0;
			hub = new double[mat.length];
			authority = new double[mat.length];
			//initialize all hub values to 1
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
				//get the authority values by multiplying the matTranspose with the hub
				authority = matrixMultiply(matT, hub);
				//get the hub values by multiplying the mat with authority
				hub = matrixMultiply(mat, authority);
				//normalize the vectors
				authority = normalizeVector(authority);
				hub = normalizeVector(hub);
				//check if the values change by a threshold
				if(computeDistance(authority, previousAuthority) <= threshold && computeDistance(hub, previousHub) <= threshold)
				{
					break;
				}
				//continue power iteration if the threshold is not reached
				previousAuthority = authority;
				previousHub = hub;
				iterationCount++;
			}
			System.out.println("\npower iteration converged in " + iterationCount + " iterations. threshold = " + threshold);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	//method to display the authority and hub values
	public void displayAH()
	{
		try
		{
			System.out.println();
			System.out.println("top 10 authorities are");
			System.out.println("-----------------------------");
			display(authority);
			System.out.println();
			System.out.println("top 10 hub are");
			System.out.println("-----------------------------");
			display(hub);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	//method to sort and display the top k values of a vector
	private void display(double[] vector)
	{
		try
		{
			IndexReader reader = IndexReader.open("result3index");
			int k = (vector.length >= 10) ? 10 : vector.length;
			Hashtable<Integer, Double> docValue = new Hashtable<Integer, Double>();
			for(int docid:baseSet.keySet())
			{
				docValue.put(docid, vector[baseSet.get(docid)]);
			}
			ArrayList myArrayList=new ArrayList(docValue.entrySet());
			Collections.sort(myArrayList, new MyComparator1());
			Iterator itr=myArrayList.iterator();
			int counter = 0;
			while(itr.hasNext())
			{
				Map.Entry<Integer, Double> e = (Map.Entry<Integer, Double>)itr.next();
				System.out.println((counter + 1) + ". docid = " + e.getKey() + "  " + reader.document(e.getKey()).get("url"));
				counter++;
				if(counter >= k)
				{
					break;
				}
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	//method to get the difference between 2 vectors
	//used during power iteration to check if the threshold has been reached
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
	
	//method to normalize a vector
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
	
	//method to multiply a matrix with a vector
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
	
	//method to return the transpose of a matrix
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
}