package StructExtraction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import Test.GetTestData;
import Test.Result;

public class test {
	static String[] storyArg;
	static String[][] question;
	static String[][][] answer;

	public static void main(String args[]) throws Exception {
		String path = "/home/lcr/workspace/ReadingComprehension/src/res/test/mc500.test.txt";
		int size = 150;
		GetTestData gData = new GetTestData(path, size);
		storyArg = gData.storyArg;
		question = gData.questionArg;
		answer = gData.answerArg;
		StructExtraction();
	}
	
	public static String questionRegular(String question) {
		int firstIndex = question.indexOf(":");
		int secondIndex = question.indexOf(":", firstIndex + 1);
		question = question.substring(secondIndex + 1);
		question = question.replaceAll("\\?", "").trim();
		return question;
	}

	public static String[] storyDivided(String story) {
		String[] sentense;
		sentense = story.split("\\.|\\?|;|\"|!|\\,");
		return sentense;
	}

	public static void StructExtraction() {
		for (int i = 0; i < answer.length; i++) {
			for (int j = 0; j < answer[i].length; j++) {
					String q = question[i][j];
					q = questionRegular(q);
					String[] struct = StructExtraction.extract(q);
					String line = q + "\t" + struct[0]+ "\t" + struct[1] + "\t" + struct[2];
					writeFile(line);
			}
		}
	}

	public static void writeFile(String str) {
		String path = "./src/res/StructExtraction/" + "structure2.txt";
		try {
			File file = new File(path);

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			// true = append file
			// FileWriter fileWritter = new FileWriter(file.getAbsoluteFile(),
			// true);
			FileOutputStream fos = new FileOutputStream(file.getAbsoluteFile(), true);
			BufferedWriter bufferWritter = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
			bufferWritter.write(str);
			bufferWritter.newLine();
			bufferWritter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
