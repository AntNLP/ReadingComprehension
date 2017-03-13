package QABasedonDT;
import java.io.File;
import Tools.Semafor;
import Tools.Tools;
import java.io.Serializable;
import java.util.ArrayList;

public class Ans extends Sentence implements Serializable{
	/***
	 * @author chenruili
	 */
	private static final long serialVersionUID = 1L;
	
	String content;
	WordNode[] wordNodes;
	String type;
	static Tools tools = new Tools();
	public WordNode root;
	String tag;
	public Boolean isFrameNull;
	public ArrayList<Frame> framesList;
	public ArrayList<String> tokensList;

	public Ans(){
		isFrameNull = true;
		framesList = new ArrayList<>();
		tokensList = new ArrayList<>();
	}
	
	/***
	 * 
	 * @param answer string
	 * @return answer object instance
	 */
	public static Ans getNewAnswer(String content){
		Ans answer = new Ans();
		answer.content = content;
		answer.wordNodes = Ans.tools.parse(content);
		answer.root = getRoot(answer.wordNodes);
		return answer;
	}
	
	/***
	 * @function conduct semafor process for answer
	 * @param answer array
	 * @param dataSetType
	 * @param start
	 * @param end
	 */
	public static void answerSemafor(Ans[][][] answers, String dataType,int start,int end) {
		Semafor semafor = new Semafor();
		semafor.setFilePath(dataType);
		boolean first = true;
		for (int i1 = start; i1 < end; i1++) {
			for (int i2 = 0; i2 < answers[i1].length; i2++) {
				for (int i3 = 0; i3 < answers[i1][i2].length; i3++) {
					try {
						semafor.fileWriter(answers[i1][i2][i3].content, first);
						first = false;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		File outfile = semafor.scriptCall();
		semafor.jsonAnswerRead(answers,outfile,start,end);
	}
	
	public String getContent(){
		return content;
	}
	
	public static WordNode getRoot(WordNode[] word_nodes){
		WordNode root = null;
		for(int i = 0;i < word_nodes.length;i++){
			if(word_nodes[i].parent == null){
				root = word_nodes[i];
				break;
			}
		}
		return root;
	}
	
	public WordNode[] getWordNode(){
		return wordNodes;
	}
	
	public void setIsFrameNull(boolean isFrameNull){
		this.isFrameNull = isFrameNull;
	}
	
	public void setFramesList(ArrayList<Frame> frame_list){
		this.framesList =frame_list;
	}
}
