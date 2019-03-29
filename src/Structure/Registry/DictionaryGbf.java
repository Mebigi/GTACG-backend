package Structure.Registry;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;

public class DictionaryGbf extends Dictionary {
	public enum Format {
		GBF,
		GFF;
	}
	
	public DictionaryGbf(File dic, Format format) throws IOException {
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
			File gbfFile = new File(base.getAbsolutePath(), line[6]);
			
			OrganismRegistry reg = addOrganismFile(genFile, name, abbrev, Color.decode(color));
			reg.setRoot(reg);
			if(format == null || format == Format.GBF)
				reg.aminos = addAminoFile(genFile, gbfFile, reg);
			else
				reg.aminos = addAminoFileGff(genFile, gbfFile, reg);
			
			for (int i = 0; i < plasm; i++) {
				line = sc.nextLine().split("\t");
				name = line[0];
				abbrev = line[1];
				//String codigo = linha[2];
				color = line[4];
				genFile = new File(base.getAbsolutePath(), line[5]);
				gbfFile = new File(base.getAbsolutePath(), line[6]);
				
				if(gbfFile.exists()) {
					OrganismRegistry regPlasm = addOrganismFile(genFile, name, abbrev, Color.decode(color));
					regPlasm.setRoot(reg);
					reg.plasmideos.add(regPlasm);
					//regPlasm.aminos = addAminoFile(genFile, gbfFile, reg);
					if(format == null || format == Format.GBF)
						regPlasm.aminos = addAminoFile(genFile, gbfFile, reg);
					else
						regPlasm.aminos = addAminoFileGff(genFile, gbfFile, reg);
				}
				else
					System.out.println("File not found: " + genFile);
			}
		}
		sc.close();
	}

	public LinkedList<GeneRegistry> addAminoFile(File genFile, File gbfFile, OrganismRegistry org) throws IOException {
		int count = 0;
		HashMap<String,Integer> starts = new HashMap<>();
		Scanner genScanner = new Scanner(genFile);
		String line;
		
		boolean startFile = false;
		Integer lineSize = null;
		while(genScanner.hasNextLine()) {
			line = genScanner.nextLine();
			count += line.getBytes("UTF-8").length + 1;
			//System.out.println((line.length() + 1) + "\t" + line.getBytes("UTF-8").length);
			if(line.equals(""))
				break;
			if(line.charAt(0) == '>') {
				startFile = true;
				if(line.indexOf(' ') >= 0)
					starts.put(line.substring(1, line.indexOf(' ')), count);
				else
					starts.put(line.substring(1), count);
			}
			else if(startFile && lineSize == null)
				lineSize = line.length();
		}
		genScanner.close();
		
		int startContig = 0;
		
		LinkedList<GeneRegistry> list = new LinkedList<>();
		BufferedReader bf = new BufferedReader(new FileReader(gbfFile));
		line = bf.readLine();

		int start = 0;
		int end = 0;
		boolean complement = false;
		String gene = null;
		String key = null;
		String contig = null;
		String product = null;
		while(line != null) {
			if(line.equals("//") || line.indexOf("     CDS") != -1 || line.indexOf("LOCUS       ") != -1) {
				if(key != null) {
					GeneRegistryFna reg = new GeneRegistryFna(key, gene, contig, complement, ">" + key + " " + product, lineSize, startContig, start, end, genFile, org);
					addGeneRegistry(reg);
					list.add(reg);
					GeneRegistryFna regNucl = new GeneRegistryFna(key, gene, contig, complement, ">" + key + " " + product, lineSize, startContig, start, end, genFile, org);
					reg.setNucleotides(regNucl);
					
					start = 0;
					end = 0;
					complement = false;
					gene = null;
					key = null;
					product = null;
				}
			}
			
			if(line.indexOf("LOCUS       ") == 0) {
				contig = line.replace("LOCUS       ", "");
				contig = contig.substring(0, contig.indexOf(' '));
				startContig = starts.get(contig);
			}
			else if(line.indexOf("     CDS             ") == 0) {
				line = line.replace("     CDS             ", "");
				if(line.indexOf("complement") != -1) {
					line = line.replace("complement(", "").replace(")", "");
					start = Integer.parseInt(line.substring(0, line.indexOf(".")));
					end = Integer.parseInt(line.substring(line.lastIndexOf(".")+1));
					complement = true;
				}
				else {
					start = Integer.parseInt(line.substring(0, line.indexOf(".")));
					end = Integer.parseInt(line.substring(line.lastIndexOf(".")+1));
				}				
			}
			else if(gene == null && line.indexOf("/gene") != -1)
				gene = line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\""));
			else if(key == null && line.indexOf("/locus_tag") != -1)
				key = line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\""));
			else if(key == null && line.indexOf("/db_xref") != -1)
				key = line.substring(Math.max(line.indexOf("\""), line.indexOf(":")) + 1, line.lastIndexOf("\""));
			else if(product == null && line.indexOf("/product") != -1) {
				while(line.indexOf("\"") == line.lastIndexOf("\""))
					line += bf.readLine().replace("                    ", "");
				product = line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\""));
			}
			line = bf.readLine();
		}
		bf.close();
		return list;
	}
	
	public LinkedList<GeneRegistry> addAminoFileGff(File genFile, File gbfFile, OrganismRegistry org) throws IOException {
		int count = 0;
		HashMap<String,Integer> starts = new HashMap<>();
		Scanner genScanner = new Scanner(genFile);
		String line;
		
		boolean startFile = false;
		Integer lineSize = null;
		while(genScanner.hasNextLine()) {
			line = genScanner.nextLine();
			count += line.getBytes("UTF-8").length + 1;
			if(line.equals(""))
				break;
			if(line.charAt(0) == '>') {
				startFile = true;
				if(line.indexOf(' ') >= 0)
					starts.put(line.substring(1, line.indexOf(' ')), count);
				else
					starts.put(line.substring(1), count);
			}
			else if(startFile && lineSize == null)
				lineSize = line.length();
		}
		genScanner.close();
		
		int startContig = 0;
		
		LinkedList<GeneRegistry> list = new LinkedList<>();
		BufferedReader bf = new BufferedReader(new FileReader(gbfFile));
		line = bf.readLine();
		line = bf.readLine();

		int start = 0;
		int end = 0;
		boolean complement = false;
		String gene = null;
		String key = null;
		String contig = null;
		String product = null;
		while(line != null) {
			String[] splitLine = line.split("\t");
			if("CDS".equals(splitLine[2])) {
				contig = splitLine[0];
				try {
					startContig = starts.get(contig);
					
				} catch (Exception e) {
					System.err.println(contig);
					System.err.println(gbfFile);
					startContig = starts.get(contig);
				}
				start = Math.min(Integer.parseInt(splitLine[3]), Integer.parseInt(splitLine[4]));
				end   = Math.max(Integer.parseInt(splitLine[3]), Integer.parseInt(splitLine[4]));
				complement = "-".equals(splitLine[6]);

				for (String attribute : splitLine[8].split(";")) {
					if(attribute.startsWith("ID="))
						key = attribute.replace("ID=", "");
					else if(attribute.startsWith("Name="))
						product = attribute.replace("Name=", "");
				}
				
				GeneRegistryFna reg = new GeneRegistryFna(key, gene, contig, complement, ">" + key + " " + product, lineSize, startContig, start, end, genFile, org);
				addGeneRegistry(reg);
				list.add(reg);
				GeneRegistryFna regNucl = new GeneRegistryFna(key, gene, contig, complement, ">" + key + " " + product, lineSize, startContig, start, end, genFile, org);
				reg.setNucleotides(regNucl);
			}
			line = bf.readLine();
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
	
	public void addGeneRegistry(GeneRegistry reg) {
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
}