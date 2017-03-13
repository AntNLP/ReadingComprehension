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

public class FeatureExtractDT {
	/***
	 * @author chenruili
	 */
	static Tools tools = new Tools();
	Word2Vec w2v;
	String type = "";

	public FeatureExtractDT(String type) {
		w2v = new Word2Vec(QABasedonDT.w2vTable);
		this.type = type;
	}
	
	public String getType(){
		return type;
	}

	/***
	 * 
	 * @param the
	 *            input text
	 * @return text(replace+toLowerCase)
	 */
	public String contentPreprocess(String string) {
		string = string.replace("'", " '");
		string = string.replaceAll("[\\pP‘'“”]", "");
		string = string.replaceAll("\\s{1,}", " ");
		string = string.toLowerCase().trim();
		return string;
	}

	/***
	 * 
	 * @param the
	 *            input text
	 * @return text(replace+toLowerCase+Lemma)
	 */
	public String contentPreprocessLemma(String string) {
		string = string.replace("'", " '");
		string = string.replaceAll("[\\pP‘'“”]", "");
		string = string.replaceAll("\\s{1,}", " ");
		string = string.toLowerCase().trim();
		string = tools.getLemma(string);
		return string;
	}

	/***
	 * 
	 * @param the
	 *            input text
	 * @return text(replace+toLowerCase+removeStopWord)
	 */
	public String contentPreprocessPassSW(String string) {
		string = string.replace("'", " '");
		string = string.replaceAll("[\\pP‘'“”]", "");
		string = string.replaceAll("\\s{1,}", " ");
		string = string.toLowerCase().trim();
		string = removeStopWord(string);
		return string;
	}

	/***
	 * 
	 * @param the
	 *            input text
	 * @return text(replace+toLowerCase+Lemma+removeStopWord)
	 */
	public String contentPreprocessPassSWLemma(String string) {
		string = string.replace("'", " '");
		string = string.replaceAll("[\\pP‘'“”]", "");
		string = string.replaceAll("\\s{1,}", " ");
		string = string.toLowerCase().trim();
		string = tools.getLemma(string);
		string = removeStopWord(string);
		return string;
	}

