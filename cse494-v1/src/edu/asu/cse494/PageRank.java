package edu.asu.cse494;

import com.lucene.index.IndexReader;
import java.io.*;

public class PageRank 
{
	TFIDFSimilarity sim = null;
	int corpusCount = 25053;
	FileWriter fw = null;
	BufferedWriter bw = null;
	double c = 0.85, k = 1.0 / 25053.0;
	
	public static void main(String[] args) 
	{
		PageRank pr = new PageRank();
		pr.startCalculation();
	}
	
	public PageRank()
	{
		//sim = new TFIDFSimilarity();
	}
	
	public void startCalculation()
	{
		try
		{
			computePageRank();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public void computePageRank()
	{
		int i = 0;
		try
		{
			fw = new FileWriter("mt.txt", true);
			bw = new BufferedWriter(fw);
			double[] column = null;
			LinkAnalysis.numDocs = corpusCount;
			LinkAnalysis la = new LinkAnalysis();
			for(i = 0; i < corpusCount; i++)
			{
				long start = System.currentTimeMillis();
				System.out.println("starting calculation for column " + i);
				column = new double[corpusCount];
				int links[] = la.getLinks(i);
				for(int link:links)
				{
					column[link] = 1;
				}
				if(links.length == 0)
				{
					for(int j = 0; j < corpusCount; j++)
					{
						column[j] = (double)(1.0 / corpusCount);
					}
				}
				else
				{
					for(int j = 0; j < corpusCount; j++)
					{
						column[j] /= (double)links.length;
						column[j] = c * column[j] + (1 - c) * k;
					}
				}
				long end = System.currentTimeMillis();
				System.out.println("ending calculation for column " + i + ". time taken = " + (end-start));
				saveToFile(column);
			}
			bw.close();
			fw.close();
		}
		catch(Exception ex)
		{
			System.out.println("crashed with column = " + i);
			ex.printStackTrace();
		}
	}
	
	private void saveToFile(double[] column)
	{
		int i = 0;
		try
		{
			long start = System.currentTimeMillis();
			
			System.out.println("starting to save to file");
			bw.write("\n");
			for(i = 0; i < column.length; i++)
			{
				bw.write(column[i] + " ");
				fw.flush();	bw.flush();
			}
			
			long end = System.currentTimeMillis();
			System.out.println("saving " + i + " values to file completed. time taken = " + (end-start));
		}
		catch(Exception ex)
		{
			System.out.println("crashed while writing to " + i + ".txt");
			ex.printStackTrace();
		}
	}
}
