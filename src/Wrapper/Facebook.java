package Wrapper;

import java.io.File;
import java.io.IOException;

import Utility.FacebookFileReader;

public class Facebook {
	private String profileId; // eg. U421, U422 (the given ID)
	private String facebookFolder; // train or test folder?
	
	private String text;
	
	public Facebook(String _profileId, String _facebookFolder) {
		profileId = _profileId;
		facebookFolder = _facebookFolder;
		this.processFacebookTxt();
	}

	private void processFacebookTxt() {
		File input = new File(facebookFolder + "/" + profileId + ".txt");
		try {
			text = FacebookFileReader.readString(input);
		} catch (IOException e) {
			System.err.println("Error while reading " + input.getName() + "facebook!");
			e.printStackTrace();
		}
	}
}
