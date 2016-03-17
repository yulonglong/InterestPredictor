package Misc;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.*;


class StreamGobbler extends Thread {
	InputStream is;
	String type;
	boolean printConsole = false;

	public StreamGobbler(InputStream is, String type, boolean _printConsole) {
		this.is = is;
		this.type = type;
		printConsole = _printConsole;
	}

	@Override
	public void run() {
		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null) {
				if (printConsole)
					System.out.println(type + "> " + line);
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}

public class ExecuteCommandHelper {
	public static void runExecutable(String[] command, File runDirectory) {
		try {
			// String[] command = {"CMD", "/C", "dir"};
			ProcessBuilder pb = new ProcessBuilder(command);
			pb.directory(runDirectory);
			Process process = pb.start();
			StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), "ERROR", false);
			StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), "OUTPUT", false);
			outputGobbler.start();
			errorGobbler.start();
			// Wait to get exit value
			try {
				process.waitFor();
				// int exitValue = process.waitFor();
				// System.out.println("\n\nExit Value is " + exitValue);
			} catch (InterruptedException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			return;
		} catch (IOException e1) {
			System.out.println("Error, exception while running executable : " + e1);
			
		}
		return;
	}
}
