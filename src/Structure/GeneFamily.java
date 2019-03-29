package Structure;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import HeaderExtractor.HeaderExtractor;
import Structure.Registry.RegistryGroup;
import Structure.Registry.RegistryGroups;
import Structure.Graph.NodeGene;
import Structure.Registry.GeneRegistry;
import Structure.Registry.GeneRegistryRandom;
import Structure.Registry.OrganismRegistry;
import ToolkitFile.AlignmentFile;
import ToolkitFile.TreeFile;
import ToolkitFile.FastaFile;
import Wrapper.AlignmentCreator;
import Wrapper.TreeCreator;
import gnu.trove.map.hash.THashMap;

public class GeneFamily {

	private THashMap<OrganismRegistry, LinkedList<GeneRegistry>> family = new THashMap<OrganismRegistry, LinkedList<GeneRegistry>>();
	private String id;
	
	public GeneFamily(LinkedList<? extends NodeGene<?, ?>> genes, String id) {
		this(genes);
		this.id = id;		
	}
	
	public GeneFamily(LinkedList<? extends NodeGene<?, ?>> genes) {
		for (NodeGene<?,?> no : genes) {
			GeneRegistry gene = no.getGene();
			OrganismRegistry org = gene.getOrganism().getRoot();
			LinkedList<GeneRegistry> list = family.get(org);
			if(list == null) {
				list = new LinkedList<GeneRegistry>();
				family.put(org, list);
			}
			list.add(gene);
		}
	}
	
	public GeneFamily(LinkedList<GeneRegistry> genes, String id, boolean d) {
		this(genes, d);
		this.id = id;
	}
	
	public GeneFamily(LinkedList<GeneRegistry> genes, boolean d) {
		for (GeneRegistry gene : genes) {
			OrganismRegistry org = gene.getOrganism().getRoot();
			LinkedList<GeneRegistry> list = family.get(org);
			if(list == null) {
				list = new LinkedList<GeneRegistry>();
				family.put(org, list);
			}
			list.add(gene);
		}
	}
	
	public THashMap<OrganismRegistry, LinkedList<GeneRegistry>> getMap() {
		return family;
	}
	
	public int numGenomes() {
		return getMap().size();
	}
	
	public int numGenes() {
		int total = 0;
		for (LinkedList<GeneRegistry> list : getMap().values()) {
			total += list.size();
		}
		return total;
	}
	
	public LinkedList<GeneRegistry> getGenes() {
		LinkedList<GeneRegistry> genes = new LinkedList<>();
		for (LinkedList<GeneRegistry> values : family.values()) {
			genes.addAll(values);
		}
		return genes;
	}
	
	public LinkedList<GeneRegistry>[] getArrayGenes(OrganismRegistry orgs[]) {
		@SuppressWarnings("unchecked")
		LinkedList<GeneRegistry> [] result = new LinkedList[orgs.length];
		for (int i = 0; i < orgs.length; i++) {
			LinkedList<GeneRegistry> orgGenes = family.get(orgs[i]);
			if(orgGenes != null)
				result[i] = new LinkedList<GeneRegistry>(orgGenes);
		}
		return result;
	}
	
	
	public String getId() {
		return id;
	}
	
	public String getDescription(HeaderExtractor ext) {
		HashMap<String, Integer> hash = new HashMap<String, Integer>();
		for (LinkedList<GeneRegistry> list : family.values()) {
			for (GeneRegistry gene : list) {
				if(hash.containsKey(ext.getDescription(gene))) {
					hash.put(ext.getDescription(gene), hash.get(ext.getDescription(gene)+1));
				}
				else
					hash.put(ext.getDescription(gene), 1);
			}
		}
		
		int max = 0;
		String result = "";
		for (String string : hash.keySet()) {
			if(hash.get(string) > max) {
				max = hash.get(string);
				result = string;
			}
		}
		return result;
	}
	
