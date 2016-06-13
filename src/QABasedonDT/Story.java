package QABasedonDT;

import java.io.Serializable;

import Tools.Tools;

public class Story implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/*************************
	 * 属性
	 */
	public String content;
	public Sentense[] sentenses;
	public String[] sentense_string;
	static Tools tools = new Tools();

	/*********************************
	 * 方法
	 * @param path
	 * @param count
	 */
	public static Story getNewStroy(String content) {
		String[] story_sentense = storyDivided(content);
		Story story = new Story();
		story.content = content;
		story.sentenses = new Sentense[story_sentense.length];
		story.sentense_string = new String[story_sentense.length];
		for (int i = 0; i < story.sentenses.length; i++) {
			story.sentense_string[i] = story_sentense[i];
			story.sentenses[i] = new Sentense();
			story.sentenses[i].content = story_sentense[i];
			if (story.sentenses[i].content.trim().length() != 0) {
				story.sentenses[i].word_nodes = tools.parse(story_sentense[i]);
			}
		}
		return story;
	}

	public static String[] storyDivided(String story) {
		String[] sentense;
		sentense = story.split("\\.|\\?|;|:|\"|!|\\,");
		return sentense;
	}
}
