package Misc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.json.JsonArray;

import Jama.Matrix;
import Utility.JsonFileReader;
import Wrapper.Tweet;

public class Main {
	
	public static void main(String[] args) throws IOException {
//		TTest T = new TTest("./SepData/");
//		for (int i = 0; i < 10; i++) {
//			Process(T, i);
//		 }
		readFromTwitterFile();
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
	
	public static ArrayList<Tweet> readFromTwitterFile() {
		File twitterTestFolder = new File(GlobalHelper.pathToTestTwitter); 
		ArrayList<Tweet> tweetList = new ArrayList<Tweet>();
		
		for (File fileEntry: twitterTestFolder.listFiles()) {
			// directory contains the profileID
			if (fileEntry.isDirectory()) {
				String profileIdDir = fileEntry.toString();
				String profileId = profileIdDir.substring(profileIdDir.lastIndexOf('\\') + 1);
				for (File twitterJson: fileEntry.listFiles()) {
					String twitterJsonDir = twitterJson.toString();
					JsonArray tweetJsonArray = null;
					
					try {
						tweetJsonArray = JsonFileReader.readJsonObject(twitterJsonDir);
					} catch (IOException e) {
						System.err.println("Error while reading JSON tweet text! Exception caught!");
						e.printStackTrace();
					}
					
					// some of the tweets are totally empty
					if (tweetJsonArray == null) {
						System.out.println(twitterJsonDir + " is empty!");
					} else {
						for (int i = 0; i < tweetJsonArray.size(); i++) {
							Tweet data = new Tweet(profileId, tweetJsonArray.getJsonObject(i));
							tweetList.add(data);
						}
					}
				}
			}
		}
		System.out.println("===============FINISH READING TWITTER FILES===============");
		return tweetList;
	}
}
