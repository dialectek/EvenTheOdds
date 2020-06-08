// Even (the odds) justice server REST client.

package com.dialectek.even_the_odds.rest.client;

import javax.ws.rs.core.MediaType;

import org.apache.commons.httpclient.util.URIUtil;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

public class EvenTheOddsClient
{
   public static String REST_ADDRESS = "http://localhost:8080/EvenTheOdds/rest/service/";
   //public static String REST_ADDRESS = "http://ec2-52-11-225-180.us-west-2.compute.amazonaws.com:8080/EvenTheOdds/rest/service/";

   public static void main(String[] args)
   {
      if (args.length == 1)
      {
         REST_ADDRESS = args[0];
      }

      try
      {
         ClientConfig clientConfig = new DefaultClientConfig();
         Client       client       = Client.create(clientConfig);

         // Add some users.
         String user1 = URIUtil.encodeQuery("User 1");
         add_user(user1, client);
         String user2 = "User2";
         add_user(user2, client);
         String user3 = "User3";
         add_user(user3, client);
         get_users(client);

         // Add some resources.
         String resource1 = "Resource1";
         String resource2 = "Resource2";

         // Rate resources.
         add_rating(user1, resource1, 1.0f, client);
         add_rating(user2, resource1, 2.0f, client);
         add_rating(user2, resource2, 2.0f, client);
         add_rating(user3, resource2, 3.0f, client);

         // Recommend new friends.
         recommend_friends(user1, 1, client);

         // Befriend user.
         add_friend(user1, user2, client);

         // Recommend new resources.
         recommend_resources(user1, 3, client);

         // Validate.
         get_user(user1, client);
         get_user(user2, client);
         get_user(user3, client);
      }
      catch (Exception e) {
         e.printStackTrace();
      }
   }


   public static void add_user(String user_name, Client client)
   {
      try
      {
         WebResource webResource =
            client.resource(REST_ADDRESS + "add_user/" + user_name);
         webResource.accept(MediaType.TEXT_PLAIN).get(String.class );
      }
      catch (Exception e) {
         e.printStackTrace();
      }
   }


   public static void delete_user(String user_name, Client client)
   {
      try
      {
         WebResource webResource =
            client.resource(REST_ADDRESS + "delete_user/" + user_name);
         webResource.accept(MediaType.TEXT_PLAIN).get(String.class );
      }
      catch (Exception e) {
         e.printStackTrace();
      }
   }


   public static void get_user(String user_name, Client client)
   {
      try
      {
         WebResource webResource =
            client.resource(REST_ADDRESS + "get_user/" + user_name);
         System.out.println(webResource.accept(MediaType.TEXT_PLAIN).get(String.class ));
      }
      catch (Exception e) {
         e.printStackTrace();
      }
   }


   public static void get_users(Client client)
   {
      try
      {
         WebResource webResource =
            client.resource(REST_ADDRESS + "get_users");
         System.out.println(webResource.accept(MediaType.TEXT_PLAIN).get(String.class ));
      }
      catch (Exception e) {
         e.printStackTrace();
      }
   }


   // Recommend new friends.
   public static void recommend_friends(String user_name, int maxFriends, Client client)
   {
      try
      {
         WebResource webResource =
            client.resource(REST_ADDRESS + "recommend_friends/" + user_name + "/" + maxFriends);
         System.out.println(webResource.accept(MediaType.TEXT_PLAIN).get(String.class ));
      }
      catch (Exception e) {
         e.printStackTrace();
      }
   }


   public static void recommend_resources(String user_name, int maxResources, Client client)
   {
      try
      {
         WebResource webResource =
            client.resource(REST_ADDRESS + "recommend_resources/" + user_name + "/" + maxResources);
         System.out.println(webResource.accept(MediaType.TEXT_PLAIN).get(String.class ));
      }
      catch (Exception e) {
         e.printStackTrace();
      }
   }


   public static void get_friends(String user_name, Client client)
   {
      try
      {
         WebResource webResource =
            client.resource(REST_ADDRESS + "get_friends/" + user_name);
         System.out.println(webResource.accept(MediaType.TEXT_PLAIN).get(String.class ));
      }
      catch (Exception e) {
         e.printStackTrace();
      }
   }


   public static void add_friend(String user_name, String friend_name, Client client)
   {
      try
      {
         WebResource webResource =
            client.resource(REST_ADDRESS + "add_friend/" + user_name + "/" + friend_name);
         webResource.accept(MediaType.TEXT_PLAIN).get(String.class );
      }
      catch (Exception e) {
         e.printStackTrace();
      }
   }


   public static void delete_friend(String user_name, String friend_name, Client client)
   {
      try
      {
         WebResource webResource =
            client.resource(REST_ADDRESS + "delete_friend/" + user_name + "/" + friend_name);
         webResource.accept(MediaType.TEXT_PLAIN).get(String.class );
      }
      catch (Exception e) {
         e.printStackTrace();
      }
   }


   public static void clear_friends(Client client)
   {
      try
      {
         WebResource webResource =
            client.resource(REST_ADDRESS + "clear_friends");
         webResource.accept(MediaType.TEXT_PLAIN).get(String.class );
      }
      catch (Exception e) {
         e.printStackTrace();
      }
   }


   public static void add_rating(String user_name, String resource_name, float rating, Client client)
   {
      try
      {
         WebResource webResource =
            client.resource(REST_ADDRESS + "add_rating/" + user_name + "/" + resource_name + "/" + rating);
         webResource.accept(MediaType.TEXT_PLAIN).get(String.class );
      }
      catch (Exception e) {
         e.printStackTrace();
      }
   }
}
