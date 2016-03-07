package Utility;

import java.io.*;
import java.util.ArrayList;

public class IO {
	public static ArrayList FileLoad(String path) throws IOException {
		ArrayList<String> arr = new ArrayList<String>();
		File f = new File(path);
		FileInputStream fis = new FileInputStream(f);
		InputStreamReader isr = new InputStreamReader(fis, "utf-8");
		BufferedReader br = new BufferedReader(isr);
		String line = "";
		while ((line = br.readLine()) != null) {
			line = line.trim();
			arr.add(line);
		}
		br.close();
		isr.close();
		fis.close();
		return arr;
	}

	public static void FileAppend(String path, String content) throws IOException {
		File f = new File(path);
		FileOutputStream fos = new FileOutputStream(f, true);
		OutputStreamWriter osw = new OutputStreamWriter(fos, "utf-8");
		BufferedWriter bw = new BufferedWriter(osw);
		bw.write(content);
		bw.close();
		osw.close();
		fos.close();
	}

	public static void FileAppend(String path, StringBuffer content) throws IOException {
		File f = new File(path);
		FileOutputStream fos = new FileOutputStream(f, true);
		OutputStreamWriter osw = new OutputStreamWriter(fos, "utf-8");
		BufferedWriter bw = new BufferedWriter(osw);
		bw.write(content.toString());
		bw.close();
		osw.close();
		fos.close();
	}

	public static void FileWrite(String path, StringBuffer content) throws IOException {
		File f = new File(path);
		FileOutputStream fos = new FileOutputStream(f, false);
		OutputStreamWriter osw = new OutputStreamWriter(fos, "utf-8");
		BufferedWriter bw = new BufferedWriter(osw);
		bw.write(content.toString());
		bw.close();
		osw.close();
		fos.close();
	}

	public static void FileWrite(String path, String content) throws IOException {
		File f = new File(path);
		FileOutputStream fos = new FileOutputStream(f, false);
		OutputStreamWriter osw = new OutputStreamWriter(fos, "utf-8");
		BufferedWriter bw = new BufferedWriter(osw);
		bw.write(content);
		bw.close();
		osw.close();
		fos.close();
	}
}
