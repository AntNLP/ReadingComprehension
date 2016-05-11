package QABasedonDT;

import Tools.Tools;

public class Question {
	/*************************
	 * 属性
	 */
	String content;
	Word_Node[] word_node;
	static Tools tools = new Tools();

	/*********************************
	 * 方法
	 * @param path
	 * @param count
	 */
	public static Question getNewQuestion(String content){
		Question question = new Question();
		question.content = content;
		question.word_node = tools.parse(content);
		return question;
	}
}
