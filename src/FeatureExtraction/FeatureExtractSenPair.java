package FeatureExtraction;

import java.util.ArrayList;
import java.util.HashMap;

import QABasedonDT.Ans;
import QABasedonDT.Element;
import QABasedonDT.Frame;
import QABasedonDT.QABasedonDT;
import QABasedonDT.Question;
import QABasedonDT.Sentence;
import QABasedonDT.Statement;
import QABasedonDT.WordNode;
import Tools.Tools;
import Tools.Word2Vec;

public class FeatureExtractSenPair {
	/***
	 * @author chenruili
	 */
	static Tools tools = new Tools();
	Word2Vec w2v;

	public FeatureExtractSenPair() {
		w2v = new Word2Vec(QABasedonDT.w2vTable);
	}

	/***
	 * 
	 * @param sentense1
	 * @param sentense2
	 * @param statement
	 * @param question
	 * @param answer
	 * @return feature list
	 */
	public HashMap<String, Double> getAllFeature(Sentence sentense1, Sentence sentense2, Statement statement,
			Question question, Ans ans) {
		HashMap<String, Double> featureMap = new HashMap<>();
		String sen_combination = sentense1.content + " " + sentense2.content;
		String queContent = question.getContent();
		String ansContent = ans.getContent();
		double sentense_distance = sentense1.getIndex() - sentense2.getIndex();
		featureMap.put("sentense_distance", sentense_distance);

		double sentense_similiar_feature_lemma = getSimiliarFeatureBasedOnSentense(
				contentPreprocessPassSWLemma(sen_combination), contentPreprocessPassSWLemma(
						contentPreprocessPassSWLemma(ansContent) + " " + contentPreprocessPassSWLemma(queContent)));
		featureMap.put("sentense_similiar_feature_lemma", sentense_similiar_feature_lemma);

		double sentense_similiar_2gram_feature_lemma = getBiwordSimiliarFeatureBasedOnSentense(
				contentPreprocessPassSWLemma(sen_combination),
				contentPreprocessPassSWLemma(ansContent) + " " + contentPreprocessPassSWLemma(queContent));
		featureMap.put("sentense_similiar_2gram_feature_lemma", sentense_similiar_2gram_feature_lemma);

		double sentense_similiar_3gram_feature_lemma = getTriwordSimiliarFeatureBasedOnSentense(
				contentPreprocessPassSWLemma(sen_combination),
				contentPreprocessPassSWLemma(ansContent) + " " + contentPreprocessPassSWLemma(queContent));
		featureMap.put("sentense_similiar_3gram_feature_lemma", sentense_similiar_3gram_feature_lemma);

		double sentense_w2v_similiar_feature_lemma = getW2VSimiliarFeatureBasedOnSentense(
				contentPreprocessPassSWLemma(sen_combination),
				contentPreprocessPassSWLemma(statement.getContent()));
		featureMap.put("sentense_w2v_similiar_feature_lemma", sentense_w2v_similiar_feature_lemma);

		double dependency_u_v_s_feautre = getDependency_U_V_Syntax_Feature(statement, sentense1)
				+ getDependency_U_V_Syntax_Feature(statement, sentense2);
		featureMap.put("dependency_u_v_s_feautre", dependency_u_v_s_feautre);

		double dependency_u_v_feature = getDependency_U_V_Feature(statement, sentense1)
				+ getDependency_U_V_Feature(statement, sentense2);
		featureMap.put("dependency_u_v_feature", dependency_u_v_feature);

		if (ansContent.split(" ").length <= 3) {
			double dependency_svo_u_v_s_feature = getDependency_SVO_U_V_Syntax_Feature(statement.getWordNode(),
					sentense1) + getDependency_SVO_U_V_Syntax_Feature(statement.getWordNode(), sentense2);
			featureMap.put("dependency_svo_u_v_s_feature", dependency_svo_u_v_s_feature);

			double dependency_svo_u_v_feature = getDependency_SVO_U_V_Feature(statement.getWordNode(), sentense1)
					+ getDependency_SVO_U_V_Feature(statement.getWordNode(), sentense2);
			featureMap.put("dependency_svo_u_v_feature", dependency_svo_u_v_feature);

			double dependency_so_u_v_s_feature = getDependency_SO_U_V_Syntax_Feature(statement.getWordNode(),
					sentense1) + getDependency_SO_U_V_Syntax_Feature(statement.getWordNode(), sentense2);
			featureMap.put("dependency_so_u_v_s_feature", dependency_so_u_v_s_feature);

			double dependency_so_u_v_feature = getDependency_SO_U_V_Feature(statement.getWordNode(), sentense1)
					+ getDependency_SO_U_V_Feature(statement.getWordNode(), sentense2);
			featureMap.put("dependency_so_u_v_feature", dependency_so_u_v_feature);
		} else {
			double dependency_svo_u_v_s_feature = getDependency_SVO_U_V_Syntax_Feature(ans.getWordNode(), sentense1)
					+ getDependency_SVO_U_V_Syntax_Feature(ans.getWordNode(), sentense2);
			featureMap.put("dependency_svo_u_v_s_feature", dependency_svo_u_v_s_feature);

			double dependency_svo_u_v_feature = getDependency_SVO_U_V_Feature(ans.getWordNode(), sentense1)
					+ getDependency_SVO_U_V_Feature(ans.getWordNode(), sentense2);
			featureMap.put("dependency_svo_u_v_feature", dependency_svo_u_v_feature);

			double dependency_so_u_v_s_feature = getDependency_SO_U_V_Syntax_Feature(ans.getWordNode(), sentense1)
					+ getDependency_SO_U_V_Syntax_Feature(ans.getWordNode(), sentense2);
			featureMap.put("dependency_so_u_v_s_feature", dependency_so_u_v_s_feature);

			double dependency_so_u_v_feature = getDependency_SO_U_V_Feature(ans.getWordNode(), sentense1)
					+ getDependency_SO_U_V_Feature(ans.getWordNode(), sentense2);
			featureMap.put("dependency_so_u_v_feature", dependency_so_u_v_feature);
		}

		double is_sharing_q_a_word_feature = (getIsSharingQAWordFeature(contentPreprocessPassSWLemma(ansContent),
				contentPreprocessPassSWLemma(queContent), contentPreprocessPassSWLemma(sentense1.getContent()))
				| getIsSharingQAWordFeature(contentPreprocessPassSWLemma(ansContent),
						contentPreprocessPassSWLemma(queContent),
						contentPreprocessPassSWLemma(sentense2.getContent())));
		featureMap.put("is_sharing_q_a_word_feature", is_sharing_q_a_word_feature);

		double frame_target_name_feature = getFrameTargetNameFeature(statement, sentense1)
				+ getFrameTargetNameFeature(statement, sentense2);
		featureMap.put("frame_target_name_feature", frame_target_name_feature);

		double frame_target_text_feature = getFrameTargetTextFeature(statement, sentense1)
				+ getFrameTargetTextFeature(statement, sentense2);
		featureMap.put("frame_target_text_feature", frame_target_text_feature);

		double frame_target_all_feature = getFrameTargetAllFeature(statement, sentense1)
				+ getFrameTargetAllFeature(statement, sentense2);
		featureMap.put("frame_target_all_feature", frame_target_all_feature);

		double frame_element_name_feature = getFrameElementNameFeature(statement, sentense1)
				+ getFrameElementNameFeature(statement, sentense2);
		featureMap.put("frame_element_name_feature", frame_element_name_feature);

		double frame_element_text_feature = getFrameElementTextFeature(statement, sentense1)
				+ getFrameElementTextFeature(statement, sentense2);
		featureMap.put("frame_element_text_feature", frame_element_text_feature);

		double frame_element_all_feature = getFrameElementAllFeature(statement, sentense1)
				+ getFrameElementAllFeature(statement, sentense2);
		featureMap.put("frame_element_all_feature", frame_element_all_feature);

		double frame_element_target_name_feature = getFrameElementTargetNameFeature(statement, sentense1)
				+ getFrameElementTargetNameFeature(statement, sentense2);
		featureMap.put("frame_element_target_name_feature", frame_element_target_name_feature);

		double frame_element_target_text_feature = getFrameElementTargetTextFeature(statement, sentense1)
				+ getFrameElementTargetTextFeature(statement, sentense2);
		featureMap.put("frame_element_target_text_feature", frame_element_target_text_feature);

		double frame_element_target_all_feature = getFrameElementTargetAllFeature(statement, sentense1)
				+ getFrameElementTargetAllFeature(statement, sentense2);
		featureMap.put("frame_element_target_all_feature", frame_element_target_all_feature);

		double is_sharing_VB_q_a_word_feature = get_Is_Sharing_VB_Q_A_Word_Edge_Feature(
				contentPreprocessPassSWLemma(ansContent), question,
				contentPreprocessPassSWLemma(sentense1.getContent()))
				| get_Is_Sharing_VB_Q_A_Word_Edge_Feature(contentPreprocessPassSWLemma(ansContent), question,
						contentPreprocessPassSWLemma(sentense2.getContent()));
		featureMap.put("is_sharing_VB_q_a_word_feature", is_sharing_VB_q_a_word_feature);

		double name_type_distance_feature = getNameTypeDistanceFeature(contentPreprocessPassSWLemma(ansContent),
				contentPreprocessPassSWLemma(queContent), contentPreprocessPassSWLemma(sen_combination));
		featureMap.put("name_type_distance_feature", name_type_distance_feature);

		double name_type_adjacent_feature = getNameTypeAdjacentFeature(contentPreprocessPassSWLemma(ansContent),
				contentPreprocessPassSWLemma(queContent), contentPreprocessPassSWLemma(sen_combination));
		featureMap.put("name_type_adjacent_feature", name_type_adjacent_feature);

		return featureMap;
	}

