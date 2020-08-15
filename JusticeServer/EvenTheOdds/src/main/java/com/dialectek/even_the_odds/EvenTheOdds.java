// Even (the odds) justice server.

package com.dialectek.even_the_odds;

import java.io.File;
import java.util.ArrayList;

import javax.ws.rs.core.Response;

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
   public synchronized boolean newCase(File uploadFile, String caseName)
   {   
	  String caseFileName = getCaseFileName(caseName); 	  
	  File caseFile = new File(caseFileName);
	  if (uploadFile.isFile() && !caseFile.exists() && uploadFile.renameTo(caseFile))
	  {
		  return true;
	  } else { 
		  return false;
	  }
   }
}
