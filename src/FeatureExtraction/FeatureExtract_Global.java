package FeatureExtraction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import QABasedonDT.Story;
import Tools.Tools;

public class FeatureExtract_Global {
	static Tools tools;

	public FeatureExtract_Global() {
		tools = new Tools();
	}

	public double[] getGlobalFeature(String answer, String question, Story story) {
		double words_bag_sim_feature = get_Words_Bag_Similiar_Feature(content_preprocess(answer),content_preprocess( question), content_preprocess(story.content));
		double words_distance_feature = getDistanceBasedFeature(content_preprocess(question),content_preprocess( answer), content_preprocess(story.content));
		double words_bag_sim_feature_lemma = get_Words_Bag_Similiar_Feature(content_preprocess_lemma(answer),
				content_preprocess_lemma(question), content_preprocess_lemma(story.content));
		double words_distance_feature_lemma = getDistanceBasedFeature(content_preprocess_lemma(question),content_preprocess_lemma( answer), content_preprocess_lemma(story.content));
		double support_question_feature = get_Support_Question_Feature(content_preprocess_lemma(answer),content_preprocess_lemma(question), story.sentense_string);
		double[] global_feature = { words_bag_sim_feature, words_distance_feature, words_bag_sim_feature_lemma,
				words_distance_feature_lemma, support_question_feature };
		// double[] global_feature = { b,
		// words_bag_sim_feature,words_bag_sim_feature_lemma,support_question_feature
		// };
//		System.out.println("words_bag_sim_feature:" + words_bag_sim_feature + ",words_distance_feature："
//				+ words_distance_feature + ",words_bag_sim_feature_lemma：" + words_bag_sim_feature_lemma
//				+ ",words_distance_feature_lemma：" + words_distance_feature_lemma + ",support_question_feature："
//				+ support_question_feature);
		return global_feature;
	}

	public String content_preprocess(String string) {
		string = string.replace("'s", "");
		string = string.replaceAll("[\\pP‘'“”]", "");
		string = string.toLowerCase().trim();
		return string;
	}
	
	public String content_preprocess_lemma(String string) {
		string = string.replace("'s", "");
		string = string.replaceAll("[\\pP‘'“”]", "");
		string = tools.getLemma(string);
		string = string.toLowerCase().trim();
		return string;
	}

	public double get_Support_Question_Feature(String answer, String question, String[] story) {
		String support = get_Most_Similiar_Support_Index(answer, story);
		double similiar = getWord_OverlapFeature(support, question);
		return similiar;
	}

	public String get_Most_Similiar_Support_Index(String answer, String[] story) {
		int index = 0;
		double max = 0;
		for (int i = 0; i < story.length; i++) {
			String story_line = content_preprocess_lemma(story[i]);
			double similiar = getWord_OverlapFeature(answer, story_line);
			if (similiar > max) {
				max = similiar;
				index = i;
			}
		}
		return story[index];
	}

	public double getWord_OverlapFeature(String str1, String str2) {
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

	public double get_Words_Bag_Similiar_Feature(String answer, String question, String story) {
		// System.out.println("----------------------------------------------------------");
		double maxScore = -100;
		Map<String, Integer> wordMap = get_bag_words(question, answer, story);
		String[] word = story.split(" ");
		for (int i = 0; i < word.length - wordMap.size(); i++) {
			double score = 0;
			for (int j = 0; j < wordMap.size(); j++) {
				if (wordMap.containsKey(word[i + j])) {
					// System.out.println(word[i+j]+","+wordMap.get(word[i+j]));
					double count = wordMap.get(word[i + j]);
					score += Math.log(1.0 + 1 / count);
					// score++;
				}
			}
			if (score > maxScore) {
				maxScore = score;
			}
		}
		return maxScore;
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
//			System.out.println(story);
//			System.out.println("Sq:"+Sq.size()+",Sa:"+Sa.size()+",answer:"+answer);
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
			distance = (1 /(double) (story_word.length - 1)) * (distance + 1);
		}
		return -distance;
	}

	public double getIs_Null(String string) {
		if (string.equals("*")) {
			return 1;
		} else {
			return 0;
		}
	}
}