	/***
	 * 
	 * @param the input text
	 * @return text(replace+toLowerCase+Lemma)
	 */
	public String content_preprocess_lemma(String string) {
		string = string.replace("'", " '");
		string = string.replaceAll("[\\pP‘'“”]", "");
		string = string.replaceAll("\\s{1,}", " ");
		string = string.toLowerCase().trim();
		string = tools.getLemma(string);
		return string;
	}

	/***
	 * 
	 * @param the input text
	 * @return text(replace+toLowerCase+Lemma+removeStopWord)
	 */
	public String contentPreprocessPassSWLemma(String string) {
		string = string.replace("'", " '");
		// string = string.replace("'s", "");
		string = string.replaceAll("[\\pP‘'“”]", "");
		string = string.replaceAll("\\s{1,}", " ");
		string = string.toLowerCase().trim();
		string = tools.getLemma(string);
		string = removeStopWord(string);
		return string;
	}

	/***
	 * 
	 * @param the input text
	 * @return text(removeStopWord)
	 */
	public String removeStopWord(String string) {
		String[] str_arg = string.split(" ");
		String new_string = "";
		for (int i = 0; i < str_arg.length; i++) {
			if (!QABasedonDT.stopWord.contains(str_arg[i])) {
				new_string += str_arg[i] + " ";
			}
		}
		return new_string;
	}

