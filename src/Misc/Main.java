package Misc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import static java.nio.file.StandardCopyOption.*;

import javax.json.JsonArray;

import Jama.Matrix;
import Utility.JsonFileReader;
import Wrapper.Facebook;
import Wrapper.LinkedIn;
import Wrapper.Tweet;

public class Main {
	
	public static void main(String[] args) throws IOException {
		readFromTwitterFile("test");
//		readFromLinkedInFile("test");
		readFromFacebookFile("test");
	}

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

	public static void runLDA(String source, int numTopics, int optimizeInterval) {
		// Test the CRF and using the Model File
		String[] commandTest = {"cmd", 
		"/c" ,
		"%MALLET_HOME%\\bin\\mallet",
		"train-topics",
		"--input",
		source+"_processed.mallet",
		"--num-topics",
		Integer.toString(numTopics),
		"--optimize-interval",
		Integer.toString(optimizeInterval),
		"--output-state",
		"topic-state.gz",
		"--output-topic-keys",
		source+"_keys.txt",
		"--output-doc-topics",
		source+"_composition.txt"};
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
		runLDA("twitter",GlobalHelper.numTopicsLDA,GlobalHelper.numOptimizeIntervalLDA);
		System.err.println("Topic Modelling Completed!");
		
		writeTrainAndTest("twitter");
	}
	
	public static void processLinkedIn() {
		ArrayList<LinkedIn> linkedinList = readFromLinkedInFile("train");
		ArrayList<LinkedIn> linkedinListTest = readFromLinkedInFile("test");
		for(int i=0;i<linkedinListTest.size();i++) {
			linkedinList.add(linkedinListTest.get(i));
		}
		writeFromLinkedInObject(linkedinList);
		
		System.err.println("Pre-processing LinkedIn Text for LDA....");
		processText("linkedin");
		System.err.println("Running Topic Modelling (LDA)..");
		System.err.println("Please wait patiently, it will take a while...");
		runLDA("linkedin",GlobalHelper.numTopicsLDA,GlobalHelper.numOptimizeIntervalLDA);
		System.err.println("Topic Modelling Completed!");
		
		writeTrainAndTest("linkedin");
	}
	
	public static void writeTrainAndTest(String source) {		
		try {
			TreeMap<Integer, ArrayList<Double>> featuresMap = new TreeMap<Integer, ArrayList<Double>>();
			
			BufferedReader br = new BufferedReader(new FileReader(GlobalHelper.pathToProcessed+"/"+source+"_composition.txt"));
	
			String line;
			while ((line = br.readLine()) != null) {
				String tokens[] = line.split("\\t");
				
				String filename = tokens[1].substring(tokens[1].lastIndexOf('/') + 1);
				String filenumber = filename.substring(0, filename.length()-4);
				// get the filenumber, which is the user id
				int index = Integer.parseInt(filenumber);
				
				// read one row of real numbers
				ArrayList<Double> features = new ArrayList<Double>();
				for(int i=2;i<tokens.length;i++){
					features.add(Double.parseDouble(tokens[i]));
				}
				featuresMap.put(index,features);
			}
			
			PrintWriter pwTrain = new PrintWriter(GlobalHelper.pathToSVMData+"/"+source+"_train.csv");
			PrintWriter pwTest = new PrintWriter(GlobalHelper.pathToSVMData+"/"+source+"_test.csv");
			for(Map.Entry<Integer,ArrayList<Double>> entry : featuresMap.entrySet()) {
				Integer key = entry.getKey();
				ArrayList<Double> value = entry.getValue();
				if (key <= GlobalHelper.numTraining) {
					pwTrain.print(value.get(0));
					for(int i=1;i<value.size();i++){
						pwTrain.print(","+value.get(0));
					}
					pwTrain.println();
					pwTrain.flush();
				}
				else {
					pwTest.print(value.get(0));
					for(int i=1;i<value.size();i++){
						pwTest.print(","+value.get(0));
					}
					pwTest.println();
					pwTest.flush();
				}
			}
			pwTrain.close();
			pwTest.close();
			
			
			Files.copy(Paths.get(GlobalHelper.pathToTrainGroundTruth+"/groundTruth.txt"), 
					Paths.get(GlobalHelper.pathToSVMData+"/gnd_train.csv"),
					REPLACE_EXISTING);
			
			Files.copy(Paths.get(GlobalHelper.pathToTestGroundTruth+"/groundTruth.txt"), 
					Paths.get(GlobalHelper.pathToSVMData+"/gnd_test.csv"),
					REPLACE_EXISTING);
		}
		catch (Exception e) { e.printStackTrace(); }
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
	
	
	public static void writeFromLinkedInObject(ArrayList<LinkedIn> linkedinList) {
		for(int i=0;i<linkedinList.size();i++) {
			PrintWriter pw = null;
			try{ pw = new PrintWriter(GlobalHelper.pathToProcessedLinkedIn+"/"+(i+1)+".txt");}
			catch (Exception e) { e.printStackTrace(); }
			pw.println(linkedinList.get(i).getText());
			pw.flush();
			pw.close();
		}
	}
	
	//	ArrayList of LinkedIn, associated with a user
	public static ArrayList<LinkedIn> readFromLinkedInFile(String type) {
		String LinkedInFolder = null;
		int numRecords = 0;
		int offset = 0;
		if (type.equals("train")) {
			LinkedInFolder = GlobalHelper.pathToTrainLinkedIn;
			numRecords = GlobalHelper.numTraining;
			offset = 1;
		}
		else {
			LinkedInFolder = GlobalHelper.pathToTestLinkedIn;
			numRecords = GlobalHelper.numTest;
			offset = GlobalHelper.numTraining+1;
		}
		
		ArrayList<LinkedIn> linkedinList = new ArrayList<LinkedIn>();
		for(int userId=0;userId<numRecords;userId++) {
			File fileEntry = new File(LinkedInFolder+"\\U"+Integer.toString(userId+offset));
			String profileIdDir = fileEntry.toString();
			String profileId = profileIdDir.substring(profileIdDir.lastIndexOf('\\') + 1);
			
			LinkedIn data = new LinkedIn(profileId, LinkedInFolder);
			linkedinList.add(data);
		}
		System.out.println("===============FINISH READING LINKEDIN FILES===============");
		return linkedinList;
	}
	
	public static ArrayList<Facebook> readFromFacebookFile(String type) {
		String facebookFolder = null;
		int numRecords = 0;
		int offset = 0;
		if (type.equals("train")) {
			facebookFolder = GlobalHelper.pathToTrainFacebook;
			numRecords = GlobalHelper.numTraining;
			offset = 1;
		}
		else {
			facebookFolder = GlobalHelper.pathToTestFacebook;
			numRecords = GlobalHelper.numTest;
			offset = GlobalHelper.numTraining+1;
		}
		
		ArrayList<Facebook> facebookList = new ArrayList<Facebook>();
		for(int userId=0;userId<numRecords;userId++) {
			File fileEntry = new File(facebookFolder+"\\U"+Integer.toString(userId+offset));
			String profileIdDir = fileEntry.toString();
			String profileId = profileIdDir.substring(profileIdDir.lastIndexOf('\\') + 1);
			
			Facebook data = new Facebook(profileId, facebookFolder);
			facebookList.add(data);
		}
		System.out.println("===============FINISH READING FACEBOOK FILES===============");
		return facebookList;
	}
}
