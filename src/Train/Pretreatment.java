package Train;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Random;


public class Pretreatment {
	static final String trainSetPath = "./src/res/trainingdata/mc500.traindata-version13.0.txt";
	static final String outPath = "./src/res/trainingdata/mc500.traindata.final_13.0.txt";
	static ArrayList<String> postive_list = new ArrayList<>();
	static ArrayList<String> negative_list = new ArrayList<>();
	
	public static void main(String args[]) throws Exception {
		balance();
		for (String string : postive_list) {
			writeFile(string);
		}
		for(String string:negative_list){
			writeFile(string);
		}
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
	
	public static int getLabel(String data){
		String[] arg = data.split("\t");
		int result = Integer.parseInt(arg[4]);
		return result;
	}

	public static void balance() throws Exception{
		FileInputStream fis = new FileInputStream(trainSetPath);
		InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
		BufferedReader br = new BufferedReader(isr);
		String s = null;
		try {
			while ((s = br.readLine()) != null) {
				if (!(s.contains("<s id=") || s.contains("</s>") || s.contains("<q id") || s.contains("</q>"))) {
					int result = getLabel(s);
					if(result == 0){
						negative_list.add(s);
					}else{
						postive_list.add(s);
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		br.close();
		
		int postive_length = postive_list.size();
		int negative_length = negative_list.size();
		int dValue = negative_length - postive_length;
		for(int i = 0; i < dValue;i++){
			Random r = new Random();
			int index = r.nextInt(negative_list.size());
			negative_list.remove(index);
		}
		
	}

}
