package SVM;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;
import Misc.GlobalHelper;
import Misc.Parameter;
import Misc.TTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.io.*;

import Jama.Matrix;
import Utility.IO;
import Utility.Ranking;

import Wrapper.Tweet;

public class Main {
	String relativePath = GlobalHelper.pathToSVMData+"/";
	int numLabels = 20;
	ArrayList<Hashtable<Integer, Boolean>> TRU = new ArrayList<Hashtable<Integer, Boolean>>();

	public Main() {
		double[][] gnd_valid = TTest.ReadMx(this.relativePath + "gnd_test.csv");
		for (int n = 0; n < gnd_valid.length; n++) {
			Hashtable<Integer, Boolean> intrest = new Hashtable<Integer, Boolean>();
			for (int r = 0; r < gnd_valid[n].length; r++) {
				if (gnd_valid[n][r] > 0.99)
					intrest.put(r, true);
			}
			TRU.add(intrest);
		}
	}

	public double[][] SVM_Late_Process(String source, boolean earlyFusion) throws IOException {
		System.err.println("Starting SVM " + source);
		System.err.println("Please wait patiently..");
		svm.svm_set_print_string_function(new libsvm.svm_print_interface(){
		    @Override public void print(String s) {} // Disables svm output
		});
		
		String relativePath = this.relativePath;
		
		svm_node[][] X_ori;
		svm_node[][] X_test;
		double[][] Y;
		
		if (earlyFusion){
			svm_node[][] X1 = loadX(relativePath + "twitter_train.csv");
			svm_node[][] X2 = loadX(relativePath + "linkedin_train.csv");
			svm_node[][] X3 = loadX(relativePath + "facebook_train.csv");
			Y = loadY(relativePath + "gnd_train.csv");
			svm_node[][] X1_test = loadX(relativePath + "twitter_test.csv");
			svm_node[][] X2_test = loadX(relativePath + "linkedin_test.csv");
			svm_node[][] X3_test = loadX(relativePath + "facebook_test.csv");
			X_ori = MergeX(X1, X2);
			X_ori = MergeX(X_ori, X3);
			X_test = MergeX(X1_test, X2_test);
			X_test = MergeX(X_test, X3_test);
		}
		else {
			X_ori = loadX(relativePath + source + "_train.csv");
			Y = loadY(relativePath + "gnd_train.csv");
			X_test = loadX(relativePath + source + "_test.csv");
		}

		double[][] score = new double[X_test.length][numLabels];
			
		for(int i = 0;i<numLabels;i++){
			//	Get the binary class for each label (out of the 20 label)
			double[] currY_ori = new double[X_ori.length];
			int numZero = 0;
			int numOne = 0;
			for(int j=0;j<X_ori.length;j++){
				currY_ori[j] = Y[j][i];
				if (currY_ori[j] > 0.99) numOne++;
				else numZero++;
			}
			
			// Over sample to make both classes balanced
			int maxNum = Math.max(numZero, numOne);
			svm_node[][] X = new svm_node[maxNum*2][X_ori[0].length];
			double[] currY = new double[maxNum*2];
			int currIndex = 0;
			for(currIndex=0;currIndex<X_ori.length;currIndex++){
				X[currIndex] = X_ori[currIndex];
				currY[currIndex] = currY_ori[currIndex];
			}
			
			while ((numZero != numOne) && (currIndex < maxNum*2)) {
				for(int j=0;j<X_ori.length;j++){
					if (currIndex >= maxNum*2) break;
					if ((numZero > numOne) && (currY_ori[j] > 0.99)) {
						X[currIndex] = X_ori[j];
						currY[currIndex] = currY_ori[j];
						currIndex++;
						numOne++;
					}
					else if ((numOne > numZero) && (currY_ori[j] < 0.01)){
						X[currIndex] = X_ori[j];
						currY[currIndex] = currY_ori[j];
						currIndex++;
						numZero++;
					}
					else if (numOne == numZero) {
						break;
					}
				}
			}
			
//			System.out.println(currIndex + " = " + numZero + " + " + numOne);
	
			svm_problem problem = new svm_problem();
			problem.l = X.length;
			problem.x = X;
			problem.y = currY; 
	
			
			svm_parameter param = new svm_parameter();
			param.svm_type = svm_parameter.C_SVC;
			param.kernel_type = svm_parameter.RBF;
			param.degree = 3;
			param.gamma = 0.5; 
			param.nu = 0.5;
			param.cache_size = 1000;
			param.C = 1;
			param.eps = 1e-5;
			param.p = 0.1;
			param.shrinking = 1;
			param.probability = 1;
			param.nr_weight = 0;
	
	//		System.out.println(svm.svm_check_parameter(problem, param)); 
			svm_model model = svm.svm_train(problem, param); 
			double[][] currProbTest = Evaluate_Late1(model, X_test);
			
//			System.out.println("currProbTest "+currProbTest.length);
			for(int j=0;j<X_test.length;j++){
				score[j][i] += currProbTest[j][1];
			}
			System.err.println("SVM : "+(i+1)+" out of 20");
		}
		return score;
		
	}

