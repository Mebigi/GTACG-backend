

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import Structure.Constants;
import Structure.GeneFamily;
import Structure.Graph.EdgeAttribute;
import Structure.Graph.GraphGenes;
import Structure.Graph.GraphM8;
import Structure.Graph.NodeGene;
import Structure.Restriction.Attribute;
import Structure.Restriction.M8Attribute;
import Structure.Restriction.M8Restriction;
import ToolkitFile.AlignmentFile;
import ToolkitFile.ToolkitBaseFile;
import ToolkitFile.TreeFile;
import Wrapper.AlignmentCreator;
import Wrapper.ClustalO;
import Wrapper.FastTree;
import Wrapper.TreeCreator;
import ToolkitFile.FastaFile;

public class Alltree2 {
	public AlignmentCreator alignCreator = ClustalO::makeAlignment;
	public TreeCreator treeCreator = FastTree::makeTree;
	private FastaFile fastaBase = new FastaFile("", ToolkitBaseFile.SequenceType.AminoAcids);
	private AlignmentFile alignBase = new AlignmentFile("", ToolkitBaseFile.FileType.fasta);
	
	public int printCount = 10;
	
	private int threads;
	private File auxFolder;
	private boolean byGene;
	private int minimum;
	private ConcurrentLinkedQueue<LinkedList<NodeGene<String, ?>>> comps;
	public ConcurrentHashMap<NodeGene<String, ?>, String> result = new ConcurrentHashMap<>();
	
	private Alltree2(boolean byGene, File auxFolder, int minimum, int threads) {
		this.threads = threads;
		this.auxFolder = auxFolder;
		this.byGene = byGene;
		this.minimum = minimum;		
	}
	
	public Alltree2(GraphM8 graph, boolean useLevels, boolean byGene, File auxFolder, int minimum, int threads) {
		this(byGene, auxFolder, minimum, threads);
		LinkedList<LinkedList<NodeGene<String, ?>>> list = null;
		if(useLevels)
			list = getComponents(graph);
		else
			list = getComponentsSimple(graph);
		Collections.sort(list, new comparator());
		comps = new ConcurrentLinkedQueue<>(list);
	}
	
	public <ATTRIBUTE extends Attribute<ATTRIBUTE>, NODE extends NodeGene<String, ATTRIBUTE>, GRAPH extends GraphGenes<String, ATTRIBUTE, NODE>> Alltree2(GRAPH graph, boolean byGene, File auxFolder, int minimum, int threads) {
		this(byGene, auxFolder, minimum, threads);
		LinkedList<LinkedList<NodeGene<String, ?>>> list = getComponentsSimple(graph);
		Collections.sort(list, new comparator());
		comps = new ConcurrentLinkedQueue<>(list);
	}
	
	public <ATTRIBUTE extends Attribute<ATTRIBUTE>, NODE extends NodeGene<String, ?>> Alltree2(LinkedList<LinkedList<NODE>> fams, boolean byGene, File auxFolder, int minimum, int threads) {
		this(byGene, auxFolder, minimum, threads);
		comps = new ConcurrentLinkedQueue<>();
		for (LinkedList<NODE> fam : fams) {
			if(byGene) {
				if(fam.size() >= minimum) {
					LinkedList<NodeGene<String, ?>> tmp = new LinkedList<>(fam);
					comps.add(tmp);
				}
			}
			else {
				if(fam.size() >= minimum && new GeneFamily(fam).getMap().size() >= minimum) {
					LinkedList<NodeGene<String, ?>> tmp = new LinkedList<>(fam);
					comps.add(tmp);
				}
			}
		}
	}
	
	public void setAlignmenteCreator(AlignmentCreator alignCreator) {
		this.alignCreator = alignCreator;
	}
	
	public void setTreeCreator(TreeCreator treeCreator) {
		this.treeCreator = treeCreator;
	}
	
	public void setFastaBase(FastaFile fasta) {
		this.fastaBase = fasta;
	}
	
	public void setAlignmenteBase(AlignmentFile align) {
		this.alignBase = align;
	}
	
	private class comparator implements Comparator<LinkedList<NodeGene<String, ?>>> {
		@Override
		public int compare(LinkedList<NodeGene<String, ?>> o1, LinkedList<NodeGene<String, ?>> o2) {
			return o2.size() - o1.size();
		}
	}
	
	public <ATTRIBUTE extends Attribute<ATTRIBUTE>, NODE extends NodeGene<String, ATTRIBUTE>, GRAPH extends GraphGenes<String, ATTRIBUTE, NODE>> 
	LinkedList<LinkedList<NodeGene<String, ?>>> getComponentsSimple(GRAPH graph) {
		
		LinkedList<LinkedList<NodeGene<String, ?>>> result = new LinkedList<>();
		for (NODE head : graph.connComponentHeads()) {
			LinkedList<NODE> comp = graph.connComponentList(head);
			if(comp.size() >= minimum) {
				LinkedList<NodeGene<String, ?>> list = new LinkedList<>();
				for (NODE node : comp) {
					list.add(node);
				}
				result.add(list);
			}
		}
		
		return result;
		
	}
		
