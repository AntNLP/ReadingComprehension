package QABasedonDT;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import FeatureExtraction.FeatureExtractSingle;

public class SingleInferenceModule {
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
	FeatureExtractSingle fSingle;
	String filetype;
	String fileSet;
	Model model;
	int featureNum;

	public SingleInferenceModule(FeatureExtractSingle f_Sing) {
		this.fSingle = f_Sing;
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
		ArrayList<Double> single_feature = null;
		if (isSerializable) {
			HashMap<String, HashMap<String, Double>> answer_map = new HashMap<>();
			for (int i = 0; i < sentenses.length; i++) {
					String key = Integer.toString(i);
					HashMap<String, Double> feature_map = fSingle.getAllFeature(sentenses[i], statement,
							question, answer);
					answer_map.put(key, feature_map);

			}
			featureSerializable(answer_map);
		} else {
			HashMap<String, HashMap<String, Double>> answer_map = readSerializableFeature();
			for (int i = 0; i < sentenses.length - 1; i++) {
					ArrayList<Double> feature_list = readSingleFeature(answer_map,i);
					double score = model.getSingleScore(feature_list);
					if (score > maxScore) {
						maxScore = score;
						single_feature = feature_list;
					}
			}
		}
		Object[] objects = { maxScore, single_feature };
		return objects;
	}

	public void featureSerializable(HashMap<String, HashMap<String, Double>> single_feature) throws Exception {
		ObjectOutputStream oos = new ObjectOutputStream(
			new FileOutputStream("../src/res/serializedata/"+fileSet+"." + filetype
						+ "/single_feature_"+featureNum+"/" + storyID + "_" + questionID + "_" + answerID + ".out"));
		oos.writeObject(single_feature);
		oos.close();
	}

	public HashMap<String, HashMap<String, Double>> readSerializableFeature() throws Exception {
		ObjectInputStream ois = new ObjectInputStream(
				new FileInputStream("../src/res/serializedata/"+fileSet+"." + filetype
						+ "/single_feature_"+featureNum+"/" + storyID + "_" + questionID + "_" + answerID + ".out"));
		HashMap<String, HashMap<String, Double>> feature_map = (HashMap<String, HashMap<String, Double>>) ois
				.readObject();
		ois.close();
		return feature_map;
	}

	public ArrayList<Double> readSingleFeature(HashMap<String, HashMap<String, Double>> answer_map,int i) throws Exception {
		ArrayList<Double> feature_list = new ArrayList<>();
		HashMap<String, Double> feature_map = answer_map.get(Integer.toString(i));
		ArrayList<String> feature_items = ConfigureProcessor.singleFeatureList;
		for (String item : feature_items) {
			if (feature_map.containsKey(item)) {
				double feature_value = feature_map.get(item);
				feature_list.add(feature_value);
			}
		}
		return feature_list;
	}
}
