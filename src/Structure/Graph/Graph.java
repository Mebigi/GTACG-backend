package Structure.Graph;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.*;
import org.graphstream.stream.file.FileSinkImages.LayoutPolicy;
import org.graphstream.stream.file.FileSinkImages.OutputType;
import org.graphstream.stream.file.FileSinkImages.Quality;
import org.graphstream.stream.file.FileSinkImages.RendererType;
import org.graphstream.stream.file.FileSinkImages.Resolution;
import org.graphstream.stream.file.FileSinkImages.Resolutions;

import Structure.Constants;
import Structure.PairLong;
import Structure.Graph.Graphics.Point;
import Structure.Restriction.Attribute;
import Structure.Restriction.Restriction;

public class Graph <KEY, ATTRIBUTE extends Attribute<ATTRIBUTE>, NODE extends Node<KEY, ATTRIBUTE>> {
	private boolean directed = false;
	private LinkedList<NODE> listNodes = new LinkedList<NODE>();
	//private THashMap<KEY, NODE> hashNodes = new THashMap<KEY, NODE>(200000, (float)0.75);
	private THashMap<KEY, NODE> hashNodes = new THashMap<KEY, NODE>();
	
	public Graph(boolean directed) {
		this.directed = directed;
	}
	
	public boolean isDirected() {
		return directed;
	}
	
	public boolean isDirected(boolean force) {
		if(force)
			return isDirected();
		
		boolean realDirected = false;
        for (NODE node : getNodes()) {
			for (EdgeAttribute<ATTRIBUTE> edge : node.getEdges()) {
				if(edge.diff(node).getEdge(node.getKey()) == null)
					realDirected = true;
			}
		}
        return realDirected;
	}
	
	public LinkedList<NODE> getNodes() {
		return listNodes;
	}
	
	public NODE getNode(KEY key) {
		return hashNodes.get(key);
	}
	
	public NODE addNode(NODE no) {
		listNodes.add(no);
		hashNodes.put(no.getKey(), no);
		return no;
	}
	
	public void removeNode(KEY key) {
		listNodes.remove(hashNodes.get(key));
		hashNodes.remove(key);
	}
	
	public EdgeAttribute<ATTRIBUTE> addEdge(NODE noA, NODE noB) {
		EdgeAttribute<ATTRIBUTE> edge = noA.getEdge(noB.getKey());
		if(edge != null)
			return edge;

		edge = new EdgeAttribute<>(noA, noB);
		
		noA.setEdge(noB.getKey(), edge);
		if(!directed) 
			noB.setEdge(noA.getKey(), edge);
		return edge;
	}
	
	public void removeEdges(KEY key) {
		NODE node = getNode(key);
		LinkedList<EdgeAttribute<ATTRIBUTE>> edges = new LinkedList<>(node.getEdges());
		for (EdgeAttribute<ATTRIBUTE> edge : edges) {
			node.remove(edge.diff(node).getKey());
			if(!directed)
				edge.diff(node).remove(node.getKey());
		}
	}
	
	public Graph<KEY, ATTRIBUTE, NODE> newGraph() {
		return new Graph<KEY, ATTRIBUTE, NODE>(directed);
	}
	
	public void join(Graph<KEY, ATTRIBUTE, NODE> grafo) {
		listNodes.addAll(grafo.listNodes);
		hashNodes.putAll(grafo.hashNodes);
	}
	
	public boolean isFullConnected() {
		int size = getNodes().size() -1;
		for (NODE node : getNodes()) {
			if(node.getEdges().size() != size)
				return false;
		}
		return true;
	}
	
	public double avgClusterCoef() {
		return avgClusterCoef(null);
	}
	
	public double avgClusterCoef(Restriction<ATTRIBUTE> rest) {
		double total = 0;
		for (NODE node : listNodes) {
			PairLong pair = clusterCoef(node, rest);
			if(pair.a != 0)
				total += (double)pair.b/pair.a;
		}
		return total/listNodes.size();
	}
	
	public double clusterCoef() {
		return clusterCoef((Restriction<ATTRIBUTE>)null);
	}
	
	public double clusterCoef(Restriction<ATTRIBUTE> rest) {
		PairLong total = new PairLong(0, 0);
		for (NODE node : listNodes) {
			total.sum(clusterCoef(node, rest));
		}
		if(total.a == 0)
			return 0;
		return (double)total.b/total.a;
	}
	
