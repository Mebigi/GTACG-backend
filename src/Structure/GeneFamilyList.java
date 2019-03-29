package Structure;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Scanner;

import Structure.Registry.Dictionary;
import Structure.Registry.GeneRegistry;
import Structure.Registry.GeneRegistryRandom;
import Structure.Registry.OrganismRegistry;
import Structure.Registry.Registry;
import gnu.trove.map.hash.THashMap;

public class GeneFamilyList {
	private THashMap<String, LinkedList<GeneFamily>> hashGenes = new THashMap<>();
	private LinkedList<GeneFamily> fams = new LinkedList<>();
	
	private GeneFamilyList() throws FileNotFoundException {
		
	}
	
	public static GeneFamilyList loadMclFormat(File in, Dictionary dic) throws FileNotFoundException {
		GeneFamilyList famList = new GeneFamilyList();
		Scanner sc = new Scanner(in);
		while(sc.hasNextLine()) {
			String line = sc.nextLine();
			String split[] = line.split("\t");
			LinkedList<GeneRegistry> list = new LinkedList<>();
			
			for (int i = 0; i < split.length; i++) {
				list.add(dic.getGeneByKey(split[i]));
			}
			GeneFamily fam = new GeneFamily(list, true);
			famList.fams.add(fam);
			if(!famList.hashGenes.containsKey(list.getFirst().getKey()))
				famList.hashGenes.put(list.getFirst().getKey(), new LinkedList<>());
			famList.hashGenes.get(list.getFirst().getKey()).add(fam);
		}
		sc.close();
		
		for (OrganismRegistry org : dic.getOrganisms()) {
			for (GeneRegistry gene : org.aminos) {
				if(famList.getFamlilies(gene.getKey()) == null) {
					LinkedList<GeneRegistry> list = new LinkedList<>();
					list.add(gene);
					famList.fams.add(new GeneFamily(list, true));
				}
			}
		}
		return famList;
	}
	
	public static GeneFamilyList loadCdHitFormat(File in, Dictionary dic) throws FileNotFoundException {
		return null;
	}
		

	public  LinkedList<GeneFamily> getFamlilies(String key) {
		return hashGenes.get(key);
	}

	public LinkedList<GeneFamily> getList() {
		return fams;
	}
}
