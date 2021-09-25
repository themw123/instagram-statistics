import java.nio.charset.Charset;
import java.security.MessageDigest;

import okhttp3.Response;

public class InstagramLogin {
	
	private String username;
	private String password;
	private APIRequest rq;
	//body
	private String enc_password;
	//headers
	private String XCSRFToken;
	private String ContentType;
	private String ContentLength;
	private String header;
	
	public InstagramLogin(String username, String password) {
		rq = new APIRequest("");
		this.username = username;
		this.password = password;
		setXCRFTOKEN();
		setPassword();
	}
	
	private void setXCRFTOKEN() {
		//Response r = rq.xcsrfToken();
		XCSRFToken = "ANwMZhNaIZKr1hyNWPgdlfcbHHPrKOu6";
	}
	
	private void setPassword() {
		
	}
}
