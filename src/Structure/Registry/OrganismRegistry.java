package Structure.Registry;

import java.awt.Color;
import java.io.File;
import java.util.LinkedList;

public class OrganismRegistry extends Registry {
	private String name;
	private String abbrev;
	private Color color;
	private String header;
	
	public LinkedList<GeneRegistry> aminos = new LinkedList<>();
	public LinkedList<GeneRegistry> nucls = new LinkedList<>();
	public LinkedList<GeneRegistry> aminosPlasm = new LinkedList<>();
	//public LinkedList<GeneRegistry> nuclsPlasm = new LinkedList<>();
	public LinkedList<OrganismRegistry> plasmideos = new LinkedList<>();
	private OrganismRegistry root;
	
	
	public OrganismRegistry(String key, String header, long start, int len, int lenHeader, long lenSeq, File f) {
		super(key, header, start, len, lenHeader, lenSeq, f);
		this.header = header;
	}
	
	public OrganismRegistry(String key, String header, long start, int len, int lenHeader, long lenSeq, File f, String name, String abbrev, Color color) {
		this(key, header, start, len, lenHeader, lenSeq, f);
		this.name = name;
		this.abbrev = abbrev;
		this.color = color;
	}
	
	@Override
	public String toString() {
		return root.getKey() + "\t" + super.toString();
	}
	
	public OrganismRegistry getRoot() {
		return root;
	}
	
	public void setRoot(OrganismRegistry raiz) {
		this.root = raiz;
	}
	
	@Override
	public String getHeader() {
		return header;
	}
	
	public String getName() {
		return name;
	}
	
	public String getAbbrev() {
		return abbrev;
	}
	
	public Color getColor() {
		return color;
	}
	
	public LinkedList<GeneRegistry> getGenes() {
		return aminos;
	}
	
	public LinkedList<GeneRegistry> getAllGenes() {
		LinkedList<GeneRegistry> result = new LinkedList<>(aminos);
		result.addAll(aminosPlasm);
		return result;
	}
}