	/***
	 * 
	 * @param the
	 *            input text
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

	/***
	 * 
	 * @param node1
	 * @param node2
	 * @param question
	 * @param answser
	 * @param story
	 * @param statement
	 * @return edge feature list
	 */
	public HashMap<String, Double> getAllEdgeFeature(WordNode z1, WordNode z2, Question que, Ans ans, String[] story,
			Statement statement) {
		HashMap<String, Double> hashMap = new HashMap<>();
		String z1_currentsentense = z1.getCurrentSentense().getContent();
		String z2_currentsentense = z2.getCurrentSentense().getContent();
		String question = que.getContent();
		String answer = ans.getContent();
		String hypotheis = question + " " + answer;

		double sentense_distance = getSentenseDistanceFeature(z1.getCurrentSentense().getIndex(),
				z2.getCurrentSentense().getIndex(), story.length);
		hashMap.put("sentense_distance", sentense_distance);

		double w_h_overlap = getWordOverlapFeature(
				contentPreprocessPassSW(z1_currentsentense) + " " + contentPreprocessPassSW(z2_currentsentense),
				contentPreprocessPassSW(hypotheis));
		hashMap.put("w_h_overlap", w_h_overlap);

		double w_h_left_overlap = getLeftDoubleWordOverlapFeature(z1, z2, hypotheis, story);
		hashMap.put("w_h_left_overlap", w_h_left_overlap);

		double w_h_right_overlap = getRightDoubleWordOverlapFeature(z1, z2, hypotheis, story);
		hashMap.put("w_h_right_overlap", w_h_right_overlap);

		double w_h_left_right_overlap = getRightLeftDoubleWordOverlapFeature(z1, z2, hypotheis, story);
		hashMap.put("w_h_left_right_overlap", w_h_left_right_overlap);

		double w_h_bigram_overlap = getBiGramsFeature(
				contentPreprocessPassSW(z1_currentsentense) + contentPreprocessPassSW(z2_currentsentense),
				contentPreprocessPassSW(statement.getContent()));
		hashMap.put("w_h_bigram_overlap", w_h_bigram_overlap);

		if (type.equals("question")) {
			double w_a_overlap = getWordOverlapFeature(
					contentPreprocessPassSW(z1_currentsentense) + " " + contentPreprocessPassSW(z2_currentsentense),
					contentPreprocessPassSW(answer));
			hashMap.put("w_a_overlap", w_a_overlap);

			double w_a_left_overlap = getLeftDoubleWordOverlapFeature(z1, z2, answer, story);
			hashMap.put("w_a_left_overlap", w_a_left_overlap);

			double w_a_right_overlap = getRightDoubleWordOverlapFeature(z1, z2, answer, story);
			hashMap.put("w_a_right_overlap", w_a_right_overlap);

			double w_a_left_right_overlap = getRightLeftDoubleWordOverlapFeature(z1, z2, answer, story);
			hashMap.put("w_a_left_right_overlap", w_a_left_right_overlap);

			double w_a_bigram_overlap = getBiGramsFeature(
					contentPreprocessPassSW(z1_currentsentense) + " " + contentPreprocessPassSW(z2_currentsentense),
					contentPreprocessPassSW(answer));
			hashMap.put("w_a_bigram_overlap", w_a_bigram_overlap);

			double w_a_overlap_lemma = getWordOverlapFeature(contentPreprocessPassSWLemma(z1_currentsentense) + " "
					+ contentPreprocessPassSWLemma(z2_currentsentense), contentPreprocessPassSWLemma(answer));
			hashMap.put("w_a_overlap_lemma", w_a_overlap_lemma);

			double w_a_left_overlap_lemma = getLeftDoubleWordOverlapFeatureLemma(z1, z2, answer, story);
			hashMap.put("w_a_left_overlap_lemma", w_a_left_overlap_lemma);

			double w_a_right_overlap_lemma = getRightDoubleWordOverlapFeatureLemma(z1, z2, answer, story);
			hashMap.put("w_a_right_overlap_lemma", w_a_right_overlap_lemma);

			double w_a_left_right_overlap_lemma = getRightLeftDoubleWordOverlapFeatureLemma(z1, z2, answer, story);
			hashMap.put("w_a_left_right_overlap_lemma", w_a_left_right_overlap_lemma);

			double w_a_bigram_overlap_lemma = getBiGramsFeature(contentPreprocessPassSWLemma(z1_currentsentense) + " "
					+ contentPreprocessPassSWLemma(z2_currentsentense), contentPreprocessPassSWLemma(answer));
			hashMap.put("w_a_bigram_overlap_lemma", w_a_bigram_overlap_lemma);

			double w_a_w2v = getW2VFeature(
					contentPreprocessPassSW(z1_currentsentense) + " " + contentPreprocessPassSW(z2_currentsentense),
					contentPreprocessPassSW(answer));
			hashMap.put("w_a_w2v", w_a_w2v);

			double w_a_w2v_lemma = getW2VFeature(
					contentPreprocessPassSWLemma(z1_currentsentense) + " "
							+ contentPreprocessPassSWLemma(z2_currentsentense),
					contentPreprocessPassSWLemma(hypotheis));
			hashMap.put("w_a_w2v_lemma", w_a_w2v_lemma);
		} else {
			double w_q_overlap = getWordOverlapFeature(
					contentPreprocessPassSW(z1_currentsentense) + " " + contentPreprocessPassSW(z2_currentsentense),
					contentPreprocessPassSW(question));
			hashMap.put("w_q_overlap", w_q_overlap);

			double w_q_left_overlap = getLeftDoubleWordOverlapFeature(z1, z2, question, story);
			hashMap.put("w_q_left_overlap", w_q_left_overlap);

			double w_q_right_overlap = getRightDoubleWordOverlapFeature(z1, z2, question, story);
			hashMap.put("w_q_right_overlap", w_q_right_overlap);

			double w_q_left_right_overlap = getRightLeftDoubleWordOverlapFeature(z1, z2, question, story);
			hashMap.put("w_q_left_right_overlap", w_q_left_right_overlap);

			double w_q_bigram_overlap = getBiGramsFeature(
					contentPreprocessPassSW(z1_currentsentense) + " " + contentPreprocessPassSW(z2_currentsentense),
					contentPreprocessPassSW(question));
			hashMap.put("w_q_bigram_overlap", w_q_bigram_overlap);

			double w_q_overlap_lemma = getWordOverlapFeature(contentPreprocessPassSWLemma(z1_currentsentense) + " "
					+ contentPreprocessPassSWLemma(z2_currentsentense), contentPreprocessPassSWLemma(question));
			hashMap.put("w_q_overlap_lemma", w_q_overlap_lemma);

			double w_q_left_overlap_lemma = getLeftDoubleWordOverlapFeatureLemma(z1, z2, question, story);
			hashMap.put("w_q_left_overlap_lemma", w_q_left_overlap_lemma);

			double w_q_right_overlap_lemma = getRightDoubleWordOverlapFeatureLemma(z1, z2, question, story);
			hashMap.put("w_q_right_overlap_lemma", w_q_right_overlap_lemma);

			double w_q_left_right_overlap_lemma = getRightLeftDoubleWordOverlapFeatureLemma(z1, z2, question, story);
			hashMap.put("w_q_left_right_overlap_lemma", w_q_left_right_overlap_lemma);

			double w_q_bigram_overlap_lemma = getBiGramsFeature(contentPreprocessPassSWLemma(z1_currentsentense) + " "
					+ contentPreprocessPassSWLemma(z2_currentsentense), contentPreprocessPassSWLemma(question));
			hashMap.put("w_q_bigram_overlap_lemma", w_q_bigram_overlap_lemma);

			double w_q_w2v = getW2VFeature(
					contentPreprocessPassSW(z1_currentsentense) + " " + contentPreprocessPassSW(z2_currentsentense),
					contentPreprocessPassSW(question));
			hashMap.put("w_q_w2v", w_q_w2v);

			double w_q_w2v_lemma = getW2VFeature(contentPreprocessPassSWLemma(z1_currentsentense) + " "
					+ contentPreprocessPassSWLemma(z2_currentsentense), contentPreprocessPassSWLemma(question));
			hashMap.put("w_q_w2v_lemma", w_q_w2v_lemma);
		}

		double w_h_overlap_lemma = getWordOverlapFeature(contentPreprocessPassSWLemma(z1_currentsentense) + " "
				+ contentPreprocessPassSWLemma(z2_currentsentense), contentPreprocessPassSWLemma(hypotheis));
		hashMap.put("w_h_overlap_lemma", w_h_overlap_lemma);

		double w_h_left_overlap_lemma = getLeftDoubleWordOverlapFeatureLemma(z1, z2, hypotheis, story);
		hashMap.put("w_h_left_overlap_lemma", w_h_left_overlap_lemma);

		double w_h_right_overlap_lemma = getRightDoubleWordOverlapFeatureLemma(z1, z2, hypotheis, story);
		hashMap.put("w_h_right_overlap_lemma", w_h_right_overlap_lemma);

		double w_h_left_right_overlap_lemma = getRightLeftDoubleWordOverlapFeatureLemma(z1, z2, hypotheis, story);
		hashMap.put("w_h_left_right_overlap_lemma", w_h_left_right_overlap_lemma);

		double w_h_bigram_overlap_lemma = getBiGramsFeature(
				contentPreprocessPassSWLemma(z1_currentsentense) + " "
						+ contentPreprocessPassSWLemma(z2_currentsentense),
				contentPreprocessPassSWLemma(statement.getContent()));
		hashMap.put("w_h_bigram_overlap_lemma", w_h_bigram_overlap_lemma);

		double w_h_w2v = getW2VFeature(
				contentPreprocessPassSW(z1_currentsentense) + " " + contentPreprocessPassSW(z2_currentsentense),
				contentPreprocessPassSW(hypotheis));
		hashMap.put("w_h_w2v", w_h_w2v);

		double w_h_w2v_lemma = getW2VFeature(contentPreprocessPassSWLemma(z1_currentsentense) + " "
				+ contentPreprocessPassSWLemma(z2_currentsentense), contentPreprocessPassSWLemma(hypotheis));
		hashMap.put("w_h_w2v_lemma", w_h_w2v_lemma);

		double word_distance = 1;
		if (sentense_distance == 0) {
			int z1_sentense_index = z1.getCurrentSentense().getWordIndex();
			int z2_sentense_index = z2.getCurrentSentense().getWordIndex();
			String sentense = z1.getCurrentSentense().getContent();
			word_distance = getSameSentenseDistanceFeature(z1_sentense_index, z2_sentense_index, sentense);
		}
		hashMap.put("word_distance", word_distance);

		double dependency_svo_u_v_s_feature = getDependency_SVO_U_V_Syntax_Feature(statement.getWordNode(),
				z1.currentSentense) + getDependency_SVO_U_V_Syntax_Feature(statement.getWordNode(), z2.currentSentense);
		hashMap.put("dependency_svo_u_v_s_feature", dependency_svo_u_v_s_feature);

		double dependency_svo_u_v_feature = getDependency_SVO_U_V_Feature(statement.getWordNode(), z1.currentSentense)
				+ getDependency_SVO_U_V_Feature(statement.getWordNode(), z2.currentSentense);
		hashMap.put("dependency_svo_u_v_feature", dependency_svo_u_v_feature);

		double dependency_so_u_v_s_feature = getDependency_SO_U_V_Syntax_Feature(statement.getWordNode(),
				z1.currentSentense) + getDependency_SO_U_V_Syntax_Feature(statement.getWordNode(), z2.currentSentense);
		hashMap.put("dependency_so_u_v_s_feature", dependency_so_u_v_s_feature);

		double dependency_so_u_v_feature = getDependency_SO_U_V_Feature(statement.getWordNode(), z1.currentSentense)
				+ getDependency_SO_U_V_Feature(statement.getWordNode(), z2.currentSentense);
		hashMap.put("dependency_so_u_v_feature", dependency_so_u_v_feature);

		double is_sharing_q_a_word_feature = getIsSharingQAWordEdgeFeature(contentPreprocessPassSWLemma(answer),
				contentPreprocessPassSWLemma(question), contentPreprocessPassSWLemma(z1_currentsentense))
				| getIsSharingQAWordEdgeFeature(contentPreprocessPassSWLemma(answer),
						contentPreprocessPassSWLemma(question), contentPreprocessPassSWLemma(z2_currentsentense));
		hashMap.put("is_sharing_q_a_word_feature", is_sharing_q_a_word_feature);

		double frame_target_name_feature = getFrameTargetNameFeature(statement, z1.currentSentense)
				+ getFrameTargetNameFeature(statement, z2.currentSentense);
		hashMap.put("frame_target_name_feature", frame_target_name_feature);

		double frame_target_text_feature = getFrameTargetTextFeature(statement, z1.currentSentense)
				+ getFrameTargetTextFeature(statement, z2.currentSentense);
		hashMap.put("frame_target_text_feature", frame_target_text_feature);

		double frame_target_all_feature = getFrameTargetAllFeature(statement, z1.currentSentense)
				+ getFrameTargetAllFeature(statement, z2.currentSentense);
		hashMap.put("frame_target_all_feature", frame_target_all_feature);

		double frame_element_name_feature = getFrameElementNameFeature(statement, z1.currentSentense)
				+ getFrameElementNameFeature(statement, z2.currentSentense);
		hashMap.put("frame_element_name_feature", frame_element_name_feature);

		double frame_element_text_feature = getFrameElementTextFeature(statement, z1.currentSentense)
				+ getFrameElementTextFeature(statement, z2.currentSentense);
		hashMap.put("frame_element_text_feature", frame_element_text_feature);

		double frame_element_all_feature = getFrameElementAllFeature(statement, z1.currentSentense)
				+ getFrameElementAllFeature(statement, z2.currentSentense);
		hashMap.put("frame_element_all_feature", frame_element_all_feature);

		double frame_element_target_name_feature = getFrameElementTargetNameFeature(statement, z1.currentSentense)
				+ getFrameElementTargetNameFeature(statement, z2.currentSentense);
		hashMap.put("frame_element_target_name_feature", frame_element_target_name_feature);

		double frame_element_target_text_feature = getFrameElementTargetTextFeature(statement, z1.currentSentense)
				+ getFrameElementTargetTextFeature(statement, z2.currentSentense);
		hashMap.put("frame_element_target_text_feature", frame_element_target_text_feature);

		double frame_element_target_all_feature = getFrameElementTargetAllFeature(statement, z1.currentSentense)
				+ getFrameElementTargetAllFeature(statement, z2.currentSentense);
		hashMap.put("frame_element_target_all_feature", frame_element_target_all_feature);

		double is_sharing_VB_q_a_word_feature = get_Is_Sharing_VB_Q_A_Word_Edge_Feature(
				contentPreprocessPassSWLemma(answer), que, contentPreprocessPassSWLemma(z1_currentsentense))
				| get_Is_Sharing_VB_Q_A_Word_Edge_Feature(contentPreprocessPassSWLemma(answer), que,
						contentPreprocessPassSWLemma(z2_currentsentense));
		hashMap.put("is_sharing_VB_q_a_word_feature", is_sharing_VB_q_a_word_feature);

		double name_type_distance_feature = getNameTypeDistanceFeature(contentPreprocessPassSWLemma(answer),
				contentPreprocessPassSWLemma(question), contentPreprocessPassSWLemma(z1_currentsentense) + " "
						+ contentPreprocessPassSWLemma(z2_currentsentense));
		hashMap.put("name_type_distance_feature", name_type_distance_feature);

		double name_type_adjacent_feature = getNameTypeAdjacentFeature(contentPreprocessPassSWLemma(answer),
				contentPreprocessPassSWLemma(question), contentPreprocessPassSWLemma(z1_currentsentense) + " "
						+ contentPreprocessPassSWLemma(z2_currentsentense));
		hashMap.put("name_type_adjacent_feature", name_type_adjacent_feature);

		double z_q_relation_feature = getIsSameRelationWithQueFeature(z1.currentSentense, z2.currentSentense, z1, z2,
				que);
		hashMap.put("z_q_relation_feature", z_q_relation_feature);

		double z_a_relation_feature = getIsSameRelationWithAnsFeature(z1.currentSentense, z2.currentSentense, z1, z2,
				ans);
		hashMap.put("z_a_relation_feature", z_a_relation_feature);

		return hashMap;
	}

