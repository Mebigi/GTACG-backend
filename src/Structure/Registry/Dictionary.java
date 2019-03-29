package Structure.Registry;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Scanner;

import Structure.Registry.DictionaryGbf.Format;
import gnu.trove.map.hash.THashMap;

public class Dictionary {
	protected THashMap<String, GeneRegistry> hashGeneKey = new THashMap<>(10000, (float)0.75);
	protected THashMap<String, OrganismRegistry> hashOrgKey = new THashMap<>(10000, (float)0.75);
	protected THashMap<String, OrganismRegistry> hashOrgAbbrev = new THashMap<>(10000, (float)0.75);
	protected THashMap<String, OrganismRegistry> hashOrgFilename = new THashMap<>(10000, (float)0.75);
	protected THashMap<Integer, OrganismRegistry> hashOrgId = new THashMap<>(10000, (float)0.75);
	
	public static Dictionary getDictionary(File dicFile, String dicFormat) throws IOException {
		Dictionary dicTmp = null;
		if(dicFormat == null || dicFormat.equals("faa"))
			dicTmp = new Dictionary(dicFile);
		else if(dicFormat.equals("gbf"))
			dicTmp = new DictionaryGbf(dicFile, Format.GBF);
		else if (dicFormat.equals("gff"))
			dicTmp = new DictionaryGbf(dicFile, Format.GFF);
		Dictionary dic = dicTmp;
		return dic;
	}
	
	public Dictionary()  {
		
	}
	
	public Dictionary(File folder, String extGenoma, String extGene) throws IOException, URISyntaxException {
		read(folder, extGenoma, extGene);
	}
	
	public Dictionary(File dic) throws IOException {
		File base = dic.getParentFile();
		Scanner sc = new Scanner(dic);
		sc.nextLine();
		while(sc.hasNext()) {
			String line[] = sc.nextLine().split("\t");
			String name = line[0];
			String abbrev = line[1];
			//String codigo = linha[2];
			int plasm = Integer.parseInt(line[3]);
			String color = line[4];
			File genFile = new File(base.getAbsolutePath(), line[5]);
			File nuclFile = new File(base.getAbsolutePath(), line[6]);
			File aminFile = new File(base.getAbsolutePath(), line[7]);
			
			OrganismRegistry reg = addOrganismFile(genFile, name, abbrev, Color.decode(color));
			reg.setRoot(reg);
			reg.aminos = addAminoFile(aminFile, reg);
			reg.nucls = addNuclFile(nuclFile, reg);
			
			for (int i = 0; i < plasm; i++) {
				line = sc.nextLine().split("\t");
				name = line[0];
				abbrev = line[1];
				//String codigo = linha[2];
				color = line[4];
				genFile = new File(base.getAbsolutePath(), line[5]);
				nuclFile = new File(base.getAbsolutePath(), line[6]);
				aminFile = new File(base.getAbsolutePath(), line[7]);
				
				if(aminFile.exists()) {
					OrganismRegistry regPlasm = addOrganismFile(genFile, name, abbrev, Color.decode(color));
					regPlasm.setRoot(reg);
					reg.plasmideos.add(regPlasm);
					regPlasm.aminos = addAminoFile(aminFile, reg);
					regPlasm.nucls = addNuclFile(nuclFile, reg);
				}
				else
					System.out.println("File not found: " + genFile);
			}
			
		}
		sc.close();
	}

