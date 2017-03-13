package QABasedonDT;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import FeatureExtraction.FeatureExtractFrame;

public class FrameModule {
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
	FeatureExtractFrame fx;
	String filetype;
	String fileSet;
	Model model;
	int id;
	double max;
	ArrayList<Double> featureList;
	int featureNum;

	public FrameModule(FeatureExtractFrame fx) {
		this.fx = fx;
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
		this.id = 0;
		this.max = 0;
		this.featureList = new ArrayList<>();
	}

	public Object[] inference() throws Exception {
		ArrayList<Frame> frameList = question.frameList;
		if (frameList != null) {
			int count = 0;
			collect(frameList, count);
			getBackPath(frameList);
		}
		Object[] object = { max, featureList };
		return object;
	}

	public void getBackPath(ArrayList<Frame> frameList) {
		for (int i = 0; i < frameList.size(); i++) {
			int index = frameList.get(i).bestIndex;
			ArrayList<Sentence> sentenses_list = frameList.get(i).supportSentense;
			if (sentenses_list != null) {
				frameList.get(i).currentSupportSentense = sentenses_list.get(index);
			}
		}
	}


	public void collect(ArrayList<Frame> frameList, int count) throws Exception {
		if (count < frameList.size()) {
			Frame frame = frameList.get(count);
			count++;
			for (int i = 0; i < frame.supportSentense.size(); i++) {
				Sentence sentense = frame.supportSentense.get(i);
				frame.currentSupportSentense = sentense;
				frame.index = i;
				collect(frameList, count);
				if (i == frame.supportSentense.size() - 1) {
					return;
				}
			}
		}
		if (count == frameList.size()) {
			ArrayList<Sentence> sentense_list = new ArrayList<>();
			for (int i = 0; i < frameList.size(); i++) {
				sentense_list.add(frameList.get(i).currentSupportSentense);
			}
			if (isLegal(sentense_list)) {
				if (isSerializable) {
					HashMap<String, Double> feature_map = fx.getAllFeature(sentense_list, answer, question, story,
							statement);
					featureSerializable(feature_map);
				} else {
					HashMap<String, Double> feature_map = readSerializableFeature();
					ArrayList<Double> feature_list = readFrameFeature(feature_map);
					double score = model.getFrameScore(feature_list);
					if (score > max) {
						this.max = score;
						this.featureList = feature_list;
						for (int i = 0; i < frameList.size(); i++) {
							frameList.get(i).bestIndex = frameList.get(i).index;
						}
					}
				}
				id++;
			}
		}
	}

	public boolean isLegal(ArrayList<Sentence> sentense_list) {
		boolean flag = true;
		int max_index = 0;
		int min_index = 1000;
		for (int i = 0; i < sentense_list.size(); i++) {
			int index = sentense_list.get(i).getIndex();
			String content = sentense_list.get(i).getContent();
			if (!content.equals("*")) {
				if (index > max_index) {
					max_index = index;
				}
				if (index < min_index) {
					min_index = index;
				}
			}
		}
		if (Math.abs(max_index - min_index) > 5) {
			flag = false;
		}
		return flag;
	}

	public void featureSerializable(HashMap<String, Double> frame_feature) throws Exception {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(
				"../src/res/serializedata/"+fileSet+"." + filetype
						+ "/frame_feature_"+featureNum+"/" + storyID + "_" + questionID + "_" + answerID + "_" + id + ".out"));
		oos.writeObject(frame_feature);
		oos.close();
	}

	public HashMap<String, Double> readSerializableFeature() throws Exception {
		ObjectInputStream ois = new ObjectInputStream(
				new FileInputStream("../src/res/serializedata/"+fileSet+"." + filetype
						+ "/frame_feature_"+featureNum+"/" + storyID + "_" + questionID + "_" + answerID + "_" + id + ".out"));
		HashMap<String, Double> feature_map = (HashMap<String, Double>) ois.readObject();
		ois.close();
		return feature_map;
	}

	public ArrayList<Double> readFrameFeature(HashMap<String, Double> feature_map) throws Exception {
		ArrayList<Double> feature_list = new ArrayList<>();
		ArrayList<String> feature_items = ConfigureProcessor.frameFeatureList;
		for (String item : feature_items) {
			if (feature_map.containsKey(item)) {
				double feature_value = feature_map.get(item);
				feature_list.add(feature_value);
			}
		}
		return feature_list;
	}
}
