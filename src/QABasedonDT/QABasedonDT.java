package QABasedonDT;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import Corpus.Corpus;
import FeatureExtraction.FeatureExtractFrame;
import FeatureExtraction.FeatureExtractGlobal;
import FeatureExtraction.FeatureExtractDT;
import FeatureExtraction.FeatureExtractSenPair;
import FeatureExtraction.FeatureExtractSingle;
import Tools.ReWriter;
import Tools.Tools;

public class QABasedonDT {
	/***
	 * @author chenruili
	 */
	String[] storyArg;
	String[][] question;
	String[][][] answer;
	int[][] answerResult;
	int[][] questionType;
	Corpus corpus;
	static Tools tools;
	public static ArrayList<String> stopWord;
	Model model;
	Story[] storys;
	Question[][] questions;
	Statement[][][] statements;
	Ans[][][] answers;
	MessagePassing mpQue;
	MessagePassing mpAns;
	PairInferenceModule imPair;
	SingleInferenceModule imSingle;
	FrameModule fmQue;
	InnerFrameModule ifmQue;
	FeatureExtractGlobal fxGlobal;
	FeatureExtractDT fxQueDT;
	FeatureExtractSenPair fxPair;
	FeatureExtractSingle fxSingle;
	FeatureExtractDT fxAnsDT;
	FeatureExtractFrame fxFrame;
	String dataType;
	String dataSet;
	String logModelName;
	int dataNum;
	int featureNum;
	int logNum;

	public static HashMap<String, float[]> w2vTable;

	/*********************************
	 * 方法
	 * 
	 * @param path
	 * @param count
	 */

	public QABasedonDT(String path, int count) {
		corpus = new Corpus(path, count);
		getCorpus(corpus);
		tools = new Tools();
		stopWord = new ArrayList<>();
		String[] data = getDataSetType(path);
		dataSet = data[0];
		dataType = data[1];
		logModelName = "";
		// word2vec = new Word2VEC();
		getStopWordList();
		w2vTable = readW2vTable();
		model = new Model();
		storys = new Story[storyArg.length];
		questions = new Question[storyArg.length][4];
		statements = new Statement[storyArg.length][4][4];
		answers = new Ans[storyArg.length][4][4];
		fxGlobal = new FeatureExtractGlobal();
		fxQueDT = new FeatureExtractDT("question");
		fxPair = new FeatureExtractSenPair();
		fxSingle = new FeatureExtractSingle();
		fxAnsDT = new FeatureExtractDT("answer");
		fxFrame = new FeatureExtractFrame();
		mpQue = new MessagePassing(fxQueDT);
		mpAns = new MessagePassing(fxAnsDT);
		imPair = new PairInferenceModule(fxPair);
		imSingle = new SingleInferenceModule(fxSingle);
		fmQue = new FrameModule(fxFrame);
		ifmQue = new InnerFrameModule(fxFrame);
	}

	public void setDataNum(int num) {
		this.dataNum = num;
	}

	public void setFeatureNum(int num) {
		this.featureNum = num;
		mpQue.featureNum = num;
		mpAns.featureNum = num;
		imPair.featureNum = num;
		imSingle.featureNum = num;
		fmQue.featureNum = num;
		ifmQue.featureNum = num;
	}

	public void setLogNum(int num) {
		this.logNum = num;
	}
	
	public void setLearningRate(double rate){
		model.setLearningRate(rate);
	}

	public void loadConfigureFile(String configurefile, Model model) {
		ConfigureProcessor.readConfigureFile(configurefile, model);
		System.out.println(ConfigureProcessor.queEdgeFeatureNum + "," + ConfigureProcessor.queNodeFeatureNum + ","
				+ ConfigureProcessor.globalFeatureNum + "," + ConfigureProcessor.pairFeatureNum + ","
				+ ConfigureProcessor.singleFeatureNum + "," + ConfigureProcessor.ansEdgeFeatureNum + ","
				+ ConfigureProcessor.ansNodeFeatureNum + "," + ConfigureProcessor.frameFeatureNum + ","
				+ ConfigureProcessor.iframeFeatureNum);
	}

