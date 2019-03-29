
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;

import HeaderExtractor.HeaderExtractorPatric;
import Structure.Graph.GraphM8;
import Structure.Graph.NodeGene;
import Structure.Registry.Dictionary;
import Structure.Restriction.M8Attribute;
import Structure.Restriction.M8Restriction;

public class ExploratoryClustering {
	public static void main(String[] args) throws NumberFormatException, IOException, InterruptedException {
		int start = 20;
		int end = 100;
		int interval = 1;
		int threads = Runtime.getRuntime().availableProcessors() - 1;
		double evalueInterval = 1;
		
		File dicFile = null;
		File m8File = null;
		String typeFile = "";
		boolean printStats = false;
		boolean extended = true;
		String dicFormat = null;
		
		/*args = new String[] {
				"-dic", "/home/caio/dados/myco2/prokka/myco.dic",
				"-m8", "/home/caio/dados/myco2/prokka/todos.faa.m8.gz",
				"-threads", "4",
				"-start", "40",
				"-end", "100",
				"-interval", "1"};*/
		
		/*args = new String[] {
				"-dic", "/media/caio/08CE4A891258E142/dados/myco2/prokka/myco.dic",
				"-m8", "/media/caio/08CE4A891258E142/dados/myco2/prokka/all.faa.m8.gz",
				"-threads", "4",
				"-start", "37",
				"-end", "100",
				"-interval", "1",
				"-evalueInterval", "1"};*/
		
		for (int i = 0; i < args.length; i++) {
			if("-help".equals(args[i])) {
				System.out.println("Input Files");
				System.out.println("\t-dic");
				System.out.println("\t-m8");
				System.out.println();
				System.out.println("Parameters");
				System.out.println("\t[-threads]");
				System.out.println("\t[-start]");
				System.out.println("\t[-end]");
				System.out.println("\t[-interval]");
				System.out.println("\t[-printStats]");
				return;
			}
			else if("-dic".equals(args[i])) {
				if(args.length > i + 1)
				dicFile = new File(args[i+1]);
				if(!dicFile.exists()) {
					System.out.println("Dictionary file not found");
					return;
				}
			}
			else if("-dicFormat".equals(args[i])) {
				if(args.length > i + 1)
					dicFormat = args[i+1];
			}
			else if("-m8".equals(args[i])) {
				if(args.length > i + 1)
				m8File = new File(args[i+1]);
				if(!m8File.exists()) {
					System.out.println("M8 file not found");
					return;
				}
				if(m8File.getName().endsWith(".gz"))
					typeFile = "gz";
			}
			else if("-threads".equals(args[i])) {
				threads = Integer.parseInt(args[i+1]);
			}
			else if("-start".equals(args[i])) {
				start = Integer.parseInt(args[i+1]);
			}
			else if("-end".equals(args[i])) {
				end = Integer.parseInt(args[i+1]);
			}
			else if("-interval".equals(args[i])) {
				interval = Integer.parseInt(args[i+1]);
			}
			else if("-evalueInterval".equals(args[i])) {
				evalueInterval = Double.parseDouble(args[i+1]);
			}
			else if("-printStats".equals(args[i])) {
				printStats = true;
				i--;
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
		
		System.setOut(new PrintStream(new File("out")));
		System.setErr(new PrintStream(new File("err")));
		
		M8Restriction baseRest = new M8Restriction(start, 0, 0, Short.MAX_VALUE, Short.MAX_VALUE, Math.pow(10, -10), 0);
		Dictionary dic = Dictionary.getDictionary(dicFile, dicFormat);
		
		GraphM8 graph = new GraphM8(m8File, typeFile, dic, baseRest);
		for (int i = start; i < end; i+=interval) {
			System.out.println("-- " + i);
			M8Restriction rest = new M8Restriction(i, 0, 0, Short.MAX_VALUE, Short.MAX_VALUE, Math.pow(10, -10), 0);
			graph.updateRestrictions(rest);
			LinkedList<M8Restriction> lista = graph.hierClustering(10, 200, evalueInterval, rest, threads, extended);
			if(lista != null && !lista.isEmpty()) {
				M8Restriction.saveFile(new File(i + ".rests"), lista);
				if(printStats) {
					GraphM8 graph2 = new GraphM8(m8File, typeFile, dic, null);
					graph2.updateRestrictions(lista.getFirst());
					graph2.addAllRestriction(lista);
		
					GraphM8.printLevels(graph2, new HeaderExtractorPatric(), new PrintStream(new File(i + ".hier")));
					while(graph2.hasNextLayer()) {
						graph2 = graph2.nextLayer(true);
					}
					
					LinkedList<NodeGene<String, M8Attribute>> heads = graph2.connComponentHeads();
					AllResults.statistics(graph2, heads, dic.getOrganisms().length, new File(i + ".stats"));
					AllResults.gerarDifLabels(graph2, heads, new HeaderExtractorPatric(), new File(i + ".diffs"));
				}
			}
			System.gc();
		}
	}
}
