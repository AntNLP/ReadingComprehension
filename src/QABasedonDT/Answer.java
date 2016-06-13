package QABasedonDT;

public class Answer {
	String content;
	double[] edge_feature;
	double[] node_feature;
	double[] global_feature;
	double score;
	double attribute;
	
	public Answer(String content,double [] edge_feature,double[] node_feature,double[] global_feature,double attribute,double score){
		this.content = content;
		this.edge_feature = edge_feature;
		this.node_feature = node_feature;
		this.global_feature = global_feature;
		this.attribute = attribute;
		this.score = score;
	}
}