	public HashMap<String, float[]> readW2vTable() {
		HashMap<String, float[]> table = new HashMap<>();
		try {
			ObjectInputStream ois = new ObjectInputStream(
					new FileInputStream("../src/res/word2vec_table/w2v_"
							+ dataSet + "." + dataType + ".out"));
			table = (HashMap<String, float[]>) ois.readObject();
			ois.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return table;
	}

	public String[] getDataSetType(String path) {
		String data_type = "";
		String data_set = "";
		String configures = "";
		if (path.contains("train")) {
			data_type = "train";
		} else if (path.contains("test")) {
			data_type = "test";
		} else {
			data_type = "dev";
		}

		if (path.contains("mc500")) {
			data_set = "mc500";
		} else {
			data_set = "mc160";
		}

		String[] data = { data_set, data_type };
		return data;
	}

	public void getCorpus(Corpus c) {
		this.storyArg = c.getStory();
		this.question = c.getQuestion();
		this.answer = c.getAnswer();
	}

	public void getStopWordList() {
		String path = "../src/res/stop_word/stopwords";
		File file = new File(path);
		FileReader fr;
		BufferedReader br;
		try {
			fr = new FileReader(file);
			br = new BufferedReader(fr);
			String word = null;
			while ((word = br.readLine()) != null) {
				word = word.trim();
				stopWord.add(word);
			}
			br.close();
			fr.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String punctutionPreprocess(String string) {
		string = string.replace("\\'", " \\'");
		string = string.replace("\\,", " \\,");
		return string.trim();
	}

	public String contentPreprocess(String string) {
		string = string.replaceAll("[\\pP‘'“”]", "");
		string = string.replaceAll("\\s{1,}", " ");
		string = tools.getLemma(string);
		string = string.toLowerCase().trim();
		return string;
	}

	public String questionRegular(String question) {
		int firstIndex = question.indexOf(":");
		int secondIndex = question.indexOf(":", firstIndex + 1);
		question = question.substring(secondIndex + 1);
		question = question.replaceAll("\\?", "").trim();
		return question;
	}

	public boolean getNegationFlag(String question) {
		boolean flag = false;
		question = question.toLowerCase();
		String[] negation_words = { "not", "didn't", "doesn't", "don't", "isn't", "aren't", "wasn't", "weren't",
				"never" };
		String[] question_words = question.split(" ");
		for (int i = 0; i < negation_words.length; i++) {
			if (flag == true) {
				break;
			}
			for (int j = 0; j < question_words.length; j++) {
				if (question_words[j].equals(negation_words[i])) {
					flag = true;
					break;
				}
			}
		}
		for (int j = 0; j < question_words.length; j++) {
			if (question_words[j].equals("why") || question_words[j].equals("if")) {
				flag = false;
				break;
			}
		}
		return flag;
	}

	public boolean whyQuestionCheck(String question) {
		question = question.toLowerCase();
		String[] word = question.split(" ");
		boolean flag = false;
		for (int i = 0; i < word.length; i++) {
			if (word[i].equals("why")) {
				flag = true;
				break;
			}
		}
		return flag;
	}

	public boolean isListAnswer(String[] answers, String question) {
		boolean flag = false;
		for (int i = 0; i < answers.length; i++) {
			if (flag) {
				break;
			}
			char[] ch = answers[i].toCharArray();
			int count = 0;
			for (int j = 0; j < ch.length; j++) {
				if (ch[j] == ',') {
					count++;
				}
				if (count >= 2) {
					flag = true;
					break;
				}
			}
		}
		question = question.toLowerCase();
		String[] q_w = question.split(" ");
		String[] type_word = { "what", "why", "how", "where", "who" };
		int count = 0;
		for (int i = 0; i < q_w.length; i++) {
			for (int j = 0; j < type_word.length; j++) {
				if (q_w[i].equals(type_word[j])) {
					count++;
				}
			}
		}
		if (count > 1) {
			flag = true;
		}
		return flag;
	}

	public double isSpecialQuestion(String question, String answer, String[] answer_candidates) {
		question = contentPreprocess(question);
		answer = contentPreprocess(answer);
		String[] question_words = question.split(" ");
		String[] answer_words = answer.split(" ");
		double no_count = 0;
		double yes_count = 0;
		for (int i = 0; i < answer_candidates.length; i++) {
			String a = contentPreprocess(answer_candidates[i]);
			String[] a_w = a.split(" ");
			for (int j = 0; j < a_w.length; j++) {
				if (a_w[j].equals("yes")) {
					yes_count++;
					break;
				} else if (a_w[j].equals("no")) {
					no_count++;
					break;
				}
			}
		}
		double score = 0;
		if (question_words[0].equals("do") || question_words[0].equals("did") || question_words[0].equals("be")) {
			for (int i = 0; i < answer_words.length; i++) {
				if (answer_words[i].equals("no")) {
					score = 10.0 / no_count;
				} else if (answer_words[i].equals("yes")) {
					score = 10.0 / yes_count;
				}
			}
		}
		return score;
	}

	public String[] answerRegular(String answer) {
		String tag = "F";
		if (answer.contains("*")) {
			tag = "T";
		}
		answer = answer.substring(answer.indexOf(")") + 1).trim();
		String[] tag_answer = { tag, answer };
		return tag_answer;
	}

	public void sentenseFrameMatch(ArrayList<Frame> frame_list, Sentence[] sentenses) {
		if (frame_list != null) {
			for (int i1 = 0; i1 < frame_list.size(); i1++) {
				Frame frame = frame_list.get(i1);
				String q_target_name = frame.target.name;
				String q_target_text = frame.target.text;
				if (!stopWord.contains(q_target_text) && q_target_text.trim().length() != 0) {
					for (int i2 = 0; i2 < sentenses.length; i2++) {
						if (sentenses[i2].frameList != null) {
							ArrayList<Frame> sentense_frame = sentenses[i2].frameList;
							for (int i3 = 0; i3 < sentense_frame.size(); i3++) {
								String s_target_name = sentense_frame.get(i3).target.name;
								String s_target_text = sentense_frame.get(i3).target.text;
								if ((q_target_name.equals(s_target_name) || q_target_text.equals(s_target_text))) {
									sentenses[i2].sentenceIndex = i2;
									frame.supportSentense.add(sentenses[i2]);
									break;
								}
							}
						}
					}
				}
				if (frame.supportSentense.size() == 0) {
					frame.supportSentense.add(new Sentence(0, "*", 0));
				}
			}
		}
	}

	public void findFrameSupportSentense(int start, int end) throws Exception {
		for (int i1 = start; i1 < end; i1++) {
			for (int i2 = 0; i2 < questions[i1].length; i2++) {
				sentenseFrameMatch(questions[i1][i2].frameList, storys[i1].sentenses);
				for (int i3 = 0; i3 < answers[i1][i2].length; i3++) {
					sentenseFrameMatch(answers[i1][i2][i3].framesList, storys[i1].sentenses);
				}
			}
		}
	}

	public void findSupportSentense(WordNode[] word_nodes, Sentence[] story_sentense, int story_id) {
		for (WordNode w : word_nodes) {
			String word_content = contentPreprocess(w.content);
			if (!stopWord.contains(word_content) && word_content.trim().length() != 0) {
				for (int i = 0; i < story_sentense.length; i++) {
					if (story_sentense[i].content.trim().length() == 0) {
						continue;
					}
					story_sentense[i].content = punctutionPreprocess(story_sentense[i].content);
					WordNode[] sentense_word = story_sentense[i].wordNodes;
					for (int j = 0; j < sentense_word.length; j++) {
						story_sentense[i].sentenceIndex = i;
						story_sentense[i].wordIndex = j;
						String sentense_w = contentPreprocess(sentense_word[j].content);
						if (sentense_w.equals(word_content)) {
							w.supportSentense.add(story_sentense[i]);
							break;
						}
					}
				}
			}
			if (w.supportSentense.size() == 0) {
				w.supportSentense.add(new Sentence(0, "*", 0));
			}
		}
	}

	public void findInnerFrameObject(Frame frame) {
		ArrayList<Element> elements_list = frame.elementsList;
		if (elements_list != null) {
			for (int i = 0; i < elements_list.size(); i++) {
				Element e = elements_list.get(i);
				String e_name = e.name;
				String e_text = e.text;
				InnerObjects inner = new InnerObjects(e_name, e_text);
				frame.innerList.add(inner);
			}
		}
		Target target = frame.target;
		String t_name = target.name;
		String t_text = target.text;
		InnerObjects inner = new InnerObjects(t_name, t_text);
		frame.innerList.add(inner);
	}

	public void sentenseInnerFrameMatch(ArrayList<Frame> frame_list, Sentence[] sentenses) {
		if (frame_list == null) {
			return;
		}
		for (int i = 0; i < frame_list.size(); i++) {
			Frame frame = frame_list.get(i);
			findInnerFrameObject(frame);
			for (int i1 = 0; i1 < frame.innerList.size(); i1++) {
				InnerObjects inner = frame.innerList.get(i1);
				String i_name = inner.name;
				String i_text = inner.text;
				boolean flag = false;
				if (!stopWord.contains(i_text) && i_text.trim().length() != 0) {
					for (int i2 = 0; i2 < sentenses.length; i2++) {
						if (sentenses[i2].frameList != null) {
							if (flag) {
								break;
							}
							ArrayList<Frame> sentense_frame = sentenses[i2].frameList;
							for (int i3 = 0; i3 < sentense_frame.size(); i3++) {
								if (flag) {
									break;
								}
								Frame s_frame = sentense_frame.get(i3);
								findInnerFrameObject(s_frame);
								ArrayList<InnerObjects> s_inner_list = s_frame.innerList;
								for (int i4 = 0; i4 < s_inner_list.size(); i4++) {
									InnerObjects s_inner = s_inner_list.get(i4);

									String si_name = s_inner.name;
									String si_text = s_inner.text;

									if ((i_name.equals(si_name) || i_text.equals(si_text))) {
										sentenses[i2].sentenceIndex = i2;
										inner.supportSentense.add(sentenses[i2]);
										flag = true;
										break;
									}
								}
							}
						}
					}
				}
				if (inner.supportSentense.size() == 0) {
					inner.supportSentense.add(new Sentence(0, "*", 0));
				}
			}
		}
	}

	public void findInnerFrameSupportSentense(int start, int end) throws Exception {
		for (int i1 = start; i1 < end; i1++) {
			for (int i2 = 0; i2 < questions[i1].length; i2++) {
				sentenseInnerFrameMatch(questions[i1][i2].frameList, storys[i1].sentenses);
				for (int i3 = 0; i3 < answers[i1][i2].length; i3++) {
					sentenseInnerFrameMatch(answers[i1][i2][i3].framesList, storys[i1].sentenses);
				}
			}
		}
	}

	public void preprocess(int start, int end) throws Exception {
		for (int i1 = start; i1 < end; i1++) {
			String story = storyArg[i1];
			story = tools.coreference(story);
			storys[i1] = Story.getNewStroy(story, start + "_" + end + "_" + i1 + "_" + dataType);
			for (int i2 = 0; i2 < questions[i1].length; i2++) {
				String type = "one";
				if (question[i1][i2].contains("multiple:")) {
					type = "mul";
				}
				String re_question = questionRegular(question[i1][i2]);
				questions[i1][i2] = Question.getNewQuestion(re_question, type);
				findSupportSentense(questions[i1][i2].wordNodes, storys[i1].sentenses, i1);
				for (int i3 = 0; i3 < answer[i1][i2].length; i3++) {
					String re_answer = answerRegular(answer[i1][i2][i3])[1];
					ReWriter rw = new ReWriter();
					String h = rw.combine(re_question, re_answer);
					Statement statement = Statement.getNewStatement(h);
					statements[i1][i2][i3] = statement;
					answers[i1][i2][i3] = Ans.getNewAnswer(re_answer);
					findSupportSentense(answers[i1][i2][i3].wordNodes, storys[i1].sentenses, i1);
				}
			}
			System.out.println("pre_process: story" + i1);
		}
		System.out.println("questions seimfor...");
		Question.questionSemafor(questions, start + "_" + end + "_" + dataType, start, end);
		System.out.println("answers seimfor...");
		Ans.answerSemafor(answers, start + "_" + end + "_" + dataType, start, end);
		System.out.println("statement seimfor...");
		Statement.statementSemafor(statements, start + "_" + end + "_" + dataType, start, end);
		findFrameSupportSentense(start, end);
		findInnerFrameSupportSentense(start, end);
		serializableProcess(start, end, dataNum);
		System.out.println("over");
	}

	public void readSerializableData(int dataNum) throws Exception {
		for (int i = 0; i < storys.length; i++) {
			ObjectInputStream ois = new ObjectInputStream(
					new FileInputStream("../src/res/serializedata/" + dataSet
							+ "." + dataType + "/story_" + dataNum + "/s" + i + ".out"));
			storys[i] = (Story) ois.readObject();
			ois.close();
		}
		for (int i = 0; i < questions.length; i++) {
			for (int j = 0; j < questions[i].length; j++) {
				ObjectInputStream ois = new ObjectInputStream(
						new FileInputStream("../src/res/serializedata/"
								+ dataSet + "." + dataType + "/question_" + dataNum + "/s" + i + "_q" + j + ".out"));
				questions[i][j] = (Question) ois.readObject();
				ois.close();
			}
		}
		for (int i = 0; i < storys.length; i++) {
			for (int i2 = 0; i2 < questions[i].length; i2++) {
				for (int i3 = 0; i3 < statements[i][i2].length; i3++) {
					ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
							"../src/res/serializedata/" + dataSet + "."
									+ dataType + "/statement_" + dataNum + "/s" + i + "_q" + i2 + "_h" + i3 + ".out"));
					statements[i][i2][i3] = (Statement) ois.readObject();
					ois.close();

					ObjectInputStream ois2 = new ObjectInputStream(new FileInputStream(
							"../src/res/serializedata/" + dataSet + "."
									+ dataType + "/answer_" + dataNum + "/s" + i + "_q" + i2 + "_a" + i3 + ".out"));
					answers[i][i2][i3] = (Ans) ois2.readObject();
					ois2.close();
				}
			}
		}
	}

	public void serializableProcess(int start, int end, int dataNum) throws Exception {
		for (int i = start; i < end; i++) {
			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream("../src/res/serializedata/" + dataSet
							+ "." + dataType + "/story_" + dataNum + "/s" + i + ".out"));
			oos.writeObject(storys[i]);
			oos.close();
		}
		for (int i = start; i < end; i++) {
			for (int j = 0; j < questions[i].length; j++) {
				ObjectOutputStream oos = new ObjectOutputStream(
						new FileOutputStream("../src/res/serializedata/"
								+ dataSet + "." + dataType + "/question_" + dataNum + "/s" + i + "_q" + j + ".out"));
				oos.writeObject(questions[i][j]);
				oos.close();
			}
		}
		for (int i = start; i < end; i++) {
			for (int i2 = 0; i2 < questions[i].length; i2++) {
				for (int i3 = 0; i3 < statements[i][i2].length; i3++) {
					ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(
							"../src/res/serializedata/" + dataSet + "."
									+ dataType + "/statement_" + dataNum + "/s" + i + "_q" + i2 + "_h" + i3 + ".out"));
					oos.writeObject(statements[i][i2][i3]);
					oos.close();

					ObjectOutputStream oos2 = new ObjectOutputStream(new FileOutputStream(
							"../src/res/serializedata/" + dataSet + "."
									+ dataType + "/answer_" + dataNum + "/s" + i + "_q" + i2 + "_a" + i3 + ".out"));
					oos2.writeObject(answers[i][i2][i3]);
					oos2.close();
				}
			}
		}
	}

	public void featureSerializable(int start, int end) throws Exception {
		readSerializableData(dataNum);
		if (end == -1) {
			end = storys.length;
		}
		for (int i1 = start; i1 < end; i1++) {
			System.out.println(dataType + i1 + "is being processed");
			for (int i2 = 0; i2 < questions[i1].length; i2++) {
				for (int i3 = 0; i3 < answers[i1][i2].length; i3++) {
					String[] tag_answer = answerRegular(answer[i1][i2][i3]);
					answers[i1][i2][i3].tag = tag_answer[0];

					mpQue.setInfo(questions[i1][i2].wordNodes, model, questions[i1][i2], answers[i1][i2][i3],
							storys[i1].sentense_string, statements[i1][i2][i3], i1, i2, i3, dataType, dataSet, true);
					Object[] object = mpQue.messagePassing();

					mpAns.setInfo(answers[i1][i2][i3].wordNodes, model, questions[i1][i2], answers[i1][i2][i3],
							storys[i1].sentense_string, statements[i1][i2][i3], i1, i2, i3, dataType, dataSet, true);
					Object[] object2 = mpAns.messagePassing();

					imPair.setInfo(storys[i1], questions[i1][i2], answers[i1][i2][i3], statements[i1][i2][i3], i1, i2,
							i3, dataType, dataSet, model, true);
					imPair.inference();

					imSingle.setInfo(storys[i1], questions[i1][i2], answers[i1][i2][i3], statements[i1][i2][i3], i1, i2,
							i3, dataType, dataSet, model, true);
					imSingle.inference();

					fmQue.setInfo(storys[i1], questions[i1][i2], answers[i1][i2][i3], statements[i1][i2][i3], i1, i2,
							i3, dataType, dataSet, model, true);
					Object[] object3 = fmQue.inference();

					ifmQue.setInfo(storys[i1], questions[i1][i2], answers[i1][i2][i3], statements[i1][i2][i3], i1, i2,
							i3, dataType, dataSet, model, true);
					Object[] object4 = ifmQue.inference();

					HashMap<String, Double> global_feature = fxGlobal.getAllFeature(answers[i1][i2][i3],
							questions[i1][i2], storys[i1], statements[i1][i2][i3]);
					ObjectOutputStream oos = new ObjectOutputStream(
							new FileOutputStream("../src/res/serializedata/"
									+ dataSet + "." + dataType + "/global_feature_" + featureNum + "/s" + i1 + "_q" + i2
									+ "_a" + i3 + ".out"));
					oos.writeObject(global_feature);
					oos.close();
				}
			}
		}
	}

	public ArrayList<Double> readGlobalFeature(int i1, int i2, int i3) throws Exception {
		ArrayList<Double> feature_list = new ArrayList<>();
		ObjectInputStream ois = new ObjectInputStream(
				new FileInputStream("../src/res/serializedata/" + dataSet + "."
						+ dataType + "/global_feature_" + featureNum + "/s" + i1 + "_q" + i2 + "_a" + i3 + ".out"));
		HashMap<String, Double> feature_map = (HashMap<String, Double>) ois.readObject();
		ois.close();
		ArrayList<String> feature_items = ConfigureProcessor.globalFeatureList;
		for (String item : feature_items) {
			if (feature_map.containsKey(item)) {
				double feature_value = feature_map.get(item);
				feature_list.add(feature_value);
			}
		}
		return feature_list;
	}

	public void train(String file) throws Exception {
		System.out.println("开始训练...");
		String logname = file.replace("para_", "log_").substring(0, file.lastIndexOf("_") - 1);
		String log_info = "-----------------------------------------\r\n" + "model:" + file
				+ "\r\n-----------------------------------------\r\n";
		writeLog(log_info, "log" + logNum + "/" + logname);
		readSerializableData(dataNum);
		int loop = 0;
		double correct_rate = 0;
		int update_count = 0;
		while (update_count <= 4) {
			update_count++;
			for (int i1 = 0; i1 < storys.length; i1++) {
				for (int i2 = 0; i2 < questions[i1].length; i2++) {
					boolean isNegation = getNegationFlag(questions[i1][i2].content);
					boolean isWhy = whyQuestionCheck(questions[i1][i2].content);
					boolean anNegation = false;
					boolean is_list_ans = isListAnswer(answer[i1][i2], questions[i1][i2].content);
					Answer[] answer_arg = new Answer[4];
					double max_score = -10000;
					double z_max_score = -10000;
					double z_max_score2 = -10000;
					double global_max_score = -10000;
					double p_max_score = -10000;
					double s_max_score = -10000;
					double f_max_score = -10000;
					double f2_max_score = -10000;
					double min_score = 10000;
					double z_min_score = 10000;
					double z_min_score2 = 10000;
					double global_min_score = 10000;
					double p_min_score = 10000;
					double s_min_score = 10000;
					double f_min_score = 10000;
					double f2_min_score = 10000;
					int pre_index = 0;
					int z_pre_index = 0;
					int z_pre_index2 = 0;
					int global_pre_index = 0;
					int p_pre_index = 0;
					int s_pre_index = 0;
					int f_pre_index = 0;
					int f2_pre_index = 0;
					int correct_index = 0;

					for (int i3 = 0; i3 < answer[i1][i2].length; i3++) {
						String[] tag_answer = answerRegular(answer[i1][i2][i3]);
						double tag = -1;
						if (tag_answer[0].equals("T")) {
							correct_index = i3;
							tag = 1;
						}
						if (getNegationFlag(answers[i1][i2][i3].getContent())) {
							anNegation = true;
						}
						String re_answer = answers[i1][i2][i3].getContent();
						double result = 0;
						double[] edge_feature = new double[Model.queEdgeFeatureNum];
						double[] node_feature = new double[Model.queNodeFeatureNum];
						double[] edge_feature2 = new double[Model.ansEdgeFeatureNum];
						double[] node_feature2 = new double[Model.ansNodeFeatureNum];
						ArrayList<Double> global_feature = new ArrayList<>();
						ArrayList<Double> pair_feature = new ArrayList<>();
						ArrayList<Double> single_feature = new ArrayList<>();
						ArrayList<Double> frame_feature = new ArrayList<>();
						ArrayList<Double> Iframe_feature = new ArrayList<>();
						double z_score = 0;
						double z_score2 = 0;
						double global_score = 0;
						double p_score = 0;
						double s_score = 0;
						double f_score = 0;
						double f2_score = 0;
						if (ConfigureProcessor.queNodeFeatureNum != 0 || ConfigureProcessor.queEdgeFeatureNum != 0) {
							mpQue.setInfo(questions[i1][i2].wordNodes, model, questions[i1][i2], answers[i1][i2][i3],
									storys[i1].sentense_string, statements[i1][i2][i3], i1, i2, i3, dataType, dataSet,
									false);
							Object[] object = mpQue.messagePassing();
							z_score = (double) object[0];
							edge_feature = (double[]) object[1];
							node_feature = (double[]) object[2];
							result += z_score;

						}
						if (ConfigureProcessor.ansNodeFeatureNum != 0 || ConfigureProcessor.ansEdgeFeatureNum != 0) {
							mpAns.setInfo(answers[i1][i2][i3].wordNodes, model, questions[i1][i2], answers[i1][i2][i3],
									storys[i1].sentense_string, statements[i1][i2][i3], i1, i2, i3, dataType, dataSet,
									false);
							Object[] object = mpAns.messagePassing();
							z_score2 = (double) object[0];
							edge_feature2 = (double[]) object[1];
							node_feature2 = (double[]) object[2];
							result += z_score2;
						}
						if (ConfigureProcessor.globalFeatureNum != 0) {
							global_feature = readGlobalFeature(i1, i2, i3);
							global_score = model.getGlobalScore(global_feature);
							result += global_score;
						}
						if (ConfigureProcessor.pairFeatureNum != 0) {
							imPair.setInfo(storys[i1], questions[i1][i2], answers[i1][i2][i3], statements[i1][i2][i3],
									i1, i2, i3, dataType, dataSet, model, false);
							Object[] objects = imPair.inference();
							p_score = (double) objects[0];
							pair_feature = (ArrayList<Double>) objects[1];
							result += p_score;
						}
						if (ConfigureProcessor.singleFeatureNum != 0) {
							imSingle.setInfo(storys[i1], questions[i1][i2], answers[i1][i2][i3], statements[i1][i2][i3],
									i1, i2, i3, dataType, dataSet, model, false);
							Object[] objects = imSingle.inference();
							s_score = (double) objects[0];
							single_feature = (ArrayList<Double>) objects[1];
							result += s_score;
						}
						if (ConfigureProcessor.frameFeatureNum != 0) {
							fmQue.setInfo(storys[i1], questions[i1][i2], answers[i1][i2][i3], statements[i1][i2][i3],
									i1, i2, i3, dataType, dataSet, model, false);
							Object[] objects = fmQue.inference();
							f_score = (double) objects[0];
							frame_feature = (ArrayList<Double>) objects[1];
							result += f_score;
						}

						if (ConfigureProcessor.iframeFeatureNum != 0) {
							ifmQue.setInfo(storys[i1], questions[i1][i2], answers[i1][i2][i3], statements[i1][i2][i3],
									i1, i2, i3, dataType, dataSet, model, false);
							Object[] objects = ifmQue.inference();
							f2_score = (double) objects[0];
							Iframe_feature = (ArrayList<Double>) objects[1];
							result += f2_score;
						}

						answer_arg[i3] = new Answer(re_answer, edge_feature, node_feature, edge_feature2, node_feature2,
								global_feature, pair_feature, single_feature, frame_feature, Iframe_feature, tag,
								result);

						if (isNegation == false || (isNegation == true && anNegation == true)) {
							if (result > max_score) {
								max_score = result;
								pre_index = i3;
							}
							if (z_score > z_max_score) {
								z_max_score = z_score;
								z_pre_index = i3;
							}
							if (z_score2 > z_max_score2) {
								z_max_score2 = z_score2;
								z_pre_index2 = i3;
							}
							if (global_score > global_max_score) {
								global_max_score = global_score;
								global_pre_index = i3;
							}
							if (p_score > p_max_score) {
								p_max_score = p_score;
								p_pre_index = i3;
							}
							if (s_score > s_max_score) {
								s_max_score = s_score;
								s_pre_index = i3;
							}
							if (f_score > f_max_score) {
								f_max_score = f_score;
								f_pre_index = i3;
							}
							if (f2_score > f2_max_score) {
								f2_max_score = f2_score;
								f2_pre_index = i3;
							}

						} else {
							if (result < min_score) {
								min_score = result;
								pre_index = i3;
							}
							if (z_score < z_min_score) {
								z_min_score = z_score;
								z_pre_index = i3;
							}
							if (z_score2 < z_min_score2) {
								z_min_score2 = z_score2;
								z_pre_index2 = i3;
							}
							if (global_score < global_min_score) {
								global_min_score = global_score;
								global_pre_index = i3;
							}
							if (p_score < p_min_score) {
								p_min_score = p_score;
								p_pre_index = i3;
							}
							if (s_score < s_min_score) {
								s_min_score = s_score;
								s_pre_index = i3;
							}
							if (f_score < f_min_score) {
								f_min_score = f_score;
								f_pre_index = i3;
							}
							if (f2_score < f2_min_score) {
								f2_min_score = f2_score;
								f2_pre_index = i3;
							}

						}

					}
					if (is_list_ans && ConfigureProcessor.ansNodeFeatureNum > 0) {
						pre_index = z_pre_index2;
					}
					if (isWhy && ConfigureProcessor.pairFeatureNum > 0) {
						pre_index = p_pre_index;
					}

					if (pre_index != correct_index) {
						loop++;
						System.out.println("第" + loop + "次模型更新,");
						model.change(answer_arg[correct_index], answer_arg[pre_index], loop);
						String log = "<update count = " + loop + ">";
						String para = getParameter(model);
						writeLog(log + "\r\n" + para + "\r\n" + "</update>", "parameter" + logNum + "/" + file);
					}
				}
			}
			// correct_rate = check(model);
			// System.out.println("第" + update_count + "次扫描,正确率:" +
			// correct_rate);
			// log_info = "The count of training:" + update_count + ",accuracy:"
			// + correct_rate;
			// writeLog(log_info + "\r\n", "log"+logNum+"/" + logname);
		}
	}

	public double check(Model model) throws Exception {
		double sum = 0;
		double correct_num = 0;
		double correct_rate = 0;
		for (int i1 = 0; i1 < storys.length; i1++) {
			for (int i2 = 0; i2 < questions[i1].length; i2++) {
				sum++;
				boolean isNegation = getNegationFlag(questions[i1][i2].content);
				boolean isWhy = whyQuestionCheck(questions[i1][i2].content);
				boolean anNegation = false;
				boolean is_list_ans = isListAnswer(answer[i1][i2], questions[i1][i2].content);
				double max_score = -10000;
				double z_max_score = -10000;
				double z_max_score2 = -10000;
				double global_max_score = -10000;
				double p_max_score = -10000;
				double s_max_score = -10000;
				double f_max_score = -10000;
				double f2_max_score = 10000;
				double min_score = 10000;
				double z_min_score = 10000;
				double z_min_score2 = 10000;
				double global_min_score = 10000;
				double p_min_score = 10000;
				double s_min_score = 10000;
				double f_min_score = 10000;
				double f2_min_score = 10000;
				int pre_index = 0;
				int z_pre_index = 0;
				int z_pre_index2 = 0;
				int global_pre_index = 0;
				int p_pre_index = 0;
				int s_pre_index = 0;
				int f_pre_index = 0;
				int f2_pre_index = 0;
				int correct_index = 0;

				for (int i3 = 0; i3 < answer[i1][i2].length; i3++) {
					String[] tag_answer = answerRegular(answer[i1][i2][i3]);
					if (tag_answer[0].equals("T")) {
						correct_index = i3;
					}
					double result = 0;
					double z_score = 0;
					double z_score2 = 0;
					double global_score = 0;
					double p_score = 0;
					double s_score = 0;
					double f_score = 0;
					double f2_score = 0;

					if (ConfigureProcessor.queNodeFeatureNum != 0 || ConfigureProcessor.queEdgeFeatureNum != 0) {
						mpQue.setInfo(questions[i1][i2].wordNodes, model, questions[i1][i2], answers[i1][i2][i3],
								storys[i1].sentense_string, statements[i1][i2][i3], i1, i2, i3, dataType, dataSet,
								false);
						Object[] object = mpQue.messagePassing();
						z_score = (double) object[0];
						result += z_score;
					}
					if (ConfigureProcessor.ansNodeFeatureNum != 0 || ConfigureProcessor.ansEdgeFeatureNum != 0) {
						mpAns.setInfo(answers[i1][i2][i3].wordNodes, model, questions[i1][i2], answers[i1][i2][i3],
								storys[i1].sentense_string, statements[i1][i2][i3], i1, i2, i3, dataType, dataSet,
								false);
						Object[] object = mpAns.messagePassing();
						z_score2 = (double) object[0];
						result += z_score2;
					}
					if (ConfigureProcessor.globalFeatureNum != 0) {
						ArrayList<Double> global_feature = readGlobalFeature(i1, i2, i3);
						global_score = model.getGlobalScore(global_feature);
						result += global_score;
					}
					if (ConfigureProcessor.pairFeatureNum != 0) {
						imPair.setInfo(storys[i1], questions[i1][i2], answers[i1][i2][i3], statements[i1][i2][i3], i1,
								i2, i3, dataType, dataSet, model, false);
						Object[] objects = imPair.inference();
						p_score = (double) objects[0];
						result += p_score;
					}
					if (ConfigureProcessor.singleFeatureNum != 0) {
						imSingle.setInfo(storys[i1], questions[i1][i2], answers[i1][i2][i3], statements[i1][i2][i3], i1,
								i2, i3, dataType, dataSet, model, false);
						Object[] objects = imSingle.inference();
						s_score = (double) objects[0];
						result += s_score;
					}
					if (ConfigureProcessor.frameFeatureNum != 0) {
						fmQue.setInfo(storys[i1], questions[i1][i2], answers[i1][i2][i3], statements[i1][i2][i3], i1,
								i2, i3, dataType, dataSet, model, false);
						Object[] objects = fmQue.inference();
						f_score = (double) objects[0];
						result += f_score;
					}
					if (ConfigureProcessor.iframeFeatureNum != 0) {
						ifmQue.setInfo(storys[i1], questions[i1][i2], answers[i1][i2][i3], statements[i1][i2][i3], i1,
								i2, i3, dataType, dataSet, model, false);
						Object[] objects = ifmQue.inference();
						f2_score = (double) objects[0];
						result += f2_score;
					}
					if (isNegation == false || (isNegation == true && anNegation == true)) {
						if (result > max_score) {
							max_score = result;
							pre_index = i3;
						}
						if (z_score > z_max_score) {
							z_max_score = z_score;
							z_pre_index = i3;
						}
						if (z_score2 > z_max_score2) {
							z_max_score2 = z_score2;
							z_pre_index2 = i3;
						}
						if (global_score > global_max_score) {
							global_max_score = global_score;
							global_pre_index = i3;
						}
						if (p_score > p_max_score) {
							p_max_score = p_score;
							p_pre_index = i3;
						}
						if (s_score > s_max_score) {
							s_max_score = s_score;
							s_pre_index = i3;
						}
						if (f_score > f_max_score) {
							f_max_score = f_score;
							f_pre_index = i3;
						}
						if (f2_score > f2_max_score) {
							f2_max_score = f2_score;
							f2_pre_index = i3;
						}

					} else {
						if (result < min_score) {
							min_score = result;
							pre_index = i3;
						}
						if (z_score < z_min_score) {
							z_min_score = z_score;
							z_pre_index = i3;
						}
						if (z_score2 < z_min_score2) {
							z_min_score2 = z_score2;
							z_pre_index2 = i3;
						}
						if (global_score < global_min_score) {
							global_min_score = global_score;
							global_pre_index = i3;
						}
						if (p_score < p_min_score) {
							p_min_score = p_score;
							p_pre_index = i3;
						}
						if (s_score < s_min_score) {
							s_min_score = s_score;
							s_pre_index = i3;
						}
						if (f_score < f_min_score) {
							f_min_score = f_score;
							f_pre_index = i3;
						}
						if (f2_score < f2_min_score) {
							f2_min_score = f2_score;
							f2_pre_index = i3;
						}

					}

				}

				if (is_list_ans && ConfigureProcessor.ansNodeFeatureNum > 0) {
					pre_index = z_pre_index2;
				}
				if (isWhy && ConfigureProcessor.pairFeatureNum > 0) {
					pre_index = p_pre_index;
				}

				if (pre_index == correct_index) {
					correct_num++;
				}
			}
		}
		correct_rate = correct_num / sum;
		return correct_rate;
	}

	public String test(String modelpath, boolean onlyTest) throws Exception {
		System.out.println("开始进行测试...");
		Model m = modelLoad(modelpath);
		getLogName(modelpath);
		readSerializableData(dataNum);
		double correct_sum = 0;
		double sum = 0;
		double one_sum = 0;
		double one_correct = 0;
		double mul_sum = 0;
		double mul_correct = 0;
		int c_count = 0;
		int w_count = 0;
		String info = "";
		for (int i1 = 0; i1 < storys.length; i1++) {
			// writeTestInfo("<story id = " + i1 + ">");
			info += "<s id=" + i1 + ">\r\n";
			System.out.println("test set_story:" + i1);
			for (int i2 = 0; i2 < questions[i1].length; i2++) {
				// writeTestInfo("<question id = " + i2 + ">");
				info += "\t" + "<q id=" + i2 + ">" + questions[i1][i2].type + "\r\n";
				if (questions[i1][i2].type.equals("one")) {
					one_sum++;
				} else {
					mul_sum++;
				}
				sum++;
				boolean isNegation = getNegationFlag(questions[i1][i2].content);
				boolean isWhy = whyQuestionCheck(questions[i1][i2].content);
				boolean anNegation = false;
				boolean is_list_ans = isListAnswer(answer[i1][i2], questions[i1][i2].content);
				double max_score = -10000;
				double z_max_score = -10000;
				double z_max_score2 = -10000;
				double global_max_score = -10000;
				double p_max_score = -10000;
				double s_max_score = -10000;
				double f_max_score = -10000;
				double f2_max_score = -10000;
				double min_score = 10000;
				double z_min_score = 10000;
				double z_min_score2 = 10000;
				double global_min_score = 10000;
				double p_min_score = 10000;
				double s_min_score = 10000;
				double f_min_score = 10000;
				double f2_min_score = 10000;
				int pre_index = 0;
				int z_pre_index = 0;
				int z_pre_index2 = 0;
				int global_pre_index = 0;
				int p_pre_index = 0;
				int s_pre_index = 0;
				int f_pre_index = 0;
				int f2_pre_index = 0;
				int correct_index = 0;
				for (int i3 = 0; i3 < answer[i1][i2].length; i3++) {
					// writeTestInfo("<answer id=" + i3 + ">");
					info += "\t\t" + "<a=" + i3 + ">";
					String[] tag_answer = answerRegular(answer[i1][i2][i3]);
					if (tag_answer[0].equals("T")) {
						correct_index = i3;
					}
					if (getNegationFlag(answers[i1][i2][i3].getContent())) {
						anNegation = true;
					}

					double result = 0;
					double z_score = 0;
					double z_score2 = 0;
					double global_score = 0;
					double p_score = 0;
					double s_score = 0;
					double f_score = 0;
					double f2_score = 0;
					String gf = "";
					String df = "";
					String zf = "";
					String ff = "";
					String pf = "";
					if ((ConfigureProcessor.queEdgeFeatureNum != 0 || ConfigureProcessor.queNodeFeatureNum != 0)) {
						mpQue.setInfo(questions[i1][i2].wordNodes, m, questions[i1][i2], answers[i1][i2][i3],
								storys[i1].sentense_string, statements[i1][i2][i3], i1, i2, i3, dataType, dataSet,
								false);
						Object[] object = mpQue.messagePassing();
						z_score = (double) object[0];
						double[] e_feature = (double[]) object[1];
						z_score += isSpecialQuestion(questions[i1][i2].content, answers[i1][i2][i3].getContent(),
								answer[i1][i2]);
						result += z_score;
						info += "m_score:" + z_score + "\t";
						for (int i = 0; i < e_feature.length; i++) {
							df += i + ":" + e_feature[i] + " ";
						}
					}
					if ((ConfigureProcessor.ansNodeFeatureNum != 0 || ConfigureProcessor.ansEdgeFeatureNum != 0)) {
						mpAns.setInfo(answers[i1][i2][i3].wordNodes, m, questions[i1][i2], answers[i1][i2][i3],
								storys[i1].sentense_string, statements[i1][i2][i3], i1, i2, i3, dataType, dataSet,
								false);
						Object[] object = mpAns.messagePassing();
						z_score2 = (double) object[0];
						result += z_score2;
						info += "z_score2:" + z_score2 + "\t";
						for (int i = 0; i < answers[i1][i2][i3].wordNodes.length; i++) {
							WordNode wordnode = answers[i1][i2][i3].wordNodes[i];
							zf += wordnode.getContent() + "\r\n";
							zf += "S:" + wordnode.getCurrentSentense().getContent() + "\r\n";
							zf += "ALL:" + "\r\n";
							for (int j = 0; j < wordnode.supportSentense.size(); j++) {
								zf += wordnode.supportSentense.get(j).getContent() + "\r\n";
							}
						}
					}
					if ((ConfigureProcessor.globalFeatureNum != 0)) {
						ArrayList<Double> global_feature = readGlobalFeature(i1, i2, i3);
						global_score = m.getGlobalScore(global_feature);
						global_score += isSpecialQuestion(questions[i1][i2].content, answers[i1][i2][i3].getContent(),
								answer[i1][i2]);
						result += global_score;
						info += "g_score:" + global_score + "\t";
						// System.out.println("global_score:"+global_score);
						for (int i = 0; i < global_feature.size(); i++) {
							gf += i + ": " + global_feature.get(i) + " ";
						}
					}
					if ((ConfigureProcessor.pairFeatureNum != 0)) {
						imPair.setInfo(storys[i1], questions[i1][i2], answers[i1][i2][i3], statements[i1][i2][i3], i1,
								i2, i3, dataType, dataSet, m, false);
						Object[] objects = imPair.inference();
						p_score = (double) objects[0];
						p_score += isSpecialQuestion(questions[i1][i2].content, answers[i1][i2][i3].getContent(),
								answer[i1][i2]);
						result += p_score;
						info += "p_score:" + p_score + "\t";
						ArrayList<Double> pair_feature = (ArrayList<Double>) objects[1];
						for (int i = 0; i < pair_feature.size(); i++) {
							pf += i + ":" + pair_feature.get(i) + " ";
						}
					}
					if ((ConfigureProcessor.singleFeatureNum != 0)) {
						imSingle.setInfo(storys[i1], questions[i1][i2], answers[i1][i2][i3], statements[i1][i2][i3], i1,
								i2, i3, dataType, dataSet, m, false);
						Object[] objects = imSingle.inference();
						s_score = (double) objects[0];
						result += s_score;
						info += "s_score:" + s_score + "\t";
					}
					if ((ConfigureProcessor.frameFeatureNum != 0)) {
						fmQue.setInfo(storys[i1], questions[i1][i2], answers[i1][i2][i3], statements[i1][i2][i3], i1,
								i2, i3, dataType, dataSet, m, false);
						Object[] objects = fmQue.inference();
						f_score = (double) objects[0];
						result += f_score;
						info += "f_score" + f_score + "\t";
					}
					if ((ConfigureProcessor.iframeFeatureNum != 0)) {
						ifmQue.setInfo(storys[i1], questions[i1][i2], answers[i1][i2][i3], statements[i1][i2][i3], i1,
								i2, i3, dataType, dataSet, m, false);
						Object[] objects = ifmQue.inference();
						f2_score = (double) objects[0];
						result += f2_score;
					}

					if (isNegation == false || (isNegation == true && anNegation == true)) {
						if (result > max_score) {
							max_score = result;
							pre_index = i3;
						}
						if (z_score > z_max_score) {
							z_max_score = z_score;
							z_pre_index = i3;
						}
						if (z_score2 > z_max_score2) {
							z_max_score2 = z_score2;
							z_pre_index2 = i3;
						}
						if (global_score > global_max_score) {
							global_max_score = global_score;
							global_pre_index = i3;
						}
						if (p_score > p_max_score) {
							p_max_score = p_score;
							p_pre_index = i3;
						}
						if (s_score > s_max_score) {
							s_max_score = s_score;
							s_pre_index = i3;
						}
						if (f_score > f_max_score) {
							f_max_score = f_score;
							f_pre_index = i3;
						}
						if (f2_score > f2_max_score) {
							f2_max_score = f2_score;
							f2_pre_index = i3;
						}

					} else {
						if (result < min_score) {
							min_score = result;
							pre_index = i3;
						}
						if (z_score < z_min_score) {
							z_min_score = z_score;
							z_pre_index = i3;
						}
						if (z_score2 < z_min_score2) {
							z_min_score2 = z_score2;
							z_pre_index2 = i3;
						}
						if (global_score < global_min_score) {
							global_min_score = global_score;
							global_pre_index = i3;
						}
						if (p_score < p_min_score) {
							p_min_score = p_score;
							p_pre_index = i3;
						}
						if (s_score < s_min_score) {
							s_min_score = s_score;
							s_pre_index = i3;
						}
						if (f_score < f_min_score) {
							f_min_score = f_score;
							f_pre_index = i3;
						}
						if (f2_score < f2_min_score) {
							f2_min_score = f2_score;
							f2_pre_index = i3;
						}

					}
					// writeTestInfo("</answer>");
					info += "\r\n";
					// info += df + "\r\n";
				}

				if (is_list_ans && ConfigureProcessor.ansNodeFeatureNum > 0) {
					pre_index = z_pre_index2;
				}
				if (isWhy && ConfigureProcessor.pairFeatureNum > 0) {
					pre_index = p_pre_index;
				}

				if (ConfigureProcessor.pairFeatureNum > 0) {
					if ((z_pre_index == global_pre_index || z_pre_index2 == global_pre_index
							|| f_pre_index == global_pre_index || p_pre_index == global_pre_index)
							&& (global_pre_index == correct_index) && (pre_index != correct_index)) {
						c_count++;
					}
					if ((z_pre_index == global_pre_index || z_pre_index2 == global_pre_index
							|| f_pre_index == global_pre_index || p_pre_index == global_pre_index)
							&& (global_pre_index != correct_index) && (pre_index == correct_index)) {
						w_count++;
					}
				}else{
					if ((z_pre_index == global_pre_index || z_pre_index2 == global_pre_index
							|| f_pre_index == global_pre_index)
							&& (global_pre_index == correct_index) && (pre_index != correct_index)) {
						c_count++;
					}
					if ((z_pre_index == global_pre_index || z_pre_index2 == global_pre_index
							|| f_pre_index == global_pre_index)
							&& (global_pre_index != correct_index) && (pre_index == correct_index)) {
						w_count++;
					}
				}



			//	if (z_pre_index == global_pre_index || z_pre_index2 == global_pre_index
			//			|| p_pre_index == global_pre_index || f_pre_index == global_pre_index) {
			//		pre_index = global_pre_index;
			//	}

				info += "\t\t*" + correct_index + "," + pre_index + "\r\n";

				// if(global_pre_index == p_pre_index){
				// pre_index = global_pre_index;
				// }else if(global_pre_index == z_pre_index){
				// pre_index = global_pre_index;
				// }else if(p_pre_index == z_pre_index){
				// pre_index = p_pre_index;
				// }else{
				// pre_index = pre_index;
				// }

				if (pre_index == correct_index) {
					correct_sum++;
					if (questions[i1][i2].type.equals("one")) {
						one_correct++;
					} else {
						mul_correct++;
					}
				}
				// writeTestInfo("predicted answer:" +
				// answer_arg[pre_index].content);
				// writeTestInfo("</question>");
			}
			// writeTestInfo("</story>" + "\r\n\r\n");
		}

		double correct_rate = correct_sum / sum;
		double mul_correct_rate = mul_correct / mul_sum;
		double one_correct_rate = one_correct / one_sum;
		System.out.println("共有问题:" + sum + ",正确个数:" + correct_sum + ",正确率:" + correct_rate);
		System.out.println("one型问题:" + one_sum + ",正确个数:" + one_correct + ",正确率:" + one_correct_rate);
		System.out.println("multi型问题:" + mul_sum + ",正确个数:" + mul_correct + ",正确率:" + mul_correct_rate);
		System.out.println("投票纠正数:" + c_count + ",投票做错数:" + w_count);
		String result = "The result of test:\r\n " + "Sum:" + sum + ",correct number:" + correct_sum + ",accuracy:"
				+ correct_rate + "\r\n" + "one type:" + one_sum + ",correct num:" + one_correct + ",accuracy:"
				+ one_correct_rate + "\r\n" + "multi type:" + mul_sum + ",correct num:" + mul_correct + ",accuracy:"
				+ mul_correct_rate + "\r\n" + "投票纠正数:" + c_count + ",投票做错数:" + w_count + "\r\n";
		if (!onlyTest) {
			String logname = modelpath.replace("para_", "log_").substring(0, modelpath.lastIndexOf("_") - 1);
			writeLog(result, "log" + logNum + "/" + logname);
		}
		// writeLog(info, "sum_35_23_dev_result");

		return result;

	}

	public void getLogName(String path) {
		logModelName = path;
	}

	public Model modelLoad(String modelname) {
		Model m = null;
		File file = new File("../src/res/Configures/parameter" + logNum + "/"
				+ modelname + ".txt");
		FileReader fr;
		BufferedReader br;
		double[] edge_feature_weight = new double[Model.queEdgeFeatureNum];
		double[] node_feature_weight = new double[Model.queNodeFeatureNum];
		double[] global_feature_weight = new double[Model.globalFeatureNum];
		double[] pair_feature_weight = new double[Model.pairFeatureNum];
		double[] single_feature_weight = new double[Model.singleFeatureNum];
		double[] edge_feature2_weight = new double[Model.ansEdgeFeatureNum];
		double[] node_feature2_weight = new double[Model.ansNodeFeatureNum];
		double[] frame_feature_weight = new double[Model.frameFeatureNum];
		double[] Iframe_feature_weight = new double[Model.iframeFeatureNum];
		int count = 0;
		try {
			fr = new FileReader(file);
			br = new BufferedReader(fr);
			String data = null;
			int line = 0;
			while ((data = br.readLine()) != null) {
				if (data.contains("<update")) {
					count++;
					line++;
				} else if (line == 1) {
					String[] edge_features_string = data.split(" ");
					vectorCombine(edge_feature_weight, edge_features_string);
					line++;
				} else if (line == 2) {
					String[] node_features_string = data.split(" ");
					vectorCombine(node_feature_weight, node_features_string);
					line++;
				} else if (line == 3) {
					String[] global_features_string = data.split(" ");
					vectorCombine(global_feature_weight, global_features_string);
					line++;
				} else if (line == 4) {
					String[] pair_features_string = data.split(" ");
					vectorCombine(pair_feature_weight, pair_features_string);
					line++;
				} else if (line == 5) {
					String[] single_features_string = data.split(" ");
					vectorCombine(single_feature_weight, single_features_string);
					line++;
				} else if (line == 6) {
					String[] edge_features2_string = data.split(" ");
					vectorCombine(edge_feature2_weight, edge_features2_string);
					line++;
				} else if (line == 7) {
					String[] node_features2_string = data.split(" ");
					vectorCombine(node_feature2_weight, node_features2_string);
					line++;
				} else if (line == 8) {
					String[] frame_features_string = data.split(" ");
					vectorCombine(frame_feature_weight, frame_features_string);
					line++;
				} else if (line == 9) {
					String[] Iframe_features_string = data.split(" ");
					vectorCombine(Iframe_feature_weight, Iframe_features_string);
					line++;
				} else if (data.contains("</update>")) {
					line = 0;
				}
			}
			br.close();
			fr.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (count != 0) {
			for (int i = 0; i < edge_feature_weight.length; i++) {
				edge_feature_weight[i] = edge_feature_weight[i] / (double) (count);
			}
			for (int i = 0; i < node_feature_weight.length; i++) {
				node_feature_weight[i] = node_feature_weight[i] / (double) (count);
			}

			for (int i = 0; i < global_feature_weight.length; i++) {
				global_feature_weight[i] = global_feature_weight[i] / (double) (count);
			}

			for (int i = 0; i < pair_feature_weight.length; i++) {
				pair_feature_weight[i] = pair_feature_weight[i] / (double) (count);
			}

			for (int i = 0; i < single_feature_weight.length; i++) {
				single_feature_weight[i] = single_feature_weight[i] / (double) (count);
			}
			for (int i = 0; i < edge_feature2_weight.length; i++) {
				edge_feature2_weight[i] = edge_feature2_weight[i] / (double) (count);
			}
			for (int i = 0; i < node_feature2_weight.length; i++) {
				node_feature2_weight[i] = node_feature2_weight[i] / (double) (count);
			}
			for (int i = 0; i < frame_feature_weight.length; i++) {
				frame_feature_weight[i] = frame_feature_weight[i] / (double) (count);
			}
			for (int i = 0; i < Iframe_feature_weight.length; i++) {
				Iframe_feature_weight[i] = Iframe_feature_weight[i] / (double) (count);
			}

			m = new Model(edge_feature_weight, node_feature_weight, global_feature_weight, pair_feature_weight,
					single_feature_weight, edge_feature2_weight, node_feature2_weight, frame_feature_weight,
					Iframe_feature_weight);
		}
		return m;
	}

	public void vectorCombine(double[] vector, String[] vector_str) {
		for (int i = 0; i < vector.length; i++) {
			vector[i] += Double.valueOf(vector_str[i]);
		}
	}

	public String getParameter(Model model) {
		String v1 = "";
		String v2 = "";
		String v3 = "";
		String v4 = "";
		String v5 = "";
		String v6 = "";
		String v7 = "";
		String v8 = "";
		String v9 = "";
		for (int i = 0; i < model.v1.length; i++) {
			v1 += model.v1[i] + " ";
		}
		for (int i = 0; i < model.v2.length; i++) {
			v2 += model.v2[i] + " ";
		}
		for (int i = 0; i < model.v3.length; i++) {
			v3 += model.v3[i] + " ";
		}
		for (int i = 0; i < model.v4.length; i++) {
			v4 += model.v4[i] + " ";
		}
		for (int i = 0; i < model.v5.length; i++) {
			v5 += model.v5[i] + " ";
		}
		for (int i = 0; i < model.v6.length; i++) {
			v6 += model.v6[i] + " ";
		}
		for (int i = 0; i < model.v7.length; i++) {
			v7 += model.v7[i] + " ";
		}
		for (int i = 0; i < model.v8.length; i++) {
			v8 += model.v8[i] + " ";
		}
		for (int i = 0; i < model.v9.length; i++) {
			v9 += model.v9[i] + " ";
		}
		String string = v1 + "\r\n" + v2 + "\r\n" + v3 + "\r\n" + v4 + "\r\n" + v5 + "\r\n" + v6 + "\r\n" + v7 + "\r\n"
				+ v8 + "\r\n" + v9 + "\r\n";
		return string;
	}

	public void writeLog(String str, String filename) {
		String path = "../src/res/Configures/" + filename + ".txt";
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

	public void frameCheck() throws Exception {
		readSerializableData(dataNum);
		String log = "";
		for (int i = 0; i < storys.length; i++) {
			log += "<s= " + i + ">" + "\r\n";
			for (int i2 = 0; i2 < storys[i].sentenses.length; i2++) {
				log += storys[i].sentense_string[i2] + "\r\n";
				for (int i3 = 0; i3 < storys[i].sentenses[i2].frameList.size(); i3++) {
					String target = "T_name:" + storys[i].sentenses[i2].frameList.get(i3).target.name + ",T_text:"
							+ storys[i].sentenses[i2].frameList.get(i3).target.text;
					log += target + "\r\n";
					for (int i4 = 0; i4 < storys[i].sentenses[i2].frameList.get(i3).elementsList.size(); i4++) {
						String element = "E_name:" + storys[i].sentenses[i2].frameList.get(i3).elementsList.get(i4).name
								+ ",E_text:" + storys[i].sentenses[i2].frameList.get(i3).elementsList.get(i4).text;
						log += element + "\r\n";
					}
				}
			}
			for (int q = 0; q < questions[i].length; q++) {
				log += "<q= " + q + ">\r\n";
				log += questions[i][q].content;
				for (int q2 = 0; q2 < questions[i][q].frameList.size(); q2++) {
					String target = "T_name:" + questions[i][q].frameList.get(q2).target.name + ",T_text:"
							+ questions[i][q].frameList.get(q2).target.text;
					log += target + "\r\n";
					for (int q3 = 0; q3 < questions[i][q].frameList.get(q2).elementsList.size(); q3++) {
						String element = "E_name:" + questions[i][q].frameList.get(q2).elementsList.get(q3).name
								+ ",E_text:" + questions[i][q].frameList.get(q2).elementsList.get(q3).text;
						log += element + "\r\n";
					}
				}
				for (int a = 0; a < answers[i][q].length; a++) {
					log += "<a= " + a + ">\r\n";
					log += answers[i][q][a].content;
					for (int a2 = 0; a2 < answers[i][q][a].framesList.size(); a2++) {
						String target = "T_name:" + answers[i][q][a].framesList.get(a2).target.name + ",T_text:"
								+ answers[i][q][a].framesList.get(a2).target.text;
						log += target + "\r\n";
						for (int a3 = 0; a3 < answers[i][q][a].framesList.get(a2).elementsList.size(); a3++) {
							String element = "E_name:" + answers[i][q][a].framesList.get(a2).elementsList.get(a3).name
									+ ",E_text:" + answers[i][q][a].framesList.get(a2).elementsList.get(a3).text;
							log += element + "\r\n";
						}
					}
				}
			}
		}
		writeLog(log, "frame_check");
		System.out.println("checking is over...");
	}

}
