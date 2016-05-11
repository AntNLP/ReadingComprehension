package FeatureExtraction;

import QABasedonDT.Sentense;
import QABasedonDT.Word_Node;

public class test {
	public static void main(String args[]){
		FeatureExtract_DT fDt = new FeatureExtract_DT();
		Word_Node w1 = new Word_Node();
		Word_Node w2 = new Word_Node();
		Sentense s1 = new Sentense();
		Sentense s2 = new Sentense();
		s1.content = "Cathy got scared when Jessica";
		s2.content = "Cathy got scared when Jessica";
		w1.currentSentense = s1;
		w2.currentSentense = s2;
//		fDt.getEdgeFeature(w1, w2, "What time did the party start",);
		
	}
}
