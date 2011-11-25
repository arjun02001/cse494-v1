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


public class Clustering 
{
	Hashtable<Integer, Hashtable<String, Double>> forwardIndex = new Hashtable<Integer, Hashtable<String,Double>>();
	Hashtable<Integer, Double> rootSet = new Hashtable<Integer, Double>();
	Hashtable<Integer, ArrayList<Integer>> cluster = new Hashtable<Integer, ArrayList<Integer>>();
	Hashtable<Integer, ArrayList<Integer>> previousCluster = new Hashtable<Integer, ArrayList<Integer>>();
	TFIDFSimilarity sim = null;
	double[] norm;
	int topKDocs = 10, clusterSize = 3, pseudoDoc = 25053, corpusCount = 25053;
	
	public static void main(String[] args) 
	{
		Clustering clustering = new Clustering();
		//clustering.saveForwardIndex();
		clustering.loadForwardIndex();
		clustering.startCalculation();
	}
	
	public Clustering()
	{
		sim = new TFIDFSimilarity();
		norm = getNorm();
	}
	
	private double[] getNorm()
	{
		norm = new double[25053];
		try
		{
			FileReader fr = new FileReader("tfnorm.txt");
			BufferedReader br = new BufferedReader(fr);
			String line = "";
			int count = 0;
			while((line = br.readLine()) != null)
			{
				norm[count++] = Double.valueOf(line).doubleValue();
			}
			br.close();
			fr.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return norm;
	}
	
	private double getNorm(Hashtable<String, Double> doc)
	{
		double norm = 0.0;
		try
		{
			for(Map.Entry<String, Double> entry : doc.entrySet())
			{
				norm += Math.pow(entry.getValue(), 2);
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return Math.sqrt(norm);
	}
	
	private void startCalculation()
	{
		getRootSet();
		pseudoDoc = corpusCount;
		formClusters();
	}
	
	private void getRootSet()
	{
		try
		{
			rootSet = sim.getTopKResults(sim.computeSimilarity(), topKDocs);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	private void formClusters()
	{
		//Hashtable<Integer, ArrayList<Integer>> previousCluster = new Hashtable<Integer, ArrayList<Integer>>();
		try
		{
			pickSeeds();
			while(true)
			{
				previousCluster = cluster;
				assignDocsToClusters();
				if(checkConvergence(previousCluster, cluster))
				{
					break;
				}
				ArrayList<Integer> newSeeds = new ArrayList<Integer>();
				System.out.println();
				for(Map.Entry<Integer, ArrayList<Integer>> entry : cluster.entrySet())
				{
					Hashtable<String, Double> centroid = getCentroid(entry.getValue());
					System.out.println();
					System.out.print(entry.getKey() + "-> ");
					for(int doc : entry.getValue())
					{
						System.out.print(doc + ", ");
					}
					newSeeds.add(pseudoDoc);
					forwardIndex.put(pseudoDoc++, centroid);
				}
				pickSeeds(newSeeds);
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	private boolean checkConvergence(Hashtable<Integer, ArrayList<Integer>> previous, Hashtable<Integer, ArrayList<Integer>> current)
	{
		try
		{
			for(Map.Entry<Integer, ArrayList<Integer>> entry : previous.entrySet())
			{
				if(!current.containsValue(entry.getValue()))
				{
					return false;
				}
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return true;
	}
	
	private void pickSeeds(ArrayList<Integer> seeds)
	{
		try
		{
			cluster.clear();
			for(int seed : seeds)
			{
				cluster.put(seed, new ArrayList<Integer>());
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	private void pickSeeds()
	{
		try
		{
			int count = 0;
			cluster.clear();
			for(Map.Entry<Integer, Double> entry : rootSet.entrySet())
			{
				cluster.put(entry.getKey(), new ArrayList<Integer>());
				count++;
				if(count >= clusterSize)
				{
					break;
				}
			}
			/*cluster.put(16674, new ArrayList<Integer>());
			cluster.put(17013, new ArrayList<Integer>());
			cluster.put(16808, new ArrayList<Integer>());*/
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	private void assignDocsToClusters()
	{
		try
		{
			int docWithMaxSim = -1;
			double maxSim = 0.0;
			for(Map.Entry<Integer, Double> rootSetEntry : rootSet.entrySet())
			{
				docWithMaxSim = -1;
				maxSim = 0.0;
				for(Map.Entry<Integer, ArrayList<Integer>> clusterEntry : cluster.entrySet())
				{
					double sim = documentDocumentSimilarity(rootSetEntry.getKey(), clusterEntry.getKey());
					if(sim > maxSim)
					{
						maxSim = sim;
						docWithMaxSim = clusterEntry.getKey();
					}
				}
				ArrayList<Integer> docsInCluster = cluster.get(docWithMaxSim);
				docsInCluster.add(rootSetEntry.getKey());
				cluster.put(docWithMaxSim, docsInCluster);
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	private double documentDocumentSimilarity(int doc1, int doc2)
	{
		double sim = 0.0;
		Hashtable<String, Double> termFreq1 = null;
		Hashtable<String, Double> termFreq2 = null;
		try
		{
			termFreq1 = forwardIndex.get(doc1);
			termFreq2 = forwardIndex.get(doc2);
			sim = (termFreq1.size() < termFreq2.size()) ? documentDocumentDotProduct(termFreq1, termFreq2) : documentDocumentDotProduct(termFreq2, termFreq1);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		double norm1 = (doc1 > (corpusCount - 1)) ? getNorm(termFreq1) : Math.sqrt(norm[doc1]);
		double norm2 = (doc2 > (corpusCount - 1)) ? getNorm(termFreq2) : Math.sqrt(norm[doc2]);
		return sim / (norm1 * norm2);
	}
	
	private double documentDocumentDotProduct(Hashtable<String, Double> termFreq1, Hashtable<String, Double> termFreq2)
	{
		double sim = 0.0;
		try
		{
			for(Map.Entry<String, Double> entry : termFreq1.entrySet())
			{
				if(termFreq2.containsKey(entry.getKey()))
				{
					sim += entry.getValue() * termFreq2.get(entry.getKey());
				}
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return sim;
	}
	
	private Hashtable<String, Double> getCentroid(ArrayList<Integer> docs)
	{
		Hashtable<String, Double> centroid = new Hashtable<String, Double>();
		try
		{
			for(int doc : docs)
			{
				Hashtable<String, Double> termFreq = new Hashtable<String, Double>();
				termFreq = forwardIndex.get(doc);
				for(Map.Entry<String, Double> entry : termFreq.entrySet())
				{
					if(centroid.containsKey(entry.getKey()))
					{
						centroid.put(entry.getKey(), centroid.get(entry.getKey()) + entry.getValue());
					}
					else
					{
						centroid.put(entry.getKey(), entry.getValue());
					}
				}
			}
			for(Map.Entry<String, Double> entry : centroid.entrySet())
			{
				centroid.put(entry.getKey(), entry.getValue() / docs.size());
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return centroid;
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
				//System.out.println(doc);
				Hashtable<String, Double> innerMap = new Hashtable<String, Double>();
				String[] wordFreqs = content[1].split(",");
				for(String wordFreq : wordFreqs)
				{
					String[] text = wordFreq.split(" ");
					innerMap.put(text[0], Double.parseDouble(text[1]));
				}
				forwardIndex.put(doc, innerMap);
			}
			br.close();
			fr.close();
			long end = System.currentTimeMillis();
			System.out.println("loading forward index took " + (end - start) + " ms");
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
			for(Map.Entry<Integer, Hashtable<String, Double>> entry : forwardIndex.entrySet())
			{
				System.out.println(entry.getKey());
				pw.print(entry.getKey() + ";");
				for(Map.Entry<String, Double> innerEntry : entry.getValue().entrySet())
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
								Hashtable<String, Double> innerMap = forwardIndex.get(termDocs.doc());
								innerMap.put(termEnum.term().text(), Double.parseDouble(Integer.toString(termDocs.freq())));
								forwardIndex.put(termDocs.doc(), innerMap);
							}
							else
							{
								Hashtable<String, Double> innerMap = new Hashtable<String, Double>();
								innerMap.put(termEnum.term().text(), Double.parseDouble(Integer.toString(termDocs.freq())));
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
