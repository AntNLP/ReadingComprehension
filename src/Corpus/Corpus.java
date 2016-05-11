package Corpus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import Test.GetTestData;
import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.ForcedSentenceEndAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class Corpus {
	static int info = 9999;
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
		read();
	}

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

	private void seperate(String str) {
		if (str.contains("***")) {
			storyCount++;
			line = 0;
			isStory = true;
		}
		if (line > 6 && isStory) {
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
