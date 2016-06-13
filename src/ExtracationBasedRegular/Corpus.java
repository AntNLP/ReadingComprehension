package ExtracationBasedRegular;

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

	/**
	 * ָ����⴦��
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
		List<CoreMap> sentences = annotation
				.get(CoreAnnotations.SentencesAnnotation.class);
		if (sentences != null && !sentences.isEmpty()) {
			CoreMap sentence = sentences.get(0);
			Map<Integer, CorefChain> corefChains = annotation
					.get(CorefCoreAnnotations.CorefChainAnnotation.class);
			if (corefChains == null) {
				return " ";
			}
			int count = 0;
			for (Map.Entry<Integer, CorefChain> entry : corefChains.entrySet()) {
				// out.println("Chain " + entry.getKey() + " ");
				int count2 = 0;
				String name = "";
				for (CorefChain.CorefMention m : entry.getValue()
						.getMentionsInTextualOrder()) {
					// We need to subtract one since the indices count from 1
					// but the Lists start from 0
					List<CoreLabel> tokens = sentences.get(m.sentNum - 1).get(
							CoreAnnotations.TokensAnnotation.class);
					if (count2 == 0) {
						name = m.toString();
						name = name.split("\"")[1];
					} else {
						String temp = m.toString().toLowerCase();
						temp = temp.split("\"")[1];
						String[] arguments = { "i", "you", "he", "she", "me",
								"him", "her", "hers","us", "they", "them" ,"his","it","its","their","theirs"};
						if ((Arrays.asList(arguments).contains(temp))) {
							int start = tokens.get(m.startIndex - 1)
									.beginPosition();
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

	public void coreferenceUsing() {
		for (int i = 0; i < storyArg.length; i++) {
			String str = coreference(storyArg[i]);
			storyArg[i] = str;
			//System.out.println(i);
		}
	}

	public String getLemma(String sentense) {
		String originSentense = "";
		Properties props = new Properties();
		props.put("annotators", "tokenize,ssplit,pos, lemma");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		Annotation document = new Annotation(sentense);
		pipeline.annotate(document);
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		for (CoreMap sentence : sentences) {
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				String word = token.get(TextAnnotation.class);
				String lema = token.get(LemmaAnnotation.class);
				originSentense += lema + " ";
			}
		}
		if (originSentense.length() > 0) {
			originSentense = originSentense.substring(0,
					originSentense.length() - 1);
		}
		return originSentense;
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

	public void printt() {
		// for(int i = 0;i < storyArg.length;i++){
		// System.out.println(storyArg[i]);
		// for(int j = 0;j < 4;j++){
		// System.out.println(question[i][j]);
		// for(int m = 0;m < 4;m++){
		// System.out.println(answer[i][j][m]);
		// }
		// }
		// }
		
		coreferenceUsing();
		Strategy s = new Strategy(storyArg, question, answer);
		s.evaluate();
	}
	
	public void printt2(){
		coreferenceUsing();
		Strategy_BasedOnStructure s = new Strategy_BasedOnStructure(storyArg,question,answer);
		s.evaluate();
	}
	
	public void printt3(){
		coreferenceUsing();
		Strategy_BasedOnSW s = new Strategy_BasedOnSW(storyArg, question, answer);
		s.evaluate();
		//s.test();
	}
	
	public void printt4(){
		coreferenceUsing();
		Strategy_BasedOnDependencyTree s = new Strategy_BasedOnDependencyTree(storyArg, question, answer);
		s.extract_supportsentense();
	}

}
