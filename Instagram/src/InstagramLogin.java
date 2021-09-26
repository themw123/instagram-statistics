import java.io.IOException;

import org.json.JSONObject;

import okhttp3.Response;

public class InstagramLogin {
	
	private String username;
	private String password;
	private APIRequest rq;
	//headers
	private String XCSRFToken;
	private String ContentType;
	private String ContentLength;
	private String header;
	//body
	private String enc_password;
	
	public InstagramLogin(String username, String password) {
		rq = new APIRequest("");
		this.username = username;
		this.password = password;
		XCSRFToken();
		enc_password();
		doLogin();
	}
	
	private void XCSRFToken() {
		Response response = rq.XCSRFToken();
		try {
			String output = response.body().string();
			JSONObject jsonObj = new JSONObject(output);
			XCSRFToken = jsonObj.getJSONObject("config").get("csrf_token").toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void enc_password() {
		long time = System.currentTimeMillis();
		enc_password = "#PWD_INSTAGRAM_BROWSER:0:" + time + ":" + password;
	}
	
	private void doLogin() {
		Response response = rq.doLogin(XCSRFToken, enc_password, username);
		try {
			String output = response.body().string();
			JSONObject jsonObj = new JSONObject(output);
			XCSRFToken = jsonObj.getJSONObject("config").get("csrf_token").toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
