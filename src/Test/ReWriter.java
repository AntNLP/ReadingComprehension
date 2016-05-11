package Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class ReWriter {
	public String combine(String question, String answer) {
		String h = question.replace("*", answer);
		return h;
	}

	public ArrayList<String> reWrite(String string) {
		ArrayList<String> newStringlist = new ArrayList<String>();
		// string = string.toLowerCase();
		String[] args = string.split(" ");
		if (args.length > 0) {
			String type = args[0].toLowerCase();
			if (type.equals("what")) {
				String whatString = string.substring(5);
				newStringlist = what_type_process(whatString);
			} else if (type.equals("how")) {
				String howString = string.substring(4);
				newStringlist = how_type_process(howString);
			} else if (type.equals("why")) {
				String whyString = string.substring(4);
				newStringlist = why_type_process(whyString);
			} else if (type.equals("where")) {
				String whereString = string.substring(6);
				newStringlist = where_type_process(whereString);
			} else if (type.equals("who")) {
				String whoString = string.substring(4);
				newStringlist = who_type_process(whoString);
			} else if (type.equals("whose")) {
				String whoseString = string.substring(6);
				newStringlist = whose_type_process(whoseString);
			} else {
				newStringlist = general_type_process(args);
			}
		}
		return newStringlist;
	}

	public ArrayList<String> general_type_process(String[] args) {
		ArrayList<String> list = new ArrayList<String>();
		String newString = "";
		for (int i = 1; i < args.length; i++) {
			newString += args[i] + " ";
		}
		newString += "*";
		list.add(newString.trim());
		return list;
	}

	public ArrayList<String> what_type_simplequestion(String[] arg) {
		ArrayList<String> list = new ArrayList<String>();
		if (getPos(arg[arg.length - 1]).equals("IN")) {
			String newString = "";
			for (int i = 1; i < arg.length - 1; i++) {
				newString += arg[i] + " ";
			}
			newString = newString + arg[0] + " " + arg[arg.length - 1] + " *";
			list.add(newString);
		} else {
			String newString1 = "";
			String newString2 = "";
			for (int i = 1; i < arg.length; i++) {
				newString1 += arg[i] + " ";
				newString2 += arg[i] + " ";
			}
			newString1 = "* " + arg[0] + " " + newString1.substring(0, newString1.length() - 1);
			newString2 = newString2 + arg[0] + " *";
			list.add(newString1);
			list.add(newString2);
		}
		return list;
	}

	public ArrayList<String> what_type_specialquestion(String[] arg, String str) {
		ArrayList<String> list = new ArrayList<String>();
		String pos = getPos(str);
		String[] posArg = pos.split("\t");
		int in_index = -1;
		int vb_index = -1;
		boolean in_flag = true;
		boolean vb_flag = true;
		for (int i = 0; i < posArg.length; i++) {
			if (posArg[i].equals("IN") && in_flag) {
				in_index = i;
				in_flag = false;
			}
			if (posArg[i].equals("VB") && vb_flag) {
				vb_index = i;
				vb_flag = false;
			}
		}
		String newString = "";
		if (in_index == -1) {
			if (vb_index != -1) {
				for (int i = 1; i < arg.length; i++) {
					newString += arg[i] + " ";
					if (i == vb_index) {
						newString += "* ";
					}
				}
			} else {
				for (int i = 1; i < arg.length; i++) {
					if (i == arg.length - 1 && (arg[i].equals("do") || arg[i].equals("doing"))) {

					} else {
						newString += arg[i] + " ";
					}
				}
				newString = newString + "*";
			}
			list.add(newString);
		} else {
			for (int i = 1; i < arg.length; i++) {
				if (i == in_index - 1) {
					if (arg[i].equals("do") || arg[i].equals("doing")) {
						newString += "* ";
					} else {
						newString += arg[i] + " * ";
					}
				} else {
					newString += arg[i] + " ";
				}
			}
			list.add(newString.substring(0, newString.length() - 1));
		}
		return list;
	}

	public ArrayList<String> what_type_negative_specialquestion(String[] arg) {
		ArrayList<String> list = new ArrayList<String>();
		String str = "";
		for (int i = 0; i < arg.length - 1; i++) {
			str += arg[i + 1] + " ";
		}
		str = str.substring(0, str.length() - 1);
		String pos = getPos(str);
		str = str.toLowerCase();
		String[] newarg = str.split(" ");
		String[] posArg = pos.split("\t");
		int in_index = -1;
		int vb_index = -1;
		boolean in_flag = true;
		for (int i = 0; i < posArg.length; i++) {
			if (posArg[i].equals("IN") && in_flag) {
				in_index = i;
				in_flag = false;
			}
			if (posArg[i].equals("VB")) {
				vb_index = i;
			}
		}

		String newString = "";
		if (in_index == -1) {
			for (int i = 0; i < newarg.length; i++) {
				if (i == newarg.length - 1 && (newarg[i].equals("do") || newarg[i].equals("doing"))) {

				} else {
					if (vb_index != -1 && i == vb_index - 1) {
						newString += newarg[i] + " " + arg[0] + " ";
					} else {
						newString += newarg[i] + " ";
					}
				}
			}
			newString = newString + "*";
			list.add(newString);
		} else {
			for (int i = 0; i < newarg.length; i++) {
				if (i == in_index - 1) {
					if (newarg[i].equals("do") || newarg[i].equals("doing")) {
						newString += "* ";
					} else {
						newString += newarg[i] + " * ";
					}
				} else {
					if (vb_index != -1 && i == vb_index - 1) {
						newString += newarg[i] + " " + arg[0] + " ";
					} else {
						newString += newarg[i] + " ";
					}
				}
			}
			list.add(newString.substring(0, newString.length() - 1));
		}
		return list;
	}

	public ArrayList<String> what_type_futuretensequestion(String[] arg) {
		ArrayList<String> list = new ArrayList<String>();
		String str = "";
		for (int i = 0; i < arg.length - 1; i++) {
			str += arg[i + 1] + " ";
		}
		str = str.substring(0, str.length() - 1);
		String pos = getPos(str);
		str = str.toLowerCase();
		String[] newarg = str.split(" ");
		String[] posArg = pos.split("\t");
		int insert_index = -1;
		int in_index = -1;
		String[] entity = { "PRP", "NN", "NNP", "NNS" };
		String[] verb = { "VB", "VBP", "RB", "VBD" };
		boolean in_flag = true;
		for (int i = 1; i < posArg.length; i++) {
			if ((Arrays.asList(verb).contains(posArg[i])) && (Arrays.asList(entity).contains(posArg[i - 1]))) {
				insert_index = i - 1;
			}
			if (posArg[i].equals("IN") && in_flag) {
				in_index = i;
				in_flag = false;
			}
		}
		String newString = "";
		if (in_index == -1) {
			for (int i = 0; i < newarg.length; i++) {
				if (i == newarg.length - 1 && (newarg[i].equals("do") || newarg[i].equals("doing"))) {

				} else {
					if (insert_index != -1 && i == insert_index) {
						newString += newarg[i] + " " + arg[0] + " ";
					} else {
						newString += newarg[i] + " ";
					}
				}
			}
			newString = newString + "*";
			list.add(newString);
		} else {
			for (int i = 0; i < newarg.length; i++) {
				if (i == in_index - 1) {
					if (newarg[i].equals("do") || newarg[i].equals("doing")) {
						newString += "* ";
					} else {
						newString += newarg[i] + " * ";
					}
				} else {
					if (insert_index != -1 && i == insert_index) {
						newString += newarg[i] + " " + arg[0] + " ";
					} else {
						newString += newarg[i] + " ";
					}
				}
			}
			list.add(newString.substring(0, newString.length() - 1));
		}
		return list;
	}

	public ArrayList<String> what_type_perfecttensequestion(String[] arg) {
		ArrayList<String> list = new ArrayList<String>();
		String str = "";
		for (int i = 0; i < arg.length - 1; i++) {
			str += arg[i + 1] + " ";
		}
		str = str.substring(0, str.length() - 1);
		String pos = getPos(str);
		str = str.toLowerCase();
		String[] newarg = str.split(" ");
		String[] posArg = pos.split("\t");
		int insert_index = -1;
		int in_index = -1;
		boolean in_flag = true;
		String newString = "";
		String[] entity = { "PRP", "NN", "NNP", "NNS" };
		String[] verb = { "VB", "VBP", "RB", "VBN" };
		for (int i = 1; i < posArg.length; i++) {
			if ((Arrays.asList(verb).contains(posArg[i])) && (Arrays.asList(entity).contains(posArg[i - 1]))) {
				insert_index = i - 1;
			}
			if (posArg[i].equals("IN") && in_flag) {
				in_index = i;
				in_flag = false;
			}
		}
		if ((Arrays.asList(verb).contains(posArg[0]))) {
			for (int i = 0; i < newarg.length; i++) {
				newString += newarg[i] + " ";
			}
			newString = "* " + arg[0] + " " + newString;
			list.add(newString.substring(0, newString.length() - 1));
		} else if (in_index == -1) {
			for (int i = 0; i < newarg.length; i++) {
				if (i == newarg.length - 1 && (newarg[i].equals("do") || newarg[i].equals("doing"))) {

				} else {
					if (insert_index != -1 && i == insert_index) {
						newString += newarg[i] + " " + arg[0] + " ";
					} else {
						newString += newarg[i] + " ";
					}
				}
			}
			newString = newString + "*";
			list.add(newString);
		} else {
			for (int i = 0; i < newarg.length; i++) {
				if (i == in_index - 1) {
					if (newarg[i].equals("do") || newarg[i].equals("doing")) {
						newString += "* ";
					} else {
						newString += newarg[i] + " * ";
					}
				} else {
					if (insert_index != -1 && i == insert_index) {
						newString += newarg[i] + " " + arg[0] + " ";
					} else {
						newString += newarg[i] + " ";
					}
				}
			}
			list.add(newString.substring(0, newString.length() - 1));
		}
		return list;
	}

	public ArrayList<String> what_type_verbquestion(String[] arg) {
		ArrayList<String> list = new ArrayList<String>();
		String newString = "";
		for (int i = 0; i < arg.length; i++) {
			newString += arg[i] + " ";
		}
		newString = "* " + newString;
		list.add(newString.substring(0, newString.length() - 1));
		return list;
	}

	public ArrayList<String> what_type_happenquestion(String[] arg) {
		ArrayList<String> list = new ArrayList<String>();
		String newString = "";
		for (int i = 1; i < arg.length; i++) {
			newString += arg[i] + " ";
		}
		newString = "* " + newString;
		list.add(newString.substring(0, newString.length() - 1));
		return list;
	}

	public ArrayList<String> what_type_generalquestion(String[] arg) {
		ArrayList<String> list = new ArrayList<String>();
		String newString = "";
		for (int i = 1; i < arg.length; i++) {
			newString += arg[i] + " ";
		}
		newString = "* " + newString;
		list.add(newString.substring(0, newString.length() - 1));
		return list;
	}

	public ArrayList<String> what_type_generaltypequestion(String[] arg, String type) {
		ArrayList<String> list = new ArrayList<String>();
		String newString = "";
		for (int i = 1; i < arg.length; i++) {
			newString += arg[i] + " ";
		}
		newString = type + " * " + newString;
		list.add(newString.substring(0, newString.length() - 1));
		return list;
	}

	public ArrayList<String> what_type_process(String str) {
		ArrayList<String> list = new ArrayList<String>();
		String[] arg = str.split(" ");
		if (arg.length > 0) {
			/**
			 * what+is\was\are\were
			 */
			if (arg[0].equals("is") || arg[0].equals("was") || arg[0].equals("are") || arg[0].equals("were")) {
				if (arg.length > 1) {
					list = what_type_simplequestion(arg);
				}
			}
			/***
			 * what+do\does+did
			 */
			else if (arg[0].equals("do") || arg[0].equals("does") || arg[0].equals("did")) {
				if (arg.length > 1) {
					list = what_type_specialquestion(arg, str);
				}
			}
			/**
			 * what+don't\didn't\doesn't
			 */
			else if (arg[0].equals("don't") || arg[0].equals("doesn't") || arg[0].equals("didn't")) {
				if (arg.length > 1) {
					list = what_type_negative_specialquestion(arg);
				}
			}
			/**
			 * what+will\would\could\can
			 */
			else if (arg[0].equals("will") || arg[0].equals("would") || arg[0].equals("can")
					|| arg[0].equals("could")) {
				list = what_type_futuretensequestion(arg);
			}
			/**
			 * what have\has\had
			 */
			else if (arg[0].equals("have") || arg[0].equals("has") || arg[0].equals("had")) {
				list = what_type_perfecttensequestion(arg);
			}
			/**
			 * what_happened
			 */
			else if (arg[0].equals("happened")) {
				list = what_type_happenquestion(arg);
			}
			/**
			 * what+verb
			 */
			else if (getPos(arg[0]).equals("VB") || getPos(arg[0]).equals("VBP") || getPos(arg[0]).equals("VBN")) {
				list = what_type_verbquestion(arg);
			}
			/**
			 * 
			 */
			else if (getPos(arg[0]).equals("NN") || getPos(arg[0]).equals("NNS") || getPos(arg[0]).equals("NNP")) {
				String[] verb = { "VB", "VBP", "VBN", "MD", "VBD" };
				int vb_index = -1;
				for (int i = 0; i < arg.length; i++) {
					if ((Arrays.asList(verb).contains(getPos(arg[i])))) {
						vb_index = i;
						;
						break;
					}
				}
				String type = "";
				if (vb_index != -1) {
					for (int i = 0; i < vb_index; i++) {
						type += arg[i] + " ";
					}
					list = what_type_generaltypequestion(arg, type);
				} else {
					list = what_type_generalquestion(arg);
				}
			} else {
				list = what_type_generalquestion(arg);
			}
		}
		return list;
	}

	public ArrayList<String> how_type_numberquestion(String[] arg) {
		ArrayList<String> list = new ArrayList<String>();
		String str = "";
		int insert_index = -1;
		String[] verb = { "VB", "VBN", "VBP", "VBD", "VBZ" };
		boolean insert_flag = true;
		for (int i = 1; i < arg.length; i++) {
			str += arg[i] + " ";
			String pos = getPos(arg[i]);
			if (Arrays.asList(verb).contains(pos) && insert_flag) {
				insert_index = i - 1;
				insert_flag = false;
			}
		}
		str = str.trim();
		String[] newarg = str.split(" ");
		if (insert_index != -1) {
			if (newarg[insert_index].equals("does") || newarg[insert_index].equals("did")
					|| newarg[insert_index].equals("do")) {
				String type = "";
				String newString = "";
				for (int i = 0; i < insert_index; i++) {
					type += newarg[i] + " ";
				}
				type = type.trim();
				for (int i = insert_index + 1; i < newarg.length; i++) {
					newString += newarg[i] + " ";
				}
				newString = newString + "* " + type;
				list.add(newString);
			} else {
				String newString = "* " + str;
				list.add(newString);
			}
		} else {
			String newString = "* " + str;
			list.add(newString);
		}
		return list;
	}

	public ArrayList<String> how_type_agequestion(String[] arg) {
		ArrayList<String> list = new ArrayList<String>();
		String[] verb = { "VB", "VBD", "VBN", "VBP", "VBZ" };
		String[] noun = { "NNP", "PRP" };
		int verb_index = 0;
		boolean verb_flag = true;
		int noun_index = 0;
		boolean noun_flag = true;
		for (int i = 1; i < arg.length; i++) {
			if (Arrays.asList(verb).contains(getPos(arg[i])) && verb_flag) {
				verb_index = i;
				verb_flag = false;
			}
			if (Arrays.asList(noun).contains(getPos(arg[i])) && noun_flag) {
				noun_index = i;
				noun_flag = false;
			}
		}
		String newString = "";
		if (noun_index != 0) {
			for (int i = noun_index; i < arg.length; i++) {
				if (i == noun_index) {
					newString += arg[i] + " " + arg[verb_index] + " * ";
				} else {
					newString += arg[i] + " ";
				}
			}
		} else {
			for (int i = verb_index + 1; i < arg.length; i++) {
				newString += arg[i] + " ";
			}
			newString += arg[verb_index] + " " + "*";
		}
		list.add(newString.trim());
		return list;
	}

	public ArrayList<String> how_type_specialquestion(String[] arg) {
		ArrayList<String> list = new ArrayList<String>();
		String newString = "";
		for (int i = 1; i < arg.length; i++) {
			newString += arg[i] + " ";
		}
		newString += newString + "*";
		list.add(newString);
		return list;
	}

	public ArrayList<String> how_type_bequestion(String[] arg) {
		ArrayList<String> list = new ArrayList<String>();
		String[] verb_adj = { "VB", "VBD", "VBP", "VBZ", "VBN", "JJ" };
		int insert_index = -1;
		for (int i = 1; i < arg.length; i++) {
			if (Arrays.asList(verb_adj).contains(getPos(arg[i]))) {
				insert_index = i - 1;
				break;
			}
		}
		String newString = "";
		for (int i = 1; i < arg.length; i++) {
			if (i == insert_index) {
				newString += arg[i] + " " + arg[0] + " ";
			} else {
				newString += arg[i] + " ";
			}
		}
		newString = newString + "*";
		list.add(newString);
		return list;
	}

	public ArrayList<String> how_type_howlongquestion(String[] arg) {
		ArrayList<String> list = new ArrayList<String>();
		String[] verb = { "VB", "VBD", "VBN", "VBP", "VBZ" };
		String newString = "";
		int insert_index = -1;
		for (int i = 2; i < arg.length; i++) {
			if (Arrays.asList(verb).contains(getPos(arg[i]))) {
				insert_index = i - 1;
				break;
			}
		}
		for (int i = 2; i < arg.length; i++) {
			if (i == insert_index) {
				newString += arg[i] + " " + arg[1] + " ";
			} else {
				newString += arg[i] + " ";
			}
		}
		newString += "*";
		list.add(newString);
		return list;
	}

	public ArrayList<String> how_type_general(String[] arg) {
		ArrayList<String> list = new ArrayList<String>();
		String newString = "";
		for (int i = 0; i < arg.length; i++) {
			newString += arg[i] + " ";
		}
		newString += "*";
		list.add(newString);
		return list;
	}

	public ArrayList<String> how_type_process(String str) {
		ArrayList<String> list = new ArrayList<String>();
		String[] arg = str.split(" ");
		if (arg.length > 0) {
			/***
			 * how+many/much
			 */
			if (arg[0].equals("much") || arg[0].equals("many")) {
				list = how_type_numberquestion(arg);
			}
			/***
			 * how+old
			 */
			else if (arg[0].equals("old")) {
				list = how_type_agequestion(arg);
			}
			/***
			 * how+do/does/did
			 */
			else if (arg[0].equals("do") || arg[0].equals("does") || arg[0].equals("did")) {
				list = how_type_specialquestion(arg);
			}
			/**
			 * how+is/are/was/were
			 */
			else if (arg[0].equals("is") || arg[0].equals("are") || arg[0].equals("was") || arg[0].equals("were")) {
				list = how_type_bequestion(arg);
			}
			/***
			 * how+long
			 */
			else if (arg[0].equals("long")) {
				list = how_type_howlongquestion(arg);
			} else {
				list = how_type_general(arg);
			}
		}
		return list;
	}

	public ArrayList<String> where_type_specialquestion(String[] arg) {
		ArrayList<String> list = new ArrayList<String>();
		String newString = "";
		int insert_index = -1;
		for (int i = 1; i < arg.length; i++) {
			if (getPos(arg[i]).equals("IN")) {
				insert_index = i - 1;
				break;
			}
		}
		if (insert_index != -1) {
			for (int i = 1; i < arg.length; i++) {
				if (i == insert_index) {
					newString += arg[i] + " * ";
				} else {
					newString += arg[i] + " ";
				}
			}
		} else {
			for (int i = 1; i < arg.length; i++) {
				newString += arg[i] + " ";
			}
			newString += "*";
		}
		list.add(newString.trim());
		return list;
	}

	public ArrayList<String> where_type_bequestion(String[] arg) {
		ArrayList<String> list = new ArrayList<String>();
		String[] noun = { "NNP", "PRP", "NN", "NNS" };
		int insert_index = -1;
		String newString = "";
		for (int i = 1; i < arg.length; i++) {
			if (Arrays.asList(noun).contains(getPos(arg[i - 1])) && getPos(arg[i]).equals("WRB")) {
				insert_index = i - 1;
				break;
			}
		}
		if (insert_index != -1) {
			for (int i = 1; i < arg.length; i++) {
				if (i == insert_index) {
					newString += arg[i] + " " + arg[0] + " " + "* ";
				} else {
					newString += arg[i] + " ";
				}
			}
		} else {
			for (int i = 1; i < arg.length; i++) {
				newString += arg[i] + " ";
			}
			newString += arg[0] + " *";
		}
		list.add(newString.trim());
		return list;
	}

	public ArrayList<String> where_type_general(String[] arg) {
		ArrayList<String> list = new ArrayList<String>();
		String newString = "";
		String[] array = { "MD", "VB", "VBN" };
		String[] verb = { "VB", "VBD", "VBN", "VBP", "VBZ" };
		int verb_index = -1;
		boolean flag = true;
		if (Arrays.asList(array).contains(getPos(arg[0]))) {
			flag = false;
		}
		for (int i = 1; i < arg.length; i++) {
			if (Arrays.asList(verb).contains(getPos(arg[i]))) {
				verb_index = i - 1;
				break;
			}
		}
		if (flag) {
			for (int i = 0; i < arg.length; i++) {
				newString += arg[i] + " ";
			}
			newString += "*";
		} else {
			for (int i = 1; i < arg.length; i++) {
				if (i == verb_index) {
					newString += arg[i] + " " + arg[0] + " ";
				} else {
					newString += arg[i] + " ";
				}
			}
			newString += "*";

		}
		list.add(newString);
		return list;
	}

	public ArrayList<String> where_type_process(String str) {
		ArrayList<String> list = new ArrayList<String>();
		String[] arg = str.split(" ");
		if (arg.length > 0) {
			if (arg[0].equals("do") || arg[0].equals("does") || arg[0].equals("did")) {
				list = where_type_specialquestion(arg);
			} else if (arg[0].equals("is") || arg[0].equals("was") || arg[0].equals("are") || arg[0].equals("were")) {
				list = where_type_bequestion(arg);
			} else {
				list = where_type_general(arg);
			}
		}
		return list;
	}

	public ArrayList<String> who_type_bequestion(String[] arg) {
		ArrayList<String> list = new ArrayList<String>();
		String newString1 = "";
		String newString2 = "";
		for (int i = 0; i < arg.length; i++) {
			newString1 += arg[i] + " ";
		}
		for (int i = 1; i < arg.length; i++) {
			newString2 += arg[i] + " ";
		}
		newString1 = "* " + newString1;
		newString2 = newString2 + arg[0] + " *";
		list.add(newString1.trim());
		list.add(newString2.trim());
		return list;
	}

	public ArrayList<String> who_type_specialquestion(String[] arg) {
		ArrayList<String> list = new ArrayList<String>();
		String newString = "";
		int insert_index = -1;
		for (int i = 1; i < arg.length; i++) {
			if (getPos(arg[i]).equals("IN")) {
				insert_index = i;
				break;
			}
		}
		if (insert_index != -1) {
			for (int i = 1; i < arg.length; i++) {
				if (i == insert_index) {
					newString += arg[i] + " * ";
				} else {
					newString += arg[i] + " ";
				}
			}
		} else {
			for (int i = 0; i < arg.length; i++) {
				newString += arg[i] + " ";
			}
			newString = "* " + newString;
		}
		list.add(newString.trim());
		return list;
	}

	public ArrayList<String> who_type_general(String[] arg) {
		ArrayList<String> list = new ArrayList<String>();
		String newString = "";
		for (int i = 0; i < arg.length; i++) {
			newString += arg[i] + " ";
		}
		newString = "* " + newString;
		list.add(newString.trim());
		return list;
	}

	public ArrayList<String> who_type_process(String str) {
		ArrayList<String> list = new ArrayList<String>();
		String[] arg = str.split(" ");
		if (arg.length > 0) {
			if (arg[0].equals("is") || arg[0].equals("was") || arg[0].equals("are") || arg[0].equals("were")) {
				list = who_type_bequestion(arg);
			} else if (arg[0].equals("do") || arg[0].equals("does") || arg[0].equals("did")) {
				list = who_type_specialquestion(arg);
			} else {
				list = who_type_general(arg);
			}
		}
		return list;
	}

	public ArrayList<String> whose_type_process(String str) {
		ArrayList<String> list = new ArrayList<String>();
		String[] arg = str.split(" ");
		if (arg.length > 0) {
			String newString = "";
			for (int i = 0; i < arg.length; i++) {
				newString += arg[i] + " ";
			}
			newString = "* " + newString;
			list.add(newString.trim());
		}
		return list;
	}

	public ArrayList<String> why_type_specialquestion(String[] arg) {
		ArrayList<String> list = new ArrayList<String>();
		String newString = "";
		for (int i = 1; i < arg.length; i++) {
			newString += arg[i] + " ";
		}
		newString = newString + "*";
		list.add(newString);
		return list;
	}

	public ArrayList<String> why_type_general(String[] arg) {
		ArrayList<String> list = new ArrayList<String>();
		String newString = "";
		int insert_index = -1;
		String[] verb = { "VB", "VBD", "VBN", "VBP", "VBZ" };
		for (int i = 1; i < arg.length; i++) {
			if (Arrays.asList(verb).contains(getPos(arg[i]))) {
				insert_index = i - 1;
				break;
			}
		}
		if (insert_index != -1) {
			for (int i = 1; i < arg.length; i++) {
				if (i == insert_index) {
					newString += arg[i] + " " + arg[0] + " ";
				} else {
					newString += arg[i] + " ";
				}
			}
			newString += "*";
		} else {
			for (int i = 1; i < arg.length; i++) {
				newString += arg[i] + " ";
			}
			newString += "*";
		}
		list.add(newString);
		return list;
	}

	public ArrayList<String> why_type_process(String str) {
		ArrayList<String> list = new ArrayList<String>();
		String[] arg = str.split(" ");
		if (arg.length > 0) {
			if (arg[0].equals("does") || arg[0].equals("did") || arg[0].equals("do")) {
				list = why_type_specialquestion(arg);
			} else {
				list = why_type_general(arg);
			}
		}
		return list;
	}

	public String getPos(String txtWord) {
		// txtWord = txtWord.replaceAll("[\\p{Punct}]", "");
		Properties props = new Properties();
		props.put("annotators", "tokenize,ssplit,pos, lemma");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		Annotation document = new Annotation(txtWord);
		pipeline.annotate(document);
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		String originSentense = "";
		for (CoreMap sentence : sentences) {
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				String pos = token.get(PartOfSpeechAnnotation.class);
				originSentense += pos + "\t";
			}
		}
		originSentense = originSentense.substring(0, originSentense.length() - 1);
		return originSentense;
	}

}
