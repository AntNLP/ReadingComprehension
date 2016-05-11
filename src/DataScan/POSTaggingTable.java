package DataScan;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.List;
import java.util.Vector;

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;

public class POSTaggingTable {
	static Vector<String> vector = new Vector<>();
	static String path1 = "./src/res/traningPOSTag.txt";
	static String path2 = "./src/res/POSTagging.txt";
	
	public static void execute() throws Exception {
		String path = "./src/res/mc500.test.txt";
		int count = 0;
		// File file = new File(path);
		FileInputStream fis = new FileInputStream(path);
		InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
		BufferedReader br = new BufferedReader(isr);
		String s = null;
		int sCount = 0;
		int qCount = 0;
		try {
			while ((s = br.readLine()) != null) {
				if (s.contains("<s id=")) {
					String str = "<s id=" + sCount + ">";
					writeFile(str,path1);
					sCount++;
					qCount = 0;
				} else if (s.contains("</s>")) {
					String str = "</s>";
					writeFile(str,path1);
				} else if (s.contains("<q id=")) {
					String str = "<q id=" + qCount + ">";
					writeFile(str,path1);
					qCount++;
				} else if (s.contains("</q>")) {
					String str = "</q>";
					writeFile(str,path1);
				} else {
					String[] arg = s.split("\t");
					arg[0] = arg[0].substring(0, arg[0].length() - 1);
					arg[1] = arg[1].substring(2, arg[1].length() - 1);
					arg[2] = arg[2].substring(1);
					String subsen = arg[0] + "\t" + arg[1] + "\t" + arg[2];
					String[] info = getInfo(subsen);
					writeFile(info);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		br.close();
		for (String string : vector) {
			writeFile(string,path2);
		}
		System.out.println("done");
	}
	
	public static void writeFile(String str,String path){
		try {
			File file = new File(path);

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			// true = append file
			// FileWriter fileWritter = new FileWriter(file.getAbsoluteFile(),
			// true);
			FileOutputStream fos = new FileOutputStream(file.getAbsoluteFile(), true);
			BufferedWriter bufferWritter = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
			bufferWritter.write(str+"\n");
			bufferWritter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void writeFile(String[] info) {
		try {
			File file = new File(path1);

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			// true = append file
			// FileWriter fileWritter = new FileWriter(file.getAbsoluteFile(),
			// true);
			FileOutputStream fos = new FileOutputStream(file.getAbsoluteFile(), true);
			BufferedWriter bufferWritter = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
			bufferWritter.write(info[0]+"\n"+"<p>"+info[1]+"</p>"+"\n");
			bufferWritter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void addVector(String posTag) {
		if (!vector.contains(posTag)) {
			vector.add(posTag);
		}
	}

	public static String[] getInfo(String subsen) {
		String[] outPut = new String[2];
		LexicalizedParser lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");

		String[] subsenArg = subsen.split("\t");
		for (String suben : subsenArg) {
			PTBTokenizer ptb = PTBTokenizer.newPTBTokenizer(new StringReader(suben));
			List words = ptb.tokenize();
			Tree parse = lp.parse(words);
			parse.pennPrint();
			System.out.println();

			TreebankLanguagePack tlp = lp.getOp().langpack();
			GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
			GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
			// Stanford dependencies in the CoNLL format
			String string1 = GrammaticalStructure.dependenciesToString(gs, gs.typedDependencies(), parse, true, false);
			String[] arg = string1.split("\n");
			for (int i = 0; i < arg.length; i++) {
				String[] temp = arg[i].split("\t");
				if (i == 0 && outPut[0]== null && outPut[1]==null) {
					outPut[0] = temp[1] + "\t";
					outPut[1] = temp[4] + "\t";
					addVector(temp[4]);
				} else {
					outPut[0] += temp[1] + "\t";
					outPut[1] += temp[4] + "\t";
					addVector(temp[4]);
				}
			}
			outPut[0] += "#";
			outPut[1] += "#";
		} 
		return outPut;
	}
}
