

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;

import Structure.Graph.GraphM8;
import Structure.Graph.NodeGene;
import Structure.Registry.Dictionary;
import Structure.Restriction.M8Attribute;
import Structure.Restriction.M8Restriction;

public class ExportGraph {

	public static void main(String[] args) throws IOException, InterruptedException {
		File m8File = null;
		File dicFile = null;
		File outFile = null;
		File restFile = null;
		File headsFile = null;
		String dicFormat = null;
		
		/*args = new String[] {
				"-m8", "/home/caio/dados/xyllela/all.faa.m8.gz", 
				"-dic", "/home/caio/dados/xyllela/xf.dic", 
				"-out", "xf.graph50", 
				"-rests", "/home/caio/dados/xyllela/xf.rests50", 
				"-heads", "xf.heads50"};*/
		
		/*args = new String[] {
				"-m8", "/home/caio/dados/myco/prokka/todos.faa.m8.gz", 
				"-dic", "/home/caio/dados/myco/prokka/myco.dic", 
				"-out", "myco.graph40", 
				"-rests", "myco.rests40", 
				"-heads", "myco.heads40"};*/
		
		/*args = new String[] {
				"-m8", "/home/caio/dados/xyllela/all_genom.m8.gz", 
				"-dic", "/home/caio/dados/xyllela/all_genom.dic", 
				"-rests", "/home/caio/dados/xyllela/76.rests", 
				"-out", "xy.graph76", 
				"-heads", "xy.heads76"};*/
		
		/*args = new String[] {
				"-m8", "/media/caio/08CE4A891258E142/dados/myco2/prokka/all.faa.m8.gz", 
				"-dic", "/media/caio/08CE4A891258E142/dados/myco2/prokka/myco.dic", 
				"-rests", "/media/caio/08CE4A891258E142/dados/myco2/prokka/rests/45.rests", 
				"-out", "/media/caio/08CE4A891258E142/dados/myco2/prokka/myco.graph45", 
				"-heads", "/media/caio/08CE4A891258E142/dados/myco2/prokka/myco.heads45"};*/
		
		for (int i = 0; i < args.length; i++) {
			if("-help".equals(args[i])) {
				System.out.println("Input Files");
				System.out.println("\t-m8");
				System.out.println("\t-dic");
				System.out.println("\t-rests");
				System.out.println();
				System.out.println("Output Files");
				System.out.println("\t-out");
				System.out.println("\t[-heads]");
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
			else if("-dicFormat".equals(args[i])) {
				if(args.length > i + 1)
					dicFormat = args[i+1];
			}
			else if("-out".equals(args[i])) {
				if(args.length > i + 1)
				outFile = new File(args[i+1]);
			}
			else if("-rests".equals(args[i])) {
				if(args.length > i + 1)
				restFile = new File(args[i+1]);
				if(!restFile.exists()) {
					System.out.println("Restriction file not found");
					return;
				}
			}
			else if("-heads".equals(args[i])) {
				if(args.length > i + 1)
				headsFile = new File(args[i+1]);
			}
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
		
		System.setOut(new PrintStream(new File("out")));
		System.setErr(new PrintStream(new File("err")));
		
		Dictionary dic = Dictionary.getDictionary(dicFile, dicFormat);
		GraphM8 graph = null;
		LinkedList<M8Restriction> layers = null;
		M8Restriction baseRest = null;
		if(restFile != null) {
			layers = M8Restriction.loadFile(restFile);
			baseRest = layers.getFirst();
		}
		
		if(m8File.getName().endsWith(".gz"))
			graph = new GraphM8(m8File, "gz", dic, baseRest);
		else
			graph = new GraphM8(m8File, dic, baseRest);
		
		graph.addAllRestriction(layers);
		while(graph.hasNextLayer()) {
			System.out.println("ok");
			graph = graph.nextLayer(true);
		}
		
		
			/*graph = new GraphM8(new File("/home/caio/Dropbox/workspace2/framework/myco.graph40"), "", dic);
			NodeGene<String, M8Attribute> node = graph.getNode("CP011491.1_00009");
			GeneRegistry reg = dic.getGeneById("CP011491.1_00009");*/
				
		
		/*try {
			AllResults.statistics(graph, graph.connComponentHeads(), dic.getOrganisms().length, new File("teste2.stats"));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		graph.export(outFile);
		
		if(headsFile != null) {
			LinkedList<NodeGene<String, M8Attribute>> heads = graph.connComponentHeads();
			graph.saveNodeList(headsFile, heads);
			AllResults.statistics(graph, heads, dic.getOrganisms().length, new File("stats"));
		}
	}
}