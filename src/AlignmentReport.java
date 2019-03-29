import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedList;

import HeaderExtractor.HeaderExtractorPatric;
import Structure.Constants;
import Structure.Graph.EdgeAttribute;
import Structure.Graph.GraphM8;
import Structure.Graph.NodeGene;
import Structure.Registry.Dictionary;
import Structure.Registry.DictionaryGbf;
import Structure.Registry.OrganismRegistry;
import Structure.Registry.RegistryGroups;
import Structure.Registry.DictionaryGbf.Format;
import Structure.Registry.GeneRegistry;
import Structure.Restriction.M8Attribute;
import ToolkitFile.TreeFile;

public class AlignmentReport {

	public static void main(String[] args) throws IOException {
		Constants.inicializacao();
		File graphFile = null;
		File headsFile = null;
		File dicFile = null;
		String dicFormat = null;
		File treesFolder = null;
		File outFile = null;
		
		/*args = new String[] {
				"-graph", "/media/caio/08CE4A891258E142/dados/streptococcus/patric/graph41.strep.gz", 
				"-heads", "/media/caio/08CE4A891258E142/dados/streptococcus/patric/heads41.strep",
				"-dic", "/media/caio/08CE4A891258E142/dados/streptococcus/patric/streptococcus2.dic",
				"-dicFormat", "gbf",
				"-trees", "/media/caio/08CE4A891258E142/dados/streptococcus/patric/trees41new2",
				"-threads", "4",
				"-ggdc", "/home/caio/Dropbox/DoutoradoCaioSantiago/resultados/streptococcus/ggdc/arvoreFormula3DDH.tree",
				//"-standardTree", "/home/caio/Dropbox/DoutoradoCaioSantiago/resultados/streptococcus/mProtein/emm_gene.tree",
				"-standardTree", "/home/caio/Dropbox/DoutoradoCaioSantiago/resultados/streptococcus/patric/super.tree",
				"-groups", "4", 
					"/media/caio/08CE4A891258E142/dados/streptococcus/patric/doenca.grupo",
			 		"/media/caio/08CE4A891258E142/dados/streptococcus/patric/genotimoM.grupo",
			 		"/media/caio/08CE4A891258E142/dados/streptococcus/patric/invasividade.grupo",
			 		"/media/caio/08CE4A891258E142/dados/streptococcus/patric/pattern.grupo",
			 	//"-printPhylogeny",
			 	//"-printFamilies",
			 	"-printPlot",
		 		"-outFile", "alignments"
		};*/
		
		for (int i = 0; i < args.length; i++) {
			if("-help".equals(args[i])) {
				System.out.println("Input Files");
				System.out.println("\t-graph");
				System.out.println("\t-heads");
				System.out.println("\t-dic");
				System.out.println("\t-groups");
				System.out.println("\t-trees");
				System.out.println();
				System.out.println("Output Files");
				System.out.println("\t-outFile");
				return;
			}
			if("-graph".equals(args[i])) {
				if(args.length > i + 1)
				graphFile = new File(args[i+1]);
				if(!graphFile.exists()) {
					System.out.println("Graph file not found");
					return;
				}
			}
			if("-heads".equals(args[i])) {
				if(args.length > i + 1)
				headsFile = new File(args[i+1]);
				if(!headsFile.exists()) {
					System.out.println("Heads file not found");
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
			else if("-trees".equals(args[i])) {
				if(args.length > i + 1)
				treesFolder = new File(args[i+1]);
				if(!treesFolder.exists()) {
					System.out.println("Trees folder not found");
					return;
				}
			}
			else if("-outFile".equals(args[i])) {
				if(args.length > i + 1)
				outFile = new File(args[i+1]);
			}
			
			else 
				i--;
			i++;
		}
		
		
		System.setOut(new PrintStream("out"));
		System.setErr(new PrintStream("err"));
		
		Dictionary dicTmp = null;
		if(dicFormat == null || dicFormat.equals("faa"))
			dicTmp = new Dictionary(dicFile);
		else if(dicFormat.equals("gbf"))
			dicTmp = new DictionaryGbf(dicFile, Format.GBF);
		else if (dicFormat.equals("gff"))
			dicTmp = new DictionaryGbf(dicFile, Format.GFF);
		Dictionary dic = dicTmp;

		GraphM8 graph = null;
		if(graphFile.getName().endsWith(".gz"))
			graph = new GraphM8(graphFile, "gz", dic);
		else
			graph = new GraphM8(graphFile, "", dic);
		LinkedList<NodeGene<String, M8Attribute>> heads = graph.loadNodeList(headsFile);
		HashMap<NodeGene<String, M8Attribute>, Struct> treeMap = ExportTreeFiles.load(treesFolder, graph);
		
		
		
		PrintStream stream = new PrintStream(outFile);
		stream.println("Key\tOrtholog\tMin\tMax\tAvg");
		for (NodeGene<String, M8Attribute> head : heads) {
			Struct struct = treeMap.get(head);
			if(struct.fam.numGenes() > 1) {
				float[] result = calc(struct, graph);
				if(result[0] < 1000)
					stream.println(head.getKey() + "\t" + struct.orthologs.isEmpty() + "\t" + result[0] + "\t" + result[1] + "\t" + result[2]);
				int id = 0;
				for (Struct structOrtho : struct.orthologs) {
					result = calc(structOrtho, graph);
					if(result[0] < 1000) {
						stream.println(head.getKey() + "\t" + id + "\t" + result[0] + "\t" + result[1] + "\t" + result[2]);
					}
					id++;				
				}
			}
		}
		stream.close();
		
	}
	
	public static float [] calc(Struct struct, GraphM8 graph) {
		float min = 10000;
		float max = 0;
		float total = 0;
		int numArestas = 0;
		for (GeneRegistry geneA : struct.fam.getGenes()) {
			for (GeneRegistry geneB : struct.fam.getGenes()) {
				if(geneA != geneB) {
					NodeGene<String, M8Attribute> nodeA = graph.getNode(geneA.getKey());
					NodeGene<String, M8Attribute> nodeB = graph.getNode(geneB.getKey());
					EdgeAttribute<M8Attribute> edge = nodeA.getEdge(nodeB.getKey());
					if(edge != null) {
						float maxLocal = 0;
						for (M8Attribute attr : edge.getAttributes()) {
							if(attr.getPercIdentity() > maxLocal)
								maxLocal = attr.getPercIdentity();
							
						}
						numArestas++;
						total += maxLocal;
						if(maxLocal < min)
							min = maxLocal;
						else if(maxLocal > max) 
							max = maxLocal;
					}
				}
			}	
		}
		//System.out.println(total + "\t" + numArestas + "\t" + struct.fam.numGenes());
		return new float[] {min, max, total/numArestas};
		
	}

}