	public double getWordOverlapFeature(String str1, String str2) {
		String[] str1_word = str1.split(" ");
		String[] str2_word = str2.split(" ");
		ArrayList<String> bag1 = new ArrayList<>();
		ArrayList<String> bag2 = new ArrayList<>();
		double overlap = 0;
		for (int i = 0; i < str1_word.length; i++) {
			if (!bag1.contains(str1_word[i])) {
				bag1.add(str1_word[i]);
			}
		}
		for (int i = 0; i < str2_word.length; i++) {
			if (!bag2.contains(str2_word[i])) {
				bag2.add(str2_word[i]);
			}
		}
		for (int i = 0; i < bag1.size(); i++) {
			for (int j = 0; j < bag2.size(); j++) {
				if (bag1.get(i).equals(bag2.get(j))) {
					overlap++;
					break;
				}
			}
		}
		overlap = overlap / (bag1.size() + bag2.size() - overlap);
		return overlap;
	}

	public double getBiWordOverlapFeature(String str1, String str2) {
		String[] str1_word = str1.split(" ");
		String[] str2_word = str2.split(" ");
		ArrayList<String> bag1 = new ArrayList<>();
		ArrayList<String> bag2 = new ArrayList<>();
		double overlap = 0;
		for (int i = 0; i < str1_word.length - 1; i++) {
			String bi_gram = str1_word[i] + " " + str1_word[i + 1];
			if (!bag1.contains(bi_gram)) {
				bag1.add(bi_gram);
			}
		}
		for (int i = 0; i < str2_word.length - 1; i++) {
			String bi_gram = str2_word[i] + " " + str2_word[i + 1];
			if (!bag2.contains(bi_gram)) {
				bag2.add(bi_gram);
			}
		}
		for (int i = 0; i < bag1.size(); i++) {
			for (int j = 0; j < bag2.size(); j++) {
				if (bag1.get(i).equals(bag2.get(j))) {
					overlap++;
					break;
				}
			}
		}
		overlap = overlap / (bag1.size() + bag2.size() - overlap);
		return overlap;
	}

	public double getTriWordOverlapFeature(String str1, String str2) {
		String[] str1_word = str1.split(" ");
		String[] str2_word = str2.split(" ");
		ArrayList<String> bag1 = new ArrayList<>();
		ArrayList<String> bag2 = new ArrayList<>();
		double overlap = 0;
		for (int i = 0; i < str1_word.length - 2; i++) {
			String bi_gram = str1_word[i] + " " + str1_word[i + 1] + " " + str1_word[i + 2];
			if (!bag1.contains(bi_gram)) {
				bag1.add(bi_gram);
			}
		}
		for (int i = 0; i < str2_word.length - 2; i++) {
			String bi_gram = str2_word[i] + " " + str2_word[i + 1] + " " + str2_word[i + 2];
			if (!bag2.contains(bi_gram)) {
				bag2.add(bi_gram);
			}
		}
		for (int i = 0; i < bag1.size(); i++) {
			for (int j = 0; j < bag2.size(); j++) {
				if (bag1.get(i).equals(bag2.get(j))) {
					overlap++;
					break;
				}
			}
		}
		overlap = overlap / (bag1.size() + bag2.size() - overlap);
		return overlap;
	}

