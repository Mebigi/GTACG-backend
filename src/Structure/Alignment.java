package Structure;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import Metrics.Blosum;
import Structure.Graph.NodeGene;
import Structure.Registry.Dictionary;
import Structure.Registry.RegistryGroup;
import Structure.Restriction.Attribute;
import ToolkitFile.AlignmentFile;
import ToolkitFile.ToolkitBaseFile.FileType;
import ToolkitFile.ToolkitBaseFile.SequenceType;

public class Alignment {
	HashMap<String, StringBuilder> seqs = new HashMap<>();
	SequenceType seqType; 
	int size;
	
	public Alignment(AlignmentFile f) throws FileNotFoundException {
		seqType = f.seqType;
		if(f.fileType == FileType.fasta) {
			readFasta(f);
		}
	}
	
	public StringBuilder getSequence(String key) {
		return seqs.get(key);
	}
	
	public Set<Entry<String, StringBuilder>> getSequences() {
		return seqs.entrySet();
	}
	
	public void readFasta(AlignmentFile f) throws FileNotFoundException {
		Scanner sc = new Scanner(f);
		String name = "";
		while (sc.hasNextLine()) {
			String string = sc.nextLine();
			if(string.charAt(0) == '>') {
				if(string.contains(" "))
					name = string.substring(1, string.indexOf(" "));
				else
					name = string.substring(1);
				seqs.put(name, new StringBuilder());
			}
			else {
				seqs.get(name).append(string);
			}
		}
		size = seqs.get(name).length();
		sc.close();
	}
	
	public int size() {
		return size;
	}
	
	public int numSequences() {
		return seqs.keySet().size();
	}
	
	public void export(File f, FileType type) throws FileNotFoundException {
		if(type == FileType.fasta) {
			exportFasta(f);
		}
		else if(type == FileType.nexus) {
			exportNexus(f);
		}
		else if(type == FileType.nexus) {
			exportNexus(f);
		}
	}
	
	private void exportNexus(File f) throws FileNotFoundException {
		PrintStream stream = new PrintStream(f);
		stream.println("#NEXUS");
		stream.println("");
		stream.println("begin data;");
		stream.println("Dimensions ntax=" + seqs.keySet().size() + " nchar=" + size + ";");
		if(seqType == SequenceType.AminoAcids)
			stream.println("Format datatype=protein missing=? gap=-;");
		else if(seqType == SequenceType.Nucleotides)
			stream.println("Format datatype=dna missing=? gap=-;");
		else 
			stream.println("Format datatype=standard missing=? gap=-;");
		stream.println("Matrix");
		for (Entry<String, StringBuilder> ent : seqs.entrySet()) {
			stream.println(ent.getKey() + " " + ent.getValue());
		}
		stream.println(";");
		stream.println("End;");
		stream.close();
	}

	int len = 60;
	private void exportFasta(File f) throws FileNotFoundException {
		PrintStream stream = new PrintStream(f);
		for (Entry<String, StringBuilder> ent : seqs.entrySet()) {
			stream.println(">" + ent.getKey());
			for (int i = 0; i < ent.getValue().length(); i+=len) {
				stream.println(ent.getValue().substring(i, Math.min(i+len, ent.getValue().length())));
			}
		}
		stream.close();
	}

	public String consense() {
		return consense(false);
	}
	
	public String consense(boolean majority) {
		char vet [] = new char[size];
		if(!majority) {
			for (int i = 0; i < size; i++) {
				char c = '\0';
				for (StringBuilder string : seqs.values()) {
					if(c == '\0')
						c = string.charAt(i);
					else if(c != string.charAt(i))
						c = '*';
				}
				vet[i] = c;
			}
		}
		else {			
			for (int i = 0; i < size; i++) {
				HashMap<Character, Integer> hash = new HashMap<>(); 
				for (StringBuilder string : seqs.values()) {
					Character c = string.charAt(i);
					if(hash.containsKey(c))
						hash.put(c, hash.get(c) + 1);
					else
						hash.put(c, 1);
				}
				Entry<Character, Integer> c = null;
				for (Entry<Character, Integer> ent : hash.entrySet()) {
					if(c == null || ent.getValue() > c.getValue())
						c = ent;
				}
				vet[i] = c.getKey();
			}
		}
		return new String(vet);
	}

