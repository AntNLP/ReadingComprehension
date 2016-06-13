package QABasedonDT;

import java.io.Serializable;

public class Sentense implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/*************************
	 * 属性
	 */
	int sentense_index;
	int word_index;
	public String content;
	double match_flag;//0 or 1
	double w2v_flag;//0~1
	double wn_flag;//0~1
	public Word_Node[] word_nodes;
	
	/*********************************
	 * 方法
	 * @param path
	 * @param count
	 */
	public Sentense(){
		content = " ";
		sentense_index = -1;
		word_index = 0;
		match_flag = 0;
		w2v_flag = 0;
		wn_flag = 0;
	}
	
	public Sentense(int sentense_index,String content,int word_index){
		this.sentense_index = sentense_index;
		this.word_index = word_index;
		this.content = content;
		sentense_index = 0;
		word_index = 0;
		match_flag = 0;
		w2v_flag = 0;
		wn_flag = 0;

	}
	
	public double getMatch_flag(){
		return match_flag;
	}
	
	public double getW2v_flag(){
		return w2v_flag;
	}
	
	public double getWn_flag(){
		return w2v_flag;
	}
	
	public int getIndex(){
		return sentense_index;
	}
	
	public int getWord_index(){
		return word_index;
	}
	
	public String getContent(){
		return content;
	}
}
