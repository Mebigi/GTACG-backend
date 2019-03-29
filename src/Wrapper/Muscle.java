package Wrapper;

import java.io.IOException;

import ToolkitFile.AlignmentFile;
import ToolkitFile.FastaFile;

public class Muscle {
	public static void makeAlignment(FastaFile in, AlignmentFile out, Integer numThreads) throws IOException, InterruptedException {
		if(in.exists()) {
			Runtime rt = Runtime.getRuntime();
			Process p = rt.exec("muscle" + 
					" -in " + in.getAbsolutePath() +
					" -out " + out.getAbsolutePath() + 
					" ");
			int i = 0;
			while(p.isAlive() && i < 300) {
				Thread.sleep(1000);
				i++;
			}
			if(i >= 300) {
				byte buffer [] = new byte[p.getInputStream().available()];
				p.getInputStream().read(buffer);
				System.out.println(new String(buffer));
			}
				
			p.waitFor();
			out.seqType = in.seqType;
		}
	}
}
