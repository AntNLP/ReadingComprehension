package Corpus;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Corpus {
	/***
	 * @author chenruili
	 */
	private String path;
	String[] storyArg;
	String[][] question;
	String[][][] answer;
	int storyCount = -1;
	int questionCount = 0;
	int answerCount = 0;
	int line = 0;
	String story;
	Boolean isAnswer = false;
	Boolean isStory = false;
	int lineLimit = 6;

	public Corpus() {

	}

	public Corpus(String path,int count) {
		this.path = path;
		storyArg = new String[count];
		question = new String[storyArg.length][4];
		answer = new String[storyArg.length][4][4];
		for (int i = 0; i < storyArg.length; i++) {
			storyArg[i] = "";
		}
		if(path.contains("mc160")){
			lineLimit = 4;
		}
		read();
	}

	/***
	 * @function read the corpus data
	 */
	public void read() {
		try {
			Scanner in = new Scanner(new File(path));
			while (in.hasNextLine()) {
				String str = in.nextLine();
				seperate(str);
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}

	/***
	 * conduct analysis for the corpus data
	 * @param str
	 */
	private void seperate(String str) {
		if (str.contains("***")) {
			storyCount++;
			line = 0;
			isStory = true;
		}
		if (line > lineLimit && isStory) {
			if (str.contains(": multiple:") || str.contains(": one:")) {
				// question[storyCount][questionCount] = str.toLowerCase();
				question[storyCount][questionCount] = str;
				questionCount++;
				isAnswer = true;
			} else if (isAnswer) {
				// answer[storyCount][questionCount - 1][answerCount] =
				// str.toLowerCase();
				answer[storyCount][questionCount - 1][answerCount] = str;
				answerCount++;
				if (answerCount > 3) {
					if (questionCount > 3) {
						isStory = false;
						questionCount = 0;
					}
					isAnswer = false;
					answerCount = 0;
				}
			} else {
				// storyArg[storyCount] += str.trim().toLowerCase();
				storyArg[storyCount] += str.trim() + " ";
			}
		}
		line++;
	}
	
	public String[] getStory(){
		return storyArg;
	}
	
	public String[][] getQuestion(){
		return question;
	}
	
	public String[][][] getAnswer(){
		return answer;
	}

}