	/***
	 * 
	 * @param node
	 * @param question
	 * @param answer
	 * @param story
	 * @param statement
	 * @return node feature list
	 */
	public HashMap<String, Double> getAllNodeFeature(WordNode w, Question que, Ans ans, String[] story,
			Statement statement) {
		HashMap<String, Double> hashMap = new HashMap<>();
		String w_currentsentense = w.getCurrentSentense().getContent();
		String question = que.getContent();
		String answer = ans.getContent();
		String hypotheis = question + answer;

		double node_h_similiar = getWordOverlapFeature(contentPreprocessPassSW(w_currentsentense),
				contentPreprocessPassSW(hypotheis));
		hashMap.put("node_h_similiar", node_h_similiar);

		double node_h_left_similiar = getLeftSingleWordOverlapFeature(w, hypotheis, story);
		hashMap.put("node_h_left_similiar", node_h_left_similiar);

		double node_h_right_similiar = getRightSingleWordOverlapFeature(w, hypotheis, story);
		hashMap.put("node_h_right_similiar", node_h_right_similiar);

		double node_h_left_right_similiar = getRightLeftSingleWordOverlapFeature(w, hypotheis, story);
		hashMap.put("node_h_left_right_similiar", node_h_left_right_similiar);

		if (type.equals("question")) {
			double node_a_similiar = getWordOverlapFeature(contentPreprocessPassSW(w_currentsentense),
					contentPreprocessPassSW(answer));
			hashMap.put("node_a_similiar", node_a_similiar);

			double node_a_left_similiar = getLeftSingleWordOverlapFeature(w, answer, story);
			hashMap.put("node_a_left_similiar", node_a_left_similiar);

			double node_a_right_similiar = getRightSingleWordOverlapFeature(w, answer, story);
			hashMap.put("node_a_right_similiar", node_a_right_similiar);

			double node_a_left_right_similiar = getRightLeftSingleWordOverlapFeature(w, answer, story);
			hashMap.put("node_a_left_right_similiar", node_a_left_right_similiar);

			double node_a_similiar_lemma = getWordOverlapFeature(contentPreprocessPassSWLemma(w_currentsentense),
					contentPreprocessPassSWLemma(answer));
			hashMap.put("node_a_similiar_lemma", node_a_similiar_lemma);

			double node_a_left_similiar_lemma = getLeftSingleWordOverlapFeatureLemma(w, answer, story);
			hashMap.put("node_a_left_similiar_lemma", node_a_left_similiar_lemma);

			double node_a_right_similiar_lemma = getRightSingleWordOverlapFeatureLemma(w, answer, story);
			hashMap.put("node_a_right_similiar_lemma", node_a_right_similiar_lemma);

			double node_a_left_right_similiar_lemma = getRightLeftSingleWordOverlapFeatureLemma(w, answer, story);
			hashMap.put("node_a_left_right_similiar_lemma", node_a_left_right_similiar_lemma);

			double node_a_bigram_similiar = getBiGramsFeature(contentPreprocessPassSW(w_currentsentense),
					contentPreprocessPassSW(answer));
			hashMap.put("node_a_bigram_similiar", node_a_bigram_similiar);

			double node_a_bigram_similiar_lemma = getBiGramsFeature(contentPreprocessPassSWLemma(w_currentsentense),
					contentPreprocessPassSWLemma(answer));
			hashMap.put("node_a_bigram_similiar_lemma", node_a_bigram_similiar_lemma);

			double node_a_w2v = getW2VFeature(contentPreprocessPassSW(w_currentsentense),
					contentPreprocessPassSW(answer));
			hashMap.put("node_a_w2v", node_a_w2v);

			double node_a_w2v_lemma = getW2VFeature(contentPreprocessPassSWLemma(w_currentsentense),
					contentPreprocessPassSWLemma(hypotheis));
			hashMap.put("node_a_w2v_lemma", node_a_w2v_lemma);
		}else{
			double node_q_similiar = getWordOverlapFeature(contentPreprocessPassSW(w_currentsentense),
					contentPreprocessPassSW(question));
			hashMap.put("node_q_similiar", node_q_similiar);

			double node_q_left_similiar = getLeftSingleWordOverlapFeature(w, question, story);
			hashMap.put("node_q_left_similiar", node_q_left_similiar);

			double node_q_right_similiar = getRightSingleWordOverlapFeature(w, question, story);
			hashMap.put("node_q_right_similiar", node_q_right_similiar);

			double node_q_left_right_similiar = getRightLeftSingleWordOverlapFeature(w, question, story);
			hashMap.put("node_q_left_right_similiar", node_q_left_right_similiar);
			
			double node_q_similiar_lemma = getWordOverlapFeature(contentPreprocessPassSWLemma(w_currentsentense),
					contentPreprocessPassSWLemma(question));
			hashMap.put("node_q_similiar_lemma", node_q_similiar_lemma);

			double node_q_left_similiar_lemma = getLeftSingleWordOverlapFeatureLemma(w, question, story);
			hashMap.put("node_q_left_similiar_lemma", node_q_left_similiar_lemma);

			double node_q_right_similiar_lemma = getRightSingleWordOverlapFeatureLemma(w, question, story);
			hashMap.put("node_q_right_similiar_lemma", node_q_right_similiar_lemma);

			double node_q_left_right_similiar_lemma = getRightLeftSingleWordOverlapFeatureLemma(w, question, story);
			hashMap.put("node_q_left_right_similiar_lemma", node_q_left_right_similiar_lemma);
			
			double node_q_bigram_similiar = getBiGramsFeature(contentPreprocessPassSW(w_currentsentense),
					contentPreprocessPassSW(question));
			hashMap.put("node_q_bigram_similiar", node_q_bigram_similiar);
			
			double node_q_bigram_similiar_lemma = getBiGramsFeature(contentPreprocessPassSWLemma(w_currentsentense),
					contentPreprocessPassSWLemma(question));
			hashMap.put("node_q_bigram_similiar_lemma", node_q_bigram_similiar_lemma);
			
			double node_q_w2v = getW2VFeature(contentPreprocessPassSW(w_currentsentense),
					contentPreprocessPassSW(question));
			hashMap.put("node_q_w2v", node_q_w2v);

			double node_q_w2v_lemma = getW2VFeature(contentPreprocessPassSWLemma(w_currentsentense),
					contentPreprocessPassSWLemma(question));
			hashMap.put("node_q_w2v_lemma", node_q_w2v_lemma);
		}
		double node_h_similiar_lemma = getWordOverlapFeature(contentPreprocessPassSWLemma(w_currentsentense),
				contentPreprocessPassSWLemma(hypotheis));
		hashMap.put("node_h_similiar_lemma", node_h_similiar_lemma);

		double node_h_left_similiar_lemma = getLeftSingleWordOverlapFeatureLemma(w, hypotheis, story);
		hashMap.put("node_h_left_similiar_lemma", node_h_left_similiar_lemma);

		double node_h_right_similiar_lemma = getRightSingleWordOverlapFeatureLemma(w, hypotheis, story);
		hashMap.put("node_h_right_similiar_lemma", node_h_right_similiar_lemma);

		double node_h_left_right_similiar_lemma = getRightLeftSingleWordOverlapFeatureLemma(w, hypotheis, story);
		hashMap.put("node_h_left_right_similiar_lemma", node_h_left_right_similiar_lemma);

		double node_h_bigram_similiar = getBiGramsFeature(contentPreprocessPassSW(w_currentsentense),
				contentPreprocessPassSW(statement.getContent()));
		hashMap.put("node_h_bigram_similiar", node_h_bigram_similiar);

		double node_h_bigram_similiar_lemma = getBiGramsFeature(contentPreprocessPassSWLemma(w_currentsentense),
				contentPreprocessPassSWLemma(statement.getContent()));
		hashMap.put("node_h_bigram_similiar_lemma", node_h_bigram_similiar_lemma);

		double node_h_w2v = getW2VFeature(contentPreprocessPassSW(w_currentsentense),
				contentPreprocessPassSW(hypotheis));
		hashMap.put("node_h_w2v", node_h_w2v);

		double node_h_left_w2v = getLeftW2VFeature(w, hypotheis, story);
		hashMap.put("node_h_left_w2v", node_h_left_w2v);

		double node_h_right_w2v = getRightW2VFeature(w, hypotheis, story);
		hashMap.put("node_h_right_w2v", node_h_right_w2v);

		double node_h_w2v_lemma = getW2VFeature(contentPreprocessPassSWLemma(w_currentsentense),
				contentPreprocessPassSWLemma(hypotheis));
		hashMap.put("node_h_w2v_lemma", node_h_w2v_lemma);

		double node_h_left_w2v_lemma = getLeftW2VFeatureLemma(w, hypotheis, story);
		hashMap.put("node_h_left_w2v_lemma", node_h_left_w2v_lemma);

		double node_h_right_w2v_lemma = getRightW2VFeatureLemma(w, hypotheis, story);
		hashMap.put("node_h_right_w2v_lemma", node_h_right_w2v_lemma);

		double dependency_u_v_s_feautre = getDependency_U_V_S_match(statement.getWordNode(),
				w.getCurrentSentense().wordNodes, true);
		hashMap.put("dependency_u_v_s_feautre", dependency_u_v_s_feautre);

		double dependency_u_v_feature = getDependency_U_V_S_match(statement.getWordNode(),
				w.getCurrentSentense().wordNodes, false);
		hashMap.put("dependency_u_v_feature", dependency_u_v_feature);

		double dependency_svo_u_v_s_feature = getDependency_SVO_U_V_Syntax_Feature(statement.getWordNode(),
				w.currentSentense);
		hashMap.put("dependency_svo_u_v_s_feature", dependency_svo_u_v_s_feature);

		double dependency_svo_u_v_feature = getDependency_SVO_U_V_Feature(statement.getWordNode(), w.currentSentense);
		hashMap.put("dependency_svo_u_v_feature", dependency_svo_u_v_feature);

		double dependency_so_u_v_s_feature = getDependency_SO_U_V_Syntax_Feature(statement.getWordNode(),
				w.currentSentense);
		hashMap.put("dependency_so_u_v_s_feature", dependency_so_u_v_s_feature);

		double dependency_so_u_v_feature = getDependency_SO_U_V_Feature(statement.getWordNode(), w.currentSentense);
		hashMap.put("dependency_so_u_v_feature", dependency_so_u_v_feature);

		Object[] sharing_q_a_word_feature = getIsSharingQAWordNodeFeature(contentPreprocessPassSWLemma(answer),
				contentPreprocessPassSWLemma(question), story);
		double is_sharing_q_a_word_feature = (double) sharing_q_a_word_feature[0];
		hashMap.put("is_sharing_q_a_word_feature", is_sharing_q_a_word_feature);

		double inner_sentense_distance_feature = -1;
		if (is_sharing_q_a_word_feature == 1) {
			inner_sentense_distance_feature = getInnerSentenseDistanceFeature(
					(ArrayList<String>) sharing_q_a_word_feature[1], contentPreprocessPassSWLemma(answer),
					contentPreprocessPassSWLemma(question));
		}
		hashMap.put("inner_sentense_distance_feature", inner_sentense_distance_feature);

		return hashMap;
	}

