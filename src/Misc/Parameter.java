package Misc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import Jama.*;
import Utility.MatrixProcess;

public class Parameter {
	public double lambda1 = 0.04;
	public double lambda2 = 40;
	public double lambda3 = 50;
	public double lambda4 = 40;

	public double iterations = 10;
	public double subIterations = 5;

	public Matrix[] Xi_train;
	public Matrix[] Xi_valid;
	public Matrix[] Xi_test;
	public Matrix X_train;
	public Matrix X_valid;
	public Matrix X_test;

	ArrayList<Hashtable<Integer, Boolean>> truth_valid;
	ArrayList<Hashtable<Integer, Boolean>> truth_test;

	public Matrix Y_train;
	public Matrix Y_valid;
	public Matrix Y_test;

	int[] Di;
	int N;
	int S;
	int K;
	int D;
	int[] Dpi;
	int Dp;

	public Matrix[] Ai;
	public Matrix[] Ci;
	public Matrix Beta;

	public double[][] MatrixMerge(double[][] mx1, double[][] mx2) {
		double[][] result = new double[mx1.length][mx1[0].length + mx2[0].length];
		for (int i = 0; i < result.length; i++) {
			for (int j = 0; j < result[0].length; j++) {
				if (j < mx1[0].length) {
					result[i][j] = mx1[i][j];
				} else {
					result[i][j] = mx2[i][j - mx1[0].length];
				}
			}
		}
		return result;
	}

	public Parameter(double[][] fb, double[][] twit, double[][] quora, double[][] twit_topic, double[][] gnd,
			double[][] valid) {
		double[][] twitter = MatrixMerge(twit, twit_topic);
		Matrix mtwit = new Matrix(twitter);
		Matrix mfb = new Matrix(fb);
		Matrix mquora = new Matrix(quora);

		this.Xi_train = new Matrix[3];
		this.Xi_train[0] = mfb;
		this.Xi_train[1] = mtwit;
		this.Xi_train[2] = mquora;

		Matrix mgnd = new Matrix(gnd);
		this.Y_train = mgnd;
		this.truth_valid = new ArrayList<Hashtable<Integer, Boolean>>();
		for (int n = 0; n < gnd.length; n++) {
			Hashtable<Integer, Boolean> intrest = new Hashtable<Integer, Boolean>();
			for (int k = 0; k < gnd[0].length; k++) {
				if (gnd[n][k] == 1)
					intrest.put(k, true);
			}
			this.truth_valid.add(intrest);
		}

		this.Y_valid = new Matrix(valid);

		this.S = 3;

		this.Di = new int[this.S];
		this.Di[0] = fb[0].length;
		this.Di[1] = twitter[0].length;
		this.Di[2] = quora[0].length;
		D = Di[0] + Di[1] + Di[2];

		this.N = this.Xi_train[0].getRowDimension();
		this.K = this.Y_train.getColumnDimension();

		this.X_train = MatrixProcess.MatrixExtendColmn(Xi_train[0], Xi_train[1]);
		this.X_train = MatrixProcess.MatrixExtendColmn(X_train, Xi_train[2]);

		this.Dpi = new int[this.S];
		this.Dp = 0;
		this.Ai = new Matrix[this.S];
		for (int s = 0; s < this.S; s++) {
			double[][] mxa = new double[this.Di[s]][this.K]; // D[i]+N
																// or
																// D[i]
																// or
																// others
			// how to initialize
			for (int j = 0; j < mxa.length; j++) {
				for (int k = 0; k < mxa[j].length; k++) {
					mxa[j][k] = 0.0;
				}
			}
			Matrix mx = new Matrix(mxa);
			this.Ai[s] = mx;
			this.Dpi[s] = Ai[s].getRowDimension();
			this.Dp += Ai[s].getRowDimension();
		}
		this.Ci = new Matrix[this.S];
		for (int s = 0; s < this.S; s++) {
			double[][] mxc = new double[this.Di[s]][this.K]; // D[i]+N
																// or
																// D[i]
																// or
																// others
			// how to initialize
			for (int j = 0; j < mxc.length; j++) {
				for (int k = 0; k < mxc[j].length; k++) {
					mxc[j][k] = Math.random();
				}
			}
			Matrix mx = new Matrix(mxc);
			this.Ci[s] = mx;
		}
		double[] mxalpha = new double[this.S];
		for (int s = 0; s < this.S; s++) {
			mxalpha[s] = 1.0;
		}
		this.Beta = new Matrix(mxalpha, this.S);
	}

	private void SetZero(double[][] mx){
		for(int i=0;i<mx.length;i++){
			for(int j=0;j<mx[0].length;j++){
				mx[i][j] = 0.0;
			}
		}
	}
	
