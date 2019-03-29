package Structure.Matrix;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.LinkedList;

import Structure.Restriction.Attribute;
import Structure.Restriction.DistanceAttribute;
import mdsj.MDSJ;
import Structure.GeneFamily;
import Structure.Graph.Graph;
import Structure.Graph.GraphGenes;
import Structure.Graph.Node;
import Structure.Graph.NodeGene;
import Structure.Graph.Graphics.Point;
import Structure.Registry.OrganismRegistry;


public class DistanceMatrix {
	public enum Distance {
		Euclidean,
		Manhattan
	}
	
	DecimalFormat df = new DecimalFormat("0.000"); 
	public String label [];
	public double dist [][];
	public DistanceMatrix(int n) {
		label = new String[n];
		dist = new double[n][n];
	}
	
	public <KEY_STATIC, ATTRIBUTE extends Attribute<ATTRIBUTE>, NO extends NodeGene<KEY_STATIC, ATTRIBUTE>, GRAFO extends GraphGenes<KEY_STATIC, ATTRIBUTE, NO>> DistanceMatrix(GRAFO graph, OrganismRegistry[] orgs) {
		this(graph, orgs, Distance.Euclidean, true);
	}
	
	public <KEY_STATIC, ATTRIBUTE extends Attribute<ATTRIBUTE>, NO extends NodeGene<KEY_STATIC, ATTRIBUTE>, GRAFO extends GraphGenes<KEY_STATIC, ATTRIBUTE, NO>> DistanceMatrix(GRAFO graph, OrganismRegistry[] orgs, Distance distance, boolean binary) {
		this(graph, graph.connComponentHeads(), orgs, distance, binary);
	}
	
	public <KEY_STATIC, ATTRIBUTE extends Attribute<ATTRIBUTE>, NO extends NodeGene<KEY_STATIC, ATTRIBUTE>, GRAFO extends GraphGenes<KEY_STATIC, ATTRIBUTE, NO>> DistanceMatrix(GRAFO graph, LinkedList<NO> heads, OrganismRegistry[] orgs, Distance distance, boolean binary) {
		this(orgs.length);
		for (int i = 0; i < orgs.length; i++) {
			label[i] = orgs[i].getAbbrev();
		}
		for (NO head : heads) {
			LinkedList<NO> component = graph.connComponentList(head);
			GeneFamily family = new GeneFamily(component);
			int[] result = family.getArrayGenesCount(orgs);
			for (int i = 0; i < result.length; i++) {
				for (int j = i + 1; j < result.length; j++) {
					if(result[i] != result[j]) {
						if(binary) {
							dist[i][j]++;
							dist[j][i]++;
						}
						else {
							dist[i][j] += Math.abs(result[i] - result[j]);
							dist[j][i] += Math.abs(result[i] - result[j]);
						}
					}
				}
			}
		}
		
		if(distance == Distance.Euclidean) {
			for (int i = 0; i < dist.length; i++) {
				for (int j = i + 1; j < dist.length; j++) {
					dist[i][j] = Math.sqrt(dist[i][j]);
					dist[j][i] = dist[i][j];
				}
			}
		}
	}

	
	public double max() {
		double max = 0;
		for (int i = 0; i < dist.length; i++) {
			for (int j = 0; j < dist.length; j++) {
				if (dist[i][j] > max)
					max = dist[i][j];
			}
		}
		return max;
	}
	
	public double min() {
		double min = dist[0][0];
		for (int i = 0; i < dist.length; i++) {
			for (int j = 0; j < dist.length; j++) {
				if (dist[i][j] < min)
					min = dist[i][j];
			}
		}
		return min;
	}
	
	public void normalize() {
		double max = max();
		for (int i = 0; i < dist.length; i++)
			for (int j = 0; j < dist.length; j++)
				dist[i][j] = dist[i][j]/max;
	}
	
	public void normalizeMinMax() {
		double max = max();
		double min = min();
		double base = max-min;
		for (int i = 0; i < dist.length; i++)
			for (int j = 0; j < dist.length; j++)
				dist[i][j] = (dist[i][j]-min)/base;
	}
	
	public void invert() {
		for (int i = 0; i < dist.length; i++) {
			for (int j = 0; j < dist.length; j++) {
				dist[i][j] = 1 - dist[i][j];
				if(i == j)
					dist[i][j] = 0;
			}
		}
	}
	
	public void printMatrix(PrintStream stream) {
		for (int i = 0; i < dist.length; i++) {
			stream.print(label[i]);
			for (int j = 0; j < dist.length; j++) {
				stream.print("\t" + df.format(dist[i][j]).replace(',', '.'));
			}
			stream.println();
		}
	}
	
