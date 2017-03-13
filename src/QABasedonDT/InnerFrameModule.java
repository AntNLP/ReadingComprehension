package QABasedonDT;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import FeatureExtraction.FeatureExtractFrame;

public class InnerFrameModule {
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
	double localMax;
	ArrayList<Double> featureList;
	ArrayList<Double> localFeatureList;
	int featureNum;

	public InnerFrameModule(FeatureExtractFrame fx) {
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
		this.localMax = 0;
		this.featureList = new ArrayList<>();
		this.localFeatureList = new ArrayList<>();
	}

	public Object[] inference() throws Exception {
		ArrayList<Frame> frameList = question.frameList;
		if (frameList != null) {
			for(int i = 0;i < frameList.size();i++){
				localMax = 0;
				localFeatureList = new ArrayList<>();
				ArrayList<InnerObjects> inner_list = frameList.get(i).innerList;
				int count = 0;
				collect(inner_list, count);
				getBackPath(inner_list);
				featureAdd(localFeatureList);
				max += localMax;
			}
		}
		if(frameList.size() > 0){
			featureNormalize(frameList.size());
			max = max / (double)(frameList.size());		
		}
		Object[] object = { max, featureList };
		return object;
	}

	public void featureNormalize(int num){
		for(int i = 0;i < featureList.size();i++){
			double r = featureList.get(i);
			r = r/(double)(num);
			featureList.set(i,r);			
		}
	}

	public void featureAdd(ArrayList<Double> local_list){
		if(this.featureList.size() == 0){
			this.featureList = local_list;
		}
		else{
			for(int i = 0;i < local_list.size();i++){
				double r = featureList.get(i) + local_list.get(i);
				featureList.set(i,r);
			}	
		}
	}

	public void getBackPath(ArrayList<InnerObjects> inner_list) {
		for (int i = 0; i < inner_list.size(); i++) {
			int index = inner_list.get(i).bestIndex;
			ArrayList<Sentence> sentenses_list = inner_list.get(i).supportSentense;
			if (sentenses_list != null) {
				inner_list.get(i).currentSupportSentense = sentenses_list.get(index);
			}
		}
	}


	public void collect(ArrayList<InnerObjects> inner_list, int count) throws Exception {
		if (count < inner_list.size()) {
			InnerObjects inner  = inner_list.get(count);
			count++;
			for (int i = 0; i < inner.supportSentense.size(); i++) {
				Sentence sentense = inner.supportSentense.get(i);
				inner.currentSupportSentense = sentense;
				inner.index = i;
				collect(inner_list, count);
				if (i == inner.supportSentense.size() - 1) {
					return;
				}
			}
		}
		
		if (count == inner_list.size()) {
			ArrayList<Sentence> sentense_list = new ArrayList<>();
			for (int i = 0; i < inner_list.size(); i++) {
				sentense_list.add(inner_list.get(i).currentSupportSentense);
			}
			if (isLegal(sentense_list)) {
				if (isSerializable) {
					HashMap<String, Double> feature_map = fx.getAllFeature(sentense_list, answer, question, story,
							statement);
					featureSerializable(feature_map);
				} else {
					HashMap<String, Double> feature_map = readSerializableFeature();
					ArrayList<Double> feature_list = readFrameFeature(feature_map);
					double score = model.getIFrameScore(feature_list);
					if (score > localMax) {
						this.localMax = score;
						this.localFeatureList = feature_list;
						for (int i = 0; i < inner_list.size(); i++) {
							inner_list.get(i).bestIndex = inner_list.get(i).index;
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
						+ "/innerframe_feature_"+featureNum+"/" + storyID + "_" + questionID + "_" + answerID + "_" + id + ".out"));
		oos.writeObject(frame_feature);
		oos.close();
	}

	public HashMap<String, Double> readSerializableFeature() throws Exception {
		ObjectInputStream ois = new ObjectInputStream(
				new FileInputStream("../src/res/serializedata/"+fileSet+"." + filetype
						+ "/innerframe_feature_"+featureNum+"/" + storyID + "_" + questionID + "_" + answerID + "_" + id + ".out"));
		HashMap<String, Double> feature_map = (HashMap<String, Double>) ois.readObject();
		ois.close();
		return feature_map;
	}

	public ArrayList<Double> readFrameFeature(HashMap<String, Double> feature_map) throws Exception {
		ArrayList<Double> feature_list = new ArrayList<>();
		ArrayList<String> feature_items = ConfigureProcessor.iframeFeatureList;
		for (String item : feature_items) {
			if (feature_map.containsKey(item)) {
				double feature_value = feature_map.get(item);
				feature_list.add(feature_value);
			}
		}
		return feature_list;
	}
}
