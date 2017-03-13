package Tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import QABasedonDT.Ans;
import QABasedonDT.Element;
import QABasedonDT.Frame;
import QABasedonDT.Question;
import QABasedonDT.Sentence;
import QABasedonDT.Statement;
import QABasedonDT.Target;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Semafor {
	/***
	 * @author chenruili
	 */
	String inputFile = "";
	String outputFile = "";
	static String shpath = "/home/crli/semafor-master/bin/runSemafor.sh"; // 程序路径
	String dataType = "";

	/***
	 * 
	 * @param dataset
	 *            type
	 * @function set the input file path and the output file path
	 */
	public void setFilePath(String dataType) {
		inputFile = "../src/res/semafor/" + dataType + "_temp.txt";
		outputFile = "../src/res/semafor/" + dataType + "_temp_out.txt";
		this.dataType = dataType;
	}

	public void fileWriter(String string, boolean first) throws Exception {
		File file = new File(inputFile);
		if (first && file.exists()) {
			file.delete();
		}
		if (!file.exists()) {
			file.createNewFile();
		}
		FileOutputStream fos = new FileOutputStream(file, true);
		BufferedWriter bufferWritter = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
		bufferWritter.write(string);
		bufferWritter.newLine();
		bufferWritter.close();
		fos.close();
	}

	/***
	 * @function call the runSemafor.sh
	 * @return the jason outfile
	 */
	public File scriptCall() {
		String command1 = "chmod 777 " + shpath;
		File file = new File(outputFile);
		if (file.exists()) {
			file.delete();
		}
		try {
			Runtime.getRuntime().exec(command1).waitFor();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		String threadnum = "1";
		String command2 = shpath + " " + inputFile + " " + outputFile + " " + threadnum;
		try {
			Process process = Runtime.getRuntime().exec(command2);
			process.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
		File infile = new File(inputFile);
		if (infile.exists()) {
			infile.delete();
		}
		File outfile = new File(outputFile);
		return outfile;
	}

	/***
	 * @function read the jason file into question's attribute
	 * @param questions
	 *            object
	 * @param the
	 *            jason file
	 * @param start
	 *            of this batch
	 * @param end
	 *            of this batch
	 */
	public void jsonQuestionRead(Question[][] questions, File outfile, int start, int end) {
		ArrayList<TempSentense> tempSentenses_list = new ArrayList<>();
		BufferedReader reader = null;
		try {
			FileInputStream fileInputStream = new FileInputStream(outfile);
			InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
			reader = new BufferedReader(inputStreamReader);
			String tempString = null;
			while ((tempString = reader.readLine()) != null) {
				TempSentense tempSentense = jasonProcess(tempString);
				tempSentenses_list.add(tempSentense);
			}
			reader.close();
			inputStreamReader.close();
			fileInputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		int count = 0;
		for (int i1 = start; i1 < end; i1++) {
			for (int i2 = 0; i2 < questions[i1].length; i2++) {
				questions[i1][i2].isFrameNull = tempSentenses_list.get(count).isFrameNull;
				questions[i1][i2].frameList = tempSentenses_list.get(count).frameList;
				ArrayList<Frame> frames_list = questions[i1][i2].frameList;
				count++;
			}
		}
	}

	/***
	 * @function read the jason file into answer's attribute
	 * @param answers
	 *            object
	 * @param the
	 *            jason file
	 * @param start
	 *            of this batch
	 * @param end
	 *            of this batch
	 */
	public void jsonAnswerRead(Ans[][][] answers, File outfile, int start, int end) {
		ArrayList<TempSentense> tempSentenses_list = new ArrayList<>();
		BufferedReader reader = null;
		try {
			FileInputStream fileInputStream = new FileInputStream(outfile);
			InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
			reader = new BufferedReader(inputStreamReader);
			String tempString = null;
			while ((tempString = reader.readLine()) != null) {
				TempSentense tempSentense = jasonProcess(tempString);
				tempSentenses_list.add(tempSentense);
			}
			inputStreamReader.close();
			fileInputStream.close();
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		int count = 0;
		for (int i1 = start; i1 < end; i1++) {
			for (int i2 = 0; i2 < answers[i1].length; i2++) {
				for (int i3 = 0; i3 < answers[i1][i2].length; i3++) {
					answers[i1][i2][i3].isFrameNull = tempSentenses_list.get(count).isFrameNull;
					answers[i1][i2][i3].framesList = tempSentenses_list.get(count).frameList;
					count++;
				}
			}
		}
	}

	/***
	 * @function read the jason file into statement's attribute
	 * @param statements
	 *            object
	 * @param the
	 *            jason file
	 * @param start
	 *            of this batch
	 * @param end
	 *            of this batch
	 */
	public void jsonStatementRead(Statement[][][] statements, File outfile, int start, int end) {
		ArrayList<TempSentense> tempSentenses_list = new ArrayList<>();
		BufferedReader reader = null;
		try {
			FileInputStream fileInputStream = new FileInputStream(outfile);
			InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
			reader = new BufferedReader(inputStreamReader);
			String tempString = null;
			while ((tempString = reader.readLine()) != null) {
				TempSentense tempSentense = jasonProcess(tempString);
				tempSentenses_list.add(tempSentense);
			}
			inputStreamReader.close();
			fileInputStream.close();
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		int count = 0;
		for (int i1 = start; i1 < end; i1++) {
			for (int i2 = 0; i2 < statements[i1].length; i2++) {
				for (int i3 = 0; i3 < statements[i1][i2].length; i3++) {
					statements[i1][i2][i3].isFrameNull = tempSentenses_list.get(count).isFrameNull;
					statements[i1][i2][i3].frameList = tempSentenses_list.get(count).frameList;
					count++;
				}
			}
		}
	}

	/***
	 * @function read the jason file into sentence's attribute
	 * @param sentences
	 *            object
	 * @param the
	 *            jason file
	 * @param start
	 *            of this batch
	 * @param end
	 *            of this batch
	 */
	public void jsonSentenseRead(Sentence[] sentences) {
		// TODO Auto-generated method stub
		BufferedReader reader = null;
		File outfile = scriptCall();
		try {
			FileInputStream fileInputStream = new FileInputStream(outfile);
			InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
			reader = new BufferedReader(inputStreamReader);
			String tempString = null;
			int count = 0;
			while ((tempString = reader.readLine()) != null) {
				TempSentense tempSentense = jasonProcess(tempString);
				sentences[count].isFrameNull = tempSentense.isFrameNull;
				sentences[count].frameList = tempSentense.frameList;
				count++;
			}
			inputStreamReader.close();
			fileInputStream.close();
			reader.close();
		} catch (IOException e) {
			try {
				logWriter(e.getMessage());
			} catch (Exception ie) {

			}
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/***
	 * @function jason file analysis
	 * @param JsonContext
	 * @return
	 */
	public TempSentense jasonProcess(String JsonContext) {
		TempSentense sentense = new TempSentense(JsonContext);
		JSONObject jsonObject = JSONObject.fromObject(JsonContext);
		JSONArray jsonArray = jsonObject.getJSONArray("frames");
		ArrayList<Frame> frame_list = new ArrayList<>();
		for (int i = 0; i < jsonArray.size(); i++) {
			String frame_level = jsonArray.getString(i);
			String target_name = (String) JSONObject.fromObject(frame_level).getJSONObject("target").get("name");
			String target_text = (String) JSONObject
					.fromObject(JSONObject.fromObject(frame_level).getJSONObject("target").getJSONArray("spans").get(0))
					.get("text");
			Target target = new Target(target_name, target_text);
			JSONArray elementArray = JSONObject
					.fromObject(JSONObject.fromObject(frame_level).getJSONArray("annotationSets").get(0))
					.getJSONArray("frameElements");
			ArrayList<Element> elements_list = new ArrayList<>();
			for (int j = 0; j < elementArray.size(); j++) {
				String element_level = elementArray.getString(j);
				String element_name = (String) JSONObject.fromObject(element_level).get("name");
				String element_text = (String) JSONObject
						.fromObject(JSONObject.fromObject(element_level).getJSONArray("spans").getString(0))
						.get("text");
				Element element = new Element(element_name, element_text);
				elements_list.add(element);
			}
			Frame frame = new Frame(target, elements_list);
			frame_list.add(frame);
		}
		if (frame_list.size() > 0) {
			sentense.isFrameNull = false;
		}
		sentense.frameList = frame_list;
		return sentense;
	}

	/***
	 * @function log print
	 * @param the
	 *            exception message
	 * @throws Exception
	 */
	public void logWriter(String string) throws Exception {
		File file = new File("../log_message.txt");
		if (!file.exists()) {
			file.createNewFile();
		}
		FileOutputStream fos = new FileOutputStream(file, true);
		BufferedWriter bufferWritter = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
		bufferWritter.write(string);
		bufferWritter.newLine();
		bufferWritter.close();
		fos.close();
	}

}
