package Misc;

import java.io.IOException;
import java.util.ArrayList;

import Utility.IO;

public class DataIO {
	public static double[][] readData(String path) throws IOException {
		ArrayList<String> content = IO.FileLoad(path);
		String arr = content.get(0);
		String[] terms = arr.split(",");
		double[][] mx = new double[content.size() - 1][terms.length - 1];
		for (int i = 1; i < content.size(); i++) {
			arr = content.get(i);
			terms = arr.split(",");
			for (int j = 1; j < terms.length; j++) {
				double term = new Double(terms[j]);
				mx[i - 1][j - 1] = term;
			}
		}
		return mx;
	}
}
