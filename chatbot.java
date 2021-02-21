package friendly_chatbot;


import java.io.*;
import java.nio.Buffer;
import java.util.*;



public class chatbot extends knowledge_B {
	
	private static String  	sInput = new String("");
	private static String  	sResponse = new String("");
	private static String  	sPrevInput = new String("");
	private static String  	sPrevResponse = new String("");
	private static String  	sEvent = new String("");
	private static String  	sPrevEvent = new String("");
	private static String  	sInputBackup = new String("");
	private static String	sSubject = new String("");
	private static String	sKeyWord = new String("");
	private static boolean	bQuitProgram = false;
	
	final static int maxInput = 1;
	final static int maxResp = 6;
	final static String delim = "?!.;,";

	
	private static Vector<String>	respList = new Vector<String>(maxResp);
	
	public static void get_input() throws Exception 
	{
		System.out.print(">");

		// saves the previous input
		save_prev_input();
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		sInput = in.readLine();
		
		preprocess_input();
	}

	public static void respond()
	{
		save_prev_response();
		set_event("BOT UNDERSTAND**");

		if(null_input())
		{
			handle_event("NULL INPUT**");
		}
		else if(null_input_repetition())
		{
			handle_event("NULL INPUT REPETITION**");
		}
		else if(user_repeat())
		{
			handle_user_repetition();
		}
		else
		{
			find_match();
		}

	    if(user_want_to_quit())
		{
			bQuitProgram = true;
		}
	    
	    if(!bot_understand())
		{
			handle_event("BOT DON'T UNDERSTAND**");
		}
	    
	    if(respList.size() > 0)
		{
			select_response();

			if(bot_repeat())
			{
				handle_repetition();
			}
			print_response();
		}
	}

	
	public static boolean quit() {
		return bQuitProgram;
	}
	
	// make a search for the user's input
	// inside the database of the program
	public static void find_match() 
	{
		respList.clear();
		
		String bestKeyWord = "";
		Vector<Integer> index_vector = new Vector<Integer>(maxResp);


		for(int i = 0; i < KnowledgeBase.length; ++i) 
		{
			String[] keyWordList = KnowledgeBase[i][0];

			for(int j = 0; j < keyWordList.length; ++j)
			{
				String keyWord = keyWordList[j];
				char firstChar=keyWord.charAt(0);
				char lastChar=keyWord.charAt(keyWord.length()-1);
				keyWord.trim();
		
				keyWord = insert_space(keyWord);

				int keyPos=sInput.indexOf(keyWord);

				if( keyPos != -1 ) 
				{
					//'keyword ranking' feature 
				if(wrong_location(keyWord,firstChar,lastChar,keyPos)){
						continue; }
					if(keyWord.length() > bestKeyWord.length())
					{
						bestKeyWord = keyWord;
						index_vector.clear();
						index_vector.add(i);
					}
					else if(keyWord.length() == bestKeyWord.length())
					{
						index_vector.add(i);
					}
				}
			}
		}
		if(index_vector.size() > 0)
		{
			sKeyWord = bestKeyWord;
			Collections.shuffle(index_vector);
			int respIndex = index_vector.elementAt(0);
			int respSize = KnowledgeBase[respIndex][1].length;
			for(int j = 0; j < respSize; ++j) 
			{
				respList.add(KnowledgeBase[respIndex][1][j]);
			}
		}
	}
	
	void preprocess_response()
	{
		if(sResponse.indexOf("*") != -1)
		{
			// extracting from input
			find_subject(); 
			// conjugating subject
			sSubject = transpose(sSubject); 

			sResponse = sResponse.replaceFirst("*", sSubject);
		}
	}

	void find_subject()
	{
		sSubject = ""; // resets subject variable
		StringBuffer buffer = new StringBuffer(sInput);
		buffer.deleteCharAt(0);
		sInput = buffer.toString();
		int pos = sInput.indexOf(sKeyWord);
		if(pos != -1)
		{
			sSubject = sInput.substring(pos + sKeyWord.length() - 1,sInput.length());		
		}
	}
	
	// implementing the 'sentence transposition' feature
	public static String transpose( String str )
	{
		boolean bTransposed = false;
		for(int i = 0; i < transposList.length; ++i)
		{
			String first = transposList[i][0];
			insert_space(first);
			String second = transposList[i][1];
			insert_space(second);
			
			String backup = str;
			str = str.replaceFirst(first, second);
			if(str != backup) 
			{
				bTransposed = true;
			}
		}

		if( !bTransposed )
		{
			for( int i = 0; i < transposList.length; ++i )
			{
				String first = transposList[i][0];
				insert_space(first);
				String second = transposList[i][1];
				insert_space(second);
				str = str.replaceFirst(first, second);
			}
		}
		return str;
	}
	