	public LinkedList<GeneRegistry> addAminoFile(File f, OrganismRegistry org) throws IOException {
		LinkedList<GeneRegistry> list = new LinkedList<>();
		
		BufferedReader bf = new BufferedReader(new FileReader(f));
		long byteStart = 0;
		String header = bf.readLine();
		int lenHeader = header.length();
		int lenSeq = 0;
		int numLines = 0;
		
		String line = bf.readLine();
		while(line != null) {
			if(line.charAt(0) == '>') {
				String id = header.substring(1, Math.max(Math.min(header.indexOf(" "), header.length()), 1));
				if("".equals(id)) 
					id = header.substring(1);
				GeneRegistryRandom reg = new GeneRegistryRandom(id, header, byteStart, lenHeader + lenSeq + numLines, lenHeader, lenSeq, f, org);
				addGeneRegistry(reg);
				list.add(reg);
				byteStart += reg.getLength() + 1;
				
				header = line;
				lenHeader = line.getBytes("UTF-8").length;
				lenSeq = 0;
				numLines = 0;
			}
			else {
				lenSeq += line.getBytes("UTF-8").length;
				numLines++;
			}
			line = bf.readLine();
		}
		String id = header.substring(1, Math.max(Math.min(header.indexOf(" "), header.length()), 1));
		if("".equals(id)) 
			id = header.substring(1);
		GeneRegistryRandom reg = new GeneRegistryRandom(id, header, byteStart, lenHeader + lenSeq, lenHeader, lenSeq, f, org);
		addGeneRegistry(reg);
		list.add(reg);
		bf.close();
		return list;
	}
	
	public LinkedList<GeneRegistry> addNuclFile(File f, OrganismRegistry org) throws IOException {
		LinkedList<GeneRegistry> list = new LinkedList<>();
		
		BufferedReader bf = new BufferedReader(new FileReader(f));
		long byteStart = 0;
		String header = bf.readLine();
		int lenHeader = header.length();
		int lenSeq = 0;
		int numLines = 0;
		
		String line = bf.readLine();
		while(line != null) {
			if(line.charAt(0) == '>') {
				String id = header.substring(1, Math.max(Math.min(header.indexOf(" "), header.length()), 1));
				if("".equals(id)) 
					id = header.substring(1);
				GeneRegistry reg = getGeneById(id); 
				if(reg != null) {
					reg.setNucleotides(byteStart, lenHeader + lenSeq + numLines, lenHeader, lenSeq, f);
				}
				else {
					reg = new GeneRegistryRandom(id, header, byteStart, lenHeader + lenSeq + numLines, lenHeader, lenSeq, f, org);
					//addGeneRegistry(reg);
					list.add(reg);
				}
				if(reg.getNucleotidesRegistry() == null)
					byteStart += reg.getLength() + 1;
				else
					byteStart += reg.getNucleotidesRegistry().getLength() + 1;
				
				header = line;
				lenHeader = line.getBytes("UTF-8").length;
				lenSeq = 0;
				numLines = 0;
			}
			else {
				lenSeq += line.getBytes("UTF-8").length;
				numLines++;
			}
			line = bf.readLine();
		}
		String id = header.substring(1, Math.max(Math.min(header.indexOf(" "), header.length()), 1));
		if("".equals(id)) 
			id = header.substring(1);
		GeneRegistry reg = getGeneById(id); 
		if(reg != null) {
			reg.setNucleotides(byteStart, lenHeader + lenSeq + numLines, lenHeader, lenSeq, f);
			/* TODO reg.setNucleotides(header, byteStart, lenHeader + lenSeq, lenHeader, lenSeq, f);*/
		}
		else {
			reg = new GeneRegistryRandom(id, header, byteStart, lenHeader + lenSeq, lenHeader, lenSeq, f, org);
			//addGeneRegistry(reg);
			list.add(reg);
		}
		bf.close();
		return list;
	}

	
	public OrganismRegistry addOrganismFile(File f, String name, String abbrev, Color color) throws IOException {
		BufferedReader bf = new BufferedReader(new FileReader(f));
		long byteStart = 0;
		String header = bf.readLine();
		int x = header.indexOf(" ");
		x = Math.min(x, header.length());
		x = Math.max(x, 1);
		String id = header.substring(1, x);
		OrganismRegistry reg = new OrganismRegistry(id, header, byteStart, (int)f.length(), header.length(), ((int)f.length()) - header.length() -1, f, name, abbrev, color);
		bf.close();
		addOrganismRegistry(reg);
		return reg;
	}
	
