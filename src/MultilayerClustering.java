

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;

import Structure.Graph.GraphM8;
import Structure.Registry.Dictionary;
import Structure.Restriction.M8Restriction;

public class MultilayerClustering {

	public static void main(String[] args) throws IOException {
		File m8File = null;
		File dicFile = null;
		File outFile = null;
		int start = 10;
		int end = 200;
		double interval = 1;
		int threads = 1;
		float minPercLenAlign = 30;
		float minPercIdentity = 0;
		int minLengthAlin = 0;
		float maxPercMistmatches = Short.MAX_VALUE;
		short maxGapOpenings = Short.MAX_VALUE;
		double maxEValue = Math.pow(10, -10);
		float minBitScore = 0;
		boolean extended = true;
		
		/*args = new String[] {
				"-m8", "/home/caio/dados/myco/prokka/todos.faa.m8.gz", 
				"-dic", "/home/caio/dados/myco/prokka/myco.dic",
				"-out", "myco.40rests",
				"-minPercLenAlign","40",
				"-threads", "4",
		};*/
		
		for (int i = 0; i < args.length; i++) {
			if("-help".equals(args[i])) {
				System.out.println("Input Files");
				System.out.println("\t-m8");
				System.out.println("\t-dic");
				System.out.println();
				System.out.println("Output Files");
				System.out.println("\t-out");
				System.out.println();
				System.out.println("Parameters");
				System.out.println("\t[-start]");
				System.out.println("\t[-end]");
				System.out.println("\t[-interval]");
				System.out.println("\t[-threads]");
				System.out.println("\t[-minPercLenAlign]");
				System.out.println("\t[-minPercIdentity]");
				System.out.println("\t[-minLengthAlin]");
				System.out.println("\t[-maxPercMistmatches]");
				System.out.println("\t[-maxGapOpenings]");
				System.out.println("\t[-maxEValue]");
				System.out.println("\t[-minBitScore]");
				return;

			}
			else if("-m8".equals(args[i])) {
				if(args.length > i + 1)
				m8File = new File(args[i+1]);
				if(!m8File.exists()) {
					System.out.println("M8 file not found");
					return;
				}
			}
			else if("-dic".equals(args[i])) {
				if(args.length > i + 1)
				dicFile = new File(args[i+1]);
				if(!dicFile.exists()) {
					System.out.println("Dictionary file not found");
					return;
				}
			}
			else if("-out".equals(args[i])) {
				if(args.length > i + 1)
				outFile = new File(args[i+1]);
			}
			else if("-start".equals(args[i])) {
				start = Integer.parseInt(args[i+1]);
			}
			else if("-end".equals(args[i])) {
				end = Integer.parseInt(args[i+1]);
			}
			else if("-interval".equals(args[i])) {
				interval = Double.parseDouble(args[i+1]);
			}
			else if("-threads".equals(args[i])) {
				threads = Integer.parseInt(args[i+1]);
			}
			else if("-minPercLenAlign".equals(args[i])) {
				minPercLenAlign = Float.parseFloat(args[i+1]);
			}
			else if("-minPercIdentity".equals(args[i])) {
				minPercIdentity = Float.parseFloat(args[i+1]);
			}
			else if("-minLengthAlin".equals(args[i])) {
				minLengthAlin = Integer.parseInt(args[i+1]);
			}
			else if("-maxPercMistmatches".equals(args[i])) {
				maxPercMistmatches = Float.parseFloat(args[i+1]);
			}
			else if("-maxGapOpenings".equals(args[i])) {
				maxGapOpenings = Short.parseShort(args[i+1]);
			}
			else if("-maxEValue".equals(args[i])) {
				maxEValue = Double.parseDouble(args[i+1]);
			}
			else if("-minBitScore".equals(args[i])) {
				minBitScore = Float.parseFloat(args[i+1]);
			}
			i++;
		}
		
		if(dicFile == null) {
			System.out.println("Dictionary file not specified");
			return;
		}
		if(m8File == null) {
			System.out.println("M8 file not specified");
			return;
		}
		if(outFile == null) {
			System.out.println("Output file not specified");
			return;
		}
		
		System.setOut(new PrintStream("out"));
		System.setErr(new PrintStream("err"));
		
		M8Restriction rest = new M8Restriction(minPercLenAlign, minPercIdentity, minLengthAlin, maxPercMistmatches, maxGapOpenings, maxEValue, minBitScore);
		Dictionary dic = new Dictionary(dicFile);
		GraphM8 graph = null;
		if(m8File.getName().endsWith(".gz"))
			graph = new GraphM8(m8File, "gz", dic, rest);
		else
			graph = new GraphM8(m8File, dic, rest);
		//graph.updateRestrictions(rest);
		LinkedList<M8Restriction> layers = graph.hierClustering(start, end, interval, rest, threads, extended);
		M8Restriction.saveFile(outFile, layers);
	}
}