	/***
	 * 
	 * @param node1
	 * @param node2
	 * @param answer
	 *            or hypothesis
	 * @param story
	 * @return the real value of feature
	 */
	public double getLeftDoubleWordOverlapFeature(WordNode z1, WordNode z2, String str, String[] story) {
		int index1 = z1.getCurrentSentense().getIndex();
		String sentense = contentPreprocessPassSW(z1.getCurrentSentense().getContent());
		if (index1 > 0) {
			sentense += " " + contentPreprocessPassSW(story[index1 - 1]);
		}
		int index2 = z2.getCurrentSentense().getIndex();
		String sentense2 = contentPreprocessPassSW(z2.getCurrentSentense().getContent());
		if (index2 > 0) {
			sentense2 += " " + contentPreprocessPassSW(story[index2 - 1]);
		}
		double score = getWordOverlapFeature(sentense + " " + sentense2, contentPreprocessPassSW(str));
		return score;
	}

	public double getLeftDoubleWordOverlapFeatureLemma(WordNode z1, WordNode z2, String str, String[] story) {
		int index1 = z1.getCurrentSentense().getIndex();
		String sentense = contentPreprocessPassSWLemma(z1.getCurrentSentense().getContent());
		if (index1 > 0) {
			sentense += " " + contentPreprocessPassSWLemma(story[index1 - 1]);
		}
		int index2 = z2.getCurrentSentense().getIndex();
		String sentense2 = contentPreprocessPassSWLemma(z2.getCurrentSentense().getContent());
		if (index2 > 0) {
			sentense2 += " " + contentPreprocessPassSWLemma(story[index2 - 1]);
		}
		double score = getWordOverlapFeature(sentense + " " + sentense2, contentPreprocessPassSWLemma(str));
		return score;
	}

