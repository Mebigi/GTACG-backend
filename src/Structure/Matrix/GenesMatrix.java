package Structure.Matrix;

import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

//import org.rosuda.JRI.Rengine;

import HeaderExtractor.HeaderExtractor;
import Structure.GeneFamily;
import Structure.Graph.GraphGenes;
import Structure.Graph.NodeGene;
import Structure.Registry.GeneRegistry;
import Structure.Registry.GeneRegistryRandom;
import Structure.Registry.OrganismRegistry;
import Structure.Restriction.Attribute;

public class GenesMatrix {
	private OrganismRegistry orgs[];
	private String labels[];
	private LinkedList<GeneRegistry> values[][];
	
	@SuppressWarnings("unchecked")
	private void iniValues(int numOrgs, int numFamilies) {
		values = new LinkedList[numFamilies][numOrgs];
	}
	
	public GenesMatrix(int numOrgs, int numFamilies) {
		orgs = new OrganismRegistry[numOrgs];
		labels = new String[numFamilies];
		iniValues(numOrgs, numFamilies);
	}
	
	public GenesMatrix(OrganismRegistry orgs[], int numFamilies) {
		this.orgs = orgs;
		labels = new String[numFamilies];
		iniValues(orgs.length, numFamilies);
	}
	
	public GenesMatrix(OrganismRegistry orgs[], String labelsFamilies[]) {
		this.orgs = orgs;
		labels = labelsFamilies;
		iniValues(orgs.length, labelsFamilies.length);
	}
	
	public <KEY_STATIC, ATTRIBUTE extends Attribute<ATTRIBUTE>, NO extends NodeGene<KEY_STATIC, ATTRIBUTE>, GRAFO extends GraphGenes<KEY_STATIC, ATTRIBUTE, NO>> GenesMatrix(GRAFO graph, OrganismRegistry[] orgs, HeaderExtractor ext) {
		this(graph, graph.connComponentHeads(), orgs, ext);
	}
	
	public <KEY_STATIC, ATTRIBUTE extends Attribute<ATTRIBUTE>, NO extends NodeGene<KEY_STATIC, ATTRIBUTE>, GRAFO extends GraphGenes<KEY_STATIC, ATTRIBUTE, NO>> GenesMatrix(GRAFO graph, LinkedList<NO> heads, OrganismRegistry[] orgs, HeaderExtractor ext) {
		this(orgs, heads.size());
		
		int k = 0;
		for (NO head : heads) {
			LinkedList<NO> component = graph.connComponentList(head);
			GeneFamily family = new GeneFamily(component);
			LinkedList<GeneRegistry>[] result = family.getArrayGenes(orgs);
			
			HashMap<String, Integer> hash = new HashMap<String, Integer>();
			for (int i = 0; i < result.length; i++) {
				if(result[i] != null) {
					setAll(k, i, result[i]);
					for (GeneRegistry reg : result[i]) {
						if(hash.containsKey(ext.getDescription(reg))) {
							int x = hash.get(ext.getDescription(reg));
							hash.put(ext.getDescription(reg), x + 1);
						}
						else {
							hash.put(ext.getDescription(reg), 1);
						}
					}
				}
			}
			
			int max = 0;
			String smax = "";
			for (String key : hash.keySet()) {
				if(hash.get(key) > max)
					smax = key;
			}
			setLabel(k, smax);
			k++;
		}
	}
	
	public LinkedList<GeneRegistry> get(int family, int org) {
		return values[family][org];
	}
	
	public void set(int family, int org, GeneRegistry v) {
		if(values[family][org] == null)
			values[family][org] = new LinkedList<GeneRegistry>();
		values[family][org].add(v);
	}
	
	public void setAll(int family, int org, Collection<GeneRegistry> v) {
		if(values[family][org] == null)
			values[family][org] = new LinkedList<GeneRegistry>();
		values[family][org].addAll(v);
	}
	
	public void setLabel(int family, String label) {
		labels[family] = label;
	}
	
	public OrganismRegistry[] getOrgs() {
		return orgs;
	}
	
	public static interface PrintList {
		public String print(LinkedList<GeneRegistry> list);
	}
	
