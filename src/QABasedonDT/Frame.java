package QABasedonDT;
import java.io.Serializable;
import java.util.ArrayList;

public class Frame implements Serializable{
	/***
	 * @author chenruili
	 */
	private static final long serialVersionUID = 1L;

	Target target;
	ArrayList<Element> elementsList;
	ArrayList<InnerObjects> innerList;
	public ArrayList<Sentence> supportSentense;
	public Sentence currentSupportSentense;
	public int index;
	public int bestIndex;
	
	public Frame(Target target,ArrayList<Element> elementsList){
		this.supportSentense = new ArrayList<>();
		this.target = target;
		this.elementsList = elementsList;
		currentSupportSentense = new Sentence();
		index = 0;
		bestIndex = 0;
		this.innerList = new ArrayList<>();
	}
	
	public Target getTarget(){
		return this.target;
	}
	
	public ArrayList<Element> getElementList(){
		return this.elementsList;
	}
}
