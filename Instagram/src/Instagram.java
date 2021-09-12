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

	private String email;
	private String password;
	private String sessionId;
	private String ds_user_id;
	
	private String[] following;
	private String[] followers;
	
	private Vector<String> notFollowingYou;
	private Vector<String> youFollowingNot;
	private Vector<String> OpenFriendRequestOut;
	private Vector<String> OpenFriendRequestIn;
	
	public Instagram(String email, String password, String sessionId, String ds_user_id) {
		this.email = email;
		this.password = password;
		this.sessionId = sessionId;
		this.ds_user_id = ds_user_id;
		setFollowingAndFollowers("following");
		setFollowingAndFollowers("followers");
		setNotFollowingYou();
		setYouFollowingNot();
		setOpenFriendRequestOut();
		setOpenFriendRequestIn();
	}
	
	public Instagram(String email, String password) {
		this.email = email;
		this.password = password;
		setSession();
		setFollowingAndFollowers("following");
		setFollowingAndFollowers("followers");
		setNotFollowingYou();
		setYouFollowingNot();
		setOpenFriendRequestOut();
		setOpenFriendRequestIn();
	}
	
	private void setSession() {
		//session id mit Instagram4j holen
		String[] session = null;
		try {
		Instagram4j instagram = Instagram4j.builder().username(email).password(password).build();
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
	
	
}
