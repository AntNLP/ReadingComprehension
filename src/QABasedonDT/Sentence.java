package QABasedonDT;

import java.io.Serializable;
import java.util.ArrayList;

public class Sentence implements Serializable{
	/***
	 * @author chenruili
	 */
	private static final long serialVersionUID = 1L;

	int sentenceIndex;
	int wordIndex;
	public String content;
	public WordNode[] wordNodes;
	public WordNode root;
	public Boolean isFrameNull;
	public ArrayList<Frame> frameList;
	public ArrayList<String> tokenList;

	public Sentence(){
		content = " ";
		sentenceIndex = -1;
		wordIndex = 0;
		isFrameNull = true;
		frameList = new ArrayList<>();
		tokenList = new ArrayList<>();
	}
	
	public Sentence(int sentenceIndex,String content,int wordIndex){
		this.sentenceIndex = sentenceIndex;
		this.wordIndex = wordIndex;
		this.content = content;
		sentenceIndex = 0;
		wordIndex = 0;
	}
	
	public int getIndex(){
		return sentenceIndex;
	}
	
	public int getWordIndex(){
		return wordIndex;
	}
	
	public String getContent(){
		return content;
	}
	
	public WordNode[] getWordNode(){
		return wordNodes;
	}

	public void setIsFrameNull(boolean isFrameNull){
		
	}
	
	public void setFrameList(ArrayList<Frame> frameList){
		
	}
}
