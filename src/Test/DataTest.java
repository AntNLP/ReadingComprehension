package Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import FeatureExtraction.FeatureExtract;
import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.LBeginAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class DataTest {
	String[] storyArg;
	String[][] question;
	String[][][] answer;
	int[][] answerResult;
	int[][] questionType;
	ArrayList<String> stop_word;
	static final int info = 9999;
	ReWriter rw;

	// String[] arguments = { ",",".","!","?",";","\""};

	public DataTest() {

	}

	public DataTest(String path, int size) throws Exception {
		GetTestData gData = new GetTestData(path, size);
		this.storyArg = gData.storyArg;
		this.question = gData.questionArg;
		this.answer = gData.answerArg;
		this.answerResult = new int[storyArg.length][4];
		this.questionType = new int[storyArg.length][4];
		rw = new ReWriter();
		stop_word = new ArrayList<>();
		getStop_WordList();
		getQuestionType();
		coreferenceUsing();
	}

	public String[] storyDivided(String story) {
		String[] sentense;
		sentense = story.split("\\.|\\?|;|\"|!|\\,");
		return sentense;
	}

	public String answerRegular(String answer) {
		answer = answer.substring(answer.indexOf(")") + 1);
		answer = answer.replaceAll("\\.", "").trim();
		return answer;
	}

	public String questionRegular(String question) {
		int firstIndex = question.indexOf(":");
		int secondIndex = question.indexOf(":", firstIndex + 1);
		question = question.substring(secondIndex + 1);
		question = question.replaceAll("\\?", "").trim();
		return question;
	}
	
	/***
	 * 获得停用词表
	 */
	public void getStop_WordList(){
		String path = "./src/res/stop_word/stop_word.txt";
		File file = new File(path);
		FileReader fr;
		BufferedReader br;
		try{
			fr = new FileReader(file);
			br = new BufferedReader(fr);
			String word = null;
			while((word = br.readLine()) != null){
				word = word.trim();
				stop_word.add(word);
			}
			br.close();
			fr.close();	
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/***
	 * 剔除听用词
	 * @param answer
	 * @return
	 */
	public String removeStop_word(String string){
		String[] word = string.split(" ");
		for(int i = 0;i < word.length;i++){
			for (String sw : stop_word) {
				if(sw.trim().equals(word[i])){
					word[i] = "***";
				}
			}
		}
		String new_str = "";
		for(int i = 0; i < word.length;i++){
			if(!word[i].equals("***")){
				new_str += word[i]+" ";
			}
		}
		new_str = new_str.trim();
		return new_str;
	}

	public Result combine(String a, String question) {
		String label = "0";
		if (a.substring(0, 1).equals("*")) {
			label = "1";
		}
		String re_answer = answerRegular(a);
		String re_question = questionRegular(question);
		ArrayList<String> re_questionlist = rw.reWrite(question);
		ArrayList<String> statementlist = new ArrayList<String>();
		for (int i = 0; i < re_questionlist.size(); i++) {
			String statement = rw.combine(re_questionlist.get(i), re_answer);
			statementlist.add(statement);
		}
		Result result = new Result();
		result.statementlist = statementlist;
		result.question = re_question;
		result.answer = re_answer;
		result.label = label;
		return result;
	}

	public double score(Result result, String[] sentense) throws Exception {
		double maxScore = 0;
		ArrayList<String> statementlist = result.statementlist;
		for (int j = 0; j < statementlist.size(); j++) {
			double sumScore = 0;
			for (int i = 0; i < sentense.length; i++) {
				if (sentense[i].trim().length() < 1) {
					break;
				}
				String statement = removeStop_word(statementlist.get(j));
				String answer = removeStop_word(result.answer);
				String sing_sentense = removeStop_word(sentense[i]);
				String line = statement+"\t"+result.question.trim() + "\t" +answer.trim() + "\t" + sing_sentense.trim() + "\t"
						+ result.label.trim();
				// System.out.println(line);
				double score = getScore(line);
				sumScore += score;
			}
			if(sumScore > maxScore){
				maxScore = sumScore;
			}
		}
		// System.out.println("!!!!!!!!!!!!!!!!!!!!!!"+sumScore);
		return maxScore;
	}

	public double getScore(String line) throws Exception {
		double score = 0;
		FeatureExtract fe = new FeatureExtract();
		double[] feature = fe.execute(line);
		score = predict(feature);
		return score;
	}

	public double predict(double[] feature) throws IOException {
		String line = "";
		for (int i = 0; i < feature.length; i++) {
			if (i == 0) {
				line = feature[i] + " ";
			} else {
				line += i + ":" + feature[i] + " ";
			}
		}
		try {
			File file = new File("/home/lcr/workspace/ReadingComprehension/src/res/test/temp.txt");

			// if file doesnt exists, then create it
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();

			// true = append file
			// FileWriter fileWritter = new FileWriter(file.getAbsoluteFile(),
			// true);
			FileOutputStream fos = new FileOutputStream(file.getAbsoluteFile(), true);
			BufferedWriter bufferWritter = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
			bufferWritter.write(line);
			bufferWritter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String[] parg = { "-b", "1", "/home/lcr/workspace/ReadingComprehension/src/res/test/temp.txt",
				"/home/lcr/workspace/ReadingComprehension/src/res/model/train_model_13.txt",
				"/home/lcr/workspace/ReadingComprehension/src/res/test/out.txt" };
		svm_predict predict = new svm_predict();
		double[] result = predict.main(parg);
		System.out.println(result[0]);
		if (result[0] == 1) {
			return result[1];
		} else {
			return 0;
		}
	}

	public int[][] getAnswerResult() throws Exception {
		for (int i = 0; i < answer.length; i++) {
			String[] sentense = storyDivided(storyArg[i]);
			for (int j = 0; j < answer[i].length; j++) {
				double maxScore = 0;
				int index = 0;
				for (int m = 0; m < answer[i][j].length; m++) {
					Result result = combine(answer[i][j][m], question[i][j]);
					double score = score(result, sentense);
					if (score >= maxScore) {
						maxScore = score;
						index = m;
					}
				}
				System.out.println("!");
				answerResult[i][j] = index;
			}
		}
		for (int i = 0; i < answerResult.length; i++) {
			for (int j = 0; j < answerResult[i].length; j++) {
				System.out.print(answerResult[i][j] + " ");
			}
			System.out.println();
		}
		return answerResult;
	}

	public int[][] getRightAnswer() {
		int[][] rightAnswer = new int[storyArg.length][4];
		for (int i = 0; i < answer.length; i++) {
			for (int j = 0; j < answer[i].length; j++) {
				for (int m = 0; m < answer[i][j].length; m++) {
					if (answer[i][j][m].contains("*")) {
						rightAnswer[i][j] = m;
						break;
					}
				}
			}
		}
		return rightAnswer;
	}

	public void getQuestionType() {
		for (int i = 0; i < question.length; i++) {
			for (int j = 0; j < question[i].length; j++) {
				if (question[i][j].contains(": one:")) {
					questionType[i][j] = 0;
				} else if (question[i][j].contains(": multiple:")) {
					questionType[i][j] = 1;
				}
			}
		}
	}

	/**
	 * 调用指代消解
	 */
	public void coreferenceUsing() {
		for (int i = 0; i < storyArg.length; i++) {
			String str = coreference(storyArg[i]);
			storyArg[i] = str;
		}
	}

	/**
	 * 对输入字符串进行指代消解
	 * 
	 * @param str
	 * @return
	 */
	public String coreference(String str) {
		PrintWriter out;
		String[][] replaceData = new String[info][info];
		for (int i = 0; i < replaceData.length; i++) {
			for (int j = 0; j < replaceData[i].length; j++) {
				replaceData[i][j] = " ";
			}
		}

		StanfordCoreNLP pipeline = new StanfordCoreNLP();

		// Initialize an Annotation with some text to be annotated. The text is
		// the argument to the constructor.
		Annotation annotation = new Annotation(str);
		StringBuilder sb = new StringBuilder(str);
		// run all the selected Annotators on this text
		pipeline.annotate(annotation);
		// print the results to file(s)
		// pipeline.prettyPrint(annotation, out);
		// if (xmlOut != null) {
		// pipeline.xmlPrint(annotation, xmlOut);
		// }

		// An Annotation is a Map and you can get and use the various analyses
		// individually.
		// For instance, this gets the parse tree of the first sentence in the
		// text.
		List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
		if (sentences != null && !sentences.isEmpty()) {
			CoreMap sentence = sentences.get(0);
			Map<Integer, CorefChain> corefChains = annotation.get(CorefCoreAnnotations.CorefChainAnnotation.class);
			if (corefChains == null) {
				return " ";
			}
			int count = 0;
			for (Map.Entry<Integer, CorefChain> entry : corefChains.entrySet()) {
				// out.println("Chain " + entry.getKey() + " ");
				int count2 = 0;
				String name = "";
				for (CorefChain.CorefMention m : entry.getValue().getMentionsInTextualOrder()) {
					// We need to subtract one since the indices count from 1
					// but the Lists start from 0
					List<CoreLabel> tokens = sentences.get(m.sentNum - 1).get(CoreAnnotations.TokensAnnotation.class);
					if (count2 == 0) {
						name = m.toString();
						name = name.split("\"")[1];
					} else {
						String temp = m.toString().toLowerCase();
						temp = temp.split("\"")[1];
						String[] arguments = { "i", "you", "he", "she", "me", "him", "her", "hers", "us", "they",
								"them", "his", "it", "its", "their", "this", "those" };
						if ((Arrays.asList(arguments).contains(temp))) {
							int start = tokens.get(m.startIndex - 1).beginPosition();
							int end = tokens.get(m.endIndex - 2).endPosition();
							replaceData[start][end] = name;
						}
					}
					count2++;
				}
				count++;
			}
		}
		for (int i = info - 1; i >= 0; i--) {
			for (int j = info - 1; j >= 0; j--) {
				if (!replaceData[i][j].equals(" ")) {
					sb.replace(i, j, replaceData[i][j]);
				}
			}
		}
		return sb.toString();
	}

	public void evaluate() throws Exception {
		int[][] rightAnswer = getRightAnswer();
		int[][] answerResult = getAnswerResult();
		double oneTypeCorrect = 0;
		double oneTypeError = 0;
		double multiTypeCorrect = 0;
		double multiTypeError = 0;
		double correct = 0;
		double fault = 0;
		for (int i = 0; i < storyArg.length; i++) {
			for (int j = 0; j < 4; j++) {
				if (questionType[i][j] == 0) {
					if (rightAnswer[i][j] == answerResult[i][j]) {
						oneTypeCorrect++;
						correct++;
					} else {
						oneTypeError++;
						fault++;
					}
				} else if (questionType[i][j] == 1) {
					if (rightAnswer[i][j] == answerResult[i][j]) {
						correct++;
						multiTypeCorrect++;
					} else {
						fault++;
						multiTypeError++;
					}
				}
			}
		}
		double correctRate = correct / (correct + fault);
		double oneTypeRate = oneTypeCorrect / (oneTypeCorrect + oneTypeError);
		double multiTypeRate = multiTypeCorrect / (multiTypeCorrect + multiTypeError);
		System.out.println("正确个数：" + correct + ",错误个数" + fault);
		System.out.println("正确率：" + correctRate);
		System.out.println("One型正确个数：" + oneTypeCorrect + ",One型错误个数: " + oneTypeError + ",One型正确率：" + oneTypeRate);
		System.out.println(
				"Multi型正确个数:" + multiTypeCorrect + ",Multi型错误个数：" + multiTypeError + ",Multi型正确率：" + multiTypeRate);
	}
}
