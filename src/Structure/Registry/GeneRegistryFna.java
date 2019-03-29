package Structure.Registry;

import java.io.File;
import java.io.IOException;

import Structure.Constants;

public class GeneRegistryFna extends Registry implements GeneRegistry {
	private OrganismRegistry organism;
	private String header;
	private GeneRegistryFna nucl = null;
	private String gene;
	private String contig;
	private boolean complement;
	private long start;
	private long end;

	public GeneRegistryFna(String key, String gene, String contig, boolean complement, String header, long lineSize, int startContig, long start, long end, File f, OrganismRegistry org) {		
		super(key, header, startContig + start + (int)(start/lineSize) -(start%lineSize==0?1:0) -1, (int)(end-start+1 + (int)(end/lineSize) - (int)((start -(start%lineSize==0?1:0))/lineSize)), 0, end-start+1, f);
		this.header = header;
		organism = org;
		this.gene = gene;
		this.contig = contig;
		this.complement = complement;
		this.start = start;
		this.end = end;
	}
	
	public void setNucleotides(GeneRegistryFna nucl) {
		this.nucl = nucl;
	}
	
	@Override 
	public String get() throws IOException{
		if(nucl != null) {
			if(!complement)
				return header + "\n" + Constants.translate(super.get(), 60);
			return header + "\n" + Constants.translateComplement(super.get(), 60);
		}
		else {
			if(!complement)
				return header + "\n" + Constants.formatSeq(super.get(), 60);
			return header + "\n" + Constants.formatComplementSeq(super.get(), 60);
		}
	}
	
	public String getSequence() throws IOException {
		return super.get();
	}
	
	public String getAminoacids() throws IOException {
		return get();
	}

	public String getNucleotides() throws IOException {
		if(nucl != null)
			return nucl.get();
		return null;
	}
	
	public GeneRegistryFna getNucleotidesRegistry() {
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
	
	public String getGeneName() {
		return gene;
	}
	
	public String getContig() {
		return contig;
	}
	
	public boolean isComplement() {
		return complement;
	}
	
	public long getStart() {
		return this.start;
	}
	
	public long getEnd() {
		return this.end;
	}
	
	public long getLengthSeq() {
		if(nucl != null)
			return (getEnd() - getStart())/3;
		return getEnd() - getStart();
	}
	
	@Override
	public long getLengthByteSeq() {
		if(nucl != null)
			return (getEnd() - getStart())/3;
		return getEnd() - getStart();
	}

	@Override
	public void setNucleotides(long start, int len, int lenHeader, long lenSeq, File f) {
		// TODO Auto-generated method stub
		
	}
}