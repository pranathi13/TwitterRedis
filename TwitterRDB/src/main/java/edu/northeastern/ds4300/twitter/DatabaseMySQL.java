package edu.northeastern.ds4300.twitter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import edu.northeastern.database.DBUtils;

public class DatabaseMySQL implements DatabaseAPI{

  DBUtils dbu;
  @Override
  public void insertTweet(Tweet t) {
    String sql = "INSERT INTO tweet (user_id, tweet_text) VALUES (?, ?)";
    try {
      Connection con = dbu.getConnection();
      PreparedStatement pstmt = con.prepareStatement(sql);

      pstmt.setInt(1, t.getUserId());
      pstmt.setString(2, t.getTweetText());
      pstmt.execute();
      pstmt.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

  }

  @Override
  public int getRandomUser() {
    String sql = "select user_id from follows order by rand() limit 1";
    int user = 0;
    try {
      Connection con = dbu.getConnection();
      Statement stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(sql);
      while(rs.next() != false)
        user = rs.getInt("user_id");
      rs.close();
      stmt.close();
    } catch (SQLException e) {
      System.err.println(e.getMessage());
      e.printStackTrace();
    }
    return user;
  }

  @Override
  public List<Tweet> getTimeline(int user) {
    String sql = "select tweet.user_id, tweet_text, tweets_ts "+
            "from follows inner join tweet on follows.follows_id = tweet.user_id " +
            "where follows.user_id = "+ user +
            " ORDER by tweets_ts desc limit 10";

    List<Tweet> tweets = new ArrayList<Tweet>();


    try {
      // get connection and initialize statement
      Connection con = dbu.getConnection();
      Statement stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(sql);
      while (rs.next() != false)
        tweets.add(new Tweet(rs.getInt("user_id"), rs.getString("tweet_text"),
                rs.getTimestamp("tweets_ts")));
      rs.close();
      stmt.close();
    } catch (SQLException e) {
      System.err.println(e.getMessage());
      e.printStackTrace();
    }

    return tweets;
  }


  @Override
  public void authenticate(String url, String user, String password) {
    dbu = new edu.northeastern.database.DBUtils(url, user, password);

  }

  @Override
  public void closeConnection() {
    dbu.closeConnection();

  }

  @Override
  public void authenticate() {

  }

  @Override
  public void following(String user_id, String follows_id) {

  }
}
