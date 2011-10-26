package edu.asu.cse494;

import com.lucene.index.IndexReader;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

public class PageRank 
{
	TFIDFSimilarity sim = null;
	int corpusCount = 25053, topKDocs = 10;
	FileWriter fw = null;
	BufferedWriter bw = null;
	double c = 0.85, k = 1.0 / 25053.0, threshold = 0.00000000001, w = 0.85;
	double []pageRank = null;
	Hashtable<Integer, Double> rootSet = new Hashtable<Integer, Double>();
	
	public static void main(String[] args) 
	{
		PageRank pr = new PageRank();
		pr.startCalculation();
	}
	
	public PageRank()
	{
		//sim = new TFIDFSimilarity();
		//pageRank = getPageRank();		
	}
	
	private double[] getPageRank() 
	{
		try
		{
			double [] pageRank = new double[corpusCount];
			
			FileReader fr = new FileReader("pagerank.txt");
			BufferedReader br = new BufferedReader(fr);
			String line = "";	
			int count = 0;
			
			while((line = br.readLine()) != null)
			{
				if(line.length() != 0)
				{
					pageRank[count++] = Double.valueOf(line).doubleValue();
				}
			}
			return pageRank;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return null;
	}

	public void startCalculation()
	{
		try
		{
			//constructMatrix();
			//double[] r1 = powerIterate();
			//computePageRank();
			while(true)
			{
				getRootSet();
				computeResults();
			}
			/*FileReader fr = new FileReader("mt_new.txt");
			BufferedReader br = new BufferedReader(fr);
			String line = "";
			int count = 0;
			FileWriter fw = new FileWriter("bhaskar.txt");
			BufferedWriter bw = new BufferedWriter(fw);
			while((line = br.readLine()) != null)
			{
				
				bw.write(line + "\n");
				
				count++;
				if(count >= 3)
				{
					break;
				}
			}
			bw.close();
			fw.close();
			br.close();
			fr.close();*/
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public void getRootSet()
	{
		try
		{
			Hashtable<Integer, Double> similarity = sim.computeSimilarity();
			//System.out.println("how many documents from TF/IDF results do you want to fetch for AH computation?");
			//BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			//topKDocs = Integer.parseInt(in.readLine());
			rootSet = sim.getTopKResults(similarity, topKDocs);
			System.out.println("choose a value for w between 0 and 1");
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			w = Double.valueOf(in.readLine()).doubleValue();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public void computeResults()
	{
		try
		{
			IndexReader reader = IndexReader.open("result3index");
			
			Hashtable<Integer, Double> results = new Hashtable<Integer, Double>();
			for(int doc:rootSet.keySet())
			{
				results.put(doc, (w * pageRank[doc]) + ((1-w) * rootSet.get(doc)));
			}
			
			ArrayList myArrayList=new ArrayList(results.entrySet());
			Collections.sort(myArrayList, new MyComparator1());
			Iterator itr=myArrayList.iterator();
			int counter = 0;
			while(itr.hasNext())
			{
				Map.Entry<Integer, Double> e = (Map.Entry<Integer, Double>)itr.next();
				System.out.println((counter + 1) + ". docid = " + e.getKey() + "  " + reader.document(e.getKey()).get("url"));
				counter++;
			}
			reader.close();	
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public void computePageRank()
	{
		try
		{
			double[] pageRank = new double[corpusCount];
			FileReader fr = new FileReader("24.txt");
			BufferedReader br = new BufferedReader(fr);
			String line = "";
			int count = 0;
			double max = 0.0;
			while((line = br.readLine()) != null)
			{
				if(line.length() != 0)
				{
					pageRank[count] = Double.valueOf(line).doubleValue();
					if(pageRank[count] > max)
					{
						max = pageRank[count];
						System.out.println(count);
					}
					count++;
				}
			}
			br.close();
			fr.close();
			FileWriter fw = new FileWriter("pagerank.txt");
			BufferedWriter bw = new BufferedWriter(fw);
			for(int i = 0; i < pageRank.length; i++)
			{
				pageRank[i] /= max;
				bw.write(pageRank[i] + "\n");
			}
			
			bw.close();
			fw.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public double[] powerIterate()
	{
		double[] r0 = new double[corpusCount];
		double[] r1 = new double[corpusCount];
		double[] row = null;
		try
		{
			FileReader fr = null;
			BufferedReader br = null;
			
			for(int i = 0; i < corpusCount; i++)
			{
				r0[i] = k;
			}
			
			int iterationCounter = 0;
			for(;;)
			{
				r1 = new double[corpusCount];
				String line = "";
				fr = new FileReader("mt_new.txt");
				br = new BufferedReader(fr);
				
				int count = 0;
				while((line = br.readLine()) != null)
				{
					
					if(line.trim().length() != 0)
					{
						row = new double[corpusCount];
						String[] rowText = line.split(" ");
						for(int i = 0; i < corpusCount; i++)
						{
							row[i] = Double.valueOf(rowText[i]).doubleValue();
						}
						
						for(int i = 0; i < corpusCount; i++)
						{
							r1[count] += row[i] * r0[i];
						}
						System.out.println("r1[" + count + "] = " + r1[count] + " . iteration = " + iterationCounter);
						count++;	
					}
				}
				br.close();
				fr.close();
				if(computeDistance(r0, r1) <= threshold)
				{
					break;
				}
				r0 = r1;
				
				FileWriter fw1 = new FileWriter(iterationCounter + ".txt");
				BufferedWriter bw1 = new BufferedWriter(fw1);
				for(int k = 0; k < corpusCount; k++)
				{
					bw1.write(r1[k] + "\n");
				}
				bw1.close();
				fw1.close();
				iterationCounter++;
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return r1;
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
	
	public void constructMatrix()
	{
		try
		{
			fw = new FileWriter("mt_new.txt", true);
			bw = new BufferedWriter(fw);
			double[] row = null;
			int[] linkCount = new int[corpusCount];
			LinkAnalysis.numDocs = corpusCount;
			LinkAnalysis la = new LinkAnalysis();
			double sinkValue = (double)(1.0 / corpusCount);
			double resetValue = (double)(0.15 / corpusCount);
			
			int i = 0;
			for(int k = 0; k < corpusCount; k++)
			{
				linkCount[k] = la.getLinks(k).length;
 			}
			
			for(i = 0; i < corpusCount; i++)
			{
				long start = System.currentTimeMillis();
				System.out.println("starting calculation for row " + i);
				row = new double[corpusCount];
				int[] citations = la.getCitations(i);
				for(int citation:citations)
				{
					row[citation] = 1.0;
				}
				for(int j = 0; j < corpusCount; j++)
				{
					if(linkCount[j] == 0)
					{
						row[j] = sinkValue;
					}
					else
					{
						row[j] = 0.85 * row[j] / linkCount[j] + resetValue;
					}
				}
				
				saveToFile(row);
				long end = System.currentTimeMillis();
				System.out.println("ending calculation for row " + i + ". time taken = " + (end-start));
			}
			bw.close();
			fw.close();
			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	private void saveToFile(double[] column)
	{
		int i = 0;
		try
		{
			for(i = 0; i < column.length; i++)
			{
				bw.write(column[i] + " ");
				fw.flush();	bw.flush();
			}
			bw.write("\n");
		}
		catch(Exception ex)
		{
			System.out.println("crashed while writing to " + i + ".txt");
			ex.printStackTrace();
		}
	}
}
