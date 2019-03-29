

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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import Structure.Constants;
import Structure.GeneFamily;
import Structure.Graph.GraphGenes;
import Structure.Graph.NodeGene;
import Structure.Registry.Dictionary;
import Structure.Registry.GeneRegistry;
import Structure.Registry.OrganismRegistry;
import ToolkitFile.AlignmentFile;
import ToolkitFile.ToolkitBaseFile;
import ToolkitFile.TreeFile;
import Wrapper.ClustalO;
import Wrapper.Phyml;
import ToolkitFile.FastaFile;
import gnu.trove.map.hash.THashMap;

public class Alltree <NODE extends NodeGene<String, ?>, GRAFO extends GraphGenes<String, ?, NODE>> {
	private GRAFO grafo;
	private ConcurrentLinkedQueue<NODE> heads = new ConcurrentLinkedQueue<>();
	private int threads;
	private int minimo;
	private File auxFolder;
	private Dictionary dic;
	private OrganismRegistry[] orgs;
	private boolean byGene;
	
	public ConcurrentHashMap<NODE, String> result;
	
	public Alltree(GRAFO grafo, Dictionary dic, boolean byGene, OrganismRegistry[] orgs, File auxFolder, int minimo, int threads) {
		this.grafo = grafo;
		this.dic = dic;
		this.orgs = orgs;
		this.threads = threads;
		this.minimo = minimo;
		this.auxFolder = auxFolder;
		this.byGene = byGene;
		result = new ConcurrentHashMap<>();
		
		LinkedList<NODE> tmp = new LinkedList<>();
		for (NODE head : grafo.connComponentHeads()) {
			if(grafo.connComponentList(head).size() >= minimo)
				tmp.add(head);
		}
		Collections.sort(tmp, new comp());
		heads.addAll(tmp);
	}
	
	class comp implements Comparator<NODE> {
		@Override
		public int compare(NODE o1, NODE o2) {
			return grafo.connComponentList(o1).size() - grafo.connComponentList(o2).size();
		}
	}
	
	public void run() {
		System.out.println("# Start Alltree - " + Calendar.getInstance().getTime());
		System.out.println("# Num Comps - " + heads.size());
		
		ExecutorService executor = Executors.newFixedThreadPool(threads);
		for(int i = 0; i < threads; i++)
		executor.execute(new WorkerThreadList<String, NODE, GRAFO>(grafo, dic, byGene, orgs, heads, result, auxFolder, minimo));
		executor.shutdown();
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static class WorkerThreadList <KEY_STATIC, NODE extends NodeGene<KEY_STATIC, ?>, GRAFO extends GraphGenes<KEY_STATIC, ?, NODE>> implements Runnable {
		GRAFO grafo;
		Dictionary dic;
		OrganismRegistry[] orgs;
		ConcurrentLinkedQueue<NODE> heads;
		ConcurrentHashMap<NODE, String> saida;
		File auxFolder;
		int minimo = 0;
		boolean byGene;
		
		public WorkerThreadList(GRAFO grafo, Dictionary dic, boolean byGene, OrganismRegistry[] orgs, ConcurrentLinkedQueue<NODE> heads, ConcurrentHashMap<NODE, String> saida, File auxFolder, int minimo) {
			this.grafo = grafo;
			this.dic = dic;
			this.orgs = orgs;
			this.heads = heads;
			this.saida = saida;
			this.auxFolder = auxFolder;
			this.minimo = minimo;
			this.byGene = byGene;
		}
		
		public void run(){
			System.out.println("# " + Thread.currentThread().getId() + "\tStart");
			
			int cont = 0;
			while(!heads.isEmpty()) {
				NODE head = heads.poll();
				LinkedList<NODE> componente = grafo.connComponentList(head);
				
				GeneFamily familia = new GeneFamily(componente);
				THashMap<OrganismRegistry, LinkedList<GeneRegistry>> mapGenes = familia.getMap();
				
				if(mapGenes.size() >= minimo) {
					String tmpId = Constants.rand();
					String file = auxFolder.getAbsolutePath() + File.separator + tmpId; 
					
					FastaFile fasta = new FastaFile(file + ".faa", ToolkitBaseFile.SequenceType.AminoAcids);
					AlignmentFile alin = new AlignmentFile(file + ".alin", ToolkitBaseFile.FileType.phylip);
					TreeFile arv = new TreeFile(file + ".tre");
					
					try {
						String tree = familia.makePhylogeny(byGene, true, fasta, alin, arv, ClustalO::makeAlignment, Phyml::makeTree);
						saida.put(head, tree);
					} catch (IOException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					cont++;
					if(cont%10 == 0)
						System.out.println("#" + Thread.currentThread().getId() + "\t" + cont + "\t" + Calendar.getInstance().getTime());
				}
			}
			System.out.println("# " + Thread.currentThread().getId() + "\tEnd");
		}
	}

	public void save(File file) throws FileNotFoundException {
		PrintStream out = new PrintStream(file);
		for (Entry<NODE, String> ent : result.entrySet()) {
			out.println(ent.getKey().getKey() + "\t" + ent.getValue());
		}
		out.close();
	}
	
	public void load(File file) throws FileNotFoundException {
		Scanner sc = new Scanner(file);
		while(sc.hasNextLine()) {
			String line[] = sc.nextLine().split("\t");
			result.put(grafo.getNode(line[0]), line[1]);
		}
		sc.close();
	}
}


