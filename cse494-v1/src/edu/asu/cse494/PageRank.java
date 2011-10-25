package edu.asu.cse494;

import com.lucene.index.IndexReader;

public class PageRank 
{
	TFIDFSimilarity sim = null;
	int corpusCount = 25053;
	double c = 0.85, k = 1 / 25053;
	
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
		try
		{
			double[] column = null;
			LinkAnalysis.numDocs = corpusCount;
			LinkAnalysis la = new LinkAnalysis();
			for(int i = 0; i < corpusCount; i++)
			{
				column = new double[corpusCount];
				int links[] = la.getLinks(0);
				for(int link:links)
				{
					column[link] = 1;
				}
				if(links.length == 0)
				{
					for(int j = 0; j < corpusCount; j++)
					{
						column[j] = 1 / corpusCount;
					}
				}
				else
				{
					for(int j = 0; j < corpusCount; j++)
					{
						column[j] /= links.length;
						column[j] = c * column[j] + (1 - c) * k;
					}
				}
				saveToFile(column);
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	private void saveToFile(double[] column)
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
