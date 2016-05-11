package Tools;

import java.io.PrintWriter;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import QABasedonDT.Word_Node;
import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.util.CoreMap;

public class Tools {
	static int info = 9999;
	LexicalizedParser lp;
	Properties props;
	StanfordCoreNLP pipeline;
	StanfordCoreNLP lemma_pipeline;
	
	public Tools(){
		parser_Initial();
		coreNLP_Initial();
		lemma_Initial();
	}
	
	public void parser_Initial(){
		lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
	}
	
	public void coreNLP_Initial(){
		pipeline = new StanfordCoreNLP();
	}
	
	public void lemma_Initial(){
		Properties props = new Properties();
		props.put("annotators", "tokenize,ssplit,pos, lemma");
		lemma_pipeline = new StanfordCoreNLP(props);
	}
	
	public String coreference(String str) {
		PrintWriter out;
		String[][] replaceData = new String[info][info];
		for (int i = 0; i < replaceData.length; i++) {
			for (int j = 0; j < replaceData[i].length; j++) {
				replaceData[i][j] = " ";
			}
		}

		// Initialize an Annotation with some text to be annotated. The text is
		// the argument to the constructor.
		Annotation annotation = new Annotation(str);
		StringBuilder sb = new StringBuilder(str);
		// run all the selected Annotators on this text
		pipeline.annotate(annotation);
		List<CoreMap> sentences = annotation
				.get(CoreAnnotations.SentencesAnnotation.class);
		if (sentences != null && !sentences.isEmpty()) {
			CoreMap sentence = sentences.get(0);
			Map<Integer, CorefChain> corefChains = annotation
					.get(CorefCoreAnnotations.CorefChainAnnotation.class);
			if (corefChains == null) {
				return " ";
			}
			int count = 0;
			for (Map.Entry<Integer, CorefChain> entry : corefChains.entrySet()) {
				// out.println("Chain " + entry.getKey() + " ");
				int count2 = 0;
				String name = "";
				for (CorefChain.CorefMention m : entry.getValue()
						.getMentionsInTextualOrder()) {
					// We need to subtract one since the indices count from 1
					// but the Lists start from 0
					List<CoreLabel> tokens = sentences.get(m.sentNum - 1).get(
							CoreAnnotations.TokensAnnotation.class);
					if (count2 == 0) {
						name = m.toString();
						name = name.split("\"")[1];
					} else {
						String temp = m.toString().toLowerCase();
						temp = temp.split("\"")[1];
						String[] arguments = { "i", "you", "he", "she", "me",
								"him", "her", "hers","us", "they", "them" ,"his","it","its","their","theirs"};
						if ((Arrays.asList(arguments).contains(temp))) {
							int start = tokens.get(m.startIndex - 1)
									.beginPosition();
							int end = tokens.get(m.endIndex - 2).endPosition();
							replaceData[start][end] = name;
						}
					}
					count2++;
				}
				count++;
			}
		}
		for (int i = info - 1; i >= 0; i--) {
			for (int j = info - 1; j >= 0; j--) {
				if (!replaceData[i][j].equals(" ")) {
					sb.replace(i, j, replaceData[i][j]);
				}
			}
		}
		return sb.toString();
	}
	
	public String getLemma(String sentense) {
		String originSentense = "";
		Annotation document = new Annotation(sentense);
		lemma_pipeline.annotate(document);
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		for (CoreMap sentence : sentences) {
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				String word = token.get(TextAnnotation.class);
				String lema = token.get(LemmaAnnotation.class);
				originSentense += lema + " ";
			}
		}
		if (originSentense.length() > 0) {
			originSentense = originSentense.substring(0,
					originSentense.length() - 1);
		}
		return originSentense;
	}
	
	public Word_Node[] parse(String str) {
		String[] outPut = new String[4];
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
		Word_Node[] word_Nodes = getTreeStruct(outPut);
		return word_Nodes;
	}
	
	public Word_Node[] getTreeStruct(String[] info){
		String[] word_conent = info[0].split("\t");
		String[] word_postag = info[1].split("\t");
		String[] word_syninfo = info[2].split("\t");
		String[] word_parentindex = info[3].split("\t");
		Word_Node[] word_node = new Word_Node[word_conent.length];
		for(int i = 0;i < word_node.length;i++){
			word_node[i] = new Word_Node(i);
		}
		for(int i = 0;i < word_node.length;i++){
			word_node[i].content = word_conent[i];
			word_node[i].postag = word_postag[i];
			word_node[i].syn_info = word_syninfo[i];
			int parentindex = Integer.parseInt(word_parentindex[i]);
			if(parentindex > 0 ){
				word_node[i].parent = word_node[parentindex - 1];
				word_node[parentindex - 1].childlist.add(word_node[i]);
			}
		}
		return word_node;
	}
}
