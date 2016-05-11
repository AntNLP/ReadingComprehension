package StructExtraction;

import java.util.ArrayList;

public class Word {
	String content;
	String postag;
	String dependency_label;
	int index;
	Word parent;
	ArrayList<Word> children;
	
	public Word(){
		this.content = "";
		this.postag = "";
		this.dependency_label = "";
		this.index = 0;
		this.parent = null;
		this.children = new ArrayList<>();
	}
	
}