	public double getRightDoubleWordOverlapFeature(WordNode z1, WordNode z2, String str, String[] story) {
		int index1 = z1.getCurrentSentense().getIndex();
		String sentense = contentPreprocessPassSW(z1.getCurrentSentense().getContent());
		if (index1 < story.length - 1) {
			sentense += " " + contentPreprocessPassSW(story[index1 + 1]);
		}
		int index2 = z2.getCurrentSentense().getIndex();
		String sentense2 = contentPreprocessPassSW(z2.getCurrentSentense().getContent());
		if (index2 < story.length - 1) {
			sentense2 += " " + contentPreprocessPassSW(story[index2 + 1]);
		}
		double score = getWordOverlapFeature(sentense + " " + sentense2, contentPreprocessPassSW(str));
		return score;
	}

	public double getRightDoubleWordOverlapFeatureLemma(WordNode z1, WordNode z2, String str, String[] story) {
		int index1 = z1.getCurrentSentense().getIndex();
		String sentense = contentPreprocessPassSWLemma(z1.getCurrentSentense().getContent());
		if (index1 < story.length - 1) {
			sentense += " " + contentPreprocessPassSWLemma(story[index1 + 1]);
		}
		int index2 = z2.getCurrentSentense().getIndex();
		String sentense2 = contentPreprocessPassSWLemma(z2.getCurrentSentense().getContent());
		if (index2 < story.length - 1) {
			sentense2 += " " + contentPreprocessPassSWLemma(story[index2 + 1]);
		}
		double score = getWordOverlapFeature(sentense + " " + sentense2, contentPreprocessPassSWLemma(str));
		return score;
	}

