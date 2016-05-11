package FeatureExtraction;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.function.DoublePredicate;

import Tools.Tools;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.optimization.QNMinimizer.eLineSearch;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.util.CoreMap;

public class FeatureExtract {
	String[] trainingData;
	Tools tools;
	public FeatureExtract(){
		tools = new Tools();
	}
	/**
	 * 36pos tag|ner(2)|假设句词重(1)+问题词重(1)+候选答案特征(3)+2grams(2)+3grams(2)|*2
	 * 
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public   double[] execute(String data) throws Exception {
		data = pre_Normalization(data);
		double label = getLabel(data);													
		double[] feature = new double[56];
		//double[] posTagFeature = getPosTagFeature(data);
		double combine_repetitionFeature = getCombineFeature(data);
		double question_repetitionFeature = getQuestionFeature(data);
		double[] answerFeature = getAnswerFeature(data);
		double[] two_gram = getNGramsFeature(data, 2);
		double[] three_gram = getNGramsFeature(data, 3);
		data = pre_Lemma(data);
		//double[] ner = getNerFeature(data);
		double lemma_combine_repetitionFeature = getCombineFeature(data);
		double lemma_question_repetitionFeature = getQuestionFeature(data);
		double[] lemma_answerFeature = getAnswerFeature(data);
		double[] lemma_two_gram = getNGramsFeature(data, 2);
		double[] lemma_three_gram = getNGramsFeature(data, 3);
		for (int i = 0; i < feature.length; i++) {
			if (i == 0) {
				feature[i] = label;
			} else if (i >= 1 && i <= 36) {
				// feature[i] = posTagFeature[i - 1];
				feature[i] = 0;
			} else if (i >= 37 && i <= 38) {
				// feature[i] = ner[i - 37];
				feature[i] = 0;
			} else if (i == 39) {
				feature[i] = combine_repetitionFeature;
			} else if (i == 40) {
				feature[i] = question_repetitionFeature;
			} else if (i >= 41 && i <= 43) {
				feature[i] = answerFeature[i - 41];
			} else if (i >= 44 && i <= 45) {
				feature[i] = two_gram[i - 44];
			} else if (i >= 46 && i <= 47) {
				feature[i] = three_gram[i - 46];
			} else if (i == 48) {
				feature[i] = lemma_combine_repetitionFeature;
			} else if (i == 49) {
				feature[i] = lemma_question_repetitionFeature;
			} else if (i >= 50 && i <= 52) {
				feature[i] = lemma_answerFeature[i - 50];
			} else if (i >= 53 && i <= 54) {
				feature[i] = lemma_two_gram[i - 53];
			} else if (i >= 55 && i <= 56) {
				feature[i] = lemma_three_gram[i - 55];
			}
		}
		return feature;
	}

	public  String pre_Normalization(String data) {
		String[] arg = data.split("\t");
		String statement = arg[0].toLowerCase();
		String question = arg[1].toLowerCase();
		String answer = arg[2].toLowerCase();
		String support = arg[3].toLowerCase();
		data = statement+"\t"+question + "\t" + answer + "\t" + support + "\t" + arg[4];
		return data;
	}
	
	public  double getWordNetScore(String data){
		double wordnet_feature = 0;
		String[] arg = data.split("\t");
		String statement = arg[0].toLowerCase();
		String support = arg[3].toLowerCase();
		WordNetScore wordNetScore = new WordNetScore(statement, support);
		wordnet_feature = wordNetScore.getSimilarity();
		return wordnet_feature;
	}

	public  String pre_Lemma(String data) {
		String[] arg = data.split("\t");
		String statment = tools.getLemma(arg[0]);
		String question = tools.getLemma(arg[1]);
		String answer = tools.getLemma(arg[2]);
		String support = tools.getLemma(arg[3]);
		data = statment+"\t"+question + "\t" + answer + "\t" + support + "\t" + arg[4];
		return data;
	}

	public   double getLabel(String data) throws Exception {
		double label;
		String[] arg = data.split("\t");
		String datalabel = arg[4].trim();
		if (datalabel.contains("0")) {
			label = 0;
		} else {
			label = 1;
		}
		return label;
	}

	public   double[] getNGramsFeature(String data, int n) {
		double[] n_grams = new double[2];
		double count = 0;
//		double count1 = 0;
//		double count2 = 0;
		String[] arg = data.split("\t");
		String question = arg[1];
		String answer = arg[2];
		String support = arg[3];
		String statment = arg[0];
		String[] statement_Ngram = null;
//		String[] question_Ngram = null;
//		String[] answer_Ngram = null;
		String[] support_Ngram = null;
		if (n == 2) {
//			question_Ngram = get_2grams(question);
//			answer_Ngram = get_2grams(answer);
			statement_Ngram = get_2grams(statment);
			support_Ngram = get_2grams(support);
		} else if (n == 3) {
//			question_Ngram = get_3grams(question);
//			answer_Ngram = get_3grams(answer);
			statement_Ngram = get_3grams(statment);
			support_Ngram = get_3grams(support);
		}
//		if (question_Ngram != null && support_Ngram != null) {
//			for (int i = 0; i < question_Ngram.length; i++) {
//				for (int j = 0; j < support_Ngram.length; j++) {
//					if (question_Ngram[i].equals(support_Ngram[j])) {
//						count1++;
//						break;
//					}
//				}
//			}
//		}
//		if (answer_Ngram != null && support_Ngram != null) {
//			for (int i = 0; i < answer_Ngram.length; i++) {
//				for (int j = 0; j < support_Ngram.length; j++) {
//					if (answer_Ngram[i].equals(support_Ngram[j])) {
//						count2++;
//						break;
//					}
//				}
//			}
//		}
		if (statement_Ngram != null && support_Ngram != null) {
			for (int i = 0; i < statement_Ngram.length; i++) {
				for (int j = 0; j < support_Ngram.length; j++) {
					if (statement_Ngram[i].equals(support_Ngram[j])) {
						count++;
						break;
					}
				}
			}
		}
//		if(question_Ngram != null){
//			count1 = count1/question_Ngram.length;
//		}else{
//			count1 = 0;
//		}
//		if(answer_Ngram != null){
//			count2 = count2/answer_Ngram.length;
//		}else{
//			count2 = 0;
//		}
		if(support_Ngram != null){
			count = count/statement_Ngram.length;
		}else{
			count = 0;
		}
		n_grams[0] = count;
		n_grams[1] = 0;
		return n_grams;
	}

	public   String[] get_2grams(String sentense) {
		String[] arg = sentense.split(" ");
		String[] arg_2grams = null;
		if (arg.length > 1) {
			arg_2grams = new String[arg.length - 1];
			for (int i = 0; i < arg.length - 1; i++) {
				arg_2grams[i] = arg[i] + " " + arg[i + 1];
			}
		}
		return arg_2grams;
	}

	public   String[] get_3grams(String sentense) {
		String[] arg = sentense.split(" ");
		String[] arg_3grams = null;
		if (arg.length > 2) {
			arg_3grams = new String[arg.length - 2];
			for (int i = 0; i < arg.length - 2; i++) {
				arg_3grams[i] = arg[i] + " " + arg[i + 1] + " " + arg[i + 2];
			}
		}
		return arg_3grams;
	}

	public   double getAnswerandQuestionFeature(String data) {
		String[] arg = data.split("\t");
		String question = arg[1];
		String answer = arg[2];
		String support = arg[3];
		double repetitionAnswer = getRepetitionRate(answer, support);
		double repetitionQuestion = getRepetitionRate(question, support);
		double rate = repetitionAnswer * repetitionQuestion * 100;
		return rate;
	}

	public   double[] getAnswerFeature(String data) {
		double[] answerFeature = new double[3];
		String[] arg = data.split("\t");
		String answer = arg[2];
		String support = arg[3];
		answerFeature[0] = getRepetitionRate(answer, support);
		answerFeature[1] = getAnswerContinousContainFeature(support, answer);
		answerFeature[2] = getAnswerContainFeature(support, answer);
		return answerFeature;
	}

	public   double getAnswerContainFeature(String support, String answer) {
		String[] answerArg = answer.split(" ");
		String[] supportArg = support.split(" ");
		for (String aWord : answerArg) {
			boolean flag = false;
			for (String sWord : supportArg) {
				if (aWord.equals(sWord)) {
					flag = true;
				}
			}
			if (flag == false) {
				return 0;
			}
		}
		return 1;
	}

	/**
	 * 
	 * 
	 * @param support
	 * @param answer
	 * @return
	 */
	public   double getAnswerContinousContainFeature(String support,
			String answer) {
		if (support.contains(answer)) {
			return 1;
		} else {
			return 0;
		}
	}

