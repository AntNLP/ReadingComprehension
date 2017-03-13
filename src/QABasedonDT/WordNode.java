package QABasedonDT;

import java.io.Serializable;
import java.util.ArrayList;
import QABasedonDT.Sentence;
import Tools.Tools;

public class WordNode implements Serializable {
	/***
	 * @author chenruili
	 */
	private static final long serialVersionUID = 1L;

	public String postag;
	public String synInfo;
	public WordNode parent;
	public int index;
	public String content;
	public ArrayList<Sentence> supportSentense;
	public Sentence currentSentense;
	public ArrayList<WordNode> childlist;
	public double[] nodeScore;
	public int[][] recordIndex;
	public double maxScore;
	public String question;
	public String[] story;
	public double[][] edgeFeature;
	public double[][] nodeFeature;
	public Tools tools;
	
	public WordNode() {

	}
	
	public WordNode(int index) {
		this.content = " ";
		supportSentense = new ArrayList<>();
		childlist = new ArrayList<>();
		this.index = index;
		this.postag = " ";
		this.synInfo = " ";
		this.currentSentense = new Sentence();
		parent = null;
	}

	public WordNode(String content, int index, String postag, String syn_info) {
		this.content = content;
		supportSentense = new ArrayList<>();
		supportSentense = new ArrayList<>();
		this.index = index;
		this.postag = postag;
		this.synInfo = syn_info;
	}

	public Sentence getCurrentSentense() {
		return currentSentense;
	}

	public String getContent() {
		return content;
	}

	public String getPostag() {
		return postag;
	}

	public String getSynInfo() {
		return synInfo;
	}

	public WordNode getParent() {
		return parent;
	}

	public ArrayList<Sentence> getSupportSentenseIndex() {
		return supportSentense;
	}

	public int getIndex() {
		return index;
	}

	public String stringProcess(String string) {
		string = string.replaceAll("[\\pP‘'“”]", "");
		string = string.toLowerCase().trim();
		return string;
	}

	public String stringProcessLemma(String string) {
		string = string.replaceAll("[\\pP‘'“”]", "");
		string = tools.getLemma(string);
		string = string.toLowerCase().trim();
		return string;
	}

	public boolean isChild(WordNode w) {
		for (WordNode word_Node : childlist) {
			if (stringProcess(word_Node.getContent()).equals(w.getContent())) {
				return true;
			}
		}
		return false;
	}

	public boolean isParent(WordNode w) {
		if (parent != null && stringProcess(parent.getContent()).equals(stringProcess(w.getContent()))) {
			return true;
		}
		return false;
	}

	public boolean isSibling(WordNode w) {
		if (w.parent != null) {
			for (WordNode word_Node : w.parent.childlist) {
				if (stringProcess(word_Node.getContent()).equals(w.getContent())) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isChildLemma(WordNode w) {
		for (WordNode word_Node : childlist) {
			if (stringProcessLemma(word_Node.getContent()).equals(stringProcessLemma(w.getContent()))) {
				return true;
			}
		}
		return false;
	}

	public boolean isParentLemma(WordNode w) {
		if (parent != null && stringProcessLemma(parent.getContent()).equals(stringProcessLemma(w.getContent()))) {
			return true;
		}
		return false;
	}

	public boolean isSiblingLemma(WordNode w) {
		if (w.parent != null && this.parent != null) {
			if (stringProcessLemma(parent.getContent()).equals(w.parent.getContent())) {
				return true;
			}
		}
		return false;
	}

	public boolean isChildLemma(String str) {
		for (WordNode word_Node : childlist) {
			if (stringProcessLemma(word_Node.getContent()).equals(stringProcessLemma(str))) {
				return true;
			}
		}
		return false;
	}

	public boolean isParentLemma(String str) {
		if (parent != null && stringProcessLemma(parent.getContent()).equals(stringProcessLemma(str))) {
			return true;
		}
		return false;
	}

	public boolean isSiblingLemma(String str) {
		if (parent != null) {
			for (WordNode word_Node : parent.childlist) {
				if (stringProcessLemma(word_Node.getContent()).equals(stringProcessLemma(str))) {
					return true;
				}
			}
		}
		return false;
	}

	public void addEdgeFeautre(int index, double[] child_edgefeature, double[] edgefeature) {
		if (edgefeature != null) {
			for (int i = 0; i < edgefeature.length; i++) {
				this.edgeFeature[index][i] += child_edgefeature[i] + edgefeature[i];
			}
		}
	}

	public void addNodeFeautre(int index, double[] child_nodefeature, double[] nodefeature) {
		if (nodefeature != null) {
			for (int i = 0; i < nodefeature.length; i++) {
				this.nodeFeature[index][i] += child_nodefeature[i] + nodefeature[i];
			}
		}
	}

}
