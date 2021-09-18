import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.brunocvcunha.instagram4j.Instagram4j;
import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.Response;

public class Instagram{
	private Object LogData = new Object();

	
	private String username;
	private String password;
	private String sessionId;
	private boolean sessionIdValid;
	private String ds_user_id;
	private Vector<String> dataPoolLog;

	/*
	time = -System.currentTimeMillis();
	System.out.println((time + System.currentTimeMillis())/1000 + " Sekunden");
	*/
	
	private APIRequest r;
	
	private String[] following;
	private String[] followers;
	private Vector<String> notFollowingYou;
	private Vector<String> youFollowingNot;
	private Vector<String> mutual = new Vector<String>();
	private Vector<String> OpenFriendRequestOut;
	private Vector<String> OpenFriendRequestIn;
	private Vector<String> myPosts;
	private int postLikeNumber = 0;
	private int postCommentNumber = 0;
	private int likes = 0;
	private int comments = 0;
	
	private Object[][] mostLikedByFollowers;
	private Object[][] mostCommentedByFollowers;
	
	private String[] ghostedLikeByFollowers;
	private String[] ghostedCommentByFollowers;
	
	
	public Instagram(String username, String sessionId, String ds_user_id) {
		this.username = username;
		this.sessionIdValid = false;
		this.sessionId = sessionId;
		this.ds_user_id = ds_user_id;
	}
	
