
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.Response;

public class Instagram{
	
	private Object[] data;
	
	private String chooseLoginprocess = "session";
	private String username;
	private String password;
	private String sessionId;
	private boolean sessionIdValid;
	private String ds_user_id;
	private Vector<String> errorLog;

	
	private APIRequest r;
	
	private Object[][] following;
	private Object[][] followers;
	private Vector<Object[]> notFollowingYou;
	private Vector<Object[]> youFollowingNot;
	private Vector<Object[]> mutual;
	private Vector<String[]> openFriendRequestOut;
	private Vector<String[]> openFriendRequestIn;
	private Vector<Object[]> myPosts;
	
	
	private int reachedLikes;
	private int reachedComments;
	private int reachedOut;
	private int reachedPostLikes;
	private int reachedPostComments;
	private long likes;
	private long comments;	
	
	private int myRealPostCount;
	private int myRealFollowersCount;
	private int myRealFollowingCount;
		
	private boolean runThread8;
	private boolean runThread9;
	private boolean runThread10;

	
	
	public Instagram(String chooseLoginprocess, String data1, String data2) {
		
		this.chooseLoginprocess = chooseLoginprocess;
			
		if(chooseLoginprocess.equals("session")) {
			this.sessionId = data1;
			this.ds_user_id = data2;
		}
			
		else if(chooseLoginprocess.equals("login")) {
			this.username = data1;
			this.password = data2;
		}
			
		initialDatastructures();

	}

	
	private void initialDatastructures() {
		this.sessionIdValid = false;
		notFollowingYou = new Vector<Object[]>();
		mutual = new Vector<Object[]>();
		youFollowingNot = new Vector<Object[]>();
		openFriendRequestOut = new Vector<String[]>();
		openFriendRequestIn = new Vector<String[]>();
		myPosts = new Vector<Object[]>();
		errorLog = new Vector<String>();
		
		reachedLikes = 0;
		reachedComments = 0;
		reachedOut = 0;
		reachedPostLikes = 0;
		reachedPostComments = 0;
		likes = 0;
		comments = 0;
		
		myRealPostCount = 0;
		myRealFollowersCount = 0;
		myRealFollowingCount = 0;
		
		runThread8 = true;
		runThread9 = true;
		runThread10 = true;
	}
	
	
	
	public void start() {
		
		if(chooseLoginprocess.equals("session") || chooseLoginprocess.equals("login")) {
			login();
			if(sessionIdValid) {	
				//sessionid und ds_user_id in App abspeichern
				if(chooseLoginprocess.equals("login")) {
					System.out.println("Login successful\n");
				}
				else if(chooseLoginprocess.equals("session")) {
					System.out.println("Session valid\n");
				}
				if(username == null) {
					username = r.getUsername(ds_user_id);	
				}	
				if(setRealCounts()) {
					data();
				}
				//errors. In UI error anzeigen
				setErrorLog();
				System.out.println();
				for(String e : errorLog) {
				    System.out.println(e);
				}
				System.out.println("\nRequests total: " + getRequestsCount());  
			}
			else {
				if(chooseLoginprocess.equals("login")) {
					System.out.println("Login failed\n");
				}
				else if(chooseLoginprocess.equals("session")) {
					System.out.println("Session error\n");
				}
				//login page fehlermeldung ausgeben
				if(sessionId == null) {
					System.out.println("Wrong password or username");
				}
				else if(sessionId == "two_factor_required") {
					System.out.println("Please disable the two factor authentication in your instagram account settings. After you logged in in this App you can reactivate it.");
				}
			}
			
		}
		else {
			System.out.println("Wrong parameter in constructor");
		}
		
	}
	
	
	
	private void login() {
		if(chooseLoginprocess.equals("login")){
			setSession();
		}
		
		this.r = new APIRequest(sessionId);
		//check if sessionId still works
		sessionIdValid = r.checkSessionId();
		
	}
	
	
	
	private void setSession() {
		
		InstagramLogin InstagramLogin = new InstagramLogin(username, password);
		String[] session = InstagramLogin.getSession();
		ds_user_id = session[0];
		sessionId = session[1];
	
	}
	

