package QABasedonDT;

import java.io.Serializable;

import Tools.Tools;

public class Question implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/*************************
	 * 属性
	 */
	String content;
	Word_Node[] word_node;
	String type;
	static Tools tools = new Tools();

	/*********************************
	 * 方法
	 * @param path
	 * @param count
	 */
	public static Question getNewQuestion(String content,String type){
		Question question = new Question();
		question.content = content;
		question.type = type;
		question.word_node = tools.parse(content);
		return question;
	}
}
