package edu.northeastern.ds4300.twitter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.TimeUnit;


/**
 * Class that holds the main function to post tweets and get timelines from a connected database.
 */
public class Twitter {
  /**
   * To connect to a SQL database.
   */
//  private static DatabaseAPI api = new DatabaseMySQL();
  /**
   * To connect to a redis database (option #2 - required).
   */
  private static DatabaseAPI api = new DatabaseRedis();

  /**
   * To connect to a redis database that doesn't track user's timelines (option #1)
   */
//  private static DatabaseAPI api = new DatabaseRedisOptional();

  /**
   * Main function that read csv files and adds entries to a connected Database Server (redis or sql).
   */
  public static void main(String[] args) throws Exception {

//    String url = "jdbc:mysql://localhost:3306/twitter?";
//    String user = System.getenv("TWITTER_USER");
//    String password = System.getenv("TWITTER_PASSWORD");
    String dataFile = "tweet.csv";
    String people = "follows.csv";
//
//    api.authenticate(url, user, password);
    /**
     * Connects to the local database.
     */
    api.authenticate();


    /**
     * Read every entry from the csv file, and adds the data into redis using the following method.
     */
    try {
      String line = "";
      BufferedReader lr = new BufferedReader(new FileReader(people));

      lr.readLine();
      while ((line = lr.readLine()) != null) {
        String[] follows = line.split(",");
        String user_id = follows[0];
        String follows_id = follows[1];
        api.following (user_id, follows_id);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }


  /**
     * Creates and inserts tweet objects from the csv file as they are being read.
     * Keep track of start time, number of operations, end time, and the number of operations/second
     */
    try {
      String line = "";
      BufferedReader lineReader = new BufferedReader(new FileReader(dataFile));

      lineReader.readLine();
      int count = 0;
      long start = System.currentTimeMillis();
      while ((line = lineReader.readLine()) != null) {
        String[] tweet = line.split(",");
        Tweet t = new Tweet(Integer.parseInt(tweet[0]), tweet[1]);
        api.insertTweet(t);
        count += 1;
      }
      long end = System.currentTimeMillis();
      lineReader.close();
      long elapsed = (end - start) / 1000;
      System.out.println("Total time to execute " + count + " queries is "
      + elapsed + " seconds.");
    }
    catch (IOException e) {
      e.printStackTrace();
    }


    /**
     * Gets random user_ids from the followers table and generate home timelines for the users.
     * Keeps track of the number of home timelines returned in one second.
     */
    int count = 0;

    long start = System.nanoTime();
    long end = start + TimeUnit.SECONDS.toNanos(1);
    while (System.nanoTime() < end) {
      int randomUser = api.getRandomUser();
      System.out.println(String.valueOf(randomUser) + api.getTimeline(randomUser));
      count += 1;
    }
    System.out.println(count);

    /**
     * Ends the program and closes the connection the database.
     */
    api.closeConnection();
  }
}