	public void printMatriz() {
		printMatrix(System.out);
	}
	
	public void printMatrixPhylip(PrintStream stream) {
		stream.println(label.length);
		for (int i = 0; i < dist.length; i++) {
			String s = label[i];
			if(s.length() > 10)
				s = s.substring(0, 10);
			else if(s.length() < 10)
				s = s + new String(new byte[Math.max(10 - s.length(), 0)]).replace("\0", " ");
			stream.print(s);
			for (int j = 0; j < dist.length; j++) {
				stream.print("\t" + df.format(dist[i][j]).replace(',', '.'));
			}
			stream.println();
		}
	}
	
	public void printMatrixPhylip() {
		printMatrixPhylip(System.out);
	}
	
	public Graph<String, DistanceAttribute, Node<String, DistanceAttribute>> makeGraph(double threshold, boolean invert) {
		Graph<String, DistanceAttribute, Node<String, DistanceAttribute>> grafo = new Graph<>(false);
		for (int i = 0; i < label.length; i++) {
			grafo.addNode(new Node<String, DistanceAttribute>(label[i]));
		}
		
		for (int i = 0; i < label.length; i++) {
			for (int j = i + 1; j < label.length; j++) {
				if(!invert) {
					if(dist[i][j] >= threshold)
						grafo.addEdge(grafo.getNode(label[i]), grafo.getNode(label[j]));
				}
				else if(dist[i][j] <= threshold)
					grafo.addEdge(grafo.getNode(label[i]), grafo.getNode(label[j]));
			}
		}
		return grafo;
	}
	
	public static String makeDendogram(LinkedList<LinkedList<String>> [] hist, int k, LinkedList<String> group, int size) {
		if(k >= hist.length) {
			String result = "";
			for (Object object : group) {
				result = result + object + ":" + size + ",";
			}
			result = result.substring(0, result.length()-1);
			return result + ",";
		}
		
        HashSet<LinkedList<String>> accept = new HashSet<LinkedList<String>>();
		for (Object obj : group) {
			for (LinkedList<String> grupoVerif : hist[k]) {
				for (String objVerif : grupoVerif) {
					if((obj).equals(objVerif)) {
						accept.add(grupoVerif);
					}
				}
			}
		}
		
		if(accept.size() == 1)
			return makeDendogram(hist, k + 1, group, size + 1);
		String result = "(";
		for (LinkedList<String> linkedList : accept) {
			String seg = makeDendogram(hist, k + 1, linkedList, 1);
			result = result + seg.substring(0, seg.length() - 1) + ",";
		}
		result = result.substring(0, result.length()-1) + ")" + (k-size+1) + ".." + k + ":" + size;
		return result + ";";
	}
	
	public String makeDendogram(int start, int end) {
		@SuppressWarnings("unchecked")
		LinkedList<LinkedList<String>> [] hist = new LinkedList[end-start+1];  
		for (int i = start; i <= end; i++) {
			Graph<String, DistanceAttribute, Node<String, DistanceAttribute>> graph = makeGraph(i, false);
			LinkedList<Graph<String, DistanceAttribute, Node<String, DistanceAttribute>>> componentes = graph.connComponentGraph(); 
			LinkedList<LinkedList<String>> lista = new LinkedList<LinkedList<String>>();
			for (Graph<String, DistanceAttribute, Node<String, DistanceAttribute>> linkedList : componentes) {
				LinkedList<String> lista2 = new LinkedList<String>();
				for (Node<String, DistanceAttribute> no : linkedList.getNodes()) {
					lista2.add(no.getKey());
				}
				lista.add(lista2);
			}
			hist[i - start] = lista;
		}
		LinkedList<String> inicioGrupo = new LinkedList<String>();
		for (LinkedList<String> list : hist[0]) {
			for (String object : list) {
				inicioGrupo.add(object);
			}
		}
		String arvore = makeDendogram(hist, start, inicioGrupo, 1);
		return arvore;
	}
	
	public String export() {
		System.out.print("\"" + label[0] + "\"");
		for (int i = 1; i < label.length; i++) {
			System.out.print(", \"" + label[i] + "\"");
		}
		System.out.println();
		
		for (int i = 0; i < label.length; i++) {
			System.out.print(dist[i][0]);
			for (int j = 1; j < label.length; j++) {
				System.out.print("," + dist[i][j]);
			}
			System.out.println();
		}		
		return "";
	}
	
