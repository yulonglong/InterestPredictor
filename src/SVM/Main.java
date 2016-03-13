package SVM;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;
import Misc.Parameter;
import Misc.TTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

import Jama.Matrix;
import Utility.IO;
import Utility.Ranking;

public class Main {
	String fold = "SVM_Data/";
	ArrayList<ArrayList<Hashtable<Integer, Boolean>>> TRU = new ArrayList<ArrayList<Hashtable<Integer, Boolean>>>();

	public Main() {
//		for (int k = 0; k < 10; k++) {
		int k = 0;
			ArrayList<Hashtable<Integer, Boolean>> truth = new ArrayList<Hashtable<Integer, Boolean>>();
			double[][] gnd_valid = TTest.ReadMx(this.fold + "gnd_valid_" + k + ".csv");
			for (int n = 0; n < gnd_valid.length; n++) {
				Hashtable<Integer, Boolean> intrest = new Hashtable<Integer, Boolean>();
				for (int r = 0; r < gnd_valid[0].length; r++) {
					if (gnd_valid[n][r] == 1)
						intrest.put(r, true);
				}
				truth.add(intrest);
			}
			this.TRU.add(truth);
//		}
	}

	public double[][] SVM_Late_Process(int k, String source) throws IOException {
		String fold = this.fold;
		svm_node[][] X = loadX(fold + source + "_train_" + k + "_new.csv");
		double[] Y = loadY(fold + "gnd_train_" + k + "_new.csv");
		svm_node[][] X_test = loadX(fold + source + "_valid_" + k + ".csv");

		if (source.equals("twit")) {
			svm_node[][] X2 = loadX(fold + "twit_top_train_" + k + "_new.csv");
			svm_node[][] X2_test = loadX(fold + "twit_top_valid_" + k + ".csv");
			X = MergeX(X, X2);
			X_test = MergeX(X_test, X2_test);
		}

		svm_problem problem = new svm_problem();
		problem.l = X.length;
		problem.x = X;
		problem.y = Y; 

		
		svm_parameter param = new svm_parameter();
		param.svm_type = svm_parameter.C_SVC;
		param.kernel_type = svm_parameter.RBF;
		param.degree = 3;
		param.gamma = 0; 
		param.nu = 0.5;
		param.cache_size = 100;
		param.C = 1;
		param.eps = 1e-3;
		param.p = 0.1;
		param.shrinking = 1;
		param.probability = 1;
		param.nr_weight = 0;
		param.weight_label = new int[0];
		param.weight = new double[0];

		System.out.println(svm.svm_check_parameter(problem, param)); 
		svm_model model = svm.svm_train(problem, param); 
		double[][] prob = Evaluate_Late1(model, X_test);
		return prob;
	}

	public double[][] Evaluate_Late1(svm_model model, svm_node[][] X) {
		double[][] prob = new double[X.length][];
		int n_label = svm.svm_get_nr_sv(model);
		int[] label = new int[n_label];
		for (int i = 0; i < X.length; i++) {
			double[] probi = new double[n_label];
			svm.svm_predict_probability(model, X[i], probi);
			prob[i] = probi;
		}
		return prob;
	}