	public void addGeneRegistry(GeneRegistryRandom reg) {
		hashGeneKey.put(reg.getKey(), reg);
	}
	
	public void addOrganismRegistry(OrganismRegistry reg) {
		hashOrgKey.put(reg.getKey(), reg);
		hashOrgAbbrev.put(reg.getAbbrev(), reg);
		hashOrgFilename.put(reg.getFile().getName().replace(".fna", ""), reg);
		hashOrgId.put(reg.getId(), reg);
	}
	
	public GeneRegistry getGeneByKey(String header) {
		return hashGeneKey.get(header);
	}
	
	public GeneRegistry getGeneById(String id) {
		return hashGeneKey.get(id);
	}
	
	public Collection<GeneRegistry> getGenes() {
		return hashGeneKey.values();
	}
	
	public OrganismRegistry getOrgByKey(String id) {
		return hashOrgKey.get(id);
	}	
	
	public OrganismRegistry getOrgByFilename(String filename) {
		return hashOrgFilename.get(filename);
	}
	
	public OrganismRegistry getOrgByAbbrev(String abbrev) {
		return hashOrgAbbrev.get(abbrev);
	}
	
	public OrganismRegistry getOrgById(Integer id) {
		return hashOrgId.get(id);
	}
	
	public void read(File folder, String extGenoma, String extGene) throws IOException, URISyntaxException {
		for (File organismoFolder : folder.listFiles()) {
			if(organismoFolder.isDirectory()) {
				LinkedList<OrganismRegistry> plasmideos = new LinkedList<OrganismRegistry>();
				OrganismRegistry max = null;
				
				for (File arquivo : organismoFolder.listFiles()) {
					if(arquivo.getName().contains(extGenoma)) {
						File faa = new File(arquivo.getAbsolutePath().replace(extGenoma, extGene));
						//System.out.println(faa);
						if(faa.exists()) {
							OrganismRegistry reg = addOrganismFile(arquivo, null, null, null);
							//reg.setSumario(getJsonFile(new File(arquivo.getAbsolutePath().replace(".fna", ".summary")))); 
							reg.aminos = addAminoFile(faa, reg);
							if(max == null)
								max = reg;
							else {
								if(reg.getLength() > max.getLength()) {
									plasmideos.add(max);
									max = reg;
								}
								else
									plasmideos.add(reg);
							}
						}
					}
				}
				if(max != null) {
					
					max.plasmideos = plasmideos;
					max.setRoot(max);
					for (OrganismRegistry registroOrganismo : plasmideos) {
						max.aminosPlasm.addAll(registroOrganismo.aminos);
						registroOrganismo.setRoot(max);
					}
				}
			}
		}
	}

	public void export(File out) throws FileNotFoundException {
		PrintStream stream = new PrintStream(out);
		stream.println("ORGANISMOS");
		for (OrganismRegistry org : hashOrgFilename.values()) {
			stream.println(org);
		}
		stream.println("GENES");
		for (GeneRegistry gene: hashGeneKey.values()) {
			stream.println(gene);
		}
		stream.close();
	}
	
	public OrganismRegistry[] getOrganisms() {
		Collection<OrganismRegistry> orgs = hashOrgId.values();
		LinkedList<OrganismRegistry> orgsSP = new LinkedList<OrganismRegistry>();
		for (OrganismRegistry registroOrganismo : orgs) {
			if(registroOrganismo.getRoot() == registroOrganismo)
				orgsSP.add(registroOrganismo);
		}
		
		OrganismRegistry[] result = new OrganismRegistry[orgsSP.size()];
		int i = 0;
		for (OrganismRegistry registroOrganismo : orgsSP) {
			result[i] = registroOrganismo;
			i++;
		}
		return result;
	}
	
	public LinkedList<OrganismRegistry> getAllOrganisms() {
		return new LinkedList<OrganismRegistry>(hashOrgId.values());
	}
}