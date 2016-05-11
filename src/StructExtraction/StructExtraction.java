package StructExtraction;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;

public class StructExtraction {
	public static void main(String args[]) {
		String str = "Who would feel left out if they couldnt come to the party ";
		extract(str);
	}

	public static String[] extract(String str) {
		Word[] word = parse(str);
		String strategy = getStrategy(word);
		String[] struct = getStruct(word, strategy);
		String subj = struct[0];
		String predicate = struct[1];
		String obj = struct[2];
		if(subj.equals("")){
			subj = "NULL";
		}
		if(predicate.equals("")){
			predicate = "NULL";
		}
		if(obj.equals("")){
			obj = "NULL";
		}
		String[] arg_struct = { subj, predicate, obj };
		System.out.println("[subj:" + subj + "]" + "\t" + "[predicate:" + predicate + "]" + "\t" + "[obj:" + obj + "]");
		return arg_struct;
	}

	public static String getStrategy(Word[] word) {
		String strategy = "";
		for (int i = 0; i < word.length; i++) {
			if (word[i].dependency_label.equals("root")) {
				if (word[i].postag.equals("VERB")) {
					strategy = "subj_do_obj";
					break;
				} else if (word[i].postag.equals("PRON")||word[i].postag.equals("PROPN")) {
					strategy = "is_a";
					break;
				}
				else if(word[i].postag.equals("ADJ")){
					strategy = "adj_root";
					break;
				}
				else {
					strategy = "others";
				}
			}
		}
		return strategy;
	}

	public static String[] getStruct(Word[] word, String strategy) {
		String subj = "";
		String predicate = "";
		String obj = "";
		if (strategy.equals("subj_do_obj")) {
			predicate = getPredicate_doStruct(word);
			subj = getSubject_doStruct(word);
			obj = getObject_doStruct(word);
		} else if (strategy.equals("is_a")) {
			for (int i = 0; i < word.length; i++) {
				if (word[i].dependency_label.equals("root")) {
					obj = word[i].content;
					for (Word children : word[i].children) {
						if (children.dependency_label.equals("cop") && children.postag.equals("VERB")) {
							predicate = children.content;
						}
						if (children.dependency_label.equals("nsubj")||children.dependency_label.equals("nsubjpass")) {
//							for (Word nsubj_child : children.children) {
//								subj += nsubj_child.content + " ";
//							}
							subj += children.content;
						}
					}
				}
			}
		}else if(strategy.equals("adj_root")) {
			for(int i = 0;i < word.length;i++){
				if(word[i].dependency_label.equals("root")){
					obj = word[i].content;
					for (Word children : word[i].children) {
						if (children.dependency_label.equals("cop") ) {
							predicate = children.content;
						}
						if (children.dependency_label.equals("nsubj")||children.dependency_label.equals("nsubjpass")) {
//							for (Word nsubj_child : children.children) {
//								subj += nsubj_child.content + " ";
//							}
							subj += children.content;
						}
					}
				}
			}
		}
		
		else {
			for(int i = 0;i < word.length;i++){
				if(word[i].dependency_label.equals("root")){
					subj = word[i].content;
					for (Word children : word[i].children) {
						if (children.postag.equals("VERB")) {
							predicate += children.content;
						}
						if (children.dependency_label.equals("dobj")) {
//							for (Word nsubj_child : children.children) {
//								obj += nsubj_child.content + " ";
//							}
							obj += children.content;
						}
					}
				}
			}
		}
		String[] struct = { subj, predicate, obj };
		return struct;
	}

	public static String getPredicate_doStruct(Word[] word) {
		String predicate = "";
		for (int i = 0; i < word.length; i++) {
			if (word[i].dependency_label.equals("root")) {
				predicate += word[i].content + " ";
				// for (Word child : word[i].children) {
				// if(child.postag.equals("ADJ")||child.postag.equals("VERB")){
				// predicate += child.content;
				// }
				// }
			}
		}
		return predicate.trim();
	}

	public static String getSubject_doStruct(Word[] word) {
		String subj = "";
		for (int i = 0; i < word.length; i++) {
			if (word[i].dependency_label.equals("nsubj") ||word[i].dependency_label.equals("nsubjpass")&& word[i].parent.dependency_label.equals("root")) {
				ArrayList<String> subjlist = new ArrayList<>();
				int index = 0;
				subjlist.add(word[i].content);
//				for (Word child : word[i].children) {
//					if(child.index < word[i].index){
//						subjlist.add(index, child.content);
//						index++;
//					}else{
//						subjlist.add(child.content);
//					}
//				}
				for (String string : subjlist) {
					subj += string+" ";
				}
				break;
			}
		}
		return subj.trim();
	}

	public static String getObject_doStruct(Word[] word) {
		String obj = "";
		for (int i = 0; i < word.length; i++) {
			if( (word[i].dependency_label.equals("dobj")) && (word[i].parent.dependency_label.equals("root")
					|| word[i].parent.parent.dependency_label.equals("root"))) {
				ArrayList<String> objlist = new ArrayList<>();
				int index = 0;
				objlist.add(word[i].content);
//				for (Word child : word[i].children) {
//					if(child.index < word[i].index){
//						objlist.add(index, child.content);
//						index++;
//					}else{
//						objlist.add(child.content);
//					}
//				}
				for (String string : objlist) {
					obj += string+" ";
				}
				break;
			}
		}
		if (obj.equals("")) {
			for (int i = 0; i < word.length; i++) {
				if (word[i].postag.equals("NOUN") && word[i].parent.dependency_label.equals("root")
						&& word[i].index < word[i].parent.index&&!word[i].dependency_label.equals("nsubj")) {
//					for (Word child : word[i].children) {
//						obj += child.content + " ";
//					}
					obj += word[i].content;
				}
			}
		}
		if (obj.equals("")) {
			for (int i = 0; i < word.length; i++) {
				if ((word[i].postag.equals("ADV") || word[i].postag.equals("PRON"))
						&& !word[i].dependency_label.equals("nsubj")&&!word[i].dependency_label.equals("nsubjpass")) {
					obj = word[i].content;
					break;
				}
			}
		}
		return obj.trim();
	}

	public static Word[] parse(String str) {
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
		Word[] word = new Word[arg.length];
		for (int i = 0; i < word.length; i++) {
			word[i] = new Word();
		}
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
			word[i].content = temp[1];
			word[i].index = i + 1;
			word[i].postag = temp[3];
			word[i].dependency_label = temp[7];
			if (Integer.valueOf(temp[6]) - 1 >= 0) {
				word[i].parent = word[Integer.valueOf(temp[6]) - 1];
			} else {
				word[i].parent = null;
			}
			if (word[i].parent != null) {
				word[i].parent.children.add(word[i]);
			}
		}
		for (int i = 0; i < outPut.length; i++) {
			System.out.println(outPut[i]);
		}

		// for(int i = 0;i < word.length;i++){
		// System.out.println(word[i].content+"\t"+"\t"+word[i].postag+"\t"+word[i].dependency_label+"\t"+word[i].index+"\t"+word[i].parent);
		// }
		return word;
	}
}