	public static class PrintListKeys implements PrintList { 
		public String print(LinkedList<GeneRegistry> list) {
			if(list == null)
				return "";
			else {
				boolean ok = false;
				String s = "";
				for (GeneRegistry reg : list) {
					s += (ok?";":"") + reg.getKey();
					ok = true;
				}
				return s;
			}
		}
	}
	
	public static class PrintListCounts implements PrintList { 
		public String print(LinkedList<GeneRegistry> list) {
			if(list == null)
				return "0";
			return "" + list.size();
		}
	}
	
	public static class PrintListBinary implements PrintList {
		public String print(LinkedList<GeneRegistry> list) {
			if(list == null)
				return "0";
			return "1";
		}
	}

	/*private void exec(PrintStream stream, Rengine re, String s) {
		if(stream != null) stream.println(s);
		re.eval(s);
	}
	
	public PCAMatrix getPCA(PrintStream stream, Rengine re) {
		if (!re.waitForR())
		{
			System.out.println ("Cannot load R");
			return null;
		}
		PrintListBinary p = new PrintListBinary();
		
		exec(stream, re, "m <- matrix(0, ncol = " + orgs.length + ", nrow = " + labels.length + ")");
		exec(stream, re, "m <- data.frame(m)");
		
		String batch = "";
		int v = 0;
		String s = "";
		for (int i = 0; i < labels.length; i++) {
			//String s = p.print(values[i][0]);
			for (int j = 0; j < orgs.length; j++) {
				s += "," + p.print(values[i][j]);
				//s += "," + p.print(values.get(i).get(j));
			}
			//s = "m[" + (i + 1) + ",]=c(" + s + ")";
			batch += s;
			if(i%10 == 0) {
				batch = batch.replaceAll(",0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0", ",rep(0,20)");
				batch = batch.replaceAll(",0,0,0,0,0,0,0,0,0,0", ",rep(0,10)");
				batch = batch.replaceAll(",0,0,0,0,0", ",rep(0,5)");
				batch = batch.replaceAll(",1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1", ",rep(1,20)");
				batch = batch.replaceAll(",1,1,1,1,1,1,1,1,1,1", ",rep(1,10)");
				batch = batch.replaceAll(",1,1,1,1,1", ",rep(1,5)");
				batch = "m[" + (v+1) + ":" + (i+1) + ",]=t(matrix(c(" + batch.substring(1) + "), nrow=" + orgs.length + "))";
				exec(stream, re, batch);
				batch = "";
				v = i + 1;
				batch = "";
				System.out.println("# " + i);
			}
		}
		//if(!batch.equals(""))
		//	exec(print, re, batch.substring(1));
		
		exec(stream, re, "pca = prcomp(m)");
		double[][] result = new double[orgs.length][orgs.length];
		for (int i = 0; i < orgs.length; i++) {
			result[i] = re.eval("pca$rotation[," + (i+1) + "]").asDoubleArray();
		}
		double[] var = re.eval("pca$sdev").asDoubleArray();
		
		PCAMatrix pca = new PCAMatrix(orgs, labels.length, result, var);
		return pca;
	}*/	

	public void export() {
		export(new PrintListBinary(), System.out);
	}
	
	public void export(PrintList p, PrintStream stream) {
		stream.print("\"GENES");
		for (int i = 0; i < orgs.length; i++) {
			stream.print("\",\"" + orgs[i].getAbbrev());
		}
		stream.println("\"");
		
		for (int i = 0; i < labels.length; i++) {
			stream.print("\"" + labels[i].replace(",", "") + "\"");
			for (int j = 0; j < orgs.length; j++) {
				stream.print("," + p.print(values[i][j]));
			}
			stream.println();
		}
	}
	
	public void exportPhylip(PrintList p, PrintStream stream) {
		exportPhylip(p, stream, null);
	}
	
	public void exportPhylip(PrintList p, PrintStream stream, Map<OrganismRegistry, String> mapNames) {
		stream.println(orgs.length + " " + labels.length); 
		String blank = "             ";
		for (int k = 0; k < orgs.length; k++) {
			String name = (orgs[k].getAbbrev() + blank).substring(0, 10);
			if(mapNames != null)
				name = mapNames.get(orgs[k]);
			while(name.length() < 10) {
				name += " ";
			}
			stream.print(name);
			for (int i = 0; i < labels.length; i++) {
				stream.print(p.print(values[i][k]));
			}
			stream.println();
		}
	}
}