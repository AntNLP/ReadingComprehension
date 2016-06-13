package FeatureExtraction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import QABasedonDT.Word_Node;
import Tools.Tools;

public class FeatureExtract_DT {
	/*************************
	 * 属性
	 */
	static Tools tools;

	/*********************************
	 * 方法
	 * 
	 * @param path
	 * @param count
	 */
	public FeatureExtract_DT() {
		tools = new Tools();
	}

	public String punctution_preprocess(String string) {
		string = string.replace("'", " '");
		return string;
	}

	public String content_preprocess(String string) {
		string = string.replaceAll("[\\pP‘'“”]", "");
		string = string.toLowerCase().trim();
		return string;
	}

	public String content_preprocess_lemma(String string) {
		string = string.replaceAll("[\\pP‘'“”]", "");
		string = tools.getLemma(string);
		string = string.toLowerCase().trim();
		return string;
	}

	public boolean isContains(String sentense, String word) {
		boolean flag = false;
		String[] sentense_word = sentense.split(" ");
		for (String string : sentense_word) {
			if (string.equals(word)) {
				flag = true;
			}
		}
		return flag;
	}

	public double[] getEdgeFeature(Word_Node z1, Word_Node z2, String question, String answer, String[] story) {
		String z1_currentsentense = z1.getCurrentSentense().getContent();
		String z2_currentsentense = z2.getCurrentSentense().getContent();
		String hypotheis = question + answer;
		double is_child_null = getIs_Null(z1_currentsentense);
		double is_partent_null = getIs_Null(z2_currentsentense);
		double sentense_distance = getSentense_distance_feature(z1.getCurrentSentense().getIndex(),
				z2.getCurrentSentense().getIndex(), story.length);
		double word_distance = 1;
		double is_z1_parent_z2 = 0;
		double is_z1_sibling_z2 = 0;
		double is_z1_child_z2 = 0;
		double is_z2_parent_z1 = 0;
		double is_z2_sibling_z1 = 0;
		double is_z2_child_z1 = 0;
		double z1_z2_overlap = getWord_OverlapFeature(content_preprocess(z1_currentsentense),
				content_preprocess(z2_currentsentense));
		double z1_z2_bigram_overlap = getBiGramsFeature(content_preprocess(z1_currentsentense),
				content_preprocess(z2_currentsentense));
		double w_h_overlap = getWord_OverlapFeature(
				content_preprocess(z1_currentsentense) + content_preprocess(z2_currentsentense),
				content_preprocess(hypotheis));
		double w_h_bigram_overlap = getBiGramsFeature(
				content_preprocess(z1_currentsentense) + content_preprocess(z2_currentsentense),
				content_preprocess(hypotheis));
		double w_q_overlap = getWord_OverlapFeature(
				content_preprocess(z1_currentsentense) + content_preprocess(z2_currentsentense),
				content_preprocess(question));
		double w_q_bigram_overlap = getBiGramsFeature(
				content_preprocess(z1_currentsentense) + content_preprocess(z2_currentsentense),
				content_preprocess(question));
		double w_a_overlap = getWord_OverlapFeature(
				content_preprocess(z1_currentsentense) + content_preprocess(z2_currentsentense),
				content_preprocess(answer));
		double w_a_bigram_overlap = getBiGramsFeature(
				content_preprocess(z1_currentsentense) + content_preprocess(z2_currentsentense),
				content_preprocess(answer));
		double is_z1_parent_z2_lemma = 0;
		double is_z1_sibling_z2_lemma = 0;
		double is_z1_child_z2_lemma = 0;
		double is_z2_parent_z1_lemma = 0;
		double is_z2_sibling_z1_lemma = 0;
		double is_z2_child_z1_lemma = 0;
		double z1_z2_overlap_lemma = getWord_OverlapFeature(content_preprocess_lemma(z1_currentsentense),
				content_preprocess_lemma(z2_currentsentense));
		double z1_z2_bigram_overlap_lemma = getBiGramsFeature(content_preprocess_lemma(z1_currentsentense),
				content_preprocess_lemma(z2_currentsentense));
		double w_h_overlap_lemma = getWord_OverlapFeature(
				content_preprocess_lemma(z1_currentsentense) + content_preprocess_lemma(z2_currentsentense),
				content_preprocess_lemma(hypotheis));
		double w_h_bigram_overlap_lemma = getBiGramsFeature(
				content_preprocess_lemma(z1_currentsentense) + content_preprocess_lemma(z2_currentsentense),
				content_preprocess_lemma(hypotheis));
		double w_q_overlap_lemma = getWord_OverlapFeature(
				content_preprocess_lemma(z1_currentsentense) + content_preprocess_lemma(z2_currentsentense),
				content_preprocess_lemma(question));
		double w_q_bigram_overlap_lemma = getBiGramsFeature(
				content_preprocess_lemma(z1_currentsentense) + content_preprocess_lemma(z2_currentsentense),
				content_preprocess_lemma(question));
		double w_a_overlap_lemma = getWord_OverlapFeature(
				content_preprocess_lemma(z1_currentsentense) + content_preprocess_lemma(z2_currentsentense),
				content_preprocess_lemma(answer));
		double w_a_bigram_overlap_lemma = getBiGramsFeature(
				content_preprocess_lemma(z1_currentsentense) + content_preprocess_lemma(z2_currentsentense),
				content_preprocess_lemma(answer));
		if (sentense_distance == 0) {
			int z1_sentense_index = z1.getCurrentSentense().getWord_index();
			int z2_sentense_index = z2.getCurrentSentense().getWord_index();
			String sentense = z1.getCurrentSentense().getContent();
			word_distance = getSameSentenseDistanceFeature(z1_sentense_index, z2_sentense_index, sentense);
		}
		// Word_Node[] z1_tree = tools.parse(z1_currentsentense);
		// Word_Node[] z2_tree = tools.parse(z2_currentsentense);
		Word_Node[] z1_tree = z1.getCurrentSentense().word_nodes;
		Word_Node[] z2_tree = z2.getCurrentSentense().word_nodes;
		try {
			Word_Node w1 = z1_tree[z1.getCurrentSentense().getWord_index()];
			w1.tools = tools;
			Word_Node w2 = z2_tree[z2.getCurrentSentense().getWord_index()];
			w2.tools = tools;
			if (w1.isParent(w2)) {
				is_z2_parent_z1 = 1;
			}
			if (w1.isChild(w2)) {
				is_z2_child_z1 = 1;
			}
			if (w1.isSibling(w2)) {
				is_z2_sibling_z1 = 1;
			}
			if (w2.isParent(w1)) {
				is_z1_parent_z2 = 1;
			}
			if (w2.isChild(w1)) {
				is_z1_child_z2 = 1;
			}
			if (w2.isSibling(w1)) {
				is_z1_sibling_z2 = 1;
			}
			if (w1.isParent_lemma(w2)) {
				is_z2_parent_z1_lemma = 1;
			}
			if (w1.isChild_lemma(w2)) {
				is_z2_child_z1_lemma = 1;
			}
			if (w1.isSibling_lemma(w2)) {
				is_z2_sibling_z1_lemma = 1;
			}
			if (w2.isParent_lemma(w1)) {
				is_z1_parent_z2_lemma = 1;
			}
			if (w2.isChild_lemma(w1)) {
				is_z1_child_z2_lemma = 1;
			}
			if (w2.isSibling_lemma(w1)) {
				is_z1_sibling_z2_lemma = 1;
			}
		} catch (Exception exception) {
			// System.out.println("exception");
			// System.out.println(z2.getCurrentSentense().getWord_index());
			// System.out.println(z2.getCurrentSentense().content);
		}
		double[] edge_feature = { is_child_null, is_partent_null, sentense_distance, word_distance, is_z1_parent_z2,
				is_z1_sibling_z2, is_z1_child_z2, is_z2_parent_z1, is_z2_sibling_z1, is_z2_child_z1, z1_z2_overlap,z1_z2_bigram_overlap,
				w_h_overlap,w_h_bigram_overlap, w_q_overlap, w_q_bigram_overlap,w_a_overlap, w_a_bigram_overlap,is_z1_parent_z2_lemma, is_z1_sibling_z2_lemma,
				is_z1_child_z2_lemma, is_z2_parent_z1_lemma, is_z2_sibling_z1_lemma, is_z2_child_z1_lemma,
				z1_z2_overlap_lemma, z1_z2_bigram_overlap_lemma,w_h_overlap_lemma, w_h_bigram_overlap_lemma,w_q_overlap_lemma, w_q_bigram_overlap_lemma,w_a_overlap_lemma ,w_a_bigram_overlap_lemma};
		// double[] edge_feature = { is_child_null, is_partent_null,
		//
		// w_a_overlap,
		// w_a_overlap_lemma };

		return edge_feature;
	}