	public double getSimiliarFeatureBasedOnSentense(String sentense, String statement) {
		double score = getWordOverlapFeature(statement, sentense);
		return score;
	}

	public double getBiwordSimiliarFeatureBasedOnSentense(String sentense, String statement) {
		double score = getBiWordOverlapFeature(statement, sentense);
		return score;
	}

	public double getTriwordSimiliarFeatureBasedOnSentense(String sentense, String statement) {
		double score = getTriWordOverlapFeature(statement, sentense);
		return score;
	}

	public double getW2VSimiliarFeatureBasedOnSentense(String sentense, String statement) {
		double score = w2v.getSentenseSimiliarity(statement, sentense)[0];
		return score;
	}

	public double getDependency_U_V_Syntax_Feature(Statement statement, Sentence sentense) {
		WordNode[] statement_words = statement.getWordNode();
		WordNode[] sentense_words = sentense.wordNodes;
		double score = 0;
		if (statement_words != null && sentense_words != null) {
			score = getDependency_U_V_S_match(statement_words, sentense_words, true);
		}

		return score;
	}

	public double getDependency_U_V_Feature(Statement statement, Sentence sentense) {
		WordNode[] statement_words = statement.getWordNode();
		WordNode[] sentense_words = sentense.wordNodes;
		double score = 0;
		if (statement_words != null && sentense_words != null) {
			score = getDependency_U_V_S_match(statement_words, sentense_words, false);
		}

		return score;
	}

	public double getDependency_U_V_S_match(WordNode[] statement_words, WordNode[] sentense_words, boolean flag) {
		double score = 0;
		for (int m = 0; m < sentense_words.length; m++) {
			for (int n = 0; n < statement_words.length; n++) {
				String sentense_word = contentPreprocessPassSWLemma(sentense_words[m].content);
				String statement_word = contentPreprocessPassSWLemma(statement_words[n].content);
				if (sentense_word.equals(statement_word)) {
					WordNode sentense_word_p = sentense_words[m].parent;
					WordNode statement_word_p = statement_words[n].parent;
					if (sentense_word_p != null && statement_word_p != null
							&& (content_preprocess_lemma(sentense_word_p.content)
									.equals(content_preprocess_lemma(statement_word_p.content)))) {
						if (!flag) {
							score++;
						} else if (flag && sentense_words[m].synInfo.equals(statement_words[n].synInfo)) {
							score++;
						}
					}
				}
			}
		}
		return score / (double) (sentense_words.length + statement_words.length);
	}

	public double getDependency_SVO_U_V_Syntax_Feature(WordNode[] wordNodes, Sentence sentense) {
		WordNode[] sentense_words = sentense.wordNodes;
		double score = 0;
		if (wordNodes != null && sentense_words != null) {
			score = getDependency_SVO_match(wordNodes, sentense_words, true);
		}

		return score;
	}

	public double getDependency_SVO_U_V_Feature(WordNode[] wordNodes, Sentence sentense) {
		WordNode[] sentense_words = sentense.wordNodes;
		double score = 0;
		if (wordNodes != null && sentense_words != null) {
			score = getDependency_SVO_match(wordNodes, sentense_words, false);
		}

		return score;
	}

	public double getDependency_SO_U_V_Syntax_Feature(WordNode[] wordNodes, Sentence sentense) {
		WordNode[] sentense_words = sentense.wordNodes;
		double score = 0;
		if (wordNodes != null && sentense_words != null) {
			score = getDependency_SO_match(wordNodes, sentense_words, true);
		}

		return score;
	}

	public double getDependency_SO_U_V_Feature(WordNode[] wordNodes, Sentence sentense) {
		WordNode[] sentense_words = sentense.wordNodes;
		double score = 0;
		if (wordNodes != null && sentense_words != null) {
			score = getDependency_SO_match(wordNodes, sentense_words, false);
		}

		return score;
	}

