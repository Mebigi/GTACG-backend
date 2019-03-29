package Structure.Graph;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSinkImages.Resolution;

import Structure.GeneFamily;
import Structure.UnionFind;
import Structure.Registry.Dictionary;
import Structure.Registry.GeneRegistry;
import Structure.Registry.RegistryGroups;
import Structure.Restriction.Attribute;
import Structure.Restriction.DistanceAttribute;
import Structure.Restriction.M8Attribute;
import Structure.Registry.GeneRegistryRandom;
import Structure.Registry.OrganismRegistry;

public class GraphGenes <KEY, ATTRIBUTE extends Attribute<ATTRIBUTE>, NODE extends NodeGene<KEY, ATTRIBUTE>> extends Graph<KEY, ATTRIBUTE, NODE> {
	public GraphGenes(boolean directed) {
		super(directed);
	}
	
	@Override
	public GraphGenes<KEY, ATTRIBUTE, NODE> newGraph() {
		return new GraphGenes<KEY, ATTRIBUTE, NODE>(isDirected());
	}
	
	public void exportEdgesFile(PrintStream stream) {
		for (NodeGene<KEY, ATTRIBUTE> node : getNodes()) {
			for (EdgeAttribute<ATTRIBUTE> edge : node.getEdges()) {
				if(node.getId() < edge.getA().getId() || node.getId() < edge.getB().getId())
					stream.println(edge.getA().getKey() + "\t" + edge.getB().getKey() + "\t1");
			}
		}
	}

	public void exportIndicatorsFile(PrintStream stream, Dictionary dic) throws IOException {
		OrganismRegistry [] orgs = dic.getOrganisms();
		
		stream.print("gene");
		for (int i = 0; i < orgs.length; i++) {
			stream.print(orgs[i].getHeader().replaceAll(">" + orgs[i].getKey() + " ", ""));
		}
		stream.println();
		
		LinkedList<NODE> heads = connComponentHeads();
		while(!heads.isEmpty()) {
			Graph<KEY, ATTRIBUTE, NODE> component = connComponentGraph(heads.poll());
			GeneFamily family = new GeneFamily(component.getNodes());
			LinkedList<GeneRegistry>[] genes = family.getArrayGenes(orgs);
			stream.print("aaa");
			for (int i = 0; i < orgs.length; i++) {
				String join = "";
				for (LinkedList<GeneRegistry> gene : genes) {
					join = join + gene + ";";
				}
				stream.print("\t" + join.substring(0, join.length()-1));
			}
			stream.println();
		}
		stream.close();
	}
	
	/*private void exec(boolean print, Rengine re, String s) {
		if(print) System.out.println(s);
		re.eval(s);
	}*/
	
	/*public void vennDiagram(boolean print, Rengine re, Dictionary dic, RegistryGroups grupos, File saidaTIFF, boolean exclusivos, boolean grupoCompleto, int minSize) {
		LinkedList<NODE> heads = connComponentHeads();
		
		String sGrupos = "";
		String sGrupos2 = "";
		for (RegistryGroup grupo : grupos.getGroups()) {
			exec(true, re, "Grupo" + grupo.getId() + " = c()");
			sGrupos = sGrupos + ",'" + grupo.getName() + "' = Grupo" + grupo.getId();
			sGrupos2 = sGrupos2 + ",'" + grupo.getName() + "'";
		}
		
		THashMap<String, LinkedList<NODE>> map = SetOperation.mapFamilies(this, heads, grupos);
		int x = 0;
		for (Entry<String, LinkedList<NODE>> ent : map.entrySet()) {
			int x2 = x + ent.getValue().size();
			String s = x + ":" + x2;
			for (int i = 0; i < ent.getKey().length(); i++) {
				if(ent.getKey().charAt(i) == '1') {
					exec(true, re, "Grupo" + i + "=c(Grupo" + i + "," + s + ")");
				}
			}
			x = x2 + 1;
		}
		

		exec(true, re, "library(VennDiagram)");
		exec(true, re, "library(gridExtra)");
		exec(true, re, "cores=c('cornflowerblue', 'green', 'yellow', 'darkorchid1', 'white')");
		exec(true, re, "nomes=c(" + sGrupos2.substring(1) + ")");
		exec(true, re, "lg <- legendGrob(labels=nomes, pch=rep(19,length(nomes)),gp=gpar(col=cores, fill='gray'),byrow=TRUE)");

		

		exec(true, re, "t = venn.plot <- venn.diagram(" +
	    		"x = list(" + sGrupos.substring(1) + ")," +
	    		"filename = NULL," +
	    		"col = \"black\"," +
	    		"lty = \"solid\"," +
	    		"lwd = 4," +
	    		"fill = cores[1:" + grupos.size() + "]," +
	    		"alpha = 0.50," +
	    		"cex = 1.5," +
	    		"fontfamily = \"serif\"," +
	    		"fontface = \"bold\"," + 
	    		"cat.cex = 1.5," +
	    		"cat.fontfamily = \"serif\"" +
	    		");");
		exec(true, re, "g <- gTree(children = gList(t))");

		exec(true, re, "pdf('" + saidaTIFF.getAbsolutePath() + "',width=12,heigh=10)");
		exec(true, re, "gridExtra::grid.arrange(g, lg, ncol = 2, widths = c(4,1))");
		exec(true, re, "dev.off()");
	}*/
	
