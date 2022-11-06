package Instagram;
import java.util.List;
import java.util.Random;

import org.json.JSONObject;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class InstagramLogin {
	
	private String password;
	//headers
	private String XCSRFToken;
	//body
	private String enc_password;
	private String username;
	//loggedIn
	private String ds_user_id;
	private String sessionId;

	public InstagramLogin(String username, String password) {
		this.username = username;
		this.password = password;
		XCSRFToken();
		enc_password();
		doLogin();
	}
	
	private void XCSRFToken() {
			
		Response response = null;
		String userAgent = getUserAgent(2);

		OkHttpClient client = new OkHttpClient().newBuilder()
				.build();
		Request request = new Request.Builder()
				.url("https://www.instagram.com/data/shared_data/")
				.method("GET", null)
				.addHeader("User-Agent", userAgent)
				.build();
		try {
			response = client.newCall(request).execute();
			String output = response.body().string();
			JSONObject jsonObj = new JSONObject(output);
			XCSRFToken = jsonObj.getJSONObject("config").getString("csrf_token");
		} catch (Exception e) {
			// e.printStackTrace();

		}
	
	}
	
	private void enc_password() {
		long time = System.currentTimeMillis();
		enc_password = "#PWD_INSTAGRAM_BROWSER:0:" + time + ":" + password;
	}
	
	private void doLogin() {
		Response response = null;
		String userAgent = getUserAgent(1);

		try {
			OkHttpClient client = new OkHttpClient().newBuilder()
					.build();
			MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
			@SuppressWarnings("deprecation")
			RequestBody body = RequestBody.create(mediaType, "enc_password=" + enc_password + "&username=" + username);
			Request request = new Request.Builder()
					.url("https://www.instagram.com/api/v1/web/accounts/login/ajax/")
					.method("POST", body)
					.addHeader("X-CSRFToken", XCSRFToken)
					.addHeader("User-Agent", userAgent)
					.build();

			response = client.newCall(request).execute();
			boolean result = false;
			String output = response.body().string();
			JSONObject jsonObj = new JSONObject(output);
				
			if(jsonObj.toString().contains("authenticated")) {
				result = (boolean) jsonObj.get("authenticated");
				if(result) {
					result = true;
				}
				else {
					ds_user_id = null;
					sessionId = null;
				}
			}
			else if(jsonObj.toString().contains("two_factor_required")) {
				result = false;
				ds_user_id = "two_factor_required";
				sessionId = "two_factor_required";
			}
			if(result) { 
				Headers headers = response.headers();
				List<String> cookieList = headers.toMultimap().get("set-cookie");
				for(String s : cookieList) {
					if(s.contains("ds_user_id")) {
						ds_user_id = s.substring(s.indexOf("=")+1, s.indexOf(";"));
					}
					else if(s.contains("sessionid")) {
						sessionId = s.substring(s.indexOf("=")+1, s.indexOf(";"));
					}
				}
			}
		} catch (Exception e) {
			// e.printStackTrace();
		}
		
	}
	
	public String[] getSession() {
		
		String[] session = new String[2];
		session[0] = ds_user_id;
		session[1] = sessionId;
		
		return session;
	}
	
	
	
	
	
	
	
	private String getUserAgent(int zahl) {
		Random r = new Random();
		String userAgent = null;
		if (zahl == 1) {
			int zahl1 = r.nextInt(101);
			int zahl2 = r.nextInt(11);
			int zahl3 = r.nextInt(51);
			userAgent = "Instagram " + zahl1 + ".0.3." + zahl2 + ".100 Android (" + zahl3
					+ "/6.4.1; 558dpi; 1440x2560; LGE; LG-E525f; vee3e; en_US";
		} else if (zahl == 2) {
			int zahl1 = r.nextInt(11);
			int zahl2 = r.nextInt(11);
			int zahl3 = r.nextInt(11);
			int zahl4 = r.nextInt(101);
			userAgent = "Mozilla/" + zahl1 + ".0 (Linux; Android " + zahl2 + ".0; Nexus " + zahl3
					+ " Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/" + zahl4
					+ ".0.4577.82 Mobile Safari/537.36";
		}
		return userAgent;
	}
	
}
