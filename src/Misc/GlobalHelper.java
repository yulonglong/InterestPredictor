package Misc;

import java.nio.file.*;

public class GlobalHelper {
	public static final String pathToTestFacebook = "./dataset/Test/Facebook";
	public static final String pathToTestLinkedIn = "./dataset/Test/LinkedIn";
	public static final String pathToTestTwitter = "./dataset/Test/Twitter";
	public static final String pathToTestGroundTruth = "./dataset/Test/GroundTruth";
	public static final String pathToTrainFacebook = "./dataset/Train/Facebook";
	public static final String pathToTrainLinkedIn = "./dataset/Train/LinkedIn";
	public static final String pathToTrainTwitter = "./dataset/Train/Twitter";
	public static final String pathToTrainGroundTruth = "./dataset/Train/GroundTruth";
	public static final int numTraining = 420;
	public static final int numTest = 150;
	public static final String f_currPathStr = Paths.get("").toAbsolutePath().toString();
	
	public static final String pathToProcessed = "./dataset/Processed";
	public static final String pathToProcessedTwitter = "./dataset/Processed/Twitter";
	public static final String pathToSVMData = "./dataset/Processed/SVM_Data";
	
	public static final int numTopicsLDA = 40;
	public static final int numOptimizeIntervalLDA = 10;
}
