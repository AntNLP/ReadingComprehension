package ExtracationBasedRegular;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.time.chrono.MinguoChronology;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;

public class Strategy_BasedOnDependencyTree {
	String[] storyArg;
	String[][] question;
	String[][][] answer;
	int[][] answerResult;
	int[][] questionType;
	int[][] rightAnswer;
	ReWriter rw;
	Corpus c;
	ArrayList<String> stop_word;

	public Strategy_BasedOnDependencyTree(String[] storyArg, String[][] question, String[][][] answer) {
		this.storyArg = storyArg;
		this.question = question;
		this.answer = answer;
		this.answerResult = new int[storyArg.length][4];
		this.questionType = new int[storyArg.length][4];
		rightAnswer = getRightAnswer();
		stop_word = new ArrayList<>();
		getStop_WordList();
		c = new Corpus();
	}

	public int[][] getRightAnswer() {
		int[][] rightAnswer = new int[storyArg.length][4];
		for (int i = 0; i < answer.length; i++) {
			for (int j = 0; j < answer[i].length; j++) {
				for (int m = 0; m < answer[i][j].length; m++) {
					if (answer[i][j][m].contains("*")) {
						rightAnswer[i][j] = m;
						break;
					}
				}
			}
		}
		return rightAnswer;
	}

	public void getStop_WordList() {
		String path = "./src/res/stop_word/stop_word2.txt";
		File file = new File(path);
		FileReader fr;
		BufferedReader br;
		try {
			fr = new FileReader(file);
			br = new BufferedReader(fr);
			String word = null;
			while ((word = br.readLine()) != null) {
				word = word.trim();
				stop_word.add(word);
			}
			br.close();
			fr.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String content_preprocess(String string) {
		string = string.replace("'s", "");
		string = string.replaceAll("[\\pP‘'“”]", "");
		string = c.getLemma(string);
		string = string.toLowerCase().trim();
		return string;
	}

	public String[] storyDivided(String story) {
		String[] sentense;
		sentense = story.split("\\.|\\?|;|:|\"|!|\\,");
		return sentense;
	}

	public String questionRegular(String question) {
		int firstIndex = question.indexOf(":");
		int secondIndex = question.indexOf(":", firstIndex + 1);
		question = question.substring(secondIndex + 1);
		question = question.replaceAll("\\?", "").trim();
		return question;
	}

	public static String[] parse(String str) {
		String[] outPut = new String[4];
		LexicalizedParser lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		// This option shows parsing a list of correctly tokenized words��һ��
		PTBTokenizer ptb = PTBTokenizer.newPTBTokenizer(new StringReader(str));
		List words = ptb.tokenize();
		Tree parse = lp.parse(words);
		// parse.pennPrint();
		System.out.println();

		TreebankLanguagePack tlp = lp.getOp().langpack();
		GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
		GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
		// Stanford dependencies in the CoNLL format
		String string = GrammaticalStructure.dependenciesToString(gs, gs.typedDependencies(), parse, true, false);
		String[] arg = string.split("\n");
		for (int i = 0; i < arg.length; i++) {
			String[] temp = arg[i].split("\t");
			if (i == 0) {
				outPut[0] = temp[1] + "\t";
				outPut[1] = temp[3] + "\t";
				outPut[2] = temp[7] + "\t";
				outPut[3] = temp[6] + "\t";
			} else {
				outPut[0] += temp[1] + "\t";
				outPut[1] += temp[3] + "\t";
				outPut[2] += temp[7] + "\t";
				outPut[3] += temp[6] + "\t";
			}
		}
		for (int i = 0; i < outPut.length; i++) {
			System.out.println(outPut[i]);
		}

		// for(int i = 0;i < word.length;i++){
		// System.out.println(word[i].content+"\t"+"\t"+word[i].postag+"\t"+word[i].dependency_label+"\t"+word[i].index+"\t"+word[i].parent);
		// }
		return outPut;
	}

	public void find_supportsentense(String[] info, String story) {
		String[] story_sentense = storyDivided(story);
		String[] word = info[0].split("\t");
		for (String w : word) {
			String support = "";
			w = content_preprocess(w);
			if (!stop_word.contains(w) && w.trim().length() != 0) {
				for (int i = 0; i < story_sentense.length; i++) {
					String sentense = content_preprocess(story_sentense[i]);
					String[] sentense_word = sentense.split(" ");
					for (String s_w : sentense_word) {
						if (s_w.equals(w)) {
							support += "(" + i + ")" + story_sentense[i] + "\r\n";
							break;
						}
					}
				}
				writeFile(w);
				writeFile(support);
			}
		}
	}

	public void find_supportsentense_adddistance(String[] info, String story) {
		String[] story_sentense = storyDivided(story);
		String[] word = info[0].split("\t");
		ArrayList<Word> wordlist = new ArrayList<>();
		for (String w : word) {
			String support = "";
			w = content_preprocess(w);
			if (!stop_word.contains(w) && w.trim().length() != 0) {
				Word words = new Word(w);
				for (int i = 0; i < story_sentense.length; i++) {
					story_sentense[i] = content_preprocess(story_sentense[i]);
					String[] sentense_word = story_sentense[i].split(" ");
					for (String s_w : sentense_word) {
						if (s_w.equals(w)) {
							words.supportsentense.add(i);
							break;
						}
					}
				}
				if (words.supportsentense.size() != 0) {
					words.min = words.supportsentense.get(0);
					words.max = words.supportsentense.get(words.supportsentense.size() - 1);
				}
				wordlist.add(words);

			}
		}
		int max = 99999999;
		int min = 0;
		for (Word w : wordlist) {
			if (w.min > min && w.max < max) {
				max = w.max;
				min = w.min;
			} else if (w.min > min&& w.min < max) {
				min = w.min;
			} else if (w.max < max&&w.max > min) {
				max = w.max;
			} else if (w.max < min) {
				min = w.max;
			} else if (w.min > max) {
				max = w.min;
			}
		}
		for (Word w : wordlist) {
			writeFile(w.content);
			String support = "";
			if (w.supportsentense.size() != 0) {
				for (int index : w.supportsentense) {
					if (index >= min && index <= max) {
						support += "(" + index + ")" + story_sentense[index] + "\r\n";
					}
				}
				writeFile(support);
			}
		}
	}

	public static void writeFile(String str) {
		String path = "./src/res/SupportSentense/" + "support_distance.txt";
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
			bufferWritter.write(str);
			bufferWritter.newLine();
			bufferWritter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String answerRegular(String answer) {
		answer = answer.substring(answer.indexOf(")") + 1);
		return answer;
	}

	public void extract_supportsentense() {
		for (int i1 = 0; i1 < storyArg.length; i1++) {
			String story = storyArg[i1];
			for (int i2 = 0; i2 < question[i1].length; i2++) {
				String re_question = questionRegular(question[i1][i2]);
				re_question = c.coreference(re_question);
				String[] info = parse(re_question);
				String parse = "";
				for (int i = 0; i < info.length; i++) {
					parse += info[i] + "\r\n";
				}
				writeFile(
						"----------------------------------------------------------------------------------------------------");
				writeFile(parse);
				String correctanswer = answerRegular(answer[i1][i2][rightAnswer[i1][i2]]);
				writeFile("correct:" + correctanswer + "\r\n");
				find_supportsentense_adddistance(info, story);
			}
		}
	}
}
