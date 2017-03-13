package QABasedonDT;

import java.io.Serializable;
import java.util.ArrayList;
import Tools.Semafor;
import java.io.File;

import Tools.Tools;

public class Story implements Serializable {
	/***
	 * @author chenruili
	 */
	
	private static final long serialVersionUID = 1L;

	public String content;
	public Sentence[] sentenses;
	public String[] sentense_string;
	static Tools tools = new Tools();

	public static Story getNewStroy(String content,String dataType) {
		Semafor semafor = new Semafor();
		semafor.setFilePath(dataType);
		Story story = new Story();
		story.content = content;
		content = content.replace("\"", "");
		ArrayList<String> story_sentense = storyDivided(content);
		story.sentenses = new Sentence[story_sentense.size()];
		story.sentense_string = new String[story_sentense.size()];
		boolean first = true;
		for (int i = 0; i < story.sentenses.length; i++) {
			story.sentense_string[i] = story_sentense.get(i);
			story.sentenses[i] = new Sentence();
			story.sentenses[i].content = story_sentense.get(i);
			try {
				semafor.fileWriter(story_sentense.get(i),first);
				first = false;
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (story.sentenses[i].content.trim().length() != 0) {
				story.sentenses[i].wordNodes = tools.parse(story_sentense.get(i));
				story.sentenses[i].root = getRoot(story.sentenses[i].wordNodes);
			}
		}
//		File outfile = semafor.script_call();
		semafor.jsonSentenseRead(story.sentenses);
		return story;
	}


	public static ArrayList<String> storyDivided(String story) {
		String[] sentense;
		sentense = story.split("\\.|\\?|;|\"|!");
		ArrayList<String> list = new ArrayList<>();
		for (int i = 0; i < sentense.length; i++) {
			String[] sub_sentense = sentense[i].split("\\,");
			String new_sentense = "";
			boolean flag = false;
			for (int j = 0; j < sub_sentense.length; j++) {
				int length = sub_sentense[j].trim().split(" ").length;
				if (new_sentense == "") {
					new_sentense = sub_sentense[j];
					continue;
				}
				if (length <= 2) {
					new_sentense += ", " + sub_sentense[j];
					flag = true;
				} else {
					if(flag == true){
						if(new_sentense.trim().length() >0){
							new_sentense += ", " + sub_sentense[j];
							list.add(new_sentense.trim());
							flag = false;
						}
						new_sentense = "";
					}else{
						if(new_sentense.trim().length() > 0 ){
							list.add(new_sentense.trim());
						}
						new_sentense = sub_sentense[j];
					}
				}
			}
			if (new_sentense.trim().length() > 0) {
				list.add(new_sentense.trim());
			}
		}
		return list;
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
}
