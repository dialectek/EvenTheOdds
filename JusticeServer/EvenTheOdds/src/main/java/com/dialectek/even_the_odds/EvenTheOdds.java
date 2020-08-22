// Even (the odds) justice server.

package com.dialectek.even_the_odds;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;

public class EvenTheOdds
{
   public static final String appDirectory = "even_the_odds";
   
   // Constructor.
   public EvenTheOdds()
   {
	   File dir = new File(appDirectory);
	   if (!dir.exists())
	   {
		   dir.mkdir();
	   }
   }

   // Get cases.
   public synchronized ArrayList<String> getCases()
   {
 	  File dir = new File(appDirectory);
 	  File[] filesList = dir.listFiles();
 	  ArrayList<String> caseNames = new ArrayList<String>();
 	  for (File file : filesList) 
 	  {
 	     if (file.isFile()) {
 	    	 String fileName = file.getName();
 	    	 int i = fileName.lastIndexOf('.');
 	    	 if (i > 0) {
 	    	     String extension = fileName.substring(i+1);
 	    	     String base = fileName.substring(0, i);
 	    	     if (extension.equals("zip"))
 	    	     {
 	    	    	 caseNames.add(base);
 	    	     }
 	    	 }    	    	 

 	     }
 	  }	  
      return(caseNames);
   }
   
   // Get case file name.
   public synchronized String getCaseFileName(String caseName)
   {
	  return appDirectory + "/" + caseName + ".zip";
   }
   
   // New case.
   public synchronized boolean newCase(InputStream fileStream, String caseName)
   {   
	  String caseFileName = getCaseFileName(caseName); 	  
	  File caseFile = new File(caseFileName);  
	  if (fileStream != null && !caseFile.exists())
	  {
		  try 
		  {
	          Reader reader = new InputStreamReader(fileStream);
	          Writer writer = new OutputStreamWriter(new FileOutputStream(caseFile));
	          for (int ch = reader.read(); ch != -1; ch = reader.read()) 
	          {
	              writer.write(ch);
	          }
	          writer.flush();
	          writer.close();
		  } catch (Exception e)
		  {
			  return false;
		  }		  
		  return true;
	  } else { 
		  return false;
	  }
   }
}
