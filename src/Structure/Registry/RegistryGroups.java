package Structure.Registry;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

public class RegistryGroups {
	private String name;
	private RegistryGroup groups [];
	private int ids = 0;
	public HashMap<Registry, RegistryGroup> mapGrups = new HashMap<Registry, RegistryGroup>();
	
	public RegistryGroups() {
		
	}
	
	public RegistryGroups(String name) {
		this.name = name;
	}
	
	public RegistryGroups(String name, int groups) {
		this.name = name;
		this.groups = new RegistryGroup[groups];
	}
	
	public RegistryGroups(File gFile, Dictionary dic) throws FileNotFoundException {
		Scanner sc = new Scanner(gFile);
		String header[] = sc.nextLine().split("\t"); 
		
		name = header[1];
		groups = new RegistryGroup[Integer.parseInt(header[0])];
		
		for (int i = 0; i < groups.length; i++) {
			String line [] = sc.nextLine().split("\t");
			int totalGenomes = Integer.parseInt(line[0]);
			String name = line[1];
			String color = "";
			if(line.length == 3)
				color = line[2].trim();
			RegistryGroup group = new RegistryGroup(name, color, ids);
			ids++;
			groups[i] = group;
			for (int j = 0; j < totalGenomes; j++) {
				String file = (new File(sc.nextLine())).getName().replace(".fna", "");
				OrganismRegistry genome = dic.getOrgByFilename(file);
				group.add(genome);
				mapGrups.put(genome, group);
			}
		}
		sc.close();
	}
	
	public String getName() {
		return name;
	}
	
	public RegistryGroup[] getGroups() {
		return groups;
	}
	
	public RegistryGroup getById(int id) {
		return groups[id];
	}
	
	public RegistryGroup getGroup(OrganismRegistry org) {
		return mapGrups.get(org);
	}
	
	public int size() {
		return groups.length;
	}
}
