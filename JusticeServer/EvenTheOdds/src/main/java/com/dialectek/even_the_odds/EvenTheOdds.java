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

   // Get file names.
   public synchronized ArrayList<String> getFileNames()
   {
 	  File dir = new File(appDirectory);
 	  File[] filesList = dir.listFiles();
 	  ArrayList<String> fileNames = new ArrayList<String>();
 	  for (File file : filesList) 
 	  {
 	     if (file.isFile()) 
 	     {
 	    	 fileNames.add(file.getName()); 
 	     }
 	  }	  
      return(fileNames);
   }
   
   // Get file path name.
   public synchronized String getPathName(String fileName)
   {
	  return appDirectory + "/" + fileName;
   }
   
   // New case.
   public synchronized boolean putFile(String fileName, InputStream fileStream)
   {   
	  String pathName = getPathName(fileName); 	  
	  File file = new File(pathName);  
	  if (fileStream != null)
	  {
		  try 
		  {
	          Reader reader = new InputStreamReader(fileStream);
	          Writer writer = new OutputStreamWriter(new FileOutputStream(file));
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
