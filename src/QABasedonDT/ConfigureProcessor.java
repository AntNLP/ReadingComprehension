package QABasedonDT;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class ConfigureProcessor {
	/***
	 * @author chenruili
	 */
	
	public static int globalFeatureNum = 0;
	public static int queEdgeFeatureNum = 0;
	public static int queNodeFeatureNum = 0;
	public static int pairFeatureNum = 0;
	public static int singleFeatureNum = 0;
	public static int ansEdgeFeatureNum = 0;
	public static int ansNodeFeatureNum = 0;
	public static int frameFeatureNum = 0;
	public static int iframeFeatureNum = 0;
	public static ArrayList<String> globalFeatureList = new ArrayList<>();
	public static ArrayList<String> queEdgeFeatureList =new ArrayList<>();
	public static ArrayList<String> queNodeFeatureList = new ArrayList<>();
	public static ArrayList<String> pairFeatureList = new ArrayList<>();
	public static ArrayList<String> singleFeatureList = new ArrayList<>();
	public static ArrayList<String> ansEdgeFeatureList = new ArrayList<>();
	public static ArrayList<String> ansNodeFeatureList = new ArrayList<>();
	public static ArrayList<String> frameFeatureList = new ArrayList<>();
	public static ArrayList<String> iframeFeatureList = new ArrayList<>();
	
	/***
	 * @function reading the feature configure file and conduct the model loading
	 * @param the configure file name
	 * @param model
	 */
	public static void readConfigureFile(String filename,Model model) {
		String path = "../src/res/Configures/feature/" + filename + ".ini";
		File file = new File(path);
		FileReader fr = null;
		BufferedReader br = null;
		try {
			fr = new FileReader(file);
			br = new BufferedReader(fr);
			String temp = null;
			int count = 0;
			while ((temp = br.readLine()) != null) {
				if(temp.equals("###")){
					count++;
					continue;
				}
				String feature = temp;
				switch (count) {
				case 0:
					globalFeatureList.add(feature);
					break;

				case 1:
					queEdgeFeatureList.add(feature);
					break;
					
				case 2:
					queNodeFeatureList.add(feature);
					break;
					
				case 3:
					pairFeatureList.add(feature);
					break;
					
				case 4:
					singleFeatureList.add(feature);
					break;
					
				case 5:
					ansEdgeFeatureList.add(feature);
					break;
					
				case 6:
					ansNodeFeatureList.add(feature);
					break;
					
				case 7:
					frameFeatureList.add(feature);
					break;
				case 8:
					iframeFeatureList.add(feature);
					break;
				}
			}
			br.close();
			fr.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
		globalFeatureNum = globalFeatureList.size();
		queEdgeFeatureNum = queEdgeFeatureList.size();
		queNodeFeatureNum = queNodeFeatureList.size();
		pairFeatureNum = pairFeatureList.size();
		singleFeatureNum = singleFeatureList.size();
		ansEdgeFeatureNum = ansEdgeFeatureList.size();
		ansNodeFeatureNum = ansNodeFeatureList.size();
		frameFeatureNum = frameFeatureList.size();
		iframeFeatureNum = iframeFeatureList.size();
		Model.setGlobalFeautreNum(globalFeatureNum);
		Model.setQueEdgeFeatureNum(queEdgeFeatureNum);
		Model.setQueNodeFeatureNum(queNodeFeatureNum);
		Model.setPairFeatureNum(pairFeatureNum);
		Model.setSingleFeatureNum(singleFeatureNum);
		Model.setAnsEdgeFeatureNum(ansEdgeFeatureNum);
		Model.setAnsNodeFeatureNum(ansNodeFeatureNum);
		Model.setFrameFeatureNum(frameFeatureNum);
		Model.setIFrameFeatureNum(iframeFeatureNum);
		model.weightInitialization();
	}
}
