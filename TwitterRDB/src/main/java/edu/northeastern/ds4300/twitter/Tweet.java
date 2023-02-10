package edu.northeastern.ds4300.twitter;


import java.sql.Timestamp;

public class Tweet {
  private int userId;
  private String tweetText;
  private Timestamp time;

  public Tweet(int userId, String tweetText) {
    this.userId = userId;
    this.tweetText = tweetText;
  }

  public Tweet(int user_id, String tweet_text, Timestamp tweets_ts) {
    this.userId = user_id;
    this.tweetText = tweet_text;
    this.time = tweets_ts;
  }

  @Override
  public String toString() {
    return "Tweet{" +
            " userID='" + userId + '\'' +
            ", text='" + tweetText + '\'' +
            ", time=" + time + '\'' +
            '}';
  }

  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  public String getTweetText() {
    return tweetText;
  }

  public void setTweetText(String tweetText) {
    this.tweetText = tweetText;
  }

  public Timestamp getTime() {
    return time;
  }

  public void setTime(Timestamp time) {
    this.time = time;
  }
}