	public SingleGraph plot(String name, File outJpg, boolean showDisplay, boolean colorOrg) throws InterruptedException, IOException {
		return plot(name, outJpg, showDisplay, null, colorOrg);
	}
	
	public SingleGraph plot(String name, File outJpg, boolean showDisplay, Resolution res, boolean colorOrg) throws InterruptedException, IOException {
		if(colorOrg) {
			HashMap<NODE, Color> colors = new HashMap<>();
			for (NODE node : getNodes()) {
				colors.put(node, node.getGene().getOrganism().getRoot().getColor());
			}
			return plot(name, outJpg, showDisplay, res, colors);
		}
		else 
			return plot(name, outJpg, showDisplay, res);
	}
	
	/*public HashSet<NODE> getMultiDomainSeqs(double difNeighbours) {
		HashSet<NODE> result = new HashSet<>();
		for (NODE node : getNodes()) {
			double total = 0;
			int cont = 0;
			for (EdgeAttribute<ATTRIBUTE> edge : node.getEdges()) {
				NODE neighbour = edge.diff(node);
				double coefn = clusterCoef(neighbour);
				if(coefn != 0 || neighbour.getEdges().size() > 1) {
					cont++;
					total += coefn;
				}
			}
			
			double media = total/cont;
			double coef = clusterCoef(node);
			if(coef < 1 && node.getEdges().size() > 1 && coef <= media - difNeighbours)
				result.add(node);
		}
		return result;
	}*/
	
	public HashSet<NODE> getMultiDomainSeqs(double difNeighbours, int numBatchs, int numThreads) {
		ConcurrentHashMap<KEY, Double> hashCoef = new ConcurrentHashMap<>();
		ConcurrentLinkedQueue<LinkedList<NODE>> batch = new ConcurrentLinkedQueue<>(makeBatchs(numBatchs, 1, getNodes()));
		
		ExecutorService executor = Executors.newFixedThreadPool(numThreads);
		for (int i = 0; i < numThreads; i++) {
			executor.submit(new Runnable() {
				@Override
				public void run() {
					LinkedList<NODE> list = batch.poll();
					while (list != null) {
						for (NODE node : list) {
							hashCoef.put(node.getKey(), clusterCoef(node));
						}
						list = batch.poll();
					}
					
				}
			});
		}
		executor.shutdown();
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		executor = Executors.newFixedThreadPool(numThreads);
		ConcurrentLinkedQueue<NODE> result = new ConcurrentLinkedQueue<>();
		for (NODE node : getNodes()) {
			executor.submit(new Runnable() {
				@Override
				public void run() {
					
					double total = 0;
					int cont = 0;
					for (EdgeAttribute<ATTRIBUTE> edge : node.getEdges()) {
						NODE neighbour = edge.diff(node);
						double coefn = hashCoef.get(neighbour.getKey());
						if(coefn != 0 || neighbour.getEdges().size() > 1) {
							cont++;
							total += coefn;
						}
					}
					
					double media = total/cont;
					double coef = hashCoef.get(node.getKey());
					if(coef < 1 && node.getEdges().size() > 1 && coef <= media - difNeighbours)
						result.add(node);
				}
			});
		}
		
		executor.shutdown();
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//HashSet<NODE> result = new HashSet<>();
		return new HashSet<NODE>(result);
	}
	
	
	public LinkedList<LinkedList<NODE>> getSingleDomains(HashSet<NODE> multidomais) {
		LinkedList<NODE> single = new LinkedList<>();
		for (NODE node : getNodes()) {
			if(!multidomais.contains(node))
				single.add(node);
		}
		return divide(single);
	}
	
	/*public LinkedList<LinkedList<NODE>> getDomains() {
		return getDomains(getMultiDomainSeqs(0));
	}
	
	public LinkedList<LinkedList<NODE>> getDomains(HashSet<NODE> multidomais) {
		LinkedList<LinkedList<NODE>> singles = getSingleDomains(multidomais);
		
		LinkedList<LinkedList<NODE>> visitar = new LinkedList<>(singles);
		System.out.println(singles.size());
		
		HashSet<NODE> visitados = new HashSet<>();
		LinkedList<LinkedList<NODE>> dominios = new LinkedList<>();
		while(!visitar.isEmpty()) {
			LinkedList<NODE> list = visitar.poll();			
			HashSet<NODE> next = new HashSet<>();
			visitados.addAll(list);
			for (NODE node : list) {
				for (EdgeAttribute<ATTRIBUTE> edge : node.getEdges()) {
					if(!visitados.contains(edge.dif(node))) {
						next.add(edge.dif(node));
					}
				}
			}
			
			if(!next.isEmpty()) {
				LinkedList<NODE> dominio = new LinkedList<>();
				dominio.addAll(list);
				dominio.addAll(next);
				dominios.add(dominio);
				visitar.addAll(divide(new LinkedList<>(next)));
			}
		}
		return dominios;
	}*/
	
