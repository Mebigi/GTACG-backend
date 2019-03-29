package Wrapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

import Structure.Alignment;
import Structure.Constants;
import ToolkitFile.AlignmentFile;
import ToolkitFile.ToolkitBaseFile;
import ToolkitFile.ToolkitBaseFile.FileType;
import ToolkitFile.TreeFile;

public class Phyml {
	private static String translateSequeceType(ToolkitBaseFile.SequenceType type) {
		if(type == ToolkitBaseFile.SequenceType.AminoAcids)
			 return "aa";
		else if (type == ToolkitBaseFile.SequenceType.Nucleotides)
			return "nt";
		return "generic";
	}
	
	public static void makeTree(AlignmentFile in, TreeFile out) throws IOException, InterruptedException {
		makeTree(in, out, " --no_memory_check ");
	}
	
	public static void makeTree(AlignmentFile in, TreeFile out, String args) throws IOException, InterruptedException {
		if(in.exists()) {
			boolean converted = false;
			AlignmentFile inFormatted = in;
			
			HashMap<String, String> map = new HashMap<>();
			if(in.fileType == FileType.fasta) {
				Alignment align = new Alignment(in);
				
				inFormatted = new AlignmentFile(Constants.rand() + ".align", FileType.phylip);
				PrintStream stream = new PrintStream(inFormatted);
				stream.println(align.numSequences() + " " + align.size());
				int it = 0;
				for (Entry<String, StringBuilder> ent : align.getSequences()) {
					String translated = "@" + (it++) + "@"; 
					map.put(translated, ent.getKey());
					stream.println((translated + "                ").substring(0, 10) + ent.getValue().toString());
				}
				stream.close();
				converted = true;
			}
			
			Random r = new Random();
			int id = r.nextInt(100000);
			
			Runtime rt = Runtime.getRuntime();
			Process p = rt.exec("phyml" + 
					" -i " + inFormatted.getAbsolutePath() +
					" -d " + translateSequeceType(in.seqType) +
					" --run_id " + id +
					" " + args + "\n");
			
			p.getOutputStream().write("\n".getBytes());
			
			p.waitFor();
			
			File f1 = new File(inFormatted.getAbsolutePath() + "_phyml_tree.txt_" + id);
			File f2 = new File(inFormatted.getAbsolutePath() + "_phyml_stats.txt_" + id);
			//System.err.println(f1.renameTo(out));
			Scanner sc = new Scanner(f1);
			PrintStream stream = new PrintStream(out);
			while(sc.hasNextLine())
				stream.println(sc.nextLine());
			sc.close();
			stream.close();
			
			f1.delete();
			f2.delete();
			if(converted) {
				inFormatted.delete();
				for (Entry<String, String> ent : map.entrySet()) {
					out.replace(ent.getKey(), ent.getValue());
					
				}
			}
		}
	}
}
