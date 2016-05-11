package QABasedonDT;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import Corpus.Corpus;
import FeatureExtraction.FeatureExtract_DT;
import Tools.Tools;
import Tools.Word2VEC;

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

	public String[] answerRegular(String answer) {
		String tag = "F";
		if (answer.contains("*")) {
			tag = "T";
		}
		answer = answer.substring(answer.indexOf(")") + 1);
		answer = content_preprocess(answer);
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

	public void pre_train() {
		for (int i1 = 0; i1 < storys.length; i1++) {
			String story = storyArg[i1];
			story = tools.coreference(story);
			storys[i1] = Story.getNewStroy(story);
			for (int i2 = 0; i2 < questions[i1].length; i2++) {
				String re_question = questionRegular(question[i1][i2]);
				String core_question = tools.coreference(re_question);
				questions[i1][i2] = Question.getNewQuestion(core_question);
				find_supportsentense(questions[i1][i2].word_node, storys[i1].sentenses);
			}
			System.out.println("pre_train: story" + i1);
		}
	}

	public void train() {
		pre_train();
		FeatureExtract_DT fx = new FeatureExtract_DT();
		double correct_rate = 0;
		int loop = 0;
		boolean flag =true;
		outer:
		while (flag) {
			for (int i1 = 0; i1 < storys.length; i1++) {
				for (int i2 = 0; i2 < questions[i1].length; i2++) {
					mp.setInfo(questions[i1][i2].word_node, model, questions[i1][i2].content,
							storys[i1].sentense_string);
					Object[] object = mp.messagePassing();
					double z_score = (double) object[0];
					double[] edge_feature = (double[]) object[1];
					double[] node_feature = (double[]) object[2];
					String edge_info = "";
					String node_info = "";
					for (int i = 0; i < edge_feature.length; i++) {
						edge_info += edge_feature[i] + " ";
						// System.out.print(edge_feature[i]+" ");
					}
					for (int i = 0; i < node_feature.length; i++) {
						node_info += node_feature[i] + " ";
						// System.out.print(node_feature[i]+" ");
					}
					// System.out.println();
					int neg_occur = 0;
					for (int i3 = 0; i3 < answer[i1][i2].length; i3++) {
						String[] tag_answer = answerRegular(answer[i1][i2][i3]);
						double tag = -1;
						if (tag_answer[0].equals("T")) {
							tag = 1;
						} else {
							neg_occur++;
						}
						if (neg_occur > 1 && tag == -1) {
							continue;
						}
						String re_answer = tag_answer[1];
						double[] answer_feature = fx.getAnswerFeature(questions[i1][i2].word_node, re_answer,
								questions[i1][i2].content, storys[i1].sentense_string);
						String word_info = "";
						for (int i = 0; i < questions[i1][i2].word_node.length; i++) {
							word_info += questions[i1][i2].word_node[i].content + ":"
									+ questions[i1][i2].word_node[i].currentSentense.content + "\r\n";
						}
						String answer_info = "";
						for (int i = 0; i < answer_feature.length; i++) {
							answer_info += answer_feature[i] + " ";
						}
						double a_score = model.getAnswerScore(answer_feature);
						double result = model.getResult(z_score, a_score, tag);
						if (result <= 0) {
							// update++;
							loop++;
							System.out.println("第" + loop + "次模型检测,");
							model.change(edge_feature, node_feature, answer_feature, tag);
							correct_rate = check(model);
							System.out.println("正确率:" + correct_rate);
							String log = "正确率：" + correct_rate;
							String para = getParameter(model);
							writeLog(log + "\r\n" + para);
							// String log = "第"+update+"次更新，第"+i1+"篇文章,问题"+i2;
							// String para = getParameter(model);
							// writeLog(log+"\r\n"+word_info+"\r\n"+"边特征:\r\n"+edge_info+"\r\n节点特征:\r\n"+node_info+"\r\n答案特征:\r\n"+answer_info+"\r\n"+para);
						}
					}
					if(correct_rate > 0.8){
						break outer;
					}
					// System.out.println("question:"+i2);
					// for(int i = 0;i <questions[i1][i2].word_node.length;i++){
					// System.out.println(questions[i1][i2].word_node[i].currentSentense.content+"
					// "+questions[i1][i2].word_node[i].currentSentense.sentense_index);
					// }
				}
			}
			// correct_rate = correct_num / sum;
			// String log = "第" + loop + "次迭代,正确个数为:" + correct_num + "总数为:" +
			// sum + "正确率：" + correct_rate;
			// String para = getParameter(model);
			// writeLog(log + "\r\n" + para);
		}
	}

	public double check(Model model) {
		double sum = 0;
		double correct_num = 0;
		double correct_rate = 0;
		for (int i1 = 0; i1 < storys.length; i1++) {
			for (int i2 = 0; i2 < questions[i1].length; i2++) {
				mp.setInfo(questions[i1][i2].word_node, model, questions[i1][i2].content, storys[i1].sentense_string);
				Object[] object = mp.messagePassing();
				double z_score = (double) object[0];
				// System.out.println();
				int neg_occur = 0;
				for (int i3 = 0; i3 < answer[i1][i2].length; i3++) {
					String[] tag_answer = answerRegular(answer[i1][i2][i3]);
					double tag = -1;
					if (tag_answer[0].equals("T")) {
						tag = 1;
					} else {
						neg_occur++;
					}
					if (neg_occur > 1 && tag == -1) {
						continue;
					}
					String re_answer = tag_answer[1];
					double[] answer_feature = fx.getAnswerFeature(questions[i1][i2].word_node, re_answer,
							questions[i1][i2].content, storys[i1].sentense_string);
					double a_score = model.getAnswerScore(answer_feature);
					double result = model.getResult(z_score, a_score, tag);
					System.out.println("问题:" + i2 + ",答案" + i3 + ",结果为:" + result);
					sum++;
					if (result > 0) {
						correct_num++;
					}
				}
			}
		}
		correct_rate = correct_num / sum;
		System.out.println("正确率:" + correct_rate);
		System.out.println("模型参数:\r\n");
		for (int i = 0; i < model.v1.length; i++) {
			System.out.print(model.v1[i] + " ");
		}
		System.out.println();
		for (int i = 0; i < model.v2.length; i++) {
			System.out.print(model.v2[i] + " ");
		}
		System.out.println();
		for (int i = 0; i < model.v3.length; i++) {
			System.out.print(model.v3[i] + " ");
		}
		System.out.println();
		return correct_rate;
	}

	// public void train() {
	// Word_Node[] word_Nodes;
	// FeatureExtract_DT fx = new FeatureExtract_DT();
	// double sum;
	// double correct_num;
	// double correct_rate = 0;
	// int loop = 0;
	// while (correct_rate < 0.8) {
	// loop++;
	// sum = 0;
	// correct_num = 0;
	// correct_rate = 0;
	// for (int i1 = 0; i1 < storyArg.length; i1++) {
	// String story = storyArg[i1];
	// story = tools.coreference(story);
	// String[] story_sentense = storyDivided(story);
	// for (int i2 = 0; i2 < question[i1].length; i2++) {
	// String re_question = questionRegular(question[i1][i2]);
	// String core_question = tools.coreference(re_question);
	// word_Nodes = tools.parse(core_question);
	// find_supportsentense(word_Nodes, story_sentense);
	// MessagePassing mp = new MessagePassing(word_Nodes, model, core_question,
	// story_sentense);
	// Object[] object = mp.messagePassing();
	// double z_score = (double) object[0];
	// double[] edge_feature = (double[]) object[1];
	// double[] node_feature = (double[]) object[2];
	// boolean neg_occur = false;
	// for (int i3 = 0; i3 < answer[i1][i2].length; i3++) {
	// String[] tag_answer = answerRegular(answer[i1][i2][i3]);
	// double tag = -1;
	// if (tag_answer[0].equals("T")) {
	// tag = 1;
	// }else{
	// neg_occur = true;
	// }
	// if(neg_occur&&tag == -1){
	// continue;
	// }
	// String re_answer = tag_answer[1];
	// double[] answer_feature = fx.getAnswerFeature(word_Nodes, re_answer,
	// core_question,
	// story_sentense);
	// double a_score = model.getAnswerScore(answer_feature);
	// double result = model.getResult(z_score, a_score, tag);
	// sum++;
	// if (result > 0) {
	// correct_num++;
	// } else {
	// model.change(edge_feature, node_feature, answer_feature, tag);
	// }
	// }
	// }
	// }
	// correct_rate = correct_num / sum;
	// String log = "第" + loop + "次迭代,正确个数为:" + correct_num + "总数为:" + sum +
	// "正确率：" + correct_rate;
	// String para = getParameter(model);
	// writeLog(log + "\r\n" + para);
	// }
	// }

	public String getParameter(Model model) {
		String v1 = "";
		String v2 = "";
		String v3 = "";
		double b = model.b;
		for (int i = 0; i < model.v1.length; i++) {
			v1 += model.v1[i] + " ";
		}
		for (int i = 0; i < model.v2.length; i++) {
			v2 += model.v2[i] + " ";
		}
		for (int i = 0; i < model.v3.length; i++) {
			v3 += model.v3[i] + " ";
		}
		String string = v1 + "\r\n" + v2 + "\r\n" + v3 + "\r\n" + b;
		return string;
	}

	public void writeLog(String str) {
		String path = "./src/res/Log/" + "log.txt";
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