	public double getRightLeftDoubleWordOverlapFeature(WordNode z1, WordNode z2, String str, String[] story) {
		int index1 = z1.getCurrentSentense().getIndex();
		String sentense = contentPreprocessPassSW(z1.getCurrentSentense().getContent());
		if (index1 > 0) {
			sentense += " " + contentPreprocessPassSW(story[index1 - 1]);
		}
		if (index1 < story.length - 1) {
			sentense += " " + contentPreprocessPassSW(story[index1 + 1]);
		}
		int index2 = z2.getCurrentSentense().getIndex();
		String sentense2 = contentPreprocessPassSW(z2.getCurrentSentense().getContent());
		if (index2 > 0) {
			sentense2 += " " + contentPreprocessPassSW(story[index2 - 1]);
		}
		if (index2 < story.length - 1) {
			sentense2 += " " + contentPreprocessPassSW(story[index2 + 1]);
		}
		double score = getWordOverlapFeature(sentense + " " + sentense2, contentPreprocessPassSW(str));
		return score;
	}

	public double getRightLeftDoubleWordOverlapFeatureLemma(WordNode z1, WordNode z2, String str, String[] story) {
		int index1 = z1.getCurrentSentense().getIndex();
		String sentense = contentPreprocessPassSWLemma(z1.getCurrentSentense().getContent());
		if (index1 > 0) {
			sentense += " " + contentPreprocessPassSWLemma(story[index1 - 1]);
		}
		if (index1 < story.length - 1) {
			sentense += " " + contentPreprocessPassSWLemma(story[index1 + 1]);
		}
		int index2 = z2.getCurrentSentense().getIndex();
		String sentense2 = contentPreprocessPassSWLemma(z2.getCurrentSentense().getContent());
		if (index2 > 0) {
			sentense2 += " " + contentPreprocessPassSWLemma(story[index2 - 1]);
		}
		if (index2 < story.length - 1) {
			sentense2 += " " + contentPreprocessPassSWLemma(story[index2 + 1]);
		}
		double score = getWordOverlapFeature(sentense + " " + sentense2, contentPreprocessPassSWLemma(str));
		return score;
	}

