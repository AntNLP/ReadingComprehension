package QABasedonDT;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import FeatureExtraction.FeatureExtractDT;

public class MessagePassing {
	/***
	 * @author chenruili
	 */

	WordNode root;
	WordNode[] wordNodes;
	Statement statement;
	Question question;
	Ans answer;
	String[] story;
	Model model;
	FeatureExtractDT fx;
	int storyID;
	int questionID;
	int answerID;
	boolean isSerializable;
	String filetype;
	String fileSet;
	int featureNum;
	String type;

	public MessagePassing(FeatureExtractDT fx) {
		this.fx = fx;
		if (fx.getType().equals("question")) {
			this.type = "question";
		} else {
			this.type = "answer";
		}
	}

	public void setFeatureNum(int num) {
		this.featureNum = num;
	}

	public void setInfo(WordNode[] wordNodes, Model model, Question question, Ans answer, String[] story,
			Statement statement, int storyID, int questionID, int answerID, String fileType, String fileSet,
			boolean isSerializable) {
		this.wordNodes = wordNodes;
		this.model = model;
		this.question = question;
		this.answer = answer;
		this.story = story;
		this.statement = statement;
		for (int i = 0; i < wordNodes.length; i++) {
			wordNodes[i].nodeScore = new double[wordNodes[i].supportSentense.size()];
			for (int j = 0; j < wordNodes[i].nodeScore.length; j++) {
				wordNodes[i].nodeScore[j] = 0;
			}
			if (type.equals("question")) {
				wordNodes[i].edgeFeature = new double[wordNodes[i].supportSentense.size()][Model.queEdgeFeatureNum];
				wordNodes[i].nodeFeature = new double[wordNodes[i].supportSentense.size()][Model.queNodeFeatureNum];
			} else {
				wordNodes[i].edgeFeature = new double[wordNodes[i].supportSentense.size()][Model.ansEdgeFeatureNum];
				wordNodes[i].nodeFeature = new double[wordNodes[i].supportSentense.size()][Model.ansNodeFeatureNum];
			}
		}
		for (int i = 0; i < wordNodes.length; i++) {
			wordNodes[i].recordIndex = new int[wordNodes[i].supportSentense.size()][wordNodes[i].childlist.size()];
		}
		this.storyID = storyID;
		this.questionID = questionID;
		this.answerID = answerID;
		this.filetype = fileType;
		this.fileSet = fileSet;
		this.isSerializable = isSerializable;
	}

	public WordNode findRoot() {
		WordNode root = null;
		for (WordNode word_Node : wordNodes) {
			if (word_Node.parent == null) {
				root = word_Node;
				break;
			}
		}
		return root;
	}

