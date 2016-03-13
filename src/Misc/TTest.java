package Misc;

import java.io.IOException;
import java.util.ArrayList;

import Utility.IO;

public class TTest {
	private ArrayList<double[][]> arr_fb = new ArrayList<double[][]>();
	private ArrayList<double[][]> arr_twit = new ArrayList<double[][]>();
	private ArrayList<double[][]> arr_quora = new ArrayList<double[][]>();
	private ArrayList<double[][]> arr_twit_top = new ArrayList<double[][]>();
	private ArrayList<double[][]> arr_gnd = new ArrayList<double[][]>();

	public ArrayList<double[][]> arr_fb_train = new ArrayList<double[][]>();
	public ArrayList<double[][]> arr_twit_train = new ArrayList<double[][]>();
	public ArrayList<double[][]> arr_quora_train = new ArrayList<double[][]>();
	public ArrayList<double[][]> arr_twit_top_train = new ArrayList<double[][]>();
	public ArrayList<double[][]> arr_gnd_train = new ArrayList<double[][]>();

	public ArrayList<double[][]> arr_fb_valid = new ArrayList<double[][]>();
	public ArrayList<double[][]> arr_twit_valid = new ArrayList<double[][]>();
	public ArrayList<double[][]> arr_quora_valid = new ArrayList<double[][]>();
	public ArrayList<double[][]> arr_twit_top_valid = new ArrayList<double[][]>();
	public ArrayList<double[][]> arr_gnd_valid = new ArrayList<double[][]>();

	public ArrayList<double[][]> arr_fb_test = new ArrayList<double[][]>();
	public ArrayList<double[][]> arr_twit_test = new ArrayList<double[][]>();
	public ArrayList<double[][]> arr_quora_test = new ArrayList<double[][]>();
	public ArrayList<double[][]> arr_twit_top_test = new ArrayList<double[][]>();
	public ArrayList<double[][]> arr_gnd_test = new ArrayList<double[][]>();
	
	public void OutputT(String fold){
		for(int i=0;i<this.arr_fb_train.size();i++){
			double[][] fb_train = this.arr_fb_train.get(i);
			double[][] twit_train = this.arr_twit_train.get(i);
			double[][] quora_train = this.arr_quora_train.get(i);
			double[][] twit_top_train = this.arr_twit_top_train.get(i);
			double[][] gnd_train = this.arr_gnd_train.get(i);
			
			double[][] fb_valid = this.arr_fb_valid.get(i);
			double[][] twit_valid = this.arr_twit_valid.get(i);
			double[][] quora_valid = this.arr_quora_valid.get(i);
			double[][] twit_top_valid = this.arr_twit_top_valid.get(i);
			double[][] gnd_valid = this.arr_gnd_valid.get(i);
			
			double[][] fb_test = this.arr_fb_train.get(i);
			double[][] twit_test = this.arr_twit_test.get(i);
			double[][] quora_test = this.arr_quora_test.get(i);
			double[][] twit_top_test = this.arr_twit_top_test.get(i);
			double[][] gnd_test = this.arr_gnd_test.get(i);
			
			WriteMx(fold + "fb_train_" + i + ".csv", fb_train);
			WriteMx(fold + "twit_train_" + i + ".csv", twit_train);
			WriteMx(fold + "quora_train_" + i + ".csv", quora_train);
			WriteMx(fold + "twit_top_train_" + i + ".csv", twit_top_train);
			WriteMx(fold + "gnd_train_" + i + ".csv", gnd_train);
			
			WriteMx(fold + "fb_valid_" + i + ".csv", fb_valid);
			WriteMx(fold + "twit_valid_" + i + ".csv", twit_valid);
			WriteMx(fold + "quora_valid_" + i + ".csv", quora_valid);
			WriteMx(fold + "twit_top_valid_" + i + ".csv", twit_top_valid);
			WriteMx(fold + "gnd_valid_" + i + ".csv", gnd_valid);
			
			WriteMx(fold + "fb_test_" + i + ".csv", fb_test);
			WriteMx(fold + "twit_test_" + i + ".csv", twit_test);
			WriteMx(fold + "quora_test_" + i + ".csv", quora_test);
			WriteMx(fold + "twit_top_test_" + i + ".csv", twit_top_test);
			WriteMx(fold + "gnd_test_" + i + ".csv", gnd_test);
		}
	}
	
