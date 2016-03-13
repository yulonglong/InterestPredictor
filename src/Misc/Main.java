package Misc;

import java.io.IOException;

import Jama.Matrix;
import Utility.IO;

public class Main {
	public static void main(String[] args) throws IOException {
		TTest T = new TTest("./SepData/");
		for (int i = 0; i < 10; i++) {
			Process(T, i);
		 }
	}

	private static void Process(double[][] fb, double[][] twit, double[][] quora, double[][] twit_top, double[][] gnd,
			double[][] valid) {
		System.out.println("Data Loading Finished...");
		Parameter pm = new Parameter(fb, twit, quora, twit_top, gnd, valid);
		Model md = new Model(pm);
		md.TrainingProcess();
	}

	private static void Process(TTest T, int i) {
		System.out.println("Data Loading Finished...");
		Parameter pm = new Parameter(T, i);
		Model md = new Model(pm);
		double value = md.TrainingProcess();
	}
}
