package QABasedonDT;

import java.io.Serializable;
import java.util.ArrayList;

public class Element implements Serializable{
	/***
	 * @author chenruili
	 */
	private static final long serialVersionUID = 1L;

	String name;
	String text;
	public ArrayList<Sentence> supportSentense;
	public Sentence currentSupportSentense;
	public int index;
	public int bestIndex;

	public Element(String name,String text)
	{
		this.name = name;
		this.text = text;
		supportSentense = new ArrayList<Sentence>();
		currentSupportSentense = new Sentence();
		index = 0;
		bestIndex = 0;

	}
	
	public String getName(){
		return name;
	}
	
	public String getText(){
		return text;
	}
	
}
