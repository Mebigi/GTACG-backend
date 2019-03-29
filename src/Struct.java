import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;

import Structure.Constants;
import Structure.GeneFamily;
import Structure.Graph.GraphM8;
import Structure.Graph.NodeGene;
import Structure.Graph.PhylogeneticTree;
import Structure.Registry.Dictionary;
import Structure.Registry.GeneRegistry;
import Structure.Restriction.M8Attribute;
import ToolkitFile.AlignmentFile;
import ToolkitFile.FastaFile;
import ToolkitFile.TreeFile;
import ToolkitFile.ToolkitBaseFile.FileType;
import ToolkitFile.ToolkitBaseFile.SequenceType;
import Wrapper.AlignmentCreator;
import Wrapper.ClustalO;
import Wrapper.FastTree;
import Wrapper.Muscle;
import Wrapper.Phyml;
import Wrapper.TreeCreator;

public class Struct implements Runnable {
	FastaFile fasta = null;
	AlignmentFile align = null;
	TreeFile tree = null;
	LinkedList<Struct> domains = new LinkedList<>();
	LinkedList<Struct> orthologs = new LinkedList<>();
	GeneFamily fam = null;
	Dictionary dic = null;
	boolean homolog = false;
	GraphM8 graph = null;
	String head = null;
	public boolean makeDomains;
	public boolean makeOrthologs;
	public int numThreadsMSA;
	
	public static AlignmentCreator alignCreator = ClustalO::makeAlignment;
	public static TreeCreator treeCreator = FastTree::makeTree;
	//public static AlignmentCreator alignCreator = Muscle::makeAlignment;
	//public static TreeCreator treeCreator = Phyml::makeTree;
	
	@Override
	public void run() {
		try {
			fam.makePhylogeny(true, true, fasta, align, tree, alignCreator, treeCreator, (gene) -> {return gene.getOrganism().getRoot().getAbbrev();}, numThreadsMSA);
			if(homolog && tree.exists() && makeOrthologs) {
				try {
					new PhylogeneticTree(tree.load().replace("\n", ""), false);
				} catch (Exception e) {
					System.err.println(tree);
				}
				PhylogeneticTree ph = new PhylogeneticTree(tree.load().replace("\n", ""), false);
				LinkedList<LinkedList<String>> lists = ph.getOrthologs(0.25);
				LinkedList<LinkedList<String>> orthologsCandidate = new LinkedList<>(); 
				if(lists.size() > 1) {
					while(!lists.isEmpty()) {
						LinkedList<String> orthoCandidate = lists.pop();
						if(orthoCandidate.size() > 2) {
							LinkedList<GeneRegistry> orthoGenes = new LinkedList<>();
							for (String key : orthoCandidate) {
								orthoGenes.add(dic.getGeneById(key));
							}							
							String fileName = Constants.rand();
							FastaFile fastaCandidate = new FastaFile(fileName + ".fasta", SequenceType.AminoAcids);
							AlignmentFile alignCandidate = new AlignmentFile(fileName + ".align", FileType.fasta);
							TreeFile treeCandidate = new TreeFile(fileName + ".tree");							
							(new GeneFamily(orthoGenes, "", true)).makePhylogeny(true, true, fastaCandidate, alignCandidate, treeCandidate, ClustalO::makeAlignment, FastTree::makeTree, null, numThreadsMSA);
							PhylogeneticTree phCandidate = new PhylogeneticTree(treeCandidate.load(), false);
							fastaCandidate.delete();
							alignCandidate.delete();
							treeCandidate.delete();
							LinkedList<LinkedList<String>> listCandidate = phCandidate.getOrthologs(0.25);
							if(listCandidate.size() == 1)
								orthologsCandidate.add(orthoCandidate);
							else {
								lists.addAll(listCandidate);
							}
						}
						else
							orthologsCandidate.add(orthoCandidate);
					}
				}
				
				if(orthologsCandidate.size() > 1) {
					int id = 0;
					for (LinkedList<String> list : orthologsCandidate) {
						LinkedList<GeneRegistry> genes = new LinkedList<>();
						LinkedList<NodeGene<String, M8Attribute>> nodes = new LinkedList<>();
						for (String key : list) {
							genes.add(dic.getGeneById(key));
							nodes.add(graph.getNode(key));
						}
						
						Struct struct = new Struct();
						struct.fam = new GeneFamily(genes, true);
						struct.fasta = new FastaFile(fasta.getAbsolutePath().replace(".fasta", ".o" + id + ".fasta"), SequenceType.AminoAcids);
						struct.align = new AlignmentFile(align.getAbsolutePath().replace(".align", ".o" + id + ".align"), FileType.fasta);
						struct.tree = new TreeFile(tree.getAbsolutePath().replace(".tree", ".o" + id + ".tree"));
						struct.dic = dic;
						struct.fam.makePhylogeny(true, true, struct.fasta, struct.align, struct.tree, alignCreator, treeCreator, (gene) -> {return gene.getOrganism().getRoot().getAbbrev();}, numThreadsMSA);
						orthologs.add(struct);
						id++;
						
						GraphM8 sub = graph.subGraph(nodes);
						if(!sub.isFullConnected() && makeDomains) {
							HashSet<NodeGene<String, M8Attribute>> mults = sub.getMultiDomainSeqs(0, 1, 1);
							if(mults != null && mults.size() > 0) {
								LinkedList<LinkedList<NodeGene<String, M8Attribute>>> doms = sub.getDomains(mults, 0.3, 100, head, 1);
								int idDom = 0;
								if(doms.size() > 1)
									//System.out.println(struct.fasta);
									for (LinkedList<NodeGene<String, M8Attribute>> dom : doms) {
										//System.out.println(dom.size());
										Struct structDom = new Struct();
										structDom.fam = new GeneFamily(dom);
										structDom.fasta = new FastaFile(struct.fasta.getAbsolutePath().replace(".fasta", ".d" + idDom + ".fasta"), SequenceType.AminoAcids);
										structDom.align = new AlignmentFile(struct.align.getAbsolutePath().replace(".align", ".d" + idDom + ".align"), FileType.fasta);
										structDom.tree = new TreeFile(struct.tree.getAbsolutePath().replace(".tree", ".d" + idDom + ".tree"));
										System.out.println("\t" + structDom.fasta);
										structDom.dic = dic;
										
										structDom.fam.makePhylogeny(true, true, structDom.fasta, structDom.align, structDom.tree, alignCreator, treeCreator, (gene) -> {return gene.getOrganism().getRoot().getAbbrev();}, numThreadsMSA);
										struct.domains.add(structDom);
										idDom++;
									}
							}
						}
					}
				}
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			System.out.println("error");
			System.out.println(fasta);
		}
	}
}
