package HeaderExtractor;

import Structure.Registry.GeneRegistry;
import Structure.Registry.OrganismRegistry;

public class HeaderExtractorPatric implements HeaderExtractor {
	public String getFileName(OrganismRegistry reg) {
		String str = reg.getFile().getName();
		return str.substring(0, str.indexOf("."));
	}
	
	public String getDescription(GeneRegistry reg) {
		try {
			String header = reg.getHeader();
			if(header.indexOf(" ") > 0 && header.lastIndexOf("[") > 0) {
				if(header.indexOf(" ") + 1 < header.lastIndexOf("["))
					header = header.substring(header.indexOf(" ") + 1, header.lastIndexOf("[") - 1);
				else
					header = header.substring(header.indexOf(" ") + 1);
			}
			else if(header.indexOf(" ") > 0)
				header = header.substring(header.indexOf(" ") + 1);
			else
				header = "";
			return header;
		} catch (Exception e) {
			return "";
		}
	}
	
	public String getDescription(String header) {
		try {
			if(header.indexOf(" ") > 0 && header.lastIndexOf("[") > 0) {
				if(header.indexOf(" ") + 1 < header.lastIndexOf("["))
					header = header.substring(header.indexOf(" ") + 1, header.lastIndexOf("[") - 1);
				else
					header = header.substring(header.indexOf(" ") + 1);
			}
			else if(header.indexOf(" ") > 0)
				header = header.substring(header.indexOf(" ") + 1);
			else
				header = "";
			return header;
		} catch (Exception e) {
			return "";
		}
	}
}
