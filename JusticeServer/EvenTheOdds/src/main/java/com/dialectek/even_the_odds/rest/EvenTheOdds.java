// Even (the odds) justice server REST interface.

package com.dialectek.even_the_odds.rest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

@Path("/service")
public class EvenTheOdds
{
   // Even (the odds).
   public static com.dialectek.even_the_odds.EvenTheOdds evenApp;

   // Synchronization.
   private static Object lock;

   // Initialize.
   static
   {
      evenApp  = new com.dialectek.even_the_odds.EvenTheOdds();
      lock = new Object();
   }

   // Get cases.
   @GET
   @Path("/get_cases")
   @Produces(MediaType.TEXT_PLAIN)
   public Response get_cases()
   {
      synchronized (lock)
      {
    	 String output = "[";
    	 ArrayList<String> caseNames = evenApp.getCases();
    	 for (int i = 0, j = caseNames.size(); i < j; i++) 
    	 {
	         output += caseNames.get(i);
	         if (i < j - 1)
	         {
    			 output += ",";    	        	 
	         }
    	 }    	 
    	 output += "]";
         return(Response.status(200).entity(output).build());
      }
   }
   
   // Get case.
   @GET
   @Path("/get_case/{case_name}")
   @Produces(MediaType.APPLICATION_OCTET_STREAM)
   public Response get_case(@PathParam ("case_name") String caseName) throws Exception
   {
	   synchronized (lock)
	   {
	       String caseFileName = evenApp.getCaseFileName(caseName);
	       File caseFile = new File(caseFileName);
		   if (!caseFile.exists()) 
		   {
		      throw new WebApplicationException(404);
		   }	
	       StreamingOutput fileStream =  new StreamingOutput() 
	       {
	           @Override
	           public void write(java.io.OutputStream output) throws IOException, WebApplicationException 
	           {
	               try
	               {
	                   java.nio.file.Path path = Paths.get(caseFileName);
	                   byte[] data = Files.readAllBytes(path);	                   
	                   output.write(data);
	                   output.flush();
	               } 
	               catch (Exception e) 
	               {
	                   throw new WebApplicationException(404);
	               }
	           }
	       };
	       return Response
	               .ok(fileStream, MediaType.APPLICATION_OCTET_STREAM)
	               .header("content-disposition","attachment; filename=" + new File(caseFileName).getName())
	               .build();
	   }
   }

   // Get case file.
   @GET
   @Path("/get_case_file/{case_name}")
   @Produces("application/zip")
   public Response get_case_file(@PathParam ("case_name") String caseName) throws Exception
   {
      synchronized (lock)
      {
	      String caseFileName = evenApp.getCaseFileName(caseName);
    	  File caseFile = new File(caseFileName);
		  if (!caseFile.exists()) 
		  {
		      throw new WebApplicationException(404);
		  }	
		  return Response.ok(caseFile).header("Content-Disposition",
		      "attachment; filename=" + new File(caseFileName).getName()).build();
      }
   }
   
   @POST   
   @Path("/new_case/{case_name}")
   @Consumes(MediaType.MULTIPART_FORM_DATA)
   @Produces(MediaType.TEXT_PLAIN)
   public Response new_case(File uploadFile, @PathParam ("case_name") String caseName)
   {
      synchronized (lock)
      {
    	  if (evenApp.newCase(uploadFile, caseName))
    	  {
			  return(Response.status(200).build());
		  } else { 
			  return(Response.status(400).build());
		  }
      }
   }   
}
