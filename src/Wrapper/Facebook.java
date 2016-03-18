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
	
	public String getText() {
		return text;
	}

	private void processFacebookTxt() {
		File input = new File(facebookFolder + "/" + profileId + ".txt");
		try {
			text = FacebookFileReader.readString(input);
			text = text.replaceAll("[0-9]+\\s+Like[s]?", "");
			text = text.replaceAll("[0-9]+\\s+Comment[s]?", "");
			text = text.replaceAll("[0-9]+\\s+Share[s]?", "");
			text = text.replaceAll("View\\s+[0-9]+\\s+more\\s+comments", "");
			text = text.replaceAll("Like\\s+Comment\\s+Share", "");
			text = text.replaceAll("[0-9]+\\s+people\\s+like\\s+this", "");
			text = text.replaceAll("Write\\s+a\\s+comment\\.\\.\\.", "");
			text = text.replaceAll("Press\\s+Enter\\s+to\\s+post", "");
			text = text.replaceAll("Like\\s+Reply", "");
			text = text.replaceAll("Automatically\\s+Translated\\s+See\\s+Original\\s+Like\\s+Share", "");
			text = text.replaceAll("See\\s+Translation", "");
			text = text.replaceAll("[A-Za-z]+\\s+[0-9]+\\s+at\\s+[0-9]+\\:[0-9]+[ap]m", "");
			text = text.replaceAll("Edited", "");
			text = text.replaceAll("changed\\s+(?:(?:her)|(?:his))\\s+profile\\s+picture", "");
			text = text.replaceAll("updated\\s+(?:(?:her)|(?:his))\\s+cover\\s+photo", "");
			text = text.replaceAll("Share", "");
			text = text.replaceAll("Remove", "");
			
		} catch (IOException e) {
			System.err.println("Error while reading " + input.getName() + "facebook!");
			e.printStackTrace();
		}
	}
}