	public double[] getNodeFeature(Word_Node w, String question, String answer, String[] story) {
		String w_currentsentense = w.getCurrentSentense().getContent();
		String hypotheis = question + answer;
		double isnull = getIs_Null(w_currentsentense);
		double word_match = w.getCurrentSentense().getMatch_flag();
		double word_2vec = w.getCurrentSentense().getW2v_flag();
		double word_net = w.getCurrentSentense().getWn_flag();
		double node_h_similiar = getWord_OverlapFeature(content_preprocess(w_currentsentense),
				content_preprocess(hypotheis));
		double node_q_similiar = getWord_OverlapFeature(content_preprocess(w_currentsentense),
				content_preprocess(question));
		double node_a_similiar = getWord_OverlapFeature(content_preprocess(w_currentsentense),
				content_preprocess(answer));
		double node__h_similiar_lemma = getWord_OverlapFeature(content_preprocess_lemma(w_currentsentense),
				content_preprocess_lemma(hypotheis));
		double node__q_similiar_lemma = getWord_OverlapFeature(content_preprocess_lemma(w_currentsentense),
				content_preprocess_lemma(question));
		double node__a_similiar_lemma = getWord_OverlapFeature(content_preprocess_lemma(w_currentsentense),
				content_preprocess_lemma(answer));
		double node__h_bigram_similiar = getBiGramsFeature(content_preprocess(w_currentsentense),
				content_preprocess(hypotheis));
		double node__q_bigram_similiar = getBiGramsFeature(content_preprocess(w_currentsentense),
				content_preprocess(question));
		double node__a_bigram_similiar = getBiGramsFeature(content_preprocess(w_currentsentense),
				content_preprocess(answer));
		double node_h_bigram_similiar_lemma = getBiGramsFeature(content_preprocess_lemma(w_currentsentense),
				content_preprocess_lemma(hypotheis));
		double node_q_bigram_similiar_lemma = getBiGramsFeature(content_preprocess_lemma(w_currentsentense),
				content_preprocess_lemma(question));
		double node_a_bigram_similiar_lemma = getBiGramsFeature(content_preprocess_lemma(w_currentsentense),
				content_preprocess_lemma(answer));
		double[] nodefeature = { isnull, word_match, word_2vec, word_net, node_h_similiar, node_q_similiar,
				node_a_similiar, node__h_similiar_lemma, node__q_similiar_lemma, node__a_similiar_lemma,
				node__h_bigram_similiar, node__q_bigram_similiar, node__a_bigram_similiar, node_h_bigram_similiar_lemma,
				node_q_bigram_similiar_lemma, node_a_bigram_similiar_lemma };
		// double[] nodefeature = { isnull,
		//
		// node_a_similiar,
		// node__a_similiar_lemma,
		//
		// node__a_bigram_similiar,
		// node_a_bigram_similiar_lemma };

		return nodefeature;
	}