	public double getDependency_SVO_match(WordNode[] statement_words, WordNode[] sentense_words, boolean flag) {
		double score = 0;
		for (int m = 0; m < sentense_words.length; m++) {
			for (int n = 0; n < statement_words.length; n++) {
				String sentense_word = contentPreprocessPassSWLemma(sentense_words[m].content);
				String statement_word = contentPreprocessPassSWLemma(statement_words[n].content);
				if (sentense_word.equals(statement_word)) {
					WordNode sentense_word_p = sentense_words[m].parent;
					WordNode statement_word_p = statement_words[n].parent;
					if (sentense_word_p != null && statement_word_p != null
							&& (content_preprocess_lemma(sentense_word_p.content)
									.equals(content_preprocess_lemma(statement_word_p.content)))) {
						ArrayList<WordNode> sentense_bro_list = sentense_word_p.childlist;
						ArrayList<WordNode> statement_bro_list = statement_word_p.childlist;
						for (int i = 0; i < sentense_bro_list.size(); i++) {
							if (sentense_bro_list.get(i) == sentense_words[m]) {
								continue;
							}
							for (int j = 0; j < statement_bro_list.size(); j++) {
								if (statement_bro_list.get(j) == statement_words[n]) {
									continue;
								}
								String sen_bro_string = content_preprocess_lemma(sentense_bro_list.get(i).getContent());
								String state_bro_string = content_preprocess_lemma(
										statement_bro_list.get(j).getContent());
								if (sen_bro_string.equals(state_bro_string)) {
									if (!flag) {
										score++;
									} else if (flag && sentense_words[m].synInfo.equals(statement_words[n].synInfo)
											&& sentense_bro_list.get(i).synInfo
													.equals(statement_bro_list.get(j).synInfo)) {
										score++;
									}
								}
							}
						}
					}
				}
			}
		}
		return score / (double) (sentense_words.length + statement_words.length);
	}

	public double getDependency_SO_match(WordNode[] statement_words, WordNode[] sentense_words, boolean flag) {
		double score = 0;
		for (int m = 0; m < sentense_words.length; m++) {
			for (int n = 0; n < statement_words.length; n++) {
				String sentense_word = contentPreprocessPassSWLemma(sentense_words[m].content);
				String statement_word = contentPreprocessPassSWLemma(statement_words[n].content);
				if (sentense_word.equals(statement_word)) {
					WordNode sentense_word_p = sentense_words[m].parent;
					WordNode statement_word_p = statement_words[n].parent;
					if (sentense_word_p != null && statement_word_p != null) {
						ArrayList<WordNode> sentense_bro_list = sentense_word_p.childlist;
						ArrayList<WordNode> statement_bro_list = statement_word_p.childlist;
						for (int i = 0; i < sentense_bro_list.size(); i++) {
							if (sentense_bro_list.get(i) == sentense_words[m]) {
								continue;
							}
							for (int j = 0; j < statement_bro_list.size(); j++) {
								if (statement_bro_list.get(j) == statement_words[n]) {
									continue;
								}
								String sen_bro_string = content_preprocess_lemma(sentense_bro_list.get(i).getContent());
								String state_bro_string = content_preprocess_lemma(
										statement_bro_list.get(j).getContent());
								if (sen_bro_string.equals(state_bro_string)) {
									if (!flag) {
										score++;
									} else if (flag && sentense_words[m].synInfo.equals(statement_words[n].synInfo)
											&& sentense_bro_list.get(i).synInfo
													.equals(statement_bro_list.get(j).synInfo)) {
										score++;
									}
								}
							}
						}
					}
				}
			}
		}
		return score / (double) (sentense_words.length + statement_words.length);
	}

	public int getIsSharingQAWordFeature(String answer, String question, String sentense) {
		int score = 0;
		String[] answer_word = answer.split(" ");
		String[] question_word = question.split(" ");
		String[] sentense_word = sentense.split(" ");
		boolean flag1 = false;
		boolean flag2 = false;
		for (int j1 = 0; j1 < sentense_word.length; j1++) {
			for (int j2 = 0; j2 < answer_word.length; j2++) {
				if (sentense_word[j1].equals(answer_word[j2])) {
					flag1 = true;
					break;
				}
			}
			if (flag1) {
				break;
			}
		}

		for (int j1 = 0; j1 < sentense_word.length; j1++) {
			for (int j2 = 0; j2 < question_word.length; j2++) {
				if (sentense_word[j1].equals(question_word[j2])) {
					flag2 = true;
					break;
				}
			}
			if (flag2) {
				break;
			}
		}
		if (flag1 && flag2) {
			score = 1;
		}

		return score;
	}

	public double getFrameTargetNameFeature(Statement statement, Sentence sentense) {
		double score = getTargetNameMatchNum(statement.frameList, sentense.frameList);
		return score / 5.0;
	}

	public double getFrameTargetTextFeature(Statement statement, Sentence sentense) {
		double score = getTargetTextMatchNum(statement.frameList, sentense.frameList);
		return score / 5.0;
	}

