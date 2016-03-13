package Misc;

import Jama.Matrix;

public class Test {
	public static void main(String[] args) {
		
		double[][] mx = new double[2][];
		double[] mx1 = {1,2,3,4};
		mx[0] = mx1;
		mx[1] = mx1;
		
		System.out.println(mx);
	}
}
