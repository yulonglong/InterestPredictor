package Misc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import Jama.Matrix;
import SVM.Value;
import Utility.Ranking;

public class Model {
	public Parameter pm;
	private Matrix[][] XX;

	public Model(Parameter pm) {
		this.pm = pm;
		this.XX = new Matrix[pm.S][pm.S];
		calcXX();
		System.out.println("Model Initialized...");
		System.out.println("Start Training...");
	}

	private void calcXX() {
		for (int s1 = 0; s1 < pm.S; s1++) {
			for (int s2 = 0; s2 < pm.S; s2++) {
				this.XX[s1][s2] = pm.Xi_train[s1].transpose().times(pm.Xi_train[s2]);
			}
		}
	}

	public double TrainingProcess() {
		double last_loss = 0.0;
		double loss_train = Loss(pm.X_train, pm.Y_train, pm.Xi_train);
		// System.out.println("Training Loss: " + loss_train);
		// double loss_valid = Loss(pm.X_valid, pm.Y_valid, pm.Xi_valid);
		// System.out.println("Valid Loss: " + loss_valid);
		// last_loss = loss_train;
		double best_value = 0.0;
		double[] bestP = new double[11];
		double[] bestS = new double[11];

		for (int i = 0; i < pm.iterations; i++) {
			System.out.println("Iteration " + i);
			Estimate();
			loss_train = Loss(pm.X_train, pm.Y_train, pm.Xi_train);
			System.out.println("Training Loss: " + loss_train);
			// loss_valid = Loss(pm.X_valid, pm.Y_valid, pm.Xi_valid);
			// System.out.println("Valid Loss: " + loss_valid);
			Value v_valid = new Value();

			// v_valid = Evaluate(pm.X_valid,pm.Y_valid, pm.Xi_valid,
			// pm.truth_valid);
			for (int k = 2; k <= 10; k += 2) {
				try {
					v_valid = Evaluate(pm.X_valid, pm.Y_valid, pm.Xi_valid, pm.truth_valid, k);
				} catch (Exception e) {
					System.out.println("..");
				}

				double pk = v_valid.pk;
				double sk = v_valid.sk;
				// System.out.println("P@" + k + "\t" + pk);
				// System.out.println("S@" + k + "\t" + sk);
				if (pk > bestP[k]) {
					bestP[k] = pk;
				}
				if (sk > bestS[k]) {
					bestS[k] = sk;
				}

				if (pk > best_value) {
					best_value = pk;
				}
				// Value v_test = Evaluate(pm.X_test,pm.Y_test, pm.Xi_test,
				// pm.truth_test);
				// System.out.println("Valid P: " + v_valid.pk);
				// System.out.println("Test P: " + P_test);
			}
		}
		for (int k = 2; k <= 10; k += 2) {
			System.out.println("P@" + k + "\t" + bestP[k]);
			System.out.println("S@" + k + "\t" + bestS[k]);
		}
		return best_value;
	}

	private double Loss(Matrix X, Matrix Y, Matrix[] Xi) {
		double loss = 0.0;
		Matrix sum_Y = new Matrix(Y.getRowDimension(), Y.getColumnDimension());
		for (int s = 0; s < pm.S; s++) {
			Matrix Ws = pm.Ai[s].plus(pm.Ci[s]);
			Matrix tmp_Y = Xi[s].times(Ws);
			Matrix Ys = tmp_Y.times(pm.Beta.get(s, 0));
			sum_Y.plusEquals(Ys);
		}
		Matrix obj_mx = Y.minus(sum_Y);
		double loss1 = Math.pow(obj_mx.norm2(), 2);
		// System.out.println("loss1\t" + loss1);
		loss += loss1;

		double loss2 = 0.0;
		for (int s1 = 0; s1 < pm.S; s1++) {
			Matrix tmx1 = Xi[s1].times(pm.Ai[s1]);
			for (int s2 = 0; s2 < pm.S; s2++) {
				if (s1 == s2)
					continue;
				Matrix tmx2 = Xi[s2].times(pm.Ai[s2]);
				Matrix dif_mx12 = tmx1.minus(tmx2);
				loss2 += Math.pow(dif_mx12.norm2(), 2);
			}
		}
		// System.out.println("loss2\t" + loss2);
		loss += pm.lambda1 * loss2;

		double loss3 = 0.0;
		for (int s = 0; s < pm.S; s++) {
			Matrix C = pm.Ci[s];
			for (int k = 0; k < C.getColumnDimension(); k++) {
				Matrix Ck = C.getMatrix(0, C.getRowDimension() - 1, k, k);
				loss3 += Ck.norm2();
			}
		}
		// System.out.println("loss3\t" + loss3);
		loss += pm.lambda2 * loss3;

		double loss4 = 0.0;
		for (int s = 0; s < pm.S; s++) {
			loss4 += Math.pow(pm.Ai[s].norm2(), 2);
		}
		// System.out.println("loss4\t" + loss4);
		loss += pm.lambda3 * loss4;

		double loss5 = 0.0;
		loss5 = Math.pow(pm.Beta.norm2(), 2);
		// System.out.println("loss5\t" + loss5);
		loss += pm.lambda4 * loss5;

		loss /= 2.0;
		return loss;
	}