	public LinkedList<GeneRegistry>[] getArrayGenes(RegistryGroups groups) {
		@SuppressWarnings("unchecked")
		LinkedList<GeneRegistry> [] result = new LinkedList[groups.size()];
		for (int i = 0; i < groups.size(); i++) {
			result[i] = new LinkedList<>();
		}
		Set<OrganismRegistry> orgs = family.keySet();
		for (OrganismRegistry org : orgs) {
			int x = groups.getGroup(org).getId();
			result[x].addAll(family.get(org));
		}
		return result;
	}

	
	public boolean[] getArrayGenesBoolean(OrganismRegistry orgs[]) {
		boolean [] result = new boolean[orgs.length];
		for (int i = 0; i < orgs.length; i++) {
			result[i] = family.get(orgs[i]) != null;
		}
		return result;
	}
	
	public boolean[] getArrayGenesBoolean(RegistryGroups groups) {
		boolean [] result = new boolean[groups.size()];
		Set<OrganismRegistry> orgs = family.keySet();
		for (OrganismRegistry org : orgs) {
			RegistryGroup group = groups.getGroup(org);
			if(group != null) {
				int x = group.getId();
				result[x] = true;
			}
		}
		return result;
	}
	
	public static boolean[] getArrayGenesBoolean(LinkedList<? extends NodeGene<?,?>> component, RegistryGroups groups) {
		boolean [] result = new boolean[groups.size()];
		for (NodeGene<?,?> no : component) {
			RegistryGroup group = groups.getGroup(no.getGene().getOrganism().getRoot());
			if(group != null) {
				int x = group.getId();
				result[x] = true;
			}
		}
		return result;
	}

	public int[] getArrayGenesCount(OrganismRegistry orgs[]) {
		int [] result = new int[orgs.length];
		for (int i = 0; i < orgs.length; i++) {
			LinkedList<GeneRegistry> genes = family.get(orgs[i]);
			if(genes != null)
				result[i] = genes.size();
		}
		return result;
	}
	
	public int[] getArrayGenesCount(RegistryGroups groups) {
		return getArrayGenesCount(groups, false);
	}
	
	public int[] getArrayGenesCount(RegistryGroups groups, boolean byGenome) {
		int [] result = new int[groups.size()];
		Set<OrganismRegistry> orgs = family.keySet();
		for (OrganismRegistry org : orgs) {
			int x = groups.getGroup(org).getId();
			if(byGenome)
				result[x]++;
			else
				result[x] += family.get(org).size();
		}
		return result;
	}
	
	public boolean isOrganismExclusive() {
		return family.size() == 1;
	}
	
	public boolean isGeneExclusive() {
		if(family.size() == 1) {
			Iterator<LinkedList<GeneRegistry>> lista = family.values().iterator();
			if(lista.next().size() == 1)
				return true;
		}
		return false;
	}
	
	public boolean isGroupExclusive(RegistryGroup group) {
		for(OrganismRegistry reg : family.keySet()) {
			if(!group.contain(reg))
				return false;
		}
		return true;
	}
		
	public String toString() {
		String s = "";
		for (OrganismRegistry org : family.keySet()) {
			s = s + org.getFile();
			for (GeneRegistry gene : family.get(org)) {
				s = s + "\t" + gene.getKey();
			}
			s = s + "\n";
		}
		return s;
	}
	
	public String printFasta(String changeHeader) throws IOException {
		String s = "";
		for (Entry<OrganismRegistry, LinkedList<GeneRegistry>> ent : family.entrySet()) {
			GeneRegistry gene = ent.getValue().getFirst();
			if(changeHeader == null)
				s += gene.get();
			String header = changeHeader.replaceAll("ID", "" + gene.getId()).replaceAll("ABREV", ent.getKey().getAbbrev()).replaceAll("KEY", gene.getKey());
			s += gene.get().replace(gene.getHeader(), ">" + header) + "\n";
		}
		return s;
	}
	
	public void makeFasta(boolean byGene, FastaFile fasta) throws IOException, InterruptedException {
		PrintStream stream = new PrintStream(fasta);
		if(byGene) {
			for (OrganismRegistry org : family.keySet())
				for (GeneRegistry gene : family.get(org))
					stream.println(gene.get().replace(gene.getHeader(), ">a" + gene.getId() + "a"));
		}
		else {
			for (OrganismRegistry org : family.keySet()) {
				GeneRegistry gene = family.get(org).getFirst();
				stream.println(gene.get().replace(gene.getHeader(), ">a" + gene.getId() + "a"));
			}
		}
		stream.close();
	}
	
