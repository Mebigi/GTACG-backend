package Structure.Graph;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import HeaderExtractor.HeaderExtractor;
import Structure.GeneFamily;
import Structure.PairLong;
import Structure.Registry.Dictionary;
import Structure.Registry.GeneRegistry;
import Structure.Registry.OrganismRegistry;
import Structure.Registry.Registry;
import Structure.Restriction.M8Attribute;
import Structure.Restriction.M8Restriction;
import gnu.trove.map.hash.THashMap;

public class GraphM8 extends GraphGenes <String, M8Attribute, NodeGene<String, M8Attribute>> {
	private LinkedList<M8Restriction> restLayers = new LinkedList<M8Restriction>();
	public int layer = 0;
	
	public GraphM8() {
		super(false);
	}
	
	@Override
	public GraphM8 newGraph(){
		GraphM8 graph = new GraphM8();
		for (M8Restriction rest : restLayers) {
			graph.addRestriction(rest);
		}
		graph.layer = layer;
		return graph;
	}
	
	public GraphM8(File graphFile, Dictionary dic) throws IOException {
		this(new BufferedReader(new FileReader(graphFile)), dic);
	}
	
	public GraphM8(File graphFile, String type, Dictionary dic) throws IOException {
		this(
			"gz".equals(type)?
					new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(graphFile)))):
					new BufferedReader(new FileReader(graphFile)), dic);
	}

	
	public GraphM8(BufferedReader graphFile, Dictionary dic) throws IOException {
		super(false);

		boolean readNodes = true;
		
		THashMap<Integer, NodeGene<String, M8Attribute>> hash = new THashMap<>();

		BufferedReader bf = graphFile;
		String buf = bf.readLine();
		int count = 0;
		while(buf != null) {
			if(readNodes) {
				if("EDGES".equals(buf)) {
					readNodes = false;
				}
				else {
					String linha [] = buf.split("\t");
					hash.put(Integer.parseInt(linha[1]), addNode(linha[0], dic.getGeneByKey(linha[0])));
					if(dic.getGeneByKey(linha[0]) == null)
						System.out.println("Not found gene: " + linha[0]);
					count++;
					if(count%100 == 0) {
						System.out.println("node " + count);
					}
				}
			}
			else {
				count++;
				if(count%1000000 == 0) {
					System.out.println("edge " + count);
					System.gc();
				}
				
				String linha [] = buf.split("\t");
				M8Attribute attrs [] = new M8Attribute[linha.length-2];
				NodeGene<String, M8Attribute> nodeA = hash.get(Integer.parseInt(linha[0]));
				NodeGene<String, M8Attribute> nodeB = hash.get(Integer.parseInt(linha[1]));
				for (int i = 0; i < attrs.length; i++) {
					String values [] = linha[i+2].substring(1, linha[i+2].length()-1).split(",");
					
					float percLengthAlin = Float.parseFloat(values[0]);
					float percIdentity = Float.parseFloat(values[1]);
					short lengthAlin = Short.parseShort(values[2]);
					float percMistmatches = Float.parseFloat(values[3]);
					short gapOpenings = Short.parseShort(values[4]);
					double eValue = Double.parseDouble(values[5]);
					float bitScore = Float.parseFloat(values[6]);
					
					attrs[i] = new M8Attribute(nodeA.getGene(), nodeB.getGene(), percLengthAlin, percIdentity, lengthAlin, percMistmatches, gapOpenings, eValue, bitScore);
				}
				addEdge(nodeA, nodeB, attrs, true);
			}
			buf = bf.readLine();
		}
		System.out.println("end edge");
		bf.close();
	}
	
	public GraphM8(File m8, Dictionary dic, M8Restriction rest) throws IOException {
		this(new BufferedReader(new FileReader(m8)), dic, rest, true);
	}
	
	public GraphM8(File m8, String type, Dictionary dic, M8Restriction rest) throws IOException {
		this(
			"gz".equals(type)?new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(m8)))):
			new BufferedReader(new FileReader(m8)), dic, rest, true);
	}
	
	public GraphM8(File m8, Dictionary dic, M8Restriction rest, boolean saveAttributes) throws IOException {
		this(new BufferedReader(new FileReader(m8)), dic, rest, saveAttributes);
	}
	
	public GraphM8(BufferedReader m8, Dictionary dic, M8Restriction rest) throws IOException {
		this(m8, dic, rest, true);
	}
	
	public GraphM8(BufferedReader m8, Dictionary dic, M8Restriction rest, boolean saveAttributes) throws IOException {
		super(false);
		 
		String buf = m8.readLine();
		int i = 0;
		while(buf != null) {
			String query = buf.split("\t")[0];
			String subject = buf.split("\t")[1];
			NodeGene<String, M8Attribute> nodeA = addNode(query, dic.getGeneByKey(query));
			NodeGene<String, M8Attribute> nodeB = addNode(subject, dic.getGeneByKey(subject));
			try {
				M8Attribute line = new M8Attribute(buf, dic);
				if(!query.equals(subject) && (rest == null || rest.check(line))) {
					addEdge(nodeA, nodeB, line, saveAttributes);
				}
				buf = m8.readLine();
				i++;
				if(i%100000 == 0) {
					System.out.println("ok " + i);
				}
				if(i%1000000 == 0) {
					System.gc();
				}
			} catch (Exception e) {
				System.err.println(i);
				System.err.println(buf);
				throw e;
			}
		}
		for (OrganismRegistry org : dic.getOrganisms()) {
			for (GeneRegistry gene : org.aminos) {
				addNode(gene.getKey(), dic.getGeneByKey(gene.getKey()));
			}
		}
		m8.close();
		System.gc();
	}
	
	public void save(File f) {
		
	}
	
	public NodeGene<String, M8Attribute> addNode(String key) {
		NodeGene<String, M8Attribute> node = getNode(key);
		if(node == null) {
			node = addNode(new NodeGene<String, M8Attribute>(key));
		}
		return node;
	}
	
	public NodeGene<String, M8Attribute> addNode(String key, GeneRegistry gene) {
		NodeGene<String, M8Attribute> node = getNode(key);
		if(node == null) {
			node = addNode(new NodeGene<String, M8Attribute>(key, gene));
		}
		return node;
	}
	
	public EdgeAttribute<M8Attribute> addEdge(NodeGene<String, M8Attribute> nodeA, NodeGene<String, M8Attribute> nodeB, M8Attribute line, boolean saveAttributes) {
		EdgeAttribute<M8Attribute> aresta = addEdge(nodeA, nodeB);
		if(saveAttributes) {
			aresta.addAttribute(line);
		}
		return aresta;
	}
	
	public EdgeAttribute<M8Attribute> addEdge(NodeGene<String, M8Attribute> nodeA, NodeGene<String, M8Attribute> nodeB, Collection<M8Attribute> lines, boolean saveAttributes) {
		EdgeAttribute<M8Attribute> edge = addEdge(nodeA, nodeB);
		if(saveAttributes)
			edge.addAttributes(lines);
		return edge;
	}
		
	public EdgeAttribute<M8Attribute> addEdge(NodeGene<String, M8Attribute> nodeA, NodeGene<String, M8Attribute> nodeB, M8Attribute [] line, boolean saveAttributes) {
		EdgeAttribute<M8Attribute> edge = addEdge(nodeA, nodeB);
		if(saveAttributes)
			edge.addAttributes(line);
		return edge;
	}
	
	public Collection<M8Restriction> getAllEdgeRestrictions() {
		TreeSet<M8Attribute> set = new TreeSet<>(M8Attribute.comparator);
		for (NodeGene<String, M8Attribute> node : getNodes()) {
			for (EdgeAttribute<M8Attribute> edge : node.getEdges()) {
				for (M8Attribute attr : edge.getAttributes()) {
					set.add(attr);
				}
			}
		}
		LinkedList<M8Restriction> list = new LinkedList<>();
		for (M8Attribute attr : set) {
			list.add(attr.revert());
		}
		return list;
	}
	
	public void export(File out) throws FileNotFoundException { 
		PrintStream stream = new PrintStream(out);
		for (NodeGene<String, M8Attribute> nodeA : getNodes()) {
			stream.println(nodeA.getKey() + "\t" + nodeA.getId());
		}
		stream.println("EDGES");
		for (NodeGene<String, M8Attribute> nodeA : getNodes()) {
			for (EdgeAttribute<M8Attribute> edge : nodeA.getEdges()) {
				NodeGene<String, M8Attribute> nodeB = edge.diff(nodeA);
				if(nodeA.getId() < nodeB.getId()) {
					stream.print(nodeA.getId() + "\t" + nodeB.getId());
					for (M8Attribute attr : edge.getAttributes()) {
						stream.print("\t" + attr.toStringCompact());
					}
					stream.println();
				}
			}
		}
		
		
		stream.close();
	}
	
	public void exportEdgesMCL(File out) throws FileNotFoundException {
		PrintStream stream = new PrintStream(out);
		for (NodeGene<String, M8Attribute> node : getNodes()) {
			stream.println(node.getKey() + "\t" + node.getKey() + "\t200");
			double min = 100;
			for (EdgeAttribute<M8Attribute> edge : node.getEdges()) {
				for (M8Attribute attr : edge.getAttributes()) {
					if(attr.getEValue() < min)
						min = attr.getEValue();
				}
				double log = -Math.log10(min);
				if(log > 200)
					log = 200;
				stream.println(node.getKey() + "\t" + edge.diff(node).getKey() + "\t" + (int)log);
			}
		}
		stream.close();
	}
	
	public void addRestriction(M8Restriction rest) {
		restLayers.add(rest);
	}
	
	public void addAllRestriction(Collection<M8Restriction> rests) {
		restLayers.addAll(rests);
	}
	
	public LinkedList<M8Restriction> getRestrictions() {
		return restLayers;
	}
	
	public int getLayer(GraphM8 graph, int l) {
		if(graph.getNodes().size() <= 1 || graph.avgClusterCoef() == 1 || l >= restLayers.size())
			return l;
		graph.updateRestrictions(restLayers.get(l));
		LinkedList<NodeGene<String, M8Attribute>> heads = graph.connComponentHeads();
		int max = l;
		for (NodeGene<String, M8Attribute> head : heads) {
			GraphM8 sub = graph.connComponentGraph(head);
			int x = getLayer(sub, l+1);
			if(x > max)
				max = x;
		}
		return max;
	}
	
	public int maxLayers() {
		int max = 0;
		for (NodeGene<String, M8Attribute> head : connComponentHeads()) {
			GraphM8 sub = connComponentGraph(head);
			for (M8Restriction rest : restLayers) {
				sub.addRestriction(rest);
			}
			int x = getLayer(sub, 0);
			if(x > max) 
				max = x;
		}
		return max;
	}
	
	public GraphM8 nextLayer(boolean aggregate) {
		GraphM8 next = newGraph();
		next.layer++;
		LinkedList<NodeGene<String, M8Attribute>> heads = connComponentHeads();
		for (NodeGene<String, M8Attribute> head : heads) {
			GraphM8 sub = connComponentGraph(head);
			if(sub.getNodes().size() > 1 && !sub.isFullConnected()) {
				sub.updateRestrictions(restLayers.get(next.layer));
				next.join(sub);
			}
			else if(aggregate) { 
				next.join(sub);
			}
		}
		return next;
	}
	
	public GraphM8 lastLayer(GraphM8 graph) {
		GraphM8 last = newGraph();
		
		while(last.layer <= restLayers.size() -2) {
			GraphM8 next = newGraph();
			last.layer++;
			next.layer = last.layer;
			
			for (NodeGene<String, M8Attribute> head : graph.connComponentHeads()) {
				GraphM8 sub = graph.connComponentGraph(head);
				if(sub.getNodes().size() > 1 && !sub.isFullConnected()) {
					sub.updateRestrictions(restLayers.get(next.layer));
					next.join(sub);
				}
				else
					last.join(sub);
			}
			graph = next;
			next = null;
			System.gc();
		}
		last.join(graph);
		return last;
	}
	
	
	/*public GraphM8 lastLevel() {
		return lastLevel(this);
	}
	
	private GraphM8 lastLevel(GraphM8 graph) {
		GraphM8 last = newGraph();
		while(last.level <= graph.restLevels.size() -2) {
			last.level++;
			
			GraphM8 next = newGraph();
			LinkedList<NodeGene<String, M8Attribute>> heads = graph.connComponentHeads();
			for (NodeGene<String, M8Attribute> head : heads) {
				GraphM8 sub = graph.connComponentGraph(head);
				if(sub.hasNextLevel())
					next.join(sub);
				else 
					last.join(sub);
			}
			
			next.updateRestrictions(restLevels.get(last.level));
			next.level = last.level;
			graph = next;
			System.gc();
		}
		return last;
	}*/
	
	public boolean hasNextLayer() {
		if(getNodes().size() < 2 || isFullConnected() || layer > restLayers.size() -2)
			return false;
		return true;
	}
	
	private static String tabs(int n) {
		String result = "";
		for (int i = 0; i < n; i++) {
			result += "\t";
		}
		return result;
	}
	
	public static void printLevels(GraphM8 graph, HeaderExtractor ext, PrintStream stream) throws IOException {
		LinkedList<NodeGene<String, M8Attribute>> heads = graph.connComponentHeads();
		for (NodeGene<String, M8Attribute> head : heads) {
			GraphM8 sub = graph.connComponentGraph(head);
			if(heads.size() > 1) {
				GeneFamily fam = new GeneFamily(sub.getNodes());
				stream.println(
						tabs(sub.layer) + 
						head.getKey() + 
						tabs(graph.restLayers.size()-sub.layer) + 
						sub.getNodes().size() + "\t" + 
						fam.getMap().size() + "\t" + ext.getDescription(head.getGene()));
			}
			if(sub.hasNextLayer())
				printLevels(sub.nextLayer(false), ext, stream);
		}
	}
	
	public void printLayers(HeaderExtractor ext, PrintStream stream) throws IOException {
		printLevels(this, ext, stream);
		System.gc();
	}
	
	public void printLevels2(HeaderExtractor ext, PrintStream stream) throws IOException {
		printLevels2(this, ext, stream);
	}
	
	private static void printLevels2(GraphM8 graph, HeaderExtractor ext, PrintStream stream) throws IOException {
		LinkedList<NodeGene<String, M8Attribute>> heads = graph.connComponentHeads();
		for (NodeGene<String, M8Attribute> head : heads) {
			GraphM8 sub = graph.connComponentGraph(head);
			if(heads.size() > 1) {
				GeneFamily fam = new GeneFamily(sub.getNodes());
				stream.println(tabs(sub.layer) + head.getKey() + tabs(graph.restLayers.size()-sub.layer) + sub.getNodes().size() + "\t" + fam.getMap().size() + "\t" + ext.getDescription(head.getGene()));
			}
			if(sub.hasNextLayer())
				printLevels(sub.nextLayer(false), ext, stream);
		}
	}
	
	public GraphM8 nextLevel2(boolean aggregate) {
		GraphM8 next = newGraph();
		next.layer++;
		for (NodeGene<String, M8Attribute> head : connComponentHeads()) {
			GraphM8 sub = connComponentGraph(head);
			if(sub.getNodes().size() > 1 && sub.avgClusterCoef() < 1) {
				sub.updateRestrictions(restLayers.get(next.layer));
				next.join(sub);
				
			}
			else if(aggregate) 
				next.join(sub);
		}
		return next;
	}
	
	private boolean decrescente(double vet[]) {
		for (int i = 1; i < vet.length; i++) {
			if(vet[i] > vet[i-1])
				return false;
		}
		return true;
	}
	
	public LinkedList<M8Restriction> hierClustering(double base, double limit, double step, M8Restriction rest, int numThreads, boolean extended) {
		LinkedList<M8Restriction> result = new LinkedList<>();
		GraphM8 graph = this;
		while(true) {
			double[] coef = graph.avgClusterCoefEValue(base, limit, step, numThreads, 2*numThreads);
			int max = 0;

			for (int i = max+1; i < coef.length; i++) {
				System.out.println(coef[i]);
				if(coef[i] > coef[max])
					max = i; 
			}
			if (extended) {
				if(decrescente(coef)) {
					graph = null;
					System.gc();
					return result;
				}
			}
			
			if(max == 0) {
				graph = null;
				System.gc();
				return result;
			}
			
			double evalue = Math.pow(10, -(base + max*step));
			base += max*step;
			System.out.println("#" + evalue);
			
			M8Restriction newRest = new M8Restriction(
					rest.getMinPercLengthAlin(), 
					rest.getMinPercIdentity(), 
					rest.getMinLengthAlin(), 
					rest.getMaxPercMistmatches(), 
					rest.getMaxGapOpenings(), 
					evalue, 
					rest.getMinBitScore());	
			result.add(newRest);
			System.out.println(newRest);
			//if(result.size() > 5)
			//	return result;

			int cont = 0;
			GraphM8 novo = graph.newGraph();
			for (NodeGene<String, M8Attribute> headComp : graph.connComponentHeads(newRest)) {
				GraphM8 sub = graph.connComponentGraph(headComp, newRest);
				if(sub.getNodes().size() > 2) {
					if(sub.getNodes().size() == 2)
						novo.join(sub);
					else if(!sub.isFullConnected())
						novo.join(sub);
				}
				cont++;
				if(cont%100000==0) {
					System.out.print(cont);
					System.gc();
				}
			}
			
			graph = novo;
			System.gc();
		}
	}
	
	public double[] avgClusterCoefEValue(double start, double end, double iteration, int numThreads, int numBatchs) {
		LinkedList<NodeGene<String, M8Attribute>> validNodes = listNodesByEdges(getNodes(), 2);
		ConcurrentLinkedQueue<LinkedList<NodeGene<String, M8Attribute>>> batchs = new ConcurrentLinkedQueue<>(makeBatchs(numBatchs, (int)((end-start)/iteration), validNodes));
		
		System.err.println("# Start coeficiente cluster - " + Calendar.getInstance().getTime());
		System.err.println("# TOTAL\t" + getNodes().size());
		System.err.println("# IGNORADOS\t" + (getNodes().size() - validNodes.size()));
		System.err.println("# BATCHS " + batchs.size());

		int totalSize = (int) ((end-start)/iteration) + 1;
		double [] total = new double[totalSize];
		ExecutorService executor = Executors.newFixedThreadPool(numThreads);
		for (int threadId = 0; threadId < numThreads; threadId++) {
			executor.execute(() -> {
				int cont = 0;
				double[] local = new double[total.length];
				LinkedList<NodeGene<String, M8Attribute>> listNodes = batchs.poll();
				while (listNodes != null) {
					for (NodeGene<String, M8Attribute> node : listNodes) {
						PairLong tmp[] = PairLong.makeArray(total.length, new PairLong(0, 0));
						avgClusterCoefEValue(node, start, end, iteration, tmp);
						for (int i = 0; i < total.length; i++)
							if(tmp[i].a != 0)
								local[i] += (double)tmp[i].b/tmp[i].a;
					}
					System.err.println("# " + Thread.currentThread().getId() + "\t" + cont++ + "\t" + Calendar.getInstance().getTime());
					listNodes = batchs.poll();
				}
				
				synchronized (total) {
					for (int i = 0; i < local.length; i++)
						total[i] += local[i];
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
		
		double [] result = new double[totalSize];
		for (int i = 0; i < total.length; i++) {
			result[i] = (double)total[i]/getNodes().size();
		}
		return result;
	}
	
	public double[] avgClusterCoefEValue(double start, double end, double iteration) {
		int total = (int) ((end-start)/iteration) + 1;
		double[] result = new double[total];
		
		for (NodeGene<String, M8Attribute> node : getNodes()) {
			if(node.getEdges().size() > 1) {
				PairLong[] tmp = new PairLong[total];
				for (int i = 0; i < tmp.length; i++) {
					tmp[i] = new PairLong(0, 0);
				}
				avgClusterCoefEValue(node, start, end, iteration, tmp);
				for (int i = 0; i < tmp.length; i++) {
					if(tmp[i].a > 0) {
						result[i] += (double)tmp[i].b/tmp[i].a;
					}
				}
			}
		}
		for (int i = 0; i < result.length; i++) {
			if(result[i] > 0) {
				result[i] /= getNodes().size();
			}
		}
		return result;
	}
	
	public void avgClusterCoefEValue(NodeGene<String, M8Attribute> node, double start, double end, double iteration, PairLong [] result) {
		EdgeAttribute<M8Attribute>[] edges = node.getEdgesArray();
		if(edges.length < 2)
			return;
		
		long totalA = 0;
		long totalB = 0;
		for(int i = 0; i < edges.length-1; i++) {
			EdgeAttribute<M8Attribute> edgeA = edges[i];
			NodeGene<String, M8Attribute> nodeA = edgeA.diff(node);
			M8Attribute[] attrA = edgeA.getAttributes();
			
			double eValueA = -Math.log10(attrA[0].getEValue());
			for (int j = 1; j < attrA.length; j++) {
				double e = -Math.log10(attrA[j].getEValue());
				if(eValueA < e) eValueA = e;
			}
			
			for(int j = i + 1; j < edges.length; j++) {
				EdgeAttribute<M8Attribute> edgeB = (EdgeAttribute<M8Attribute>)edges[j];
				NodeGene<String, M8Attribute> nodeB = edgeB.diff(node);
				M8Attribute[] attrB = edgeB.getAttributes();
				EdgeAttribute<M8Attribute> edgeAB = (EdgeAttribute<M8Attribute>)nodeA.getEdge(nodeB.getKey());
				
				double eValueB = -Math.log10(attrB[0].getEValue());
				for (int j2 = 1; j2 < attrB.length; j2++) {
					double e = -Math.log10(attrB[j2].getEValue());
					if(eValueB < e) eValueB = e;
				}
				
				double maxAB = Math.min(eValueA, eValueB);
				int posAB = (int)Math.floor(((maxAB) - start) / iteration);
				if(posAB >= result.length)
					posAB = result.length -1;
				if(posAB >= 0) {
					result[posAB].a++;
					totalA++;
				}
				
				int posABC = -1;
				double eValueAB = -1;
				if(edgeAB != null) {
					M8Attribute[] attrAB = edgeAB.getAttributes();
					eValueAB = -Math.log10(attrAB[0].getEValue());
					for (int j2 = 1; j2 < attrAB.length; j2++) {
						double e = -Math.log10(attrAB[j2].getEValue());
						if(eValueAB < e)
							eValueAB = e;
					}
					
					posABC = (int)Math.floor((eValueAB - start) / iteration);
					posABC = Math.min(posAB, posABC);
					
					if(posABC >= result.length)
						posABC = result.length -1;
					if(posABC >= 0) {
						result[posABC].b++;
						totalB++;
					}
				}
			}
		}
		
		for (int i = 0; i < result.length; i++) {
			long localA = result[i].a;
			long localB = result[i].b;
			result[i].a = totalA;
			result[i].b	= totalB;
			totalA -= localA;
			totalB -= localB;
		}
	}
	
	public void saveNodeList(File file, LinkedList<NodeGene<String, M8Attribute>> list) throws FileNotFoundException {
		PrintStream stream = new PrintStream(file);
		for (NodeGene<String, M8Attribute> node : list) {
			stream.println(node.getKey());
		}
		stream.close();
	}

	public LinkedList<NodeGene<String, M8Attribute>> loadNodeList(File file) throws FileNotFoundException {
		LinkedList<NodeGene<String, M8Attribute>> list = new LinkedList<>();
		Scanner sc = new Scanner(file);
		while(sc.hasNextLine()) {
			list.add(getNode(sc.nextLine()));
		}
		sc.close();
		return list;
	}
	
	public GraphM8 subGraph(LinkedList<NodeGene<String, M8Attribute>> list) {
		return (GraphM8)super.subGraph(list);
	}
	
	public GraphM8 connComponentGraph(NodeGene<String, M8Attribute> no) {
		return (GraphM8) super.connComponentGraph(no, null);
	}
	
	public LinkedList<Graph<String, M8Attribute, NodeGene<String, M8Attribute>>> connComponentGraph(M8Restriction rest) {
		return super.connComponentGraph(rest);
	}
	
	public LinkedList<LinkedList<NodeGene<String, M8Attribute>>> connComponentList(M8Restriction rest) {
		return super.connComponentList(rest);
	}
	
	public GraphM8 connComponentGraph(NodeGene<String, M8Attribute> no, M8Restriction rest) {
		return (GraphM8) super.connComponentGraph(no, rest);
	}
	
	public LinkedList<NodeGene<String, M8Attribute>> connComponentList(NodeGene<String, M8Attribute> no, M8Restriction rest) {
		return super.connComponentList(no, rest);
	}
	
	public LinkedList<NodeGene<String, M8Attribute>> connComponentHeads(M8Restriction rest) {
		return super.connComponentHeads(rest);
	}
	
	public HashMap<String, LinkedList<String>> simplify(HashSet<NodeGene<String, M8Attribute>> mult, int numThreads) {
		HashMap<String, LinkedList<String>> map = new HashMap<>();
    	for (NodeGene<String, M8Attribute> node : getNodes()) {
    		LinkedList<String> list = new LinkedList<>();
    		list.add(node.getKey());
			map.put(node.getKey(), list);
		}
    	
    	ConcurrentLinkedQueue<NodeGene<String, M8Attribute>> remove = new ConcurrentLinkedQueue<>();
        LinkedList<LinkedList<NodeGene<String, M8Attribute>>> singles = getSingleDomains(mult);
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        for (LinkedList<NodeGene<String, M8Attribute>> listA : singles) {
        	executor.submit(new Runnable() {
				@Override
				public void run() {
					HashMap<NodeGene<String, M8Attribute>, LinkedList<M8Attribute>> tmp = new HashMap<>(); 
		        	LinkedList<NodeGene<String, M8Attribute>> list = listA;
					NodeGene<String, M8Attribute> nodeA = maxConnectedNode(list);
					HashSet<NodeGene<String, M8Attribute>> hash = new HashSet<>(list);
					list.remove(nodeA);
					remove.addAll(list);
					for (NodeGene<String, M8Attribute> nodeB : list) {
						for (EdgeAttribute<M8Attribute> edge : nodeB.getEdges()) {
							NodeGene<String, M8Attribute> neighbor = edge.diff(nodeB);
							if(!hash.contains(neighbor)) {
								if(!tmp.containsKey(neighbor)) {
									tmp.put(neighbor, new LinkedList<>());
								}
								for (M8Attribute attr : edge.getAttributes()) {
									tmp.get(neighbor).add(attr);
								}
							}
						}
						map.get(nodeA.getKey()).add(nodeB.getKey());
					}
					for (Entry<NodeGene<String, M8Attribute>, LinkedList<M8Attribute>> ent : tmp.entrySet()) {
						if(nodeA.getEdge(ent.getKey().getKey()) != null)
							for (M8Attribute attr : nodeA.getEdge(ent.getKey().getKey()).getAttributes()) {
								ent.getValue().add(attr);
							}
						addEdge(nodeA, ent.getKey()).addAttributes(ent.getValue());
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
		
		for (NodeGene<String, M8Attribute> node : remove) {
			removeEdges(node.getKey());
			removeNode(node.getKey());
		}
        
        LinkedList<NodeGene<String, M8Attribute>> lmult = new LinkedList<>(mult);
        for (int i = 0; i < lmult.size(); i++) {
        	NodeGene<String, M8Attribute> nodeA = lmult.get(i);
        	
        	HashMap<NodeGene<String, M8Attribute>, LinkedList<M8Attribute>> tmp = new HashMap<>();
			for (int j = i+1; j < lmult.size(); j++) {
				NodeGene<String, M8Attribute> nodeB = lmult.get(j);
				
				if(getNode(nodeA.getKey()) != null && getNode(nodeB.getKey()) != null) {
					
					HashSet<NodeGene<String, M8Attribute>> viz = new HashSet<>();
					for (EdgeAttribute<M8Attribute> edge: nodeA.getEdges()) {
						viz.add(edge.diff(nodeA));
					}
					viz.remove(nodeB);
					boolean allEquals = true;
					for (EdgeAttribute<M8Attribute> edge : nodeB.getEdges()) {
						if(edge.diff(nodeB) != nodeA && !viz.remove(edge.diff(nodeB))) {
							allEquals = false;
						}
					}
					
					if(allEquals == true && viz.isEmpty()) {
						for (EdgeAttribute<M8Attribute> edge : nodeB.getEdges()) {
							if(edge.diff(nodeB) != nodeA) {
								for (M8Attribute attr : edge.getAttributes()) {
									if(!tmp.containsKey(edge.diff(nodeB)))
										tmp.put(edge.diff(nodeB), new LinkedList<>());
									tmp.get(edge.diff(nodeB)).add(attr);
								}
							}
						}
						removeEdges(nodeB.getKey());
						removeNode(nodeB.getKey());
						tmp.remove(nodeB);
						map.get(nodeA.getKey()).add(nodeB.getKey());
					}
				}
			}
			if(!tmp.isEmpty()) {
				for (Entry<NodeGene<String, M8Attribute>, LinkedList<M8Attribute>> ent : tmp.entrySet()) {
					if(nodeA.getEdge(ent.getKey().getKey()) != null)
						for (M8Attribute attr : nodeA.getEdge(ent.getKey().getKey()).getAttributes()) {
							ent.getValue().add(attr);
						}
					addEdge(nodeA, ent.getKey()).addAttributes(ent.getValue());
				}
			}
		}
        return map;
    }
	
    public GraphGenes<String, M8Attribute, NodeGene<String,M8Attribute>> project(double diffMax, int minLenExtra, HashMap<String, LinkedList<String>> map) {
    	GraphGenes<String, M8Attribute, NodeGene<String,M8Attribute>> result = new GraphGenes<>(true);
    	
    	for (NodeGene<String, M8Attribute> node : getNodes()) {
			result.addNode(new NodeGene<String, M8Attribute>(node.getKey(), node.getGene()));
		}
    	
    	for (NodeGene<String, M8Attribute> nodeA : getNodes()) {
			for (EdgeAttribute<M8Attribute> edge : nodeA.getEdges()) {
				NodeGene<String, M8Attribute> nodeB = edge.diff(nodeA);
				if(nodeA.getId() < nodeB.getId()) {
					double minDif = 100000;
					Boolean direction = null;
					boolean sameDirection = true;
					for (M8Attribute attr : edge.getAttributes()) {
						GeneRegistry geneA = attr.getGeneA();
						GeneRegistry geneB = edge.diff(nodeA).getGene();
						if(!map.get(nodeA.getKey()).contains(attr.getGeneA().getKey())) {
							geneA = edge.diff(nodeA).getGene();
							geneB = attr.getGeneA();
						}
						
						double diff = Math.abs(((double)geneA.getLength() - geneB.getLength())/attr.getLengthAlign());
						if(diff < minDif) {
							minDif = diff;
							boolean directionLocal = geneA.getLength() < geneB.getLength();
							if(direction != null && direction != directionLocal)
								sameDirection = false;
							direction = directionLocal;
						}
						if(Math.abs(geneA.getLength() - geneB.getLength()) < minLenExtra)
							sameDirection = false;
					}

					NodeGene<String, M8Attribute> newA = result.getNode(nodeA.getKey());
					NodeGene<String, M8Attribute> newB = result.getNode(nodeB.getKey());
					if(minDif > diffMax && sameDirection) {
						if(direction)
							result.addEdge(newA, newB).addAttributes(edge.getAttributes());
						else
							result.addEdge(newB, newA).addAttributes(edge.getAttributes());
					}
					else {
						result.addEdge(newA, newB).addAttributes(edge.getAttributes());
						result.addEdge(newB, newA).addAttributes(edge.getAttributes());
					}
				}
			}
		}
    	return result;
    }
    
    public LinkedList<LinkedList<NodeGene<String, M8Attribute>>> getDomains(HashSet<NodeGene<String, M8Attribute>> multDomainSeqs, double maxDiff, int minLenExtra, String headName, int numThreads) {
    	LinkedList<NodeGene<String, M8Attribute>> heads = connComponentHeads();
    	GraphM8 copy = connComponentGraph(heads.poll());
    	for (NodeGene<String, M8Attribute> head : heads) {
			copy.join(connComponentGraph(head));
		}
    	
    	LinkedList<LinkedList<NodeGene<String, M8Attribute>>> doms = new LinkedList<>();
    	
    	HashSet<NodeGene<String, M8Attribute>> multDomainSeqsCp = new HashSet<>();
    	multDomainSeqs.forEach((node)->{multDomainSeqsCp.add(copy.getNode(node.getKey()));});
    	
    	HashMap<String, LinkedList<String>> map = copy.simplify(multDomainSeqsCp, numThreads);
    	/*try {
    		HashMap<NodeGene<String, M8Attribute>, Color> hsCol = new HashMap<>();
    		for (NodeGene<String, M8Attribute> node : copy.getNodes()) {
				if(multDomainSeqs.contains(node))
					hsCol.put(node, Color.red);
				else
					hsCol.put(node, Color.black);
			}
			copy.plot("", new File("d2.jpg"), false, new Resolution() {@Override public int getWidth() {return 500;} @Override public int getHeight() {return 500;}}, hsCol);
		} catch (InterruptedException | IOException e1) {
			e1.printStackTrace();
		}*/
    	
    	
    	GraphGenes<String, M8Attribute, NodeGene<String, M8Attribute>> digraph = copy.project(maxDiff, minLenExtra, map);
    	
    	/*try {
    		HashMap<NodeGene<String, M8Attribute>, Color> hsCol = new HashMap<>();
    		for (NodeGene<String, M8Attribute> node : digraph.getNodes()) {
				if(multDomainSeqs.contains(node))
					hsCol.put(node, Color.red);
				else
					hsCol.put(node, Color.black);
			}
			digraph.plot("", new File("d3.jpg"), false, new Resolution() {@Override public int getWidth() {return 500;} @Override public int getHeight() {return 500;}}, hsCol);
		} catch (InterruptedException | IOException e1) {
			e1.printStackTrace();
		}*/
    	
    	HashMap<NodeGene<String, M8Attribute>, Color> colors = new HashMap<>(); 
    	multDomainSeqsCp.forEach((node)->{colors.put(node, Color.RED);});
    	
    	/*try {
			copy.plot("", new File("aaa." + headName + ".S1.jpg"), false, null, colors);
			digraph.plot("", new File("aaa." + headName + ".S2.jpg"), false, null, colors);
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}*/
    	if(digraph.isDirected(true)) {
    		doms.addAll(digraph.getDomains());
    	}
    	else {
    		doms.add(new LinkedList<NodeGene<String, M8Attribute>>(getNodes()));
    		return doms;
    	}
    	
    	LinkedList<LinkedList<NodeGene<String, M8Attribute>>> result = new LinkedList<>();	
    	for (LinkedList<NodeGene<String, M8Attribute>> dom : doms) {
    		LinkedList<NodeGene<String, M8Attribute>> list = new LinkedList<>();
			for (NodeGene<String, M8Attribute> node : dom) {
				for (String key : map.get(node.getKey())) {
					list.add(getNode(key));
				}
			}
			result.add(list);
		}
    	return result;
    }
}