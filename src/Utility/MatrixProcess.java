package Utility;

import Jama.Matrix;

public class MatrixProcess {
	public static Matrix MatrixExtendRow(Matrix mx){
		int r = mx.getRowDimension();
		int c = mx.getColumnDimension();
		Matrix result = new Matrix(r+c, c);
		Matrix I = Matrix.identity(c, c);
		result.setMatrix(0, r-1, 0, c-1, mx);
		result.setMatrix(r, r+c-1, 0, c-1, I);
		return result;
	}
	
	public static Matrix MatrixExtendColmn(Matrix mx){
		int r = mx.getRowDimension();
		int c = mx.getColumnDimension();
		Matrix result = new Matrix(r, c+r);
		Matrix I = Matrix.identity(r, r);
		result.setMatrix(0, r-1, 0, c-1, mx);
		result.setMatrix(0, r-1, c, c+r-1, I);
		return result;
	}
	
	public static Matrix MatrixExtendRow(Matrix mx1, Matrix mx2){
		int r1 = mx1.getRowDimension();
		int c1 = mx1.getColumnDimension();
		int r2 = mx2.getRowDimension();
		int c2 = mx2.getColumnDimension();
		if(c1!=c2){
			System.out.println("Row Extend Error!");
		}
		Matrix result = new Matrix(r1+r2, c1);
		result.setMatrix(0, r1-1, 0, c1-1, mx1);
		result.setMatrix(r1, r1+r2-1, 0, c1-1, mx2);
		return result;
	}
	
	public static Matrix MatrixExtendColmn(Matrix mx1, Matrix mx2){
		int r1 = mx1.getRowDimension();
		int c1 = mx1.getColumnDimension();
		int r2 = mx2.getRowDimension();
		int c2 = mx2.getColumnDimension();
		if(r1!=r2){
			System.out.println("Colmn Extend Error!");
		} 
		Matrix result = new Matrix(r1, c1 + c2);
		result.setMatrix(0, r1-1, 0, c1-1, mx1);
		result.setMatrix(0, r1-1, c1, c1+c2-1, mx2);
		return result;
	}
}
