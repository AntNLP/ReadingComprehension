package ExtracationBasedRegular;

import edu.sussex.nlp.jws.JWS;
import edu.sussex.nlp.jws.Lin;

public class WordNetScore {
	private String str1;
	private String str2;
	private String dir = "D:/res/WordNet-3.0";
	private JWS ws = new JWS(dir, "3.0");
	
	public WordNetScore(String str1,String str2){
	       this.str1=str1;
	       this.str2=str2;
}

public double getSimilarity(){
	String[] strs1 = splitString(str1);
	String[] strs2 = splitString(str2);
	double sum = 0.0;
	for(int i = 0;i < strs1.length;i++){
		for(int j = 0;j < strs2.length;j++){
			double sc= maxScoreOfLin(strs1[i],strs2[j]);
	                 sum+= sc;
	                 System.out.println("��ǰ����: "+strs1[i]+" VS "+strs2[j]+" �����ƶ�Ϊ:"+sc);
	             }
	         }
//	         double Similarity = sum /(strs1.length * strs2.length);
	         double Similarity = sum;
	         sum=0;
	         return Similarity;
	     }

private String[] splitString(String str){
	String[] ret = str.split(" ");
	return ret;
}

private double maxScoreOfLin(String str1,String str2){
	Lin lin = ws.getLin();
	double sc = 0;
	if(str1.equals(str2)){
		sc = 1;
	}
	else{
		sc = lin.max(str1, str2, "n");
		if(sc==0){
		  sc = lin.max(str1, str2, "v");
		}
	}
	 return sc;
}
}
