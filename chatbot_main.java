package friendly_chatbot;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Vector;

public class chatbot_main extends chatbot {
	public static void main(String[] args) throws Exception  {
		// TODO Auto-generated method stub

		try {
			signon();
			while(!quit()) {
				get_input();
				respond();
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
