import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import Structure.Constants;
import Structure.GeneFamily;
import Structure.Graph.GraphM8;
import Structure.Graph.NodeGene;
import Structure.Registry.Dictionary;
import Structure.Restriction.M8Attribute;
import ToolkitFile.AlignmentFile;
import ToolkitFile.FastaFile;
import ToolkitFile.ToolkitBaseFile.FileType;
import ToolkitFile.ToolkitBaseFile.SequenceType;
import ToolkitFile.TreeFile;
import Wrapper.ClustalO;
import Wrapper.FastTree;
import Wrapper.Phyml;

public class ExportTreeFiles {
	public static void export(File out, HashMap<NodeGene<String, M8Attribute>, Struct> map) throws FileNotFoundException {
		PrintStream stream = new PrintStream(out);
		for (Entry<NodeGene<String, M8Attribute>, Struct> ent : map.entrySet()) {
			System.out.println(ent.getKey());
			stream.print(ent.getKey() + "\t" + ent.getValue().fasta.getName() + "\t" + ent.getValue().align.getName() + "\t" + ent.getValue().tree.getName() + "\t" + ent.getValue().domains.size());
			for (Struct struct : ent.getValue().domains) {
				stream.print("\t" + struct.fasta.getName() + "\t" + struct.align.getName() + "\t" + struct.tree.getName());
			}
			stream.print("\t" + ent.getValue().orthologs.size());
			for (Struct struct : ent.getValue().orthologs) {
				stream.print("\t" + struct.fasta.getName() + "\t" + struct.align.getName() + "\t" + struct.tree.getName() + "\t" + struct.domains.size());
				for (Struct structDom : struct.domains) {
					stream.print("\t" + structDom.fasta.getName() + "\t" + structDom.align.getName() + "\t" + structDom.tree.getName());
				}
			}
			stream.println();
		}
		stream.close();		
	}
	
