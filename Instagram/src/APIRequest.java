import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class APIRequest {

	private OkHttpClient client;
	private Request request;
	private String sessionId;
			
	APIRequest(String sessionId) {
		this.client = new OkHttpClient().newBuilder().build();
		this.sessionId = sessionId;
	}
	
	
	public Response doRequest(String url) {
		
		Response response = null;
		
		this.request = new Request.Builder()
			.url(url)
			.method("GET", null)
			.addHeader("X-IG-App-ID", "936619743392459")
			.addHeader("Cookie", "sessionid=" + sessionId)
			.build();
		
		try {
			response = client.newCall(request).execute();
		} catch (IOException e) {
			System.out.println("Error in Request");
			//e.printStackTrace(
		}
		return response;
	}
}