	public double[][] Evaluate_Late1(svm_model model, svm_node[][] X) {
		double[][] prob = new double[X.length][];
		int n_label = svm.svm_get_nr_sv(model);
//		System.out.println("SVM Label "+n_label);
		int[] label = new int[n_label];
		for (int i = 0; i < X.length; i++) {
			double[] probi = new double[n_label];
//			System.out.println("Test row");
//			for(int j=0;j<X[i].length;j++){
//				System.out.print(X[i][j].value +",");
//			}
//			System.out.println();
			svm.svm_predict_probability(model, X[i], probi);
			prob[i] = probi;
//			System.out.println("Probability");
//			System.out.println(Arrays.toString(probi));
			
		}
		return prob;
	}

	public void Evaluate_Late2(double[][] mx1, double[][] mx2, double[][] mx3, int k,
			ArrayList<Hashtable<Integer, Boolean>> truth, Value v) {
		double l1 = 0.9;
		double l2 = 0.3;
		double l3 = 0.6;
		double[][] mx = new double[mx1.length][mx1[0].length];
		for (int i = 0; i < mx.length; i++) {
			for (int j = 0; j < mx[0].length; j++) {
				mx[i][j] = (l1 * mx1[i][j] +l2 *  mx2[i][j] +l3 *  mx3[i][j]) / 3.0;
			}
		}
		double sum_sk = 0.0;
		double sum_pk = 0.0;
		double acc_sk = 0.0;
		double acc_pk = 0.0;
		Ranking rk = new Ranking();
		for (int n = 0; n < mx.length; n++) {
			sum_sk += 1.0;
			sum_pk += k;
			double[] row = mx[n];
			int[] list = rk.rank(row);
			Hashtable<Integer, Boolean> interest = truth.get(n);
			boolean flag = false;
			for (int i = 0; i < k; i++) {
				if (interest.containsKey(list[i])) {
					acc_pk += 1.0;
					flag = true;
				}
			}
			if (flag) {
				acc_sk += 1.0;
			}
		}
//		Value v = new Value();
		v.pk = acc_pk / sum_pk;
		v.sk = acc_sk / sum_sk;
//		System.out.println("P@" + k + "\t" + v.pk);
//		System.out.println("S@" + k + "\t" + v.sk);
	}

	public void Evaluate_Early(String source, double[][] mx, int k, ArrayList<Hashtable<Integer, Boolean>> truth,Value v) {
		PrintWriter pw = null;
		try { pw = new PrintWriter(GlobalHelper.pathToProcessed+"/"+source+"_probability.txt");}
		catch (Exception e) { e.printStackTrace(); }
		
		double sum_sk = 0.0;
		double sum_pk = 0.0;
		double sum_rk = 0.0;
		double acc_sk = 0.0;
		double acc_pk = 0.0;
		double acc_rk = 0.0;
		Ranking rk = new Ranking();
		for (int n = 0; n < mx.length; n++) {
			Hashtable<Integer, Boolean> interest = truth.get(n);
			double curr_rk = 0;
			// find the number of correct interest;
			for(int i=0;i<GlobalHelper.numClasses;i++) {
				if (interest.containsKey(i)) {
					curr_rk += 1.0;
				}
			}
			
			sum_rk += curr_rk;
			sum_sk += 1.0;
			sum_pk += k;
			double[] row = mx[n];
			pw.println("Test probability row " + n + " : " + Arrays.toString(row));
			int[] list = rk.rank(row);
			
			boolean flag = false;
			for (int i = 0; i < k; i++) {
				if (interest.containsKey(list[i])) {
					acc_pk += 1.0;
					acc_rk += 1.0;
					flag = true;
				}
			}
			if (flag) {
				acc_sk += 1.0;
			}
			pw.flush();
		}
//		Value v = new Value();
		v.pk = acc_pk / sum_pk;
		v.sk = acc_sk / sum_sk;
		v.rk = acc_rk / sum_rk;
//		System.out.println("P@" + k + "\t" + v.pk);
//		System.out.println("S@" + k + "\t" + v.sk);
		pw.close();
	}

	public svm_node[][] loadX(String path) throws IOException {
		ArrayList<String> content = IO.FileLoad(path);
		svm_node[][] p = new svm_node[content.size()][];
		for (int i = 0; i < content.size(); i++) {
			String line = content.get(i);
			String[] terms = line.split(",");
			svm_node[] pi = new svm_node[terms.length];
			
			for (int j = 0; j < terms.length; j++) {
				svm_node pij = new svm_node();
				pij.index = j;
				pij.value = Double.valueOf(terms[j]);
				pi[j] = pij;
			}
			p[i] = pi;
		}
		return p;
	}

