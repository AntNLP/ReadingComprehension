package QABasedonDT;

import java.io.File;

public class demo {
	/***
	 * @author chenruili
	 */

	/***
	 * @function the entrance of the program
	 */
	public static void main(String args[]) throws Exception {
		String parameter = args[0];
		if (parameter.equals("-r")) {
			String configurefile = args[1];
			String trainCorpus = args[2];
			int trainSize = Integer.valueOf(args[3]);
			String testCorpus = args[4];
			int testSize = Integer.valueOf(args[5]);
			int dataNum = Integer.valueOf(args[6]);
			int featureNum = Integer.valueOf(args[7]);
			int logNum = Integer.valueOf(args[8]);
			double learningRate = Double.valueOf(args[9]);
			execute_run(configurefile, trainCorpus, trainSize, testCorpus, testSize, dataNum, featureNum, logNum,
					learningRate);
		} else if (parameter.equals("-p")) {
			String corpus = args[1];
			int size = Integer.valueOf(args[2]);
			int dataNum = Integer.valueOf(args[3]);
			int start = Integer.valueOf(args[4]);
			int end = Integer.valueOf(args[5]);
			preprocess(corpus, dataNum, size, start, end);
		} else if (parameter.equals("-s")) {
			String corpus = args[1];
			int dataNum = Integer.valueOf(args[2]);
			int featureNum = Integer.valueOf(args[3]);
			int size = Integer.valueOf(args[4]);
			int start = Integer.valueOf(args[5]);
			int end = Integer.valueOf(args[6]);
			execute_featureSerializable(corpus, dataNum, featureNum, size, start, end);
		} else if (parameter.equals("-test")) {
			String configurefile = args[1];
			String parafile = args[2];
			String corpus = args[3];
			int dataSize = Integer.valueOf(args[4]);
			int dataNum = Integer.valueOf(args[5]);
			int featureNum = Integer.valueOf(args[6]);
			int logNum = Integer.valueOf(args[7]);
			execute_test(configurefile, parafile, corpus, dataSize, dataNum, featureNum,logNum);
		} else if (parameter.equals("f")) {
			String corpus = args[1];
			int dataSize = Integer.valueOf(args[2]);
			int dataNum = Integer.valueOf(args[3]);
			System.out.println("checking is beginning");
			execute_framecheck(corpus, dataSize, dataNum);
		}
	}

	/***
	 * @function extract the frame
	 */
	public static void execute_framecheck(String corpus, int dataSize, int dataNum) throws Exception {
		QABasedonDT fc = new QABasedonDT("../src/res/corpus/" + corpus,
				dataSize);
		fc.setDataNum(dataNum);
		fc.frameCheck();
	}

	// public static void execute_findframesupport(String type, int num) throws
	// Exception{
	// QABasedonDT fc = new
	// QABasedonDT("/home/crli/projection/ReadingComprehension/../src/res/corpus/mc500."+type+".txt",
	// num);
	// fc.evalue_frame_supportsentense();
	// }

	/***
	 * @function conduct feature serializing
	 * @param dataset
	 *            type
	 * @param dataset
	 *            size
	 * @param start
	 * @param end
	 * @throws Exception
	 */
	public static void execute_featureSerializable(String corpus, int dataNum, int featureNum, int num, int start,
			int end) throws Exception {
		QABasedonDT testv = new QABasedonDT("../src/res/corpus/" + corpus, num);
		testv.setDataNum(dataNum);
		testv.setFeatureNum(featureNum);
		testv.featureSerializable(start, end);
	}

	/***
	 * @function conduct train and test
	 * @param configurefile
	 */
	public static void execute_run(String configurefile, String trainCorpus, int trainSize, String testCorpus,
			int testSize, int dataNum, int featureNum, int logNum, double learningRate) {
		int count = 0;
		boolean flag = true;
		String log = "";
		while (flag) {
			log = "para_" + configurefile + "_" + count;
			File file = new File("../src/res/Configures/parameter" + logNum
					+ "/" + log + ".txt");
			if (!file.exists()) {
				flag = false;
			} else {
				count++;
			}
		}
		QABasedonDT train = new QABasedonDT("../src/res/corpus/" + trainCorpus,
				trainSize);
		train.setDataNum(dataNum);
		train.setFeatureNum(featureNum);
		train.setLogNum(logNum);
		train.loadConfigureFile(configurefile, train.model);
		try {
			train.train(log);
		} catch (Exception e) {
			e.printStackTrace();
		}
		QABasedonDT test = new QABasedonDT("../src/res/corpus/" + testCorpus,
				testSize);
		test.setDataNum(dataNum);
		test.setFeatureNum(featureNum);
		test.setLogNum(logNum);
		try {
			test.test(log, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void execute_test(String configurefile, String parafile, String corpus, int dataSize, int dataNum,
			int featureNum,int logNum) {
		QABasedonDT testv = new QABasedonDT("../src/res/corpus/" + corpus,
				dataSize);
		testv.setDataNum(dataNum);
		testv.setFeatureNum(featureNum);
		testv.setLogNum(logNum);
		testv.loadConfigureFile(configurefile, testv.model);
		try {
			testv.test(parafile, true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			 e.printStackTrace(); 
		}
	}

	/***
	 * @function conduct data preprocessing
	 * @param dataset
	 *            type
	 * @param dataset
	 *            size
	 * @param start
	 * @param end
	 * @throws Exception
	 */
	public static void preprocess(String corpus, int dataNum, int num, int start, int end) throws Exception {
		QABasedonDT testv = new QABasedonDT("../src/res/corpus/" + corpus, num);
		testv.setDataNum(dataNum);
		testv.preprocess(start, end);
	}
}