	public Parameter(TTest T, int t) {
		this.S = 3;
		
		double[][] twit = T.arr_twit_train.get(t);
		double[][] fb = T.arr_fb_train.get(t);
		double[][] quora = T.arr_quora_train.get(t);
		double[][] twit_topic = T.arr_twit_top_train.get(t);
		double[][] gnd = T.arr_gnd_train.get(t);

		double[][] twit_valid = T.arr_twit_valid.get(t);
		double[][] fb_valid = T.arr_fb_valid.get(t);
		double[][] quora_valid = T.arr_quora_valid.get(t);
		double[][] twit_topic_valid = T.arr_twit_top_valid.get(t);
		double[][] gnd_valid = T.arr_gnd_valid.get(t);

		double[][] twit_test = T.arr_twit_test.get(t);
		double[][] fb_test = T.arr_fb_test.get(t);
		double[][] quora_test = T.arr_quora_test.get(t);
		double[][] twit_topic_test = T.arr_twit_top_test.get(t);
		double[][] gnd_test = T.arr_gnd_test.get(t);

		double[][] twitter = MatrixMerge(twit, twit_topic);
		
		Matrix mtwit_train = new Matrix(twitter);
		Matrix mfb_train = new Matrix(fb);
		Matrix mquora_train = new Matrix(quora);
		this.Xi_train = new Matrix[this.S];
		this.Xi_train[0] = mfb_train;
		this.Xi_train[1] = mtwit_train;
		this.Xi_train[2] = mquora_train;

		double[][] twitter_valid = MatrixMerge(twit_valid, twit_topic_valid);
		Matrix mtwit_valid = new Matrix(twitter_valid);
		Matrix mfb_valid = new Matrix(fb_valid);
		Matrix mquora_valid = new Matrix(quora_valid);
		this.Xi_valid = new Matrix[this.S];
		this.Xi_valid[0] = mfb_valid;
		this.Xi_valid[1] = mtwit_valid;
		this.Xi_valid[2] = mquora_valid;

		double[][] twitter_test = MatrixMerge(twit_test, twit_topic_test);
		Matrix mtwit_test = new Matrix(twitter_test);
		Matrix mfb_test = new Matrix(fb_test);
		Matrix mquora_test = new Matrix(quora_test);
		this.Xi_test = new Matrix[this.S];
		this.Xi_test[0] = mfb_test;
		this.Xi_test[1] = mtwit_test;
		this.Xi_test[2] = mquora_test;

		this.Y_train = new Matrix(gnd);

		this.Y_valid = new Matrix(gnd_valid);
		this.truth_valid = new ArrayList<Hashtable<Integer, Boolean>>();
		for (int n = 0; n < gnd_valid.length; n++) {
			Hashtable<Integer, Boolean> intrest = new Hashtable<Integer, Boolean>();
			for (int k = 0; k < gnd_valid[0].length; k++) {
				if (gnd_valid[n][k] == 1)
					intrest.put(k, true);
			}
			this.truth_valid.add(intrest);
		}

		this.Y_test = new Matrix(gnd_test);
		this.truth_test = new ArrayList<Hashtable<Integer, Boolean>>();
		for (int n = 0; n < gnd_test.length; n++) {
			Hashtable<Integer, Boolean> intrest = new Hashtable<Integer, Boolean>();
			for (int k = 0; k < gnd_test[0].length; k++) {
				if (gnd_test[n][k] == 1)
					intrest.put(k, true);
			}
			this.truth_test.add(intrest);
		}

		this.Di = new int[this.S];
		this.Di[0] = fb[0].length;
		this.Di[1] = twitter[0].length;
		this.Di[2] = quora[0].length;
		D = Di[0] + Di[1] + Di[2];
//		D = Di[0];

		this.N = this.Xi_train[0].getRowDimension();
		this.K = this.Y_train.getColumnDimension();

//		this.X_train = Xi_train[0];
		this.X_train = MatrixProcess.MatrixExtendColmn(Xi_train[0], Xi_train[1]);
		this.X_train = MatrixProcess.MatrixExtendColmn(X_train, Xi_train[2]);

		this.Dpi = new int[this.S];
		this.Dp = 0;
		this.Ai = new Matrix[this.S];
		for (int s = 0; s < this.S; s++) {
			double[][] mxa = new double[this.Di[s]][this.K]; // D[i]+N
																// or
																// D[i]
																// or
																// others
			// how to initialize
			for (int j = 0; j < mxa.length; j++) {
				for (int k = 0; k < mxa[j].length; k++) {
					mxa[j][k] = Math.random();
				}
			}
			Matrix mx = new Matrix(mxa);
			this.Ai[s] = mx;
			this.Dpi[s] = Ai[s].getRowDimension();
			this.Dp += Ai[s].getRowDimension();
		}
		this.Ci = new Matrix[this.S];
		for (int s = 0; s < this.S; s++) {
			double[][] mxc = new double[this.Di[s]][this.K]; // D[i]+N
																// or
																// D[i]
																// or
																// others
			// how to initialize
			for (int j = 0; j < mxc.length; j++) {
				for (int k = 0; k < mxc[j].length; k++) {
					mxc[j][k] = Math.random();
				}
			}
			Matrix mx = new Matrix(mxc);
			this.Ci[s] = mx;
		}
		double[] mxalpha = new double[this.S];
		for (int s = 0; s < this.S; s++) {
			mxalpha[s] = Math.random();
		}
		this.Beta = new Matrix(mxalpha, this.S);
	}