	private double Loss() {
		double loss = 0.0;
		Matrix sum_Y = new Matrix(pm.Y_train.getRowDimension(), pm.Y_train.getColumnDimension());
		for (int s = 0; s < pm.S; s++) {
			Matrix Ws = pm.Ai[s].plus(pm.Ci[s]);
			Matrix tmp_Y = pm.Xi_train[s].times(Ws);
			Matrix Ys = tmp_Y.times(pm.Beta.get(s, 0));
			sum_Y.plusEquals(Ys);
		}
		Matrix obj_mx = pm.Y_train.minus(sum_Y);
		loss += Math.pow(obj_mx.norm2(), 2);

		double sum_cons = 0.0;
		for (int s1 = 0; s1 < pm.S; s1++) {
			Matrix tmx1 = pm.Xi_train[s1].times(pm.Ai[s1]);
			for (int s2 = 0; s2 < pm.S; s2++) {
				if (s1 == s2)
					continue;
				Matrix tmx2 = pm.Xi_train[s2].times(pm.Ai[s2]);
				Matrix dif_mx12 = tmx1.minus(tmx2);
				sum_cons += Math.pow(dif_mx12.norm2(), 2);
			}
		}
		loss += pm.lambda1 * sum_cons;

		double sum_comp = 0.0;
		for (int s = 0; s < pm.S; s++) {
			Matrix C = pm.Ci[s];
			for (int k = 0; k < C.getColumnDimension(); k++) {
				Matrix Ck = C.getMatrix(0, C.getRowDimension() - 1, k, k);
				sum_comp += Ck.norm2();
			}
		}
		loss += pm.lambda2 * sum_comp;

		double sum_norm = 0.0;
		for (int s = 0; s < pm.S; s++) {
			sum_norm += pm.lambda3 * Math.pow(pm.Ai[s].norm2(), 2);
		}
		sum_norm += pm.lambda4 * Math.pow(pm.Beta.norm2(), 2);
		loss += sum_norm;

		loss /= 2.0;
		return loss;
	}

	private void Estimate() {
		System.out.println("alpha");
		Estimate_alpha();
		System.out.println("A");
		Estimate_A();
		System.out.println("C");
		Estimate_C();
	}

	private void Estimate_alpha() {
		double[] vec_e = { 1, 1, 1 };
		Matrix e = new Matrix(vec_e, pm.S);
		Matrix eT = e.transpose();
		Matrix[] W = new Matrix[pm.S];
		for (int s = 0; s < pm.S; s++) {
			W[s] = pm.Ai[s].plus(pm.Ci[s]);
		}
		Matrix M = new Matrix(pm.S, pm.S);
		for (int k = 0; k < pm.K; k++) {
			Matrix Yk = pm.Y_train.getMatrix(0, pm.Y_train.getRowDimension() - 1, k, k);
			Matrix YkeT = Yk.times(eT);
			Matrix Wk = GenerateWk(W, k);
			Matrix XWk = pm.X_train.times(Wk);
			Matrix tMx = YkeT.minus(XWk);
			Matrix m = tMx.transpose().times(tMx);
			M.plusEquals(m);
		}
		Matrix I = Matrix.identity(pm.S, pm.S);
		I.timesEquals(pm.lambda4);
		M.plusEquals(I);

		Matrix MI = M.inverse();
		Matrix divisor = MI.times(e);
		Matrix dividend = eT.times(MI).times(e);
		pm.Beta = divisor.times(dividend.inverse());
	}

