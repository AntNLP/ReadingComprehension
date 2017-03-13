package QABasedonDT;

import java.util.ArrayList;
import java.util.Random;

public class Model {
	/***
	 * @author chenruili
	 */
	double[] v1;//weight vector for question edge features
	double[] v2;//weight vector for question node features
	double[] v3;//weight vector for global features
	double[] v4;//weight vector for pair features
	double[] v5;//weight vector for single features
	double[] v6;//weight vector for answer edge features
	double[] v7;//weight vector for answer node features
	double[] v8;//weight vector for inter-frame features
	double[] v9;//weight vector for inner-frame features
	double a = 0.001;//the learning rate
	static int queEdgeFeatureNum = 33;
	static int queNodeFeatureNum = 37;
	static int globalFeatureNum = 40;
	static int pairFeatureNum = 12;
	static int singleFeatureNum = 12;
	static int ansEdgeFeatureNum = 33;
	static int ansNodeFeatureNum = 37;
	static int frameFeatureNum = 10;
	static int iframeFeatureNum = 10;

	public Model() {
		weightInitialization();
	}
	
	public void setLearningRate(double rate){
		this.a = rate;
	}

	public void weightInitialization() {
		v1 = new double[queEdgeFeatureNum];
		Random r = new Random();
		for (int i = 0; i < v1.length; i++) {
			v1[i] = r.nextDouble();

		}
		v2 = new double[queNodeFeatureNum];
		for (int i = 0; i < v2.length; i++) {
			v2[i] = r.nextDouble();

		}
		v3 = new double[globalFeatureNum];
		for (int i = 0; i < v3.length; i++) {
			v3[i] = r.nextDouble();
		}
		v4 = new double[pairFeatureNum];
		for (int i = 0; i < v4.length; i++) {
			v4[i] = r.nextDouble();
		}
		v5 = new double[singleFeatureNum];
		for (int i = 0; i < v5.length; i++) {
			v5[i] = r.nextDouble();
		}
		v6 = new double[ansEdgeFeatureNum];
		for (int i = 0; i < v6.length; i++) {
			v6[i] = r.nextDouble();
		}
		v7 = new double[ansNodeFeatureNum];
		for (int i = 0; i < v7.length; i++) {
			v7[i] = r.nextDouble();
		}
		v8 = new double[frameFeatureNum];
		for (int i = 0; i < v8.length; i++) {
			v8[i] = r.nextDouble();
		}
		v9 = new double[iframeFeatureNum];
		for (int i = 0; i < v9.length; i++) {
			v9[i] = r.nextDouble();
		}

	}

	public Model(double[] v1, double[] v2, double[] v3, double[] v4, double[] v5, double[] v6, double[] v7, double[] v8,
			double[] v9) {
		this.v1 = v1;
		this.v2 = v2;
		this.v3 = v3;
		this.v4 = v4;
		this.v5 = v5;
		this.v6 = v6;
		this.v7 = v7;
		this.v8 = v8;
		this.v9 = v9;
	}

	public double getEdgeScore(double[] edge_feature) {
		double score = 0;
		for (int i = 0; i < edge_feature.length; i++) {
			score += v1[i] * edge_feature[i];
		}
		return score;
	}

	public double getNodeScore(double[] node_feature) {
		double score = 0;
		for (int i = 0; i < node_feature.length; i++) {
			score += v2[i] * node_feature[i];
		}
		return score;
	}

	public double getEdge2Score(double[] edge_feature) {
		double score = 0;
		for (int i = 0; i < edge_feature.length; i++) {
			score += v6[i] * edge_feature[i];
		}
		return score;
	}

	public double getNode2Score(double[] node_feature) {
		double score = 0;
		for (int i = 0; i < node_feature.length; i++) {
			score += v7[i] * node_feature[i];
		}
		return score;
	}

	public double getGlobalScore(ArrayList<Double> global_feature) {
		double score = 0;
		for (int i = 0; i < global_feature.size(); i++) {
			score += v3[i] * global_feature.get(i);
		}
		return score;
	}

