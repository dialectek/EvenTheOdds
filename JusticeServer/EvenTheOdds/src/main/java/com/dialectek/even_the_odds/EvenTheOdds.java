// Even (the odds) justice server.

package com.dialectek.even_the_odds;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Base64;

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

   // Get directory names.
   public synchronized ArrayList<String> getDirNames()
   {
 	  File dir = new File(appDirectory);
 	  File[] filesList = dir.listFiles();
 	  ArrayList<String> dirNames = new ArrayList<String>();
 	  for (File file : filesList) 
 	  {
 	     if (file.isDirectory()) 
 	     {
 	    	 dirNames.add(file.getName()); 
 	     }
 	  }	  
      return(dirNames);
   }
   
   // Get file path name.
   public synchronized String getPathName(String fileName)
   {
	  String[] tokens = fileName.split("\\.(?=[^\\.]+$)");
	  if (tokens.length == 2)
	  {
		  return appDirectory + "/" + tokens[0] + "/" + fileName;
	  } else {
		  return appDirectory + "/" + fileName + "/" + fileName;		  
	  }
   }
   
   // Make parent directory.
   public synchronized boolean mkParentDir(String fileName)
   {
	  String[] tokens = fileName.split("\\.(?=[^\\.]+$)");
	  File dir;
	  if (tokens.length == 2)
	  {
		  dir = new File(appDirectory + "/" + tokens[0]);
	  } else {
		  dir = new File(appDirectory + "/" + fileName);		  
	  }
	  if (!dir.exists())
	  {
		  return dir.mkdir();
	  } else {	  
		  return true;
	  }
   } 
   
   // New case.
   public synchronized boolean putFile(String fileName, InputStream fileStream)
   {
	  mkParentDir(fileName);
	  String pathName = getPathName(fileName);	  
	  File file = new File(pathName);  
	  if (fileStream != null)
	  {
		  try 
		  {
	          Reader reader = new InputStreamReader(fileStream);
              ArrayList<Byte> zipByteList = new ArrayList<Byte>();
              for (int ch = reader.read(); ch != -1; ch = reader.read()) {
                 zipByteList.add((byte)ch);
              }
              byte[] zipBytes = new byte[zipByteList.size()];
              for (int i = 0, j = zipBytes.length; i < j; i++) {
                 zipBytes[i] = zipByteList.get(i);
              }
              byte[] zipBytesDecoded = Base64.getDecoder().decode(zipBytes);
              FileOutputStream outputStream = new FileOutputStream(file);
              outputStream.write(zipBytesDecoded);
              outputStream.close();	          
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
