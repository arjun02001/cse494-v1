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
	TFIDFSimilarity sim = null;
	double[] norm;
	int topKDocs = 50, clusterSize = 3;
	
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
		norm = sim.norm;
	}
	
	private void startCalculation()
	{
		getRootSet();
		System.out.println(documentDocumentSimilarity(0, 0));
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
	
	private double documentDocumentSimilarity(int doc1, int doc2)
	{
		double sim = 0.0;
		try
		{
			Hashtable<String, Double> termFreq1 = forwardIndex.get(doc1);
			Hashtable<String, Double> termFreq2 = forwardIndex.get(doc2);
			sim = (termFreq1.size() < termFreq2.size()) ? documentDocumentDotProduct(termFreq1, termFreq2) : documentDocumentDotProduct(termFreq2, termFreq1);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return sim / (norm[doc1] * norm[doc2]);
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
