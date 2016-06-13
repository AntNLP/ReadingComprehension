package ExtracationBasedRegular;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Strategy_BasedOnSW {
	String[] storyArg;
	String[][] question;
	String[][][] answer;
	int[][] answerResult;
	int[][] questionType;
	ReWriter rw;
	Corpus c;
	
//	public  void test(){
//		String story = content_preprocess("Tomorrow was Little Bunny's birthday.  He was very excited.  He wanted to invite all of his friends.  \"We only have enough cake for five friends.\" His mother said.  Little Bunny thought and thought.  He wanted to invite Rabbit, Bear, Duck and Goose.  Little Bunny could invite one more friend. He thought about Turtle. Turtle was lots of fun and always told funny jokes.  He thought about Fox. Fox was super nice and always made Little Bunny feel good.  He also thought about how not inviting one of his friends would make them feel bad.  Little Bunny didn't want to make anyone feel bad. After a little bit he had an idea.  He told his mother his idea.\"I like both Turtle and Fox, and I want them both to come.  One would feel really left out if they didn't get invited.  I can give my cake to one of my friends, and that way they can both come and have a piece.\" His mother thought it was very sweet of Little Bunny to give up his piece of birthday cake so that none of his friends would feel left out.\"I'll tell you what.\" Said his mother.  \"I'll make a batch of cupcakes, and all of your friends can have some.\"");
//		double score = score("who be fun and tell funny joke", "turtle", story);
//		System.out.println(score);
//	}

	public Strategy_BasedOnSW(String[] storyArg, String[][] question, String[][][] answer) {
		this.storyArg = storyArg;
		this.question = question;
		this.answer = answer;
		this.answerResult = new int[storyArg.length][4];
		this.questionType = new int[storyArg.length][4];
		c = new Corpus();
		getQuestionType();
	}

	public void getQuestionType() {
		for (int i = 0; i < question.length; i++) {
			for (int j = 0; j < question[i].length; j++) {
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

	public String content_preprocess(String string) {
		string = string.replace("'s", "");
		string = string.replaceAll("[\\pP‘'“”]", "");
		string = c.getLemma(string);
		string = string.toLowerCase().trim();
		return string;
	}

	public String questionRegular(String question) {
		int firstIndex = question.indexOf(":");
		int secondIndex = question.indexOf(":", firstIndex + 1);
		question = question.substring(secondIndex + 1);
		question = content_preprocess(question);
		return question;
	}

	public String answerRegular(String answer) {
		answer = answer.substring(answer.indexOf(")") + 1);
		answer = content_preprocess(answer);
		return answer;
	}

	public Map<String, Integer> get_bag_words(String question, String answer, String story) {
		Map<String, Integer> map = new HashMap<>();
		String statement = question + " " + answer;
		String[] word = statement.split(" ");
		String[] story_word = story.split(" ");
		for (int i = 0; i < word.length; i++) {
			int count = 0;
			if (!map.containsKey(word[i])) {
				for (int j = 0; j < story_word.length; j++) {
					if (story_word[j].equals(word[i])) {
						count++;
					}
				}
				map.put(word[i], count);
			}
		}
		return map;
	}

	public double distanceBased(String question, String answer, String story) {
		double distance = 1000;
		Map<Integer, String> Sq = new HashMap<Integer, String>();
		Map<Integer, String> Sa = new HashMap<Integer, String>();
		String[] question_word = question.split(" ");
		String[] story_word = story.split(" ");
		String[] answer_word = answer.split(" ");
		for (int i = 0; i < story_word.length; i++) {
			for (int j = 0; j < question_word.length; j++) {
				if (story_word[i].equals(question_word[j])) {
					Sq.put(i, story_word[i]);
				}
			}
		}
		for (int i = 0; i < story_word.length; i++) {
			for (int j = 0; j < answer_word.length; j++) {
				if (story_word[i].equals(answer_word[j])) {
					boolean flag = true;
					for (int m = 0; m < question_word.length; m++) {
						if (question_word[m].equals(story_word[i])) {
							flag = false;
						}
					}
					if (flag) {
						Sa.put(i, story_word[i]);
					}
				}
			}
		}
		if (Sq.size() == 0 || Sa.size() == 0) {
			distance = 1;
		} else {
			for (Map.Entry<Integer, String> entry : Sa.entrySet()) {
				for (Map.Entry<Integer, String> entry2 : Sq.entrySet()) {
					double local_distance = Math.abs(entry.getKey() - entry2.getKey());
					if (local_distance < distance) {
						distance = local_distance;
					}
				}
			}
			distance = (1 /(double) (story_word.length - 1)) * (distance + 1);
		}
		return distance;
	}

	public double score(String question, String answer, String story) {
//		System.out.println("----------------------------------------------------------");
		double maxScore = -100;
		Map<String, Integer> wordMap = get_bag_words(question, answer, story);
		String[] word = story.split(" ");
		for (int i = 0; i < word.length - wordMap.size(); i++) {
			double score = 0;
			for (int j = 0; j < wordMap.size(); j++) {
				if (wordMap.containsKey(word[i + j])) {
//					System.out.println(word[i+j]+","+wordMap.get(word[i+j]));
					double count = wordMap.get(word[i + j]);
					score += Math.log(1.0 + 1 / count);
//					score++;
				}
			}
			if (score > maxScore) {
				maxScore = score;
				System.out.println(word[i]+":"+maxScore);
			}
		}
		System.out.println("q:"+question+",a:"+answer+"score:"+maxScore);
		maxScore = maxScore - distanceBased(question, answer, story);
		return maxScore;
	}

	public int[][] getAnswerResult() {
		for (int i = 0; i < answer.length; i++) {
			String story = content_preprocess(storyArg[i]);
//			System.out.println(story);
			for (int j = 0; j < answer[i].length; j++) {
				double maxScore = 0;
				int index = 0;
				String re_question = questionRegular(question[i][j]);
				for (int m = 0; m < answer[i][j].length; m++) {
					String re_answer = answerRegular(answer[i][j][m]);
					double score = score(re_question, re_answer, story);
					if (score > maxScore) {
						maxScore = score;
						index = m;
					}
				}
				answerResult[i][j] = index;
			}
		}
		// for (int i = 0; i < answerResult.length; i++) {
		// for (int j = 0; j < answerResult[i].length; j++) {
		// System.out.print(answerResult[i][j] + " ");
		// }
		// System.out.println();
		// }
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
		// for (String str : datat) {
		// System.out.println(str);
		// }
		// System.out.println(stop_word.size());
		for(int i = 0;i < storyArg.length;i++){
			System.out.println("<s id ="+i+" >");
			for(int j = 0;j < answerResult[i].length;j++){
				System.out.print(answerResult[i][j]+" ");
			}
			System.out.println();
			System.out.println("</s>");
		}
	}

}
