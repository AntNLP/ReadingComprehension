package QABasedonDT;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import FeatureExtraction.FeatureExtractSenPair;

public class PairInferenceModule {
	/***
	 * @author chenruili
	 */
	Story story;
	Question question;
	Ans answer;
	Statement statement;
	int storyID;
	int questionID;
	int answerID;
	boolean isSerializable;
	FeatureExtractSenPair fPair;
	String filetype;
	String fileSet;
	Model model;
	int featureNum;
	
	public PairInferenceModule(FeatureExtractSenPair FeatureExtractSenPair) {
		this.fPair = FeatureExtractSenPair;
	}
	
	public void setFeatureNum(int num){
		this.featureNum = num;
	}

	public void setInfo(Story story, Question question, Ans answer, Statement statement, int storyID, int questionID,
			int answerID, String filetype, String fileSet, Model model, boolean isSerializeable) {
		this.story = story;
		this.question = question;
		this.answer = answer;
		this.statement = statement;
		this.storyID = storyID;
		this.questionID = questionID;
		this.answerID = answerID;
		this.filetype = filetype;
		this.fileSet = fileSet;
		this.model = model;
		this.isSerializable = isSerializeable;
	}

	public Object[] inference() throws Exception {
		Sentence[] sentenses = story.sentenses;
		double maxScore = -100;
		ArrayList<Double> pair_feature = null;
		if (isSerializable) {
			HashMap<String, HashMap<String, Double>> answer_map = new HashMap<>();
			for (int i = 0; i < sentenses.length - 1; i++) {
				for (int j = i + 1; j < sentenses.length && j - i < 4; j++) {
					String key = i + "_" + j;
					HashMap<String, Double> feature_map = fPair.getAllFeature(sentenses[i], sentenses[j], statement,
							question, answer);
					answer_map.put(key, feature_map);
				}
			}
			featureSerializable(answer_map);
		} else {
			HashMap<String, HashMap<String, Double>> answer_map = readSerializableFeature();
			for (int i = 0; i < sentenses.length - 1; i++) {
				for (int j = i + 1; j < sentenses.length && j - i < 4; j++) {
					ArrayList<Double> feature_list = readPairFeature(answer_map,i, j);
					double score = model.getPairScore(feature_list);
					if (score > maxScore) {
						maxScore = score;
						pair_feature = feature_list;
					}
				}
			}
		}
		Object[] objects = { maxScore, pair_feature };
		return objects;
	}

	public void featureSerializable(HashMap<String, HashMap<String, Double>> pair_feature) throws Exception {
		ObjectOutputStream oos = new ObjectOutputStream(
				new FileOutputStream("../src/res/serializedata/"+fileSet+"." + filetype
						+ "/pair_feature_"+featureNum+"/" + storyID + "_" + questionID + "_" + answerID + ".out"));
		oos.writeObject(pair_feature);
		oos.close();
	}

	public HashMap<String, HashMap<String, Double>> readSerializableFeature() throws Exception {
		ObjectInputStream ois = new ObjectInputStream(
				new FileInputStream("../src/res/serializedata/"+fileSet+"." + filetype
						+ "/pair_feature_"+featureNum+"/" + storyID + "_" + questionID + "_" + answerID + ".out"));
		HashMap<String, HashMap<String, Double>> feature_map = (HashMap<String, HashMap<String, Double>>) ois
				.readObject();
		ois.close();
		return feature_map;
	}

	public ArrayList<Double> readPairFeature(HashMap<String, HashMap<String, Double>> answer_map,int i, int j) throws Exception {
		ArrayList<Double> feature_list = new ArrayList<>();
		HashMap<String, Double> feature_map = answer_map.get(i+"_"+j);
		ArrayList<String> feature_items = ConfigureProcessor.pairFeatureList;
		for (String item : feature_items) {
			if (feature_map.containsKey(item)) {
				double feature_value = feature_map.get(item);
				feature_list.add(feature_value);
			}
		}
		return feature_list;
	}
}