	public static HashMap<NodeGene<String, M8Attribute>, Struct> load(File folder, GraphM8 graph) throws FileNotFoundException {
		HashMap<NodeGene<String, M8Attribute>, Struct> map = new HashMap<>();
		File index = new File(folder.getAbsolutePath() + "/index");
		Scanner sc = new Scanner(index);
		int countA = 0;
		while(sc.hasNextLine()) {
			String[] line = sc.nextLine().split("\t");
			NodeGene<String, M8Attribute> head = graph.getNode(line[0]);
			Struct struct = new Struct();
			struct.fasta = new FastaFile(folder.getAbsolutePath() + "/" + line[1], SequenceType.AminoAcids);
			struct.align = new AlignmentFile(folder.getAbsolutePath() + "/" + line[2], FileType.fasta);
			struct.tree = new TreeFile(folder.getAbsolutePath() + "/" + line[3]);
			struct.homolog = true;
			struct.graph = graph;
			
			int countB = 0;
			LinkedList<NodeGene<String, M8Attribute>> list = new LinkedList<>();
			for (String key : struct.fasta.getAllLocusTag()) {
				countB++;
				list.add(graph.getNode(key));
			}
			countA++;
			try {
				struct.fam = new GeneFamily(list);
			} catch (Exception e) {
				System.err.println("! " + countA + "\t" + countB + "\t" + list.size() + "\t" + struct.fasta.getAllLocusTag().size() + "\t" + list.getFirst());
				for (NodeGene<String, M8Attribute> nodeGene : list) {
					System.out.println("# " + nodeGene);
				}
				for (NodeGene<String, M8Attribute> nodeGene : list) {
					System.out.println("@ " + nodeGene.getGene());
				}
				for (NodeGene<String, M8Attribute> nodeGene : list) {
					System.out.println("! " + nodeGene.getGene().getOrganism());
				}
				
				struct.fam = new GeneFamily(list);
			}
			int numDomains = Integer.parseInt(line[4]);
			int posDomains = 5;
			for (int i = 0; i < numDomains; i++) {
				Struct structDom = new Struct();
				structDom.fasta = new FastaFile(folder.getAbsolutePath() + "/" + line[posDomains++], SequenceType.AminoAcids);
				structDom.align = new AlignmentFile(folder.getAbsolutePath() + "/" + line[posDomains++], FileType.fasta);
				structDom.tree = new TreeFile(folder.getAbsolutePath() + "/" + line[posDomains++]);
				structDom.graph = graph;
				
				list = new LinkedList<>();
				for (String key : structDom.fasta.getAllLocusTag()) {
					list.add(graph.getNode(key));
				}
				structDom.fam = new GeneFamily(list);
				struct.domains.add(structDom);
			}
			
			int numOrthologs = Integer.parseInt(line[5 + numDomains*3]);
			int posOrthologs = 6 + numDomains*3;
			for (int i = 0; i < numOrthologs; i++) {
				Struct structOrtho = new Struct();
				structOrtho.fasta = new FastaFile(folder.getAbsolutePath() + "/" + line[posOrthologs++], SequenceType.AminoAcids);
				structOrtho.align = new AlignmentFile(folder.getAbsolutePath() + "/" + line[posOrthologs++], FileType.fasta);
				structOrtho.tree = new TreeFile(folder.getAbsolutePath() + "/" + line[posOrthologs++]);
				
				list = new LinkedList<>();
				for (String key : structOrtho.fasta.getAllLocusTag()) {
					list.add(graph.getNode(key));
				}
				structOrtho.fam = new GeneFamily(list);
				struct.orthologs.add(structOrtho);
				
				int numOrthologsDomains = Integer.parseInt(line[posOrthologs++]); 
				for (int j = 0; j < numOrthologsDomains; j++) {
					Struct structOrthoDom = new Struct();
					structOrthoDom.fasta = new FastaFile(folder.getAbsolutePath() + "/" + line[posOrthologs++], SequenceType.AminoAcids);
					structOrthoDom.align = new AlignmentFile(folder.getAbsolutePath() + "/" + line[posOrthologs++], FileType.fasta);
					structOrthoDom.tree = new TreeFile(folder.getAbsolutePath() + "/" + line[posOrthologs++]);

					list = new LinkedList<>();
					for (String key : structOrthoDom.fasta.getAllLocusTag()) {
						list.add(graph.getNode(key));
					}
					structOrthoDom.fam = new GeneFamily(list);
					structOrtho.domains.add(structOrthoDom);
				}
			}
			map.put(head, struct);
		}
		sc.close();
		return map;
	}
	
