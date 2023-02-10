package edu.northeastern.ds4300.twitter;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import redis.clients.jedis.Jedis;

/**
 * An implementation of the interface DatabaseAPI connecting to the redis server.
 */
public class DatabaseRedis implements  DatabaseAPI{

  Jedis jedis;
  Long tweet_key;

  /**
   * Sets up the following relationships among users in a list, as well as creating a set of all
   * unique user_ids. Here the following list represents "who is the given user_id following" and
   * the followers list represents "what users follow the given user_id."
   * @param user the id of the user
   * @param follower the id of the person the user is following
   */
  public void following (String user, String follower){
    jedis.sadd("user_list", user);
    jedis.sadd("user_list", follower);
    jedis.smembers("user_list");
    String following_id = "following:" + user;
    String followers_id = "followers:" + follower;
    jedis.lpush(following_id, follower);
    jedis.lpush(followers_id, user);
  }

  /**
   * Creates a tweet as a hashmap with the user id, time (current time found in Java), and text.
   * Inserts created tweet into a list of the user who tweeted it.
   * Adds each tweet into the timelines of every user who follows the user_id that posted the tweet.
   * Tweet_id is incremented.
   * @param tweet the tweet read from the csv to be inserted into the data.
   */
  @Override
  public void insertTweet(Tweet tweet) {
    if (!jedis.exists("tweet_id")) {
      jedis.set("tweet_id", "0");
    }

    tweet_key = jedis.incr("tweet_id");
    String tweet_id = "Tweet:" + tweet_key;
    LocalDateTime now = LocalDateTime.now();
    DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    String time = now.format(format);
    String user_key = "user_tweets:" + tweet.getUserId();

    jedis.hset(tweet_id, "user_id", String.valueOf(tweet.getUserId()));
    jedis.hset(tweet_id, "tweet_text", tweet.getTweetText());
    jedis.hset(tweet_id, "tweet_ts", time);

    jedis.lpush(user_key, tweet_id);

    String follow_id = "followers:" + tweet.getUserId();

    List<String> followers = jedis.lrange(follow_id, 0, -1);

    for (String f : followers){
      String timeline_id = "Timeline:" + f;
      jedis.lpush(timeline_id, tweet_id);

    }

  }


  /**
   * Gets a random instance os a user_id stored in the redis database.
   * @return user_id of the user whose timeline we will retrieve.
   */
  @Override
  public int getRandomUser() {
    return Integer.parseInt(jedis.srandmember("user_list"));
  }

  /**
   * Gets last ten tweets in the user's timeline and find the hash values for the associated tweet.
   * @param user_id current user whose timeline we are returning
   * @return a list of 10 tweets representing the user's timeline
   */
  @Override
  public List<Tweet> getTimeline(int user_id) {
    Timestamp timestamp = null;
    List<Tweet> tweets = new ArrayList<>();
    String timeline_id = "Timeline:" + user_id;
    List<String> timeline = jedis.lrange(timeline_id, 0, 9);
    for (String t : timeline) {
      int user = Integer.parseInt(jedis.hget(t, "user_id"));
      String text = jedis.hget(t, "tweet_text");
      String time = jedis.hget(t, "tweet_ts");
      try {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date parsedDate = dateFormat.parse(time);
        timestamp = new java.sql.Timestamp(parsedDate.getTime());
      } catch (Exception e) {
        System.out.println(e);
      }
      tweets.add(new Tweet(user, text, timestamp));
    }
    return tweets;
  }


  @Override
  public void authenticate(String url, String user, String password) {
  }


  /**
   * Closes the redis connection.
   */
  @Override
  public void closeConnection() {
    jedis.close();

  }

  /**
   * Connects to the Redis local host.
   */
  @Override
  public void authenticate() {
    jedis = new Jedis();
    jedis.flushAll();
  }
}