	public static boolean wrong_location(String keyWord, char firstChar, char lastChar, int pos){
		boolean wrongPos=false;
		pos+=keyWord.length();
		if((firstChar=='_' && lastChar=='_' && sInput!=keyWord) || 
		   (firstChar!='_' && lastChar!='_' && pos!=sInput.length()) || 
		   (firstChar=='_' && lastChar!='_' && pos==sInput.length())){
    			wrongPos=true;
		}
		return wrongPos;
	}

	public static void handle_repetition()
	{
		if(respList.size() > 0)
		{
			respList.removeElementAt(0);
		}
		if(no_response())
		{
			save_input();
			set_input(sEvent);

			find_match();
			restore_input();
		}
		select_response();
	}
	
	public static void handle_user_repetition()
	{
		if(same_input()) 
		{
			handle_event("REPETITION T1**");
		}
		else if(similar_input())
		{
			handle_event("REPETITION T2**");
		}
	}
	
	public static void handle_event(String str)
	{
		save_prev_event();
		set_event(str);

		save_input();
		str = insert_space(str);
		
		set_input(str);
		
		if(!same_event()) 
		{
			find_match();
		}

		restore_input();
	}
	
	public static void signon()
	{
		handle_event("SIGNON**");
		select_response();
		print_response();
	}

	public static void select_response() {
		Collections.shuffle(respList);
		sResponse = respList.elementAt(0);
	}

	public static void save_prev_input() {
		sPrevInput = sInput;
	}

	public static void save_prev_response() {
		sPrevResponse = sResponse;
	}

	public static void save_prev_event() {
		sPrevEvent = sEvent;
	}

	public static void set_event(String str) {
		sEvent = str;
	}

	public static void save_input() {
		sInputBackup = sInput;
	}

	public static void set_input(String str) {
		sInput = str;
	}
	
	public static void restore_input() {
		sInput = sInputBackup;
	}
	
	public static void print_response()  {
		if(sResponse.length() > 0) {
			System.out.println(sResponse);
		}
	}
	
	public static void preprocess_input() {
		sInput = cleanString(sInput);
		sInput = sInput.toUpperCase();
		sInput = insert_space(sInput);
	}

	public static boolean bot_repeat()  {
		return (sPrevResponse.length() > 0 && 
			sResponse == sPrevResponse);
	}

	public static boolean user_repeat()  {
		return (sPrevInput.length() > 0 &&
			((sInput == sPrevInput) || 
			(sInput.indexOf(sPrevInput) != -1) ||
			(sPrevInput.indexOf(sInput) != -1)));
	}

	public static boolean bot_understand()  {
		return respList.size() > 0;
	}

	public static boolean null_input()  {
		return (sInput.length() == 0 && sPrevInput.length() != 0);
	}

	public static boolean null_input_repetition()  {
		return (sInput.length() == 0 && sPrevInput.length() == 0);
	}

	public static boolean user_want_to_quit()  {
		return sInput.indexOf("BYE") != -1;
	}

	public static boolean same_event()  {
		return (sEvent.length() > 0 && sEvent == sPrevEvent);
	}

	public static boolean no_response()  {
		return respList.size() == 0;
	}

	public static boolean same_input()  {
		return (sInput.length() > 0 && sInput == sPrevInput);
	}

	public static boolean similar_input()  {
		return (sInput.length() > 0 &&
			(sInput.indexOf(sPrevInput) != -1 ||
			sPrevInput.indexOf(sInput) != -1));
	}
	
	static boolean isPunc(char ch) {
		return delim.indexOf(ch) != -1;
	}
	
	// removes punctuation and redundant
	// spaces from the user's input
	static String cleanString(String str) {
		StringBuffer temp = new StringBuffer(str.length());
		char prevChar = 0;
		for(int i = 0; i < str.length(); ++i) {
			if((str.charAt(i) == ' ' && prevChar == ' ' ) || !isPunc(str.charAt(i))) {
				temp.append(str.charAt(i));
				prevChar = str.charAt(i);
			}
			else if(prevChar != ' ' && isPunc(str.charAt(i)))
			{
				temp.append(' ');
			}
			
		}
		return temp.toString();
	}
	
	static String insert_space(String str)
	{
		StringBuffer temp = new StringBuffer(str);
		temp.insert(0, ' ');
		temp.insert(temp.length(), ' ');
		return temp.toString();
	}
	
	

}
