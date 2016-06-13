package QABasedonDT;

import java.io.Serializable;
import java.util.ArrayList;

import QABasedonDT.Sentense;
import Tools.Tools;

public class Word_Node implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/*************************
	 * 属性
	 */
	public String postag;
	public String syn_info;
	public Word_Node parent;
	public int index;
	public String content;
	public ArrayList<Sentense> support_sentense;
	public Sentense currentSentense;
	public ArrayList<Word_Node> childlist;
	public double[] node_socre;
	public int [][] record_index;
	public double max_score;
	public String question;
	public String[] story;
	public double[][] edge_feature;
	public double[][] node_feature;
	public double[][] answer_feature;
	public Tools tools; 

	/*********************************
	 * 方法
	 * @param path
	 * @param count
	 */
	public Word_Node() {

	}

	public Sentense getCurrentSentense() {
		return currentSentense;
	}

	public String getContent() {
		return content;
	}

	public String getPostag() {
		return postag;
	}

	public String getSyn_info() {
		return syn_info;
	}

	public Word_Node getParent() {
		return parent;
	}

	public ArrayList<Sentense> getSupport_Sentense_index() {
		return support_sentense;
	}

	public int getIndex() {
		return index;
	}

	public Word_Node(int index) {
		this.content = " ";
		support_sentense = new ArrayList<>();
		childlist = new ArrayList<>();
		this.index = index;
		this.postag = " ";
		this.syn_info = " ";
		this.currentSentense = new Sentense();
		parent = null;
	}

	public Word_Node(String content, int index, String postag, String syn_info) {
		this.content = content;
		support_sentense = new ArrayList<>();
		support_sentense = new ArrayList<>();
		this.index = index;
		this.postag = postag;
		this.syn_info = syn_info;
	}

	public String string_process(String string) {
		string = string.replaceAll("[\\pP‘'“”]", "");
		string = string.toLowerCase().trim();
		return string;
	}

	public String string_process_lemma(String string) {
		string = string.replaceAll("[\\pP‘'“”]", "");
		string = tools.getLemma(string);
		string = string.toLowerCase().trim();
		return string;
	}

	public boolean isChild(Word_Node w) {
		for (Word_Node word_Node : childlist) {
			if (string_process(word_Node.getContent()).equals(w.getContent())) {
				return true;
			}
		}
		return false;
	}

	public boolean isParent(Word_Node w) {
		if (parent != null && string_process(parent.getContent()).equals(string_process(w.getContent()))) {
			return true;
		}
		return false;
	}

	public boolean isSibling(Word_Node w) {
		if (w.parent != null) {
			for (Word_Node word_Node : w.parent.childlist) {
				if (string_process(word_Node.getContent()).equals(w.getContent())) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isChild_lemma(Word_Node w) {
		for (Word_Node word_Node : childlist) {
			if (string_process_lemma(word_Node.getContent()).equals(string_process_lemma(w.getContent()))) {
				return true;
			}
		}
		return false;
	}

	public boolean isParent_lemma(Word_Node w) {
		if (parent != null && string_process_lemma(parent.getContent()).equals(string_process_lemma(w.getContent()))) {
			return true;
		}
		return false;
	}

	public boolean isSibling_lemma(Word_Node w) {
		if (w.parent != null) {
			for (Word_Node word_Node : w.parent.childlist) {
				if (string_process_lemma(word_Node.getContent()).equals(string_process_lemma(w.getContent()))) {
					return true;
				}
			}
		}
		return false;
	}
	
	public void add_EdgeFeautre(int index,double[] child_edgefeature, double[] edgefeature){
		for(int i = 0;i < edgefeature.length;i++){
			this.edge_feature[index][i] += child_edgefeature[i]+edgefeature[i];
		}
	}
	
	public void add_NodeFeautre(int index,double[] child_nodefeature,double[] nodefeature){
		for(int i = 0;i < nodefeature.length;i++){
			this.node_feature[index][i] += child_nodefeature[i]+ nodefeature[i];
		}
	}
	
	public void add_AnswerFeature(int index,double[] child_answerfeature,double[] answerfeature){
		for(int i = 0;i < answerfeature.length;i++){
			this.answer_feature[index][i] += child_answerfeature[i]+ answerfeature[i];
		}
	}
	

}
