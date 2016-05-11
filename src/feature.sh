#~/bin/bash
str=~/tools/libsvm-3.20/java/libsvm.jar:~/tools/stanford-corenlp-full-2015-04-20/stanford-corenlp-3.5.2.jar:~/tools/stanford-corenlp-full-2015-04-20/stanford-corenlp-3.5.2-models.jar:~/tools/stanford-parser-full-2013-06-20/stanford-parser.jar:~/tools/stanford-parser-full-2013-06-20/stanford-parser-3.2.0-models.jar
cd FeatureExtraction
javac -cp $str *.java


