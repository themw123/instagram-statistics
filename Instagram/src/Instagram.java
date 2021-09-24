import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.brunocvcunha.instagram4j.Instagram4j;
import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.Response;

public class Instagram{
	private Object CountLiker = new Object();
	private Object CountCommenter = new Object();
	private Object mutualObj = new Object();
	
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
	
	private Object[][] following;
	private Object[][] followers;
	private Vector<Object[]> notFollowingYou;
	private Vector<Object[]> youFollowingNot;
	private Vector<Object[]> mutual = new Vector<Object[]>();
	private Vector<String> OpenFriendRequestOut;
	private Vector<String> OpenFriendRequestIn;
	private Vector<Object[]> myPosts;
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
	}
	

	
    public void data1(){
		
    	//System.out.println("Data1-Thread running");
		Thread t1 = new Thread(() -> setFollowingAndFollowers("following"));
		t1.start();
		
		
    	//System.out.println("Data2-Thread running");
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
	    	//System.out.println("Data3-Thread running");
			t3 = new Thread(() -> setNotFollowingYou());
			t3.start();
			
	    	//System.out.println("Data4-Thread running");
			t4 = new Thread(() -> setYouFollowingNot());
			t4.start();
		}
		
		
    	//System.out.println("Data5-Thread running");
		Thread t5 = new Thread(() -> setOpenFriendRequestOut());
		t5.start();
		
    	//System.out.println("Data6-Thread running");
		Thread t6 = new Thread(() -> setOpenFriendRequestIn());
		t6.start();
		
		
		
		
    	//System.out.println("Data7-Thread running");
		Thread t7 = new Thread(() -> setMyPosts());
		t7.start();
		
		//Auf Posts warten
		try {
			t7.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		
	
		
		
		
		
		//Auf übrige Threads warten
    	try {
    		if(following != null && followers != null) {
				t3.join();
				t4.join();
    		}
			t5.join();
			t6.join();
			t7.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}
	
	
	
    
    
    
    
    
    
    
    public void data2() {

		if(following != null && followers != null && myPosts != null) {
			//System.out.println("\n!!!!!!!!HEAVY!!!!!!!!");
		    //System.out.println("Data8-Thread running");
			Thread t8 = new Thread(() -> setMostLikedOrCommentedByFollowers("liker"));
			t8.start();
			
			
		    //System.out.println("Data9-Thread running");
			Thread t9 = new Thread(() -> setMostLikedOrCommentedByFollowers("commenter"));
			t9.start();
			
			
			try {
				t8.join();
				t9.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
	        setDataPoolLog();
			sortPosts("likes", "down");
			sortFollower("likes", "down");
		}
		
		
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
			
			if(urlParameter.equals("following") && following == null) {
				following = new Object[length][4];
			}
			else if(urlParameter.equals("followers") && followers == null) {
				followers = new Object[length][4];
			}

			
			for(int i=0;i<length;i++) {
				JSONObject userJson = ja_users.getJSONObject(i);
				String username = userJson.getString("username");
				String picture = userJson.getString("profile_pic_url");
				if(urlParameter.equals("following")) {
					this.following[i][0] = username;
					this.following[i][1] = picture;
					this.following[i][2] = 0;
					this.following[i][3] = 0;

				}
				else if(urlParameter.equals("followers")) {
					this.followers[i][0] = username;
					this.followers[i][1] = picture;
					this.followers[i][2] = 0;
					this.followers[i][3] = 0;
				}
			}
					
		} catch (Exception e) {
			System.out.println("setFollowingAndFollowers failed -> " + error);
			if(urlParameter.equals("following")) {
				//following = null;
			}
			else if(urlParameter.equals("followers")) {
				//followers = null;
			}
			//e.printStackTrace();
		}
		
		
		/*
		if(urlParameter.equals("following")) {
			System.out.println("Data1-Thread finished");
		}
		else if(urlParameter.equals("followers")) {
			System.out.println("Data2-Thread finished");
		}
		*/
	}
	
	
	private void setNotFollowingYou() {
		
		notFollowingYou = new Vector<Object[]>();

		boolean drin = false;
		for(Object[] foingObj : following) {
			String foing = (String) foingObj[0];
			
			for(Object[] foersObj : followers) {
				String foers = (String) foersObj[0];
				if(foing.equals(foers)) {
					drin = true;
					
					synchronized(mutualObj) {
						mutual.add(foersObj);
					}
					break;
				}
			}
			if(!drin) {
				notFollowingYou.add(foingObj);
			}
			drin = false;
		}
		
		//System.out.println("Data3-Thread finished");

	    
	}
	
	private void setYouFollowingNot() {
		youFollowingNot = new Vector<Object[]>();

		boolean drin = false;
		for(Object[] foersObj : followers) {
			String foers = (String) foersObj[0];
			for(Object[] foingObj : following) {
				String foing = (String) foingObj[0];
				if(foing.equals(foers)) {
					drin = true;
					
					boolean in = false;
					synchronized(mutualObj) {
						for(Object[] m : mutual) {
							if(m[0].equals(foers)) {
								in = true;
								break;
							}
						}
					}
					if(!in) {
						mutual.add(foersObj);
					}
					break;
				}
			}
			if(!drin) {
				youFollowingNot.add(foersObj);
			}
			drin = false;
		}
	    
		//System.out.println("Data4-Thread finished");

		
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
		
		//System.out.println("Data5-Thread finished");

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
			
		//System.out.println("Data6-Thread finished");

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
		
		myPosts = new Vector<Object[]>();
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
					int likes = 0;
					int comments = 0;
					
					JSONObject post = jsonArr.getJSONObject(i).getJSONObject("node");
					Object[] postObj = new Object[3];
					
					try {
						likes = Integer.parseInt(post.getJSONObject("edge_liked_by").get("count").toString());
					}catch(Exception e) {
						likes = Integer.parseInt(post.getJSONObject("edge_media_preview_like").get("count").toString());
					}
					comments = Integer.parseInt(post.getJSONObject("edge_media_to_comment").get("count").toString());
					
					String shortcode = post.getString("shortcode");
					
					postObj[0] = shortcode;
					postObj[1] = likes;
					postObj[2] = comments;
					
					this.likes = this.likes + likes;
					this.comments = this.comments + comments;
					
					myPosts.add(postObj);
				}
			} catch (Exception e) {
				System.out.println("setMyPosts Post: " + durchlauf + " failed -> " + error);
				//myPosts = null;
				//e.printStackTrace();
				break;
			}
					
			durchlauf++;
				
		}while(has_next_page.equals("true") && durchlauf < max);		
				
		//System.out.println("Data7-Thread finished");
		
	}
	
	

	
	
	private void setMostLikedOrCommentedByFollowers(String likerOrCommenter) {
				
		dataPoolLog = new Vector<String>();

	
		//Maximal 12 Threads laufen gleichzeitig.
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(12);
        /*
		if(likerOrCommenter.equals("liker")) {
        	System.out.println("Data8pool running");
        }
		else if(likerOrCommenter.equals("commenter")) {
    		System.out.println("Data9pool running");
        }
        */
		for(Object[] postObj : myPosts) {
			String post = (String) postObj[0];
            executor.submit(() -> {
            	boolean answer = true;
                answer = mostLikedOrCommentedByFollowers(post, likerOrCommenter);
                
	            if(!answer) {
	            	executor.shutdownNow();
	            }
            	
            });
		}
		executor.shutdown();
		try {
			while (!executor.awaitTermination(24L, TimeUnit.HOURS)) {
			    System.out.println("Not yet. Still waiting for termination");
			}
		} catch (InterruptedException e) {
			System.out.println("Waiting for Threads failed.");
			//e.printStackTrace();
		}
		
		/*
        if(likerOrCommenter.equals("liker")) {
        	System.out.println("Data8pool finished");
        }
		else if(likerOrCommenter.equals("commenter")) {
        	System.out.println("Data9pool finished");
		}
        */
        		
				
	}
	
	
	private boolean mostLikedOrCommentedByFollowers(String post, String likerOrCommenter) {
		boolean answer = true;
		int durchlauf = 0;
		String error = null;
		
		
		try {	
			
			int max = 200; //20 durchläufe entsprechen 1000 Likes die betrachtet werden, Faktor 50
			int count = 5000000;
			String end_cursor = null;
			String has_next_page = "false";
			/*
			int likes = 0;
			int comments = 0;
			*/
		
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
					/*
		        	if(durchlauf == 0) {
						likes = likes + Integer.parseInt(jsonObj.get("count").toString());
					}
					*/
		        }
				else if(likerOrCommenter.equals("commenter")) {
					jsonObj = jsonObj.getJSONObject("data").getJSONObject("shortcode_media").getJSONObject("edge_media_to_parent_comment");
					/*
					if(durchlauf == 0) {
						comments = comments + Integer.parseInt(jsonObj.get("count").toString());
					}
					*/
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
									
						for(int k=0;k<followers.length;k++) {
							if(followers[k][0].equals(username)) {
								int likesFollower = ((int) followers[k][2])+1;
								followers[k][2] = likesFollower;
							}
						}
									
					}
		        }
				else if(likerOrCommenter.equals("commenter")) {
					for(int i=0;i<len;i++) {
						JSONObject commenter = jsonArr.getJSONObject(i).getJSONObject("node").getJSONObject("owner");
						String username = commenter.getString("username");
						for(int k=0;k<followers.length;k++) {
							if(followers[k][0].equals(username)) {
								int likesCommenter = ((int) followers[k][3])+1;
								followers[k][3] = likesCommenter;
							}
						}
									
					}
				}
		
						
			durchlauf++;
					
			}while(has_next_page.equals("true") && durchlauf < max);


			
			if(likerOrCommenter.equals("liker")) {
				synchronized(CountLiker)
				{
					postLikeNumber++;
					//this.likes = this.likes + likes;
				}
			}
			else if(likerOrCommenter.equals("commenter")) {
				synchronized(CountCommenter)
				{
					postCommentNumber++;
					//this.comments = this.comments + comments;
				}
			}	
			
		} 
		
		catch (Exception e) {
			
			if(error.contains("message")) {
				if(likerOrCommenter.equals("liker")) {	
					dataPoolLog.add("setMostLikedByFollowers Post: " + postLikeNumber + " Durchlauf: " + durchlauf + " failed -> " + error);
				}
				else if(likerOrCommenter.equals("commenter")) {
					dataPoolLog.add("setMostCommentedByFollowers Post: " + postCommentNumber + " Durchlauf: " + durchlauf + " failed -> " + error);
				}
				answer = false;
			}
			
		}
		
		
		return answer;
	}
	
	
	
	
	
	
	public boolean getSessionIdValid() {
		return sessionIdValid;
	}

	
	
	private void setDataPoolLog() {
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
	
	private void sortPosts(String likesOrcomments, String order) {
		
		boolean run;
		int count1 = 0, count2 = 0;
		while(true) { 
			run = false;
			for(int k=0;k<myPosts.size()-1;k++) {
				Object[] obj1 = myPosts.get(k);
				Object[] obj2 = myPosts.get(k+1);
	
				if(likesOrcomments.equals("likes")) {
					count1 = (int) obj1[1];
					count2 = (int) obj2[1];
				}
				else if(likesOrcomments.equals("comments")) {
					count1 = (int) obj1[2];
					count2 = (int) obj2[2];
				}
				
				if(order.equals("down")) {
					if(count2 > count1) {
						Collections.swap(myPosts,k,k+1);
						run = true;
					}
				}
				else if(order.equals("up")) {
					if(count2 < count1) {
						Collections.swap(myPosts,k,k+1);
						run = true;
					}
				}
			}
			if(!run) {
				break;
			}
		}
		//System.out.println("");
	}
	
	public void sortFollower(String likesOrcomments, String order) {
		
		boolean run;
		int count1 = 0, count2 = 0;
		while(true) { 
			run = false;
			for(int k=0;k<followers.length-1;k++) {
				
				
				Object[] obj1 = followers[k];
				Object[] obj2 = followers[k+1];
				
				if(likesOrcomments.equals("likes")) {
					count1 = (int) obj1[2];
					count2 = (int) obj2[2];
				}
				else if(likesOrcomments.equals("comments")) {
					count1 = (int) obj1[3];
					count2 = (int) obj2[3];
				}
				

				if(order.equals("down")) {
					if(count2 > count1) {
						Object[] hilf = obj1;
						followers[k] = obj2;
						followers[k+1] = hilf;
						run = true;
					}
				}
				else if(order.equals("up")) {
					if(count2 < count1) {
						Object[] hilf = obj1;
						followers[k] = obj2;
						followers[k+1] = hilf;
						run = true;
					}
				}
				
			}
			if(!run) {
				break;
			}
		}
		//System.out.println("");
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