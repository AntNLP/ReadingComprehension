package FeatureExtraction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import QABasedonDT.Ans;
import QABasedonDT.Element;
import QABasedonDT.Frame;
import QABasedonDT.QABasedonDT;
import QABasedonDT.Question;
import QABasedonDT.Sentence;
import QABasedonDT.Statement;
import QABasedonDT.Story;
import QABasedonDT.WordNode;

import Tools.Tools;
import Tools.Word2Vec;

public class FeatureExtractGlobal {
	/***
	 *@author chenruili 
	 */
	static Tools tools = new Tools();
	Word2Vec w2v;

	public FeatureExtractGlobal() {
		w2v = new Word2Vec(QABasedonDT.w2vTable);
	}

	/***
	 * 
	 * @param answer
	 * @param question
	 * @param story
	 * @param statement
	 * @return global feature list
	 */
	public HashMap<String, Double> getAllFeature(Ans ans, Question question, Story story, Statement statement) {
		HashMap<String, Double> featureMap = new HashMap<>();
		String queContent = question.getContent();
		String ansContent = ans.getContent();

		double words_bag_sim_feature = getWordsBagSimiliarFeature(contentPreprocessPassSW(ansContent),
				contentPreprocessPassSW(queContent), contentPreprocessPassSW(story.content));
		featureMap.put("words_bag_sim_feature", words_bag_sim_feature);

		double long_words_bag_sim_feature = getDoubleWordsBagSimiliarFeature(contentPreprocessPassSW(ansContent),
				contentPreprocessPassSW(queContent), contentPreprocessPassSW(story.content));
		featureMap.put("long_words_bag_sim_feature", long_words_bag_sim_feature);

		double ave_words_bag_sim_feature = getAverageWordsBagSimiliarFeature(contentPreprocessPassSW(ansContent),
				contentPreprocessPassSW(queContent), contentPreprocessPassSW(story.content));
		featureMap.put("ave_words_bag_sim_feature", ave_words_bag_sim_feature);

		double words_bag_add_w2v_sim_feature = getWordsBagAddWord2VecSimiliarFeature(
				contentPreprocessPassSW(ansContent), contentPreprocessPassSW(queContent),
				contentPreprocessPassSW(story.content));
		featureMap.put("words_bag_add_w2v_sim_feature", words_bag_add_w2v_sim_feature);

		double words_bag_mul_w2v_sim_feature = getWordsBagMulWord2VecSimiliarFeature(
				contentPreprocessPassSW(ansContent), contentPreprocessPassSW(queContent),
				contentPreprocessPassSW(story.content));
		featureMap.put("words_bag_mul_w2v_sim_feature", words_bag_mul_w2v_sim_feature);

		double words_bag_sum_w2v_sim_feature = getWordsBagSumWord2VecSimiliarFeature(
				contentPreprocessPassSW(ansContent), contentPreprocessPassSW(queContent),
				contentPreprocessPassSW(story.content));
		featureMap.put("words_bag_sum_w2v_sim_feature", words_bag_sum_w2v_sim_feature);

		double words_distance_feature = getDistanceBasedFeature(contentPreprocessPassSW(queContent),
				contentPreprocessPassSW(ansContent), contentPreprocessPassSW(story.content));
		featureMap.put("words_distance_feature", words_distance_feature);

		double sentense_similiar_feature_lemma = getSimiliarFeatureBasedOnSentense(
				contentPreprocessPassSWLemma(ansContent), contentPreprocessPassSWLemma(queContent),
				story.sentense_string);
		featureMap.put("sentense_similiar_feature_lemma", sentense_similiar_feature_lemma);

		double sentense_similiar_2gram_feature_lemma = getBiwordSimiliarFeatureBasedOnSentense(
				contentPreprocessPassSWLemma(statement.getContent()), contentPreprocessPassSWLemma(ansContent),
				story.sentense_string);
		featureMap.put("sentense_similiar_2gram_feature_lemma", sentense_similiar_2gram_feature_lemma);

		double sentense_similiar_3gram_feature_lemma = getTriwordSimiliarFeatureBasedOnSentense(
				contentPreprocessPassSWLemma(statement.getContent()), contentPreprocessPassSWLemma(ansContent),
				story.sentense_string);
		featureMap.put("sentense_similiar_3gram_feature_lemma", sentense_similiar_3gram_feature_lemma);

		double bi_sentense_similiar_feature_lemma = getSimiliarFeatureBasedOn2Sentense(
				contentPreprocessPassSWLemma(ansContent), contentPreprocessPassSWLemma(queContent),
				story.sentense_string);
		featureMap.put("bi_sentense_similiar_feature_lemma", bi_sentense_similiar_feature_lemma);

		double bi_sentense_similiar_2gram_feature_lemma = getBiwordSimiliarFeatureBasedOn2Sentense(
				contentPreprocessPassSWLemma(statement.getContent()), contentPreprocessPassSWLemma(ansContent),
				story.sentense_string);
		featureMap.put("bi_sentense_similiar_2gram_feature_lemma", bi_sentense_similiar_2gram_feature_lemma);

		double bi_sentense_similiar_3gram_feature_lemma = getTriwordSimiliarFeatureBasedOn2Sentense(
				contentPreprocessPassSWLemma(statement.getContent()), contentPreprocessPassSWLemma(ansContent),
				story.sentense_string);
		featureMap.put("bi_sentense_similiar_3gram_feature_lemma", bi_sentense_similiar_3gram_feature_lemma);

		double all_bi_sentense_similiar_feature_lemma = getSimiliarFeatureBasedOnAll2Sentense(
				contentPreprocessPassSWLemma(statement.getContent()), contentPreprocessPassSWLemma(ansContent),
				story.sentense_string);
		featureMap.put("all_bi_sentense_similiar_feature_lemma", all_bi_sentense_similiar_feature_lemma);

		double all_bi_sentense_similiar_2gram_feature_lemma = getBiWordSimiliarFeatureBasedOnAll2Sentense(
				contentPreprocessPassSWLemma(statement.getContent()), contentPreprocessPassSWLemma(ansContent),
				story.sentense_string);
		featureMap.put("all_bi_sentense_similiar_2gram_feature_lemma", all_bi_sentense_similiar_2gram_feature_lemma);

		double all_bi_sentense_similiar_3gram_feature_lemma = getTriWordSimiliarFeatureBasedOnAll2Sentense(
				contentPreprocessPassSWLemma(statement.getContent()), contentPreprocessPassSWLemma(ansContent),
				story.sentense_string);
		featureMap.put("all_bi_sentense_similiar_3gram_feature_lemma", all_bi_sentense_similiar_3gram_feature_lemma);

		double sentense_w2v_similiar_feature_lemma = getW2VSimiliarFeatureBasedOnSentense(
				contentPreprocessPassSWLemma(ansContent), contentPreprocessPassSWLemma(queContent),
				story.sentense_string);
		featureMap.put("sentense_w2v_similiar_feature_lemma", sentense_w2v_similiar_feature_lemma);

		double bi_sentense_w2v_similiar_lemma = getW2VSimiliarFeatureBasedOn2Sentense(
				contentPreprocessPassSWLemma(statement.getContent()), story.sentense_string);
		featureMap.put("bi_sentense_w2v_similiar_lemma", bi_sentense_w2v_similiar_lemma);

		double all_bi_sentense_w2v_similiar_lemma = getW2VSimiliarFeatureBasedOnAll2Sentense(
				contentPreprocessPassSWLemma(statement.getContent()), story.sentense_string);
		featureMap.put("all_bi_sentense_w2v_similiar_lemma", all_bi_sentense_w2v_similiar_lemma);

		double words_bag_sim_feature_lemma = getWordsBagSimiliarFeature(contentPreprocessPassSWLemma(ansContent),
				contentPreprocessPassSWLemma(queContent), contentPreprocessPassSWLemma(story.content));
		featureMap.put("words_bag_sim_feature_lemma", words_bag_sim_feature_lemma);

		double long_words_bag_sim_feature_lemma = getDoubleWordsBagSimiliarFeature(
				contentPreprocessPassSWLemma(ansContent), contentPreprocessPassSWLemma(queContent),
				contentPreprocessPassSWLemma(story.content));
		featureMap.put("long_words_bag_sim_feature_lemma", long_words_bag_sim_feature_lemma);

		double ave_words_bag_sim_feature_lemma = getAverageWordsBagSimiliarFeature(
				contentPreprocessPassSWLemma(ansContent), contentPreprocessPassSWLemma(queContent),
				contentPreprocessPassSWLemma(story.content));
		featureMap.put("ave_words_bag_sim_feature_lemma", ave_words_bag_sim_feature_lemma);

		double words_bag_add_w2v_sim_feature_lemma = getWordsBagAddWord2VecSimiliarFeature(
				contentPreprocessPassSWLemma(ansContent), contentPreprocessPassSWLemma(queContent),
				contentPreprocessPassSWLemma(story.content));
		featureMap.put("words_bag_add_w2v_sim_feature_lemma", words_bag_add_w2v_sim_feature_lemma);

		double words_bag_mul_w2v_sim_feature_lemma = getWordsBagMulWord2VecSimiliarFeature(
				contentPreprocessPassSWLemma(ansContent), contentPreprocessPassSWLemma(queContent),
				contentPreprocessPassSWLemma(story.content));
		featureMap.put("words_bag_mul_w2v_sim_feature_lemma", words_bag_mul_w2v_sim_feature_lemma);

		double words_bag_sum_w2v_sim_feature_lemma = getWordsBagSumWord2VecSimiliarFeature(
				contentPreprocessPassSWLemma(ansContent), contentPreprocessPassSWLemma(queContent),
				contentPreprocessPassSWLemma(story.content));
		featureMap.put("words_bag_sum_w2v_sim_feature_lemma", words_bag_sum_w2v_sim_feature_lemma);

		double words_distance_feature_lemma = getDistanceBasedFeature(contentPreprocessPassSWLemma(queContent),
				contentPreprocessPassSWLemma(ansContent), contentPreprocessPassSWLemma(story.content));
		featureMap.put("words_distance_feature_lemma", words_distance_feature_lemma);

		double support_answer_feature = getSupportAnswerFeature(contentPreprocessPassSWLemma(ansContent),
				contentPreprocessPassSWLemma(queContent), story.sentense_string);
		featureMap.put("support_answer_feature", support_answer_feature);

		double support_question_answer_distance_feature = getSupport_Q_A_DistanceFeature(
				contentPreprocessPassSWLemma(ansContent), contentPreprocessPassSWLemma(queContent),
				story.sentense_string);
		featureMap.put("support_question_answer_distance_feature", support_question_answer_distance_feature);

		double question_support_root_comparison_feature = getRootComparisonFeature(ansContent, question, story);
		featureMap.put("question_support_root_comparison_feature", question_support_root_comparison_feature);

		double dependency_u_v_s_w1_feautre = getDependency_U_V_Syntax_Feature(statement, story, 1);
		featureMap.put("dependency_u_v_s_w1_feautre", dependency_u_v_s_w1_feautre);

		double dependency_u_v_w1_feature = getDependency_U_V_Feature(statement, story, 1);
		featureMap.put("dependency_u_v_w1_feature", dependency_u_v_w1_feature);

		double dependency_u_v_s_w2_feature = getDependency_U_V_Syntax_Feature(statement, story, 2);
		featureMap.put("dependency_u_v_s_w2_feature", dependency_u_v_s_w2_feature);

		double dependency_u_v_w2_feature = getDependency_U_V_Feature(statement, story, 2);
		featureMap.put("dependency_u_v_w2_feature", dependency_u_v_w2_feature);

		double dependency_svo_u_v_s_feature = getDependency_SVO_U_V_Syntax_Feature(statement.getWordNode(), story, 1);
		featureMap.put("dependency_svo_u_v_s_feature", dependency_svo_u_v_s_feature);

		double dependency_svo_u_v_feature = getDependency_SVO_U_V_Feature(statement.getWordNode(), story, 1);
		featureMap.put("dependency_svo_u_v_feature", dependency_svo_u_v_feature);

		double dependency_so_u_v_s_feature = getDependency_SO_U_V_Syntax_Feature(statement.getWordNode(), story, 1);
		featureMap.put("dependency_so_u_v_s_feature", dependency_so_u_v_s_feature);

		double dependency_so_u_v_feature = getDependency_SO_U_V_Feature(statement.getWordNode(), story, 1);
		featureMap.put("dependency_so_u_v_feature", dependency_so_u_v_feature);

		Object[] sharing_q_a_word_feature = getIsSharingQAWordFeature(contentPreprocessPassSWLemma(ansContent),
				contentPreprocessPassSWLemma(queContent), story.sentense_string);
		double is_sharing_q_a_word_feature = (double) sharing_q_a_word_feature[0];
		featureMap.put("is_sharing_q_a_word_feature", is_sharing_q_a_word_feature);

		double inner_sentense_distance_feature = -1;
		if (is_sharing_q_a_word_feature == 1) {
			inner_sentense_distance_feature = getInnerSentenseDistanceFeature(
					(ArrayList<String>) sharing_q_a_word_feature[1], contentPreprocessPassSWLemma(ansContent),
					contentPreprocessPassSWLemma(queContent));
		}
		featureMap.put("inner_sentense_distance_feature", inner_sentense_distance_feature);

		double sentense_distance_feature = getOuterSentenseDistanceFeature(contentPreprocessPassSWLemma(ansContent),
				contentPreprocessPassSWLemma(queContent), story.sentense_string);
		featureMap.put("sentense_distance_feature", sentense_distance_feature);

		double sequential_feature = getSequentialFeature(content_preprocess_lemma(queContent),
				content_preprocess_lemma(ansContent), story.sentense_string);
		featureMap.put("sequential_feature", sequential_feature);

		double frame_target_name_feature = getFrameTargetNameFeature(statement, story);
		featureMap.put("frame_target_name_feature", frame_target_name_feature);

		double frame_target_text_feature = getFrameTargetTextFeature(statement, story);
		featureMap.put("frame_target_text_feature", frame_target_text_feature);

		double frame_target_all_feature = getFrameTargetAllFeature(statement, story);
		featureMap.put("frame_target_all_feature", frame_target_all_feature);

		double frame_element_name_feature = getFrameElementNameFeature(statement, story);
		featureMap.put("frame_element_name_feature", frame_element_name_feature);

		double frame_element_text_feature = getFrameElementTextFeature(statement, story);
		featureMap.put("frame_element_text_feature", frame_element_text_feature);

		double frame_element_all_feature = getFrameElementAllFeature(statement, story);
		featureMap.put("frame_element_all_feature", frame_element_all_feature);

		double frame_element_target_name_feature = getFrameElementTargetNameFeature(statement, story);
		featureMap.put("frame_element_target_name_feature", frame_element_target_name_feature);

		double frame_element_target_text_feature = getFrameElementTargetTextFeature(statement, story);
		featureMap.put("frame_element_target_text_feature", frame_element_target_text_feature);

		double frame_element_target_all_feature = getFrameElementTargetAllFeature(statement, story);
		featureMap.put("frame_element_target_all_feature", frame_element_target_all_feature);

		double is_sharing_VB_q_a_word_feature = get_Is_Sharing_VB_Q_A_Word_Feature(question, ans,
				story.sentense_string);
		featureMap.put("is_sharing_VB_q_a_word_feature", is_sharing_VB_q_a_word_feature);

		double name_type_distance_feature = getNameTypeDistanceFeature(contentPreprocessPassSWLemma(ansContent),
				contentPreprocessPassSWLemma(queContent), contentPreprocessPassSWLemma(story.content));
		featureMap.put("name_type_distance_feature", name_type_distance_feature);

		double name_type_adjacent_feature = getNameTypeAdjacentFeature(contentPreprocessPassSWLemma(ansContent),
				contentPreprocessPassSWLemma(queContent), contentPreprocessPassSWLemma(story.content));
		featureMap.put("name_type_adjacent_feature", name_type_adjacent_feature);

		return featureMap;
	}