	static int total = 0;
	public static void main(String[] args) throws IOException, InterruptedException {
		Constants.inicializacao();
		
		File graphFile = null;
		File headsFile = null;
		File dicFile = null;
		File outFolder = null;
		int threads = 1;
		boolean makeDomains = true;
		boolean makeOrthologs = true;
		int numThreadsMSA = 1;
		String dicFormat = null;
		
		//args = new String[] {"-graph", "/home/caio/dados/streptococcus/patric/graph.strep.gz", "-heads", "/home/caio/dados/streptococcus/patric/heads.strep", "-dic", "/home/caio/dados/streptococcus/patric/streptococcus.dic", "-outFolder", "outFolder", "-threads", "5"};
		//args = new String[] {"-graph", "/home/caio/dados/streptococcus/patric/graph.strep.gz", "-heads", "teste.heads", "-dic", "/home/caio/dados/streptococcus/patric/streptococcus.dic", "-outFolder", "outFolder", "-threads", "4"};
		/*args = new String[] {
				"-graph", 
				"/home/caio/dados/streptococcus/patric/graph41.strep.gz", 
				"-heads", "/home/caio/dados/streptococcus/patric/heads41.strep", 
				//"-heads", "teste.heads",
				"-dic", "/home/caio/dados/streptococcus/patric/streptococcus.dic", 
				"-outFolder", "/home/caio/dados/streptococcus/patric/treesTeste", 
				"-threads", "4"};*/
		
		/*args = new String[] {
				"-graph", "/media/caio/08CE4A891258E142/dados/xantho15/gb/xantho15.graph.gz", 
				"-heads", "/media/caio/08CE4A891258E142/dados/xantho15/gb/xantho15.heads", 
				"-dic", "/media/caio/08CE4A891258E142/dados/xantho15/gb/xantho15.dic", 
				"-outFolder", "/media/caio/08CE4A891258E142/dados/xantho15/gb/trees", 
				"-threads", "4"};*/
		
		/*args = new String[] {
				"-graph", "/home/caio/dados/xyllela/xf.graph50.gz", 
				"-heads", "/home/caio/dados/xyllela/xf.heads50", 
				"-dic", "/home/caio/dados/xyllela/xf.dic", 
				"-outFolder", "/home/caio/dados/xyllela/trees50", 
				"-threads", "1"};*/
		
		/*args = new String[] {
				"-graph", "/home/caio/dados/myco/prokka/myco.graph40.gz", 
				"-heads", "/home/caio/dados/myco/prokka/myco.heads40", 
				"-dic", "/home/caio/dados/myco/pro	kka/myco.dic", 
				"-outFolder", "/home/caio/dados/myco/prokka/trees40", 
				"-threads", "4"};*/
		/*args = new String[] {
				"-graph", "/home/caio/dados/streptococcus/patric/graph41.strep.gz", 
				"-heads", "/home/caio/dados/streptococcus/patric/heads41.strep", 
				//"-heads", "heads",
				"-dic", "/home/caio/dados/streptococcus/patric/streptococcus.dic",
				"-outFolder", "/home/caio/dados/streptococcus/patric/trees41new2", 
				"-threads", "4"};*/
		
		/*args = new String[] {
				"-graph", "/home/caio/dados/xyllela/xy.graph76.gz", 
				"-heads", "/home/caio/dados/xyllela/xy.heads76", 
				"-dic", "/home/caio/dados/xyllela/all_genom.dic",
				"-outFolder", "/home/caio/dados/xyllela/trees76", 
				"-threads", "4"};*/
		
		/*args = new String[] {
				"-graph", "/media/caio/08CE4A891258E142/dados/myco2/prokka/myco.graph45.gz", 
				"-heads", "/media/caio/08CE4A891258E142/dados/myco2/prokka/myco.heads45", 
				"-dic", "/media/caio/08CE4A891258E142/dados/myco2/prokka/myco.dic",
				"-outFolder", "/media/caio/08CE4A891258E142/dados/myco2/prokka/trees45", 
				"-threads", "2"};*/

		
		for (int i = 0; i < args.length; i++) {
			if("-help".equals(args[i])) {
				System.out.println("Input Files");
				System.out.println("\t-graph");
				System.out.println("\t-heads");
				System.out.println("\t-dic");
				System.out.println();
				System.out.println("Output Files");
				System.out.println("\t-outFolder");
				System.out.println();
				System.out.println("Parameters");
				System.out.println("\t[-align]");
				System.out.println("\t[-tree]");
				System.out.println("\t[-threads]");
				System.out.println("\t[-threadsMSA]");
				System.out.println("\t[-withoutDomains");
				System.out.println("\t[-withoutOrthologs");
				
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
					System.out.println("Graph file not found");
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
			else if("-outFolder".equals(args[i])) {
				if(args.length > i + 1)
				outFolder = new File(args[i+1]);
			}
			else if("-align".equals(args[i])) {
				if("clustalO".equals(args[i+1]))
					Struct.alignCreator = ClustalO::makeAlignment;
			}
			else if("-tree".equals(args[i])) {
				if("phyml".equals(args[i+1]))
					Struct.treeCreator = Phyml::makeTree;
				else if("fastTree".equals(args[i+1]))
					Struct.treeCreator = FastTree::makeTree;
			}
			else if("-threads".equals(args[i])) {
				threads = Integer.parseInt(args[i+1]);
			}
			else if("-threadsMSA".equals(args[i])) {
				numThreadsMSA = Integer.parseInt(args[i+1]);
			}
			else if("-withoutDomains".equals(args[i])) {
				makeDomains = false;
				i--;
			}
			else if("-withoutOrthologs".equals(args[i])) {
				makeOrthologs = false;
				i--;
			}
			i++;
		}
		
		
		if(dicFile == null) {
			System.out.println("Dictionary file not specified");
			return;
		}
		if(graphFile == null) {
			System.out.println("Graph file not specified");
			return;
		}
		if(outFolder == null) {
			System.out.println("Output folder not specified");
			return;
		}
		
		System.setOut(new PrintStream(new File("out")));
		System.setErr(new PrintStream(new File("err")));
		
		
		if(!outFolder.exists())
			outFolder.mkdir();
		
		Dictionary dic = Dictionary.getDictionary(dicFile, dicFormat);
		GraphM8 graph = null;
		if(graphFile.getName().endsWith(".gz"))
			graph = new GraphM8(graphFile, "gz", dic);
		else
			graph = new GraphM8(graphFile, "", dic);
		LinkedList<NodeGene<String, M8Attribute>> heads = graph.loadNodeList(headsFile);
		
		HashMap<NodeGene<String, M8Attribute>, Struct> map = new HashMap<>();
		
		ConcurrentLinkedDeque<Struct> structs = new ConcurrentLinkedDeque<>();
		ExecutorService executor = Executors.newFixedThreadPool(threads);
		for (NodeGene<String, M8Attribute> head : heads) {
			GraphM8 sub = graph.connComponentGraph(head);
			Struct struct = new Struct();
			struct.fam = new GeneFamily(sub.getNodes());
			struct.fasta = new FastaFile(outFolder.getAbsolutePath() + "/" + head.getKey() + ".fasta", SequenceType.AminoAcids);
			struct.align = new AlignmentFile(outFolder.getAbsolutePath() + "/" + head.getKey() + ".align", FileType.fasta);
			struct.tree = new TreeFile(outFolder.getAbsolutePath() + "/" + head.getKey() + ".tree");
			struct.homolog = true;
			struct.dic = dic;
			struct.head = head.getKey();
			struct.graph = graph;
			struct.makeDomains = makeDomains;
			struct.makeOrthologs = makeOrthologs;
			struct.numThreadsMSA = numThreadsMSA;
			
			if(!sub.isFullConnected() && makeDomains) {
				HashSet<NodeGene<String, M8Attribute>> mults = sub.getMultiDomainSeqs(0, threads, threads);
				if(mults != null && mults.size() > 0) {
					LinkedList<LinkedList<NodeGene<String, M8Attribute>>> doms = sub.getDomains(mults, 0.3, 100, head.getKey(), threads);
					int id = 0;
					if(doms.size() > 1)
						for (LinkedList<NodeGene<String, M8Attribute>> dom : doms) {
							Struct structDom = new Struct();
							structDom.fam = new GeneFamily(dom);
							structDom.fasta = new FastaFile(outFolder.getAbsolutePath() + "/" + head.getKey() + ".d" + id + ".fasta", SequenceType.AminoAcids);
							structDom.align = new AlignmentFile(outFolder.getAbsolutePath() + "/" + head.getKey() + ".d" + id + ".align", FileType.fasta);
							structDom.tree = new TreeFile(outFolder.getAbsolutePath() + "/" + head.getKey() + ".d" + id + ".tree");
							structDom.dic = dic;
							
							struct.domains.addLast(structDom);
							structs.add(structDom);
							executor.execute(structDom);
							id++;
						}
				}
			}
			map.put(head, struct);
			structs.add(struct);
			executor.execute(struct);
		}
		
		/*for (Struct struct : map.values()) {
			structs.add(struct);
			if(struct.domains != null)
				for (Struct structDom : struct.domains) {
					structs.add(structDom);
				}
		}
		
		for (Struct struct : structs) {
			//for (int i = 0; i < threads; i++) {
				executor.execute(struct);
			//}
		}*/		
		executor.shutdown();
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		export(new File(outFolder.getAbsolutePath() + "/index"), map);
	}
}
