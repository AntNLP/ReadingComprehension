package QABasedonDT;

public class Model {
	/*************************
	 * 属性
	 */
	double[] v1;
	double[] v2;
	double[] v3;
	double b = 1;
	double a = 0.1;
	
	/*********************************
	 * 方法
	 * @param path
	 * @param count
	 */
	public Model(){
		v1 = new double[18];
		for(int i = 0;i < v1.length;i++){
			v1[i] = 1;
		}
		v2 = new double[7];
		for(int i = 0;i < v2.length;i++){
			v2[i] = 1;
		}
		v3 = new double[8];
		for(int i = 0;i < v3.length;i++){
			v3[i] = 1;
		}
	}
	
	public double getEdgeScore(double[] edge_feature){
		double score = 0;
		for(int i = 0;i < edge_feature.length;i++){
			score += v1[i]*edge_feature[i];
		}
		return score;
	}
	
	public double getNodeScore(double[] node_feature){
		double score = 0;
		for(int i = 0;i < node_feature.length;i++){
			score += v2[i]*node_feature[i];
		}
		return score;
	}
	
	public double getAnswerScore(double[] answer_feature){
		double score = 0;
		for(int i = 0;i < answer_feature.length;i++){
			score += v3[i]*answer_feature[i];
		}
		return score;
	}
	
	public double getResult(double score1,double score2,double tag){
		double result = tag*(score1 + score2 + b);
		return result;
	}
	
	
	public void change(double[] edge_feature,double[] node_feature,double[] answer_feature,double tag){
		for(int i = 0;i < edge_feature.length;i++){
			v1[i] = v1[i]+a*tag*edge_feature[i];
		}
		for(int i = 0;i < node_feature.length;i++){
			v2[i] = v2[i]+a*tag*node_feature[i];
		}
		for(int i = 0;i < answer_feature.length;i++){
			v3[i] = v3[i]+a*tag*answer_feature[i];
		}
		b = b + a*tag;
	}
	
	
}