	private void Estimate_A() {
		Matrix L = new Matrix(pm.Dp, pm.Dp);
		int idxi_start = -1;
		int idxi_stop = -1;
		for (int si = 0; si < pm.S; si++) {
			idxi_start = idxi_stop + 1;
			idxi_stop = idxi_stop + pm.Dpi[si];
			int idxj_start = -1;
			int idxj_stop = -1;
			for (int sj = 0; sj < pm.S; sj++) {
				Matrix Lij;
				if (si == sj) {
					Lij = GenerateLii(si);
				} else {
					Lij = GenerateLij(si, sj);
				}
				idxj_start = idxj_stop + 1;
				idxj_stop = idxj_stop + pm.Dpi[sj];
				L.setMatrix(idxi_start, idxi_stop, idxj_start, idxj_stop, Lij);
			}
		}

		Matrix B = new Matrix(pm.Dp, pm.K);
		Matrix XC = GenerateXC();
		int idx_start = -1;
		int idx_stop = -1;
		for (int s = 0; s < pm.S; s++) {
			Matrix Bs = GenerateBi(s, XC);
			idx_start = idx_stop + 1;
			idx_stop = idx_stop + pm.Dpi[s];
			B.setMatrix(idx_start, idx_stop, 0, pm.K - 1, Bs);
		}

		Matrix A = L.solve(B);
		idx_start = -1;
		idx_stop = -1;
		for (int s = 0; s < pm.S; s++) {
			idx_start = idx_stop + 1;
			idx_stop = idx_stop + pm.Dpi[s];
			pm.Ai[s] = A.getMatrix(idx_start, idx_stop, 0, pm.K - 1);
		}
	}

	private Matrix GenerateLii(int s) {
		Matrix I = Matrix.identity(pm.Dpi[s], pm.Dpi[s]);
		I.timesEquals(pm.lambda3);
		double wmx2 = Math.pow(pm.Beta.get(s, 0), 2) + pm.lambda1 * (pm.S - 1);
		Matrix mx2 = this.XX[s][s].times(wmx2);
		Matrix Lii = I.plus(mx2);
		return Lii;
	}

	private Matrix GenerateLij(int si, int sj) {
		double wmx = pm.Beta.get(si, 0) * pm.Beta.get(sj, 0) - pm.lambda1;
		Matrix Lij = this.XX[si][sj].times(wmx);
		return Lij;
	}

	private Matrix GenerateXC() {
		Matrix XC = new Matrix(pm.N, pm.K);
		for (int s = 0; s < pm.S; s++) {
			Matrix mx = pm.Xi_train[s].times(pm.Ci[s]).times(pm.Beta.get(s, 0));
			XC.plusEquals(mx);
		}
		return XC;
	}

	private Matrix GenerateBi(int s, Matrix XC) {
		Matrix mx1 = pm.Xi_train[s].transpose().times(pm.Beta.get(s, 0));
		Matrix mx2 = pm.Y_train.minus(XC);
		Matrix Bi = mx1.times(mx2);
		return Bi;
	}

	private void Estimate_C() {
		double[][] theta = new double[pm.S][pm.K];

		for (int subite = 0; subite < pm.subIterations; subite++) {
			CalculateTheta(theta);

			Matrix Q = new Matrix(pm.Dp, pm.Dp);
			int idxi_start = -1;
			int idxi_stop = -1;
			for (int s1 = 0; s1 < pm.S; s1++) {
				idxi_start = idxi_stop + 1;
				idxi_stop = idxi_stop + pm.Dpi[s1];
				int idxj_start = -1;
				int idxj_stop = -1;
				for (int s2 = 0; s2 < pm.S; s2++) {
					idxj_start = idxj_stop + 1;
					idxj_stop = idxj_stop + pm.Dpi[s2];
					Matrix Qij;
					if (s1 != s2) {
						Qij = GenerateQij(s1, s2);
						Q.setMatrix(idxi_start, idxi_stop, idxj_start, idxj_stop, Qij);
					}
				}
			}

			for (int k = 0; k < pm.K; k++) {
				Matrix H = new Matrix(pm.Dp, 1);
				Matrix XA = GenerateXA(k);
				int idx_start = -1;
				int idx_stop = -1;
				int idxh_start = -1;
				int idxh_stop = -1;
				for (int s = 0; s < pm.S; s++) {
					idx_start = idx_stop + 1;
					idx_stop = idx_stop + pm.Dpi[s];
					Matrix Qii = GenerateQii(s, k, theta);
					Q.setMatrix(idx_start, idx_stop, idx_start, idx_stop, Qii);
					idxh_start = idxh_stop + 1;
					idxh_stop = idxh_stop + pm.Dpi[s];
					Matrix Hi = GenerateHi(s, k, XA);
					H.setMatrix(idxh_start, idxh_stop, 0, 0, Hi);
				}
				Matrix Ck = Q.solve(H);
				int idxc_start = -1;
				int idxc_stop = -1;
				for (int s = 0; s < pm.S; s++) {
					idxc_start = idxc_stop + 1;
					idxc_stop = idxc_stop + pm.Dpi[s];
					Matrix Csk = Ck.getMatrix(idxc_start, idxc_stop, 0, 0);
					pm.Ci[s].setMatrix(0, pm.Ci[s].getRowDimension() - 1, k, k, Csk);
				}
			}
		}
	}

