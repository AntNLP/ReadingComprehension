package ExtracationBasedRegular;

import java.util.ArrayList;

import StructExtraction.StructExtraction;

public class Strategy_BasedOnStructure {
	String[] storyArg;
	String[][] question;
	String[][][] answer;
	int[][] answerResult;
	int[][] questionType;
	ReWriter rw;
	Corpus c;

	public Strategy_BasedOnStructure(String[] storyArg, String[][] question, String[][][] answer) {
		this.storyArg = storyArg;
		this.question = question;
		this.answer = answer;
		this.answerResult = new int[storyArg.length][4];
		this.questionType = new int[storyArg.length][4];
		c = new Corpus();
		rw = new ReWriter();
		getQuestionType();
	}

	public void getQuestionType() {
		for (int i = 0; i < question.length; i++) {
			for (int j = 0; j < question[i].length; j++) {
				System.out.println(question[i][j]);
				if (question[i][j].contains(": one:")) {
					questionType[i][j] = 0;
				} else if (question[i][j].contains(": multiple:")) {
					questionType[i][j] = 1;
				}
			}
		}
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

	public String answerRegular(String answer) {
		answer = answer.substring(answer.indexOf(")") + 1);
		answer = answer.replaceAll("\\.", "").trim();
		return answer;
	}

	public String sentenseFilter(String sentense) {
		sentense = sentense.replace("'s", "");
		sentense = c.getLemma(sentense).toLowerCase();
		return sentense;
	}

	public ArrayList<String> extractSupport(String[] struct, String[] sentense) {
		ArrayList<String> supportlist = new ArrayList<>();
		String subj = c.getLemma(struct[0]).toLowerCase();
		String pre = c.getLemma(struct[1]).toLowerCase();
		String obj = c.getLemma(struct[2]).toLowerCase();
		boolean subj_label = false;
		boolean pre_label = false;
		boolean obj_label = false;
		for (int i = 0; i < sentense.length; i++) {
			String sen = sentense[i];
			sen = sentenseFilter(sen);
			String[] word = sen.split(" ");
			for (String w : word) {
				if (w.equals(subj)) {
					subj_label = true;
				}
				if (w.equals(pre)) {
					pre_label = true;
				}
				if (w.equals(obj)) {
					obj_label = true;
				}
			}
			if ((subj_label && pre_label) || (obj_label && pre_label)) {
				supportlist.add(sen);
			}
		}
		return supportlist;
	}

	public double getWordMatchScore(String support, String answer) {
		double score = 0;
		String[] support_word = support.split(" ");
		String[] answer_word = answer.split(" ");
		for (int i = 0; i < support_word.length; i++) {
			for (int j = 0; j < answer_word.length; j++) {
				if (support_word[i].equals(answer_word[j])) {
					score++;
				}
			}
		}
		return score;
	}

	public static double getNGramsScore(String statement, String sentense, int n) {
		double n_grams = 0;
		double count = 0;
		String[] statement_Ngram = null;
		String[] support_Ngram = null;
		int flag = 2;
		if (n == 2) {
			flag = 2;
			statement_Ngram = get_2grams(statement);
			support_Ngram = get_2grams(sentense);
		} else if (n == 3) {
			flag = 3;
			statement_Ngram = get_3grams(statement);
			support_Ngram = get_3grams(sentense);
		}

		if (statement_Ngram != null && support_Ngram != null) {
			for (int i = 0; i < statement_Ngram.length; i++) {
				for (int j = 0; j < support_Ngram.length; j++) {
					if (statement_Ngram[i].equals(support_Ngram[j])) {
						if (flag == 2) {
							count = count + 2;
							break;
						} else if (flag == 3) {
							count = count + 3;
							break;
						}
					}
				}
			}
		}
		n_grams = count;
		if (support_Ngram != null && support_Ngram.length > 0) {
			n_grams = n_grams / support_Ngram.length;
		}
		return n_grams;
	}

	public static String[] get_2grams(String sentense) {
		String[] arg = sentense.split(" ");
		String[] arg_2grams = null;
		if (arg.length > 1) {
			arg_2grams = new String[arg.length - 1];
			for (int i = 0; i < arg.length - 1; i++) {
				arg_2grams[i] = arg[i] + " " + arg[i + 1];
			}
		}
		return arg_2grams;
	}

	public static String[] get_3grams(String sentense) {
		String[] arg = sentense.split(" ");
		String[] arg_3grams = null;
		if (arg.length > 2) {
			arg_3grams = new String[arg.length - 2];
			for (int i = 0; i < arg.length - 2; i++) {
				arg_3grams[i] = arg[i] + " " + arg[i + 1] + " " + arg[i + 2];
			}
		}
		return arg_3grams;
	}

	public double score(ArrayList<String> supportlist, String answer) {
		double score = 0;
		answer = sentenseFilter(answer);
		for (String support : supportlist) {
			score += getWordMatchScore(support, answer);
			score += getNGramsScore(answer, support, 2);
			score += getNGramsScore(answer, support, 3);
		}
		return score;
	}
	
	public boolean isRegulation(String sentense,String words){
		String[] sentenseArg = sentense.split(" ");
		boolean flag = false;
		for(int i = 0;i < sentenseArg.length;i++){
			if(sentenseArg[i].equals(words)){
				flag = true;
				break;
			}
		}
		return flag;
	}
	
	public double match(String statement, String sentense,String answer) {
		Corpus c = new Corpus();
		String state_Regular = statement.toLowerCase();
		String keyAnswer = answer.toLowerCase();
		state_Regular = c.getLemma(state_Regular);
		String s_Regular = sentense.toLowerCase();
		s_Regular = c.getLemma(s_Regular);
		keyAnswer = c.getLemma(keyAnswer);
//		state_Regular = removeStop_word(state_Regular);
//		s_Regular = removeStop_word(s_Regular);
		String[] statementArg = state_Regular.split(" ");
		double score = 0;
//		WordNetScore wordnet = new WordNetScore(state_Regular, s_Regular);
//		score = wordnet.getSimilarity();
		for (int i = 0; i < statementArg.length; i++) {
			String temp = statementArg[i];
			if (isRegulation(s_Regular,temp)) {
				score++;
			} else {
				score = score - 0.5;
			}
		}
		if(s_Regular.contains(keyAnswer)){
			score = score + 10;
		}
		score += getNGramsScore(state_Regular, s_Regular, 2);
		score += getNGramsScore(state_Regular, s_Regular, 3);
		return score;
	}
	
	public ArrayList<String> combine(String answer, String question) {
		ArrayList<String> re_questionlist = rw.reWrite(question);
		ArrayList<String> statementlist = new ArrayList<String>();
		for(int i =0;i < re_questionlist.size();i++){
			String statement = rw.combine(re_questionlist.get(i), answer);
			statementlist.add(statement);
		}
		return statementlist;
	}
	
	public double score(ArrayList<String> statementlist, String[] sentense,String answer) {
		double maxScore = -100;
		for(int i = 0;i < statementlist.size();i++){
			for (int j = 0; j < sentense.length; j++) {
				double score = match(statementlist.get(i), sentense[j],answer);
				if (score > maxScore) {
					maxScore = score;
				}
			}
		}

		return maxScore;
	}

	public int[][] getAnswerResult() {
		StructExtraction se = new StructExtraction();
		for (int i = 0; i < answer.length; i++) {
			String[] sentense = storyDivided(storyArg[i]);
			for (int j = 0; j < answer[i].length; j++) {
				double maxScore = 0;
				int index = 0;
				String re_question = questionRegular(question[i][j]);
				String[] struct = se.extract(re_question);
				ArrayList<String> supportList = extractSupport(struct, sentense);
				if (supportList.size() != 0) {
					for (int m = 0; m < answer[i][j].length; m++) {
						String re_answer = answerRegular(answer[i][j][m]);
						double score = score(supportList, re_answer);
						if (score > maxScore) {
							maxScore = score;
							index = m;
						}
					}
				}
				else{
					for (int m = 0; m < answer[i][j].length; m++) {
						String re_answer = answerRegular(answer[i][j][m]);
						ArrayList<String> statement = combine(re_answer, re_question);
						double score = score(statement, sentense,re_answer);
						if (score > maxScore) {
							maxScore = score;
							index = m;
						}
					}
				}
				answerResult[i][j] = index;
			}
		}
		for (int i = 0; i < answerResult.length; i++) {
			for (int j = 0; j < answerResult[i].length; j++) {
				System.out.print(answerResult[i][j] + " ");
			}
			System.out.println();
		}
		return answerResult;
	}

	public void evaluate() {
		int[][] rightAnswer = getRightAnswer();
		int[][] answerResult = getAnswerResult();
		double oneTypeCorrect = 0;
		double oneTypeError = 0;
		double multiTypeCorrect = 0;
		double multiTypeError = 0;
		double correct = 0;
		double fault = 0;
		for (int i = 0; i < storyArg.length; i++) {
			for (int j = 0; j < 4; j++) {
				if (questionType[i][j] == 0) {
					if (rightAnswer[i][j] == answerResult[i][j]) {
						oneTypeCorrect++;
						correct++;
					} else {
						oneTypeError++;
						fault++;
					}
				} else if (questionType[i][j] == 1) {
					if (rightAnswer[i][j] == answerResult[i][j]) {
						correct++;
						multiTypeCorrect++;
					} else {
						fault++;
						multiTypeError++;
					}
				}
			}
		}
		double correctRate = correct / (correct + fault);
		double oneTypeRate = oneTypeCorrect / (oneTypeCorrect + oneTypeError);
		double multiTypeRate = multiTypeCorrect / (multiTypeCorrect + multiTypeError);
		System.out.println("正确个数：" + correct + ",错误个数" + fault);
		System.out.println("正确率：" + correctRate);
		System.out.println("One型正确个数：" + oneTypeCorrect + ",One型错误个数: " + oneTypeError + ",One型正确率：" + oneTypeRate);
		System.out.println(
				"Multi型正确个数:" + multiTypeCorrect + ",Multi型错误个数：" + multiTypeError + ",Multi型正确率：" + multiTypeRate);

	}
}
