// Even (the odds) justice server.

package com.dialectek.even_the_odds;

import java.util.ArrayList;

public class EvenTheOdds
{
   // Constructor.
   public EvenTheOdds()
   {
   }

   // Get cases.
   public synchronized ArrayList<String> get_cases()
   {
	  ArrayList<String> cases = new ArrayList<String>();
	  cases.add("Case 1");
	  cases.add("Case 2");
	  cases.add("Case 3");
      return(cases);
   }
}
