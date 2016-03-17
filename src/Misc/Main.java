package Misc;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;

import javax.json.JsonArray;

import Jama.Matrix;
import Utility.JsonFileReader;
import Wrapper.Tweet;

public class Main {
	
//	public static void main(String[] args) throws IOException {
////		TTest T = new TTest("./SepData/");
////		for (int i = 0; i < 10; i++) {
////			Process(T, i);
////		 }
//		readFromTwitterFile("test");
//	}
//
//	private static void Process(double[][] fb, double[][] twit, double[][] quora, double[][] twit_top, double[][] gnd,
//			double[][] valid) {
//		System.out.println("Data Loading Finished...");
//		Parameter pm = new Parameter(fb, twit, quora, twit_top, gnd, valid);
//		Model md = new Model(pm);
//		md.TrainingProcess();
//	}
//
//	private static void Process(TTest T, int i) {
//		System.out.println("Data Loading Finished...");
//		Parameter pm = new Parameter(T, i);
//		Model md = new Model(pm);
//		double value = md.TrainingProcess();
//	}
	
	public static void processText(String source) {
		// Train the CRF and get Model File
		String[] commandTrain = {"cmd", 
		"/c" ,
		"%MALLET_HOME%\\bin\\mallet",
		"import-dir",
		"--input",
		"./"+source,
		"--output",
		source+"_processed.mallet",
		"--keep-sequence",
		"--remove-stopwords"};
		// System.out.println(Arrays.toString(commandTrain));
		ExecuteCommandHelper.runExecutable(commandTrain, new File(GlobalHelper.pathToProcessed));
	}

	public static void runLDA(String source, int numTopics) {
		// Test the CRF and using the Model File
		String[] commandTest = {"cmd", 
		"/c" ,
		"%MALLET_HOME%\\bin\\mallet",
		"train-topics",
		"--input",
		source+"_processed.mallet",
		"--num-topics",
		Integer.toString(numTopics),
		"--output-state",
		"topic-state.gz",
		"--output-topic-keys",
		source+"_keys.txt",
		"--output-doc-topics",
		source+"_compositition.txt"};
		// System.out.println(Arrays.toString(commandTest));
		ExecuteCommandHelper.runExecutable(commandTest, new File(GlobalHelper.pathToProcessed));
	}
	
	public static void processTwitter() {
		ArrayList<ArrayList<Tweet>> userTweetList = readFromTwitterFile("train");
		ArrayList<ArrayList<Tweet>> userTweetListTest = readFromTwitterFile("test");
		for(int i=0;i<userTweetListTest.size();i++) {
			userTweetList.add(userTweetListTest.get(i));
		}
		writeFromTwitterObject(userTweetList);
		
		System.err.println("Pre-processing Twitter Text for LDA....");
		processText("twitter");
		System.err.println("Running Topic Modelling (LDA)..");
		System.err.println("Please wait patiently, it will take a while...");
		runLDA("twitter",30);
		System.err.println("Topic Modelling Completed!");
	}
	
	public static void writeFromTwitterObject(ArrayList<ArrayList<Tweet>> userTweetList) {
		for(int i=0;i<userTweetList.size();i++) {
			StringBuilder sb = new StringBuilder();
			for(int j=0;j<userTweetList.get(i).size();j++){
				sb.append(userTweetList.get(i).get(j).getText()+"\n");
			}
			PrintWriter pw = null;
			try{ pw = new PrintWriter(GlobalHelper.pathToProcessedTwitter+"/"+(i+1)+".txt");}
			catch (Exception e) { e.printStackTrace(); }
			pw.println(sb.toString());
			pw.flush();
			pw.close();
		}
	}
	
	//	ArrayList of tweet, associated with a user
	public static ArrayList<ArrayList<Tweet>> readFromTwitterFile(String type) {
		String twitterFolder = null;
		int numRecords = 0;
		int offset = 0;
		if (type.equals("train")) {
			twitterFolder = GlobalHelper.pathToTrainTwitter;
			numRecords = GlobalHelper.numTraining;
			offset = 1;
		}
		else {
			twitterFolder = GlobalHelper.pathToTestTwitter;
			numRecords = GlobalHelper.numTest;
			offset = GlobalHelper.numTraining+1;
		}
		
		ArrayList<ArrayList<Tweet>> userTweetList = new ArrayList<ArrayList<Tweet>>();
		for(int userId=0;userId<numRecords;userId++) {
			File fileEntry = new File(twitterFolder+"\\U"+Integer.toString(userId+offset));
			String profileIdDir = fileEntry.toString();
			String profileId = profileIdDir.substring(profileIdDir.lastIndexOf('\\') + 1);
			
			ArrayList<Tweet> tweetList = new ArrayList<Tweet>();
			
			if (fileEntry.isDirectory()) {
				System.out.println(profileIdDir + " " + profileId);
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
				
				userTweetList.add(tweetList);
			}
		}
		System.out.println("===============FINISH READING TWITTER FILES===============");
		return userTweetList;
	}
}
