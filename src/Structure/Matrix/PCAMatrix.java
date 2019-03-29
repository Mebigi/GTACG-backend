package Structure.Matrix;

import Structure.Registry.OrganismRegistry;

public class PCAMatrix {
	public OrganismRegistry orgs[];
	public double values[][];
	public double variance[];
	
	public PCAMatrix(OrganismRegistry orgs[], int numFamilias, double values[][], double variance[]) {
		this.orgs = orgs;
		this.values = values;
		this.variance = variance;
	}
	
/*	private void exec(PrintStream stream, Rengine re, String s) {
		if(stream != null) stream.println(s);
		re.eval(s);
	}

	public void plot(PrintStream stream, Rengine re, RegistryGroups groups, boolean autoColor) {
		if (!re.isAlive()) {
			System.out.println ("Cannot load R");
			return;
		}
		
		exec(stream, re, "m <- matrix(0, nrow = " + orgs.length + ", ncol = 2)");
		exec(stream, re, "m <- data.frame(m)");
		exec(stream, re, "colnames(m) <- c(\"x\", \"y\")");
		for (int i = 0; i < orgs.length; i++) {
			exec(stream, re, "m["+ (i+1) +", ] = c(" + values[0][i] + ", " + values[1][i] + ")");
		}
		String s = "\"" + orgs[0].getAbbrev();
		for (int i = 1; i < orgs.length; i++) {
			s += "\", \"" + orgs[i].getAbbrev();
		}
		exec(stream, re, "row.names(m) <- c(" + s + "\")");
		
		String colorOrg = "";
		String colorGroup = "";
		String nameGroup = "";
		String names = "";
		for (int i = 0; i < orgs.length; i++) {
			System.out.println("a");
			colorOrg += ",\"#" + Integer.toHexString(orgs[i].getColor().getRGB()).substring(2) + "\"";
			colorGroup += ",\"" + (groups==null?"null":"#"+Integer.toHexString(groups.getGroup(orgs[i]).getColor().getRGB()).substring(2)) + "\"";
			nameGroup += ",\"" + (groups==null?"null":groups.getGroup(orgs[i]).getName()) + "\"";
			names += ",\"" + orgs[i].getAbbrev() + "\"";
			
		}
		exec(stream, re, "colorOrg = c(" + colorOrg.substring(1) + ")");
		exec(stream, re, "colorGroup = c(" + colorGroup.substring(1) + ")");
		exec(stream, re, "nameGroup = c(" + nameGroup.substring(1) + ")");
		exec(stream, re, "names = c(" + names.substring(1) + ")");
		String opColor = "";
        if(autoColor) {
        	if(groups == null)
        		opColor = "factor(names)";
        	else
        		opColor = "factor(nameGroup)";
        }
        else {
        	if(groups == null)
        		opColor = "colorOrg";
        	else
        		opColor = "colorGroup";
        }
		
		exec(stream, re, "library(ggplot2)");
		exec(stream, re, "library(ggrepel)");
		exec(stream, re, "ggplot(m) + geom_point(aes(x, y), color = 'red') + geom_label_repel(aes(x, y, label = rownames(m)), fill = " + opColor + ", fontface = 'bold', color = 'white', box.padding = unit(0.35, \"lines\"), point.padding = unit(0.5, \"lines\"),  segment.color = 'grey50')");
	}*/
}