	public void Evaluate_Late2(double[][] mx1, double[][] mx2, double[][] mx3, int k,
			ArrayList<Hashtable<Integer, Boolean>> truth) {
		double l1 = 0.5;
		double l2 = 0.3;
		double l3 = 0.9;
		double[][] mx = new double[mx1.length][mx1[0].length];
		for (int i = 0; i < mx.length; i++) {
			for (int j = 0; j < mx[0].length; j++) {
				mx[i][j] = l1 * mx1[i][j] +l2 *  mx2[i][j] +l3 *  mx3[i][j];
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
		Value v = new Value();
		v.pk = acc_pk / sum_pk;
		v.sk = acc_sk / sum_sk;
		System.out.println("P@" + k + "\t" + v.pk);
		System.out.println("S@" + k + "\t" + v.sk);
	}

	public void Evaluate_Early(double[][] mx, int k, ArrayList<Hashtable<Integer, Boolean>> truth) {
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
		Value v = new Value();
		v.pk = acc_pk / sum_pk;
		v.sk = acc_sk / sum_sk;
		System.out.println("P@" + k + "\t" + v.pk);
		System.out.println("S@" + k + "\t" + v.sk);
	}

	public double[][] SVM_Early_Process(int k) throws IOException {
		String fold = this.fold;
		svm_node[][] X1 = loadX(fold + "fb_train_" + k + "_new.csv");
		svm_node[][] X2 = loadX(fold + "quora_train_" + k + "_new.csv");
		svm_node[][] X3 = loadX(fold + "twit_train_" + k + "_new.csv");
		svm_node[][] X4 = loadX(fold + "twit_top_train_" + k + "_new.csv");
		double[] Y = loadY(fold + "gnd_train_" + k + "_new.csv");
		svm_node[][] X1_test = loadX(fold + "fb_valid_" + k + ".csv");
		svm_node[][] X2_test = loadX(fold + "quora_valid_" + k + ".csv");
		svm_node[][] X3_test = loadX(fold + "twit_valid_" + k + ".csv");
		svm_node[][] X4_test = loadX(fold + "twit_top_valid_" + k + ".csv");
		svm_node[][] X = MergeX(X1, X2);
		X = MergeX(X, X3);
		X = MergeX(X, X4);
		svm_node[][] X_test = MergeX(X1_test, X2_test);
		X_test = MergeX(X_test, X3_test);
		X_test = MergeX(X_test, X4_test);

		svm_problem problem = new svm_problem();
		problem.l = X.length; 
		problem.x = X; 
		problem.y = Y; 

		svm_parameter param = new svm_parameter();
		param.svm_type = svm_parameter.C_SVC;
		param.kernel_type = svm_parameter.RBF;
		param.degree = 3;
		param.gamma = 0; 
		param.coef0 = 0;
		param.nu = 0.5;
		param.cache_size = 100;
		param.C = 1;
		param.eps = 1e-3;
		param.p = 0.1;
		param.shrinking = 1;
		param.probability = 1;
		param.nr_weight = 0;
		param.weight_label = new int[0];
		param.weight = new double[0];

		System.out.println(svm.svm_check_parameter(problem, param)); 
		svm_model model = svm.svm_train(problem, param); 
		double[][] prob = Evaluate_Late1(model, X_test);
		return prob;
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

	public double[] loadY(String path) throws IOException {
		ArrayList<String> content = IO.FileLoad(path);
		String line = content.get(0);
		String[] terms = line.split(",");
		double[] Y = new double[terms.length];
		for (int i = 0; i < terms.length; i++) {
			Y[i] = Double.valueOf(terms[i]);
		}
		return Y;
	}

	public static void main(String[] args) throws IOException {
		Main t = new Main();
		int set = 0;
		
		double[][] result = t.SVM_Early_Process(set);
  		t.Evaluate_Early(result, 2, t.TRU.get(set));
 		t.Evaluate_Early(result, 4, t.TRU.get(set));
 		t.Evaluate_Early(result, 6, t.TRU.get(set));
 		t.Evaluate_Early(result, 8, t.TRU.get(set));
 		t.Evaluate_Early(result, 10, t.TRU.get(set));

//		 double[][] result1 = t.SVM_Late_Process(set, "fb");
//		 double[][] result2 = t.SVM_Late_Process(set, "quora");
//		 double[][] result3 = t.SVM_Late_Process(set, "twit");
//		 System.out.println("set " + set);
//		 t.Evaluate_Late2(result1, result2, result3, 2, t.TRU.get(set));
//		 t.Evaluate_Late2(result1, result2, result3, 4, t.TRU.get(set));
//		 t.Evaluate_Late2(result1, result2, result3, 6, t.TRU.get(set));
//		 t.Evaluate_Late2(result1, result2, result3, 8, t.TRU.get(set));
//		 t.Evaluate_Late2(result1, result2, result3, 10, t.TRU.get(set));
	}
}

class Value {
	double pk = 0.0;
	double sk = 0.0;
}