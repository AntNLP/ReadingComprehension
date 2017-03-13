package QABasedonDT;

import java.io.Serializable;
import java.util.ArrayList;

public class Target implements Serializable{
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

	
	public Target(String name,String text)
	{
		this.name = name;
		this.text = text;
		supportSentense = new ArrayList<Sentence>();
		currentSupportSentense = new Sentence();
		index = 0;
		bestIndex = 0;
	}
	
	public String getName(){
		return this.name;
	}
	
	public String getText(){
		return this.text;
	}
}
