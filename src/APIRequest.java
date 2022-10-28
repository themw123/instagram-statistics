
import java.util.Random;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class APIRequest {

	private OkHttpClient client;
	private String sessionId;
	private static int requestsCount = 0;

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
			String output = response.body().string();
			String t = "";
		} catch (Exception e) {
			// System.out.println("Error in Request");
			e.printStackTrace();
		}
		requestsCount++;
		return response;
	}

	public String getUsername(String ds_user_id) {
		String username = null;
		String userAgent = getUserAgent(1);
		OkHttpClient client = new OkHttpClient().newBuilder()
				.build();
		Request request = new Request.Builder()
				.url("https://i.instagram.com/api/v1/users/" + ds_user_id + "/info/")
				.method("GET", null)
				.addHeader("Cookie", "sessionid=" + sessionId)
				.addHeader("User-Agent", userAgent)
				.build();
		try {
			Response response = client.newCall(request).execute();
			String output = response.body().string();
			JSONObject jsonObj = new JSONObject(output);
			username = jsonObj.getJSONObject("user").getString("username");
		} catch (Exception e) {
			// e.printStackTrace();
		}
		return username;

	}

	public boolean checkSessionId() {

		boolean sessionIdValid = false;
		String CookieRest = "; ; csrftoken=nZuDlWBFwyeYeMq2310WJTkZ7TJ94eW8; ds_user_id=***REMOVED***; ig_did=73E57DB4-02C8-4C6E-8E5B-0BAA0F9D98D1; mid=Y1woHQAEAAGYjGiATqfQP_GveoAU; rur=\"NAO\\054***REMOVED***\\0541698519975:01f74828e5c0bceeb4544c99b27213051a5aa2d7bcf9d3f52e5132b65383f703f02e5c0e\"; shbid=\"19131\\054***REMOVED***\\0541698519975:01f79c6adc26ebc8eabd0594f3757b0420275e2be32994fa68cc976e8d41f1eff2194cb0\"; shbts=\"1666983975\\054***REMOVED***\\0541698519975:01f77f12b8de0c42160475896f818e4552a08e35c277825f17961443e408ead9fda86315\"";

		OkHttpClient client = new OkHttpClient().newBuilder()
				.build();
		Request request = new Request.Builder()
				.url("https://i.instagram.com/api/v1/accounts/edit/web_form_data/")
				.method("GET", null)
				.addHeader("X-IG-App-ID", "936619743392459")
				.addHeader("Cookie",
						"sessionid=" + sessionId)
				.build();
		try {
			Response response = client.newCall(request).execute();
			String output = response.body().string();
			if (!output.contains("<!DOCTYPE html>")) {
				sessionIdValid = true;
			}
		} catch (Exception e) {
			// e.printStackTrace();
		}
		requestsCount++;
		return sessionIdValid;
	}

	public Response XCSRFToken() {
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

		} catch (Exception e) {
			// e.printStackTrace();

		}
		requestsCount++;
		return response;
	}

	public Response doLogin(String XCSRFToken, String enc_password, String username) {

		Response response = null;
		String userAgent = getUserAgent(2);

		try {
			OkHttpClient client = new OkHttpClient().newBuilder()
					.build();
			MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
			@SuppressWarnings("deprecation")
			RequestBody body = RequestBody.create(mediaType, "enc_password=" + enc_password + "&username=" + username);
			Request request = new Request.Builder()
					.url("https://www.instagram.com/accounts/login/ajax/")
					.method("POST", body)
					.addHeader("X-CSRFToken", XCSRFToken)
					.addHeader("User-Agent", userAgent)
					.build();

			response = client.newCall(request).execute();
		} catch (Exception e) {
			// e.printStackTrace();
		}

		requestsCount++;
		return response;

	}

	public int getRequestsCount() {
		return requestsCount;
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