	public Parameter(double[][] twit_train, double[][] twit_test, double[][] twit_top_train, double[][] twit_top_test,
			double[][] fb_train, double[][] fb_test, double[][] quora_train, double[][] quora_test,
			double[][] gnd_train, double[][] gnd_test) {
		double[][] twit = twit_train;
		double[][] fb = fb_train;
		double[][] quora = quora_train;
		double[][] twit_topic = twit_top_train;
		double[][] gnd = gnd_train;

		double[][] twit_valid = twit_test;
		double[][] fb_valid = fb_test;
		double[][] quora_valid = quora_test;
		double[][] twit_topic_valid = twit_top_test;
		double[][] gnd_valid = gnd_test;

		double[][] twitter = MatrixMerge(twit, twit_topic);
		Matrix mtwit_train = new Matrix(twitter);
		Matrix mfb_train = new Matrix(fb);
		Matrix mquora_train = new Matrix(quora);
		this.Xi_train = new Matrix[3];
		this.Xi_train[0] = mfb_train;
		this.Xi_train[1] = mtwit_train;
		this.Xi_train[2] = mquora_train;

		double[][] twitter_valid = MatrixMerge(twit_valid, twit_topic_valid);
		Matrix mtwit_valid = new Matrix(twitter_valid);
		Matrix mfb_valid = new Matrix(fb_valid);
		Matrix mquora_valid = new Matrix(quora_valid);
		this.Xi_valid = new Matrix[3];
		this.Xi_valid[0] = mfb_valid;
		this.Xi_valid[1] = mtwit_valid;
		this.Xi_valid[2] = mquora_valid;

		this.Y_train = new Matrix(gnd);

		this.Y_valid = new Matrix(gnd_valid);
		this.truth_valid = new ArrayList<Hashtable<Integer, Boolean>>();
		for (int n = 0; n < gnd_valid.length; n++) {
			Hashtable<Integer, Boolean> intrest = new Hashtable<Integer, Boolean>();
			for (int k = 0; k < gnd_valid[0].length; k++) {
				if (gnd_valid[n][k] == 1)
					intrest.put(k, true);
			}
			this.truth_valid.add(intrest);
		}

		this.Y_test = new Matrix(gnd_test);
		this.truth_test = new ArrayList<Hashtable<Integer, Boolean>>();
		for (int n = 0; n < gnd_test.length; n++) {
			Hashtable<Integer, Boolean> intrest = new Hashtable<Integer, Boolean>();
			for (int k = 0; k < gnd_test[0].length; k++) {
				if (gnd_test[n][k] == 1)
					intrest.put(k, true);
			}
			this.truth_test.add(intrest);
		}

		this.S = 3;

		this.Di = new int[this.S];
		this.Di[0] = fb[0].length;
		this.Di[1] = twitter[0].length;
		this.Di[2] = quora[0].length;
		D = Di[0] + Di[1] + Di[2];

		this.N = this.Xi_train[0].getRowDimension();
		this.K = this.Y_train.getColumnDimension();

		this.X_train = MatrixProcess.MatrixExtendColmn(Xi_train[0], Xi_train[1]);
		this.X_train = MatrixProcess.MatrixExtendColmn(X_train, Xi_train[2]);

		this.Dpi = new int[this.S];
		this.Dp = 0;
		this.Ai = new Matrix[this.S];
		for (int s = 0; s < this.S; s++) {
			double[][] mxa = new double[this.Di[s]][this.K]; // D[i]+N
																// or
																// D[i]
																// or
																// others
			// how to initialize
			for (int j = 0; j < mxa.length; j++) {
				for (int k = 0; k < mxa[j].length; k++) {
					mxa[j][k] = 0.0;
				}
			}
			Matrix mx = new Matrix(mxa);
			this.Ai[s] = mx;
			this.Dpi[s] = Ai[s].getRowDimension();
			this.Dp += Ai[s].getRowDimension();
		}
		this.Ci = new Matrix[this.S];
		for (int s = 0; s < this.S; s++) {
			double[][] mxc = new double[this.Di[s]][this.K]; // D[i]+N
																// or
																// D[i]
																// or
																// others
			// how to initialize
			for (int j = 0; j < mxc.length; j++) {
				for (int k = 0; k < mxc[j].length; k++) {
					mxc[j][k] = Math.random();
				}
			}
			Matrix mx = new Matrix(mxc);
			this.Ci[s] = mx;
		}
		double[] mxalpha = new double[this.S];
		for (int s = 0; s < this.S; s++) {
			mxalpha[s] = 1.0;
		}
		this.Beta = new Matrix(mxalpha, this.S);
	}
}
