package Wrapper;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

public class FigTree {
	public static void printTree(File in, boolean showFile, File out) throws IOException, InterruptedException {
		Process process = 
                new ProcessBuilder(new String[] {"bash", "-c", "figtree -graphic PDF " + in.getPath() + " " + out.getAbsolutePath()})
                    .redirectErrorStream(true)
                    .start();
		process.waitFor();
		
		if(showFile)
			Runtime.getRuntime().exec("gnome-open " + out.getAbsolutePath());
	}
	
	public static void printTree(String in, boolean showFile, File out) throws IOException, InterruptedException {
		File f = new File("saida.tmp.phylip");
		
		PrintStream stream = new PrintStream(f);
		stream.print(in);
		stream.close();
		printTree(f, showFile, out);
		
		f.delete();
	}
}
