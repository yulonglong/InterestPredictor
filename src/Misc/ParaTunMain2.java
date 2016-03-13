package Misc;

import java.io.IOException;

import Jama.Matrix;
import Utility.IO;

public class ParaTunMain2 {
	public static void main(String[] args) throws IOException {
		TTest T = new TTest("./SepData/");
//		double start = 0.1;
//		double stop = 1.0;
//		int n = 2;
		double start = Double.valueOf(args[0]);
		double stop = Double.valueOf(args[1]);
		int n = Integer.valueOf(args[2]);
		String file = "./ParaTun/L" + n + "_" + start + "-" + stop + ".csv";
		double L = start;
		while (L <= stop) {
			String line = L + ",";
			try {
				IO.FileAppend(file, line);
				for (int i = 0; i < 10; i++) {
					Parameter pm = new Parameter(T, i);
					if (n == 1)
						pm.lambda1 = L;
					else if (n == 2)
						pm.lambda2 = L;
					else if (n == 3)
						pm.lambda3 = L;
					else
						pm.lambda4 = L;
					Model md = new Model(pm);
					double value = md.TrainingProcess();
					line = "," + value;
					IO.FileAppend(file, line);
				}
				line = "\n";
				IO.FileAppend(file, line);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (L >= 0.01 && L < 0.1) {
				L += 0.01;
			} else if (L >= 0.1 && L < 1.0) {
				L += 0.1;
			} else if (L >= 1.0 && L < 10) {
				L += 1.0;
			} else {
				L += 10.0;
			}
		}
	}

	private static void Process(TTest T, int i) {
		System.out.println("Data Loading Finished...");
		Parameter pm = new Parameter(T, i);
		Model md = new Model(pm);
		double value = md.TrainingProcess();
	}
}
