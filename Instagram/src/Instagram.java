import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.Response;

public class Instagram{
	
	private Object[] data;
	
	private Logger logger;
	private LoggerFormat formatter;
	private ConsoleHandler handler;
	private Vector<String> prepareLog;
	private long time;
	
	private String chooseLoginprocess;
	private APIRequest r;
	private String username;
	private String password;
	private String sessionId;
	private String ds_user_id;
	private boolean sessionIdValid;
	
	private Object[][] following;
	private Object[][] followers;
	private Vector<Object[]> notFollowingYou;
	private Vector<Object[]> youFollowingNot;
	private Vector<Object[]> mutual;
	private Vector<String[]> openFriendRequestOut;
	private Vector<String[]> openFriendRequestIn;
	private Vector<Object[]> myPosts;
	
	private int playValue;
	private long likes;
	private long comments;	
	private double averageLikes;
	private double averageComments;
	private int realPostCount;
	private int realFollowersCount;
	private int realFollowingCount;
	private int reachedPostLikes;
	private int reachedPostComments;
	private int reachedLikes;
	private int reachedComments;

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
		login();
	}

	
	private void initialDatastructures() {
		logger = Logger.getLogger(Instagram.class.getName());
		logger.setLevel(Level.ALL);
		logger.setUseParentHandlers(false);
		formatter = new LoggerFormat();
		handler = new ConsoleHandler();
        handler.setFormatter(formatter);
		handler.setLevel(Level.ALL);
		logger.addHandler(handler);
		prepareLog = new Vector<String>();

		sessionIdValid = false;
		notFollowingYou = new Vector<Object[]>();
		youFollowingNot = new Vector<Object[]>();
		mutual = new Vector<Object[]>();
		openFriendRequestOut = new Vector<String[]>();
		openFriendRequestIn = new Vector<String[]>();
		myPosts = new Vector<Object[]>();
		
		playValue = 10;
		likes = 0;
		comments = 0;
		averageLikes = 0;
		averageComments = 0;
		realPostCount = 0;
		realFollowersCount = 0;
		realFollowingCount = 0;
		reachedPostLikes = 0;
		reachedPostComments = 0;
		reachedLikes = 0;
		reachedComments = 0;

		runThread8 = true;
		runThread9 = true;
		runThread10 = true;
	}
	
	public void setLogLevel(Level level) {
		logger.setLevel(level);
	}
	
	
	//alles neu in main machen
	public void start() {
		double time = 0;
		if(chooseLoginprocess.equals("session") || chooseLoginprocess.equals("login")) {
			login();
			if(sessionIdValid) {	
				//sessionid und ds_user_id in App abspeichern
				if(chooseLoginprocess.equals("login")) {
					logger.info("Login successful\n");
				}
				else if(chooseLoginprocess.equals("session")) {
					logger.info("Session valid\n");
				}
				if(username == null) {
					username = r.getUsername(ds_user_id);	
				}	
				if(setRealCounts()) {
					/*
					time = -System.currentTimeMillis();
					data();
					time = (time + System.currentTimeMillis())/1000;
					*/
				}
				setPrepareLog();
				
				int count = 0;
				String br = "";
				for(String e : prepareLog) {
					if(count == prepareLog.size()-1) {
						br = "\n";
					}
					logger.warning(e + br);
					count++;
				}
				logger.info("Requests total: " + getRequestsCount());
				logger.info("data took " + time + " seconds");
			}
			else {
				//In UI error anzeigen
				if(chooseLoginprocess.equals("login")) {
					logger.severe("Login failed");
				}
				else if(chooseLoginprocess.equals("session")) {
					logger.severe("Session error");
				}
				//login page fehlermeldung ausgeben
				if(sessionId == null) {
					logger.severe("Wrong password or username");
				}
				else if(sessionId.equals("two_factor_required")) {
					logger.severe("Please disable the two factor authentication in your instagram account settings. After you logged in in this App you can reactivate it.");
				}
			}
			
		}
		else {
			logger.severe("Wrong parameter in constructor");
		}
		
	}
	

	
	
	public void login() {
		if(chooseLoginprocess.equals("login")){
			logger.info("Trying to login ...");
			setSession();
		}
		
		this.r = new APIRequest(sessionId);
		//check if sessionId still works
		sessionIdValid = r.checkSessionId();
		
		if(sessionIdValid) {	
			if(chooseLoginprocess.equals("login")) {
				logger.info("Login successful\n");
			}
			else if(chooseLoginprocess.equals("session")) {
				logger.info("Session valid\n");
			}
			if(username == null) {
				username = r.getUsername(ds_user_id);	
			}	
		}
		else {
			//In UI error anzeigen
			if(chooseLoginprocess.equals("login")) {
				logger.severe("Login failed");
			}
			else if(chooseLoginprocess.equals("session")) {
				logger.severe("Session error");
			}
			//login page fehlermeldung ausgeben
			if(sessionId == null) {
				logger.severe("Wrong password or username");
			}
			else if(sessionId.equals("two_factor_required")) {
				logger.severe("Please disable the two factor authentication in your instagram account settings. After you logged in in this App you can reactivate it.");
			}
		}
		
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
						
		try {			
			Response response = r.doRequest(url);

			String output = response.body().string();
			JSONObject jsonObj = new JSONObject(output);
			error = output;
			realPostCount = jsonObj.getJSONObject("graphql").getJSONObject("user").getJSONObject("edge_owner_to_timeline_media").getInt("count");
			realFollowingCount = jsonObj.getJSONObject("graphql").getJSONObject("user").getJSONObject("edge_follow").getInt("count");
			realFollowersCount = jsonObj.getJSONObject("graphql").getJSONObject("user").getJSONObject("edge_followed_by").getInt("count");
		}catch(Exception e) {
			logger.warning("setRealCounts failed -> "  + error);
			success = false;
		}
		return success;
    }
	
    public void page1(){
		
    	time = -System.currentTimeMillis();    	
    	setRealCounts();
    	
		Thread t1 = new Thread(() -> setFollowingAndFollowers("following"));
		Thread t2 = new Thread(() -> setFollowingAndFollowers("followers"));
		Thread t3 = new Thread(() -> setNotFollowingYou());
		Thread t4 = new Thread(() -> setYouFollowingNot());
		Thread t5 = new Thread(() -> setOpenFriendRequestOut());
		Thread t6 = new Thread(() -> setOpenFriendRequestOutExtras());
		Thread t7 = new Thread(() -> setOpenFriendRequestIn());
		Thread t8 = new Thread(() -> setMyPosts());

		
		logger.info("Threads:page1 running");

    	//System.out.println("Data1-Thread running");
		t1.start();
    	//System.out.println("Data2-Thread running");
		t2.start();
    	//System.out.println("Data6-Thread running");
		t5.start();
    	//System.out.println("Data6-Thread running");
		t7.start();
    	//System.out.println("Data7-Thread running");
		t8.start();
		
		
		
		//load Main UI and show waiting symbols on general and second page
		
		
		
		
		//Auf following/follower warten. Bei fail, nur Thread 5,6 und 7 starten
		try {
			t1.join();
			t2.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		

		try {
			if((this.following.length != 0 || this.followers.length != 0) && getFollowersCount()+playValue >= realFollowersCount && getFollowingCount()+playValue >= realFollowingCount) {
		    	//System.out.println("Data3-Thread running");
				t3.start();
				//System.out.println("Data4-Thread running");
				t4.start();
				
				t3.join();
				t4.join();
			}
			t5.join();
			if(openFriendRequestOut.size() != 0) {
				t6.start();
			}
			t7.join();
			t8.join();
			if(openFriendRequestOut.size() != 0) {
				try {
					t6.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		logger.info("Threads:page1 finished");

	

    }
    
    
    public void page2(){
    	setRealCounts();
    	logger.info("Thread:page2 running");
		setMyPosts();
    	logger.info("Thread:page2 finished");
    }
    
    public void page3(){
    	setRealCounts();
		if(this.followers != null && this.followers.length != 0 && getFollowersCount()+playValue >= realFollowersCount && !myPosts.isEmpty()) {
			Thread t1 = new Thread(() -> setMostLikedOrCommentedByFollowers("liker"));
			Thread t2 = new Thread(() -> setMostLikedOrCommentedByFollowers("commenter"));
			//System.out.println("Data8-Thread running");
	    	logger.info("Threads:page3 running");
			t1.start();
		    //System.out.println("Data9-Thread running");
			t2.start();
			
			try {
				t1.join();
				t2.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			setLikerAndCommenterPostCountsIfNull();
	    	logger.info("Threads:page3 finished");
		}
		
		setPrepareLog();
    }
    
    private void data(){


		
	   	/*
	   	data = new Object[20];
    	data[0] = postsCount;
    	data[1] = followers;
    	data[2] = following;
    	data[3] = likes;
    	data[4] = comments;
    	data[5] = averageLikes;
    	data[6] = averageComments;
    	data[7] = notFollowingYou;
    	data[8] = youFollowingNot;
    	data[9] = mutual;
    	data[10] = openFriendRequestIn;
    	data[11] = mostLikesPosts;
    	data[12] = mostCommentsPosts;
    	data[13] = leastLikesPosts;
    	data[14] = leastCommentsPosts;
    	data[15] = OpenFriendRequestOut;
    	data[16] = mostLikesFrom;
    	data[17] = mostCommentsFrom;
    	data[18] = leastLikesFrom;
    	data[19] = leastCommentsFrom;	 
    	*/  	
	}
    
  
	private void setFollowingAndFollowers(String urlParameter) {
		int count = 1000000;
		String error = null;
		int realCount = 0;
		
		String url = "https://i.instagram.com/api/v1/friendships/"+ ds_user_id + "/" + urlParameter + "/?count=" + count + "";
				
		try {
			Response response = r.doRequest(url);
			
			String output = response.body().string();
			JSONObject jsonObj = new JSONObject(output);
			error = output;

			
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
			count = 0;
			if(urlParameter.equals("following")) {
				count = getFollowingCount();
				realCount = realFollowingCount;
			}
			else if(urlParameter.equals("followers")) {
				count = getFollowersCount();
				realCount = realFollowersCount;
			}
			
			prepareLog.add("setFollowingAndFollowers " + urlParameter + " failed -> " + count + " from " + realCount + " -> "  + error); 
		}
		
		
		count = 0;
		realCount = 0;
		boolean exist = false;
		String methodName = null;
		
		if(urlParameter.equals("following")) {
			count = getFollowingCount();
			realCount = realFollowingCount;
			methodName = "setFollowingAndFollowers following";
			
		}
		else if(urlParameter.equals("followers")) {
			count = getFollowersCount();
			realCount = realFollowersCount;
			methodName = "setFollowingAndFollowers followers";
			
		}
		
		for(String e : prepareLog) {
			if(e.contains(methodName)) {
				exist = true;
				break;
			}
		}
		if(!exist && count+10 < realCount) {
			prepareLog.add("setFollowingAndFollowers failed -> " + count + " from " + realCount + " -> you have got too much " + urlParameter); 
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
						
			try {
				Response response = r.doRequest(url);
				String output = response.body().string();
				JSONObject jsonObj = new JSONObject(output);
				error = output;	
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
				prepareLog.add("setOpenFriendRequestOut failed -> Durchlauf: " + durchlauf + " -> " + error); 
				break;
			}
			durchlauf++;
		}while(cursor != null);
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
				//logger.info("Not yet. Still waiting for termination"); 
			}
		} catch (InterruptedException e) {
			logger.warning("Waiting for Threads failed."); 
			//e.printStackTrace();
		}

	}
	
	private void openFriendRequestOutExtras(String username, int i) {
		
		if(runThread8) {
			
			String error = "";
			String url = "https://www.instagram.com/" + username + "/?__a=1";
			try {
				Response response = r.doRequest(url);
				String output = response.body().string();
				JSONObject jsonObj = new JSONObject(output);
				error = output;
				String id = jsonObj.getJSONObject("graphql").getJSONObject("user").getString("id");
				String picture = jsonObj.getJSONObject("graphql").getJSONObject("user").getString("profile_pic_url_hd");
	
				openFriendRequestOut.get(i)[1] = id;
				openFriendRequestOut.get(i)[2] = picture;
								
			} catch (Exception e) {
				runThread8 = false;
				
				if(error.contains("message")) {
					prepareLog.add("getOpenFriendRequestOutIds failed -> " + error); 
				}
			}
		
		}
	}
	
	
	private void setOpenFriendRequestIn() {

		String error = null;
		String url = "https://i.instagram.com/api/v1/friendships/pending/";
		
		try {
			Response response = r.doRequest(url);
			String output = response.body().string();
			JSONObject jsonObj = new JSONObject(output);
			error = output;
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
			prepareLog.add("setOpenFriendRequestIn failed -> " + error); 
		}
		//System.out.println("Data6-Thread finished");

	}
	
	
	
	private void setMyPosts() {
				
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
		
		int count = 100000;
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
			
			
				
			try {		
				Response response = r.doRequest(url);
				String output = response.body().string();
				JSONObject jsonObj = new JSONObject(output);
				error = output;
				
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
					durchlauf = 12 + ((durchlauf-1) * 50);  
				}
				
				//In UI fehler anzeigen(bla von bla posts).
				prepareLog.add("setMyPosts failed -> post " + durchlauf + " from " + realPostCount + " -> " + error); 
				break;
			}
					
			durchlauf++;
				
		}while(has_next_page.equals("true"));		

		averageLikes =  Math.round(((double) likes / myPosts.size()) * 100.0) / 100.0;
		averageComments = Math.round(((double) comments / myPosts.size()) * 100.0) / 100.0;
		
		
		boolean exist = false;
		for(String e : prepareLog) {
			if(e.contains("setMyPosts")) {
				exist = true;
				break;
			}
		}
		if(!exist && myPosts.size() < realPostCount) {
			if (durchlauf == 1) {
				durchlauf = 12;
			}
			else if(durchlauf > 1){
				durchlauf = 13 + ((durchlauf-2) * 50);  
			}
			prepareLog.add("setMyPosts failed -> post " + durchlauf + " from " + realPostCount + " -> a maximum about 600 Posts possible"); 
		}
		
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
				//logger.info("Not yet. Still waiting for termination"); 
			}
		} catch (InterruptedException e) {
			logger.warning("Waiting for Threads failed."); 
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
			
			try {
			
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
					error = output;
						
						
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
		
					}while(has_next_page.equals("true"));
				
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
						prepareLog.add("setMostLikedByFollowers failed -> maximum of posts analysed -> " + error); 
					}
					else if(likerOrCommenter.equals("commenter")) {
						prepareLog.add("setMostCommentedByFollowers failed ->  maximum of posts analysed -> " + error); 
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
	
	
	

	
	
	
	

	
	
	private void setLikerAndCommenterPostCountsIfNull() {
		
		boolean allNull = true;
		for(Object[] f : followers) {
			if((int)f[1] != 0) {
				allNull = false;
				break;
			}
		}
		if(allNull) {
			reachedPostLikes = 0;
		}
		

		allNull = true;
		for(Object[] f : followers) {
			if((int)f[2] != 0) {
				allNull = false;
				break;
			}
		}
		if(allNull) {
			reachedPostComments = 0;
		}
	}
	
	
	
	
	public void setPrepareLog() {
		
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
		
		for(int i=0;i<prepareLog.size();i++) {
			String error = prepareLog.get(i);
			if(error.contains("setMostLikedByFollowers")) {
				if(print1) {
					print1 = false;
				}
				else {
					prepareLog.remove(i);
					i--;
				}
			}
			else if(error.contains("setMostCommentedByFollowers")) {
				if(print2) {
					print2 = false;
				}
				else {
					prepareLog.remove(i);
					i--;
				}
			}
			else if(error.contains("getOpenFriendRequestOutIds")) {
				if(print3) {
					print3 = false;
				}
				else {
					prepareLog.remove(i);
					i--;
				}
			}
			else if(error.contains("setMyPosts")) {
				if(print4) {
					print4 = false;
				}
				else {
					prepareLog.remove(i);
					i--;
				}
			}
		}
		
		
		for(int i=0;i<prepareLog.size();i++) {
			if(prepareLog.get(i).contains("setMostLikedByFollowers")) {
				String s = prepareLog.get(i);
				String beg = s.substring(0, s.indexOf("posts"));
				String end = s.substring(s.indexOf("posts"), s.length());
				s = beg + reachedPostLikes + " " + end;
				prepareLog.set(i, s);
			}
			else if(prepareLog.get(i).contains("setMostCommentedByFollowers")) {
				String s = prepareLog.get(i);
				String beg = s.substring(0, s.indexOf("posts"));
				String end = s.substring(s.indexOf("posts"), s.length());
				s = beg + reachedPostComments + " " + end;
				prepareLog.set(i, s);

			}
		}
		
		
		int count = 0;
		String br = "";
		for(String e : prepareLog) {
			if(count == prepareLog.size()-1) {
				br = "\n";
			}
			logger.warning(e + br);
			count++;
		}
		time = (time + System.currentTimeMillis())/1000;
		logger.info("Requests total: " + getRequestsCount());
		logger.info("data took " + time + " seconds");
		
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

	public long getRealFollowersCount() {
		return realFollowersCount;
	}
	
	public long getRealFollowingCount() {
		return realFollowingCount;
	}
	
	public long getRealPostCount() {
		return realPostCount;
	}
	
	public long getReachedPostComments() {
		return reachedPostComments;
	}
	
	public long getReachedPostLikes() {
		return reachedPostLikes;
	}
	
	public Vector<String>getPrepareLog() {
		return prepareLog;
	}
	
	public long getPlayValue() {
		return playValue;
	}
	
	public long getLikes() {
		return likes;
	}
	
	public long getComments() {
		return comments;
	}
	
	public double getAverageLikes() {
		return averageLikes;
	}
	
	public double getAverageComments() {
		return averageComments;
	}
	
	public boolean getSessionIdValid() {
		return sessionIdValid;
	}
	
	public Object[] getData() {
		return data;
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


	
	public int getRequestsCount() {
		int count = 0;
		try {
			count = r.getRequestsCount();
		}
		catch(Exception e) {
		}
		return count;
	}
	
}