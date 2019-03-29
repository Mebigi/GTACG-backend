package Wrapper;

import java.io.IOException;

import ToolkitFile.AlignmentFile;
import ToolkitFile.TreeFile;

public class FastTree {
	public static void makeTree(AlignmentFile in, TreeFile out) throws IOException, InterruptedException {
		makeTree(in, out, "");
	}
	
	public static void makeTree(AlignmentFile in, TreeFile out, String args) throws IOException, InterruptedException {
		if(in.exists()) {
			/*System.out.println("fasttree" + 
					" -out " + out.getAbsolutePath() +
					" " + args +
					" " + in.getAbsolutePath());*/
			Runtime rt = Runtime.getRuntime();
			Process p;
				p = rt.exec("fasttree" + 
						" -out " + out.getAbsolutePath() +
						" " + args + 
						" " + in.getAbsolutePath() + "\n");
				p.getOutputStream().write("\n".getBytes());
				p.waitFor();
			
		}
	}
}
