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
import QABasedonDT.Story;
import Tools.Tools;
import Tools.Word2Vec;

public class FeatureExtractFrame {
	/***
	 * @author chenruili
	 */
	static Tools tools = new Tools();
	Word2Vec w2v;

	public FeatureExtractFrame() {
		w2v = new Word2Vec(QABasedonDT.w2vTable);
	}

	/***
	 * 
	 * @param candidate supporting sentence list
	 * @param answer
	 * @param question
	 * @param story
	 * @param statement
	 * @return frame feature list
	 */
	public HashMap<String, Double> getAllFeature(ArrayList<Sentence> sentenceList, Ans ans, Question que, Story story,
			Statement statement) {
		HashMap<String, Double> featureMap = new HashMap<>();
		String question = que.getContent();
		String answer = ans.getContent();
		
		double h_sentense_similiar_feature_lemma = getSimiliarFeatureBasedOnSentense(sentenceList,
				contentPreprocessPassSWLemma(answer) + " " + contentPreprocessPassSWLemma(question));
		featureMap.put("h_sentense_similiar_feature_lemma", h_sentense_similiar_feature_lemma);

		double h_sentense_similiar_2gram_feature_lemma = getBiwordSimiliarFeatureBasedOnSentense(sentenceList,
				contentPreprocessPassSWLemma(answer) + " " + contentPreprocessPassSWLemma(question));
		featureMap.put("h_sentense_similiar_2gram_feature_lemma", h_sentense_similiar_2gram_feature_lemma);

		double h_sentense_similiar_3gram_feature_lemma = getTriwordSimiliarFeatureBasedOnSentense(sentenceList,
				contentPreprocessPassSWLemma(answer) + " " + contentPreprocessPassSWLemma(question));
		featureMap.put("h_sentense_similiar_3gram_feature_lemma", h_sentense_similiar_3gram_feature_lemma);

		double a_sentense_similiar_feature_lemma = getSimiliarFeatureBasedOnSentense(sentenceList,
				contentPreprocessPassSWLemma(answer));
		featureMap.put("a_sentense_similiar_feature_lemma", a_sentense_similiar_feature_lemma);

		double a_sentense_similiar_2gram_feature_lemma = getBiwordSimiliarFeatureBasedOnSentense(sentenceList,
				contentPreprocessPassSWLemma(answer));
		featureMap.put("a_sentense_similiar_2gram_feature_lemma", a_sentense_similiar_2gram_feature_lemma);

		double a_sentense_similiar_3gram_feature_lemma = getTriwordSimiliarFeatureBasedOnSentense(sentenceList,
				contentPreprocessPassSWLemma(answer));
		featureMap.put("a_sentense_similiar_3gram_feature_lemma", a_sentense_similiar_3gram_feature_lemma);

		double is_sharing_q_a_word_feature = getIsSharingQAWordFeature(contentPreprocessPassSWLemma(answer),
				contentPreprocessPassSWLemma(question), sentenceList);
		featureMap.put("is_sharing_q_a_word_feature", is_sharing_q_a_word_feature);

		double is_sharing_VB_q_a_word_feature = get_Is_Sharing_VB_Q_A_Word_Feature(
				contentPreprocessPassSWLemma(answer), que, sentenceList);
		featureMap.put("is_sharing_VB_q_a_word_feature", is_sharing_VB_q_a_word_feature);

		double frame_target_name_feature = getFrameTargetNameFeature(statement, sentenceList);
		featureMap.put("frame_target_name_feature", frame_target_name_feature);

		double frame_target_text_feature = getFrameTargetTextFeature(statement, sentenceList);
		featureMap.put("frame_target_text_feature", frame_target_text_feature);

		double frame_target_all_feature = getFrameTargetAllFeature(statement, sentenceList);
		featureMap.put("frame_target_all_feature", frame_target_all_feature);

		double frame_element_name_feature = getFrameElementNameFeature(statement, sentenceList);
		featureMap.put("frame_element_name_feature", frame_element_name_feature);

		double frame_element_text_feature = getFrameElementTextFeature(statement, sentenceList);
		featureMap.put("frame_element_text_feature", frame_element_text_feature);

		double frame_element_all_feature = getFrameElementAllFeature(statement, sentenceList);
		featureMap.put("frame_element_all_feature", frame_element_all_feature);

		double frame_element_target_name_feature = getFrameElementTargetNameFeature(statement, sentenceList);
		featureMap.put("frame_element_target_name_feature", frame_element_target_name_feature);

		double frame_element_target_text_feature = getFrameElementTargetTextFeature(statement, sentenceList);
		featureMap.put("frame_element_target_text_feature", frame_element_target_text_feature);

		double frame_element_target_all_feature = getFrameElementTargetAllFeature(statement, sentenceList);
		featureMap.put("frame_element_target_all_feature", frame_element_target_all_feature);

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

	public double getSimiliarFeatureBasedOnSentense(ArrayList<Sentence> sentenses, String statement) {
		double score = 0;
		for (int i = 0; i < sentenses.size(); i++) {
			score += getWordOverlapFeature(statement, contentPreprocessPassSWLemma(sentenses.get(i).getContent()));
		}
		if(sentenses.size() != 0){
			score = score / (double)(sentenses.size());
		}
		return score;
	}

	public double getBiwordSimiliarFeatureBasedOnSentense(ArrayList<Sentence> sentenses, String statement) {
		double score = 0;
		for (int i = 0; i < sentenses.size(); i++) {
			score += getBiWordOverlapFeature(statement,
					contentPreprocessPassSWLemma(sentenses.get(i).getContent()));
		}
		if(sentenses.size() != 0){
			score = score / (double)(sentenses.size());
		}
		return score;
	}

	public double getTriwordSimiliarFeatureBasedOnSentense(ArrayList<Sentence> sentenses, String statement) {
		double score = 0;
		for (int i = 0; i < sentenses.size(); i++) {
			score += getTriWordOverlapFeature(statement,
					contentPreprocessPassSWLemma(sentenses.get(i).getContent()));
		}
		if(sentenses.size() != 0){
			score = score / (double)(sentenses.size());
		}
		return score;
	}

	public double getIsSharingQAWordFeature(String answer, String question, ArrayList<Sentence> sentenses) {
		double score = 0;
		String[] answer_word = answer.split(" ");
		String[] question_word = question.split(" ");
		String sentense = "";
		for (int i = 0; i < sentenses.size(); i++) {
			sentense += sentenses.get(i) + " ";
		}
		sentense = contentPreprocessPassSWLemma(sentense);
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

	public double get_Is_Sharing_VB_Q_A_Word_Feature(String answer, Question que, ArrayList<Sentence> sentenses) {
		double score = 0;
		String[] answer_word = answer.split(" ");
		String sentense = "";
		for (int i = 0; i < sentenses.size(); i++) {
			sentense += sentenses.get(i) + " ";
		}
		sentense = contentPreprocessPassSWLemma(sentense);
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

	public double getFrameTargetNameFeature(Statement statement, ArrayList<Sentence> sentenses) {
		double score = 0;
		for (int i = 0; i < sentenses.size(); i++) {
			score += getTargetNameMatchNum(statement.frameList, sentenses.get(i).frameList);
		}
		if(sentenses.size() != 0){
			score = score / (double)(sentenses.size());
		}
		return score / 5.0;
	}

	public double getFrameTargetTextFeature(Statement statement, ArrayList<Sentence> sentenses) {
		double score = 0;
		for (int i = 0; i < sentenses.size(); i++) {
			score += getTargetTextMatchNum(statement.frameList, sentenses.get(i).frameList);
		}
		if(sentenses.size() != 0){
			score = score / (double)(sentenses.size());
		}
		return score / 5.0;
	}

	public double getFrameTargetAllFeature(Statement statement, ArrayList<Sentence> sentenses) {
		double score = 0;
		for (int i = 0; i < sentenses.size(); i++) {
			score += getTargetAllMatchNum(statement.frameList, sentenses.get(i).frameList);
		}
		if(sentenses.size() != 0){
			score = score / (double)(sentenses.size());
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

	public double getFrameElementNameFeature(Statement statement, ArrayList<Sentence> sentenses) {
		double score = 0;
		for (int i = 0; i < sentenses.size(); i++) {
			score += getElementNameNum(statement.frameList, sentenses.get(i).frameList);
		}
		if(sentenses.size() != 0){
			score = score / (double)(sentenses.size());
		}
		return score / 5.0;
	}

	public double getFrameElementTextFeature(Statement statement, ArrayList<Sentence> sentenses) {
		double score = 0;
		for (int i = 0; i < sentenses.size(); i++) {
			score += getElementTextNum(statement.frameList, sentenses.get(i).frameList);
		}
		if(sentenses.size() != 0){
			score = score / (double)(sentenses.size());
		}
		return score / 5.0;
	}

	public double getFrameElementAllFeature(Statement statement, ArrayList<Sentence> sentenses) {
		double score = 0;
		for (int i = 0; i < sentenses.size(); i++) {
			score += getElementAllNum(statement.frameList, sentenses.get(i).frameList);
		}
		if(sentenses.size() != 0){
			score = score / (double)(sentenses.size());
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

	public double getFrameElementTargetNameFeature(Statement statement, ArrayList<Sentence> sentenses) {
		double score = 0;
		for (int i = 0; i < sentenses.size(); i++) {
			score += getElementTargetNameMatchNum(statement.frameList, sentenses.get(i).frameList);
		}
		if(sentenses.size() != 0){
			score = score / (double)(sentenses.size());
		}
		return score / 5.0;
	}

	public double getFrameElementTargetTextFeature(Statement statement, ArrayList<Sentence> sentenses) {
		double score = 0;
		for (int i = 0; i < sentenses.size(); i++) {
			score += getElementTargetTextMatchNum(statement.frameList, sentenses.get(i).frameList);
		}
		if(sentenses.size() != 0){
			score = score / (double)(sentenses.size());
		}
		return score / 5.0;
	}

	public double getFrameElementTargetAllFeature(Statement statement, ArrayList<Sentence> sentenses) {
		double score = 0;
		for (int i = 0; i < sentenses.size(); i++) {
			score += getElementTargetAllMatchNum(statement.frameList, sentenses.get(i).frameList);
		}
		if(sentenses.size() != 0){
			score = score / (double)(sentenses.size());
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
}