	private void CalculateTheta(double[][] theta) {
		double sum = 0.0;
		for (int s = 0; s < theta.length; s++) {
			for (int k = 0; k < theta[0].length; k++) {
				Matrix Cik = pm.Ci[s].getMatrix(0, pm.Ci[s].getRowDimension() - 1, k, k);
				double L2norm = Cik.norm2();
				theta[s][k] = L2norm;
				sum += L2norm;
			}
		}
		for (int s = 0; s < theta.length; s++) {
			for (int k = 0; k < theta[0].length; k++) {
				theta[s][k] /= sum;
			}
		}
	}

	private Matrix GenerateQii(int s, int k, double[][] theta) {
		Matrix I = Matrix.identity(pm.Dpi[s], pm.Dpi[s]);
		I.timesEquals(pm.lambda2 / theta[s][k]);
		Matrix mx2 = this.XX[s][s].times(Math.pow(pm.Beta.get(s, 0), 2));
		Matrix Qii = I.plus(mx2);
		return Qii;
	}

	private Matrix GenerateQij(int si, int sj) {
		double wmx = pm.Beta.get(si, 0) * pm.Beta.get(sj, 0);
		Matrix Qij = this.XX[si][sj].times(12);
		Qij.timesEquals(wmx);
		return Qij;
	}

	private Matrix GenerateXA(int k) {
		Matrix XA = new Matrix(pm.N, 1);
		for (int s = 0; s < pm.S; s++) {
			Matrix Ajk = pm.Ai[s].getMatrix(0, pm.Ai[s].getRowDimension() - 1, k, k);
			Matrix mx = pm.Xi_train[s].times(Ajk).times(pm.Beta.get(s, 0));
			XA.plusEquals(mx);
		}
		return XA;
	}

	private Matrix GenerateHi(int s, int k, Matrix XA) {
		Matrix Yk = pm.Y_train.getMatrix(0, pm.Y_train.getRowDimension() - 1, k, k);
		Yk.minusEquals(XA);
		Matrix Hi = pm.Xi_train[s].transpose().times(Yk).times(pm.Beta.get(s, 0));
		return Hi;
	}

	private Matrix GenerateWk(Matrix[] mx, int k) {
		int sum_D = mx[0].getRowDimension() + mx[1].getRowDimension() + mx[2].getRowDimension();
		// int sum_D = mx[0].getRowDimension();
		Matrix result = new Matrix(sum_D, pm.S);
		int idx_start = -1;
		int idx_stop = -1;
		for (int s = 0; s < mx.length; s++) {
			idx_start = idx_stop + 1;
			idx_stop = idx_stop + mx[s].getRowDimension();
			Matrix vec = mx[s].getMatrix(0, mx[s].getRowDimension() - 1, k, k);
			result.setMatrix(idx_start, idx_stop, s, s, vec);
		}
		return result;
	}

	public void Evaluate() {
		Ranking rk = new Ranking();
		Matrix sum_Y = new Matrix(pm.Y_train.getRowDimension(), pm.Y_train.getColumnDimension());
		for (int s = 0; s < pm.S; s++) {
			Matrix Ws = pm.Ai[s].plus(pm.Ci[s]);
			Matrix tmp_Y = pm.Xi_train[s].times(Ws);
			Matrix Ys = tmp_Y.times(pm.Beta.get(s, 0));
			sum_Y.plusEquals(Ys);
		}
		double[][] mx = sum_Y.getArray();
		double sum = 0.0;
		double acc = 0.0;
		for (int n = 0; n < sum_Y.getRowDimension(); n++) {
			sum += 1.0;
			double[] row = mx[n];
			int[] list = rk.rank(row);
			Hashtable<Integer, Boolean> interest = pm.truth_valid.get(n);
			// S@10
			for (int i = 0; i < 10; i++) {
				if (interest.containsKey(list[i])) {
					acc += 1.0;
					break;
				}
			}
		}
		System.out.println(acc / sum);
	}

	public Value Evaluate(Matrix X, Matrix Y, Matrix[] Xi, ArrayList<Hashtable<Integer, Boolean>> truth, int k) {
		Ranking rk = new Ranking();
		Matrix sum_Y = new Matrix(Y.getRowDimension(), Y.getColumnDimension());
		for (int s = 0; s < pm.S; s++) {
			Matrix Ws = pm.Ai[s].plus(pm.Ci[s]);
			Matrix tmp_Y = Xi[s].times(Ws);
			Matrix Ys = tmp_Y.times(pm.Beta.get(s, 0));
			sum_Y.plusEquals(Ys);
		}
		double[][] mx = sum_Y.getArray();
		double sum_sk = 0.0;
		double sum_pk = 0.0;
		double acc_sk = 0.0;
		double acc_pk = 0.0;
		for (int n = 0; n < sum_Y.getRowDimension(); n++) {
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
		return v;
	}
}