	public double clusterCoef(NODE node) {
		PairLong pair = clusterCoef(node, null);
		if(pair.a == 0)
			return 0;
		return (double)pair.b/pair.a;
	}
	
	public PairLong clusterCoef(NODE node, Restriction<ATTRIBUTE> rest) {
		long triangles = 0;
		long connected = 0;

		EdgeAttribute<ATTRIBUTE> [] edges = node.getEdgesArray();
		for(int i = 0; i < edges.length -1; i++) {
			if(rest == null || rest.check(edges[i].getAttributes())) {
				NODE nodeA = edges[i].diff(node);
				for(int j = i + 1; j < edges.length; j++) {
					if(rest == null || rest.check(((EdgeAttribute<ATTRIBUTE>)edges[j]).getAttributes())) {
						NODE nodeB = edges[j].diff(node);
						
						EdgeAttribute<ATTRIBUTE> edgeAB = nodeA.getEdge(nodeB.getKey());
						triangles++;
						if(edgeAB != null && (rest == null || rest.check(((EdgeAttribute<ATTRIBUTE>)edgeAB).getAttributes())))
							connected++;
					}
				}
			}
		}
		return new PairLong(triangles, connected);
	}
	
	protected void clusterCoef(NODE node, Restriction<ATTRIBUTE> [] rests, PairLong [] result, boolean simplifiedByBatch) {
		EdgeAttribute<ATTRIBUTE> [] edges = node.getEdgesArray();
		for(int i = 0; i < edges.length -1; i++) {
			EdgeAttribute<ATTRIBUTE> edgeA = edges[i];
			NODE nodeA = edgeA.diff(node);
			ATTRIBUTE[] attrA = edgeA.getAttributes();
			for(int j = i + 1; j < edges.length; j++) {
				EdgeAttribute<ATTRIBUTE> edgeB = edges[j];
				NODE nodeB = edgeB.diff(node);
				ATTRIBUTE[] attrB = edgeB.getAttributes();
				
				EdgeAttribute<ATTRIBUTE> edgeAB = nodeA.getEdge(nodeB.getKey());
				int cont = 0;
				if(edgeAB == null) {
					for (Restriction<ATTRIBUTE> rest : rests) {
						if(rest.check(attrA) && rest.check(attrB)) {
							result[cont].a++;
						}
						cont++;
					}
				}
				else {
					if(simplifiedByBatch) {
						if(node.getId() < nodeA.getId() && node.getId() < nodeB.getId()) {
							ATTRIBUTE[] attrAB = edgeAB.getAttributes();
							for (Restriction<ATTRIBUTE> rest : rests) {
								if(rest.check(attrA) && rest.check(attrB)) {
									result[cont].a += 3;
									if(rest.check(attrAB))
										result[cont].b += 3;
									
								}
								cont++;
							}
						}
					}
					else {
						ATTRIBUTE[] attrAB = edgeAB.getAttributes();
						for (Restriction<ATTRIBUTE> rest : rests) {
							if(rest.check(attrA) && rest.check(attrB)) {
								result[cont].a++;
								if(rest.check(attrAB))
									result[cont].b++;
								
							}
							cont++;
						}
					}
				}
			}
		}
	}

	public static <KEY, ATTRIBUTE extends Attribute<ATTRIBUTE>, NODE extends Node<KEY, ATTRIBUTE>> LinkedList<NODE> listNodesByEdges(LinkedList<NODE> list, int numEdges) {
		LinkedList<NODE> result = new LinkedList<>();
		for (NODE node : list) {
			if(node.getEdges().size() >= numEdges)
				result.add(node);
		}
		return result;
	}
	