	public double getPairScore(ArrayList<Double> pair_feature) {
		double score = 0;
		for (int i = 0; i < pair_feature.size(); i++) {
			score += v4[i] * pair_feature.get(i);
		}
		return score;
	}

	public double getSingleScore(ArrayList<Double> single_feature) {
		double score = 0;
		for (int i = 0; i < single_feature.size(); i++) {
			score += v5[i] * single_feature.get(i);
		}
		return score;
	}

	public double getFrameScore(ArrayList<Double> frame_feature) {
		double score = 0;
		for (int i = 0; i < frame_feature.size(); i++) {
			score += v8[i] * frame_feature.get(i);
		}
		return score;
	}

	public double getIFrameScore(ArrayList<Double> Iframe_feature) {
		double score = 0;
		for (int i = 0; i < Iframe_feature.size(); i++) {
			score += v9[i] * Iframe_feature.get(i);
		}
		return score;

	}

	public void change(Answer correct_answer, Answer pre_answer, int loop) {
		double size = a;

		for (int i = 0; i < correct_answer.queEdgeFeature.length; i++) {
			v1[i] = v1[i] + size * (correct_answer.queEdgeFeature[i] - pre_answer.queEdgeFeature[i]);
		}
		for (int i = 0; i < correct_answer.queNodeFeature.length; i++) {
			v2[i] = v2[i] + size * (correct_answer.queNodeFeature[i] - pre_answer.queNodeFeature[i]);
		}
		for (int i = 0; i < correct_answer.globalFeature.size(); i++) {
			v3[i] = v3[i] + size * (correct_answer.globalFeature.get(i) - pre_answer.globalFeature.get(i));
		}
		for (int i = 0; i < correct_answer.pairFeature.size(); i++) {
			v4[i] = v4[i] + size * (correct_answer.pairFeature.get(i) - pre_answer.pairFeature.get(i));
		}
		for (int i = 0; i < correct_answer.singleFeature.size(); i++) {
			v5[i] = v5[i] + size * (correct_answer.singleFeature.get(i) - pre_answer.singleFeature.get(i));
		}
		for (int i = 0; i < correct_answer.ansEdgeFeature.length; i++) {
			v6[i] = v6[i] + size * (correct_answer.ansEdgeFeature[i] - pre_answer.ansEdgeFeature[i]);
		}
		for (int i = 0; i < correct_answer.ansNodeFeature.length; i++) {
			v7[i] = v7[i] + size * (correct_answer.ansNodeFeature[i] - pre_answer.ansNodeFeature[i]);
		}
		for (int i = 0; i < correct_answer.frameFeature.size() && i < pre_answer.frameFeature.size(); i++) {
			v8[i] = v8[i] + size * (correct_answer.frameFeature.get(i) - pre_answer.frameFeature.get(i));
		}
		for (int i = 0; i < correct_answer.iframeFeature.size() && i < pre_answer.iframeFeature.size(); i++) {
			v9[i] = v9[i] + size * (correct_answer.iframeFeature.get(i) - pre_answer.iframeFeature.get(i));
		}
	}

	public static void setGlobalFeautreNum(int num) {
		globalFeatureNum = num;
	}

	public static void setQueEdgeFeatureNum(int num) {
		queEdgeFeatureNum = num;
	}

	public static void setQueNodeFeatureNum(int num) {
		queNodeFeatureNum = num;
	}

	public static void setAnsEdgeFeatureNum(int num) {
		ansEdgeFeatureNum = num;
	}

	public static void setAnsNodeFeatureNum(int num) {
		ansNodeFeatureNum = num;
	}

	public static void setPairFeatureNum(int num) {
		pairFeatureNum = num;
	}

	public static void setSingleFeatureNum(int num) {
		singleFeatureNum = num;
	}

	public static void setFrameFeatureNum(int num) {
		frameFeatureNum = num;
	}

	public static void setIFrameFeatureNum(int num) {
		iframeFeatureNum = num;
	}

}
