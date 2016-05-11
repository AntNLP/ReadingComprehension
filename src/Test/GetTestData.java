package Test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class GetTestData {
	private String path;
	public String[] storyArg;
	public String[][] questionArg;
	public String[][][] answerArg;
	int storyCount;
	int questionCount;
	int answerCount;
	int line;
	Boolean isAnswer;
	Boolean isStory;
	
	public GetTestData() {

	}

	public GetTestData(String path,int size) throws Exception {
		this.path = path;
		this.storyArg = new String[size];
		this.questionArg = new String[storyArg.length][4];
		this.answerArg = new String[storyArg.length][4][4];
		for(int i = 0; i < storyArg.length;i++){
			storyArg[i] = "";
			for(int j = 0;j < 4;j++){
				questionArg[i][j] = "";
				for(int m = 0;m < 4;m++){
					answerArg[i][j][m] = "";
				}
			}
		}
		this.storyCount = -1;
		this.questionCount = 0;
		this.answerCount = 0;
		this.line = 0;
		this.isAnswer = false;
		this.isStory = false;

		read();
	}
	
	public void read() throws Exception{
		FileInputStream fis = new FileInputStream(path);
		InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
		BufferedReader br = new BufferedReader(isr);
		String s = null;
		try {
			while ((s = br.readLine()) != null) {
				seperate(s);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		br.close();
	}
	
	private void seperate(String str) {
		if (str.contains("***")) {
			storyCount++;
			line = 0;
			isStory = true;
		}
		if (line > 6 && isStory) {
			if (str.contains(": multiple:") || str.contains(": one:")) {
				// question[storyCount][questionCount] = str.toLowerCase();
				questionArg[storyCount][questionCount] = str;
				questionCount++;
				isAnswer = true;
			} else if (isAnswer) {
				// answer[storyCount][questionCount - 1][answerCount] =
				// str.toLowerCase();
				answerArg[storyCount][questionCount - 1][answerCount] = str;
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
}