	/*private static void exec(PrintStream out, Rengine re, String s) {
		if(out != null) out.println(s);
		re.eval(s);
	}
	
	public void multiScallingR(PrintStream out, Rengine re, OrganismRegistry orgs[], RegistryGroups groups, boolean autoColor) {		
		re.eval("m <- matrix(0, nrow = " + orgs.length + ", ncol = " + orgs.length + ")");
		re.eval("m <- data.frame(m)");
		String names = "";
		for (int i = 0; i < orgs.length; i++) {
			names += ",\"" + orgs[i].getAbbrev() + "\"";
			for (int j = 0; j < orgs.length; j++) {
				re.eval("m[" + (i+1) + ", " + (j+1) + "] = " + dist[i][j]);
			}
		}
		re.eval("names = c(" + names.substring(1) + ")");
		re.eval("colnames(m) <- names");
		re.eval("row.names(m) <- names");
		re.eval("fit <- cmdscale(m, eig = TRUE, k = 2)");
		
		double x[] = re.eval("fit$points[,1]").asDoubleArray();
		double y[] = re.eval("fit$points[,2]").asDoubleArray();

		exec(out, re, "m <- matrix(0, nrow = " + orgs.length + ", ncol = 2)");
		exec(out, re, "m <- data.frame(m)");

		String corOrg = "";
		String corGrupo = "";
		String nomeGrupo = "";
		for (int i = 0; i < orgs.length; i++) {
			corOrg += ",\"#" + Integer.toHexString(orgs[i].getColor().getRGB()).substring(2) + "\"";
			corGrupo += ",\"" + (groups==null?"null":"#"+Integer.toHexString(groups.getGroup(orgs[i]).getColor().getRGB()).substring(2)) + "\"";
			nomeGrupo += ",\"" + (groups==null?"null":groups.getGroup(orgs[i]).getName()) + "\"";
			exec(out, re, "m[" + (i+1) + ",] <- c(" + x[i] + ", " + y[i] + ")");
		}
		
		exec(out, re, "colnames(m) <- c(\"x\", \"y\")");
		exec(out, re, "row.names(m) = c(" + names.substring(1) + ")");
		exec(out, re, "colorOrg = c(" + corOrg.substring(1) + ")");
		exec(out, re, "colorGroup = c(" + corGrupo.substring(1) + ")");
		exec(out, re, "nameGroup = c(" + nomeGrupo.substring(1) + ")");
		exec(out, re, "names = c(" + names.substring(1) + ")");
		exec(out, re, "library(ggplot2)");
		exec(out, re, "library(ggrepel)");
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
		exec(out, re, "pdf(file=\"qqplot.pdf\")");
		exec(out, re, "ggplot(m) + geom_point(aes(x, y), color = 'red') + geom_label_repel(aes(x, y, label = rownames(m)), fill = " + opColor + ", fontface = 'bold', color = 'black', box.padding = unit(0.35, \"lines\"), point.padding = unit(0.5, \"lines\"),  segment.color = 'grey50')");
		exec(out, re, "dev.off()");
	}
	
	public Point[] multiScalling(PrintStream out, Rengine re, OrganismRegistry orgs[], RegistryGroups groups, boolean autoColor) {		
		re.eval("m <- matrix(0, nrow = " + orgs.length + ", ncol = " + orgs.length + ")");
		re.eval("m <- data.frame(m)");
		String names = "";
		for (int i = 0; i < orgs.length; i++) {
			names += ",\"" + orgs[i].getAbbrev() + "\"";
			for (int j = 0; j < orgs.length; j++) {
				re.eval("m[" + (i+1) + ", " + (j+1) + "] = " + dist[i][j]);
			}
		}
		re.eval("names = c(" + names.substring(1) + ")");
		re.eval("colnames(m) <- names");
		re.eval("row.names(m) <- names");
		re.eval("fit <- cmdscale(m, eig = TRUE, k = 2)");
		
		double x[] = re.eval("fit$points[,1]").asDoubleArray();
		double y[] = re.eval("fit$points[,2]").asDoubleArray();
		
		Point points[] = new Point[orgs.length];
		for (int i = 0; i < points.length; i++) {
			points[i] = new Point(x[i], y[i]);
		}
		return points;
	}*/
	
	public Point[] multiScalling() {
		double[][] result = MDSJ.classicalScaling(dist);
		//double[][] result = MDSJ.stressMinimization(dist);
		Point points[] = new Point[label.length];
		for (int i = 0; i < points.length; i++) {
			points[i] = new Point(result[0][i], result[1][i]);
		}
		return points;
	}
}