	public String content_preprocess_lemma(String string) {
		string = string.replace("'", " '");
		string = string.replaceAll("[\\pP‘'“”]", "");
		string = string.replaceAll("\\s{1,}", " ");
		string = string.toLowerCase().trim();
		string = tools.getLemma(string);
		return string;
	}

	public String contentPreprocessPassSW(String string) {
		string = string.replace("'", " '");
		string = string.replaceAll("[\\pP‘'“”]", "");
		string = string.replaceAll("\\s{1,}", " ");
		string = string.toLowerCase().trim();
		string = removeStopWord(string);
		return string;
	}

	public String contentPreprocessPassSWLemma(String string) {
		string = string.replace("'", " '");
		string = string.replaceAll("[\\pP‘'“”]", "");
		string = string.replaceAll("\\s{1,}", " ");
		string = string.toLowerCase().trim();
		string = tools.getLemma(string);
		string = removeStopWord(string);
		return string;
	}

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

	public double getSupportAnswerFeature(String answer, String question, String[] story) {
		int support_index = getMostSimiliarSupportIndex(answer, story);
		double similiar = getWordOverlapFeature(contentPreprocessPassSWLemma(story[support_index]), question);
		return similiar;
	}

	public double getSupport_Q_A_DistanceFeature(String answer, String question, String[] story) {
		int a_index = getMostSimiliarSupportIndex(answer, story);
		int q_index = getMostSimiliarSupportIndex(question, story);
		double distance = Math.abs(q_index - a_index);
		distance = distance / (double) (story.length);
		return -distance;
	}

