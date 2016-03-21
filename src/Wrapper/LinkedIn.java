package Wrapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import Misc.GlobalHelper;

public class LinkedIn {
	private String profileId; // eg. U421, U422 (the given ID)
	private String linkedinFolder; // train or test folder?
	private boolean isMissing; // true if linkedin profile is missing
	
	private String fullname;
	private String location;
	private String current; //eg. Technology Project Manager, Organiser Ladies Who Code meetup
	private String summary; //background summary
	private ArrayList<String> skills;
	private String currentExperience;
	private ArrayList<String> pastExperiences;
	private ArrayList<String> honors; 
	private ArrayList<String> organisations;
	private ArrayList<String> interests;
	
	public LinkedIn(String _profileId, String _linkedinFolder) {
		profileId = _profileId;
		linkedinFolder = _linkedinFolder;
		skills = new ArrayList<String>();
		pastExperiences = new ArrayList<String>();
		honors = new ArrayList<String>();
		interests = new ArrayList<String>();
		organisations = new ArrayList<String>();
		isMissing = false;
		this.processLinkedinHtml();
	}
	
	public String getText() {
		StringBuilder sb = new StringBuilder();
//		sb.append(current+"\n");
		sb.append(summary+"\n");
		for(int i=0;i<skills.size();i++) sb.append(skills.get(i)+"\n");
//		sb.append(currentExperience+"\n");
//		for(int i=0;i<pastExperiences.size();i++) sb.append(pastExperiences.get(i)+"\n");
//		for(int i=0;i<honors.size();i++) sb.append(honors.get(i)+"\n");
//		for(int i=0;i<organisations.size();i++) sb.append(organisations.get(i)+"\n");
		for(int i=0;i<interests.size();i++) sb.append(interests.get(i)+"\n");
		String text =  sb.toString().replaceAll("null\n", "");
		
		if (GlobalHelper.useBigram) {
			sb = new StringBuilder(text+" ");
			String[] lines = text.split("\n");
			for(int i=0;i<lines.length;i++) {
				String[] tokens = lines[i].split(" ");
				for (int j=1;j<tokens.length;j++){
					sb.append(tokens[j-1]+"_"+tokens[j]+" ");
				}
				sb.append("\n");
			}
			
			text = sb.toString().trim();
		}
		
		return text;
	}

	private void processLinkedinHtml() {
		File input = new File(linkedinFolder + "/" + profileId + ".html");
		Document doc = null;
		try {
			doc = Jsoup.parse(input, "UTF-8");
		} catch (IOException e) {
			System.out.println("Unable to parse " + input + " using Jsoup!");
			e.printStackTrace();
		}
		
		Element _fullname = doc.select(".full-name").first();
		Element _location = doc.select("#demographics").select(".locality").first();
		Element _current = doc.select(".profile-overview-content").select("p.title").first();
		Element _summary = doc.select(".summary").select(".description").first();
		Elements _skills = doc.select(".skill-pill").select(".endorse-item-name");
		Element _currentExperience = doc.select(".current-position").first();
		Elements _pastExperiences = doc.select(".past-position");
		Elements _honors = doc.select(".background-honors").select(".editable-item");
		Elements _organisations = doc.select(".background-organizations").select(".section-item");
		Elements _interests = doc.select(".interests-listing");
		
		// name doesnt exist means linkedin profile is missing!
		if (_fullname == null) {
			isMissing = true;
			System.out.println(profileId + " : isMissing!");
		} else {
			fullname = _fullname.text();
			System.out.println(profileId + " : " + fullname);
			location = _location.text();
			
			if (current != null)
				current = _current.text();
			
			if (summary != null)
				summary = _summary.text();
			
			for (Element _skill: _skills) {
				skills.add(_skill.text());
			}
			
			if (_currentExperience != null)
				currentExperience = _currentExperience.text();
			
			for (Element _past: _pastExperiences) {
				pastExperiences.add(_past.text());
			}
			
			for (Element _honor: _honors) {
				honors.add(_honor.text());
			}
			
			for (Element _org: _organisations) {
				organisations.add(_org.text());
			}
			
			for (Element _interest: _interests) {
				interests.add(_interest.text());
			}
		}
	}	
}
