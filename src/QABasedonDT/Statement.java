package QABasedonDT;

import java.io.Serializable;
import java.util.ArrayList;
import java.io.File;

import Tools.Semafor;
import Tools.Tools;

public class Statement extends Sentence implements Serializable {
	/***
	 * @author chenruili
	 */
	private static final long serialVersionUID = 1L;
	
	String content;
	WordNode[] wordNodes;
	static Tools tools = new Tools();
	public WordNode root;
	public Boolean isFrameNull;
	public ArrayList<Frame> frameList;
	public ArrayList<String> tokenList;

	public Statement() {
		isFrameNull = true;
		this.frameList = new ArrayList<>();
		this.tokenList = new ArrayList<>();
	}

	/***
	 * 
	 * @param the hypothesis content
	 * @return the statement object instance
	 */
	public static Statement getNewStatement(String content) {
		Statement statement = new Statement();
		statement.content = content;
		statement.wordNodes = tools.parse(content);
		statement.root = getRoot(statement.wordNodes);
		return statement;
	}

	/***
	 * @function conduct semafor process for hypothesis
	 * @param statement array
	 * @param dataSetType
	 * @param start
	 * @param end
	 */
	public static void statementSemafor(Statement[][][] statements, String dataType,int start,int end) {
		Semafor semafor = new Semafor();
		semafor.setFilePath(dataType);
		boolean first = true;
		for (int i1 = start; i1 < end; i1++) {
			for (int i2 = 0; i2 < statements[i1].length; i2++) {
				for (int i3 = 0; i3 < statements[i1][i2].length; i3++) {
					try {
						semafor.fileWriter(statements[i1][i2][i3].content, first);
						first = false;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		File outfile = semafor.scriptCall();
		semafor.jsonStatementRead(statements,outfile,start,end);
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

	public void setFramesList(ArrayList<Frame> frame_list) {
		this.frameList = frame_list;
	}
}
