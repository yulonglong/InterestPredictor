package Utility;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Vector;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;


public class FacebookFileReader {
	public static String readString(File file) throws IOException{
		StringBuilder sb = new StringBuilder();
		
		if(file.isFile() && file.exists()){
			InputStreamReader read = new InputStreamReader(new FileInputStream(file));
			BufferedReader bufferedReader = new BufferedReader(read);
			String lineTxt = null;
			while((lineTxt = bufferedReader.readLine()) != null)
				sb.append(lineTxt);
			read.close();
		}else{
			System.err.println("can not find input file: " + file.getName());
		}
		return sb.toString();
	}
}
