// Even (the odds) justice server REST interface.

package com.dialectek.even_the_odds.rest;

import java.util.ArrayList;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
    	 ArrayList<String> cases = evenApp.get_cases();
    	 for (int i = 0, j = cases.size(); i < j; i++)
    	 {
    		 output += cases.get(i);
    		 if (i < j - 1)
    		 {
    			 output += ", ";
    		 }
    	 }
    	 output += "]";
         return(Response.status(200).entity(output).build());
      }
   }
}