	public void WriteMx(String path, double[][] mx){
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<mx.length;i++){
			for(int j=0;j<mx[0].length;j++){
				if(j!=0){
					sb.append(",");
				}
				sb.append(mx[i][j]);
			}
			sb.append("\n");
		}
		try {
			IO.FileWrite(path, sb);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static double[][] ReadMx(String path){
		ArrayList<String> content = new ArrayList<>();
		try {
			content = IO.FileLoad(path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		double[][] mx = new double[content.size()][];
		for(int i=0;i<mx.length;i++){
			String line = content.get(i);
			String[] value = line.split(",");
			double[] vector = new double[value.length];
			for(int j=0;j<value.length;j++){
				vector[j] = Double.valueOf(value[j]);
			}
			mx[i] = vector;
		}
		return mx;
	}

	public TTest(int k, double[][] fb, double[][] twit, double[][] quora, double[][] twit_top, double[][] gnd) {
		int n = gnd.length;
		ArrayList<Integer> idx = shuffle(n);
		generateKFold(k, idx, fb, twit, quora, twit_top, gnd);
		for (int i = 0; i < k; i++) {
			TrainValidTest(i, (i + 1) % k);
		}
	}
	
	public TTest(String fold) {
		for(int i=0;i<10;i++){
			double[][] fb_train = ReadMx(fold + "fb_train_" + i + ".csv");
			this.arr_fb_train.add(fb_train);
			double[][] twit_train = ReadMx(fold + "twit_train_" + i + ".csv");
			this.arr_twit_train.add(twit_train);
			double[][] quora_train = ReadMx(fold + "quora_train_" + i + ".csv");
			this.arr_quora_train.add(quora_train);
			double[][] twit_top_train = ReadMx(fold + "twit_top_train_" + i + ".csv");
			this.arr_twit_top_train.add(twit_top_train);
			double[][] gnd_train = ReadMx(fold + "gnd_train_" + i + ".csv");
			this.arr_gnd_train.add(gnd_train);
			
			double[][] fb_valid = ReadMx(fold + "fb_valid_" + i + ".csv");
			this.arr_fb_valid.add(fb_valid);
			double[][] twit_valid = ReadMx(fold + "twit_valid_" + i + ".csv");
			this.arr_twit_valid.add(twit_valid);
			double[][] quora_valid = ReadMx(fold + "quora_valid_" + i + ".csv");
			this.arr_quora_valid.add(quora_valid);
			double[][] twit_top_valid = ReadMx(fold + "twit_top_valid_" + i + ".csv");
			this.arr_twit_top_valid.add(twit_top_valid);
			double[][] gnd_valid = ReadMx(fold + "gnd_valid_" + i + ".csv");
			this.arr_gnd_valid.add(gnd_valid);
			
			double[][] fb_test = ReadMx(fold + "fb_test_" + i + ".csv");
			this.arr_fb_test.add(fb_test);
			double[][] twit_test = ReadMx(fold + "twit_test_" + i + ".csv");
			this.arr_twit_test.add(twit_test);
			double[][] quora_test = ReadMx(fold + "quora_test_" + i + ".csv");
			this.arr_quora_test.add(quora_test);
			double[][] twit_top_test = ReadMx(fold + "twit_top_test_" + i + ".csv");
			this.arr_twit_top_test.add(twit_top_test);
			double[][] gnd_test = ReadMx(fold + "gnd_test_" + i + ".csv");
			this.arr_gnd_test.add(gnd_test);
		}
	}

	private double[][] mxMerge(double[][] mx1, double[][] mx2) {
		double[][] result = new double[mx1.length + mx2.length][mx2[0].length];
		int idx = -1;
		for (int i = 0; i < mx1.length; i++) {
			idx += 1;
			result[idx] = mx1[i];
		}
		for (int i = 0; i < mx2.length; i++) {
			idx += 1;
			result[idx] = mx2[i];
		}
		return result;
	}

	private void TrainValidTest(int valid, int test) {
		double[][] fb_train = new double[0][0];
		double[][] twit_train = new double[0][0];
		double[][] quora_train = new double[0][0];
		double[][] twit_top_train = new double[0][0];
		double[][] gnd_train = new double[0][0];

		double[][] fb_valid = new double[0][0];
		double[][] twit_valid = new double[0][0];
		double[][] quora_valid = new double[0][0];
		double[][] twit_top_valid = new double[0][0];
		double[][] gnd_valid = new double[0][0];

		double[][] fb_test = new double[0][0];
		double[][] twit_test = new double[0][0];
		double[][] quora_test = new double[0][0];
		double[][] twit_top_test = new double[0][0];
		double[][] gnd_test = new double[0][0];

		for (int i = 0; i < arr_gnd.size(); i++) {
			if (i == valid) {
				fb_valid = arr_fb.get(i);
				twit_valid = arr_twit.get(i);
				quora_valid = arr_quora.get(i);
				twit_top_valid = arr_twit_top.get(i);
				gnd_valid = arr_gnd.get(i);
			} else if (i == test) {
				fb_test = arr_fb.get(i);
				twit_test = arr_twit.get(i);
				quora_test = arr_quora.get(i);
				twit_top_test = arr_twit_top.get(i);
				gnd_test = arr_gnd.get(i);
			} else {
				fb_train = mxMerge(fb_train, arr_fb.get(i));
				twit_train = mxMerge(twit_train, arr_twit.get(i));
				quora_train = mxMerge(quora_train, arr_quora.get(i));
				twit_top_train = mxMerge(twit_top_train, arr_twit_top.get(i));
				gnd_train = mxMerge(gnd_train, arr_gnd.get(i));
			}
		}
		arr_fb_train.add(fb_train);
		arr_twit_train.add(twit_train);
		arr_quora_train.add(quora_train);
		arr_twit_top_train.add(twit_top_train);
		arr_gnd_train.add(gnd_train);

		arr_fb_valid.add(fb_valid);
		arr_twit_valid.add(twit_valid);
		arr_quora_valid.add(quora_valid);
		arr_twit_top_valid.add(twit_top_valid);
		arr_gnd_valid.add(gnd_valid);

		arr_fb_test.add(fb_test);
		arr_twit_test.add(twit_test);
		arr_quora_test.add(quora_test);
		arr_twit_top_test.add(twit_top_test);
		arr_gnd_test.add(gnd_test);
	}

	private void generateKFold(int k, ArrayList<Integer> idx, double[][] fb, double[][] twit, double[][] quora,
			double[][] twit_top, double[][] gnd) {
		int remain = idx.size() % k;
		int n = (int) idx.size() / k;
		int index = -1;

		for (int i = 0; i < remain; i++) {
			double[][] fb_fold = new double[n + 1][fb[0].length];
			double[][] twit_fold = new double[n + 1][twit[0].length];
			double[][] quora_fold = new double[n + 1][quora[0].length];
			double[][] twit_top_fold = new double[n + 1][twit_top[0].length];
			double[][] gnd_fold = new double[n + 1][gnd[0].length];
			for (int j = 0; j < n + 1; j++) {
				index += 1;
				int id = idx.get(index);
				fb_fold[j] = fb[id];
				twit_fold[j] = twit[id];
				quora_fold[j] = quora[id];
				twit_top_fold[j] = twit_top[id];
				gnd_fold[j] = gnd[id];
			}
			this.arr_fb.add(fb_fold);
			this.arr_twit.add(twit_fold);
			this.arr_quora.add(quora_fold);
			this.arr_twit_top.add(twit_top_fold);
			this.arr_gnd.add(gnd_fold);
		}

		for (int i = remain; i < k; i++) {
			double[][] fb_fold = new double[n][fb[0].length];
			double[][] twit_fold = new double[n][twit[0].length];
			double[][] quora_fold = new double[n][quora[0].length];
			double[][] twit_top_fold = new double[n][twit_top[0].length];
			double[][] gnd_fold = new double[n][gnd[0].length];
			for (int j = 0; j < n; j++) {
				index += 1;
				int id = idx.get(index);
				fb_fold[j] = fb[id];
				twit_fold[j] = twit[id];
				quora_fold[j] = quora[id];
				twit_top_fold[j] = twit_top[id];
				gnd_fold[j] = gnd[id];
			}
			this.arr_fb.add(fb_fold);
			this.arr_twit.add(twit_fold);
			this.arr_quora.add(quora_fold);
			this.arr_twit_top.add(twit_top_fold);
			this.arr_gnd.add(gnd_fold);
		}
	}

	private ArrayList<Integer> shuffle(int n) {
		ArrayList<Integer> result = new ArrayList<Integer>();
		ArrayList<Integer> idx = new ArrayList<Integer>();
		for (int i = 0; i < n; i++) {
			idx.add(i);
		}
		while (idx.size() > 0) {
			int rand_idx = (int) (Math.random() * idx.size());
			result.add(idx.get(rand_idx));
			idx.remove(rand_idx);
		}
		return result;
	}

	public static void main(String[] args) throws IOException {
		double[][] fb = DataIO.readData("./data/user_topic_facebook.csv");
		double[][] twit = DataIO.readData("./data/user_topic_twitter.csv");
		double[][] quora = DataIO.readData("./data/user_topic_quora.csv");
		double[][] twit_top = DataIO.readData("./data/contextual_topic_twitter.csv");
		double[][] gnd = DataIO.readData("./data/gnd.csv");
		TTest test = new TTest(10, fb, twit, quora, twit_top, gnd);
		test.OutputT("D:/Program Space/Java Space/MultiSource_withoutB_a/SepData/");
	}
}
