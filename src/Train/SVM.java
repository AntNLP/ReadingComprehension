package Train;

import java.io.IOException;

public class SVM {
	public static void main(String args[]) throws IOException{
		String[] arg = {"./src/res/trainingdata/train_feature_13.txt","./src/res/model/train_model_13.txt"};
		svm_train train  = new svm_train();
		train.main(arg);
	}
}