	public <ATTRIBUTE extends Attribute<ATTRIBUTE>> double scoreDiff(RegistryGroup group, LinkedList<NodeGene<String, ATTRIBUTE>> nodes) throws FileNotFoundException {
		HashSet<String> newGroup = new HashSet<>();
		for (NodeGene<String, ?> node : nodes) {
			if(group.contain(node.getGene().getOrganism().getRoot()))
				newGroup.add(node.getKey());
		}
		return scoreDiff(newGroup);
	}
	
	public double scoreDiff(HashSet<String> group) throws FileNotFoundException {
		if(group.isEmpty())
			return 0;
		
		HashSet<String> outs = new HashSet<>();
		for (String string : seqs.keySet()) {
			if(!group.contains(string))
				outs.add(string);
		}
		
		if(outs.isEmpty())
			return 0;
		
		Blosum blosum = new Blosum(new File("/home/caio/Dropbox/workspace/Ferramentas/BLOSUM62.txt"));
		double score = 0;
		
		for (int i = 0; i < size; i++) {
			double pos = 0;
			for (String s1 : group) {
				for (String s2 : outs) {
					if((seqs.get(s1).charAt(i) == '-' || seqs.get(s2).charAt(i) == '-' ) && seqs.get(s1).charAt(i) != seqs.get(s2).charAt(i))
						pos += 10;
					else if(seqs.get(s1).charAt(i) != seqs.get(s2).charAt(i))
						pos += -blosum.get(seqs.get(s1).charAt(i), seqs.get(s2).charAt(i));
				}
			}
			//System.out.println(pos + "\t" + (pos/(group.size()*outs.size())));
			score += pos/(group.size()*outs.size()); 
		}
		
		return (score/size);
	}
	
	public <ATTRIBUTE extends Attribute<ATTRIBUTE>> double scoreDiffSimple(RegistryGroup group, Dictionary dic) throws FileNotFoundException {
		HashSet<String> newGroup = new HashSet<>();
		for (String string : seqs.keySet()) {
			if(group.contain(dic.getGeneByKey(string).getOrganism().getRoot()))
				newGroup.add(string);
		}
		
		return scoreDiffSimple(newGroup);
	}
	
	public double scoreDiffSimple(HashSet<String> group) {
		if(group.isEmpty())
			return 0;
		
		HashSet<String> outs = new HashSet<>();
		for (String string : seqs.keySet()) {
			if(!group.contains(string))
				outs.add(string);
		}
		
		if(outs.isEmpty())
			return 0;
		
		int diffs = 0;
		
		for (int i = 0; i < size; i++) {
			boolean diff = true;
			for (String s1 : group) {
				for (String s2 : outs) {
					if(seqs.get(s1).charAt(i) == seqs.get(s2).charAt(i))
						diff = false;
				}
			}
			if(diff) {
				diffs++;
			}
		}
		
		double diffs2 = diffs;
		return (diffs2/size);
	}
	
	public static void main(String args[]) throws FileNotFoundException {
		Alignment align = new Alignment(new AlignmentFile("/home/caio/Dropbox/workspace/Ferramentas/rel/a.alin", FileType.fasta, SequenceType.AminoAcids));
		HashSet<String> group = new HashSet<>();
		//group.add("fig|1314.380.peg.1");
		//group.add("fig|1314.415.peg.1787");
		
		double score = align.scoreDiffSimple(group);
		System.out.println(score);
		System.out.println((score/align.size));
		//align.exportNexus(new File("b.align"));
	}

	public double diversity() {
		LinkedList<StringBuilder> ss = new LinkedList<>(seqs.values());
		double diversity = 0;
		for (int i = 0; i < ss.getFirst().length(); i++) {
			int diffs = 0;
			for (StringBuilder seq1 : ss) {
				for (StringBuilder seq2 : ss) {
					if(seq1.charAt(i) != seq2.charAt(i))
						diffs++;
				}
			}
			diversity += ((double)(diffs)) / (ss.getFirst().length()*ss.getFirst().length());
		}
		return diversity/ss.getFirst().length();
	}

	
	
	
}