	public static Graph<Integer, DistanceAttribute, Structure.Graph.Node<Integer, DistanceAttribute>> domainsToGraph(LinkedList<LinkedList<NodeGene<String, M8Attribute>>> dominios) {
		Graph<Integer, DistanceAttribute, Structure.Graph.Node<Integer, DistanceAttribute>> gdom = new Graph<>(false);
        for (int i = 0; i < dominios.size(); i++) {
            gdom.addNode(new Structure.Graph.Node<Integer, DistanceAttribute>(i));
        }
         
        for (int j = 0; j < dominios.size(); j++) {
            for (int j2 = j+1; j2 < dominios.size(); j2++) {
                HashSet<NodeGene<String, M8Attribute>> set1 = new HashSet<>(dominios.get(j));
                HashSet<NodeGene<String, M8Attribute>> set2 = new HashSet<>(dominios.get(j2));
                set1.retainAll(set2);
                if(!set1.isEmpty())
                    gdom.addEdge(gdom.getNode(j), gdom.getNode(j2));
            }
        }
		
		return gdom;
	}
	
	private static <KEY, ATTRIBUTE extends Attribute<ATTRIBUTE>, NODE extends NodeGene<KEY, ATTRIBUTE>> LinkedList<LinkedList<NODE>> divide(LinkedList<NODE> seqs) {
		HashMap<NODE, Integer> map = new HashMap<>();
		for (int i = 0; i < seqs.size(); i++) {
			map.put(seqs.get(i), i);
		}
		
		UnionFind uf = new UnionFind(seqs.size());
		for (int i = 0; i < seqs.size(); i++) {
			NODE seqi = seqs.get(i);
			for (EdgeAttribute<ATTRIBUTE> edge : seqi.getEdges()) {
				NODE seqj = edge.diff(seqi);
				if(map.containsKey(seqj)) {
					Integer j = map.get(seqj);
					uf.union(i, j);
				}
			}
		}
		
		HashMap<Integer, LinkedList<NODE>> groups = new HashMap<>();
		for (int i = 0; i < seqs.size(); i++) {
			int x = uf.find(i);
			LinkedList<NODE> novo = new LinkedList<>();
			if(groups.containsKey(x)) {
				novo = groups.get(x);
			}
			novo.add(seqs.get(i));
			groups.put(x, novo);
		}
		return new LinkedList<>(groups.values());
	}
			
    public LinkedList<LinkedList<NODE>> getDomains() {
    	TreeSet<String> set = new TreeSet<>();
    	for (NODE node : getNodes()) {
			set.add(printPath(node));
		}
    	
    	LinkedList<LinkedList<NODE>> result = new LinkedList<>();
    	for (String string : set) {
			String[] vet = string.split(",");
			LinkedList<NODE> list = new LinkedList<>();
			for (String key : vet) {
				list.add(getNode((KEY)key));
			}
			result.add(list);
		}

    	return result;
    }
    
	private String printPath(NODE head) {
		LinkedList<NODE> lista = new LinkedList<>();
		lista.add(head);
		
		HashSet<NODE> hash = new HashSet<>();
		while(!lista.isEmpty()) {
			NODE node = lista.poll();
			if(!hash.contains(node)) {
				hash.add(node);
				
				for (EdgeAttribute<ATTRIBUTE> edge : node.getEdges()) {
					lista.add(edge.diff(node));
				}
			}
		}
		
		return listNodeToString(new LinkedList<>(hash));
	}
	
	public static <KEY, ATTRIBUTE extends Attribute<ATTRIBUTE>, NODE extends NodeGene<KEY, ATTRIBUTE>> String listNodeToString(LinkedList<NODE> list) {
    	LinkedList<String> strings = new LinkedList<>();
    	for (NODE node : list) {
			strings.add((String)node.getKey());
		}
    	Collections.sort(strings);
    	
    	String result = "";
    	for (String string : strings) {
			result = result + "," + string;
		}
    	return result.substring(1);
    }
	
	public SingleGraph plot(String name, File inJpg, boolean showDisplay, Resolution res, RegistryGroups rg) throws InterruptedException, IOException {
		HashMap<NODE, Color> colors = new HashMap<>();
		for (NODE node : getNodes()) {
			colors.put(node, rg.getGroup(node.getGene().getOrganism().getRoot()).getColor());
		}
		
		return plot(name, inJpg, showDisplay, res, colors);
	}
}