	public double getLeftSingleWordOverlapFeature(WordNode z1, String str, String[] story) {
		int index1 = z1.getCurrentSentense().getIndex();
		String sentense = contentPreprocessPassSW(z1.getCurrentSentense().getContent());
		if (index1 > 0) {
			sentense += " " + contentPreprocessPassSW(story[index1 - 1]);
		}
		double score = getWordOverlapFeature(sentense, contentPreprocessPassSW(str));
		return score;
	}

	public double getLeftSingleWordOverlapFeatureLemma(WordNode z1, String str, String[] story) {
		int index1 = z1.getCurrentSentense().getIndex();
		String sentense = contentPreprocessPassSWLemma(z1.getCurrentSentense().getContent());
		if (index1 > 0) {
			sentense += " " + contentPreprocessPassSWLemma(story[index1 - 1]);
		}
		double score = getWordOverlapFeature(sentense, contentPreprocessPassSWLemma(str));
		return score;
	}

	public double getRightSingleWordOverlapFeature(WordNode z1, String str, String[] story) {
		int index1 = z1.getCurrentSentense().getIndex();
		String sentense = contentPreprocessPassSW(z1.getCurrentSentense().getContent());
		if (index1 < story.length - 1) {
			sentense += " " + contentPreprocessPassSW(story[index1 + 1]);
		}

		double score = getWordOverlapFeature(sentense, contentPreprocessPassSW(str));
		return score;
	}

	public double getRightSingleWordOverlapFeatureLemma(WordNode z1, String str, String[] story) {
		int index1 = z1.getCurrentSentense().getIndex();
		String sentense = contentPreprocessPassSWLemma(z1.getCurrentSentense().getContent());
		if (index1 < story.length - 1) {
			sentense += " " + contentPreprocessPassSWLemma(story[index1 + 1]);
		}

		double score = getWordOverlapFeature(sentense, contentPreprocessPassSWLemma(str));
		return score;
	}

	public double getRightLeftSingleWordOverlapFeature(WordNode z1, String str, String[] story) {
		int index1 = z1.getCurrentSentense().getIndex();
		String sentense = contentPreprocessPassSW(z1.getCurrentSentense().getContent());
		if (index1 > 0) {
			sentense += " " + contentPreprocessPassSW(story[index1 - 1]);
		}
		if (index1 < story.length - 1) {
			sentense += " " + contentPreprocessPassSW(story[index1 + 1]);
		}

		double score = getWordOverlapFeature(sentense, contentPreprocess(str));
		return score;
	}

	public double getRightLeftSingleWordOverlapFeatureLemma(WordNode z1, String str, String[] story) {
		int index1 = z1.getCurrentSentense().getIndex();
		String sentense = contentPreprocessPassSWLemma(z1.getCurrentSentense().getContent());
		if (index1 > 0) {
			sentense += " " + contentPreprocessPassSWLemma(story[index1 - 1]);
		}
		if (index1 < story.length - 1) {
			sentense += " " + contentPreprocessPassSWLemma(story[index1 + 1]);
		}
		double score = getWordOverlapFeature(sentense, contentPreprocessPassSWLemma(str));
		return score;
	}

	/***
	 * @param sentense1
	 * @param sentense2
	 * @param length
	 *            of story
	 * @return
	 */
	public double getSentenseDistanceFeature(int s1, int s2, int length) {
		double dis = 0;
		dis = Math.abs((double) (s1 - s2) / (double) length);
		return -dis;
	}

