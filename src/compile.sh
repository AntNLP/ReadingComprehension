#!/bin/bash
str=~/tools/libsvm-3.20/java/libsvm.jar:~/tools/stanford-corenlp-full-2015-04-20/stanford-corenlp-3.5.2.jar:~/tools/stanford-corenlp-full-2015-04-20/stanford-corenlp-3.5.2-models.jar:~/tools/stanford-parser-full-2013-06-20/stanford-parser.jar:~/tools/stanford-parser-full-2013-06-20/stanford-parser-3.2.0-models.jar:~/tools/JWSCodebeta/edu.mit.jwi_2.1.4.jar:~/tools/JWSCodebeta/edu.sussex.nlp.jws.beta.11.jar
cd Corpus
javac -cp $str:.. -d ../../compilefile *.java
cd ..

cd FeatureExtraction
javac -cp $str:.. -d ../../compilefile *.java
cd ..

cd PPDB
javac -cp $str:.. -d ../../compilefile *.java
cd ..

cd Tools
javac -cp $str:.. -d ../../compilefile *.java
cd ..

cd Word2Vec
javac -cp $str:.. -d ../../compilefile *.java
cd ..

cd QABasedonDT
javac -cp $str:.. -d ../../compilefile *.java
cd ..