	public   double getCombineFeature(String data) throws Exception {
		double repetitionFeature;
		String[] arg = data.split("\t");
		String question = arg[1];
		String answer = arg[2];
		String support = arg[3];
		String statement = arg[0];
		double repetitionRate = getRepetitionRate(statement, support);
		repetitionFeature = repetitionRate;
		return repetitionFeature;
	}

	public   double getQuestionFeature(String data) throws Exception {
		double repetitionFeature;
		String[] arg = data.split("\t");
		String question = arg[1];
		String answer = arg[2];
		String support = arg[3];
		double repetitionRate = getRepetitionRate(question, support);
		repetitionFeature = repetitionRate;
		return repetitionFeature;
	}

	public   double getRepetitionRate(String statement, String support) {
		String[] statementArg = statement.split(" ");
		String[] supportArg = support.split(" ");
		double num = 0;
		for (int i = 0; i < supportArg.length; i++) {
			for (int j = 0; j < statementArg.length; j++) {
				if (supportArg[i].equals(statementArg[j])) {
					num++;
					break;
				}
			}
		}
		double repetition = num / (double)statement.length();
		return repetition;
	}

	// public   double[] getPosTagFeature(String data) throws Exception {
	// Vector<String> vector = new Vector<>();
	// vector = getPosList();
	// double[] posTagFeature = new double[2 * vector.size()];
	// int featureCount1 = 0, featureCount2 = 35;
	// String[] arg = data.split("\t");
	// String subsen = arg[0] + "\t" + arg[1];
	// Vector<String> statementV = getInfo(subsen);
	// Vector<String> supportV = getInfo(arg[2]);
	// for (String vString : vector) {
	// int count1 = 0;
	// int count2 = 0;
	// for (String stateV : statementV) {
	// if (vString.equals(stateV)) {
	// count1++;
	// }
	// }
	// for (String support : supportV) {
	// if (vString.equals(support)) {
	// count2++;
	// }
	// }
	// posTagFeature[featureCount1] = count1;
	// posTagFeature[featureCount2] = count2;
	// featureCount1++;
	// featureCount2++;
	// }
	// return posTagFeature;
	// }