	public double getSameSentenseDistanceFeature(int z1_index, int z2_index, String sentense) {
		double distance = 1;
		String[] sentense_word = sentense.split(" ");
		distance = Math.abs((z1_index - z2_index) + 0.1 / sentense_word.length - 1 + 0.1);
		return distance;
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

	public double getW2VFeature(String str1, String str2) {
		double score = w2v.getSentenseSimiliarity(str1, str2)[0];
		return score;
	}

	public double getLeftW2VFeature(WordNode z1, String str, String[] story) {
		int index1 = z1.getCurrentSentense().getIndex();
		String sentense = contentPreprocess(z1.getCurrentSentense().getContent());
		if (index1 > 0) {
			sentense += " " + contentPreprocess(story[index1 - 1]);
		}
		double score = w2v.getSentenseSimiliarity(str, sentense)[0];
		return score;
	}

	public double getLeftW2VFeatureLemma(WordNode z1, String str, String[] story) {
		int index1 = z1.getCurrentSentense().getIndex();
		String sentense = contentPreprocessLemma(z1.getCurrentSentense().getContent());
		if (index1 > 0) {
			sentense += " " + contentPreprocessLemma(story[index1 - 1]);
		}
		double score = w2v.getSentenseSimiliarity(str, sentense)[0];
		return score;
	}

	public double getRightW2VFeature(WordNode z1, String str, String[] story) {
		int index1 = z1.getCurrentSentense().getIndex();
		String sentense = contentPreprocess(z1.getCurrentSentense().getContent());
		if (index1 < story.length - 1) {
			sentense += " " + contentPreprocess(story[index1 + 1]);
		}

		double score = w2v.getSentenseSimiliarity(str, sentense)[0];
		return score;
	}

	public double getRightW2VFeatureLemma(WordNode z1, String str, String[] story) {
		int index1 = z1.getCurrentSentense().getIndex();
		String sentense = contentPreprocessLemma(z1.getCurrentSentense().getContent());
		if (index1 < story.length - 1) {
			sentense += " " + contentPreprocessLemma(story[index1 + 1]);
		}

		double score = w2v.getSentenseSimiliarity(str, sentense)[0];
		return score;
	}

	public double getDependency_U_V_S_match(WordNode[] statement_words, WordNode[] sentense_words, boolean flag) {
		double score = 0;
		if (sentense_words != null) {
			for (int m = 0; m < sentense_words.length; m++) {
				for (int n = 0; n < statement_words.length; n++) {
					String sentense_word = contentPreprocessLemma(sentense_words[m].content);
					String statement_word = contentPreprocessLemma(statement_words[n].content);
					if (sentense_word.equals(statement_word)) {
						WordNode sentense_word_p = sentense_words[m].parent;
						WordNode statement_word_p = statement_words[n].parent;
						if (sentense_word_p != null && statement_word_p != null
								&& (contentPreprocessLemma(sentense_word_p.content)
										.equals(contentPreprocessLemma(sentense_word_p.content)))) {
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
		} else {
			return score;
		}
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
							&& (contentPreprocessLemma(sentense_word_p.content)
									.equals(contentPreprocessLemma(statement_word_p.content)))) {
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
								String sen_bro_string = contentPreprocessLemma(sentense_bro_list.get(i).getContent());
								String state_bro_string = contentPreprocessLemma(
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
								String sen_bro_string = contentPreprocessLemma(sentense_bro_list.get(i).getContent());
								String state_bro_string = contentPreprocessLemma(
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

	public int getIsSharingQAWordEdgeFeature(String answer, String question, String sentense) {
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

	public Object[] getIsSharingQAWordNodeFeature(String answer, String question, String[] story) {
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
				String qa_target_text = contentPreprocessLemma(qa_frame_list.get(i).getTarget().getText());
				for (int j = 0; j < s_frame_list.size(); j++) {
					String s_target_text = contentPreprocessLemma(s_frame_list.get(j).getTarget().getText());
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
				String qa_target_text = contentPreprocessLemma(qa_frame_list.get(i).getTarget().getText());
				for (int j = 0; j < s_frame_list.size(); j++) {
					String s_target_name = s_frame_list.get(j).getTarget().getName();
					String s_target_text = contentPreprocessLemma(s_frame_list.get(j).getTarget().getText());
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
						String qa_element_text = contentPreprocessLemma(qa_element_list.get(m).getText());
						for (int n = 0; n < s_element_list.size(); n++) {
							String s_element_text = contentPreprocessLemma(s_element_list.get(n).getText());
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
						String qa_element_text = contentPreprocessLemma(qa_element_list.get(m).getText());
						String qa_element_name = qa_element_list.get(m).getName();
						for (int n = 0; n < s_element_list.size(); n++) {
							String s_element_text = contentPreprocessLemma(s_element_list.get(n).getText());
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
							String qa_element_text = contentPreprocessLemma(qa_element_list.get(m).getText());
							String qa_element_name = qa_element_list.get(m).getName();
							for (int n = 0; n < s_element_list.size(); n++) {
								String s_element_text = contentPreprocessLemma(s_element_list.get(n).getText());
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
				String q_w = contentPreprocessLemma(que.getWordNode()[j2].content);
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

	public double getIsSameRelationWithQueFeature(Sentence s1, Sentence s2, WordNode z1, WordNode z2, Question que) {
		double score = 0;
		if (s1 == s2) {
			WordNode q1 = null;
			WordNode q2 = null;
			z1.tools = tools;
			z2.tools = tools;
			for (int i = 0; i < s1.getWordNode().length; i++) {
				WordNode w = s1.getWordNode()[i];
				if (contentPreprocessLemma(w.getContent()).trim() != "") {
					if (contentPreprocessLemma(z1.getContent()).equals(contentPreprocessLemma(w.getContent()))) {
						q1 = w;
						q1.tools = tools;
					}
					if (contentPreprocessLemma(z2.getContent()).equals(contentPreprocessLemma(w.getContent()))) {
						q2 = w;
						q2.tools = tools;
					}
				}
			}

			if (q1 != null && q2 != null) {
				if (z1.isChildLemma(z2) && q1.isChildLemma(q2)) {
					score = 1;
				} else if (z2.isChildLemma(z1) && q2.isChildLemma(q1)) {
					score = 1;
				} else if (z1.isParentLemma(z2) && q1.isParentLemma(q2)) {
					score = 1;
				} else if (z2.isParentLemma(z1) && q2.isParentLemma(q1)) {
					score = 1;
				} else if (z1.isSiblingLemma(z2) && q1.isSiblingLemma(q2)) {
					score = 1;
				} else if (z2.isSiblingLemma(z1) && q2.isSiblingLemma(q1)) {
					score = 1;
				}
			}
		}
		return score;
	}

	public double getIsSameRelationWithAnsFeature(Sentence s1, Sentence s2, WordNode z1, WordNode z2, Ans ans) {
		double score = 0;
		if (s1 == s2) {
			WordNode a1 = null;
			WordNode a2 = null;
			z1.tools = tools;
			z2.tools = tools;
			for (int i = 0; i < s1.getWordNode().length; i++) {
				WordNode w = s1.getWordNode()[i];
				if (contentPreprocessLemma(w.getContent()).trim() != "") {
					if (contentPreprocessLemma(z1.getContent()).equals(contentPreprocessLemma(w.getContent()))) {
						a1 = w;
						a1.tools = tools;
					}
					if (contentPreprocessLemma(z2.getContent()).equals(contentPreprocessLemma(w.getContent()))) {
						a2 = w;
						a2.tools = tools;
					}
				}
			}

			if (a1 != null && a2 != null) {
				if (z1.isChildLemma(z2) && a1.isChildLemma(a2)) {
					score = 1;
				} else if (z2.isChildLemma(z1) && a2.isChildLemma(a1)) {
					score = 1;
				} else if (z1.isParentLemma(z2) && a1.isParentLemma(a2)) {
					score = 1;
				} else if (z2.isParentLemma(z1) && a2.isParentLemma(a1)) {
					score = 1;
				} else if (z1.isSiblingLemma(z2) && a1.isSiblingLemma(a2)) {
					score = 1;
				} else if (z2.isSiblingLemma(z1) && a2.isSiblingLemma(a1)) {
					score = 1;
				}
			}
		}
		return score;
	}

}
