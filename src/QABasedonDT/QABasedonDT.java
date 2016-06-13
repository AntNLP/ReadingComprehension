package QABasedonDT;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import Corpus.Corpus;
import FeatureExtraction.FeatureExtract_DT;
import FeatureExtraction.FeatureExtract_Global;
import Tools.Tools;
import Tools.Word2VEC;
import edu.stanford.nlp.io.EncodingPrintWriter.out;

public class QABasedonDT {
	/*************************
	 * 属性
	 */
	String[] storyArg;
	String[][] question;
	String[][][] answer;
	int[][] answerResult;
	int[][] questionType;
	Corpus c;
	static Tools tools;
	ArrayList<String> stop_word;
	Word2VEC word2vec;
	Model model;
	Story[] storys;
	Question[][] questions;
	MessagePassing mp;
	FeatureExtract_DT fx;;

	/*********************************
	 * 方法
	 * 
	 * @param path
	 * @param count
	 */

	public QABasedonDT(String path, int count) {
		c = new Corpus(path, count);
		getCorpus(c);
		tools = new Tools();
		stop_word = new ArrayList<>();
		// word2vec = new Word2VEC();
		getStop_WordList();
		model = new Model();
		storys = new Story[storyArg.length];
		questions = new Question[storyArg.length][4];
		fx = new FeatureExtract_DT();
		mp = new MessagePassing(fx);
	}

	public void getCorpus(Corpus c) {
		this.storyArg = c.getStory();
		this.question = c.getQuestion();
		this.answer = c.getAnswer();
	}