	public double getFrameTargetAllFeature(Statement statement, Sentence sentense) {
		double score = getTargetAllMatchNum(statement.frameList, sentense.frameList);
		return score / 5.0;
	}

	public double getTargetNameMatchNum(ArrayList<Frame> qa_frame_list, ArrayList<Frame> s_frame_list) {
		double score = 0;
		if (qa_frame_list != null && s_frame_list != null) {
			for (int i = 0; i < qa_frame_list.size(); i++) {
				String qa_target_name = qa_frame_list.get(i).getTarget().getName();
				for (int j = 0; j < s_frame_list.size(); j++) {
					String s_target_name = s_frame_list.get(j).getTarget().getName();
					if (qa_target_name.equals(s_target_name)) {
						score++;
					}
				}
			}
		}
		return score;
	}

	public double getTargetTextMatchNum(ArrayList<Frame> qa_frame_list, ArrayList<Frame> s_frame_list) {
		double score = 0;
		if (qa_frame_list != null && s_frame_list != null) {
			for (int i = 0; i < qa_frame_list.size(); i++) {
				String qa_target_text = content_preprocess_lemma(qa_frame_list.get(i).getTarget().getText());
				for (int j = 0; j < s_frame_list.size(); j++) {
					String s_target_text = content_preprocess_lemma(s_frame_list.get(j).getTarget().getText());
					if (qa_target_text.equals(s_target_text)) {
						score++;
					}
				}
			}
		}
		return score;
	}

	public double getTargetAllMatchNum(ArrayList<Frame> qa_frame_list, ArrayList<Frame> s_frame_list) {
		double score = 0;
		if (qa_frame_list != null && s_frame_list != null) {
			for (int i = 0; i < qa_frame_list.size(); i++) {
				String qa_target_name = qa_frame_list.get(i).getTarget().getName();
				String qa_target_text = content_preprocess_lemma(qa_frame_list.get(i).getTarget().getText());
				for (int j = 0; j < s_frame_list.size(); j++) {
					String s_target_name = s_frame_list.get(j).getTarget().getName();
					String s_target_text = content_preprocess_lemma(s_frame_list.get(j).getTarget().getText());
					if (qa_target_text.equals(s_target_text) && qa_target_name.equals(s_target_name)) {
						score++;
					}
				}
			}
		}
		return score;
	}

	public double getFrameElementNameFeature(Statement statement, Sentence sentense) {
		double score = getElementNameNum(statement.frameList, sentense.frameList);
		return score / 5.0;
	}

	public double getFrameElementTextFeature(Statement statement, Sentence sentense) {
		double score = getElementTextNum(statement.frameList, sentense.frameList);
		return score / 5.0;
	}

	public double getFrameElementAllFeature(Statement statement, Sentence sentense) {
		double score = getElementAllNum(statement.frameList, sentense.frameList);
		return score / 5.0;
	}

	public double getElementNameNum(ArrayList<Frame> qa_frameList, ArrayList<Frame> s_frameList) {
		double score = 0;
		if (qa_frameList != null && s_frameList != null) {
			for (int i = 0; i < qa_frameList.size(); i++) {
				ArrayList<Element> qa_element_list = qa_frameList.get(i).getElementList();
				for (int j = 0; j < s_frameList.size(); j++) {
					ArrayList<Element> s_element_list = s_frameList.get(j).getElementList();
					for (int m = 0; m < qa_element_list.size(); m++) {
						String qa_element_name = qa_element_list.get(m).getName();
						for (int n = 0; n < s_element_list.size(); n++) {
							String s_element_name = s_element_list.get(n).getName();
							if (qa_element_name.equals(s_element_name)) {
								score++;
							}
						}
					}
				}
			}
		}
		return score;
	}

	public double getElementTextNum(ArrayList<Frame> qa_frameList, ArrayList<Frame> s_frameList) {
		double score = 0;
		if (qa_frameList != null && s_frameList != null) {
			for (int i = 0; i < qa_frameList.size(); i++) {
				ArrayList<Element> qa_element_list = qa_frameList.get(i).getElementList();
				for (int j = 0; j < s_frameList.size(); j++) {
					ArrayList<Element> s_element_list = s_frameList.get(j).getElementList();
					for (int m = 0; m < qa_element_list.size(); m++) {
						String qa_element_text = content_preprocess_lemma(qa_element_list.get(m).getText());
						for (int n = 0; n < s_element_list.size(); n++) {
							String s_element_text = content_preprocess_lemma(s_element_list.get(n).getText());
							if (qa_element_text.equals(s_element_text)) {
								score++;
							}
						}
					}
				}
			}
		}
		return score;
	}

