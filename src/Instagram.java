import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.Response;

public class Instagram {

	private Logger logger;
	private LoggerFormat formatter;
	private ConsoleHandler handler;
	private ArrayList<String> prepareLog;
	private long time;

	private String chooseLoginprocess;
	private APIRequest r;
	private String username;
	private String password;
	private String sessionId;
	private String ds_user_id;
	private boolean sessionIdValid;

	private ArrayList<Person> following;
	private ArrayList<Person> followers;
	private ArrayList<Person> notFollowingYou;
	private ArrayList<Person> youFollowingNot;
	private ArrayList<Person> mutual;
	private ArrayList<Person> openFriendRequestIn;
	private ArrayList<Post> myPosts;

	private int playValue;
	private long likes;
	private long comments;
	private double averageLikes;
	private double averageComments;
	private int realPostCount;
	private int realFollowersCount;
	private int realFollowingCount;

	public Instagram(String chooseLoginprocess, String data1, String data2) {

		this.chooseLoginprocess = chooseLoginprocess;

		if (chooseLoginprocess.equals("session")) {
			this.sessionId = data1;
			this.ds_user_id = data2;
		}

		else if (chooseLoginprocess.equals("login")) {
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
		prepareLog = new ArrayList<String>();

		sessionIdValid = false;
		playValue = 10;

		prepareLog = new ArrayList<String>();
		following = new ArrayList<Person>();
		followers = new ArrayList<Person>();
		likes = 0;
		comments = 0;
		notFollowingYou = new ArrayList<Person>();
		youFollowingNot = new ArrayList<Person>();
		mutual = new ArrayList<Person>();
		openFriendRequestIn = new ArrayList<Person>();
		myPosts = new ArrayList<Post>();

	}

	public void setLogLevel(Level level) {
		logger.setLevel(level);
	}

	public void login() {

		if (chooseLoginprocess.equals("session") || chooseLoginprocess.equals("login")) {

			if (chooseLoginprocess.equals("login")) {
				logger.info("Trying to login ...");
				setSession();
			}

			this.r = new APIRequest(sessionId);
			// check if sessionId still works
			sessionIdValid = r.checkSessionId();

			if (sessionIdValid) {
				if (chooseLoginprocess.equals("login")) {
					logger.info("Login successful\n");
				} else if (chooseLoginprocess.equals("session")) {
					logger.info("Session valid\n");
				}
				if (username == null) {
					username = r.getUsername(ds_user_id);
				}
			} else {
				if (chooseLoginprocess.equals("login")) {
					logger.severe("Login failed");
				} else if (chooseLoginprocess.equals("session")) {
					logger.severe("Session error");
				}
				if (sessionId == null) {
					logger.severe("Wrong password or username");
				} else if (sessionId.equals("two_factor_required")) {
					logger.severe(
							"Please disable the two factor authentication in your instagram account settings. After you logged in in this App you can reactivate it.");
				}
			}
		} else {
			logger.severe("Wrong parameter in constructor");
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
		String url = "https://i.instagram.com/api/v1/users/web_profile_info/?username=" + username;

		try {
			Response response = r.doRequest(url, ds_user_id);

			String output = response.body().string();
			JSONObject jsonObj = new JSONObject(output);
			error = output;
			realPostCount = jsonObj.getJSONObject("data").getJSONObject("user")
					.getJSONObject("edge_owner_to_timeline_media").getInt("count");
			realFollowingCount = jsonObj.getJSONObject("data").getJSONObject("user").getJSONObject("edge_follow")
					.getInt("count");
			realFollowersCount = jsonObj.getJSONObject("data").getJSONObject("user").getJSONObject("edge_followed_by")
					.getInt("count");
		} catch (Exception e) {
			e.printStackTrace();

			logger.warning("setRealCounts failed -> " + error);
			success = false;
		}
		return success;
	}

	public void doWork() {

		if (!sessionIdValid) {
			logger.warning("you can not do this. You are not logged in.");
			return;
		}

		time = -System.currentTimeMillis();

		if (!setRealCounts()) {
			return;
		}

		prepareLog = new ArrayList<String>();
		following = new ArrayList<Person>();
		followers = new ArrayList<Person>();
		likes = 0;
		comments = 0;
		notFollowingYou = new ArrayList<Person>();
		youFollowingNot = new ArrayList<Person>();
		mutual = new ArrayList<Person>();
		openFriendRequestIn = new ArrayList<Person>();
		myPosts = new ArrayList<Post>();

		Thread t1 = new Thread(() -> setFollowingAndFollowers("following"));
		Thread t2 = new Thread(() -> setFollowingAndFollowers("followers"));
		Thread t3 = new Thread(() -> setNotFollowingYou());
		Thread t4 = new Thread(() -> setYouFollowingNot());
		Thread t5 = new Thread(() -> setOpenFriendRequestIn());
		Thread t6 = new Thread(() -> setMyPosts());

		logger.info("Threads: running");

		t1.start();
		t2.start();
		t5.start();
		t6.start();

		try {
			t1.join();
			t2.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		try {
			if ((following != null && followers != null && following.size() != 0 || followers.size() != 0)
					&& getFollowersCount() + playValue >= realFollowersCount
					&& getFollowingCount() + playValue >= realFollowingCount) {
				t3.start();
				t4.start();

				t3.join();
				t4.join();
			}
			t5.join();
			t6.join();

		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		logger.info("Threads: finished");
		setPrepareLog();

	}

	private void setFollowingAndFollowers(String urlParameter) {

		int count = 1000000;
		String error = null;
		int realCount = 0;
		boolean has_next_page = false;
		String next_max_id_String = "";
		int next_max_id_int = 0;


		do {
			

			String url = "";
			if (has_next_page) {
				if(urlParameter.equals("followers"))
				url = "https://i.instagram.com/api/v1/friendships/" + ds_user_id + "/" + urlParameter + "/?count="
						+ count + "&max_id=" + next_max_id_String;
				if(urlParameter.equals("following"))
				url = "https://i.instagram.com/api/v1/friendships/" + ds_user_id + "/" + urlParameter + "/?count="
						+ count + "&max_id=" + next_max_id_int;
			} else {
				url = "https://i.instagram.com/api/v1/friendships/" + ds_user_id + "/" + urlParameter + "/?count="
						+ count + "";
			}

			try {
				Response response = r.doRequest(url, ds_user_id);

				String output = response.body().string();
				JSONObject jsonObj = new JSONObject(output);
				error = output;

				String nextString = "";
				int nextInt = 0;
				try {
					if(urlParameter.equals("followers")) {
						nextString = jsonObj.getString("next_max_id");
						next_max_id_String = nextString;
					}
					if(urlParameter.equals("following")) {
						nextInt = jsonObj.getInt("next_max_id");
						next_max_id_int = nextInt;
					}
					has_next_page = true;
				} catch (Exception e) {
					has_next_page = false;
				}

				JSONArray ja_users = jsonObj.getJSONArray("users");
				int length = ja_users.length();

				for (int i = 0; i < length; i++) {
					JSONObject userJson = ja_users.getJSONObject(i);
					String username = userJson.getString("username");
					String picture = userJson.getString("profile_pic_url");
					long id = userJson.getLong("pk");

					Person p = new Person(id, username, picture);

					if (urlParameter.equals("following")) {
						following.add(p);
					} else if (urlParameter.equals("followers")) {
						followers.add(p);
					}
				}

			} catch (Exception e) {
				// e.printStackTrace();
				count = 0;
				if (urlParameter.equals("following")) {
					count = getFollowingCount();
					realCount = realFollowingCount;
				} else if (urlParameter.equals("followers")) {
					count = getFollowersCount();
					realCount = realFollowersCount;
				}

				prepareLog.add("setFollowingAndFollowers " + urlParameter + " failed -> " + count + " from " + realCount
						+ " -> " + error);
				break;
			}
		} while (has_next_page);

		count = 0;
		realCount = 0;
		boolean exist = false;
		String methodName = null;

		if (urlParameter.equals("following")) {
			count = getFollowingCount();
			realCount = realFollowingCount;
			methodName = "setFollowingAndFollowers following";

		} else if (urlParameter.equals("followers")) {
			count = getFollowersCount();
			realCount = realFollowersCount;
			methodName = "setFollowingAndFollowers followers";

		}

		for (String e : prepareLog) {
			if (e.contains(methodName)) {
				exist = true;
				break;
			}
		}
		if (!exist && count + playValue < realCount) {
			prepareLog.add("setFollowingAndFollowers failed -> " + count + " from " + realCount
					+ " -> you have got too much " + urlParameter);
		}

	}

	private void setNotFollowingYou() {
		boolean drin = false;
		for (Person p1 : following) {
			long followingId = p1.getId();
			for (Person p2 : followers) {
				long followersId = p2.getId();
				if (followingId == followersId) {
					drin = true;
					mutual.add(p2);
					break;
				}
			}
			if (!drin) {
				notFollowingYou.add(p1);
			}
			drin = false;
		}

	}

	private void setYouFollowingNot() {

		boolean drin = false;

		for (Person p1 : followers) {
			long followersId = p1.getId();
			for (Person p2 : following) {
				long followingId = p2.getId();
				if (followingId == followersId) {
					drin = true;
					break;
				}
			}
			if (!drin) {
				youFollowingNot.add(p1);
			}
			drin = false;
		}

	}

	private void setOpenFriendRequestIn() {

		String error = null;
		String url = "https://i.instagram.com/api/v1/friendships/pending/";

		try {
			Response response = r.doRequest(url, ds_user_id);
			String output = response.body().string();
			JSONObject jsonObj = new JSONObject(output);
			error = output;
			JSONArray jsonArr = jsonObj.getJSONArray("users");

			int length = jsonArr.length();
			for (int i = 0; i < length; i++) {
				JSONObject userJson = jsonArr.getJSONObject(i);
				String username = userJson.getString("username");
				String picture = userJson.getString("profile_pic_url");
				String id = userJson.get("pk").toString();

				Person p = new Person(id, username, picture);
				openFriendRequestIn.add(p);
			}

		} catch (Exception e) {
			// e.printStackTrace();
			prepareLog.add("setOpenFriendRequestIn failed -> " + error);
		}

	}

	private void setMyPosts() {

		// achtung nach 1 jahr musste ds_user_id in cookie mitangegeben werden, sonnst
		// funktionierten die weiteren iterationen nicht

		/*
		 * query_id:
		 * 17851374694183129 = posts for tags
		 * 17874545323001329 = user following
		 * 17851374694183129 = user followers
		 * 17888483320059182 = user posts
		 * 17864450716183058 = likes on posts
		 * 17852405266163336 = comments on posts
		 * 17842794232208280 = posts on feed
		 * 17847560125201451 = feed profile suggestions
		 * 17863787143139595 = post suggestions
		 */

		int count = 100000;
		String has_next_page = "false";
		String end_cursor = null;
		String error = null;
		int durchlauf = 0;

		do {
			String url = "";
			if (has_next_page.equals("false")) {
				url = "https://i.instagram.com/api/v1/users/web_profile_info/?username=" + username;
			} else if (has_next_page.equals("true")) {
				url = "https://www.instagram.com/graphql/query/?query_id=17888483320059182&variables={\"id\":\""
						+ ds_user_id + "\",\"first\":" + count + ",\"after\":\"" + end_cursor + "\"}";
			}

			try {
				Response response = r.doRequest(url, ds_user_id);
				String output = response.body().string();
				JSONObject jsonObj = new JSONObject(output);
				error = output;

				if (has_next_page.equals("false")) {
					jsonObj = jsonObj.getJSONObject("data").getJSONObject("user")
							.getJSONObject("edge_owner_to_timeline_media");
				} else {
					jsonObj = jsonObj.getJSONObject("data").getJSONObject("user")
							.getJSONObject("edge_owner_to_timeline_media");
				}

				has_next_page = jsonObj.getJSONObject("page_info").get("has_next_page").toString();

				if (has_next_page.equals("true")) {
					end_cursor = jsonObj.getJSONObject("page_info").get("end_cursor").toString();
				}

				JSONArray jsonArr = jsonObj.getJSONArray("edges");
				int length = jsonArr.length();
				for (int i = 0; i < length; i++) {
					int likes = 0;
					int comments = 0;

					JSONObject post = jsonArr.getJSONObject(i).getJSONObject("node");

					try {
						likes = Integer.parseInt(post.getJSONObject("edge_liked_by").get("count").toString());
					} catch (Exception e) {
						likes = Integer.parseInt(post.getJSONObject("edge_media_preview_like").get("count").toString());
					}
					comments = Integer.parseInt(post.getJSONObject("edge_media_to_comment").get("count").toString());

					String shortcode = post.getString("shortcode");

					String display_url = post.getString("display_url");

					Post p = new Post(shortcode, likes, comments, display_url);
					this.likes = this.likes + likes;
					this.comments = this.comments + comments;
					myPosts.add(p);
				}
			} catch (Exception e) {
				// e.printStackTrace();
				if (durchlauf == 1) {
					durchlauf = 12;
				} else if (durchlauf > 1) {
					durchlauf = 12 + ((durchlauf - 1) * 50);
				}

				prepareLog.add("setMyPosts failed -> post " + durchlauf + " from " + realPostCount + " -> error");
				break;
			}

			durchlauf++;

		} while (has_next_page.equals("true"));

		averageLikes = Math.round(((double) likes / myPosts.size()) * 100.0) / 100.0;
		averageComments = Math.round(((double) comments / myPosts.size()) * 100.0) / 100.0;

		boolean exist = false;
		for (String e : prepareLog) {
			if (e.contains("setMyPosts")) {
				exist = true;
				break;
			}
		}
		if (!exist && myPosts.size() + playValue < realPostCount) {
			if (durchlauf == 1) {
				durchlauf = 12;
			} else if (durchlauf > 1) {
				durchlauf = 13 + ((durchlauf - 2) * 50);
			}
			prepareLog.add("setMyPosts failed -> post " + durchlauf + " from " + realPostCount
					+ " -> a maximum about 600 Posts possible");
		}

	}

	public void setPrepareLog() {

		boolean print1 = true;
		boolean print2 = true;
		boolean print3 = true;
		boolean print4 = true;

		for (int i = 0; i < prepareLog.size(); i++) {
			String error = prepareLog.get(i);
			if (error.contains("setMostLikedByFollowers")) {
				if (print1) {
					print1 = false;
				} else {
					prepareLog.remove(i);
					i--;
				}
			} else if (error.contains("setMostCommentedByFollowers")) {
				if (print2) {
					print2 = false;
				} else {
					prepareLog.remove(i);
					i--;
				}
			} else if (error.contains("getOpenFriendRequestOutIds")) {
				if (print3) {
					print3 = false;
				} else {
					prepareLog.remove(i);
					i--;
				}
			} else if (error.contains("setMyPosts")) {
				if (print4) {
					print4 = false;
				} else {
					prepareLog.remove(i);
					i--;
				}
			}
		}

		int count = 0;
		String br = "";
		for (String e : prepareLog) {
			if (count == prepareLog.size() - 1) {
				br = "\n";
			}
			logger.warning(e + br);
			count++;
		}
		time = (time + System.currentTimeMillis()) / 1000;
		logger.info("Requests total: " + getRequestsCount());
		logger.info("data took " + time + " seconds");

	}

	public ArrayList<Post> getPosts(String likesOrcomments, String order) {

		ArrayList<Post> orderedPosts = (ArrayList<Post>) myPosts.clone();

		if (likesOrcomments.equals("likes")) {
			if (order.equals("up")) {
				Collections.sort(orderedPosts, (o1, o2) -> o1.getLikes() - o2.getLikes());
			} else if (order.equals("down")) {
				Collections.sort(orderedPosts, (o1, o2) -> o2.getLikes() - o1.getLikes());
			}
		} else if (likesOrcomments.equals("comments")) {
			if (order.equals("up")) {
				Collections.sort(orderedPosts, (o1, o2) -> o1.getComments() - o2.getComments());
			} else if (order.equals("down")) {
				Collections.sort(orderedPosts, (o1, o2) -> o2.getComments() - o1.getComments());
			}
		}
		return orderedPosts;

	}

	// getter

	public ArrayList<Post> getUnorderedPosts() {
		return myPosts;
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

	public ArrayList<String> getPrepareLog() {
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

	public int getPostsCount() {
		if (myPosts != null) {
			return myPosts.size();
		} else {
			return 0;
		}
	}

	public int getFollowersCount() {
		if (followers != null) {
			return followers.size();
		} else {
			return 0;
		}
	}

	public int getFollowingCount() {
		if (following != null) {
			return following.size();
		} else {
			return 0;
		}
	}

	public ArrayList<Person> getNotFollowingYou() {
		return this.notFollowingYou;
	}

	public ArrayList<Person> getYouFollowingNot() {
		return this.youFollowingNot;
	}

	public ArrayList<Person> getMutual() {
		return this.mutual;
	}

	public ArrayList<Person> getOpenFriendRequestIn() {
		return this.openFriendRequestIn;
	}

	public int getRequestsCount() {
		int count = 0;
		try {
			count = r.getRequestsCount();
		} catch (Exception e) {
		}
		return count;
	}

}