package edu.northeastern.ds4300.twitter;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;;

/**
 * Implementation of a Redis API that extends DatabaseRedis, but doesn't add tweets to a timeline
 * when inserting.
 */
public class DatabaseRedisOptional extends DatabaseRedis{

  /**
   * Creates a tweet as a hashmap with the user id, time (current time found in Java), and text.
   * Inserts created tweet into a sorted set of the user who tweeted it with the weight being the
   * time in milliseconds.
   * @param tweet the tweet read from the csv to be inserted into the data.
   */
  @Override
  public void insertTweet(Tweet tweet) {
    if (!jedis.exists("tweet_id")) {
      jedis.set("tweet-id", "0");
    }
    tweet_key = jedis.incr("tweet_id");
    String tweet_id = "Tweet:" + tweet_key;
    LocalDateTime now = LocalDateTime.now();
    DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    String time = now.format(format);
    String user_key = "user_tweets:" + tweet.getUserId();

    jedis.hset(tweet_id, "user_id", String.valueOf(tweet.getUserId()));
    jedis.hset(tweet_id, "tweet_text", tweet.getTweetText());
    jedis.hset(tweet_id, "tweet_ts", time);

    float timeMilli = System.currentTimeMillis();

    jedis.zadd(user_key, timeMilli, tweet_id);

  }

  /**
   * Goes through everyone who this user is following and combines all the tweets posted by following
   * members through union, and then returns the last ten sorted by time.
   * @param user_id current user whose timeline we are returning
   * @return
   */
  @Override
  public List<Tweet> getTimeline(int user_id) {
    String user_key = "following:" + user_id;
    List<String> following = jedis.lrange(user_key, 1, -1);
    String last_entry = "user_tweets:" + jedis.lindex(user_key, 0);
    String all_tweet = "all_tweets:" + user_id;
    for (String member: following){
      String followers_id = "user_tweets:" + member;
      if (jedis.exists(all_tweet)) {
        jedis.zunionstore(all_tweet, "2", all_tweet, followers_id);
      } else {
        jedis.zunionstore(all_tweet, "2", last_entry, followers_id);
      }
    }
    List<String> tweets = jedis.zrevrange(all_tweet, 0, 9);
    List<Tweet> timeline = new ArrayList<>();
    Timestamp timestamp = null;

    for (String t : tweets) {
      int user = Integer.parseInt(jedis.hget(t, "user_id"));
      String text = jedis.hget(t, "tweet_text");
      String time = jedis.hget(t, "tweet_ts");
      try {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date parsedDate = dateFormat.parse(time);
        timestamp = new java.sql.Timestamp(parsedDate.getTime());
      } catch (Exception e) {
        System.out.println(e);
      }
      timeline.add(new Tweet(user, text, timestamp));
    }

    return timeline;
  }
}
