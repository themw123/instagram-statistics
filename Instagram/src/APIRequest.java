import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class APIRequest {

	private OkHttpClient client;
	private Request request;
	private String sessionId;
	private double time;
	/*
	time = -System.currentTimeMillis();
	System.out.println((time + System.currentTimeMillis())/1000 + " Sekunden");
	*/
	
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
			//System.out.println("Error in Request");
			//e.printStackTrace()
		}
		return response;
	}
	
	public boolean checkSessionId(String url) {
		
		boolean sessionIdValid = false;
		
		this.request = new Request.Builder()
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
}