	public void getStop_WordList() {
		String path = "./src/res/stop_word/stop_word2.txt";
		File file = new File(path);
		FileReader fr;
		BufferedReader br;
		try {
			fr = new FileReader(file);
			br = new BufferedReader(fr);
			String word = null;
			while ((word = br.readLine()) != null) {
				word = word.trim();
				stop_word.add(word);
			}
			br.close();
			fr.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String punctution_preprocess(String string) {
		string = string.replace("\\'", " \\'");
		return string.trim();
	}

	public String content_preprocess(String string) {
		string = string.replaceAll("[\\pP‘'“”]", "");
		string = tools.getLemma(string);
		string = string.toLowerCase().trim();
		return string;
	}

	// public String[] storyDivided(String story) {
	// String[] sentense;
	// sentense = story.split("\\.|\\?|;|:|\"|!|\\,");
	// return sentense;
	// }

	public String questionRegular(String question) {
		int firstIndex = question.indexOf(":");
		int secondIndex = question.indexOf(":", firstIndex + 1);
		question = question.substring(secondIndex + 1);
		question = question.replaceAll("\\?", "").trim();
		return question;
	}

	public boolean get_Negation_Feature(String question) {
		boolean flag = false;
		String[] negation_words = { "not", "didn't", "doesn't", "don't", "isn't", "aren't" };
		for (int i = 0; i < negation_words.length; i++) {
			if (question.contains(negation_words[i])) {
				flag = true;
				break;
			}
		}
		return flag;
	}

	public String[] answerRegular(String answer) {
		String tag = "F";
		if (answer.contains("*")) {
			tag = "T";
		}
		answer = answer.substring(answer.indexOf(")") + 1);
		String[] tag_answer = { tag, answer };
		return tag_answer;
	}

	// public void find_supportsentense(Word_Node[] word_nodes, String[]
	// story_sentense) {
	// for (Word_Node w : word_nodes) {
	// String word_content = content_preprocess(w.content);
	// if (!stop_word.contains(word_content) && word_content.trim().length() !=
	// 0) {
	// for (int i = 0; i < story_sentense.length; i++) {
	// if (story_sentense[i].trim().length() == 0) {
	// continue;
	// }
	// String sentense = punctution_preprocess(story_sentense[i]);
	// String[] sentense_word = sentense.split(" ");
	// for (int j = 0; j < sentense_word.length; j++) {
	// Sentense s = new Sentense(i, sentense, j);
	// String sentense_w = content_preprocess(sentense_word[j]);
	// if (sentense_w.equals(word_content)) {
	// s.match_flag = 1;
	// s.w2v_flag = 1;
	// w.support_sentense.add(s);
	// // break;
	// }
	// // else if(word2vec.getSimiliar(sentense_w,
	// // word_content) > 0.65){
	// // s.w2v_flag = 1;
	// // w.support_sentense.add(s);
	// //// break;
	// // }
	// }
	// }
	// }
	// if (w.support_sentense.size() == 0) {
	// w.support_sentense.add(new Sentense(0, "*", 0));
	// }
	// }
	// }

	public void find_supportsentense(Word_Node[] word_nodes, Sentense[] story_sentense) {
		for (Word_Node w : word_nodes) {
			String word_content = content_preprocess(w.content);
			if (!stop_word.contains(word_content) && word_content.trim().length() != 0) {
				for (int i = 0; i < story_sentense.length; i++) {
					if (story_sentense[i].content.trim().length() == 0) {
						continue;
					}
					story_sentense[i].content = punctution_preprocess(story_sentense[i].content);
					Word_Node[] sentense_word = story_sentense[i].word_nodes;
					for (int j = 0; j < sentense_word.length; j++) {
						story_sentense[i].sentense_index = i;
						story_sentense[i].word_index = j;
						String sentense_w = content_preprocess(sentense_word[j].content);
						if (sentense_w.equals(word_content)) {
							story_sentense[i].match_flag = 1;
							story_sentense[i].w2v_flag = 1;
							w.support_sentense.add(story_sentense[i]);
							// break;
						}
						// else if(word2vec.getSimiliar(sentense_w,
						// word_content) > 0.65){
						// s.w2v_flag = 1;
						// w.support_sentense.add(s);
						//// break;
						// }
					}
				}
			}
			if (w.support_sentense.size() == 0) {
				w.support_sentense.add(new Sentense(0, "*", 0));
			}
		}
	}

	public void pre_process() throws Exception {
		for (int i1 = 0; i1 < storys.length; i1++) {
			String story = storyArg[i1];
			story = tools.coreference(story);
			storys[i1] = Story.getNewStroy(story);
			for (int i2 = 0; i2 < questions[i1].length; i2++) {
				String type = "one";
				if (question[i1][i2].contains("multiple:")) {
					type = "mul";
				}
				String re_question = questionRegular(question[i1][i2]);
				String core_question = tools.coreference(re_question);
				questions[i1][i2] = Question.getNewQuestion(core_question, type);
				find_supportsentense(questions[i1][i2].word_node, storys[i1].sentenses);
			}
			System.out.println("pre_process: story" + i1);
		}
		serializableProcess();
	}
	
	public void read_serializableData(String type) throws Exception{
		for(int i = 0;i < storys.length;i++){
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream("./src/res/serializedata/mc500."+type+"/story/s"+i+".out"));
			storys[i] = (Story)ois.readObject();
			ois.close();
		}
		for(int i = 0; i < questions.length;i++){
			for(int j = 0;j < questions[i].length;j++){
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream("./src/res/serializedata/mc500."+type+"/question/s"+i+"_q"+j+".out"));
				questions[i][j] = (Question)ois.readObject();
				ois.close();
			}
		}
	}
	
