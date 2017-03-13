package QABasedonDT;

import java.io.Serializable;
import java.util.HashMap;

public class DTFeautreSerialization implements Serializable{
	/***
	 * @author chenruili
	 */
	private static final long serialVersionUID = 1L;
	
	HashMap<String, Double> edgeFeature = new HashMap<>();
	HashMap<String, Double> nodeFeature = new HashMap<>();
	
	public DTFeautreSerialization(HashMap<String, Double> edgeFeature,HashMap<String, Double> nodeFeature){
		this.edgeFeature = edgeFeature;
		this.nodeFeature = nodeFeature;
	}	
}