	public double getElementAllNum(ArrayList<Frame> qa_frameList, ArrayList<Frame> s_frameList) {
		double score = 0;
		if (qa_frameList != null && s_frameList != null) {
			for (int i = 0; i < qa_frameList.size(); i++) {
				ArrayList<Element> qa_element_list = qa_frameList.get(i).getElementList();
				for (int j = 0; j < s_frameList.size(); j++) {
					ArrayList<Element> s_element_list = s_frameList.get(j).getElementList();
					for (int m = 0; m < qa_element_list.size(); m++) {
						String qa_element_text = content_preprocess_lemma(qa_element_list.get(m).getText());
						String qa_element_name = qa_element_list.get(m).getName();
						for (int n = 0; n < s_element_list.size(); n++) {
							String s_element_text = content_preprocess_lemma(s_element_list.get(n).getText());
							String s_element_name = s_element_list.get(n).getName();
							if (qa_element_text.equals(s_element_text) && qa_element_name.equals(s_element_name)) {
								score++;
							}
						}
					}
				}
			}
		}
		return score;
	}

	public double getFrameElementTargetNameFeature(Statement statement, Sentence sentense) {
		double score = getElementTargetNameMatchNum(statement.frameList, sentense.frameList);
		return score / 5.0;
	}

	public double getFrameElementTargetTextFeature(Statement statement, Sentence sentense) {
		double score = getElementTargetTextMatchNum(statement.frameList, sentense.frameList);
		return score / 5.0;
	}

	public double getFrameElementTargetAllFeature(Statement statement, Sentence sentense) {
		double score = getElementTargetAllMatchNum(statement.frameList, sentense.frameList);
		return score / 5.0;
	}

	public double getElementTargetNameMatchNum(ArrayList<Frame> qa_frame_list, ArrayList<Frame> s_frame_list) {
		double score = 0;
		if (qa_frame_list != null && s_frame_list != null) {
			for (int i = 0; i < qa_frame_list.size(); i++) {
				String qa_target_name = qa_frame_list.get(i).getTarget().getName();
				ArrayList<Element> qa_element_list = qa_frame_list.get(i).getElementList();
				for (int j = 0; j < s_frame_list.size(); j++) {
					String s_target_name = s_frame_list.get(j).getTarget().getName();
					ArrayList<Element> s_element_list = s_frame_list.get(j).getElementList();
					if (qa_target_name.equals(s_target_name)) {
						for (int m = 0; m < qa_element_list.size(); m++) {
							String qa_element_name = qa_element_list.get(m).getName();
							for (int n = 0; n < s_element_list.size(); n++) {
								String s_element_name = s_element_list.get(n).getName();
								if (qa_element_name.equals(s_element_name)) {
									score++;
								}
							}
						}
					}
				}
			}
		}
		return score;
	}

	public double getElementTargetTextMatchNum(ArrayList<Frame> qa_frame_list, ArrayList<Frame> s_frame_list) {
		double score = 0;
		if (qa_frame_list != null && s_frame_list != null) {
			for (int i = 0; i < qa_frame_list.size(); i++) {
				String qa_target_name = qa_frame_list.get(i).getTarget().getName();
				ArrayList<Element> qa_element_list = qa_frame_list.get(i).getElementList();
				for (int j = 0; j < s_frame_list.size(); j++) {
					String s_target_name = s_frame_list.get(j).getTarget().getName();
					ArrayList<Element> s_element_list = s_frame_list.get(j).getElementList();
					if (qa_target_name.equals(s_target_name)) {
						for (int m = 0; m < qa_element_list.size(); m++) {
							String qa_element_text = qa_element_list.get(m).getText();
							for (int n = 0; n < s_element_list.size(); n++) {
								String s_element_text = s_element_list.get(n).getText();
								if (qa_element_text.equals(s_element_text)) {
									score++;
								}
							}
						}
					}
				}
			}
		}
		return score;
	}

	public double getElementTargetAllMatchNum(ArrayList<Frame> qa_frame_list, ArrayList<Frame> s_frame_list) {
		double score = 0;
		if (qa_frame_list != null && s_frame_list != null) {
			for (int i = 0; i < qa_frame_list.size(); i++) {
				String qa_target_name = qa_frame_list.get(i).getTarget().getName();
				ArrayList<Element> qa_element_list = qa_frame_list.get(i).getElementList();
				for (int j = 0; j < s_frame_list.size(); j++) {
					String s_target_name = s_frame_list.get(j).getTarget().getName();
					ArrayList<Element> s_element_list = s_frame_list.get(j).getElementList();
					if (qa_target_name.equals(s_target_name)) {
						for (int m = 0; m < qa_element_list.size(); m++) {
							String qa_element_text = content_preprocess_lemma(qa_element_list.get(m).getText());
							String qa_element_name = qa_element_list.get(m).getName();
							for (int n = 0; n < s_element_list.size(); n++) {
								String s_element_text = content_preprocess_lemma(s_element_list.get(n).getText());
								String s_element_name = s_element_list.get(n).getName();
								if (qa_element_text.equals(s_element_text) && qa_element_name.equals(s_element_name)) {
									score++;
								}
							}
						}
					}
				}
			}
		}
		return score;
	}

