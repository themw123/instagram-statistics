import java.io.IOException;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class APIRequest {

	private OkHttpClient client;
	private String sessionId;

	APIRequest() {
		this.client = new OkHttpClient().newBuilder().build();
	}
	
	APIRequest(String sessionId) {
		this.client = new OkHttpClient().newBuilder().build();
		this.sessionId = sessionId;
	}
	
	
	public Response doRequest(String url) {
		Response response = null;
		
		Request request = new Request.Builder()
			.url(url)
			.method("GET", null)
			.addHeader("X-IG-App-ID", "936619743392459")
			.addHeader("Cookie", "sessionid=" + sessionId)
			.build();

		try {
			response = client.newCall(request).execute();
		} catch (IOException e) {
			//System.out.println("Error in Request");
			//e.printStackTrace()
		}
		return response;
	}
	
	public boolean checkSessionId(String url) {
		
		boolean sessionIdValid = false;
		
		Request request = new Request.Builder()
			.url(url)
			.method("GET", null)
			.addHeader("X-IG-App-ID", "936619743392459")
			.addHeader("Cookie", "sessionid=" + sessionId)
			.build();
		
		try {
			Response response = client.newCall(request).execute();
			if(response.code() == 200) {
				sessionIdValid = true;
			}
		} catch (IOException e) {
			System.out.println();
			//e.printStackTrace(
		}
		return sessionIdValid;
	}
	
	public Response XCSRFToken() {
		Response response = null;
		
		OkHttpClient client = new OkHttpClient().newBuilder()
				  .build();
				Request request = new Request.Builder()
				  .url("https://www.instagram.com/data/shared_data/")
				  .method("GET", null)
				  .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/93.0.4577.82 Mobile Safari/537.36")
				  .build();
				try {
					response = client.newCall(request).execute();
					
				} catch (IOException e) {
					e.printStackTrace();
				}
		
		return response;
	}
	
	public Response doLogin(String XCSRFToken, String enc_password, String username) {
		
		Response response = null;

		OkHttpClient client = new OkHttpClient().newBuilder()
				  .build();
				MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
				@SuppressWarnings("deprecation")
				RequestBody body = RequestBody.create(mediaType, "enc_password=" + enc_password + "&username=" + username);
				Request request = new Request.Builder()
				  .url("https://www.instagram.com/accounts/login/ajax/")
				  .method("POST", body)
				  .addHeader("X-CSRFToken", XCSRFToken)
				  .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/93.0.4577.82 Mobile Safari/537.36")
				  .build();
				try {
					response = client.newCall(request).execute();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	
	
		return response;
	
	}

}