	public static <KEY, ATTRIBUTE extends Attribute<ATTRIBUTE>, NODE extends Node<KEY, ATTRIBUTE>> LinkedList<LinkedList<NODE>> makeBatchs(int numBatchs, int constant, LinkedList<NODE> nodes) {
		int[] totais = new int[numBatchs];
		PriorityQueue<Integer> queue = new PriorityQueue<>(new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return totais[o1] - totais[o2];
			}
		});
		
		HashMap<NODE, Integer> edgeSize = new HashMap<>();
		Collections.sort(nodes, new Comparator<NODE>() {
			@Override
			public int compare(NODE o1, NODE o2) {
				Integer a1 = edgeSize.get(o1);
				if(a1 == null) {
					a1 = o1.getEdges().size();
					edgeSize.put(o1, a1);
				}
				
				Integer a2 = edgeSize.get(o2);
				if(a2 == null) {
					a2 = o2.getEdges().size();
					edgeSize.put(o2, a2);
				}
				return a2 - a1;
			}
		});
		
		ArrayList<LinkedList<NODE>> batchs = new ArrayList<>(numBatchs);
		for (int i = 0; i < numBatchs; i++) {
			batchs.add(0, new LinkedList<>());
			queue.add(i);
		}
		
		for (NODE node : nodes) {
			Integer pos = queue.poll();
			batchs.get(pos).add(node);
			int edges = 0;
			if(edgeSize.get(node) != null)
				edges = edgeSize.get(node);
			totais[pos] += edges*(edges -1) + constant;
			queue.add(pos);
		}
		return new LinkedList<LinkedList<NODE>>(batchs);
	}
	
	public double[] clusterCoef(Restriction<ATTRIBUTE>[] rests, int threads) {
		return clusterCoef(rests, threads, false);
	}
	
	public double[] clusterCoef(Restriction<ATTRIBUTE>[] rests, int threads, int numBatchs) {
		return clusterCoef(rests, threads, false, numBatchs);
	}
	
	public double[] avgClusterCoef(Restriction<ATTRIBUTE>[] rests, int threads) {
		return clusterCoef(rests, threads, true);
	}
	
	public double[] avgClusterCoef(Restriction<ATTRIBUTE>[] rests, int threads, int numBatchs) {
		return clusterCoef(rests, threads, true, numBatchs);
	}
	
	protected double[] clusterCoef(Restriction<ATTRIBUTE>[] rests, int threads, boolean avg) {
		return clusterCoef(rests, threads, avg, threads);
	}

	protected double[] clusterCoef(Restriction<ATTRIBUTE>[] rests, int numThreads, boolean avg, int numBatchs) {
		LinkedList<NODE> validNodes = listNodesByEdges(getNodes(), 2);
		ConcurrentLinkedQueue<LinkedList<NODE>> batchs = new ConcurrentLinkedQueue<>(makeBatchs(numBatchs, rests.length, validNodes));
				
		System.err.println("# Start coeficiente cluster - " + Calendar.getInstance().getTime());
		System.err.println("# TOTAL\t" + getNodes().size());
		System.err.println("# IGNORADOS\t" + (listNodes.size() - validNodes.size()));
		System.err.println("# BATCHS " + batchs.size());
		
		double [] total = new double[rests.length];
		PairLong[] pairs = PairLong.makeArray(rests.length, new PairLong(0, 0));
		ExecutorService executor = Executors.newFixedThreadPool(numThreads);
		for (int threadId = 0; threadId < numThreads; threadId++) {
			executor.execute(() -> {
				int cont = 0;
				double[] localTotal = new double[total.length];
				PairLong[] localPairs = PairLong.makeArray(total.length, new PairLong(0, 0));
				LinkedList<NODE> listNodes = batchs.poll();
				while (listNodes != null) {
					for (NODE node : listNodes) {
						if(avg) {
							PairLong tmp[] = PairLong.makeArray(total.length, new PairLong(0, 0));
							clusterCoef(node, rests, tmp, false);
							for (int i = 0; i < total.length; i++)
								if(tmp[i].a != 0)
									localTotal[i] += (double)tmp[i].b/tmp[i].a;
						}
						else {
							for (NODE no : listNodes) {
					    		clusterCoef(no, rests, localPairs, true);
					    	}
						}
					}
					System.err.println("# " + Thread.currentThread().getId() + "\t" + cont++ + "\t" + Calendar.getInstance().getTime());
					listNodes = batchs.poll();
				}
				
				if(avg)
					synchronized (total) {
						for (int i = 0; i < total.length; i++)
							total[i] += localTotal[i];
					}
				else
					synchronized (pairs) {
						for (int i = 0; i < pairs.length; i++)
							pairs[i].sum(localPairs[i]);
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

		double [] result = new double[rests.length];
		if(avg)
			for (int i = 0; i < total.length; i++) {
				result[i] = (double)total[i]/getNodes().size();
			}
		else
			for (int i = 0; i < pairs.length; i++) {
				result[i] = (double)pairs[i].b/pairs[i].a;
			}
		
		return result;
	}
	
	public boolean updateRestrictions(Restriction<ATTRIBUTE> rest) {
        int mod = 0;
        
        LinkedList<EdgeAttribute<ATTRIBUTE>> delete = new LinkedList<EdgeAttribute<ATTRIBUTE>>();
        for (NODE node : listNodes) {
            for (EdgeAttribute<ATTRIBUTE> edge : node.getEdges()) {
                int sum = rest.sumChecks(edge.getAttributes());
                if(sum == 0) {
                    mod++;
                    edge.clearAttributes();
                    delete.add(edge);
                }
                else if(sum != edge.getAttributes().length) {
                    mod++;
                    ATTRIBUTE[] attrs = edge.getAttributes();
                    edge.clearAttributes();
                    for (ATTRIBUTE attr : attrs) {
                        if(rest.check(attr)) {
                            edge.addAttribute(attr);
                        }
                    }
                }
            }
        }
        for (EdgeAttribute<ATTRIBUTE> edge : delete) {
            NODE a = hashNodes.get(edge.getA().getKey());
            NODE b = hashNodes.get(edge.getB().getKey());
            a.remove(b.getKey());
            b.remove(a.getKey());
        }
        if(delete.isEmpty() && mod == 0)
            return false;
        return true;
	}
	
	public boolean updateRestrictions2(Restriction<ATTRIBUTE> rest) {
		int mod = 0;
		
		LinkedList<EdgeAttribute<ATTRIBUTE>> delete = new LinkedList<EdgeAttribute<ATTRIBUTE>>();
		for (NODE node : listNodes) {
			for (EdgeAttribute<ATTRIBUTE> edge : node.getEdges()) {
				int sum = rest.sumChecks(edge.getAttributes());
				if(sum == 0) {
					mod++;
					edge.clearAttributes();
					delete.add(edge);
				}
				else if(sum != edge.getAttributes().length) {
					mod++;
					ATTRIBUTE[] attrs = edge.getAttributes();
					edge.clearAttributes();
					for (ATTRIBUTE attr : attrs) {
						if(rest.check(attr)) {
							edge.addAttribute(attr);
						}
					}
				}
			}
		}
		for (EdgeAttribute<ATTRIBUTE> edge : delete) {
			NODE a = hashNodes.get(edge.getA().getKey());
			NODE b = hashNodes.get(edge.getB().getKey());
			a.remove(b.getKey());
			b.remove(a.getKey());
		}
		if(delete.isEmpty() && mod == 0)
			return false;
		System.gc();
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public Graph<KEY, ATTRIBUTE, NODE> subGraph(LinkedList<NODE> list) {
		Graph<KEY, ATTRIBUTE, NODE> sub = newGraph();
		for (NODE node : list) {
			sub.addNode((NODE)node.clone());
		}
		for (NODE nodeA : list) {
			NODE subNodeA = sub.getNode(nodeA.getKey());
			for (EdgeAttribute<ATTRIBUTE> edge : nodeA.getEdges()) {
				NODE nodeB = edge.diff(nodeA);
				NODE subNodeB = sub.getNode(nodeB.getKey());
				if(subNodeB != null)
					sub.addEdge(subNodeA, subNodeB).addAttributes(edge.getAttributes());
			}
		}
		return sub;
	}
			
	public LinkedList<Graph<KEY, ATTRIBUTE, NODE>> connComponentGraph() {
		return connComponentGraph((Restriction<ATTRIBUTE>)null);
	}
	
	protected LinkedList<Graph<KEY, ATTRIBUTE, NODE>> connComponentGraph(Restriction<ATTRIBUTE> rest) {
		LinkedList<Graph<KEY, ATTRIBUTE, NODE>> groups = new LinkedList<Graph<KEY, ATTRIBUTE, NODE>>();
		THashSet<NODE> visited = new THashSet<NODE>();
		for (NODE node : listNodes) {
			if(!visited.contains(node)) {
				Graph<KEY, ATTRIBUTE, NODE> group = newGraph();
				connComponentGraphRec(node, group, visited, rest);
				groups.add(group);
			}
		}
		return groups;
	}
	
	public Graph<KEY, ATTRIBUTE, NODE> connComponentGraph(NODE node) {
		return connComponentGraph(node, null);
	}
	
	protected Graph<KEY, ATTRIBUTE, NODE> connComponentGraph(NODE node, Restriction<ATTRIBUTE> rest) {
		THashSet<NODE> visited = new THashSet<NODE>();
		Graph<KEY, ATTRIBUTE, NODE> graph = newGraph();
		connComponentGraphRec(node, graph, visited, rest);
		return graph;
	}
	
	@SuppressWarnings("unchecked")
	protected void connComponentGraphRec(NODE node, Graph<KEY, ATTRIBUTE, NODE> group, THashSet<NODE> visited, Restriction<ATTRIBUTE> rest) {
		LinkedList<NODE> toVisit = new LinkedList<>();
		toVisit.add(node);
		while(!toVisit.isEmpty()) {
			NODE nodeA = toVisit.poll();
			NODE newNodeA = group.getNode(nodeA.getKey());
			if(newNodeA == null) {
				newNodeA = (NODE)nodeA.clone();
				group.addNode(newNodeA);
				
				for (EdgeAttribute<ATTRIBUTE> edge : nodeA.getEdges()) {
					if(rest == null || rest.check(edge.getAttributes())) {
						NODE nodeB = edge.diff(nodeA);
						NODE newNodeB = group.getNode(nodeB.getKey());
						if(newNodeB != null) {
							group.addEdge(newNodeA, newNodeB).copyValues(edge);
                            group.addEdge(newNodeB, newNodeA).copyValues(nodeB.getEdge(nodeA.getKey()));
						}
						toVisit.add(nodeB);
					}
				}
			}
		}
	}	
	
	public LinkedList<LinkedList<NODE>> connComponentList() {
		return connComponentList((Restriction<ATTRIBUTE>)null);
	}
	
	protected LinkedList<LinkedList<NODE>> connComponentList(Restriction<ATTRIBUTE> rest) {
		LinkedList<LinkedList<NODE>> groups = new LinkedList<LinkedList<NODE>>();
		HashSet<NODE> visited = new HashSet<NODE>();
		for (NODE node : listNodes) {
			if(!visited.contains(node)) {
				LinkedList<NODE> group = new LinkedList<NODE>();
				connComponentListRec(node, group, visited, rest);
				groups.add(group);
			}
		}
		return groups;
	}
	
	public LinkedList<NODE> connComponentList(NODE node) {
		return connComponentList(node, null);
	}
	
	protected LinkedList<NODE> connComponentList(NODE node, Restriction<ATTRIBUTE> rest) {
		HashSet<NODE> visited = new HashSet<NODE>();
		LinkedList<NODE> list = new LinkedList<NODE>();
		connComponentListRec(node, list, visited, rest);
		return list;
	}
	

	
	protected void connComponentListRec(NODE node, LinkedList<NODE> group, HashSet<NODE> visited, Restriction<ATTRIBUTE> rest) {
		LinkedList<NODE> toVisit = new LinkedList<>();
		toVisit.add(node);
		while(!toVisit.isEmpty()) {
			NODE nodeA = toVisit.poll();
			if(!visited.contains(nodeA)) {
				visited.add(nodeA);
				group.add(nodeA);
				for (EdgeAttribute<ATTRIBUTE> edge : nodeA.getEdges()) {
					if(rest == null || rest.check(edge.getAttributes())) {
						toVisit.add(edge.diff(nodeA));
					}
				}
			}
		}
	}

	public LinkedList<NODE> connComponentHeads() {
		return connComponentHeads(null);
	}
	
	protected LinkedList<NODE> connComponentHeads(Restriction<ATTRIBUTE> rest) {
		LinkedList<NODE> groups = new LinkedList<NODE>();
		HashSet<NODE> visited = new HashSet<NODE>();
		for (NODE node : listNodes) {
			if(!visited.contains(node)) {
				connComponentHeadRec(node, visited, rest);
				groups.add(node);
			}
		}
		return groups;
	}
	
	protected void connComponentHeadRec(NODE node, HashSet<NODE> visited, Restriction<ATTRIBUTE> rest) {
		LinkedList<NODE> toVisit = new LinkedList<>();
		toVisit.add(node);
		while(!toVisit.isEmpty()) {
			NODE nodeA = toVisit.poll();
			if(!visited.contains(nodeA)) {
				visited.add(nodeA);
				for (EdgeAttribute<ATTRIBUTE> edge : nodeA.getEdges()) {
					if(rest == null || rest.check(edge.getAttributes())) {
						toVisit.add(edge.diff(nodeA));
					}
				}
			}
		}
	}
	
	public SingleGraph plot(String name, File inJpg, boolean showDisplay) throws InterruptedException, IOException {
		return plot(name, inJpg, showDisplay, null, null);
	}
	
	public SingleGraph plot(String name, File inJpg, boolean showDisplay, Resolution res) throws InterruptedException, IOException {
		return plot(name, inJpg, showDisplay, res, null);
	}
	
	public SingleGraph plot(String name, File inJpg, boolean showDisplay, Resolution res, HashMap<NODE, Color> colors) throws InterruptedException, IOException {
		SingleGraph graph = new SingleGraph(name);
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		graph.addAttribute("ui.stylesheet", "node{shape: rounded-box;padding: 5px;fill-color: white;fill-mode: dyn-plain;stroke-mode: plain;size-mode: fit;} edge {shape: line; arrow-shape: arrow; fill-color: #222;}");
		graph.addAttribute("ui.quality");
		graph.addAttribute("ui.antialias");
		
		for (NODE node : listNodes) {
			graph.addNode("" + node.getId());
			graph.getNode("" + node.getId()).addAttribute("ui.label", node.getKey());
			if(colors != null && colors.containsKey(node))
				graph.getNode("" + node.getId()).addAttribute("ui.color", colors.get(node));
		} 

		for (NODE node : listNodes) {
			for (EdgeAttribute<ATTRIBUTE> edge : node.getEdges()) {
				if(node.getId() < edge.diff(node).getId() || directed) {
					try {
						boolean d = edge.diff(node).getEdge(node.getKey())==null;
						if(d == true || node.getId() < edge.diff(node).getId()) {
							graph.addEdge(node.getId() + "-" + edge.diff(node).getId(), "" + node.getId(), "" + edge.diff(node).getId(), d);
							graph.getEdge(node.getId() + "-" + edge.diff(node).getId()).addAttribute("ui.style", "fill-color: rgb(" + ((int)(Math.random()*246)+10) + "," + ((int)(Math.random()*246)+10) + "," + ((int)(Math.random()*246)+10) + ");");
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		if(inJpg != null) {
			if(res == null)
				res = Resolutions.HD720;
			FileSinkImages pic = new FileSinkImages(OutputType.JPG, res);
			
			pic.setQuality(Quality.HIGH);
			pic.setRenderer(RendererType.SCALA);
			pic.setStyleSheet("node{text-size: 16px; shape: rounded-box;padding: 5px;fill-color: white;stroke-mode: plain;size-mode: fit;} edge {shape: line; arrow-shape: arrow; arrow-size: 40px, 20px; fill-color: #222;}");
			pic.setLayoutPolicy(LayoutPolicy.COMPUTED_FULLY_AT_NEW_IMAGE);
			pic.writeAll(graph, inJpg.getAbsolutePath());
		}
		if(showDisplay)
			graph.display();
		return graph;
	}
	
	
	
	
	
	/*private static String listNodeToString(GraphGenes<String, M8Attribute, NodeGene<String, M8Attribute>> sub, NodeGene<String, M8Attribute> head) {
		LinkedList<NodeGene<String, M8Attribute>> lista = new LinkedList<>();
		lista.add(head);
		
		HashSet<NodeGene<String, M8Attribute>> hash = new HashSet<>();
		while(!lista.isEmpty()) {
			NodeGene<String, M8Attribute> node = lista.poll();
			if(!hash.contains(node)) {
				hash.add(node);
				
				for (EdgeAttribute<M8Attribute> edge : node.getEdges()) {
					lista.add(edge.dif(node));
				}
			}
		}
		
		return listString(new LinkedList<>(hash));
	}*/
	
	public void plotGraphviz(File outSvg, LinkedList<NODE> heads, HashMap<NODE, Color> colors) throws InterruptedException, IOException {
		HashMap<NODE, Integer> compSize = new HashMap<>();
		for (NODE node : heads) {
			compSize.put(node, connComponentList(node).size());
		}
		heads.sort(new Comparator<NODE>() {
			@Override
			public int compare(NODE o1, NODE o2) {
				return compSize.get(o2) - compSize.get(o1);
			}
		});
		
		Double raio = 0.0;
		int cont = 0;
		int largura = 1000;
		int altura = 1000;
		int util = (int) largura/2-10;
		double difAngulo = 0.01;
		double angulo = 0;
		
		
		HashMap<NODE, Point> pos = new HashMap<>();
		Random r = new Random();
		int i = 0;
		for (NODE head : heads) {
			LinkedList<NODE> comp = connComponentList(head);
			
			angulo += comp.size()*difAngulo/4;
			cont   += comp.size()/2;
			raio    = 1.0*cont*util/comp.size();
			
			for (NODE node : comp) {
				pos.put(node, new Point(
						Math.sin(angulo)*raio+largura/2+(r.nextDouble()*comp.size()/2-comp.size()/4),
						Math.cos(angulo)*raio+altura/2+(r.nextDouble()*comp.size()/2-comp.size()/4)
						));
				if (i==0){
					cont+=2.2*comp.size()/(3*10+3);
				}else if (i<10){
					cont+=comp.size()/(3*10+3);
				}
				cont+=1.2*comp.size()/2;
				angulo += comp.size()*difAngulo/4;


				angulo += 4*difAngulo;
			}
		}
		
		
		
		
		File tmp = new File(Constants.rand());
		PrintStream stream = new PrintStream(tmp);
		if(directed) 
			stream.println("digraph G {");
		else
			stream.println("graph G {");
		
		for (NODE node : listNodes) {
			stream.println("\"" + node.getKey() + "\" [pos=\"" + pos.get(node).x +"," + pos.get(node).y + "\"];");
		}
		
		String sedge = "--";
		if(directed)
			sedge = "->";
		
		for (NODE nodeA : listNodes) {
			for (EdgeAttribute<ATTRIBUTE> edge : nodeA.getEdges()) {
				NODE nodeB = edge.diff(nodeA);
				if(directed || (nodeA.getId() < nodeB.getId())) {
					stream.println("\"" + nodeA.getKey() + "\" " + sedge + " \"" + nodeB.getKey() + "\";");
				}
			}
		}
		stream.println("}");
		stream.close();
		
		/*Runtime rt = Runtime.getRuntime();
		Process p = rt.exec("circo -Tsvg " + tmp + " > " + outSvg.getAbsolutePath());
		p.waitFor();*/
	}
	
	public HashMap<NODE, Point> loadGraphviz(File in) throws InterruptedException, IOException {
		Scanner sc = new Scanner(in);
		
		HashMap<NODE, Point> result = new HashMap<>();
		while(sc.hasNextLine()) {
			String line = sc.nextLine();
			//System.out.println(line);
			if(line.contains("[height=")) {
				String key = line.substring(line.indexOf("\"")+1, line.lastIndexOf("\""));
				String line2 = sc.nextLine();
				String[] pos = line2.substring(line2.indexOf("\"")+1, line2.lastIndexOf("\"")).split(",");
				result.put(getNode((KEY) key), new Point(Double.parseDouble(pos[0]), Double.parseDouble(pos[1])));
			}
		}
		sc.close();
		return result;
	}

	
	public static <KEY, ATTRIBUTE extends Attribute<ATTRIBUTE>, NODE extends Node<KEY, ATTRIBUTE>> NODE maxConnectedNode(LinkedList<NODE> list) {
    	NODE result = list.getFirst();
    	int max = result.getEdges().size();
    	for (NODE node : list) {
			if(node.getEdges().size() > max) {
				result = node;
				max = result.getEdges().size();
			}
		}
    	return result;
    }
	
	public BufferedImage gerarImagem(HashMap<NODE, Point> points){
		
		
		
		double diferencaCirculo = Math.PI/4 + 0.1;

		try{
			int larguraImagem = 10000;
			int alturaImagem =  10000;
			double maxAresta = 1;
			double minAresta = 0;
			double variacaoAresta = 1;
			BufferedImage image = new BufferedImage(larguraImagem, alturaImagem, BufferedImage.TYPE_INT_RGB);
			//WritableRaster raster = image.getRaster();
			Graphics g1 = image.getGraphics();
			g1.setColor(Color.white);
			g1.fillRect(0,0,larguraImagem,alturaImagem);
			Font f = g1.getFont();
			f.deriveFont(f.getStyle(), f.getSize()-4);
			g1.setFont(f);

			g1.setColor(Color.gray);
			int x1, x2, y1, y2;
			
			for (NODE node : listNodes) {
				int largura = 50;
				/*g1.fillRect((int)(points.get(node).x),(int)(points.get(node).y),largura,largura);
				g1.setColor(Color.black);
				g1.drawRect((int)(points.get(node).x),(int)(points.get(node).y),largura,largura);*/
				double v = 0;
				//if (v>10) System.out.println(v);
				g1.setColor(Color.red);
				g1.fillOval((int)(points.get(node).x+v),(int)(points.get(node).y+v),largura-3,largura-3);
				g1.setColor(Color.black);
				g1.drawOval((int)(points.get(node).x+v),(int)(points.get(node).y+v),largura,largura);
				
				
				
				
				/*}else if (dc.usarGeneros && no1.genero == 'X'){
					double largura = ((dc.tamanhoNo+peso)*diferencaCirculo*raizDeDois+0.499)/2;
					int[] px = {(int)(no1.posicao.x),(int)(no1.posicao.x+largura),(int)(no1.posicao.x+2*largura),(int)(no1.posicao.x)};
					int[] py = {(int)(no1.posicao.y+largura),(int)(no1.posicao.y-largura),(int)(no1.posicao.y+largura),(int)(no1.posicao.y+largura)};
					Polygon pol = new Polygon(px, py, 4);
					g1.fillPolygon(pol);
					//g1.fillRect((int)(no1.posicao.x),(int)(no1.posicao.y),largura,largura);
					g1.setColor(Color.black);
					//g1.drawRect((int)(no1.posicao.x),(int)(no1.posicao.y),largura,largura);
					g1.drawPolygon(pol);
				}else{
					double v =  Math.max(0.0,(peso-dc.tamanhoNo)/2);
					//TODO
					v = 0;
					//if (v>10) System.out.println(v);
					g1.fillOval((int)(no1.posicao.x+v),(int)(no1.posicao.y+v),(int)(dc.tamanhoNo+peso),(int)(dc.tamanhoNo+peso));
					g1.setColor(Color.black);
					g1.drawOval((int)(no1.posicao.x+v),(int)(no1.posicao.y+v),(int)(dc.tamanhoNo+peso),(int)(dc.tamanhoNo+peso));
				}
				if (dc.numeroDeLetrasNosRotulos>0 && no1.peso >= dc.grauMinimoParaRotulos) {
					String nome = "";
					if (no1.nome.length() <= dc.numeroDeLetrasNosRotulos) nome = no1.nome;
					else if (dc.tagNosRotulos == '\t') nome = no1.nome.substring(0,Math.min(no1.nome.length(),dc.numeroDeLetrasNosRotulos));
					else{
						String[] temp = no1.nome.split(""+dc.tagNosRotulos);
						int t = 1;
						nome = temp[0];
						while (nome.length() < dc.numeroDeLetrasNosRotulos && t < temp.length){
							nome += "" + dc.tagNosRotulos + temp[t];			
							t++;
						}

					}
					if (dc.centralizarTexto){
						g1.drawString(nome, (int)(no1.posicao.x+(dc.tamanhoNo+peso)/2-14), (int)(no1.posicao.y + (dc.tamanhoNo+peso+6)/2));
					}else{
						g1.drawString(nome, (int)(no1.posicao.x + dc.tamanhoNo+peso+2), (int)(no1.posicao.y + dc.tamanhoNo+peso+1));
					}
				}*/
				//if (dc.numeroDeLetrasNosRotulos>0) g1.drawString(""+no1.tipo, (int)(no1.posicao.x + dc.tamanhoNo+peso+2), (int)(no1.posicao.y + dc.tamanhoNo+peso+1));
				//if (no1.nome.contains("Drosophila_melanogaster")) g1.drawString(no1.nome.substring(0,Math.min(no1.nome.length(),50)), (int)(no1.posicao.x + dc.tamanhoNo+peso+2), (int)(no1.posicao.y + dc.tamanhoNo+peso+1));
			}
			return image;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}


}