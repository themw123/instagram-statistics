import java.io.IOException;
import java.util.List;
import org.json.JSONObject;
import okhttp3.Headers;
import okhttp3.Response;

public class InstagramLogin {
	
	private String password;
	private APIRequest rq;
	//headers
	private String XCSRFToken;
	//body
	private String enc_password;
	private String username;
	//loggedIn
	private String ds_user_id;
	private String sessionId;

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
			System.out.println("cant get XCSRFToken");
		}
	}
	
	private void enc_password() {
		long time = System.currentTimeMillis();
		enc_password = "#PWD_INSTAGRAM_BROWSER:0:" + time + ":" + password;
	}
	
	private void doLogin() {
		Response response = rq.doLogin(XCSRFToken, enc_password, username);
		
		boolean result = false;
		try {
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

			
		} catch (IOException e) {
			System.out.println("error in result of login");
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
	}
	
	public String[] getSession() {
		
		String[] session = new String[2];
		session[0] = ds_user_id;
		session[1] = sessionId;
		
		return session;
	}
}
