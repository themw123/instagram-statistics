import java.io.IOException;
import java.util.List;
import java.util.Vector;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.brunocvcunha.instagram4j.Instagram4j;
import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Instagram {

	private String username;
	private String password;
	private String sessionId;
	private String ds_user_id;
	
	private String[] following;
	private String[] followers;
	
	private Vector<String> notFollowingYou;
	private Vector<String> youFollowingNot;
	private Vector<String> OpenFriendRequestOut;
	private Vector<String> OpenFriendRequestIn;
	private Vector<String> myPosts;
	
	public Instagram(String username, String password, String sessionId, String ds_user_id) {
		this.username = username;
		this.password = password;
		this.sessionId = sessionId;
		this.ds_user_id = ds_user_id;
		setFollowingAndFollowers("following");
		setFollowingAndFollowers("followers");
		setNotFollowingYou();
		setYouFollowingNot();
		setOpenFriendRequestOut();
		setOpenFriendRequestIn();
		setMyPosts();
	}
	
	public Instagram(String username, String password) {
		this.username = username;
		this.password = password;
		setSession();
		setFollowingAndFollowers("following");
		setFollowingAndFollowers("followers");
		setNotFollowingYou();
		setYouFollowingNot();
		setOpenFriendRequestOut();
		setOpenFriendRequestIn();
		setMyPosts();
	}
	
	private void setSession() {
		//session id mit Instagram4j holen
		String[] session = null;
		try {
		Instagram4j instagram = Instagram4j.builder().username(username).password(password).build();
		instagram.setup();
		instagram.login();
		
		CookieStore cookies = instagram.getCookieStore();
		List<Cookie> cookieList = cookies.getCookies();
		String cookieS1 = cookieList.get(4).toString();
		String cookieS2 = cookieS1.substring(cookieS1.indexOf("value:")+7, cookieS1.length());
		String sessionId = cookieS2.substring(0, cookieS2.indexOf("]"));
		
		cookieS1 = cookieList.get(1).toString();
		cookieS2 = cookieS1.substring(cookieS1.indexOf("value:")+7, cookieS1.length());
		String ds_user_id = cookieS2.substring(0, cookieS2.indexOf("]"));
		
		this.sessionId = sessionId;
		this.ds_user_id = ds_user_id;
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}
	
	private void setFollowingAndFollowers(String urlParameter) {
		
		OkHttpClient client = new OkHttpClient().newBuilder()
				  .build();
				Request request = new Request.Builder()
				  .url("https://i.instagram.com/api/v1/friendships/"+ ds_user_id + "/" + urlParameter + "/")
				  .method("GET", null)
				  .addHeader("X-IG-App-ID", "936619743392459")
				  .addHeader("Cookie", "sessionid=" + sessionId)
				  .build();
				try {
					Response response = client.newCall(request).execute();
					
					String output = response.body().string();
					JSONObject jsonObj = new JSONObject(output);
					
					JSONArray ja_users = jsonObj.getJSONArray("users");
					int length = ja_users.length();
					
					String[] user = new String[length];
					for(int i=0;i<length;i++) {
						JSONObject userJson = ja_users.getJSONObject(i);
						String username = userJson.getString("username");
						user[i] = username;
					}
					
					if(urlParameter.equals("following")) {
						this.following = user;
					}
					else if(urlParameter.equals("followers")) {
						this.followers = user;
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}	
	}
	
	private void setNotFollowingYou() {
		notFollowingYou = new Vector<String>();

		boolean drin = false;
		for(String foing : following) {
			for(String foers : followers) {
				if(foing.equals(foers)) {
					drin = true;
					break;
				}
			}
			if(!drin) {
				notFollowingYou.add(foing);
			}
			drin = false;
		}
	    
	}
	
	private void setYouFollowingNot() {
		youFollowingNot = new Vector<String>();

		boolean drin = false;
		for(String foers : followers) {
			for(String foing : following) {
				if(foing.equals(foers)) {
					drin = true;
					break;
				}
			}
			if(!drin) {
				youFollowingNot.add(foers);
			}
			drin = false;
		}
	    
	}
	
	private void setOpenFriendRequestOut() {
		
		OpenFriendRequestOut = new Vector<String>();
		String cursor = null;
		do {
			String url = null;
			if(cursor == null) {
				url = "https://www.instagram.com/accounts/access_tool/current_follow_requests?__a=1";
			}
			else {
				url = "https://www.instagram.com/accounts/access_tool/current_follow_requests?__a=1&cursor=" + cursor;
			}
			OkHttpClient client = new OkHttpClient().newBuilder()
					  .build();
					Request request = new Request.Builder()
					  .url(url)
					  .method("GET", null)
					  .addHeader("X-IG-App-ID", "936619743392459")
					  .addHeader("Cookie", "sessionid=" + sessionId)
					  .build();
					try {
						Response response = client.newCall(request).execute();
						String output = response.body().string();
						JSONObject jsonObj = new JSONObject(output);
						jsonObj = jsonObj.getJSONObject("data");
						
						String cursor1 = jsonObj.toString();
						String cursor2 = cursor1.substring(cursor1.indexOf("cursor")+9, cursor1.length());
						cursor = cursor2.substring(0, cursor2.indexOf(",")-1);
						if(cursor.equals("ul")) {
							cursor = null;
						}
						
						JSONArray jsonArr = jsonObj.getJSONArray("data");
						int length = jsonArr.length();
						for(int i=0;i<length;i++) {
							JSONObject userJson = jsonArr.getJSONObject(i);
							String username = userJson.getString("text");
							OpenFriendRequestOut.add(username);
						}
						
						
					} catch (IOException e) {
						e.printStackTrace();
					}
		}while(cursor != null);
		
	}
	
	private void setOpenFriendRequestIn() {
		
		OpenFriendRequestIn = new Vector<String>();
		OkHttpClient client = new OkHttpClient().newBuilder()
				  .build();
				Request request = new Request.Builder()
				  .url("https://i.instagram.com/api/v1/friendships/pending/")
				  .method("GET", null)
				  .addHeader("X-IG-App-ID", "936619743392459")
				  .addHeader("Cookie", "sessionid=" + sessionId)
				  .build();
				try {
					Response response = client.newCall(request).execute();
					String output = response.body().string();
					JSONObject jsonObj = new JSONObject(output);
					JSONArray jsonArr = jsonObj.getJSONArray("users");
					
					int length = jsonArr.length();
					for(int i=0;i<length;i++) {
						JSONObject userJson = jsonArr.getJSONObject(i);
						String username = userJson.getString("username");
						OpenFriendRequestIn.add(username);
					}
					
				} catch (IOException e) {
					e.printStackTrace();
				}
				
	}
	
	private void setMyPosts() {
		
		myPosts = new Vector<String>();
		int count = 1000;
		String has_next_page = "true";
		String end_cursor = null;
		
		OkHttpClient client = new OkHttpClient().newBuilder()
				  .build();
				Request request = new Request.Builder()
				  .url("https://www.instagram.com/" + username + "/?__a=1")
				  .method("GET", null)
				  .addHeader("X-IG-App-ID", "936619743392459")
				  .addHeader("Cookie", "sessionid=" + sessionId)
				  .build();
				try {
					Response response = client.newCall(request).execute();
					
					String output = response.body().string();
					JSONObject jsonObj = new JSONObject(output);
					jsonObj = jsonObj.getJSONObject("graphql").getJSONObject("user").getJSONObject("edge_owner_to_timeline_media");
					
					
					String countStr = jsonObj.toString();
					countStr = countStr.substring(countStr.indexOf("count")+7, countStr.length());
					countStr = countStr.substring(0, countStr.indexOf(","));
					count = Integer.parseInt(countStr);
					

					String has_next_pageStr = jsonObj.getJSONObject("page_info").toString();
					has_next_pageStr = has_next_pageStr.substring(has_next_pageStr.indexOf("has_next_page")+15, has_next_pageStr.length());
					has_next_page = has_next_pageStr.substring(0, has_next_pageStr.indexOf(","));
					
					
					if(has_next_page.equals("true")) {
						String end_cursorStr = jsonObj.getJSONObject("page_info").toString();
						end_cursorStr = end_cursorStr.substring(end_cursorStr.indexOf("end_cursor")+13, end_cursorStr.length());
						end_cursor = end_cursorStr.substring(0, end_cursorStr.indexOf("\""));
					}
					
					JSONArray jsonArr = jsonObj.getJSONArray("edges");
					int length = jsonArr.length();
					for(int i=0;i<length;i++) {
						JSONObject post = jsonArr.getJSONObject(i).getJSONObject("node");
						String shortcode = post.getString("shortcode");
						myPosts.add(shortcode);
					}
	
				} catch (IOException e) {
					e.printStackTrace();
				}
				
		if(has_next_page.equals("true")) {	
			
			/*
			query_id:
			17851374694183129 = posts for tags
			17874545323001329 = user following
			17851374694183129 = user followers
			17888483320059182 = user posts
			17864450716183058 = likes on posts
			17852405266163336 = comments on posts
			17842794232208280 =	posts on feed
			17847560125201451 = feed profile suggestions
			17863787143139595 = post suggestions
			*/
			
			OkHttpClient client2 = new OkHttpClient().newBuilder()
							.build();
					Request request2 = new Request.Builder()
							.url("https://www.instagram.com/graphql/query/?query_id=17888483320059182&variables={\"id\":\""+ ds_user_id + "\",\"first\":"+ count +",\"after\":\"" + end_cursor + "\"}")
							.method("GET", null)
							.addHeader("X-IG-App-ID", "936619743392459")
							.addHeader("Cookie", "sessionid=" + sessionId)
							.build();
					try {
						Response response2 = client2.newCall(request2).execute();
						
						String output = response2.body().string();
						JSONObject jsonObj = new JSONObject(output);
						jsonObj = jsonObj.getJSONObject("data").getJSONObject("user").getJSONObject("edge_owner_to_timeline_media");
						
						JSONArray jsonArr = jsonObj.getJSONArray("edges");
						int length = jsonArr.length();
						for(int i=0;i<length;i++) {
							JSONObject post = jsonArr.getJSONObject(i).getJSONObject("node");
							String shortcode = post.getString("shortcode");
							myPosts.add(shortcode);
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		}
	}
	
	
}