	public Object[] messagePassing() {
		root = findRoot();
		for (int i = 0; i < root.childlist.size(); i++) {
			collect(root, root.childlist.get(i), i);
		}
		try {
			rootCollect();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		double max = -100;
		int index = 0;
		for (int i = 0; i < root.nodeScore.length; i++) {
			if (root.nodeScore[i] > max) {
				max = root.nodeScore[i];
				index = i;
			}
		}
		getBackPath(root, index);
		double[] edge_feature = root.edgeFeature[index];
		double[] node_feature = root.nodeFeature[index];
		edge_feature = vectorNormalize(edge_feature);
		node_feature = vectorNormalize(node_feature);
		if (type.equals("answer")) {
			max = scoreNormalize(max);
		}
		Object[] ob = { max, edge_feature, node_feature };
		return ob;
	}

	public double scoreNormalize(double score) {
		double count = 0;
		for (int i = 0; i < wordNodes.length; i++) {
			if (!wordNodes[i].supportSentense.get(0).getContent().equals("*")) {
				count++;
			}
		}
		if (count != 0) {
			score = score / count;
		}
		return score;
	}

	public double[] vectorNormalize(double[] vector_feature) {
		double count = 0;
		for (int i = 0; i < wordNodes.length; i++) {
			if (!wordNodes[i].supportSentense.get(0).getContent().equals("*")) {
				count++;
			}
		}
		if (count != 0) {
			for (int i = 0; i < vector_feature.length; i++) {
				vector_feature[i] = vector_feature[i] / count;
			}
		}
		return vector_feature;
	}

	public void rootCollect() throws Exception {
		for (int i = 0; i < root.supportSentense.size(); i++) {
			root.currentSentense = root.supportSentense.get(i);
			if (isSerializable) {
				HashMap<String, Double> node_feature = fx.getAllNodeFeature(root, question, answer, story, statement);
				featureSerializable(null, node_feature, root, root, -1, i);
			} else {
				DTFeautreSerialization ds = readFeatureSerializable(root, root, -1, i);
				double[] node_feature_vector = getFeatureVector((HashMap<String, Double>) ds.nodeFeature, "N");
				double score_z = 0;
				if (type.equals("question")) {
					score_z = model.getNodeScore(node_feature_vector);
				} else {
					score_z = model.getNode2Score(node_feature_vector);
				}
				root.nodeScore[i] += score_z;
				root.addNodeFeautre(i, root.nodeFeature[i], node_feature_vector);

			}
		}
	}

	public void getBackPath(WordNode word_Node, int index) {
		try {
			word_Node.currentSentense = word_Node.supportSentense.get(index);
			// System.out.println(index);
		} catch (Exception e) {
			// TODO: handle exception
		}
		for (int i = 0; i < word_Node.childlist.size(); i++) {
			getBackPath(word_Node.childlist.get(i), word_Node.recordIndex[index][i]);
		}
	}

	public void collect(WordNode parent_node, WordNode child_node, int index) {
		for (int i = 0; i < child_node.childlist.size(); i++) {
			collect(child_node, child_node.childlist.get(i), i);
		}
		if (!child_node.supportSentense.get(0).content.equals("*") || child_node.childlist.size() != 0) {
			try {
				sendMaxMessage(child_node, parent_node, index);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// sendSumMessage(child_node, parent_node, index);
		}
	}

	public void sendMaxMessage(WordNode child_node, WordNode parent_node, int index) throws Exception {
		for (int i = 0; i < parent_node.supportSentense.size(); i++) {
			double max = 0;
			int max_child = 0;
			double[] edge_feature = null;
			double[] node_feature = null;
			parent_node.currentSentense = parent_node.supportSentense.get(i);
			for (int j = 0; j < child_node.supportSentense.size(); j++) {
				child_node.currentSentense = child_node.supportSentense.get(j);

				if (isSerializable) {
					Object[] objects = getAllFeatureScore(child_node, parent_node);
					featureSerializable((HashMap<String, Double>) objects[1], (HashMap<String, Double>) objects[2],
							child_node, parent_node, i, j);
				} else {
					DTFeautreSerialization ds = readFeatureSerializable(child_node, parent_node, i, j);
					double[] edge_feature_vector = getFeatureVector((HashMap<String, Double>) ds.edgeFeature, "E");
					double[] node_feature_vector = getFeatureVector((HashMap<String, Double>) ds.nodeFeature, "N");
					double score = 0;
					double limitLine = 5;
					if (type.equals("question")) {
						score = model.getNodeScore(node_feature_vector) + model.getEdgeScore(edge_feature_vector);
						limitLine = 5;
					} else {
						score = model.getNode2Score(node_feature_vector) + model.getEdge2Score(edge_feature_vector);
						limitLine = 10;
					}
					score += child_node.nodeScore[j];
					boolean limit = false;
					if ((Math.abs(parent_node.currentSentense.sentenceIndex
							- child_node.currentSentense.sentenceIndex) <= limitLine)
							|| parent_node.currentSentense.content.equals("*")
							|| child_node.currentSentense.content.equals("*")) {
						limit = true;
					}
					if (score >= max && limit) {
						max = score;
						max_child = j;
						edge_feature = edge_feature_vector;
						node_feature = node_feature_vector;
					}

				}
			}
			if (!isSerializable) {
				parent_node.nodeScore[i] += max;
				parent_node.addEdgeFeautre(i, child_node.edgeFeature[max_child], edge_feature);
				parent_node.addNodeFeautre(i, child_node.nodeFeature[max_child], node_feature);
				parent_node.recordIndex[i][index] = max_child;
			}

		}
	}

	public DTFeautreSerialization readFeatureSerializable(WordNode child, WordNode parent, int i, int j)
			throws Exception {
		String filePath = "";
		if (type.equals("question")) {
			filePath = "../src/res/serializedata/" + fileSet + "." + filetype + "/dt_feature" + featureNum + "/" + storyID
					+ "_" + questionID + "_" + answerID + "_" + child.index + "_" + parent.index + "_" + i + "_" + j
					+ ".out";
		} else {
			filePath = "../src/res/serializedata/" + fileSet + "." + filetype + "/ans_dt_feature" + featureNum + "/"
					+ storyID + "_" + questionID + "_" + answerID + "_" + child.index + "_" + parent.index + "_" + i
					+ "_" + j + ".out";
		}
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath));
		DTFeautreSerialization ds = (DTFeautreSerialization) ois.readObject();
		ois.close();
		return ds;
	}

	public void featureSerializable(HashMap<String, Double> edge_feature, HashMap<String, Double> node_feature,
			WordNode child, WordNode parent, int i, int j) throws Exception {
		String filePath = "";
		if (type.equals("question")) {
			filePath = "../src/res/serializedata/" + fileSet + "." + filetype + "/dt_feature" + featureNum + "/" + storyID
					+ "_" + questionID + "_" + answerID + "_" + child.index + "_" + parent.index + "_" + i + "_" + j
					+ ".out";
		} else {
			filePath = "../src/res/serializedata/" + fileSet + "." + filetype + "/ans_dt_feature" + featureNum + "/"
					+ storyID + "_" + questionID + "_" + answerID + "_" + child.index + "_" + parent.index + "_" + i
					+ "_" + j + ".out";
		}
		DTFeautreSerialization ds = new DTFeautreSerialization(edge_feature, node_feature);
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath));
		oos.writeObject(ds);
		oos.close();
	}

	public Object[] getAllFeatureScore(WordNode child_node, WordNode parent_node) {
		double score = 0;
		HashMap<String, Double> edge_feature = fx.getAllEdgeFeature(child_node, parent_node, question, answer, story,
				statement);
		HashMap<String, Double> node_feature = fx.getAllNodeFeature(child_node, question, answer, story, statement);
		Object[] objects = { score, edge_feature, node_feature };
		return objects;
	}

	public double[] getFeatureVector(HashMap<String, Double> hashMap, String flag) {
		ArrayList<Double> list = new ArrayList<>();
		if (flag.equals("E")) {
			ArrayList<String> edge_feature = type.equals("question") ? ConfigureProcessor.queEdgeFeatureList
					: ConfigureProcessor.ansEdgeFeatureList;
			for (String item : edge_feature) {
				if (hashMap.containsKey(item)) {
					double value = hashMap.get(item);
					list.add(value);
				}
			}
		} else {
			ArrayList<String> node_feature = type.equals("question") ? ConfigureProcessor.queNodeFeatureList
					: ConfigureProcessor.ansNodeFeatureList;
			for (String item : node_feature) {
				if (hashMap.containsKey(item)) {
					double value = hashMap.get(item);
					list.add(value);
				}
			}
		}
		double[] vec = new double[list.size()];
		for (int i = 0; i < vec.length; i++) {
			vec[i] = list.get(i);
		}
		return vec;
	}
}
