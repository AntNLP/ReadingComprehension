package QABasedonDT;

import java.util.ArrayList;

public class Answer {
	/***
	 * @author chenruili
	 */
	
	String content;
	double[] queEdgeFeature;
	double[] queNodeFeature;
	double[] ansEdgeFeature;
	double[] ansNodeFeature;
	ArrayList<Double> globalFeature;
	ArrayList<Double> pairFeature;
	ArrayList<Double> singleFeature;
	ArrayList<Double> frameFeature;
	ArrayList<Double> iframeFeature;
	double score;
	double attribute;
	
	public Answer(String content,double [] queEdgeFeature,double[] queNodeFeature,
			double[] ansEdgeFeature, double[] ansNodeFeature,ArrayList<Double> globalFeature,
			ArrayList<Double> pairFeature,ArrayList<Double> singleFeature, ArrayList<Double> frameFeature,
			ArrayList<Double> iframeFeature,double attribute,double score){
		this.content = content;
		this.queEdgeFeature = queEdgeFeature;
		this.queNodeFeature = queNodeFeature;
		this.ansEdgeFeature = ansEdgeFeature;
		this.ansNodeFeature = ansNodeFeature;
		this.globalFeature = globalFeature;
		this.pairFeature = pairFeature;
		this.singleFeature = singleFeature;
		this.frameFeature = frameFeature;
		this.iframeFeature = iframeFeature;
		this.attribute = attribute;
		this.score = score;
	}
}
