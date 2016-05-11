package ExtracationBasedRegular;

import java.util.ArrayList;

public class Word {
	ArrayList<Integer> supportsentense;
	int min;
	int max;
	String content;
	
	public Word(String string){
		supportsentense = new ArrayList<>();
		this.content = string;
		min = 0;
		max = 0;
	}
}
