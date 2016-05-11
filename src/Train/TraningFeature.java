package Train;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import FeatureExtraction.FeatureExtract;

public class TraningFeature {
	static ArrayList<String> trainingList = new ArrayList<>();
	static final String trainSetPath = "./src/res/trainingdata/mc500.traindata.final_13.0.txt";
	static final String outPath = "./src/res/trainingdata/train_feature_13.txt";

	public static void main(String args[]) throws Exception {
		getTrainingData();
		FeatureExtract fe = new FeatureExtract();
		for (String string : trainingList) {
			String data = format(string);
			double[] feature = fe.execute(data);
			String line = toStringFormat(feature);
			writeFile(line);
		}
	}

	public static String toStringFormat(double[] feature) {
		String line = "";
		for (int i = 0; i < feature.length; i++) {
			if (i == 0) {
				line = feature[i] + " ";
			} else {
				if (feature[i] != 0) {
					line += i + ":" + feature[i] + " ";
				}
			}
		}
		return line;
	}

	public static void writeFile(String str) {
		try {
			File file = new File(outPath);

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			// true = append file
			// FileWriter fileWritter = new FileWriter(file.getAbsoluteFile(),
			// true);
			FileOutputStream fos = new FileOutputStream(file.getAbsoluteFile(), true);
			BufferedWriter bufferWritter = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
			bufferWritter.write(str + "\n");
			bufferWritter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String format(String data) {
		String[] arg = data.split("\t");
		String statement = arg[0];
		String question = arg[1].trim();
		String answer = arg[2].trim();
		String support = arg[3].trim();
		String result = statement+"\t"+question + "\t" + answer + "\t" + support + "\t" + arg[4];
		return result;
	}

//	public static int getTrainingCount() throws Exception {
//		FileInputStream fis = new FileInputStream(trainSetPath);
//		InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
//		BufferedReader br = new BufferedReader(isr);
//		String s = null;
//		int count = 0;
//		try {
//			while ((s = br.readLine()) != null) {
//					count++;
//			}
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		br.close();
//		return count;
//	}

	public static void getTrainingData() throws Exception {
//		int length = getTrainingCount();
//		trainingData = new String[length];
		FileInputStream fis = new FileInputStream(trainSetPath);
		InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
		BufferedReader br = new BufferedReader(isr);
		String s = null;
//		int dataCount = 0;
		try {
			while ((s = br.readLine()) != null) {
				if (!(s.contains("<s id=") || s.contains("</s>") || s.contains("<q id") || s.contains("</q>"))) {
					trainingList.add(s);
//					dataCount++;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
