package Structure.Registry;

import java.io.File;
import java.io.IOException;

public interface GeneRegistry {
	public void setNucleotides(long start, int len, int lenHeader, long lenSeq, File f);
	
	public String getKey();
	public int getId();
	public String get() throws IOException;
	public String getSequence() throws IOException;
	public String getAminoacids() throws IOException;
	public String getNucleotides() throws IOException;
	public GeneRegistry getNucleotidesRegistry();
	public String toString();
	public OrganismRegistry getOrganism();
	public String getHeader();
	public int getLength();
	public long getLengthByteSeq();
}