	public svm_node[][] MergeX(svm_node[][] X1, svm_node[][] X2) {
		if (X1.length != X2.length) {
			System.out.println("error");
		}
		svm_node[][] X = new svm_node[X1.length][];
		for (int i = 0; i < X.length; i++) {
			svm_node[] X1i = X1[i];
			svm_node[] X2i = X2[i];
			svm_node[] Xi = new svm_node[X1i.length + X2i.length];
			for (int j = 0; j < X1i.length; j++) {
				Xi[j] = X1i[j];
			}
			int idx = X1i.length;
			for (int j = 0; j < X2i.length; j++) {
				int idxj = idx + j;
				Xi[idxj] = X2i[j];
			}
			X[i] = Xi;
		}
		return X;
	}

	public double[][] loadY(String path) throws IOException {
//		System.out.println("Test File");
		ArrayList<String> content = IO.FileLoad(path);
		double[][] Y = new double[content.size()][];
		for (int i = 0; i < content.size(); i++) {
			String line = content.get(i);
			String[] terms = line.split(",");
			double[] Yi = new double[terms.length];
			for (int j = 0; j < terms.length; j++) {
				Yi[j] = Double.valueOf(terms[j]);
			}
			Y[i] = Yi;
//			System.out.println(Arrays.toString(Y[i]));
		}
		return Y;
	}

	public static void main(String[] args) throws IOException {
//		Misc.Main.folderCheck();
//		Misc.Main.processTwitter(false);
//		Misc.Main.processLinkedIn(false);
//		Misc.Main.processFacebook(false);
		
		PrintWriter pw = new PrintWriter(GlobalHelper.pathToProcessed+"/result_log.txt");
		pw.println("K\tEarly-P\tEarly-R\tEarly-S\tFB-P\tFB-R\tFB-S\tLI-P\tLI-R\tLI-S\tTW-P\tTW-R\tTW-S\tLate-P\tLate-R\tLate-S");
		
		Main t = new Main();
		
		double[][] result = t.SVM_Late_Process("early-fusion", true);
		double[][] fbResult = t.SVM_Late_Process("facebook", false);
		double[][] linkedinResult = t.SVM_Late_Process("linkedin", false);
		double[][] twitterResult = t.SVM_Late_Process("twitter", false);
		
		int maxK = 10;
		
		double[] totalScore = new double[15];
		int scoreIndex = 0;
		for(int i=1;i<=maxK;i++) {
			pw.print(i);
			Value v = new Value();
			scoreIndex = 0;
			
			t.Evaluate_Early("EarlyFusion", result, i, t.TRU, v);
			pw.print("\t"+String.format( "%.3f", v.pk )+"\t"+String.format( "%.3f", v.rk )+"\t"+String.format( "%.3f", v.sk ));
			totalScore[scoreIndex++] += v.pk; totalScore[scoreIndex++] += v.rk; totalScore[scoreIndex++] += v.sk;
			
			t.Evaluate_Early("FB", fbResult, i, t.TRU, v);
			pw.print("\t"+String.format( "%.3f", v.pk )+"\t"+String.format( "%.3f", v.rk )+"\t"+String.format( "%.3f", v.sk ));
			totalScore[scoreIndex++] += v.pk; totalScore[scoreIndex++] += v.rk; totalScore[scoreIndex++] += v.sk;
			
			t.Evaluate_Early("LI", linkedinResult, i, t.TRU, v);
			pw.print("\t"+String.format( "%.3f", v.pk )+"\t"+String.format( "%.3f", v.rk )+"\t"+String.format( "%.3f", v.sk ));
			totalScore[scoreIndex++] += v.pk; totalScore[scoreIndex++] += v.rk; totalScore[scoreIndex++] += v.sk;
			
			t.Evaluate_Early("TW", twitterResult, i, t.TRU, v);
			pw.print("\t"+String.format( "%.3f", v.pk )+"\t"+String.format( "%.3f", v.rk )+"\t"+String.format( "%.3f", v.sk ));
			totalScore[scoreIndex++] += v.pk; totalScore[scoreIndex++] += v.rk; totalScore[scoreIndex++] += v.sk;
			
			t.Evaluate_Late2(fbResult, linkedinResult, twitterResult, i, t.TRU, v);
			pw.print("\t"+String.format( "%.3f", v.pk )+"\t"+String.format( "%.3f", v.rk )+"\t"+String.format( "%.3f", v.sk ));
			totalScore[scoreIndex++] += v.pk; totalScore[scoreIndex++] += v.rk; totalScore[scoreIndex++] += v.sk;
			
			pw.println();
			pw.flush();
		}
		pw.print("Ave");
		for(int i=0;i<15;i++) {
			pw.print("\t"+String.format( "%.3f", totalScore[i]/(double)maxK ));
		}
		pw.println();
		pw.flush();
		pw.close();
	}
}

class Value {
	double pk = 0.0;
	double sk = 0.0;
	double rk = 0.0;
}