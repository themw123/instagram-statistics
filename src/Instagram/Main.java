package Instagram;

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import org.json.JSONObject;

public class Main {
	public static void main(String[] args) {

		String username = "xxx";
		String password = "xxx";

		String sessionId = "xxx";
		String ds_user_id = "xxx";


		Instagram instagram = new Instagram();
		instagram.connect("session", sessionId, ds_user_id);
		// password = password_input();
		// instagram.connect("login", username, password);

		// instagram.setLogLevel(Level.ALL);
		JSONObject jo = instagram.getData(true);
		// System.out.println(jo.toString(4) + "\n");
	}

	private static String password_input() {
		String password = "";
		String message = "Enter password";
		if (System.console() == null) { // inside IDE like Eclipse or NetBeans
			final JPasswordField pf = new JPasswordField();
			password = JOptionPane.showConfirmDialog(null, pf, message,
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION ? new String(pf.getPassword()) : "";
		} else
			password = new String(System.console().readPassword("%s> ", message));

		return password;
	}

}