	public   double[] getPosTagFeature(String data) throws Exception {
		Vector<String> vector = new Vector<>();
		vector = getPosList();
		double[] posTag = new double[2 * vector.size()];
		int featureCount1 = 0, featureCount2 = 36;
		String[] arg = data.split("\t");
		String statment = arg[0];
		Vector<String> statementV = getInfo(statment);
		Vector<String> supportV = getInfo(arg[3]);
		for (String vString : vector) {
			int count1 = 0;
			int count2 = 0;
			for (String stateV : statementV) {
				if (vString.equals(stateV)) {
					count1 = 1;
					break;
				}
			}
			for (String support : supportV) {
				if (vString.equals(support)) {
					count2 = 1;
					break;
				}
			}
			posTag[featureCount1] = count1;
			posTag[featureCount2] = count2;
			featureCount1++;
			featureCount2++;
		}
		double[] posTagFeature = new double[vector.size()];
		for (int i = 0; i < vector.size(); i++) {
			if (posTag[i] == posTag[36 + i]) {
				posTagFeature[i] = 1;
			} else {
				posTagFeature[i] = 0;
			}
		}
		return posTagFeature;
	}

	public   double[] getNerFeature(String data) {
		double[] ner = new double[2];
		String[] arg = data.split("\t");
		String question = arg[1];
		String answer = arg[2];
		String support = arg[3];
		String[][] questionNer = getNer(question);
		String[][] answerNer = getNer(answer);
		String[][] supportNer = getNer(support);
		double count1 = 0;
		double count2 = 0;
		for (int i = 0; i < questionNer.length; i++) {
			if (questionNer[i][0] == null) {
				break;
			}
			for (int j = 0; j < supportNer.length; j++) {
				if (supportNer[j][0] == null) {
					break;
				} else if (!questionNer[i][0].equals("O")
						&& questionNer[i][0].equals(supportNer[j][0])
						&& questionNer[i][1].equals(supportNer[j][1])) {
					count1++;
				}
			}
		}
		for (int i = 0; i < answerNer.length; i++) {
			if (answerNer[i][0] == null) {
				break;
			}
			for (int j = 0; j < supportNer.length; j++) {
				if (supportNer[j][0] == null) {
					break;
				} else if (!answerNer[i][0].equals("O")
						&& answerNer[i][0].equals(supportNer[j][0])
						&& answerNer[i][1].equals(supportNer[j][1])) {
					count2++;
				}
			}
		}
		ner[0] = count1;
		ner[1] = count2;
		return ner;
	}