	public Instagram(String username, String password) {
		this.username = username;
		this.password = password;
		this.sessionIdValid = false;
	}
	
	
	public void login() {
		//(String username, String sessionId, String ds_user_id)
		if(sessionId != null && ds_user_id != null) {
			this.r = new APIRequest(sessionId);
			//check if sessionId still works
			sessionIdValid = r.checkSessionId("https://www.instagram.com/" + username + "/?__a=1");
		}
		
		//(String username, String password)
		else {
			setSession();
			if(sessionId != null && ds_user_id != null) {
				this.r = new APIRequest(sessionId);
				sessionIdValid = r.checkSessionId("https://www.instagram.com/" + username + "/?__a=1");
			}
		}
		System.out.println("Login-Thread finished");
		startThread("LogData");
	}
	

	
    public void data(){
		
    	System.out.println("Data1-Thread running");
		Thread t1 = new Thread(() -> setFollowingAndFollowers("following"));
		t1.start();
		
		
    	System.out.println("Data2-Thread running");
		Thread t2 = new Thread(() -> setFollowingAndFollowers("followers"));
		t2.start();
		
		//Auf following/follower warten. Bei fail, nur Thread 5,6 und 7 starten
		try {
			t1.join();
			t2.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		Thread t3 = null;
		Thread t4 = null;
		if(following != null && followers != null) {
	    	System.out.println("Data3-Thread running");
			t3 = new Thread(() -> setNotFollowingYou());
			t3.start();
			
	    	System.out.println("Data4-Thread running");
			t4 = new Thread(() -> setYouFollowingNot());
			t4.start();
		}
		
		
    	System.out.println("Data5-Thread running");
		Thread t5 = new Thread(() -> setOpenFriendRequestOut());
		t5.start();
		
    	System.out.println("Data6-Thread running");
		Thread t6 = new Thread(() -> setOpenFriendRequestIn());
		t6.start();
		
		
		
		
    	System.out.println("Data7-Thread running");
		Thread t7 = new Thread(() -> setMyPosts());
		t7.start();
		
		//Auf Posts warten
		try {
			t7.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		
		
		Thread t8 = null;
		Thread t9 = null;
		if(following != null && followers != null && myPosts != null) {
			System.out.println("\n!!!!!!!!HEAVY!!!!!!!!");
		    System.out.println("Data8-Thread running");
			t8 = new Thread(() -> setMostLikedOrCommentedByFollowers("liker"));
			t8.start();
			
			
		    System.out.println("Data9-Thread running");
			t9 = new Thread(() -> setMostLikedOrCommentedByFollowers("commenter"));
			t9.start();
			
		}
		
		
		
		
		
		
		
		
		//Auf übrige Threads warten
    	try {
    		if(following != null && followers != null) {
				t3.join();
				t4.join();
    		}
			t5.join();
			t6.join();
    		if(following != null && followers != null && myPosts != null) {
				t8.join();
				t9.join();
		        setDataPoolLog();
				System.out.println("!!!!!!!!HEAVY!!!!!!!!\n");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	
       //Main Thread starten
		System.out.println("Data-Thread finished");
		startThread("LogData");
	}
	
	
	
	
	private void setSession() {
		//session id mit Instagram4j holen
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
			sessionId = null;
			ds_user_id = null;
			//e.printStackTrace();
		}
	
	}
	
	private void setFollowingAndFollowers(String urlParameter) {
		
		int count = 1000000000;
		String error = null;
		
		String url = "https://i.instagram.com/api/v1/friendships/"+ ds_user_id + "/" + urlParameter + "/?count=" + count + "";
		
		Response response = r.doRequest(url);
		
		try {
			String output = response.body().string();
			JSONObject jsonObj = new JSONObject(output);
			error = jsonObj.toString();
			
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
			System.out.println("setFollowingAndFollowers failed -> " + error);
			if(urlParameter.equals("following")) {
				following = null;
			}
			else if(urlParameter.equals("followers")) {
				followers = null;
			}
			//e.printStackTrace();
		}
		

		if(urlParameter.equals("following")) {
			System.out.println("Data1-Thread finished");
		}
		else if(urlParameter.equals("followers")) {
			System.out.println("Data2-Thread finished");
		}
	}
	
	
	private void setNotFollowingYou() {
				
		notFollowingYou = new Vector<String>();

		boolean drin = false;
		for(String foing : following) {
			for(String foers : followers) {
				if(foing.equals(foers)) {
					drin = true;
					mutual.add(foing);
					break;
				}
			}
			if(!drin) {
				notFollowingYou.add(foing);
			}
			drin = false;
		}
		
		System.out.println("Data3-Thread finished");

	    
	}
	
	private void setYouFollowingNot() {
		youFollowingNot = new Vector<String>();

		boolean drin = false;
		for(String foers : followers) {
			for(String foing : following) {
				if(foing.equals(foers)) {
					drin = true;
					if(!mutual.contains(foers)) {
					mutual.add(foers);
					}
					break;
				}
			}
			if(!drin) {
				youFollowingNot.add(foers);
			}
			drin = false;
		}
	    
		System.out.println("Data4-Thread finished");

		
	}
	
	
	
	private void setOpenFriendRequestOut() {
		
		OpenFriendRequestOut = new Vector<String>();
		String cursor = null;
		String error = null;
		int durchlauf = 0;
		
		do {
			String url = null;
			if(cursor == null) {
				url = "https://www.instagram.com/accounts/access_tool/current_follow_requests?__a=1";
			}
			else {
				url = "https://www.instagram.com/accounts/access_tool/current_follow_requests?__a=1&cursor=" + cursor;
			}
			
			Response response = r.doRequest(url);
			
			try {
				String output = response.body().string();
				JSONObject jsonObj = new JSONObject(output);
				error = jsonObj.toString();	
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
				System.out.println("setOpenFriendRequestOut" + " Durchlauf: " + durchlauf + "failed -> " + error);
				OpenFriendRequestOut = null;
				//e.printStackTrace();
				break;
			}
			durchlauf++;
		}while(cursor != null);
		
		System.out.println("Data5-Thread finished");

	}
	
	private void setOpenFriendRequestIn() {
		
		OpenFriendRequestIn = new Vector<String>();
		
		String url = "https://i.instagram.com/api/v1/friendships/pending/";

		Response response = r.doRequest(url);

		String error = null;
		
		try {
			String output = response.body().string();
			JSONObject jsonObj = new JSONObject(output);
			error = jsonObj.toString();
			JSONArray jsonArr = jsonObj.getJSONArray("users");
	
			
			int length = jsonArr.length();
			for(int i=0;i<length;i++) {
				JSONObject userJson = jsonArr.getJSONObject(i);
				String username = userJson.getString("username");
				OpenFriendRequestIn.add(username);
			}
					
		} catch (Exception e) {
			System.out.println("setOpenFriendRequestIn failed -> " + error);
			OpenFriendRequestIn = null;
			//e.printStackTrace();
			
		}
			
		System.out.println("Data6-Thread finished");

	}
	
	
	
	private void setMyPosts() {
		
		int max = 13; //Bei, ersten mal holt er 12 und dann immer Faktor 40.
		
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
		String error = null;
		
		int durchlauf = 0;
		
		
		do {
			
			String url = "";	
			if(has_next_page.equals("false")) {
				url = "https://www.instagram.com/" + username + "/?__a=1";
			}	
			else if (has_next_page.equals("true")) {
				url = "https://www.instagram.com/graphql/query/?query_id=17888483320059182&variables={\"id\":\""+ ds_user_id + "\",\"first\":"+ count +",\"after\":\"" + end_cursor + "\"}";
			}
			
			Response response = r.doRequest(url);
			
			try {			
				String output = response.body().string();
				JSONObject jsonObj = new JSONObject(output);
				error = jsonObj.toString();
				
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
			} catch (Exception e) {
				System.out.println("setMyPosts Post: " + durchlauf + " failed -> " + error);
				myPosts = null;
				//e.printStackTrace();
				break;
			}
					
			durchlauf++;
				
		}while(has_next_page.equals("true") && durchlauf < max);		
				
		System.out.println("Data7-Thread finished");

	}
	

	
	private void setMostLikedOrCommentedByFollowers(String likerOrCommenter) {
				
		dataPoolLog = new Vector<String>();

		
		int length = followers.length;
		int counter = 0;
		
		if(likerOrCommenter.equals("liker")) {
			mostLikedByFollowers = new Object[length][2];
			for(String name : followers) {
				mostLikedByFollowers[counter][0] = name;
				mostLikedByFollowers[counter][1] = 0;
				counter++;
			}
		}
		else if(likerOrCommenter.equals("commenter")) {
			mostCommentedByFollowers = new Object[length][2];
			for(String name : followers) {
				mostCommentedByFollowers[counter][0] = name;
				mostCommentedByFollowers[counter][1] = 0;
				counter++;
			}
		}
		
		
		//Maximal 12 Threads laufen gleichzeitig.
		if(likerOrCommenter.equals("liker")) {
	        ExecutorService executor1 = Executors.newFixedThreadPool(6);
	        System.out.println("Data8pool running");
	        
			for(String post : myPosts) {
	            executor1.submit(() -> {
	            	boolean answer = true;
	                answer = mostLikedOrCommentedByFollowers(post, likerOrCommenter);
	                
		            if(!answer) {
		            	executor1.shutdownNow();
		            }
	            	
	            });
			}
			executor1.shutdown();
			try {
				while (!executor1.awaitTermination(24L, TimeUnit.HOURS)) {
				    System.out.println("Not yet. Still waiting for termination");
				}
			} catch (InterruptedException e) {
				System.out.println("Waiting for Threads failed.");
				//e.printStackTrace();
			}
			
	        System.out.println("Data8pool finished");

		}
		else if(likerOrCommenter.equals("commenter")) {
	        ExecutorService executor2 = Executors.newFixedThreadPool(12);
	        System.out.println("Data9pool running");
	        
			for(String post : myPosts) {
	            executor2.submit(() -> {
	            	boolean answer = true;
	                answer = mostLikedOrCommentedByFollowers(post, likerOrCommenter);
	                
		            if(!answer) {
		            	executor2.shutdownNow();
		            }
	            	
	            });
			}
			executor2.shutdown();
			try {
				while (!executor2.awaitTermination(24L, TimeUnit.HOURS)) {
				    System.out.println("Not yet. Still waiting for termination");
				}
			} catch (InterruptedException e) {
				System.out.println("Waiting for Threads failed.");
				//e.printStackTrace();
			}
			
	        System.out.println("Data9pool finished");

		}
        
        		
		
        if(likerOrCommenter.equals("liker")) {
			if(mostLikedByFollowers != null) {
				Arrays.sort(mostLikedByFollowers, new Comparator<Object[]>() {
					@Override
					public int compare(Object[] o1, Object[] o2) {
				            Integer quantityOne = (Integer) o1[1];
					    Integer quantityTwo = (Integer) o2[1];
					   
					    return quantityTwo.compareTo(quantityOne);
		
					}
				});
				
				Object[][] mostLikedByFollowers2 = mostLikedByFollowers;
				
				int counterLiker = 0;
				int counterGhoster = 0;
				for(Object[] follower : mostLikedByFollowers2) {
					if((int) follower[1] != 0) {
						counterLiker++;
					}
					else {
						counterGhoster++;
					}
				}
				
				mostLikedByFollowers = new Object [counterLiker][2];
				ghostedLikeByFollowers = new String [counterGhoster];
				
				counterLiker = 0;
				counterGhoster = 0;
				
				for(Object[] follower : mostLikedByFollowers2) {
					if((int) follower[1] != 0) {
						mostLikedByFollowers[counterLiker][0] = follower[0];
						mostLikedByFollowers[counterLiker][1] = follower[1];
						counterLiker++;
					}
					else {
						ghostedLikeByFollowers[counterGhoster] = (String) follower[0];
						counterGhoster++;
					}
				}
			}
			System.out.println("Data8-Thread finished");
	    }
		else if(likerOrCommenter.equals("commenter")) {
			if(mostCommentedByFollowers != null) {
				
				Arrays.sort(mostCommentedByFollowers, new Comparator<Object[]>() {
					@Override
					public int compare(Object[] o1, Object[] o2) {
				            Integer quantityOne = (Integer) o1[1];
					    Integer quantityTwo = (Integer) o2[1];
					   
					    return quantityTwo.compareTo(quantityOne);
		
					}
				});
				
				
				Object[][] mostCommentedByFollowers2 = mostCommentedByFollowers;
				
				int counterCommenter = 0;
				int counterGhoster = 0;
				for(Object[] follower : mostCommentedByFollowers2) {
					if((int) follower[1] != 0) {
						counterCommenter++;
					}
					else {
						counterGhoster++;
					}
				}
				
				mostCommentedByFollowers = new Object [counterCommenter][2];
				ghostedCommentByFollowers = new String [counterGhoster];
				
				counterCommenter = 0;
				counterGhoster = 0;
				
				for(Object[] follower : mostCommentedByFollowers2) {
					if((int) follower[1] != 0) {
						mostCommentedByFollowers[counterCommenter][0] = follower[0];
						mostCommentedByFollowers[counterCommenter][1] = follower[1];
						counterCommenter++;
					}
					else {
						ghostedCommentByFollowers[counterGhoster] = (String) follower[0];
						counterGhoster++;
					}
				}
			
			}
		
			System.out.println("Data9-Thread finished");
		}
				
	}
	
	
	private boolean mostLikedOrCommentedByFollowers(String post, String likerOrCommenter) {
		int durchlauf = 0;
		String error = null;
		boolean answer = true;
		
		
		try {	
			
			int max = 200; //20 durchläufe entsprechen 1000 Likes die betrachtet werden, Faktor 50
			int count = 5000000;
			String end_cursor = null;
			String has_next_page = "false";
			int likes = 0;
			int comments = 0;

		
			do {
				
				String url = "";
		        if(likerOrCommenter.equals("liker")) {	
					if(has_next_page.equals("false")) {
						url = "https://www.instagram.com/graphql/query/?query_id=17864450716183058&variables={\"shortcode\":\"" + post + "\",\"include_reel\":true,\"first\":" + count + "}";
					}	
					else if (has_next_page.equals("true")) {
						url = "https://www.instagram.com/graphql/query/?query_id=17864450716183058&variables={\"shortcode\":\""+ post + "\",\"include_reel\":true,\"first\":" + count + ",\"after\":\"" + end_cursor + "\"}";
					}	
		        }
				else if(likerOrCommenter.equals("commenter")) {
					if(has_next_page.equals("false")) {
						url = "https://www.instagram.com/graphql/query/?query_hash=2efa04f61586458cef44441f474eee7c&variables={\"shortcode\":\"" + post + "\",\"parent_comment_count\":" + count + ",\"has_threaded_comments\":true}";
					}	
					else if (has_next_page.equals("true")) {
						url = "https://www.instagram.com/graphql/query/?query_hash=bc3296d1ce80a24b1b6e40b1e72903f5&variables={\"shortcode\":\"" + post + "\",\"first\":" + count + ",\"after\":\"" + end_cursor + "\"}";
					}	
				}
				Response response = r.doRequest(url);
	
			
				String output = response.body().string();
				JSONObject jsonObj = new JSONObject(output);
				error = jsonObj.toString();
				
				
		        if(likerOrCommenter.equals("liker")) {	
		        	jsonObj = jsonObj.getJSONObject("data").getJSONObject("shortcode_media").getJSONObject("edge_liked_by");
					if(durchlauf == 0) {
						likes = likes + Integer.parseInt(jsonObj.get("count").toString());
					}
		        }
				else if(likerOrCommenter.equals("commenter")) {
					jsonObj = jsonObj.getJSONObject("data").getJSONObject("shortcode_media").getJSONObject("edge_media_to_parent_comment");
					if(durchlauf == 0) {
						comments = comments + Integer.parseInt(jsonObj.get("count").toString());
					}
				}


							
				has_next_page = jsonObj.getJSONObject("page_info").get("has_next_page").toString();
							
							
				if(has_next_page.equals("true")) {
					end_cursor = jsonObj.getJSONObject("page_info").get("end_cursor").toString();
				}
							
				JSONArray jsonArr = jsonObj.getJSONArray("edges");
				int len = jsonArr.length();
				
		        if(likerOrCommenter.equals("liker")) {	
					for(int i=0;i<len;i++) {
								
						JSONObject liker = jsonArr.getJSONObject(i).getJSONObject("node");
						String username = liker.getString("username");
									
						for(int k=0;k<mostLikedByFollowers.length;k++) {
							if(mostLikedByFollowers[k][0].equals(username)) {
								int likesFollower = ((int) mostLikedByFollowers[k][1])+1;
								mostLikedByFollowers[k][1] = likesFollower;
							}
						}
									
					}
		        }
				else if(likerOrCommenter.equals("commenter")) {
					for(int i=0;i<len;i++) {
						JSONObject commenter = jsonArr.getJSONObject(i).getJSONObject("node").getJSONObject("owner");
						String username = commenter.getString("username");
						for(int k=0;k<mostCommentedByFollowers.length;k++) {
							if(mostCommentedByFollowers[k][0].equals(username)) {
								int likesCommenter = ((int) mostCommentedByFollowers[k][1])+1;
								mostCommentedByFollowers[k][1] = likesCommenter;
							}
						}
									
					}
				}
		
						
			durchlauf++;
					
			}while(has_next_page.equals("true") && durchlauf < max);
		
			if(likerOrCommenter.equals("liker")) {	
				postLikeNumber++;
				this.likes = this.likes + likes;
			}
			else if(likerOrCommenter.equals("commenter")) {
				postCommentNumber++;
				this.comments = this.comments + comments;
			}	
		
		} catch (Exception e) {
			
			if(likerOrCommenter.equals("liker")) {	
				if(error != null && !error.contains("shortcode_media")) {
					dataPoolLog.add("setMostLikedByFollowers Post: " + postLikeNumber + " Durchlauf: " + durchlauf + " failed -> " + error);
				}
			}
			else if(likerOrCommenter.equals("commenter")) {
				if(error != null && !error.contains("shortcode_media")) {
					dataPoolLog.add("setMostCommentedByFollowers Post: " + postCommentNumber + " Durchlauf: " + durchlauf + " failed -> " + error);
				}
			}
			

			answer = false;
			//e.printStackTrace();
			
		}
		
		
		return answer;
	}
	
	
	
	
	
	
	public boolean getSessionIdValid() {
		return sessionIdValid;
	}
	
	public void startThread(String obj) {
		if(obj.equals("LogData")) {
	        synchronized(LogData)
	        {
	        	LogData.notify();
	        }
		}
	}
	
	public void waitThread(String obj) {
	    try
	    {
	    	if(obj.equals("LogData")) {		
		      synchronized(LogData)
		      {
		    	  LogData.wait();
		      }
	    	}
	    }
	    catch(InterruptedException ie) { }
	}
	
	private void setDataPoolLog() {
		String errorLiker = null;
		String errorCommenter = null;
		boolean print1 = true;
		boolean print2 = true;
		
		for(String error : dataPoolLog) {
			if(error.contains("setMostLikedByFollowers")) {
				if(print1) {
				System.out.println(error);
				print1 = false;
				}
			}
			else if(error.contains("setMostCommentedByFollowers")) {
				if(print2) {
				System.out.println(error);
				print2 = false;
				}
			}
		}
		
	}
	
	
	public int getPostLikeNumber() {
		return postLikeNumber;
	}
	
	public int getPostCommentNumber() {
		return postCommentNumber;
	}
	
	public int getPostNumber() {
		if(myPosts != null) { 
			return myPosts.size(); 
		}
		else {
			return -1;
		}
	}	
}