	public int get_Is_Sharing_VB_Q_A_Word_Edge_Feature(String answer, Question que, String sentense) {
		int score = 0;
		String[] answer_word = answer.split(" ");
		String[] sentense_word = sentense.split(" ");
		boolean flag1 = false;
		boolean flag2 = false;
		for (int j1 = 0; j1 < sentense_word.length; j1++) {
			for (int j2 = 0; j2 < answer_word.length; j2++) {
				if (sentense_word[j1].equals(answer_word[j2])) {
					flag1 = true;
					break;
				}
			}
			if (flag1) {
				break;
			}
		}

		for (int j1 = 0; j1 < sentense_word.length; j1++) {
			for (int j2 = 0; j2 < que.getWordNode().length; j2++) {
				String postag = que.getWordNode()[j2].getPostag();
				String q_w = content_preprocess_lemma(que.getWordNode()[j2].content);
				if (sentense_word[j1].equals(q_w) && postag.equals("VERB")) {
					flag2 = true;
					break;
				}
			}
			if (flag2) {
				break;
			}
		}
		if (flag1 && flag2) {
			score = 1;
		}

		return score;
	}

	public double getNameTypeDistanceFeature(String answer, String question, String sentense) {
		String[] q_w = question.split(" ");
		double score = 0;
		boolean tag = false;
		String who = "";
		if (q_w[q_w.length - 1].equals("name") && q_w.length - 2 >= 0) {
			who = q_w[q_w.length - 2];
			tag = true;
		} else if (question.contains("name of")) {
			int index = question.indexOf("name of");
			who = question.substring(index + 8).trim();
			String[] name_list = who.split(" ");
			who = name_list[name_list.length - 1];
			tag = true;
		}
		if (tag) {
			String[] s_w = sentense.split(" ");
			String[] a_w = answer.split(" ");
			ArrayList<Integer> answer_index = new ArrayList<>();
			ArrayList<Integer> who_index = new ArrayList<>();
			for (int i = 0; i < s_w.length; i++) {
				if (s_w[i].equals(who)) {
					who_index.add(i);
				}
				for (int j = 0; j < a_w.length; j++) {
					if (s_w[i].equals(a_w[j])) {
						answer_index.add(i);
						break;
					}
				}
			}
			double min = 1000;
			boolean flag = false;
			for (int i = 0; i < who_index.size(); i++) {
				if (flag) {
					break;
				}
				for (int j = 0; j < answer_index.size(); j++) {
					double local_min = Math.abs(who_index.get(i) - answer_index.get(j));
					if (local_min == 0) {
						flag = true;
						break;
					}
					if (local_min < min) {
						min = local_min;
					}
				}
			}
			if (min == 0) {
				score = 0;
			} else {
				score = 1 / min;
			}
		}
		return score;
	}

	public double getNameTypeAdjacentFeature(String answer, String question, String sentense) {
		String[] q_w = question.split(" ");
		double score = 0;
		boolean tag = false;
		String who = "";
		if (q_w[q_w.length - 1].equals("name") && q_w.length - 2 > 0) {
			who = q_w[q_w.length - 2];
			tag = true;
		} else if (question.contains("name of")) {
			int index = question.indexOf("name of");
			who = question.substring(index + 8).trim();
			String[] name_list = who.split(" ");
			who = name_list[name_list.length - 1];
			tag = true;
		}

		if (tag) {
			String[] s_w = sentense.split(" ");
			String[] a_w = answer.split(" ");
			ArrayList<String> adjacent_word = new ArrayList<>();
			for (int i = 0; i < s_w.length; i++) {
				if (s_w[i].equals(who)) {
					for (int i2 = i + 1; i2 < i + 4 && i2 < s_w.length; i2++) {
						adjacent_word.add(s_w[i2]);
					}
				}
			}
			boolean flag = false;
			for (int i = 0; i < adjacent_word.size(); i++) {
				if (flag) {
					break;
				}
				for (int j = 0; j < a_w.length; j++) {
					if (adjacent_word.get(i).equals(a_w[j])) {
						score = 1;
						flag = true;
						break;
					}
				}
			}
		}
		return score;
	}

}
