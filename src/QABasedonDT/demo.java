package QABasedonDT;

public class demo {
	public static void main(String args[]) throws Exception{
		execute_train();
	}
	
	public static void execute_train(){
		QABasedonDT testv = new QABasedonDT("./src/res/corpus/mc500.train.txt", 300);
		try {
			testv.train("log9.txt");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void execute_test(){
		QABasedonDT testv = new QABasedonDT("./src/res/corpus/mc500.test.txt", 150);
		try {
			testv.test("/home/lcr/workspace/ReadingComprehension/src/res/Log/log8.txt");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
