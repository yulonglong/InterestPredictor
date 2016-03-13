package Misc;

import java.io.IOException;

import Jama.Matrix;
import Utility.IO;

public class ParameterTurningMain {
	public static void main(String[] args) throws IOException {
		double[][] fb = DataIO.readData("./data/user_topic_facebook_train.csv");
		double[][] twit = DataIO.readData("./data/user_topic_twitter_train.csv");
		double[][] quora = DataIO.readData("./data/user_topic_quora_train.csv");
		double[][] twit_top = DataIO.readData("./data/contextual_topic_twitter_train.csv");
		double[][] gnd = DataIO.readData("./data/gnd_train.csv");

		double[][] fb_valid = DataIO.readData("./data/user_topic_facebook_valid.csv");
		double[][] twit_valid = DataIO.readData("./data/user_topic_twitter_valid.csv");
		double[][] quora_valid = DataIO.readData("./data/user_topic_quora_valid.csv");
		double[][] twit_top_valid = DataIO.readData("./data/contextual_topic_twitter_valid.csv");
		double[][] gnd_valid = DataIO.readData("./data/gnd_valid.csv");

		double start = Double.valueOf(0.07);
		double stop = Double.valueOf(0.07);
//		double stop = Double.valueOf(10);
		PraTuning(twit, twit_valid, twit_top, twit_top_valid, fb, fb_valid, quora, quora_valid, gnd, gnd_valid, start, stop);
	}

	private static void PraTuning(double[][] twit_train, double[][] twit_test, double[][] twit_top_train,
			double[][] twit_top_test, double[][] fb_train, double[][] fb_test, double[][] quora_train,
			double[][] quora_test, double[][] gnd_train, double[][] gnd_test, double start, double stop) {
		String file = "pt_" + start + "_" + stop + ".csv";
		double l1 = start;
		while (l1 <= stop) {
			double l2 = 30;
			while (l2 <= 30) {
				double l3 = 8;
				while (l3 <= 30) {
					double l4 = 1;
					while (l4 <= 10) {
						String line = l1 + "," + l2 + "," + l3 + "," + l4 + ",";
						try {
							IO.FileAppend(file, line);
							double avg = 0.0;
							double sum = 0.0;
							Parameter pm = new Parameter(twit_train, twit_test, twit_top_train, twit_top_test, fb_train,
									fb_test, quora_train, quora_test, gnd_train, gnd_test);
							pm.lambda1 = l1;
							pm.lambda2 = l2;
							pm.lambda3 = l3;
							pm.lambda4 = l4;
							Model md = new Model(pm);
							double value = md.TrainingProcess();
							line = "," + value + "\n";
							IO.FileAppend(file, line);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (l4 >= 0.01 && l4 < 0.1) {
							l4 += 0.01;
						} else if (l4 >= 0.1 && l4 < 1.0) {
							l4 += 0.1;
						} else if (l4 >= 1.0 && l4 < 10) {
							l4 += 1.0;
						} else {
							l4 += 10.0;
						}
					}
					if (l3 >= 0.01 && l3 < 0.1) {
						l3 += 0.01;
					} else if (l3 >= 0.1 && l3 < 1.0) {
						l3 += 0.1;
					} else if (l3 >= 1.0 && l3 < 10) {
						l3 += 1.0;
					} else {
						l3 += 10.0;
					}
				}
				if (l2 >= 0.01 && l2 < 0.1) {
					l2 += 0.01;
				} else if (l2 >= 0.1 && l2 < 1.0) {
					l2 += 0.1;
				} else if (l2 >= 1.0 && l2 < 10) {
					l2 += 1.0;
				} else {
					l2 += 10.0;
				}
			}
			if (l1 >= 0.01 && l1 < 0.1) {
				l1 += 0.01;
			} else if (l1 >= 0.1 && l1 < 1.0) {
				l1 += 0.1;
			} else if (l1 >= 1.0 && l1 < 10) {
				l1 += 1.0;
			} else {
				l1 += 10.0;
			}
		}
	}
}