	public interface HeaderProducer {
		public String produce(GeneRegistry gene);
	}
	
	public String makePhylogeny(boolean byGene, boolean printGeneKey, FastaFile fasta, AlignmentFile alin, TreeFile tree, AlignmentCreator alignCreator, TreeCreator treeCreator) throws IOException, InterruptedException {
		return makePhylogeny(byGene, printGeneKey, fasta, alin, tree, alignCreator, treeCreator, null);
	}
	
	public String makePhylogeny(boolean byGene, boolean printGeneKey, FastaFile fasta, AlignmentFile align, TreeFile tree, AlignmentCreator alignCreator, TreeCreator treeCreator, HeaderProducer hProducer) throws IOException, InterruptedException {
		return makePhylogeny(byGene, printGeneKey, fasta, align, tree, alignCreator, treeCreator, hProducer, null);
	}
	
	public String makePhylogeny(boolean byGene, boolean printGeneKey, FastaFile fasta, AlignmentFile align, TreeFile tree, AlignmentCreator alignCreator, TreeCreator treeCreator, HeaderProducer hProducer, Integer numThreads) throws IOException, InterruptedException {
		if(fasta.exists()) fasta.delete();
		if(align.exists()) align.delete();
		if(tree.exists()) tree.delete();
		
		
		makeFasta(byGene, fasta);
		int num = numGenes();
		if(num >= 2) alignCreator.makeAligment(fasta, align, numThreads);
		if(num >= 3) {
			if(num > 500)
				treeCreator.makeTree(align, tree, " -fastest ");
			else 
				treeCreator.makeTree(align, tree, "");
		}
		
		/*if(tree.exists()) {
			String result = tree.load().replace("\n", "");
			for (OrganismRegistry org : family.keySet())
				for (GeneRegistry gene : family.get(org))
					if(printGeneKey)
						result = result.replaceAll("a" + gene.getId() + "a", gene.getKey());
					else
						result = result.replaceAll("a" + gene.getId() + "a", gene.getOrganism().getRoot().getKey());
		
			return result;
		}*/
		
		if(hProducer == null)
			for (LinkedList<GeneRegistry> list : getMap().values()) {
				for (GeneRegistry gene : list) {
					String label = gene.getKey();
					if(!printGeneKey)
						label = gene.getOrganism().getRoot().getAbbrev();
					if(fasta.exists()) fasta.replace("a" + gene.getId() + "a", label);
					if(align.exists()) align.replace("a" + gene.getId() + "a", label);
					if(tree.exists())  tree.replace("a" + gene.getId() + "a", label);
				}
			}
		else
			for (LinkedList<GeneRegistry> list : getMap().values()) {
				for (GeneRegistry gene : list) {
					String label = gene.getKey();
					if(!printGeneKey)
						label = gene.getOrganism().getRoot().getAbbrev();
					if(fasta.exists()) fasta.replace("a" + gene.getId() + "a", label + " " + hProducer.produce(gene));
					if(align.exists()) align.replace("a" + gene.getId() + "a", label + " " + hProducer.produce(gene));
					if(tree.exists())  tree.replace("a" + gene.getId() + "a", label);
				}
			}
		
		//for (iterable_type iterable_element : family) {
			
		//}
		return null;
	}
	
	
	public static String vet(boolean v[], String sep) {
		String s;
		if(v[0])
			s = "1";
		else
			s = "0";
		for (int i = 1; i < v.length; i++) {
			if(v[i])
				s += sep + "1";
			else
				s += sep + "0";
		}
		return s;
	}
	
	public static String vet(boolean v[]) {
		return vet(v, "");
	}

	public String getAllDescriptions(HeaderExtractor ext) {
		TreeSet<String> set = new TreeSet<>();
		for (LinkedList<GeneRegistry> list : getMap().values()) {
			for (GeneRegistry gene : list) {
				set.add(ext.getDescription(gene));
			}
		}
		String result = "";
		for (String string : set) {
			result += "\n" + string;
		}
		
		if(set.isEmpty())
			return null;
		return result.substring(1);
	}
}