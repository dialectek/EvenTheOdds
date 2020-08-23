// Even (the odds) justice server REST interface.

package com.dialectek.even_the_odds.rest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

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

   // Get files.
   @GET
   @Path("/get_files")
   @Produces(MediaType.TEXT_PLAIN)
   public Response get_files()
   {
      synchronized (lock)
      {
    	 String output = "[";
    	 ArrayList<String> fileNames = evenApp.getFileNames();
    	 for (int i = 0, j = fileNames.size(); i < j; i++) 
    	 {
	         output += fileNames.get(i);
	         if (i < j - 1)
	         {
    			 output += ",";    	        	 
	         }
    	 }    	 
    	 output += "]";
         return(Response.status(200).entity(output).build());
      }
   }
   
   // Get file.
   @GET
   @Path("/get_file/{file_name}")
   @Produces(MediaType.APPLICATION_OCTET_STREAM)
   public Response get_file(@PathParam ("file_name") String fileName) throws WebApplicationException 
   {
	   synchronized (lock)
	   {
	       String pathName = evenApp.getPathName(fileName);
	       File file = new File(pathName);
		   if (file.exists()) 
		   {	
		       StreamingOutput fileStream =  new StreamingOutput() 
		       {
		           @Override
		           public void write(java.io.OutputStream output) throws IOException, WebApplicationException 
		           {
		               try
		               {
		                   java.nio.file.Path path = Paths.get(pathName);
		                   byte[] data = Files.readAllBytes(path);
		                   output.write(data);
		                   output.flush();
		               } 
		               catch (Exception e) 
		               {
		                   throw new WebApplicationException(400);
		               }
		           }
		       };
		       return Response
		               .ok(fileStream, MediaType.APPLICATION_OCTET_STREAM)
		               .header("content-disposition","attachment; filename=" + fileName)
		               .build();
		   } else {
			  return(Response.status(404).build()); 
		   }		   
	   }
   }
   
   @POST   
   @Path("/put_file")
   @Consumes(MediaType.MULTIPART_FORM_DATA)
   @Produces(MediaType.TEXT_PLAIN)   
   public Response put_file(MultipartFormDataInput input) throws WebApplicationException
   {
       synchronized (lock)
       {	   
		   String fileName = null;
		   Map<String, List<InputPart>> formParts = input.getFormDataMap();
		   List<InputPart> inPart = formParts.get("file_name"); 
		   for (InputPart inputPart : inPart) 
		   {
		       MultivaluedMap<String, String> headers = inputPart.getHeaders();
		       String[] contentDispositionHeader = headers.getFirst("Content-Disposition").split(";");
		       for (String name : contentDispositionHeader) 
		       {
		         if ((name.trim().startsWith("filename"))) 
		         {
		           String[] tmp = name.split("=");
		           fileName = tmp[1].trim().replaceAll("\"","");
		           break;
		         }
		       }
		   }
		   if (fileName != null)
		   {
		       try
		       {
		          InputStream fileStream = input.getFormDataPart("file_name", InputStream.class, null);	
		     	  if (evenApp.putFile(fileName, fileStream))
		     	  {
		 			  return(Response.status(200).build());
		 		  } else { 
		 			  return(Response.status(400).build());
		 		  }
		       } 
		       catch (Exception e) 
		       {
		           throw new WebApplicationException(400);
		       } 
		   } else {
	           throw new WebApplicationException(400);		   
		   }
   		}
   }
}
