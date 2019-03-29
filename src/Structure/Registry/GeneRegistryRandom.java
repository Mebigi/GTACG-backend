package Structure.Registry;

import java.io.File;
import java.io.IOException;

public class GeneRegistryRandom extends Registry implements GeneRegistry {
	private OrganismRegistry organism;
	private String header;
	private GeneRegistryRandom nucl = null;

	public GeneRegistryRandom(String key, String header, long start, int len, int lenHeader, long lenSeq, File f, OrganismRegistry org) {
		super(key, header, start, len, lenHeader, lenSeq, f);
		organism = org;
		this.header = header;
	}
	
	public void setNucleotides(String header, long start, int len, int lenHeader, long lenSeq, File f) {
		nucl = new GeneRegistryRandom(getKey(), header, start, len, lenHeader, lenSeq, f, getOrganism());
	}
	
	public String getSequence() throws IOException {
		String sequence = get();
		return sequence.substring(sequence.indexOf("\n") + 1);
	}
	
	public String getAminoacids() throws IOException {
		return get();
	}

	public String getNucleotides() throws IOException {
		if(nucl != null)
			return nucl.get();
		return null;
	}
	
	public GeneRegistryRandom getNucleotidesRegistry() {
		return nucl;
	}
	
	@Override
	public String toString() {
		return organism.getKey() + "\t" + super.toString(); 
	}
	
	public OrganismRegistry getOrganism() {
		return organism;
	}
	
	@Override
	public String getHeader() {
		return header;
	}

	@Override
	public void setNucleotides(long start, int len, int lenHeader, long lenSeq, File f) {
		nucl = new GeneRegistryRandom(super.getKey(), header, start, len, lenHeader, lenSeq, f, organism);
	}
}
