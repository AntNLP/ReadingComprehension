package Test;

public class main {
	static final String path = "/home/lcr/workspace/ReadingComprehension/src/res/test/mc500.test.txt";
	static final int size = 150;
	public static void main(String args[]) throws Exception{
		long startMili = System.currentTimeMillis();
		DataTest dataTest = new DataTest(path, size);
		dataTest.evaluate();                                                                                
		long endMili = System.currentTimeMillis();
		long second = (startMili - endMili)/1000;
		System.out.println("总耗时为："+second+"秒");                                                                                                                                                                                                                                                                                                                                                                 
	}
}
                                                                                                                                                                                                                                                                                      