	public LinkedList<LinkedList<NodeGene<String, ?>>> getComponents(GraphM8 graph) {
		LinkedList<LinkedList<NodeGene<String, ?>>> result = new LinkedList<>(); 
		
		for (NodeGene<String, M8Attribute> headBase : graph.connComponentHeads()) {
			GraphM8 subBase = graph.connComponentGraph(headBase);
			while(subBase.hasNextLayer()) {
				subBase = subBase.nextLayer(true);
			}
			
			for (NodeGene<String, M8Attribute> head : subBase.connComponentHeads()) {
				GraphM8 sub = subBase.connComponentGraph(head);
				GeneFamily fam = new GeneFamily(sub.getNodes());
				if(fam.getMap().size() >= minimum) {
					if(sub.getNodes().size() == fam.getMap().size()) {
						result.add(new LinkedList<>(sub.getNodes()));
					}
					else {
						TreeSet<Double> set = new TreeSet<>();
						for (NodeGene<String, M8Attribute> node : sub.getNodes()) {
							for (EdgeAttribute<M8Attribute> edge : node.getEdges()) {
								TreeSet<Double> aux = new TreeSet<>();
								for (M8Attribute attr : ((EdgeAttribute<M8Attribute>)edge).getAttributes()) {
									aux.add(attr.getEValue());
								}
								set.add(aux.first());
							}
						}
						
						LinkedList<M8Restriction> listaRest = new LinkedList<>(); 
						for (Double evalue : set) {
							listaRest.addFirst(new M8Restriction(0, 0, 20, 99, (short)5, evalue, 50));
						}
						
						int maxA = 0;
						int maxB = 0;
						M8Restriction maxRest = null;
						
						for (M8Restriction rest : listaRest) {
							sub.updateRestrictions(rest);
							int totalA = 0;
							int totalB = 0;	
							for (NodeGene<String, M8Attribute> sHead : sub.connComponentHeads()) {
								LinkedList<NodeGene<String, M8Attribute>> sComp = sub.connComponentList(sHead);
								GeneFamily sFam = new GeneFamily(sComp);
								if(sFam.getMap().size() == fam.getMap().size()) {
									totalA++;
									if(sComp.size() == fam.getMap().size())
										totalB++;
									
								}
							}
							if(totalA > maxA) {
								maxA = totalA;
								maxB = totalB;
								maxRest = rest;
							}
							else if(totalA == maxA && totalB >= maxB) {
								maxB = totalB;
								maxRest = rest;
							}
						}
						
						sub = subBase.connComponentGraph(head);
						sub.updateRestrictions(maxRest);
						for (NodeGene<String, M8Attribute> sHead : sub.connComponentHeads()) {
							if(sub.connComponentList(sHead).size() >= minimum) {
								LinkedList<NodeGene<String, ?>> tmp = new LinkedList<>();
								for(NodeGene<String, M8Attribute> tmp2 : sub.connComponentList(sHead))
									tmp.add(tmp2);
								result.add(tmp);
							}
						}
					}
				}
			}
		}
		return result;
	}
	
	public void run() {
		System.out.println("# Start Alltree - " + Calendar.getInstance().getTime());
		System.out.println("# Num Comps - " + comps.size());
		
		ExecutorService executor = Executors.newFixedThreadPool(threads);
		for(int i = 0; i < threads; i++) {
			executor.execute(() -> {
				System.out.println("# " + Thread.currentThread().getId() + "\tStart");
			
				int cont = 0;
				LinkedList<NodeGene<String, ?>> comp = comps.poll();
				while(comp != null) {
					if(comp.size() >= minimum) {
						GeneFamily fam = new GeneFamily(comp);
						
						String file = auxFolder.getAbsolutePath() + File.separator + Constants.rand();
						FastaFile fasta = fastaBase.clone(file + ".faa");
						AlignmentFile alin = alignBase.clone(file + ".alin");
						TreeFile arv = new TreeFile(file + ".tre");
						
						try {
							String tree = fam.makePhylogeny(byGene, true, fasta, alin, arv, alignCreator, treeCreator);
							result.put(comp.getFirst(), tree);
						} catch (IOException | InterruptedException e1) {
							e1.printStackTrace();
						}
					}
					
					comp = comps.poll();
					cont++;
					if(cont%printCount == 0)
						System.out.println("#" + Thread.currentThread().getId() + "\t" + cont + "\t" + Calendar.getInstance().getTime());
				}
				System.out.println("# " + Thread.currentThread().getId() + "\tEnd");
			});
		}
		
		executor.shutdown();
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void save(File file) throws FileNotFoundException {
		PrintStream out = new PrintStream(file);
		for (Entry<NodeGene<String, ?>, String> ent : result.entrySet()) {
			out.println(ent.getKey().getKey() + "\t" + ent.getValue());
		}
		out.close();
	}
	
	public static ConcurrentHashMap<NodeGene<String, M8Attribute>, String> load(File file, GraphM8 graph) throws FileNotFoundException {
		ConcurrentHashMap<NodeGene<String, M8Attribute>, String> result = new ConcurrentHashMap<>();
		Scanner sc = new Scanner(file);
		while(sc.hasNextLine()) {
			String line[] = sc.nextLine().split("\t");
			result.put(graph.getNode(line[0]), line[1]);
		}
		sc.close();
		return result;
	}
}