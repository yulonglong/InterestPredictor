package Misc;

import java.io.IOException;
import java.util.ArrayList;

import Jama.Matrix;
import Utility.IO;

public class Debug {
	public static void main(String[] args) throws IOException {
		double[][] fb = DataIO.readData("./data/user_topic_facebook.csv");
		double[][] twit = DataIO.readData("./data/user_topic_twitter.csv");
		double[][] quora = DataIO.readData("./data/user_topic_quora.csv");
		double[][] twit_top = DataIO.readData("./data/contextual_topic_twitter.csv");
		double[][] gnd = DataIO.readData("./data/gnd.csv");
		Matrix test = new Matrix(fb);
	}
}
