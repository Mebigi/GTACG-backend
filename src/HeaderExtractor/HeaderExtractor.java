package HeaderExtractor;

import Structure.Registry.GeneRegistry;
import Structure.Registry.OrganismRegistry;

public interface HeaderExtractor {
	public String getFileName(OrganismRegistry reg);
	
	public String getDescription(GeneRegistry reg);
}