	public int getMostSimiliarSupportIndex(String answer, String[] story) {
		int index = 0;
		double max = 0;
		for (int i = 0; i < story.length; i++) {
			String story_line = contentPreprocessPassSWLemma(story[i]);
			double similiar = getWordOverlapFeature(answer, story_line);
			if (similiar > max) {
				max = similiar;
				index = i;
			}
		}
		return index;
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

	public double getWordsBagSimiliarFeature(String answer, String question, String story) {
		double maxScore = 0;
		Map<String, Integer> wordMap = get_bag_words(question, answer, story);
		String[] word = story.split(" ");
		for (int i = 0; i < word.length - wordMap.size() + 1; i++) {
			double score = 0;
			ArrayList<String> temp_list = new ArrayList<>();
			for (int j = 0; j < wordMap.size(); j++) {
				if (wordMap.containsKey(word[i + j]) && !temp_list.contains(word[i + j])) {
					double count = wordMap.get(word[i + j]);
					temp_list.add(word[i + j]);
					// score += Math.log(1.0 + 1 / count);
					score++;
				}
			}
			if (score > maxScore) {
				maxScore = score;
			}
		}
		return maxScore / (double) (wordMap.size());
	}

	public double getWordsBagAddWord2VecSimiliarFeature(String answer, String question, String story) {
		double maxScore = -100;
		Map<String, Integer> wordMap = get_bag_words(question, answer, story);
		String statement = "";
		Iterator iterator = wordMap.keySet().iterator();
		while (iterator.hasNext()) {
			statement += iterator.next() + " ";
		}
		String[] word = story.split(" ");
		for (int i = 0; i < word.length - wordMap.size() + 1; i++) {
			double score = 0;
			String story_sentense = "";
			ArrayList<String> temp_list = new ArrayList<>();
			for (int j = 0; j < wordMap.size(); j++) {
				if (!temp_list.contains(word[i + j])) {
					story_sentense += (word[i + j] + " ");
					temp_list.add(word[i + j]);
				}
			}
			score = w2v.getSentenseSimiliarity(statement, story_sentense)[0];
			if (score > maxScore) {
				maxScore = score;
			}
		}
		return maxScore;
	}

	public double getWordsBagMulWord2VecSimiliarFeature(String answer, String question, String story) {
		double maxScore = -100;
		Map<String, Integer> wordMap = get_bag_words(question, answer, story);
		String statement = "";
		Iterator iterator = wordMap.keySet().iterator();
		while (iterator.hasNext()) {
			statement += iterator.next() + " ";
		}
		String[] word = story.split(" ");
		for (int i = 0; i < word.length - wordMap.size() + 1; i++) {
			double score = 0;
			String story_sentense = "";
			ArrayList<String> temp_list = new ArrayList<>();
			for (int j = 0; j < wordMap.size(); j++) {
				if (!temp_list.contains(word[i + j])) {
					story_sentense += (word[i + j] + " ");
					temp_list.add(word[i + j]);
				}
			}
			score = w2v.getSentenseSimiliarity(statement, story_sentense)[1];
			if (score > maxScore) {
				maxScore = score;
			}
		}
		return maxScore;
	}

	public double getWordsBagSumWord2VecSimiliarFeature(String answer, String question, String story) {
		double maxScore = -100;
		Map<String, Integer> wordMap = get_bag_words(question, answer, story);
		String statement = "";
		Iterator iterator = wordMap.keySet().iterator();
		while (iterator.hasNext()) {
			statement += iterator.next() + " ";
		}
		String[] word = story.split(" ");
		for (int i = 0; i < word.length - wordMap.size() + 1; i++) {
			double score = 0;
			String story_sentense = "";
			ArrayList<String> temp_list = new ArrayList<>();
			for (int j = 0; j < wordMap.size(); j++) {
				if (!temp_list.contains(word[i + j])) {
					story_sentense += (word[i + j] + " ");
					temp_list.add(word[i + j]);
				}
			}
			score = w2v.getSentenseSimiliarity(statement, story_sentense)[2];
			if (score > maxScore) {
				maxScore = score;
			}
		}
		return maxScore;
	}

	public double getDoubleWordsBagSimiliarFeature(String answer, String question, String story) {
		// System.out.println("----------------------------------------------------------");
		double maxScore = 0;
		Map<String, Integer> wordMap = get_bag_words(question, answer, story);
		String[] word = story.split(" ");
		for (int i = 0; i < word.length - wordMap.size() * 2 + 1; i++) {
			double score = 0;
			ArrayList<String> temp_list = new ArrayList<>();
			for (int j = 0; j < wordMap.size() * 2; j++) {
				if (wordMap.containsKey(word[i + j]) && !temp_list.contains(word[i + j])) {
					// System.out.println(word[i+j]+","+wordMap.get(word[i+j]));
					double count = wordMap.get(word[i + j]);
					temp_list.add(word[i + j]);
					// score += Math.log(1.0 + 1 / count);
					score++;
				}
			}
			if (score > maxScore) {
				maxScore = score;
			}
		}
		return maxScore / (double) (wordMap.size());
	}

	public double getAverageWordsBagSimiliarFeature(String answer, String question, String story) {
		Map<String, Integer> wordMap = get_bag_words(question, answer, story);
		String[] word = story.split(" ");
		double sum = 0;
		for (int a = 2; a <= 30; a++) {
			double maxScore = 0;
			for (int i = 0; i <= word.length - a; i++) {
				double score = 0;
				ArrayList<String> temp_list = new ArrayList<>();
				for (int j = 0; j < a; j++) {
					if (wordMap.containsKey(word[i + j]) && !temp_list.contains(word[i + j])) {
						double count = wordMap.get(word[i + j]);
						temp_list.add(word[i + j]);
						// score += Math.log(1.0 + 1 / count);
						score++;
					}
				}
				if (score > maxScore) {
					maxScore = score;
				}
			}
			sum += maxScore;
		}
		sum = sum / 29;
		return sum / (double) (wordMap.size());
	}

	public Map<String, Integer> get_bag_words(String question, String answer, String story) {
		Map<String, Integer> map = new HashMap<>();
		String statement = question + " " + answer;
		String[] word = statement.split(" ");
		String[] story_word = story.split(" ");
		for (int i = 0; i < word.length; i++) {
			int count = 0;
			if (!map.containsKey(word[i])) {
				for (int j = 0; j < story_word.length; j++) {
					if (story_word[j].equals(word[i])) {
						count++;
					}
				}
				map.put(word[i], count);
			}
		}
		return map;
	}

	public double getDistanceBasedFeature(String question, String answer, String story) {
		double distance = 1000;
		Map<Integer, String> Sq = new HashMap<Integer, String>();
		Map<Integer, String> Sa = new HashMap<Integer, String>();
		String[] question_word = question.split(" ");
		String[] story_word = story.split(" ");
		String[] answer_word = answer.split(" ");
		for (int i = 0; i < story_word.length; i++) {
			for (int j = 0; j < question_word.length; j++) {
				if (story_word[i].equals(question_word[j])) {
					Sq.put(i, story_word[i]);
				}
			}
		}
		for (int i = 0; i < story_word.length; i++) {
			for (int j = 0; j < answer_word.length; j++) {
				if (story_word[i].equals(answer_word[j])) {
					boolean flag = true;
					for (int m = 0; m < question_word.length; m++) {
						if (question_word[m].equals(story_word[i])) {
							flag = false;
						}
					}
					if (flag) {
						Sa.put(i, story_word[i]);
					}
				}
			}
		}
		if (Sq.size() == 0 || Sa.size() == 0) {
			// System.out.println(story);
			// System.out.println("Sq:"+Sq.size()+",Sa:"+Sa.size()+",answer:"+answer);
			distance = 1;
		} else {
			for (Map.Entry<Integer, String> entry : Sa.entrySet()) {
				for (Map.Entry<Integer, String> entry2 : Sq.entrySet()) {
					double local_distance = Math.abs(entry.getKey() - entry2.getKey());
					if (local_distance < distance) {
						distance = local_distance;
					}
				}
			}
			distance = (1 / (double) (story_word.length - 1)) * (distance + 1);
		}
		return -distance;
	}

	public double getW2VSimiliarFeatureBasedOnSentense(String question, String answer, String[] sentenses) {
		String statement = question + " " + answer;
		double maxScore = 0;
		for (int i = 0; i < sentenses.length; i++) {
			String sentense = contentPreprocessPassSWLemma(sentenses[i]);
			double score = w2v.getSentenseSimiliarity(statement, sentense)[0];
			if (score > maxScore) {
				maxScore = score;
			}
		}
		return maxScore;
	}

	public double getW2VSimiliarFeatureBasedOn2Sentense(String statement, String[] sentenses) {
		double maxScore = 0;
		for (int i = 0; i < sentenses.length - 1; i++) {
			String sentense1 = contentPreprocessPassSWLemma(sentenses[i]);
			double score1 = w2v.getSentenseSimiliarity(statement, sentense1)[0];
			String sentense2 = contentPreprocessPassSWLemma(sentenses[i + 1]);
			double score2 = w2v.getSentenseSimiliarity(statement, sentense2)[0];
			double score = score1 + score2;
			if (score > maxScore) {
				maxScore = score;
			}
		}
		return maxScore;
	}

	public double getW2VSimiliarFeatureBasedOnAll2Sentense(String statement, String[] sentenses) {
		double maxScore = 0;
		for (int i = 0; i < sentenses.length; i++) {
			for (int j = i + 1; j < sentenses.length; j++) {
				String sentense1 = contentPreprocessPassSWLemma(sentenses[i]);
				double score1 = w2v.getSentenseSimiliarity(statement, sentense1)[0];
				String sentense2 = contentPreprocessPassSWLemma(sentenses[j]);
				double score2 = w2v.getSentenseSimiliarity(statement, sentense2)[0];
				double score = score1 + score2;
				if (score > maxScore) {
					maxScore = score;
				}
			}
		}
		return maxScore;
	}

	public boolean isContains(String str1, String str2) {
		boolean flag = false;
		String[] str1_word = str1.split(" ");
		String[] str2_word = str2.split(" ");
		for (int i = 0; i < str1_word.length; i++) {
			if (flag) {
				break;
			}
			for (int j = 0; j < str2_word.length; j++) {
				if (str1_word[i].equals(str2_word[j])) {
					flag = true;
					break;
				}
			}
		}
		return flag;
	}

	public double getSimiliarFeatureBasedOnSentense(String question, String answer, String[] sentenses) {
		String statement = question + " " + answer;
		double maxScore = 0;
		for (int i = 0; i < sentenses.length; i++) {
			String sentense = contentPreprocessPassSWLemma(sentenses[i]);
			if (isContains(answer, sentense)) {
				double score = getWordOverlapFeature(statement, sentense);
				if (score > maxScore) {
					maxScore = score;
				}
			}
		}
		return maxScore;
	}

	public double getBiwordSimiliarFeatureBasedOnSentense(String statement, String answer, String[] sentenses) {
		double maxScore = 0;
		for (int i = 0; i < sentenses.length; i++) {
			String sentense = contentPreprocessPassSWLemma(sentenses[i]);
			if (isContains(answer, sentense)) {
				double score = getBiWordOverlapFeature(statement, sentense);
				if (score > maxScore) {
					maxScore = score;
				}
			}
		}
		return maxScore;
	}

	public double getTriwordSimiliarFeatureBasedOnSentense(String statement, String answer, String[] sentenses) {
		double maxScore = 0;
		for (int i = 0; i < sentenses.length; i++) {
			String sentense = contentPreprocessPassSWLemma(sentenses[i]);
			if (isContains(answer, sentense)) {
				double score = getTriWordOverlapFeature(statement, sentense);
				if (score > maxScore) {
					maxScore = score;
				}
			}
		}
		return maxScore;
	}

	public double getSimiliarFeatureBasedOn2Sentense(String question, String answer, String[] sentenses) {
		String statement = question + " " + answer;
		double maxScore = 0;
		for (int i = 0; i < sentenses.length - 1; i++) {
			String sentense = contentPreprocessPassSWLemma(sentenses[i]) + " "
					+ contentPreprocessPassSWLemma(sentenses[i + 1]);
			if (isContains(answer, sentense)) {
				double score = getWordOverlapFeature(statement, sentense);
				if (score > maxScore) {
					maxScore = score;
				}
			}
		}
		return maxScore;
	}

	public double getBiwordSimiliarFeatureBasedOn2Sentense(String statement, String answer, String[] sentenses) {
		double maxScore = 0;
		for (int i = 0; i < sentenses.length - 1; i++) {
			String sentense = contentPreprocessPassSWLemma(sentenses[i]) + " "
					+ contentPreprocessPassSWLemma(sentenses[i + 1]);
			if (isContains(answer, sentense)) {
				double score = getBiWordOverlapFeature(statement, sentense);
				if (score > maxScore) {
					maxScore = score;
				}
			}
		}
		return maxScore;
	}

	public double getTriwordSimiliarFeatureBasedOn2Sentense(String statement, String answer, String[] sentenses) {
		double maxScore = 0;
		for (int i = 0; i < sentenses.length - 1; i++) {
			String sentense = contentPreprocessPassSWLemma(sentenses[i]) + " "
					+ contentPreprocessPassSWLemma(sentenses[i + 1]);
			if (isContains(answer, sentense)) {
				double score = getTriWordOverlapFeature(statement, sentense);
				if (score > maxScore) {
					maxScore = score;
				}
			}
		}
		return maxScore;
	}

	public double getSimiliarFeatureBasedOnAll2Sentense(String statement, String answer, String[] sentenses) {
		double maxScore = 0;
		for (int i = 0; i < sentenses.length; i++) {
			for (int j = i + 1; j < sentenses.length; j++) {
				String sentense = contentPreprocessPassSWLemma(sentenses[i]) + " "
						+ contentPreprocessPassSWLemma(sentenses[j]);
				if (isContains(answer, sentense)) {
					double score = getWordOverlapFeature(statement, sentense);
					if (score > maxScore) {
						maxScore = score;
					}
				}
			}
		}
		return maxScore;
	}

	public double getBiWordSimiliarFeatureBasedOnAll2Sentense(String statement, String answer, String[] sentenses) {
		double maxScore = 0;
		for (int i = 0; i < sentenses.length; i++) {
			for (int j = i + 1; j < sentenses.length; j++) {
				String sentense = contentPreprocessPassSWLemma(sentenses[i]) + " "
						+ contentPreprocessPassSWLemma(sentenses[j]);
				if (isContains(answer, sentense)) {
					double score = getBiWordOverlapFeature(statement, sentense);
					if (score > maxScore) {
						maxScore = score;
					}
				}
			}
		}
		return maxScore;
	}

	public double getTriWordSimiliarFeatureBasedOnAll2Sentense(String statement, String answer,
			String[] sentenses) {
		double maxScore = 0;
		for (int i = 0; i < sentenses.length; i++) {
			for (int j = i + 1; j < sentenses.length; j++) {
				String sentense = contentPreprocessPassSWLemma(sentenses[i]) + " "
						+ contentPreprocessPassSWLemma(sentenses[j]);
				if (isContains(answer, sentense)) {
					double score = getTriWordOverlapFeature(statement, sentense);
					if (score > maxScore) {
						maxScore = score;
					}
				}
			}
		}
		return maxScore;
	}

	public double getRootComparisonFeature(String answer, Question question, Story story) {
		answer = contentPreprocessPassSWLemma(answer);
		String[] answer_words = answer.split(" ");
		Sentence[] sentenses = story.sentenses;
		double max_score = 0;
		for (int i = 0; i < sentenses.length; i++) {
			String s = contentPreprocessPassSWLemma(sentenses[i].content);
			double score = 0;
			for (int j = 0; j < answer_words.length; j++) {
				if (s.contains(answer_words[j])) {
					score = getRootScore(question, story.sentenses[i]);
					break;
				}
			}
			if (score > max_score) {
				max_score = score;
			}
		}
		return max_score;
	}

	public double getRootScore(Question question, Sentence sentense) {
		double score = 0;
		String q_root = "";
		String s_root = "";
		try {
			q_root = contentPreprocessPassSWLemma(question.root.content);
		} catch (Exception e) {
			q_root = "";
		}
		try {
			s_root = contentPreprocessPassSWLemma(sentense.root.content);
		} catch (Exception e) {
			s_root = "";
		}
		if (q_root.equals(s_root)) {
			score = 1;
		} else {
			score = w2v.getWordSimiliarity(q_root, s_root);
		}
		return score;
	}

	public double getDependency_U_V_Syntax_Feature(Statement statement, Story story, int window_num) {
		Sentence[] sentenses = story.sentenses;
		double max_score = 0;
		WordNode[] statement_words = statement.getWordNode();
		for (int i = 0; i <= sentenses.length - window_num; i++) {
			double score = 0;
			for (int j = 0; j < window_num; j++) {
				WordNode[] sentense_words = sentenses[i + j].wordNodes;
				if (statement_words != null && sentense_words != null) {
					score += getDependency_U_V_S_match(statement_words, sentense_words, true);
				}
			}
			if (score > max_score) {
				max_score = score;
			}
		}
		return max_score;
	}

	public double getDependency_U_V_Feature(Statement statement, Story story, int window_num) {
		Sentence[] sentenses = story.sentenses;
		double max_score = 0;
		WordNode[] statement_words = statement.getWordNode();
		for (int i = 0; i <= sentenses.length - window_num; i++) {
			double score = 0;
			for (int j = 0; j < window_num; j++) {
				WordNode[] sentense_words = sentenses[i + j].wordNodes;
				if (statement_words != null && sentense_words != null) {
					score += getDependency_U_V_S_match(statement_words, sentense_words, false);
				}
			}
			if (score > max_score) {
				max_score = score;
			}
		}
		return max_score;
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

	public double getDependency_SVO_U_V_Syntax_Feature(WordNode[] wordNodes, Story story, int window_num) {
		Sentence[] sentenses = story.sentenses;
		double max_score = 0;
		for (int i = 0; i <= sentenses.length - window_num; i++) {
			double score = 0;
			for (int j = 0; j < window_num; j++) {
				WordNode[] sentense_words = sentenses[i + j].wordNodes;
				if (wordNodes != null && sentense_words != null) {
					score += getDependency_SVO_match(wordNodes, sentense_words, true);
				}
			}
			if (score > max_score) {
				max_score = score;
			}
		}
		return max_score;
	}

	public double getDependency_SVO_U_V_Feature(WordNode[] wordNodes, Story story, int window_num) {
		Sentence[] sentenses = story.sentenses;
		double max_score = 0;
		for (int i = 0; i <= sentenses.length - window_num; i++) {
			double score = 0;
			for (int j = 0; j < window_num; j++) {
				WordNode[] sentense_words = sentenses[i + j].wordNodes;
				if (wordNodes != null && sentense_words != null) {
					score += getDependency_SVO_match(wordNodes, sentense_words, false);
				}
			}
			if (score > max_score) {
				max_score = score;
			}
		}
		return max_score;
	}

	public double getDependency_SO_U_V_Syntax_Feature(WordNode[] wordNodes, Story story, int window_num) {
		Sentence[] sentenses = story.sentenses;
		double max_score = 0;
		for (int i = 0; i <= sentenses.length - window_num; i++) {
			double score = 0;
			for (int j = 0; j < window_num; j++) {
				WordNode[] sentense_words = sentenses[i + j].wordNodes;
				if (wordNodes != null && sentense_words != null) {
					score += getDependency_SO_match(wordNodes, sentense_words, true);
				}
			}
			if (score > max_score) {
				max_score = score;
			}
		}
		return max_score;
	}

	public double getDependency_SO_U_V_Feature(WordNode[] wordNodes, Story story, int window_num) {
		Sentence[] sentenses = story.sentenses;
		double max_score = 0;
		for (int i = 0; i <= sentenses.length - window_num; i++) {
			double score = 0;
			for (int j = 0; j < window_num; j++) {
				WordNode[] sentense_words = sentenses[i + j].wordNodes;
				if (wordNodes != null && sentense_words != null) {
					score += getDependency_SO_match(wordNodes, sentense_words, false);
				}
			}
			if (score > max_score) {
				max_score = score;
			}
		}
		return max_score;
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
						for (int i = 0; i < sentense_bro_list.size()
								&& sentense_bro_list.get(i) != sentense_words[m]; i++) {
							if(sentense_bro_list.get(i) == sentense_words[m]){
								continue;
							}
							for (int j = 0; j < statement_bro_list.size()
									&& statement_bro_list.get(j) != statement_words[n]; j++) {
								if(statement_bro_list.get(j) == statement_words[n]){
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

	public Object[] getIsSharingQAWordFeature(String answer, String question, String[] story) {
		double score = 0;
		String[] answer_word = answer.split(" ");
		String[] question_word = question.split(" ");
		boolean flag = false;
		ArrayList<String> sentense_list = new ArrayList<>();
		for (int i = 0; i < story.length; i++) {
			String sentense = contentPreprocessPassSWLemma(story[i]);
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
				flag = true;
				sentense_list.add(sentense);
			}
		}
		if (flag) {
			score = 1;
		}
		Object[] objects = { score, sentense_list };
		return objects;
	}

	public double getInnerSentenseDistanceFeature(ArrayList<String> sentenselist, String answer, String question) {
		double minDis = 1;
		String[] answer_word = answer.split(" ");
		String[] question_word = question.split(" ");
		for (int i = 0; i < sentenselist.size(); i++) {
			String[] sentense_word = sentenselist.get(i).split(" ");
			ArrayList<Integer> answer_index = new ArrayList<>();
			ArrayList<Integer> question_index = new ArrayList<>();
			for (int j1 = 0; j1 < sentense_word.length; j1++) {
				for (int j2 = 0; j2 < answer_word.length; j2++) {
					if (sentense_word[j1].equals(answer_word[j2])) {
						answer_index.add(j1);
						break;
					}
				}
			}

			for (int j1 = 0; j1 < sentense_word.length; j1++) {
				for (int j2 = 0; j2 < question_word.length; j2++) {
					if (sentense_word[j1].equals(question_word[j2])) {
						question_index.add(j1);
						break;
					}
				}
			}

			for (int n1 = 0; n1 < answer_index.size(); n1++) {
				for (int n2 = 0; n2 < question_index.size(); n2++) {
					double dis = Math.abs(answer_index.get(n1) - question_index.get(n2));
					dis = dis / sentense_word.length;
					if (dis < minDis) {
						minDis = dis;
					}
				}
			}
		}
		return -minDis;
	}

	public double getOuterSentenseDistanceFeature(String answer, String question, String[] sentenses) {
		ArrayList<Integer> answer_index = new ArrayList<>();
		ArrayList<Integer> question_index = new ArrayList<>();
		String[] answer_word = answer.split(" ");
		String[] question_word = question.split(" ");
		for (int i = 0; i < sentenses.length; i++) {
			String[] sentense_word = content_preprocess_lemma(sentenses[i]).split(" ");
			for (int j = 0; j < sentense_word.length; j++) {
				for (int k1 = 0; k1 < answer_word.length; k1++) {
					if (sentense_word[j].equals(answer_word[k1])) {
						if (!answer_index.contains(i)) {
							answer_index.add(i);
						}
					}
				}
				for (int k2 = 0; k2 < question_word.length; k2++) {
					if (sentense_word[j].equals(question_word[k2])) {
						if (!question_index.contains(i)) {
							question_index.add(i);
						}
					}
				}
			}
		}

		double distance = sentenses.length;
		for (Integer index1 : question_index) {
			for (Integer index2 : answer_index) {
				double local_dis = Math.abs(index1 - index2);
				if (local_dis < distance) {
					distance = local_dis;
				}
			}
		}

		return -distance / (double) (sentenses.length);
	}

	public double getIs_Null(String string) {
		if (string.equals("*")) {
			return 1;
		} else {
			return 0;
		}
	}

	public double getSequentialFeature(String question, String answer, String[] story) {
		double score = 0;
		if (question.contains("before")) {
			int index = question.indexOf("before");
			String key_string = question.substring(index);
			int key_index = getMostSimiliarSupportIndex(key_string, story);
			int answer_index = getMostSimiliarSupportIndex(answer, story);
			if (key_index - answer_index == 0) {
				score = 2;
			} else if (key_index - answer_index > 0) {
				score = 2.0 / (double) (key_index - answer_index + 1);
			}
		} else if (question.contains("after")) {
			int index = question.indexOf("after");
			String key_string = question.substring(index);
			int key_index = getMostSimiliarSupportIndex(key_string, story);
			int answer_index = getMostSimiliarSupportIndex(answer, story);
			if (answer_index - key_index == 0) {
				score = 2;
			} else if (answer_index - key_index > 0) {
				score = 2.0 / (double) (answer_index - key_index);
			}
		}
		return score;
	}

	public double getFrameTargetNameFeature(Statement statement, Story story) {
		double score = 0;
		for (int i = 0; i < story.sentenses.length; i++) {
			double local_score = getTargetNameMatchNum(statement.frameList, story.sentenses[i].frameList);
			if (local_score > score) {
				score = local_score;
			}
		}
		return score / 5.0;
	}

	public double getFrameTargetTextFeature(Statement statement, Story story) {
		double score = 0;
		for (int i = 0; i < story.sentenses.length; i++) {
			double local_score = getTargetTextMatchNum(statement.frameList, story.sentenses[i].frameList);
			if (local_score > score) {
				score = local_score;
			}
		}
		return score / 5.0;
	}

	public double getFrameTargetAllFeature(Statement statement, Story story) {
		double score = 0;
		for (int i = 0; i < story.sentenses.length; i++) {
			double local_score = getTargetAllMatchNum(statement.frameList, story.sentenses[i].frameList);
			if (local_score > score) {
				score = local_score;
			}
		}
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

	public double getFrameElementNameFeature(Statement statement, Story story) {
		double score = 0;
		for (int i = 0; i < story.sentenses.length; i++) {
			double local_score = getElementNameNum(statement.frameList, story.sentenses[i].frameList);
			if (local_score > score) {
				score = local_score;
			}
		}
		return score / 5.0;
	}

	public double getFrameElementTextFeature(Statement statement, Story story) {
		double score = 0;
		for (int i = 0; i < story.sentenses.length; i++) {
			double local_score = getElementTextNum(statement.frameList, story.sentenses[i].frameList);
			if (local_score > score) {
				score = local_score;
			}
		}
		return score / 5.0;
	}

	public double getFrameElementAllFeature(Statement statement, Story story) {
		double score = 0;
		for (int i = 0; i < story.sentenses.length; i++) {
			double local_score = getElementAllNum(statement.frameList, story.sentenses[i].frameList);
			if (local_score > score) {
				score = local_score;
			}
		}
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

	public double getFrameElementTargetNameFeature(Statement statement, Story story) {
		double score = 0;
		for (int i = 0; i < story.sentenses.length; i++) {
			double local_score = getElementTargetNameMatchNum(statement.frameList,
					story.sentenses[i].frameList);
			if (local_score > score) {
				score = local_score;
			}
		}
		return score / 5.0;
	}

	public double getFrameElementTargetTextFeature(Statement statement, Story story) {
		double score = 0;
		for (int i = 0; i < story.sentenses.length; i++) {
			double local_score = getElementTargetTextMatchNum(statement.frameList,
					story.sentenses[i].frameList);
			if (local_score > score) {
				score = local_score;
			}
		}
		return score / 5.0;
	}

	public double getFrameElementTargetAllFeature(Statement statement, Story story) {
		double score = 0;
		for (int i = 0; i < story.sentenses.length; i++) {
			double local_score = getElementTargetAllMatchNum(statement.frameList,
					story.sentenses[i].frameList);
			if (local_score > score) {
				score = local_score;
			}
		}
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

	public double get_Is_Sharing_VB_Q_A_Word_Feature(Question que, Ans ans, String[] story) {
		double score = 0;
		String answer = ans.getContent();
		String[] answer_word = answer.split(" ");
		boolean flag = false;
		for (int i = 0; i < story.length; i++) {
			String sentense = contentPreprocessPassSWLemma(story[i]);
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
				flag = true;
			}
		}
		if (flag) {
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
