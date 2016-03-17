package Wrapper;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonObject;

import Misc.GlobalHelper;
import Utility.JsonFileReader;

public class Tweet {
	private String profileId; // eg. U421, U422 (the given ID)
	private String text;
	private JsonObject tweetJson;
	
	private List<String> hashtags;
	private int retweets;
	private String timezone;
	private int friendsCount; // The number of users this account is following (AKA their ¡°followings¡±)
	private int followersCount; // The number of followers this account currently has. 
	private int favouritesCount; // The number of tweets this user has favorited in the account¡¯s lifetime.
	private int userId; // id of the person posting this tweet
	
	public Tweet(String _profileId, JsonObject _tweetJson) {
		profileId = _profileId;
		tweetJson = _tweetJson;
		hashtags = new ArrayList<String>();
		this.extractFeaturesFromJson();
	}
	
	private void extractFeaturesFromJson() {
		//get the origin tweet text from the tweet JsonObject
		if(tweetJson.containsKey("text")){
			String rawText = (tweetJson.getString("text"));
			text = rawText.replaceAll("(?:(?:\\r\\n)|(?:\\r)|(?:\\n))+", " | ");
			text =  text.replaceAll("\\t+", " ");
			
			// Ignore URL, replace with blank character
			String urlRegex = "(?:http[s]?:\\/\\/t\\.co\\/[A-Za-z0-9]+)";
			text =  text.replaceAll(urlRegex, "");
			
			urlRegex = "(?:http[s]?:\\/\\/t\\.co)";
			text =  text.replaceAll(urlRegex, "");
		}
		else{
			System.err.println("no text key in: " + profileId + " json!");
			return;
		}
		
		// get the hashtags information from the tweet JsonObject
		JsonArray hashtagsArray = tweetJson.getJsonObject("entities").getJsonArray("hashtags");
		for (int i = 0; i < hashtagsArray.size(); i++) {
			hashtags.add(hashtagsArray.getJsonObject(i).getString("text"));
		}
		
		//get the retweets information
		retweets = tweetJson.getInt("retweet_count");
		
		//get the timezone information, can be null
		if (!tweetJson.getJsonObject("user").isNull("time_zone")) {
			timezone = tweetJson.getJsonObject("user").getString("time_zone");			
		} else {
			timezone = "null";
		}
			
		// get friendscount
		friendsCount = tweetJson.getJsonObject("user").getInt("friends_count");
		
		// get followersscount
		followersCount = tweetJson.getJsonObject("user").getInt("followers_count");
		
		// get favouritescount
		favouritesCount = tweetJson.getJsonObject("user").getInt("favourites_count");
		
		// get userId
		userId = tweetJson.getJsonObject("user").getInt("id");
	}
	
	public String getText() { return text; }
	public int getRetweets() {  return retweets; }
	public String getTimezone() {  return timezone; }
	public int getFriendsCount() { return friendsCount; }
	public int getFollowersCount() { return followersCount; }
	public int getFavouritesCount() { return favouritesCount; }
	public int getUserId() { return userId; }
	
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(profileId + "\t");
		sb.append("\""+text+"\"");
		return sb.toString();
	}
}