    private boolean setRealCounts() {
    	boolean success = true;
		String error = null;
		String url = "https://www.instagram.com/" + username + "/?__a=1";
			
		Response response = r.doRequest(url);
			
		try {			
			String output = response.body().string();
			JSONObject jsonObj = new JSONObject(output);
			error = jsonObj.toString();
			myRealPostCount = jsonObj.getJSONObject("graphql").getJSONObject("user").getJSONObject("edge_owner_to_timeline_media").getInt("count");
			myRealFollowingCount = jsonObj.getJSONObject("graphql").getJSONObject("user").getJSONObject("edge_follow").getInt("count");
			myRealFollowersCount = jsonObj.getJSONObject("graphql").getJSONObject("user").getJSONObject("edge_followed_by").getInt("count");
		}catch(Exception e) {
			errorLog.add("setRealCounts failed -> "  + error);
			success = false;
		}
		return success;
    }
	
    
    
    private void data(){
   	
    	int playvalue = 4;
    	
		Thread t1 = new Thread(() -> setFollowingAndFollowers("following"));
		Thread t2 = new Thread(() -> setFollowingAndFollowers("followers"));
		Thread t3 = new Thread(() -> setNotFollowingYou());
		Thread t4 = new Thread(() -> setYouFollowingNot());
		Thread t5 = new Thread(() -> setOpenFriendRequestOut());
		Thread t6 = new Thread(() -> setOpenFriendRequestIn());
		Thread t7 = new Thread(() -> setMyPosts());
		
		Thread t8 = new Thread(() -> setOpenFriendRequestOutExtras());

		Thread t9 = new Thread(() -> setMostLikedOrCommentedByFollowers("liker"));
		Thread t10 = new Thread(() -> setMostLikedOrCommentedByFollowers("commenter"));
		
		
		System.out.println("Threads:1-7 running");

    	//System.out.println("Data1-Thread running");
		t1.start();
    	//System.out.println("Data2-Thread running");
		t2.start();
    	//System.out.println("Data6-Thread running");
		t5.start();
    	//System.out.println("Data6-Thread running");
		t6.start();
    	//System.out.println("Data7-Thread running");
		t7.start();
		
		
		
		//load Main UI and show waiting symbols on general and second page
		
		
		
		
		//Auf following/follower warten. Bei fail, nur Thread 5,6 und 7 starten
		try {
			t1.join();
			t2.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		

		try {
			if((this.following.length != 0 || this.followers.length != 0) && getFollowersCount()+playvalue >= myRealFollowersCount && getFollowingCount()+playvalue >= myRealFollowingCount) {
		    	//System.out.println("Data3-Thread running");
				t3.start();
				//System.out.println("Data4-Thread running");
				t4.start();
				
				t3.join();
				t4.join();
			}
			t5.join();
			t6.join();
			t7.join();
			System.out.println("Threads:1-7 finished");
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		
		
		
		
		
		//data1 fertig
		//data 1-7 into general page
		int postsCount = getPostsCount();
		if(postsCount+playvalue < myRealPostCount) {
			//In UI fehler anzeigen(bla von bla posts).
			errorLog.add("MyPosts: " + postsCount + " from " + myRealPostCount + " failed -> Too much posts. Only round about 600 possible.");
			postsCount = myRealPostCount;
		}
		int followers = getFollowersCount();
		if(followers+playvalue < myRealFollowersCount) {
			//wert von myRealFollowersCount anzeigen.
			errorLog.add("Followers failed: " + followers + " from " + myRealFollowersCount);
			followers = myRealFollowersCount;
		}
		int following = getFollowingCount();
		if(following+playvalue < myRealFollowingCount) {
			//wert von myRealFollowingCount anzeigen.
			errorLog.add("Following: failed " + following + " from " + myRealFollowingCount);
			following = myRealFollowingCount;
		}
		long likes = getLikes();
		long comments = getComments();
		
		Object[] notFollowingYou = getNotFollowingYou();
		Object[] youFollowingNot = getYouFollowingNot();
		Object[] mutual = getMutual();
		if(followers+playvalue < myRealFollowersCount || following+playvalue < myRealFollowingCount) {
			//In UI fehler bei allen drei anzeigen. Fehler: follower/following limit
		}
		
		Object[] openFriendRequestIn = getOpenFriendRequestIn();
	
		
		//get data1+ into second page
	    Object[] mostLikesPosts= getPosts("likes", "down");
	    Object[] mostCommentsPosts= getPosts("comments", "down");
	    Object[] leastLikesPosts= getPosts("likes", "up");
	    Object[] leastCommentsPosts= getPosts("comments", "up");
	    if(postsCount+playvalue < myRealPostCount) {
			//In UI fehler bei allen vier anzeigen. Fehler: Post limit aber zeigt die geholten an.
	    }
		
	    
	    
	    
	    
		if(openFriendRequestOut.size() != 0) {
			System.out.println("Thread:8 running");
			t8.start();
		}
		if(this.followers != null && this.followers.length != 0 && getFollowersCount()+playvalue >= myRealFollowersCount && !myPosts.isEmpty()) {
		    //System.out.println("Data8-Thread running");
			System.out.println("Thread:9-10 running");
			t9.start();
		    //System.out.println("Data9-Thread running");
			t10.start();
		}
		
		if(openFriendRequestOut.size() != 0) {
			try {
				t8.join();
				System.out.println("Threads:8 finished");				
	
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		
		
		//data2 fertig
		//data 8 into first page
		Object[] OpenFriendRequestOut = getOpenFriendRequestOut();
		
		
		
		
		if(this.followers != null && this.followers.length != 0 && getFollowersCount()+playvalue >= myRealFollowersCount && !myPosts.isEmpty()) {
			try {
				//data2 fertig
				t9.join();
				t10.join();
				System.out.println("Threads:9-10 finished");	   	
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		
		
		
		//data3 fertig
		//data 9-10 into second page
	   	Object[] mostLikesFrom = getMostLikesandCommentsFrom("likes", "down");
	   	Object[] mostCommentsFrom = getMostLikesandCommentsFrom("comments", "down");
	   	Object[] leastLikesFrom = getMostLikesandCommentsFrom("likes", "up");
	   	Object[] leastCommentsFrom = getMostLikesandCommentsFrom("comments", "up");
		if(getFollowersCount()+playvalue < myRealFollowersCount) {
			//In UI fehler bei allen vier anzeigen. Fehler: follower limit
		}
		else if(reachedLikes < getPostsCount()) {
			//In UI fehler allen mostLikeFrom und leastLikesFrom anzeigen. Fehler: nicht alle Posts möglich, aber bis dato werden angezeigt
			//postLikeCount
		}
		else if(reachedComments < getPostsCount()) {
			//In UI fehler bei mostCommentsFrom und leastCommentsFrom anzeigen. Fehler: nicht alle Posts möglich, aber bis dato werden angezeigt
			//postCommentCount
		}
		
	   	
	   	data = new Object[18];
    	data[0] = postsCount;
    	data[1] = followers;
    	data[2] = following;
    	data[3] = likes;
    	data[4] = comments;
    	data[5] = notFollowingYou;
    	data[6] = youFollowingNot;
    	data[7] = mutual;
    	data[8] = openFriendRequestIn;
    	data[9] = mostLikesPosts;
    	data[10] = mostCommentsPosts;
    	data[11] = leastLikesPosts;
    	data[12] = leastCommentsPosts;
    	data[13] = OpenFriendRequestOut;
    	data[14] = mostLikesFrom;
    	data[15] = mostCommentsFrom;
    	data[16] = leastLikesFrom;
    	data[17] = leastCommentsFrom;	   	
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
				following = new Object[length][5];
			}
			else if(urlParameter.equals("followers") && followers == null) {
				followers = new Object[length][5];
			}

			
			for(int i=0;i<length;i++) {
				JSONObject userJson = ja_users.getJSONObject(i);
				String username = userJson.getString("username");
				String picture = userJson.getString("profile_pic_url");
				long id = userJson.getLong("pk");
				
				if(urlParameter.equals("following")) {
					this.following[i][0] = username;
					this.following[i][1] = 0;
					this.following[i][2] = 0;
					this.following[i][3] = id;
					this.following[i][4] = picture;

				}
				else if(urlParameter.equals("followers")) {
					this.followers[i][0] = username;
					this.followers[i][1] = 0;
					this.followers[i][2] = 0;
					this.followers[i][3] = id;
					this.followers[i][4] = picture;
				}
			}
					
		} catch (Exception e) {
			//e.printStackTrace();
			errorLog.add("setFollowingAndFollowers failed -> "  + error);
		}
		/*
		if(urlParameter.equals("following")) {
			System.out.println("Data1-Thread finished");
		}
		else if(urlParameter.equals("followers")) {
			System.out.println("Data3-Thread finished");
		}
		*/
	}
	
	
	private void setNotFollowingYou() {	
		boolean drin = false;
		for(Object[] foingObj : following) {
			String foing = (String) foingObj[0];
			for(Object[] foersObj : followers) {
				String foers = (String) foersObj[0];
				if(foing.equals(foers)) {
					drin = true;
					mutual.add(foersObj);
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

		boolean drin = false;
		for(Object[] foersObj : followers) {
			String foers = (String) foersObj[0];
			for(Object[] foingObj : following) {
				String foing = (String) foingObj[0];
				if(foing.equals(foers)) {
					drin = true;
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
		
		int max = 10;//faktor 10
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
					//platzhalter für picture und id, weil nicht in request vorhanden
					String picture = "https://scontent-hel3-1.cdninstagram.com/v/t51.2885-19/44884218_345707102882519_2446069589734326272_n.jpg?_nc_ht=scontent-hel3-1.cdninstagram.com&_nc_ohc=isg9IBAnxdAAX-Wj2Wc&edm=AEsR1pMBAAAA&ccb=7-4&oh=fbaa5b506be987e7c37fb50eface7cf2&oe=615717CF&_nc_sid=3f45ac&ig_cache_key=YW5vbnltb3VzX3Byb2ZpbGVfcGlj.2-ccb7-4";
					String id = "0";
					String[] person = new String[3];
					person[0] = username;
					person[1] = id;
					person[2] = picture;
					openFriendRequestOut.add(person);
				}
						
						
			} catch (Exception e) {
				//e.printStackTrace();
				errorLog.add("setOpenFriendRequestOut" + " Durchlauf: " + durchlauf + " failed -> " + error);
				break;
			}
			durchlauf++;
		}while(cursor != null && durchlauf < max);
		//System.out.println("Data5-Thread finished");
	}
	
	private void setOpenFriendRequestOutExtras() {

		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(12);

		for(int i=0;i<openFriendRequestOut.size();i++) {
			int ip = i;
			String username = openFriendRequestOut.get(i)[0];
            executor.submit(() -> {
                openFriendRequestOutExtras(username, ip);
            });
		}
		executor.shutdown();
		try {
			while (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
			    System.out.println("Not yet. Still waiting for termination");
			}
		} catch (InterruptedException e) {
			System.out.println("Waiting for Threads failed.");
			//e.printStackTrace();
		}

	}
	
	private void openFriendRequestOutExtras(String username, int i) {
		
		if(runThread8) {
			
			String error = "";
			boolean answer = true;
			String url = "https://www.instagram.com/" + username + "/?__a=1";
			Response response = r.doRequest(url);
			try {
				String output = response.body().string();
				JSONObject jsonObj = new JSONObject(output);
				error = jsonObj.toString();
				String id = jsonObj.getJSONObject("graphql").getJSONObject("user").getString("id");
				String picture = jsonObj.getJSONObject("graphql").getJSONObject("user").getString("profile_pic_url_hd");
	
				openFriendRequestOut.get(i)[1] = id;
				openFriendRequestOut.get(i)[2] = picture;
								
			} catch (Exception e) {
				runThread8 = false;
				
				if(error.contains("message")) {
					errorLog.add("getOpenFriendRequestOutIds -> " + error);
				}
			}
			finally {
				reachedOut++;
			}
		
		}
	}
	
	
	private void setOpenFriendRequestIn() {

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
				String picture = userJson.getString("profile_pic_url");
				String id = userJson.get("pk").toString();
				String[] person = new String[3];
				person[0] = username;
				person[1] = id;
				person[2] = picture;
				openFriendRequestIn.add(person);
			}
					
		} catch (Exception e) {
			//e.printStackTrace();	
			errorLog.add("setOpenFriendRequestIn failed -> " + error);
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
					Object[] postObj = new Object[4];
					
					try {
						likes = Integer.parseInt(post.getJSONObject("edge_liked_by").get("count").toString());
					}catch(Exception e) {
						likes = Integer.parseInt(post.getJSONObject("edge_media_preview_like").get("count").toString());
					}
					comments = Integer.parseInt(post.getJSONObject("edge_media_to_comment").get("count").toString());
					
					String shortcode = post.getString("shortcode");
					
					String display_url = post.getString("display_url");

					
					postObj[0] = shortcode;
					postObj[1] = likes;
					postObj[2] = comments;
					postObj[3] = display_url;


					this.likes = this.likes + likes;
					this.comments = this.comments + comments;
					
					myPosts.add(postObj);
				}
			} catch (Exception e) {
				//e.printStackTrace();
				if (durchlauf == 1) {
					durchlauf = 12;
				}
				else if(durchlauf > 1){
					durchlauf = 12 + ((durchlauf-1) * 40);  
				}
				errorLog.add("setMyPosts Post: " + durchlauf + " failed -> " + error);
				break;
			}
					
			durchlauf++;
				
		}while(has_next_page.equals("true") && durchlauf < max);		
		//System.out.println("Data7-Thread finished");
	}
	
	

	
	
	private void setMostLikedOrCommentedByFollowers(String likerOrCommenter) {
	
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
                mostLikedOrCommentedByFollowers(post, likerOrCommenter);        	
            });
		}
		executor.shutdown();
		try {
			while (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
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
	
	
	private void mostLikedOrCommentedByFollowers(String post, String likerOrCommenter) {
		
		
        if((likerOrCommenter.equals("liker") && runThread9) || (likerOrCommenter.equals("commenter") && runThread10)) {
		
			String error = "";
			int durchlauf = 0;	
			
			try {
			
				int max = 200; //20 durchläufe entsprechen 1000 Likes die betrachtet werden, Faktor 50
				int count = 5000000;
				String end_cursor = null;
				String has_next_page = "false";
				
				
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
				    }
					else if(likerOrCommenter.equals("commenter")) {
						jsonObj = jsonObj.getJSONObject("data").getJSONObject("shortcode_media").getJSONObject("edge_media_to_parent_comment");
					}
				    
					
					has_next_page = jsonObj.getJSONObject("page_info").get("has_next_page").toString();
									
									
					if(has_next_page.equals("true")) {
						end_cursor = jsonObj.getJSONObject("page_info").get("end_cursor").toString();
					}
									
					JSONArray jsonArr = jsonObj.getJSONArray("edges");
					int len = jsonArr.length();
					
				    if(likerOrCommenter.equals("liker")) {	
						reachedLikes = reachedLikes + len;
					}
				    else if(likerOrCommenter.equals("commenter")) {
						reachedComments = reachedComments + len;
				    }
				    
				    if(likerOrCommenter.equals("liker")) {	
						for(int i=0;i<len;i++) {
											
							JSONObject liker = jsonArr.getJSONObject(i).getJSONObject("node");
							String username = liker.getString("username");
												
							for(int k=0;k<followers.length;k++) {
								if(followers[k][0].equals(username)) {;
									followers[k][1] = (int)followers[k][1]+1;
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
									followers[k][2] = (int)followers[k][2]+1;
								}
							}
											
						}
					}
				        
					durchlauf++;
							
					}while(has_next_page.equals("true") && durchlauf < max);
				
			} 
			
			catch (Exception e) {
				
		        if(likerOrCommenter.equals("liker")) {
		        	runThread9 = false;
		        }
		        else if(likerOrCommenter.equals("commenter")){
		        	runThread10 = false;
		        }
		        
		        
	 			if(error.contains("message")) {
					if(likerOrCommenter.equals("liker")) {	
						errorLog.add("setMostLikedByFollowers -> " + error);
					}
					else if(likerOrCommenter.equals("commenter")) {
						errorLog.add("setMostCommentedByFollowers -> " + error);
					}
				}
				
			}
			finally {
				if(likerOrCommenter.equals("liker")) {	
					reachedPostLikes++;
				}
				else if(likerOrCommenter.equals("commenter")) {
					reachedPostComments++;
				}
			}
			
		}

	}
	
	
	

	
	
	
	

	
	
	public void setErrorLog() {
		
		boolean print1 = true;
		boolean print2 = true;
		boolean print3 = true;
		boolean print4 = true;
		
				
		/*
		int reachedPostLikes = 0;
		int likes = 0;
		if(reachedLikes != 0) {
			for(int i=0;i<myPosts.size();i++) {
				Object[] p = myPosts.get(i);
				likes = likes + (int)p[1];
				reachedPostLikes++;
				if(likes >= reachedLikes) {
					break;
				}
			}
		}
		
		int reachedPostComments = 0;
		int comments = 0;
		
		if(reachedComments != 0) {
			for(int i=0;i<myPosts.size();i++) {
				Object[] p = myPosts.get(i);
				comments = comments + (int)p[2];
				reachedPostComments++;
				if(comments >= reachedComments) {
					break;
				}
			}
		}
		*/
		
		
		for(int i=0;i<errorLog.size();i++) {
			String error = errorLog.get(i);
			if(error.contains("setMostLikedByFollowers")) {
				if(print1) {
					String beg = error.substring(0, error.indexOf("->")-1);
					String end = error.substring(error.indexOf("{")-1, error.indexOf("}")+1);
					error = beg + " maximum of " + reachedPostLikes + " posts analysed" + end;
					errorLog.set(i, error);
					print1 = false;
				}
				else {
					errorLog.remove(i);
					i--;
				}
			}
			else if(error.contains("setMostCommentedByFollowers")) {
				if(print2) {
					String beg = error.substring(0, error.indexOf("->")-1);
					String end = error.substring(error.indexOf("{")-1, error.indexOf("}")+1);
					error = beg + " maximum of " + reachedPostComments + " posts analysed" + end;
					errorLog.set(i, error);
					print2 = false;
				}
				else {
					errorLog.remove(i);
					i--;
				}
			}
			else if(error.contains("getOpenFriendRequestOutIds")) {
				if(print3) {
					String beg = error.substring(0, error.indexOf("->")-1);
					String end = error.substring(error.indexOf("{")-1, error.indexOf("}")+1);
					error = beg + " maximum of " + reachedOut + " persons " + end;
					errorLog.set(i, error);
					print3 = false;
				}
				else {
					errorLog.remove(i);
					i--;
				}
			}
			else if(error.contains("MyPosts")) {
				if(print4) {
					print4 = false;
				}
				else {
					errorLog.remove(i);
					i--;
				}
			}
		}
		
	}
	
	public Object[] getPosts(String likesOrcomments, String order) {
		
		if(!myPosts.isEmpty()) {
		
			Object[] posts = new Object[myPosts.size()];
			int count = 0;
			for(Object post : myPosts) {
				posts[count] = (Object[]) post;
				count++;
			}
			
			
			boolean run;
			int count1 = 0, count2 = 0;
			while(true) { 
				run = false;
				for(int k=0;k<posts.length-1;k++) {
							
					Object[] obj1 = (Object[]) posts[k];
					Object[] obj2 = (Object[]) posts[k+1];
					
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
							Object[] hilf = obj1;
							posts[k] = obj2;
							posts[k+1] = hilf;
							run = true;
						}
					}
					else if(order.equals("up")) {
						if(count2 < count1) {
							Object[] hilf = obj1;
							posts[k] = obj2;
							posts[k+1] = hilf;
							run = true;
						}
					}
				}
				if(!run) {
					break;
				}
			}
			//System.out.println("");
			return posts;
		}
		else {
			return null;
		}
	}
	
	public Object[] getMostLikesandCommentsFrom(String likesOrComments, String order) {
		
		Object[] onlyMost;
		
		if(followers != null) {
		
			int count = 0;
			
			for(int i=0;i<followers.length;i++) {
				count++;
			}
			
			Object[] followers = new Object[count];
			count = 0;
			for(Object f : this.followers) {
				followers[count] = f;
				count++;
			}
			
			
			
			boolean run;
			int count1 = 0, count2 = 0;
			while(true) { 
				run = false;
				for(int k=0;k<followers.length-1;k++) {
					
					
					Object[] obj1 = (Object[]) followers[k];
					Object[] obj2 = (Object[]) followers[k+1];
					
					if(likesOrComments.equals("likes")) {
						count1 = (int) obj1[1];
						count2 = (int) obj2[1];
					}
					else if(likesOrComments.equals("comments")) {
						count1 = (int) obj1[2];
						count2 = (int) obj2[2];
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
			
			
			count = 0;
			int c = 0;
			for(int i=0;i<followers.length;i++) {
				Object[] f = (Object[]) followers[i];
				if(likesOrComments.equals("likes")) {
					c = (int)f[1];
				}
				else if(likesOrComments.equals("comments")) {
					c = (int)f[2];
				}
				
				if(order.equals("down") && c == 0) {
					break;
				}
				else if(order.equals("up") && c != 0) {
					break;
				}
				
				count++;
			}
			
			
			onlyMost = new Object[count];
			count = 0;
			c = 0;
			for(int i=0;i<followers.length;i++) {
				Object[] f = (Object[]) followers[i];
				if(likesOrComments.equals("likes")) {
					c = (int)f[1];
				}
				else if(likesOrComments.equals("comments")) {
					c = (int)f[2];
				}
				
				if(order.equals("down") && c == 0) {
					break;
				}
				else if(order.equals("up") && c != 0) {
					break;
				}
				onlyMost[count] = followers[i];
				count++;
			}
		}
		else {
			onlyMost = null;
		}
		
		if(onlyMost != null && onlyMost.length == 0) {
			onlyMost = null;
		}
		
		if(order.equals("up")) {
			if(onlyMost != null && onlyMost.length == followers.length) {
				onlyMost = null;
			}
		}
		return onlyMost;
		//System.out.println("");
	}

	public int getPostsCount() {
		return myPosts.size();
	}
	
	public int getFollowersCount() {
		if(followers != null) {
			return followers.length;
		}
		else {
			return 0;
		}
	}
	
	public int getFollowingCount() {
		if(following != null) {
			return following.length;
		}
		else {
			return 0;
		}
	}
	
	public long getLikes() {
		return likes;
	}
	
	public long getComments() {
		return comments;
	}
	
	public Object[] getNotFollowingYou() {
		if(!notFollowingYou.isEmpty()) {
			Object[] not = new Object[notFollowingYou.size()];
			int count = 0;
			for(Object nf : notFollowingYou) {
				not[count] = nf;
				count++;
			}
			return not;
		}
		else {
			return null;
		}
	}
	
	public Object[] getYouFollowingNot() {
		if(!youFollowingNot.isEmpty()) {
			Object[] you = new Object[youFollowingNot.size()];
			int count = 0;
			for(Object fy : youFollowingNot) {
				you[count] = fy;
				count++;
			}
			return you;
		}
		else {
			return null;
		}
	}
	
	public Object[] getMutual() {
		
		if(!mutual.isEmpty()) {
			Object[] m = new Object[mutual.size()];
			int count = 0;
			for(Object mo : mutual) {
				m[count] = mo;
				count++;
			}
			return m;
		}
		else {
			return null;
		}
	}
	
	public Object[] getOpenFriendRequestOut() {
		
		if(!openFriendRequestOut.isEmpty()) {
			Object[] o = new Object[openFriendRequestOut.size()];
			int count = 0;
			for(Object op : openFriendRequestOut) {
				o[count] = op;
				count++;
			}
			return o;
		}
		else {
			return null;
		}
		
	}
	
	
	public Object[] getOpenFriendRequestIn() {
		if(!openFriendRequestIn.isEmpty()) {
			Object[] i = new Object[openFriendRequestIn.size()];
			int count = 0;
			for(Object in : openFriendRequestIn) {
				i[count] = in;
				count++;
			}
			return i;
		}
		else {
			return null;
		}
	}

	
	
	public boolean getSessionIdValid() {
		return sessionIdValid;
	}

	
	public int getRequestsCount() {
		int count = 0;
		try {
			count = r.getRequestsCount();
		}
		catch(Exception e) {
		}
		return count;
	}
	
	public Object[] getData() {
		return data;
	}
}