	public void serializableProcess() throws Exception{
		for(int i = 0;i < storys.length;i++){
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("./src/res/serializedata/mc500.test/story/s"+i+".out"));
			oos.writeObject(storys[i]);
			oos.close();
		}
		for(int i = 0;i < questions.length;i++){
			for(int j = 0;j < questions[i].length;j++){
				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("./src/res/serializedata/mc500.test/question/s"+i+"_q"+j+".out"));
				oos.writeObject(questions[i][j]);
				oos.close();
			}
		}
	}

	public void train(String file) throws Exception {
//		pre_process();
		read_serializableData("train");
		FeatureExtract_DT fx = new FeatureExtract_DT();
		FeatureExtract_Global fx_global = new FeatureExtract_Global();
		int loop = 0;
		double correct_rate = 0;
		int update_count = 0;
		outer: while (update_count <= 5) {
			update_count++;
			// double correct_sum = 0;
			// double sum = 0;
			for (int i1 = 0; i1 < storys.length; i1++) {
				for (int i2 = 0; i2 < questions[i1].length; i2++) {
					// sum++;
					boolean isNegation = get_Negation_Feature(questions[i1][i2].content);
					Answer[] answer_arg = new Answer[4];
					double max_score = 0;
					double min_score = 10000;
					int pre_index = 0;
					int correct_index = 0;
					for (int i3 = 0; i3 < answer[i1][i2].length; i3++) {
						String[] tag_answer = answerRegular(answer[i1][i2][i3]);
						double tag = -1;
						if (tag_answer[0].equals("T")) {
							correct_index = i3;
							tag = 1;
						}
						String re_answer = tag_answer[1];
						 mp.setInfo(questions[i1][i2].word_node, model,
						 questions[i1][i2].content, re_answer,
						 storys[i1].sentense_string);
						 Object[] object = mp.messagePassing();
						 double z_score = (double) object[0];
						 double[] edge_feature = (double[]) object[1];
						 double[] node_feature = (double[]) object[2];

						// System.out.println("问题"+i2+",答案:"+i3+"的边特征");
						// for(int i = 0;i <edge_feature.length;i++){
						// System.out.print(edge_feature[i]+" ");
						// }
						// System.out.println();
						// System.out.println("问题"+i2+",答案:"+i3+"的点特征");
						// for(int i = 0;i <node_feature.length;i++){
						// System.out.print(node_feature[i]+" ");
						// }
						// System.out.println();
						// System.out.println("问题"+i2+",答案:"+i3+"的得分");
						// for(int i = 0;i
						// <questions[i1][i2].word_node.length;i++){
						// System.out.println("节点"+questions[i1][i2].word_node[i].content+",支持句："+questions[i1][i2].word_node[i].currentSentense.content);
						// }
						// System.out.println();
						// System.out.println("问题"+i2+",答案:"+i3+re_answer);
						double[] global_feature = fx_global.getGlobalFeature(re_answer, questions[i1][i2].content,
								storys[i1]);
						double global_score = model.getGlobalScore(global_feature);
						// System.out.println("问题"+i2+",答案:"+i3+"的全局得分"+global_score);
						double result = z_score+global_score;
						answer_arg[i3] = new Answer(re_answer, edge_feature, node_feature, global_feature, tag, result);
						if (isNegation == false) {
							if (Math.exp(answer_arg[i3].score) > max_score) {
								max_score = Math.exp(answer_arg[i3].score);
								pre_index = i3;
							}
						} else {
							if (Math.exp(answer_arg[i3].score) < min_score) {
								min_score = Math.exp(answer_arg[i3].score);
								pre_index = i3;
							}
						}
					}
					if (pre_index != correct_index) {
						loop++;
						System.out.println("第" + loop + "次模型更新,");
						model.change(answer_arg[correct_index], answer_arg[pre_index], loop);
						String log = "<update count = " + loop + ">";
						String para = getParameter(model);
						writeLog(log + "\r\n" + para + "\r\n" + "</update>", file);
					} else {
						// correct_sum++;
					}
					// System.out.println("question:"+i2);
					// for(int i = 0;i <questions[i1][i2].word_node.length;i++){
					// System.out.println(questions[i1][i2].word_node[i].currentSentense.content+"
					// "+questions[i1][i2].word_node[i].currentSentense.sentense_index);
					// }
				}
			}
			// correct_rate = correct_sum / sum;
			// if(correct_rate >= 0.85){
			// flag = false;
			// }
			correct_rate = check(model);
			System.out.println("第" + update_count + "次扫描,正确率:" + correct_rate);
			String info = "第" + update_count + "次扫描,正确率:" + correct_rate;
			writeLog(info, "update_"+file);
		}
	}

	public double check(Model model) {
		FeatureExtract_Global fx_global = new FeatureExtract_Global();
		double sum = 0;
		double correct_num = 0;
		double correct_rate = 0;
		for (int i1 = 0; i1 < storys.length; i1++) {
			for (int i2 = 0; i2 < questions[i1].length; i2++) {
				sum++;
				boolean isNegation = get_Negation_Feature(questions[i1][i2].content);
				double max_score = 0;
				double min_score = 1000;
				int pre_index = 0;
				int correct_index = 0;
				for (int i3 = 0; i3 < answer[i1][i2].length; i3++) {
					String[] tag_answer = answerRegular(answer[i1][i2][i3]);
					if (tag_answer[0].equals("T")) {
						correct_index = i3;
					}
					String re_answer = tag_answer[1];
					mp.setInfo(questions[i1][i2].word_node, model, questions[i1][i2].content, re_answer,
							storys[i1].sentense_string);
					Object[] object = mp.messagePassing();
					double z_score = (double) object[0];
					double[] global_feature = fx_global.getGlobalFeature(re_answer, questions[i1][i2].content,
							storys[i1]);
					double global_score = model.getGlobalScore(global_feature);
					double result = z_score+global_score;
					if (isNegation == false) {
						if (Math.exp(result) > max_score) {
							max_score = Math.exp(result);
							pre_index = i3;
						}
					} else {
						if (Math.exp(result) < min_score) {
							min_score = Math.exp(result);
							pre_index = i3;
						}
					}
				}
				if (pre_index == correct_index) {
					correct_num++;
				} else {
					// System.out.println("问题" + i2 + "正确答案为" + correct_index +
					// ",预测答案为:" + pre_index);
				}
			}
		}
		correct_rate = correct_num / sum;
		// System.out.println("正确率:" + correct_rate);
		// for (int i = 0; i < model.v1.length; i++) {
		// System.out.print(model.v1[i] + " ");
		// }
		// System.out.println();
		// for (int i = 0; i < model.v2.length; i++) {
		// System.out.print(model.v2[i] + " ");
		// }
		// System.out.println();
		// for (int i = 0; i < model.v3.length; i++) {
		// System.out.print(model.v3[i] + " ");
		// }
		// System.out.println();
		return correct_rate;
	}

	// public void train() {
	// pre_train();
	// double correct_rate = 0;
	// int loop = 0;
	// boolean flag =true;
	// outer:
	// while (flag) {
	// for (int i1 = 0; i1 < storys.length; i1++) {
	// for (int i2 = 0; i2 < questions[i1].length; i2++) {
	// for (int i3 = 0; i3 < answer[i1][i2].length; i3++) {
	// String[] tag_answer = answerRegular(answer[i1][i2][i3]);
	// double tag = -1;
	// if (tag_answer[0].equals("T")) {
	// tag = 1;
	// }
	// String re_answer = tag_answer[1];
	// mp.setInfo(questions[i1][i2].word_node, model,
	// questions[i1][i2].content,re_answer,
	// storys[i1].sentense_string);
	// Object[] object = mp.messagePassing();
	// double[] edge_feature = (double[]) object[0];
	// double[] node_feature = (double[]) object[1];
	//// double[] answer_feature = (double[])object[2];
	//
	//// String word_info = "";
	//// for (int i = 0; i < questions[i1][i2].word_node.length; i++) {
	//// word_info += questions[i1][i2].word_node[i].content + ":"
	//// + questions[i1][i2].word_node[i].currentSentense.content + "\r\n";
	//// }
	//// String answer_info = "";
	//// for (int i = 0; i < answer_feature.length; i++) {
	//// answer_info += answer_feature[i] + " ";
	//// }
	// double e_score = model.getEdgeScore(edge_feature);
	// double v_score = model.getNodeScore(node_feature);
	//// double a_score = model.getAnswerScore(answer_feature);
	//// double exp = model.getResult(a_score+e_score+v_score);
	// double exp = model.getResult(e_score+v_score);
	// if((exp >= 0 && tag == -1)||(exp < 0 && tag == 1) ){
	// // update++;
	// loop++;
	// System.out.println("第" + loop + "次模型检测,");
	//// model.change(edge_feature, node_feature, answer_feature, tag);
	// model.change(edge_feature, node_feature, tag,loop);
	// correct_rate = check(model);
	// System.out.println("正确率:" + correct_rate);
	// String log = "正确率：" + correct_rate;
	// String para = getParameter(model);
	// writeLog(log + "\r\n" + para);
	// // String log = "第"+update+"次更新，第"+i1+"篇文章,问题"+i2;
	// // String para = getParameter(model);
	// //
	// writeLog(log+"\r\n"+word_info+"\r\n"+"边特征:\r\n"+edge_info+"\r\n节点特征:\r\n"+node_info+"\r\n答案特征:\r\n"+answer_info+"\r\n"+para);
	// }
	// }
	// if(correct_rate > 0.8){
	// break outer;
	// }
	// // System.out.println("question:"+i2);
	// // for(int i = 0;i <questions[i1][i2].word_node.length;i++){
	// //
	// System.out.println(questions[i1][i2].word_node[i].currentSentense.content+"
	// // "+questions[i1][i2].word_node[i].currentSentense.sentense_index);
	// // }
	// }
	// }
	// // correct_rate = correct_num / sum;
	// // String log = "第" + loop + "次迭代,正确个数为:" + correct_num + "总数为:" +
	// // sum + "正确率：" + correct_rate;
	// // String para = getParameter(model);
	// // writeLog(log + "\r\n" + para);
	// }
	// }
	//
	// public double check(Model model) {
	// double sum = 0;
	// double correct_num = 0;
	// double correct_rate = 0;
	// for (int i1 = 0; i1 < storys.length; i1++) {
	// for (int i2 = 0; i2 < questions[i1].length; i2++) {
	//
	// for (int i3 = 0; i3 < answer[i1][i2].length; i3++) {
	// String[] tag_answer = answerRegular(answer[i1][i2][i3]);
	// double tag = -1;
	// if (tag_answer[0].equals("T")) {
	// tag = 1;
	// }
	// String re_answer = tag_answer[1];
	// mp.setInfo(questions[i1][i2].word_node, model,
	// questions[i1][i2].content,re_answer,
	// storys[i1].sentense_string);
	// Object[] object = mp.messagePassing();
	// double[] edge_feature = (double[]) object[0];
	// double[] node_feature = (double[]) object[1];
	//// double[] answer_feature = (double[])object[2];
	// double e_score = model.getEdgeScore(edge_feature);
	// double v_score = model.getNodeScore(node_feature);
	//// double a_score = model.getAnswerScore(answer_feature);
	//// double exp = model.getResult(a_score+e_score+v_score);
	// double exp = model.getResult(e_score+v_score);
	// sum++;
	// System.out.println("问题"+i2+",答案"+i3+","+exp);
	// if(exp > 0){
	// System.out.println("true");
	// }else{
	// System.out.println("false");
	// }
	// if ((exp >= 0 && tag == 1)||(exp < 0 && tag == -1)) {
	// correct_num++;
	// }
	// }
	// }
	// }
	// correct_rate = correct_num / sum;
	// System.out.println("正确率:" + correct_rate);
	// System.out.println("模型参数:\r\n");
	// for (int i = 0; i < model.v1.length; i++) {
	// System.out.print(model.v1[i] + " ");
	// }
	// System.out.println();
	// for (int i = 0; i < model.v2.length; i++) {
	// System.out.print(model.v2[i] + " ");
	// }
	// System.out.println();
	// for (int i = 0; i < model.v3.length; i++) {
	// System.out.print(model.v3[i] + " ");
	// }
	// System.out.println();
	// return correct_rate;
	// }

	public void test(String modelpath) throws Exception {
		Model m = modelLoad(modelpath);
//		pre_process();
		read_serializableData("test");
		FeatureExtract_Global fx_global = new FeatureExtract_Global();
		double correct_sum = 0;
		double sum = 0;
		double one_sum = 0;
		double one_correct = 0;
		double mul_sum = 0;
		double mul_correct = 0;
		for (int i1 = 0; i1 < storys.length; i1++) {
			for (int i2 = 0; i2 < questions[i1].length; i2++) {
				if (questions[i1][i2].type.equals("one")) {
					one_sum++;
				} else {
					mul_sum++;
				}
				sum++;
				boolean isNegation = get_Negation_Feature(questions[i1][i2].content);
				Answer[] answer_arg = new Answer[4];
				double max_score = 0;
				double min_score = 10000;
				int pre_index = 0;
				int correct_index = 0;
				for (int i3 = 0; i3 < answer[i1][i2].length; i3++) {
					String[] tag_answer = answerRegular(answer[i1][i2][i3]);
					double tag = -1;
					if (tag_answer[0].equals("T")) {
						correct_index = i3;
						tag = 1;
					}
					String re_answer = tag_answer[1];
					mp.setInfo(questions[i1][i2].word_node, m, questions[i1][i2].content, re_answer,
							storys[i1].sentense_string);
					Object[] object = mp.messagePassing();
					double z_score = (double) object[0];
					double[] edge_feature = (double[]) object[1];
					double[] node_feature = (double[]) object[2];
					double[] global_feature = fx_global.getGlobalFeature(re_answer, questions[i1][i2].content,
							storys[i1]);
					double global_score = model.getGlobalScore(global_feature);
					double result = z_score+global_score;
					answer_arg[i3] = new Answer(re_answer, edge_feature, node_feature, global_feature, tag, result);
					if (isNegation == false) {
						if (Math.exp(answer_arg[i3].score) > max_score) {
							max_score = Math.exp(answer_arg[i3].score);
							pre_index = i3;
						}
					}else{
						if (Math.exp(answer_arg[i3].score) < min_score) {
							min_score = Math.exp(answer_arg[i3].score);
							pre_index = i3;
						}
					}
				}
				if (pre_index == correct_index) {
					correct_sum++;
					if (questions[i1][i2].type.equals("one")) {
						one_correct++;
					} else {
						mul_correct++;
					}
				}
			}
		}
		double correct_rate = correct_sum / sum;
		double mul_correct_rate = mul_correct / mul_sum;
		double one_correct_rate = one_correct / one_sum;
		System.out.println("共有问题:" + sum + ",正确个数:" + correct_sum + ",正确率:" + correct_rate);
		System.out.println("one型问题:" + one_sum + ",正确个数:" + one_correct + ",正确率:" + one_correct_rate);
		System.out.println("multi型问题:" + mul_sum + ",正确个数:" + mul_correct + ",正确率:" + mul_correct_rate);

		// writeLog(info, "update5.txt");

	}

	public Model modelLoad(String path) {
		Model m = null;
		File file = new File(path);
		FileReader fr;
		BufferedReader br;
		double[] edge_feature_weight = new double[model.edge_feature_num];
		double[] node_feature_weight = new double[model.node_feature_num];
		double[] global_feature_weight = new double[model.global_feature_num];
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
				} 
				else if (data.contains("</update>")) {
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
				System.out.print(edge_feature_weight[i] + " ");
			}
			System.out.println();
			for (int i = 0; i < node_feature_weight.length; i++) {
				node_feature_weight[i] = node_feature_weight[i] / (double) (count);
				System.out.print(node_feature_weight[i] + " ");
			}
			System.out.println();

			for (int i = 0; i < global_feature_weight.length; i++) {
				global_feature_weight[i] = global_feature_weight[i] / (double) (count);
				System.out.print(global_feature_weight[i] + " ");
			}
			System.out.println();
			m = new Model(edge_feature_weight, node_feature_weight, global_feature_weight);
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
		for (int i = 0; i < model.v1.length; i++) {
			v1 += model.v1[i] + " ";
		}
		for (int i = 0; i < model.v2.length; i++) {
			v2 += model.v2[i] + " ";
		}
		for (int i = 0; i < model.v3.length; i++) {
			v3 += model.v3[i] + " ";
		}
		String string = v1 + "\r\n" + v2 + "\r\n" + v3 + "\r\n";
		return string;
	}

	public void writeLog(String str, String filename) {
		String path = "./src/res/Log/" + filename;
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
