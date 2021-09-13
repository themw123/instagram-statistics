import java.util.Arrays;
import java.util.Comparator;
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
	private int likes = 0;
	private int comments = 0;
	
	private Object[][] mostLikedByFollowers;
	private Object[][] mostCommentedByFollowers;
	
	
	
	
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
		setMostLikedByFollowers();
		setMostCommentedByFollowers();
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
		setMostLikedByFollowers();
		setMostCommentedByFollowers();
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
		
		} catch (Exception e) {
			System.out.println("Login fehlgeschlagen");
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
					System.out.println("setFollowingAndFollowers fehlgeschlagen");
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
						
						
						cursor = jsonObj.get("cursor").toString();
						if(cursor.equals("null")) {
							cursor = null;
						}
						
						JSONArray jsonArr = jsonObj.getJSONArray("data");
						int length = jsonArr.length();
						for(int i=0;i<length;i++) {
							JSONObject userJson = jsonArr.getJSONObject(i);
							String username = userJson.getString("text");
							OpenFriendRequestOut.add(username);
						}
						
						
					} catch (Exception e) {
						System.out.println("setOpenFriendRequestOut fehlgeschlagen");
						e.printStackTrace();
						break;
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
					
				} catch (Exception e) {
					System.out.println("setOpenFriendRequestIn fehlgeschlagen");
					e.printStackTrace();
				}
				
	}
	
	private void setMyPosts() {
		
		int max = 1; //Bei, ersten mal holt er 12 und dann immer Faktor 40.
		
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
		
		myPosts = new Vector<String>();
		int count = 1000;
		String has_next_page = "false";
		String end_cursor = null;
		
		
		int durchlauf = 0;
		int sumPosts = 0;
		
		
		do {
			
		String url = "";	
		if(has_next_page.equals("false")) {
			url = "https://www.instagram.com/" + username + "/?__a=1";
		}	
		else if (has_next_page.equals("true")) {
			url = "https://www.instagram.com/graphql/query/?query_id=17888483320059182&variables={\"id\":\""+ ds_user_id + "\",\"first\":"+ count +",\"after\":\"" + end_cursor + "\"}";
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
					
					if(has_next_page.equals("false")) {
						jsonObj = jsonObj.getJSONObject("graphql").getJSONObject("user").getJSONObject("edge_owner_to_timeline_media");
					}
					else {
						jsonObj = jsonObj.getJSONObject("data").getJSONObject("user").getJSONObject("edge_owner_to_timeline_media");
					}
					
					has_next_page = jsonObj.getJSONObject("page_info").get("has_next_page").toString();
					
					
					if(has_next_page.equals("true")) {
						end_cursor = jsonObj.getJSONObject("page_info").get("end_cursor").toString();
					}
					
					JSONArray jsonArr = jsonObj.getJSONArray("edges");
					int length = jsonArr.length();
					for(int i=0;i<length;i++) {
						JSONObject post = jsonArr.getJSONObject(i).getJSONObject("node");
						
						String shortcode = post.getString("shortcode");
						myPosts.add(shortcode);
					}
					sumPosts = sumPosts + length;
				} catch (Exception e) {
					System.out.println("setMyPosts" + durchlauf + " fehlgeschlagen");
					e.printStackTrace();
					break;
				}
				
				durchlauf++;
				
		}while(has_next_page.equals("true") && durchlauf < max);		
				
			
	}
	
	private void setMostLikedByFollowers() {
		
		int max = 20; //20 durchläufe entsprechen 1000 Likes die betrachtet werden, Faktor 50
		
		int length = followers.length;
		int counter = 0;
		
		mostLikedByFollowers = new Object[length][2];
		
		for(String name : followers) {
			mostLikedByFollowers[counter][0] = name;
			mostLikedByFollowers[counter][1] = 0;
			counter++;
		}
		
		
		
		
		String has_next_page = "false";
		String end_cursor = null;
		int count = 5000000;
		int durchlauf = 0;
		int sumLikes = 0;
		
		schleife:
		for(String post : myPosts) {
			durchlauf = 0;
			sumLikes = 0;
			
			do {
				
				String url = "";	
				if(has_next_page.equals("false")) {
					url = "https://www.instagram.com/graphql/query/?query_id=17864450716183058&variables={\"shortcode\":\"" + post + "\",\"include_reel\":true,\"first\":" + count + "}";
				}	
				else if (has_next_page.equals("true")) {
					url = "https://www.instagram.com/graphql/query/?query_id=17864450716183058&variables={\"shortcode\":\""+ post + "\",\"include_reel\":true,\"first\":" + count + ",\"after\":\"" + end_cursor + "\"}";
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
							jsonObj = jsonObj.getJSONObject("data").getJSONObject("shortcode_media").getJSONObject("edge_liked_by");
							
							if(durchlauf == 0) {
								likes = likes + Integer.parseInt(jsonObj.get("count").toString());
							}
							
							has_next_page = jsonObj.getJSONObject("page_info").get("has_next_page").toString();
							
							
							if(has_next_page.equals("true")) {
								end_cursor = jsonObj.getJSONObject("page_info").get("end_cursor").toString();
							}
							
							JSONArray jsonArr = jsonObj.getJSONArray("edges");
							int len = jsonArr.length();
							for(int i=0;i<len;i++) {
								
								JSONObject liker = jsonArr.getJSONObject(i).getJSONObject("node");
								String username = liker.getString("username");
								
								for(int k=0;k<mostLikedByFollowers.length;k++) {
									if(mostLikedByFollowers[k][0].equals(username)) {
										int likes = ((int) mostLikedByFollowers[k][1])+1;
										mostLikedByFollowers[k][1] = likes;
									}
								}
								
							}
							
							sumLikes = sumLikes + len;
							
						} catch (Exception e) {
							System.out.println("setMostLikedByFollowers" + durchlauf + " fehlgeschlagen");
							e.printStackTrace();
							break schleife;
						}
						
						durchlauf++;
					
			}while(has_next_page.equals("true") && durchlauf < max);	
			
		}
		
		Arrays.sort(mostLikedByFollowers, new Comparator<Object[]>() {
			@Override
			public int compare(Object[] o1, Object[] o2) {
		            Integer quantityOne = (Integer) o1[1];
			    Integer quantityTwo = (Integer) o2[1];
			   
			    return quantityTwo.compareTo(quantityOne);

			}
		});
		
		Object[][] mostLikedByFollowers2 = mostLikedByFollowers;
		
		counter = 0;
		for(Object[] follower : mostLikedByFollowers2) {
			if((int) follower[1] != 0) {
				counter++;
			}
			else {
				break;
			}
		}
		
		mostLikedByFollowers = new Object [counter][2];
		counter = 0;
		for(Object[] follower : mostLikedByFollowers2) {
			if((int) follower[1] != 0) {
				mostLikedByFollowers[counter][0] = follower[0];
				mostLikedByFollowers[counter][1] = follower[1];
				counter++;
			}
			else {
				break;
			}
		}
		
	}
	
	
	private void setMostCommentedByFollowers() {
		
		int max = 2; //2 durchläufe entsprechen 100 Kommentare die betrachtet werden, Faktor 50
		
		int length = followers.length;
		int counter = 0;
		
		mostCommentedByFollowers = new Object[length][2];
		
		for(String name : followers) {
			mostCommentedByFollowers[counter][0] = name;
			mostCommentedByFollowers[counter][1] = 0;
			counter++;
		}
		
		String has_next_page = "false";
		String end_cursor = null;
		int count = 5000000;
		int durchlauf = 0;
		int sumComments = 0;
		
		schleife:
		for(String post : myPosts) {
			durchlauf = 0;
			sumComments = 0;
			
			do {
				
				String url = "";	
				if(has_next_page.equals("false")) {
					url = "https://www.instagram.com/graphql/query/?query_hash=2efa04f61586458cef44441f474eee7c&variables={\"shortcode\":\"" + post + "\",\"parent_comment_count\":" + count + ",\"has_threaded_comments\":true}";
				}	
				else if (has_next_page.equals("true")) {
					url = "https://www.instagram.com/graphql/query/?query_hash=bc3296d1ce80a24b1b6e40b1e72903f5&variables={\"shortcode\":\"" + post + "\",\"first\":" + count + ",\"after\":\"" + end_cursor + "\"}";
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
							jsonObj = jsonObj.getJSONObject("data").getJSONObject("shortcode_media").getJSONObject("edge_media_to_parent_comment");
							
							if(durchlauf == 0) {
								comments = comments + Integer.parseInt(jsonObj.get("count").toString());
							}
							
							has_next_page = jsonObj.getJSONObject("page_info").get("has_next_page").toString();
							
							if(has_next_page.equals("true")) {
								end_cursor = jsonObj.getJSONObject("page_info").get("end_cursor").toString();
							}
							
							JSONArray jsonArr = jsonObj.getJSONArray("edges");
							int len = jsonArr.length();
							for(int i=0;i<len;i++) {
								JSONObject commenter = jsonArr.getJSONObject(i).getJSONObject("node").getJSONObject("owner");
								String username = commenter.getString("username");
								for(int k=0;k<mostCommentedByFollowers.length;k++) {
									if(mostCommentedByFollowers[k][0].equals(username)) {
										int likes = ((int) mostCommentedByFollowers[k][1])+1;
										mostCommentedByFollowers[k][1] = likes;
									}
								}
									
							}
							sumComments = sumComments + len;
						} catch (Exception e) {
							System.out.println("setMostCommentedByFollowers" + durchlauf + "fehlgeschlagen");
							e.printStackTrace();
							break schleife;
						}
						durchlauf++;
						
			}while(has_next_page.equals("true") && durchlauf < max);			
				
		}
		
		Arrays.sort(mostCommentedByFollowers, new Comparator<Object[]>() {
			@Override
			public int compare(Object[] o1, Object[] o2) {
		            Integer quantityOne = (Integer) o1[1];
			    Integer quantityTwo = (Integer) o2[1];
			   
			    return quantityTwo.compareTo(quantityOne);

			}
		});
		
		
		Object[][] mostCommentedByFollowers2 = mostCommentedByFollowers;
		
		counter = 0;
		for(Object[] follower : mostCommentedByFollowers2) {
			if((int) follower[1] != 0) {
				counter++;
			}
			else {
				break;
			}
		}
		
		mostCommentedByFollowers = new Object [counter][2];
		counter = 0;
		for(Object[] follower : mostCommentedByFollowers2) {
			if((int) follower[1] != 0) {
				mostCommentedByFollowers[counter][0] = follower[0];
				mostCommentedByFollowers[counter][1] = follower[1];
				counter++;
			}
			else {
				break;
			}
		}
	}
	
	
}
