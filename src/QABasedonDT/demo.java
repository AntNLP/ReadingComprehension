package QABasedonDT;

public class demo {
	public static void main(String args[]){
		QABasedonDT testv = new QABasedonDT("./src/res/corpus/test.txt", 10);
		testv.train();
	}
}
