package edu.northeastern.ds4300.twitter;

import java.util.List;

public interface DatabaseAPI {
  /**
   * Inserts a Tweet object into the tweet table in SQL - tweet_id and time are automatically generated.
   * @param t tweet object to be inserted
   */
  public void insertTweet(Tweet t);

  /**
   * Picks a random user from the user table.
   * @return the user_id of a random user.
   */
  public int getRandomUser();

  /**
   * Generates ten most recent tweets from users that the given user_id is following.
   * @param user_id current user
   * @return timeline of tweets.
   */
  public List<Tweet> getTimeline(int user_id);

  /**
   * Validates the inputs and connects to a database.
   * @param url the host url of database
   * @param user the username to connect to database.
   * @param password the password to connect to database.
   */
  public void authenticate(String url, String user, String password);

  /**
   * Ends the connection to the database server.
   */
  public void closeConnection();

  /**
   * Connects to a databse, with no given inputs.
   */
  void authenticate();

  /**
   * Adds the user, following relationship to the database.
   * @param user_id the given user who is following
   * @param follows_id the id of the person being followed.
   */
  void following(String user_id, String follows_id);
}