	// public double[] getAnswerFeature(Word_Node[] word_nodes, String answer,
	// String question, String[] story) {
	// double[] question_type = getQuestionTypeFeature(question);
	// double answer_support_distance = getAnswer_Support_Distance(word_nodes,
	// answer, story);
	// double answer_support_similiar = getAnswer_Support_Similiar(word_nodes,
	// answer);
	// double[] answerfeature = { question_type[0], question_type[1],
	// question_type[2], question_type[3],
	// question_type[4], question_type[5], answer_support_distance,
	// answer_support_similiar };
	// return answerfeature;
	// }

	public double[] getAnswerFeature(Word_Node word_nodes, String answer, String question, String[] story) {
		String w_currentsentense = word_nodes.getCurrentSentense().getContent();
		double isnull = getIs_Null(w_currentsentense);
		double[] question_type = getQuestionTypeFeature(question);
		double answer_support_distance = getAnswer_Support_Distance(word_nodes, answer, story);
		double answer_support_similiar = getAnswer_Support_Similiar(word_nodes, answer);
		double[] answerfeature = { isnull, question_type[0], question_type[1], question_type[2], question_type[3],
				question_type[4], question_type[5], answer_support_distance, answer_support_similiar };
		return answerfeature;
	}

	public double getIs_Null(String string) {
		if (string.equals("*")) {
			return 1;
		} else {
			return 0;
		}
	}

