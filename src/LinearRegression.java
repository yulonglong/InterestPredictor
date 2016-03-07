import java.io.IOException;
import java.util.ArrayList;

import Jama.Matrix;

import Utility.IO;
import Utility.KroneckerOperation;
import Utility.Ranking;

/**
 * The implementation of the linear regression.
 * @author wang xiang, xiangwang1223@gmail.com;
 *
 */
public class LinearRegression {
	Matrix X_train;
	Matrix Y_train;
	Matrix X_test;
	Matrix Y_test;
	Matrix Y_pred;
	
	int feature_dim;
	int class_dim;

	/*
	 * The parameter lambda controls the weight of the regularization term;
	 * Please modify its value by yourselves.
	 */
	double lambda = 0.1;
	
	/*
	 * The required learned coefficient matrix W; 
	 */
	Matrix W;
	
	/**
	 * The constructor of Linear Regression;
	 * @param fold: the path of input file;
	 */
	public LinearRegression(String fold){
		String X_train_path = fold + "/train.txt";
		String Y_train_path = fold + "/gnd_train.txt";
		
		String X_test_path = fold + "/test.txt";
		String Y_test_path = fold + "/gnd_test.txt";
		
		X_train = new Matrix(loadMX(X_train_path));
		Y_train = new Matrix(loadMX(Y_train_path));
		X_test = new Matrix(loadMX(X_test_path));
		Y_test = new Matrix(loadMX(Y_test_path));
		
		feature_dim = X_train.getColumnDimension();
		class_dim = Y_train.getColumnDimension();
		
		W = new Matrix(feature_dim, class_dim);
	}
	
	
	/**
	 * The function for loading matrix from the file;
	 * @param path: the path of input file.
	 * @return mx: the corresponding matrix.
	 */
	public double[][] loadMX(String path){
		ArrayList<String> content = new ArrayList<String>();
		try {
			content = IO.FileLoad(path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		double[][] mx = new double[content.size()][];
		for(int i=0;i<mx.length;i++){
			String line = content.get(i);
			String[] value = line.split(",");
			double[] vector = new double[value.length];
			for(int j=0;j<value.length;j++){
				vector[j] = Double.valueOf(value[j]);
			}
			mx[i] = vector;
		}
		return mx;
	}
	
	/**
	 * The function for training your model to obtain the coefficient matrix W;
	 * The details of the linear regression are summarized into the document.
	 */
	public void training(){
		Matrix I_class = Matrix.identity(class_dim, class_dim);
		Matrix I_feature = Matrix.identity(feature_dim, feature_dim);
		
		Matrix XY = X_train.transpose().times(Y_train);
		Matrix XX = X_train.transpose().times(X_train).plus(I_feature.times(lambda));
		
		Matrix XY_vec = new Matrix(class_dim*feature_dim, 1);
		
		/*
		 * Reshape the matrix to the corresponding vector version;
		 */
		for(int i = 0; i < class_dim; i++){
			XY_vec.setMatrix(i*feature_dim, (i+1)*feature_dim-1, 0, 0, XY.getMatrix(0, feature_dim-1, i, i));
		}
		
		double[][] XX_vec = KroneckerOperation.product(I_class.getArray(), XX.getArray());
		Matrix W_vec = new Matrix(XX_vec);
		W_vec = W_vec.times(XY_vec);
		
		/*
		 * Reshape the vector to the corresponding matrix version;
		 */
		for(int i = 0; i < class_dim; i++){
			W_vec.getMatrix(i*feature_dim, (i+1)*feature_dim - 1, 0, 0);
			W.setMatrix(0, feature_dim-1, i, i, W_vec.getMatrix(i*feature_dim, (i+1)*feature_dim - 1, 0, 0));
		}
		
		System.out.println("Training Done.");
	}
	
	/**
	 * The function for testing your learned W to obtain the predicted label matrix Y_pred;
	 * The details of the linear regression are summarized into the document.
	 */
	public void testing(){
		Y_pred = X_test.times(W);
		System.out.println("Testing Done.");
	}
	
	/**
	 * The function for evaluating your predicted Y_pred with the comparison to the ground truth Y_test;
	 * Here we only provide the metric S@K as an example, which is summarized into the document.
	 * Please modify or create your own evaluation functions.
	 * @param k: the precision or recall at the top k positions.
	 */
	public void evaluating(int k){
		Ranking rk = new Ranking();
		
		double[][] y_pred = Y_pred.getArray();
		double[][] y_test = Y_test.getArray();
		double sum_sk = 0.0;
		double acc_sk = 0.0;
		
		for(int n = 0; n < Y_pred.getRowDimension(); n++){
			sum_sk += 1.0;
			
			double[] i_y_pred = y_pred[n];
			int[] i_y_pred_sorted = rk.rank(i_y_pred);
			
			double[] i_y_test = y_test[n];
			
			boolean flg = false;
			for(int i = 0; i < k; i ++){
				int pred_index = i_y_pred_sorted[i];
				if(i_y_test[pred_index] != 0){
					flg = true;
				}
			}
			if(flg){
				acc_sk += 1.0;
			}
		}
		
		double s_k = acc_sk / sum_sk;
		System.out.printf("Evaluating Done. S@%d=%f\n",k, s_k);
	}
	
	public static void main(String[] args){
		LinearRegression LR = new LinearRegression("D:/Developer/Multi-view/baseline/EarlyFusion/EarlyFussion/SVM_Data");
		LR.training();
		LR.testing();
		for(int k = 1; k <=10; k++){
			LR.evaluating(k);
		}
	}
}
