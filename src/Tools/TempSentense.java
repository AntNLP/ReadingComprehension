package Tools;

import java.util.ArrayList;
import QABasedonDT.Frame;

public class TempSentense {
	/***
	 * @author chenruili
	 */
	Boolean isFrameNull;
	ArrayList<Frame> frameList;
	ArrayList<String> tokenList;
	String content;
	
	public TempSentense(String content){
		this.content = content;
		isFrameNull = true;
		frameList = new ArrayList<>();
		tokenList = new ArrayList<>();
	}
}