	public double getAnswer_Support_Similiar(Word_Node word_nodes, String answer) {
		double similiar = 0;
		String word_sentense_content = word_nodes.getCurrentSentense().getContent();
		if (!word_sentense_content.equals(" ")) {
			answer = content_preprocess(answer);
			String story_line = content_preprocess(word_sentense_content);
			similiar += getWord_OverlapFeature(answer, story_line);
		}
		return similiar;
	}

	public double getAnswer_Support_Distance(Word_Node word_nodes, String answer, String[] story) {
		double disntance = 0;
		int index = getAnswer_Index(answer, story);
		boolean flag = false;
		int word_sentense_index = word_nodes.getCurrentSentense().getIndex();
		if (word_sentense_index != -1) {
			flag = true;
			disntance += Math.abs(index - word_sentense_index);
		}
		if (flag) {
			return disntance / (double) (story.length);
		} else {
			return 1;
		}
	}

	// public double getAnswer_Support_Similiar(Word_Node[] word_nodes, String
	// answer) {
	// double similiar = 0;
	// double count = 0;
	// for (int i = 0; i < word_nodes.length; i++) {
	// String word_sentense_content =
	// word_nodes[i].getCurrentSentense().getContent();
	// if (!word_sentense_content.equals(" ")) {
	// answer = content_preprocess(answer);
	// String story_line = content_preprocess(word_sentense_content);
	// similiar += getWord_OverlapFeature(answer, story_line);
	// count++;
	// }
	// }
	// return similiar / count;
	// }
	//
	// public double getAnswer_Support_Distance(Word_Node[] word_nodes, String
	// answer, String[] story) {
	// double disntance = 0;
	// int index = getAnswer_Index(answer, story);
	// double count = 0;
	// boolean flag = false;
	// for (int i = 0; i < word_nodes.length; i++) {
	// int word_sentense_index = word_nodes[i].getCurrentSentense().getIndex();
	// if (word_sentense_index != -1) {
	// flag = true;
	// disntance += Math.abs(index - word_sentense_index);
	// count++;
	// }
	// }
	// if (flag) {
	// return disntance / (double)(count * story.length);
	// } else {
	// return 1;
	// }
	// }

	public int getAnswer_Index(String answer, String[] story) {
		int index = 0;
		double max = 0;
		for (int i = 0; i < story.length; i++) {
			answer = content_preprocess(answer);
			String story_line = content_preprocess(story[i]);
			double similiar = getWord_OverlapFeature(answer, story_line);
			if (similiar > max) {
				max = similiar;
				index = i;
			}
		}
		return index;
	}

	public double[] getQuestionTypeFeature(String question) {
		double[] type_vector = { 0, 0, 0, 0, 0, 0 };
		question = content_preprocess(question);
		String[] question_word = question.split(" ");
		String question_type = question_word[0];
		if (question_type.equals("what")) {
			type_vector[0] = 1;
		} else if (question_type.equals("where")) {
			type_vector[1] = 1;
		} else if (question_type.equals("when")) {
			type_vector[2] = 1;
		} else if (question_type.equals("who")) {
			type_vector[3] = 1;
		} else if (question_type.equals("why")) {
			type_vector[4] = 1;
		} else {
			type_vector[5] = 1;
		}
		return type_vector;
	}

	public double getSentense_distance_feature(int s1, int s2, int length) {
		double dis = 0;
		dis = Math.abs((double) (s1 - s2) / (double) length);
		return dis;
	}

	public double getSameSentenseDistanceFeature(int z1_index, int z2_index, String sentense) {
		double distance = 1;
		String[] sentense_word = sentense.split(" ");
		distance = Math.abs((z1_index - z2_index) + 0.1 / sentense_word.length - 1 + 0.1);
		return distance;
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

	public double getBiGramsFeature(String str1, String str2) {
		double bigrams_score = 0;
		double count = 0;
		String[] str1_Ngram = null;
		String[] str2_Ngram = null;
		str1_Ngram = get_2grams(str1);
		str2_Ngram = get_2grams(str2);
		if (str1_Ngram != null && str2_Ngram != null) {
			for (int i = 0; i < str1_Ngram.length; i++) {
				for (int j = 0; j < str2_Ngram.length; j++) {
					if (str1_Ngram[i].equals(str2_Ngram[j])) {
						count++;
						break;
					}
				}
			}
			bigrams_score = count / (str1_Ngram.length + str2_Ngram.length - count);
		}
		return bigrams_score;
	}

	public String[] get_2grams(String sentense) {
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

}
