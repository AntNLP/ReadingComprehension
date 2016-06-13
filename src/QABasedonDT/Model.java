package QABasedonDT;

import java.util.Random;

public class Model {
	/*************************
	 * 属性
	 */
	double[] v1;
	double[] v2;
	double[] v3;
	double a = 0.0005;
//	double b = 1;
	static int edge_feature_num = 32;
	static int node_feature_num = 16;
	static int global_feature_num = 5;

	/*********************************
	 * 方法
	 * 
	 * @param path
	 * @param count
	 */
	public Model() {
		v1 = new double[edge_feature_num];
		Random r = new Random();
		for (int i = 0; i < v1.length; i++) {
			v1[i] = r.nextDouble();

		}
		v2 = new double[node_feature_num];
		for (int i = 0; i < v2.length; i++) {
			v2[i] = r.nextDouble();

		}
		v3 = new double[global_feature_num];
		for (int i = 0; i < v3.length; i++) {
			v3[i] = r.nextDouble();

		}
	}

	public Model(double[] v1, double[] v2, double[] v3) {
		this.v1 = v1;
		this.v2 = v2;
		this.v3 = v3;
//		this.b = b;
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

	public double getGlobalScore(double[] global_feature) {
		double score = 0;
		for (int i = 0; i < global_feature.length; i++) {
			score += v3[i] * global_feature[i];
		}
		return score;
	}

	public void change(Answer correct_answer, Answer pre_answer, int loop) {
		double size = a;

		if (correct_answer.score == pre_answer.score) {
			for (int i = 0; i < correct_answer.edge_feature.length; i++) {
				v1[i] = v1[i] + size * (correct_answer.edge_feature[i] - 2 * pre_answer.edge_feature[i]);
			}
			for (int i = 0; i < correct_answer.node_feature.length; i++) {
				v2[i] = v2[i] + size * (correct_answer.node_feature[i] - 2 * pre_answer.node_feature[i]);
			}
			for (int i = 0; i < correct_answer.global_feature.length; i++) {
				v3[i] = v3[i] + size * (correct_answer.global_feature[i] - 2 * pre_answer.global_feature[i]);
			}
		} else {
			for (int i = 0; i < correct_answer.edge_feature.length; i++) {
				v1[i] = v1[i] + size * (correct_answer.edge_feature[i] - pre_answer.edge_feature[i]);
			}
			for (int i = 0; i < correct_answer.node_feature.length; i++) {
				v2[i] = v2[i] + size * (correct_answer.node_feature[i] - pre_answer.node_feature[i]);
			}
			for (int i = 0; i < correct_answer.global_feature.length; i++) {
				v3[i] = v3[i] + size * (correct_answer.global_feature[i] - pre_answer.global_feature[i]);
			}
//			b = b + size * (pre_answer.attribute);
		}
	}

	// public void change(double[] edge_feature,double[] node_feature,double[]
	// answer_feature,double tag){
	// for(int i = 0;i < edge_feature.length;i++){
	// v1[i] = v1[i]+a*tag*edge_feature[i];
	// }
	// for(int i = 0;i < node_feature.length;i++){
	// v2[i] = v2[i]+a*tag*node_feature[i];
	// }
	// for(int i = 0;i < answer_feature.length;i++){
	// v3[i] = v3[i]+a*tag*answer_feature[i];
	// }
	// b = b + a*tag;
	// }

}