	public   String[][] getNer(String txtWord) {
		String[][] nerArg = new String[100][2];
		int count = 0;
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma,ner");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		Annotation document = new Annotation(txtWord);
		pipeline.annotate(document);
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		String originSentense = "";
		for (CoreMap sentence : sentences) {
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				String ner = token.get(NamedEntityTagAnnotation.class);
				String text = token.get(TextAnnotation.class);
				// originSentense += ner + "\t";
				if (ner != "O") {
					nerArg[count][0] = ner;
					nerArg[count][1] = text;
					count++;
				}
			}
		}
		return nerArg;
	}

	// public   int getTrainingCount() throws Exception {
	// String path = "./src/res/mc500.test.txt";
	// FileInputStream fis = new FileInputStream(path);
	// InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
	// BufferedReader br = new BufferedReader(isr);
	// String s = null;
	// int count = 0;
	// try {
	// while ((s = br.readLine()) != null) {
	// if (!(s.contains("<s id=") || s.contains("</s>") || s.contains("<q id")
	// || s.contains("</q>"))) {
	// count++;
	// }
	// }
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// br.close();
	// return count;
	// }

	public   Vector<String> getInfo(String subsen) {
		Vector<String> vector = new Vector<>();
		LexicalizedParser lp = LexicalizedParser
				.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");

		String[] subsenArg = subsen.split("\t");
		for (String suben : subsenArg) {
			PTBTokenizer ptb = PTBTokenizer.newPTBTokenizer(new StringReader(
					suben));
			List words = ptb.tokenize();
			Tree parse = lp.parse(words);
			parse.pennPrint();
			TreebankLanguagePack tlp = lp.getOp().langpack();
			GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
			GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
			// Stanford dependencies in the CoNLL format
			String string1 = GrammaticalStructure.dependenciesToString(gs,
					gs.typedDependencies(), parse, true, false);
			String[] arg = string1.split("\n");
			for (int i = 0; i < arg.length; i++) {
				String[] temp = arg[i].split("\t");
				vector.add(temp[4]);
			}
		}
		return vector;
	}

	public   Vector<String> getPosList() throws Exception {
		Vector<String> vector = new Vector<>();
		String path = "./src/res/POSTagging.txt";
		int count = 0;
		// File file = new File(path);
		FileInputStream fis = new FileInputStream(path);
		InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
		BufferedReader br = new BufferedReader(isr);
		String s = null;
		try {
			while ((s = br.readLine()) != null) {
				vector.add(s.trim());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		br.close();
		return vector;
	}

}