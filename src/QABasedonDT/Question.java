package QABasedonDT;

import java.io.Serializable;
import java.util.ArrayList;
import java.io.File;

import Tools.Semafor;
import Tools.Tools;

public class Question extends Sentence implements Serializable {
	/***
	 * @author chenruili
	 */
	private static final long serialVersionUID = 1L;

	String content;
	WordNode[] wordNodes;
	String type;
	static Tools tools = new Tools();
	public WordNode root;
	public Boolean isFrameNull;
	public ArrayList<Frame> frameList;
	public ArrayList<String> tokenList;

	public Question() {
		isFrameNull = true;
		frameList = new ArrayList<>();
		tokenList = new ArrayList<>();
	}

	/***
	 * 
	 * @param question content
	 * @param question type(mul/single)
	 * @return question object instance
	 */
	public static Question getNewQuestion(String content, String type) {
		Question question = new Question();
		question.content = content;
		question.type = type;
		question.wordNodes = Question.tools.parse(content);
		question.root = getRoot(question.wordNodes);
		return question;
	}

	/***
	 * @function conduct semafor process for question
	 * @param question array
	 * @param dataSetType
	 * @param start
	 * @param end
	 */
	public static void questionSemafor(Question[][] questions, String dataType, int start, int end) {
		Semafor semafor = new Semafor();
		semafor.setFilePath(dataType);
		boolean first = true;
		for (int i1 = start; i1 < end; i1++) {
			for (int i2 = 0; i2 < questions[i1].length; i2++) {
				try {
					semafor.fileWriter(questions[i1][i2].content, first);
					first = false;
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
		File outfile = semafor.scriptCall();
		semafor.jsonQuestionRead(questions, outfile, start, end);
	}

	public String getContent() {
		return content;
	}

	public static WordNode getRoot(WordNode[] word_nodes) {
		WordNode root = null;
		for (int i = 0; i < word_nodes.length; i++) {
			if (word_nodes[i].parent == null) {
				root = word_nodes[i];
				break;
			}
		}
		return root;
	}

	public WordNode[] getWordNode() {
		return wordNodes;
	}

	public void setIsFrameNull(boolean isFrameNull) {
		this.isFrameNull = isFrameNull;
	}

	public void setFrameList(ArrayList<Frame> frameList) {
		this.frameList = frameList;
	}
}
