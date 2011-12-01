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
import java.util.Map.Entry;

import org.omg.CORBA.DoubleSeqHolder;

public class RelevanceFeedback 
{
	Hashtable<Integer, Hashtable<String, Double>> forwardIndex = new Hashtable<Integer, Hashtable<String,Double>>();
	Hashtable<Integer, Hashtable<String, Double>> relevantDocs = new Hashtable<Integer, Hashtable<String,Double>>();
	Hashtable<Integer, Hashtable<String, Double>> irrelevantDocs = new Hashtable<Integer, Hashtable<String,Double>>();
	Hashtable<String, Double> idfWeights = new Hashtable<String, Double>();
	Hashtable<Integer, Double> docs = new Hashtable<Integer, Double>();
	TFIDFSimilarity sim = null;
	int topKDocs = 10;
	double alpha = 1.0, beta = 0.75, gamma = 0.25;
	
	public static void main(String[] args) 
	{
		RelevanceFeedback relevanceFeedback = new RelevanceFeedback();
		//relevanceFeedback.saveIDF();
		relevanceFeedback.loadIDF();
		relevanceFeedback.loadForwardIndex();
		relevanceFeedback.startCalculation();
	}
	
	public RelevanceFeedback()
	{
		sim = new TFIDFSimilarity();
	}
	
	private void startCalculation()
	{
		while(true)
		{
			getRootSet();
			separateDocs();
			constructNewQuery();
		}
	}
	
	private void constructNewQuery()
	{
		try
		{
			Hashtable<String, Double> newQuery = new Hashtable<String, Double>();
			Hashtable<String, Double> relevantCentroid = getCentroid(relevantDocs, beta);
			Hashtable<String, Double> irrelevantCentroid = getCentroid(irrelevantDocs, gamma);
			Hashtable<String, Double> oldQuery = sim.tokenizedQuery;
			
			for(Entry<String, Double> entry : relevantCentroid.entrySet())
			{
				if(newQuery.containsKey(entry.getKey()))
				{
					newQuery.put(entry.getKey(), newQuery.get(entry.getKey()) + entry.getValue());
				}
				else
				{
					newQuery.put(entry.getKey(), entry.getValue());
				}
			}
			for(Entry<String, Double> entry : irrelevantCentroid.entrySet())
			{
				if(newQuery.containsKey(entry.getKey()))
				{
					newQuery.put(entry.getKey(), newQuery.get(entry.getKey()) - entry.getValue());
				}
				else
				{
					newQuery.put(entry.getKey(), - entry.getValue());
				}
			}
			for(Entry<String, Double> entry : oldQuery.entrySet())
			{
				if(newQuery.containsKey(entry.getKey()))
				{
					newQuery.put(entry.getKey(), newQuery.get(entry.getKey()) + entry.getValue());
				}
				else
				{
					newQuery.put(entry.getKey(), entry.getValue());
				}
			}
			
			IndexReader reader = IndexReader.open("result3index");
			System.out.println("new query generated using rocchio feedback");
			System.out.println("recomputing similarity with the new query. may take 20-30 sec");
			long start = System.currentTimeMillis();
			Hashtable<Integer, Double> similarity = sim.computeSimilarity(newQuery);
			long end = System.currentTimeMillis();
			//System.out.println("recomputing similarity took " + (end - start) + "ms");
			System.out.println();
			Hashtable<Integer, Double> docs = sim.getTopKResults(similarity, topKDocs);
			ArrayList myArrayList=new ArrayList(docs.entrySet());
			Collections.sort(myArrayList, new MyComparator1());
			Iterator itr=myArrayList.iterator();
			int count = 0;
			while(itr.hasNext())
			{
				Map.Entry<Integer, Double> entry = (Map.Entry<Integer, Double>)itr.next();
				System.out.println((count + 1) + ". docid = " + entry.getKey() + "  title = " + reader.document(entry.getKey()).get("title"));
				System.out.println("   url = " + reader.document(entry.getKey()).get("url"));
				count++;
			}
			reader.close();
			System.out.println();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	private Hashtable<String, Double> getCentroid(Hashtable<Integer, Hashtable<String, Double>> docs, double constant)
	{
		Hashtable<String, Double> centroid = new Hashtable<String, Double>();
		try
		{
			for(Entry<Integer, Hashtable<String, Double>> doc : docs.entrySet())
			{
				for(Map.Entry<String, Double> entry : doc.getValue().entrySet())
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
				centroid.put(entry.getKey(), (entry.getValue() * constant) / docs.size());
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return centroid;
	}
	
	private void separateDocs()
	{
		try
		{
			ArrayList myArrayList = new ArrayList(docs.entrySet());
			Collections.sort(myArrayList, new MyComparator1());
			Iterator itr=myArrayList.iterator();
			int numRelevantDocs = (int) Math.ceil(topKDocs / 3);
			//irrelevantDocs = forwardIndex;
			while(itr.hasNext())
			{
				Map.Entry<Integer, Double> entry = (Map.Entry<Integer, Double>)itr.next();
				Hashtable<String, Double> termWeights = new Hashtable<String, Double>();
				for(Entry<String, Double> e : forwardIndex.get(entry.getKey()).entrySet())
				{
					termWeights.put(e.getKey(), e.getValue());
				}
				if(numRelevantDocs > 0)
				{
					relevantDocs.put(entry.getKey(), termWeights);
					numRelevantDocs--;
					//irrelevantDocs.remove(entry.getKey());
				}
				else
				{
					irrelevantDocs.put(entry.getKey(), termWeights);
					//break;
				}
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	private void getRootSet()
	{
		try
		{
			Hashtable<Integer, Double> similarity = sim.computeSimilarity();
			docs = sim.getTopKResults(similarity, topKDocs);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	private void loadIDF()
	{
		try
		{
			FileReader fr = new FileReader("idfweights.txt");
			BufferedReader br = new BufferedReader(fr);
			String line = "";
			idfWeights.clear();
			while((line = br.readLine()) != null)
			{
				String[] content = line.split(" ");
				idfWeights.put(content[0], Double.parseDouble(content[1]));
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	private void saveIDF()
	{
		try
		{
			idfWeights.clear();
			IndexReader reader = IndexReader.open("result3index");
			int corpusCount = reader.numDocs();
			TermEnum termEnum = reader.terms();
			while(termEnum.next())
			{
				if(termEnum.term().field().equals("contents"))
				{
					System.out.println(termEnum.term().text());
					TermDocs termDocs = reader.termDocs(termEnum.term());
					int count = 0;
					while(termDocs.next())
					{
						count++;
					}
					idfWeights.put(termEnum.term().text(), Math.log((double)(corpusCount / count)));
				}
			}
			reader.close();
			
			FileWriter fw = new FileWriter("idfweights.txt");
			PrintWriter pw = new PrintWriter(fw);
			for(Entry<String, Double> entry : idfWeights.entrySet())
			{
				pw.println(entry.getKey() + " " + entry.getValue());
			}
			pw.close();
			System.out.println("done");
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	private Hashtable<Integer, Hashtable<String, Double>> loadForwardIndex()
	{
		try
		{
			System.out.println("loading forward index. may take 5-10 secs");
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
					innerMap.put(text[0], Double.parseDouble(text[1]) * idfWeights.get(text[0]));
				}
				forwardIndex.put(doc, innerMap);
			}
			br.close();
			fr.close();
			long end = System.currentTimeMillis();
			//System.out.println("loading forward index took " + (end - start) + " ms");
			System.out.println("top k/3 (k=10 in his case) docs would be automatically considered relevant, rest of the docs are irrelevant");
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return forwardIndex;
	}
}
