package Structure.Graph;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import org.forester.archaeopteryx.AptxUtil;
import org.forester.archaeopteryx.Archaeopteryx;
import org.forester.archaeopteryx.Configuration;
import org.forester.archaeopteryx.TreeColorSet;
import org.forester.phylogeny.Phylogeny;
import org.forester.phylogeny.PhylogenyNode;
import org.forester.phylogeny.data.BranchColor;
import org.forester.phylogeny.data.NodeVisualData;
import org.forester.phylogeny.data.NodeVisualData.FontType;
import org.forester.phylogeny.data.NodeVisualData.NodeFill;
import org.forester.phylogeny.data.NodeVisualData.NodeShape;
import org.forester.phylogeny.data.PropertiesList;
import org.forester.phylogeny.data.Property;
import org.forester.phylogeny.data.Property.AppliesTo;
import org.forester.phylogeny.data.Sequence;
import org.graphstream.graph.implementations.SingleGraph;

import Structure.Alignment;
import Structure.Registry.Dictionary;
import Structure.Registry.GeneRegistry;
import Structure.Registry.GeneRegistryRandom;
import Structure.Registry.OrganismRegistry;
import Structure.Registry.RegistryGroup;
import Structure.Registry.RegistryGroups;
import Structure.Restriction.DistanceAttribute;
import ToolkitFile.TreeFile;

public class PhylogeneticTree extends Graph<String, DistanceAttribute, Node<String, DistanceAttribute>> {
	public Node<String, DistanceAttribute> root = null;
	public boolean rooted = false;
	int pos = 0;
	public HashSet<Node<String, DistanceAttribute>> setLeaves = new HashSet<>();
	public HashMap<String, Node<String, DistanceAttribute>> hashLeaves = new HashMap<>();
	public ArrayList<Node<String, DistanceAttribute>> arrayLeaves = new ArrayList<>();
	public HashMap<Node<String, DistanceAttribute>, Integer> posArray = new HashMap<>(); 
	
	final public static String archaeopteryxJsHeader() { 
		return
			"<script src='http://d3js.org/d3.v3.min.js'></script>\n" +
			"<link rel='stylesheet' href='https://code.jquery.com/ui/1.12.0/themes/base/jquery-ui.css'>\n" +
			"<script src='https://code.jquery.com/jquery-1.12.4.js'></script>\n" +
			"<script src='https://code.jquery.com/ui/1.12.0/jquery-ui.js'></script>\n" +
			"<script src='http://www.phyloxml.org/js/dependencies/sax.js'></script>\n" +
			"<script src='http://www.phyloxml.org/js/dependencies/rgbcolor.js'></script>\n" +
			"<script src='http://www.phyloxml.org/js/dependencies/Blob.js'></script>\n" +
			"<script src='http://www.phyloxml.org/js/dependencies/canvas-toBlob.js'></script>\n" +
			"<script src='http://www.phyloxml.org/js/dependencies/canvg.js'></script>\n" +
			"<script src='http://www.phyloxml.org/js/dependencies/FileSaver.js'></script>\n" +
			"<script src='http://www.phyloxml.org/js/phyloxml_0_912.js'></script>\n" +
			"<script src='../forester.js'></script>\n" +
			"<script src='../archaeopteryx.js'></script>";
	}
	
	final public static String archaeopteryxJsFunctionLoad(String fileName, String nodeVis) {
		return 
			"function loadTree() {\n" + 
			"  var options = optionsArchaeopteryx;\n" + 
			"  options.treeName = '" + fileName + "';\n" + 
			"  var nodeVisualizations = {};\n" +
			nodeVis +
			"  var loc = '" + fileName + ".xml';\n" + 
			"  jQuery.get(loc, function (data) {archaeopteryx.launchArchaeopteryx('#phylogram',loc,data,options,settingsArchaeopteryx,true,false,nodeVisualizations)},'text');\n" +
			"}\n";
	}
	
	final public static String archaeopteryxJsHtmlElem() {
		return 
				"<div id='phylogram'></div>\n" +
				"<div id='controls0' class='ui-widget-content'></div>\n" +
				"<div id='controls1' class='ui-widget-content'></div>";
	}
	
	public LinkedList<Node<String, DistanceAttribute>> getLeaves() {
		return new LinkedList<Node<String, DistanceAttribute>>(setLeaves);
	}
	
	int noCount = 0;
	Node<String, DistanceAttribute> read(String newick) {
		pos++;
		String label = "";
		String weight = "";
		boolean marklabel = false;
		Node<String, DistanceAttribute> node = new Node<String, DistanceAttribute>("NO" + noCount++);
		addNode(node);
		Node<String, DistanceAttribute> last = null;
		EdgeAttribute<DistanceAttribute> lastEdge = null;
		while(true) {
			char letter = newick.charAt(pos);
			if(letter == ')') {
				if(last == null) {
					Node<String, DistanceAttribute> newNode = new Node<>(label);
					addNode(newNode);
					double distance = 0;
					if(weight.length() > 0)
						distance = Double.parseDouble(weight);
					addEdge(node, newNode).addAttribute(new DistanceAttribute(distance));
					/*EdgeAttribute<DistanceAttribute> edge = new EdgeAttribute<>(node, newNode);
					edge.addAttribute(new DistanceAttribute(distance));
					node.setEdge(newNode.getKey() + pos, edge);
					newNode.setEdge(node.getKey() + pos, edge);*/
					weight = "";
					label = "";
					setLeaves.add(newNode);
					hashLeaves.put(newNode.getKey(), newNode);
				}
				else {
					//last.getKey() = label;
					double distance = 0;
					if(weight.length() > 0)
						distance = Double.parseDouble(weight);
					lastEdge.addAttribute(new DistanceAttribute(distance));
					last = null;
				}
				return node;
			}
			else if(letter == '('){
				Node<String, DistanceAttribute> novo = read(newick);
				lastEdge = addEdge(node, novo);
				last = novo;
				//System.out.println("ok");
			}
			else if(letter == ',') {
				if(last == null) {
					Node<String, DistanceAttribute> newNode = new Node<>(label);
					addNode(newNode);
					double distance = 0;
					if(weight.length() > 0)
						distance = Double.parseDouble(weight);
					addEdge(node, newNode).addAttribute(new DistanceAttribute(distance));
					/*EdgeAttribute<DistanceAttribute> edge = new EdgeAttribute<>(node, newNode);
					edge.addAttribute(new DistanceAttribute(distance));
					node.setEdge(newNode.getKey() + pos, edge);
					newNode.setEdge(node.getKey() + pos, edge);*/
					weight = "";
					label = "";
					setLeaves.add(newNode);
					hashLeaves.put(newNode.getKey(), newNode);
				}
				else {
					//last.getKey() = label;
					double distance = 0;
					if(weight.length() > 0)
						distance = Double.parseDouble(weight);
					lastEdge.addAttribute(new DistanceAttribute(distance));
					last = null;
				}
				label = "";
				weight = "";
				marklabel = false;
			}
			else if(letter == ':') {
				marklabel = true;
			}
			else {
				if(!marklabel)
					label += letter;
				else
					weight += letter;
					
			}
			pos++;
		}
	}
	
	public PhylogeneticTree(TreeFile tree, boolean rooted) throws IOException {
		this(tree.load().replace("\n", ""), rooted);
	}
	
	public PhylogeneticTree(String tree, boolean rooted) {
		super(false);
		rooted = false;
		pos = 0;
		root = read(tree.replace("\n", ""));
		arrayLeaves.addAll(setLeaves);
		for (int i = 0; i < arrayLeaves.size(); i++) {
			posArray.put(arrayLeaves.get(i), i);
		}
	}
	
	public String print(boolean simplified) {
		return print(root, simplified);
	}
	
	public String print() {
		return print(root, false);
	}

	public String print(Map<String, String> map, boolean simplified) {
		return print(root, map, simplified);
	}
	
	public String print(Map<String, String> map) {
		return print(root, map, false);
	}

	public String print(Node<String, DistanceAttribute> node, boolean simplified) {
		return print(node, new HashSet<Node<String, DistanceAttribute>>(), null, simplified) + ";";
	}
	
	public String print(Node<String, DistanceAttribute> node) {
		return print(node, new HashSet<Node<String, DistanceAttribute>>(), null, false) + ";";
	}

	
	public String print(Node<String, DistanceAttribute> node, Map<String, String> map, boolean simplified) {
		return print(node, new HashSet<Node<String, DistanceAttribute>>(), map, simplified) + ";";
	}
	
	public String print(Node<String, DistanceAttribute> node, Map<String, String> map) {
		return print(node, new HashSet<Node<String, DistanceAttribute>>(), map, false) + ";";
	}
	
	public String print(Node<String, DistanceAttribute> node, HashSet<Node<String, DistanceAttribute>> visited, Map<String, String> map, boolean simplified) {
		visited.add(node);
		Collection<? extends EdgeAttribute<DistanceAttribute>> edges = node.getEdges();
		String result = "(";
		for (EdgeAttribute<DistanceAttribute> aresta : edges) {
			Node<String, DistanceAttribute> dif = aresta.diff(node);
			if(!visited.contains(dif)) {
				if(setLeaves.contains(dif)) {
					if(simplified)
						result += (map==null?dif.getKey():map.get(dif.getKey())) + ",";
					else
						result += (map==null?dif.getKey():map.get(dif.getKey())) + ":" + aresta.getAttributes()[0].distance + ",";
				}					
				else {
					if(simplified)
						result += print(dif, visited, map, simplified) + ",";
					else
						result += print(dif, visited, map, simplified) + ":" + aresta.getAttributes()[0].distance + ",";
				}
			}
		}
			
		return result.substring(0, result.length() - 1) + ")";
	}
	
	public boolean isomorph(PhylogeneticTree phTree) throws InterruptedException, IOException {
		return isomorph(phTree, false);
	}
	
	public boolean isomorph(PhylogeneticTree phTree, boolean debug) throws InterruptedException, IOException {
		if(rooted) {
			return isomorfo(root, phTree, phTree.root, null);
		}
		
		Node<String, DistanceAttribute> nodeA = hashLeaves.values().iterator().next();
		Node<String, DistanceAttribute> nodeB = phTree.hashLeaves.get(nodeA.getKey());
		if(nodeB == null || !nodeA.getKey().equals(nodeB.getKey())) {
			return false;
		}
		
		SingleGraph graph = null;
		if(debug)
			graph = plot("", null, true);
		
		if(isomorfo(nodeA, phTree, nodeB, graph))
			return true;
		return false;
	}
	
	private void mark(Node<String, DistanceAttribute> node, SingleGraph graph, Color color) {
		if(graph != null) {
			graph.getNode("" + node.getId()).addAttribute("ui.color", color);
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {}
		}
	}
	
	HashSet<Node<String, DistanceAttribute>> visited = new HashSet<>();
	private boolean isomorfo(Node<String, DistanceAttribute> nodeA, PhylogeneticTree phTree, Node<String, DistanceAttribute> nodeB, SingleGraph graph) throws InterruptedException {
		if(visited.contains(nodeA))
			return false;
		visited.add(nodeA);
		mark(nodeA, graph, Color.RED);
		for (EdgeAttribute<DistanceAttribute> edge : nodeA.getEdges()) {
			Node<String, DistanceAttribute> dif = edge.diff(nodeA);
			if(!visited.contains(dif)) {
				if(setLeaves.contains(dif)) {
					if(nodeB.getEdge(dif.getKey()) == null) {
						mark(nodeA, graph, Color.YELLOW);
						mark(nodeA, graph, Color.WHITE);
						visited.remove(nodeA);
						return false;
					}
				}
				else {
					boolean contain = false;
					for (EdgeAttribute<DistanceAttribute> edge2 : nodeB.getEdges()) {
						Node<String, DistanceAttribute> dif2 = edge2.diff(nodeB);
						if(!phTree.setLeaves.contains(dif2)) {
							if(isomorfo(dif, phTree, dif2, graph)) {
								contain = true;
								break;
							}
						}
					}
					if(!contain) {
						mark(nodeA, graph, Color.GREEN);
						mark(nodeA, graph, Color.WHITE);
						visited.remove(nodeA);
						return false;
					}
				}	
			}
		}
		visited.remove(nodeA);
		mark(nodeA, graph, Color.WHITE);
		return true;
	}
	
	public void simplify(double minimo) {
		HashSet<Node<String, DistanceAttribute>> visited = new HashSet<>();
		LinkedList<Node<String, DistanceAttribute>> copy = new LinkedList<>(getNodes());
		for (Node<String, DistanceAttribute> node : copy) {
			if(!visited.contains(node)) {
				simplify(node, minimo, visited);
				if(node.edges.size() == 2) {
					EdgeAttribute<DistanceAttribute>[] edges = node.getEdgesArray();
					EdgeAttribute<DistanceAttribute> edgeA = edges[0];
					EdgeAttribute<DistanceAttribute> edgeB = edges[1];
					
					Node<String,DistanceAttribute> neighborA = edgeA.diff(node);
					Node<String,DistanceAttribute> neighborB = edgeB.diff(node);
					
					double distance = edgeA.getAttributes()[0].distance + edgeB.getAttributes()[0].distance;
					addEdge(neighborA, neighborB).addAttribute(new DistanceAttribute(distance));
					removeEdges(node.getKey());
					removeNode(node.getKey());
					
					if(root == node) {
						if(neighborA.edges.size() > 1)
							root = neighborA;
						else
							root = neighborB;
					}
				}
			}
		}
	}
	
	public void simplify(Node<String, DistanceAttribute> node, double min, HashSet<Node<String, DistanceAttribute>> visited) {
		if(!setLeaves.contains(node)) {
			visited.add(node);
			EdgeAttribute<DistanceAttribute> neighborEdge = null;
			LinkedList<EdgeAttribute<DistanceAttribute>> leaf = new LinkedList<>();
			for (EdgeAttribute<DistanceAttribute> edge : node.getEdges()) {
				if(neighborEdge == null && !setLeaves.contains(edge.diff(node))) 
					neighborEdge = edge;
				else {
					leaf.add(edge);
				}
			}
			if(neighborEdge != null) {
				if(neighborEdge.getAttributes()[0].distance <= min) {
					System.out.println(neighborEdge.getAttributes()[0].distance);
					System.out.println(min);
					Node<String, DistanceAttribute> neighbor = neighborEdge.diff(node);
					for (EdgeAttribute<DistanceAttribute> edge : leaf) {
						edge.getA().remove(edge.diff(edge.getA()).getKey());
						edge.getB().remove(edge.diff(edge.getB()).getKey());
						addEdge(neighbor, edge.diff(node)).addAttribute(new DistanceAttribute(edge.getAttributes()[0].distance));
					}
					neighbor.remove(node.getKey());
					node.edges.remove(neighbor.getKey());
					getNodes().remove(node);
					removeNode(node.getKey());
					if(root == node)
						root = neighbor;
					simplify(neighbor, min, visited);
				}
			}
		}
	}
	
	private PhylogenyNode makePhylogeny(
			Node<String, DistanceAttribute> node, 
			HashSet<Node<String, DistanceAttribute>> visited, 
			Dictionary dic, 
			boolean byGene,
			RegistryGroups gs[],
			Alignment align,
			boolean color,
			HashMap<String, Color> colors,
			boolean saveSeqs) {
		visited.add(node);
		PhylogenyNode fNode = new PhylogenyNode();
		PropertiesList list1 = new PropertiesList();
		list1.addProperty(new Property("ird:Pattern", "E", null, "xsd:string", AppliesTo.PARENT_BRANCH));
		fNode.getNodeData().setProperties(list1);
		if(node.getEdges().size() == 1) {
			if(byGene) {
				GeneRegistry gene = dic.getGeneByKey(node.getKey());
				fNode.setName(gene.getOrganism().getRoot().getAbbrev());
				
				if(gs != null) {
					PropertiesList list = new PropertiesList();
					for (int i = 0; i < gs.length; i++) {
						RegistryGroup g = gs[i].getGroup(dic.getGeneById(node.getKey()).getOrganism().getRoot());
						list.addProperty(new Property("ird:" + gs[i].getName(), g.getName(), null, "xsd:string", AppliesTo.NODE));
					}
					fNode.getNodeData().setProperties(list);
				}
				Sequence seq = new Sequence();
				seq.setGeneName(gene.getKey());
				if(align != null && saveSeqs) {
					seq.setMolecularSequence(align.getSequence(node.getKey()).toString());
					seq.setMolecularSequenceAligned(true);
				}
				fNode.getNodeData().setSequence(seq);
				if(color && colors != null && colors.get(node.getKey()) != null) {
					fNode.getNodeData().setNodeVisualData(new NodeVisualData("", FontType.BOLD, (byte)20, colors.get(node.getKey()), NodeShape.CIRCLE, NodeFill.SOLID, new Color( 0, 0, 0 ), (float)0, (float)0));
					fNode.getBranchData().setBranchColor(new BranchColor(colors.get(node.getKey())));
				}
			}
			else {
				OrganismRegistry org = dic.getOrgByKey(node.getKey());
				if(org == null)
					org = dic.getOrgByAbbrev(node.getKey());
				System.out.println("#" + node.getKey() + "#");
				fNode.setName(org.getRoot().getAbbrev());
				
				if(gs != null) {
					PropertiesList list = new PropertiesList();
					for (int i = 0; i < gs.length; i++) {
						RegistryGroup g = gs[i].getGroup(org);
						list.addProperty(new Property("ird:" + gs[i].getName(), g.getName(), null, "xsd:string", AppliesTo.NODE));
					}
					fNode.getNodeData().setProperties(list);
				}
				
				
				if(color && colors != null && colors.get(node.getKey()) != null) {
					fNode.getNodeData().setNodeVisualData(new NodeVisualData("", FontType.BOLD, (byte)20, colors.get(node.getKey()), NodeShape.CIRCLE, NodeFill.SOLID, new Color( 0, 0, 0 ), (float)0, (float)0));
					fNode.getBranchData().setBranchColor(new BranchColor(colors.get(node.getKey())));
				}
			}
		}
		
		for (String s : node.getNeighbors()) {
			EdgeAttribute<DistanceAttribute> edge = node.getEdge(s);
			Node<String, DistanceAttribute> neighborNode = edge.diff(node);
			if(!visited.contains(neighborNode)) {
				PhylogenyNode nodeVizinho = makePhylogeny(neighborNode, visited, dic, byGene, gs, align, color, colors, saveSeqs);
				/*Integer[] v1 = phh.get(node).get(neighborNode);
				Integer[] v2 = phh.get(neighborNode).get(node);
				
				int x1 = unitary(v1);
				int x2 = unitary(v2);
				
				if()
					
				nodeVizinho.getBranchData().*/
				
				fNode.addAsChild(nodeVizinho);
				nodeVizinho.setDistanceToParent(edge.getAttributes()[0].distance);
			}
			
		}
		return fNode;
	}
	
	public void plotArchaeopteryx(Dictionary dic, boolean byGene, File out, boolean saveSeqs, boolean show) {
		plotArchaeopteryx(dic, byGene, false, (HashMap<String, Color>)null, out, saveSeqs, show);
	}
	
	public void plotArchaeopteryx(Dictionary dic, boolean byGene, boolean color, File out, boolean saveSeqs, boolean show) {
		HashMap<String, Color> colors = null;
		if(color == true) {
			colors = new HashMap<String, Color>();
			for (Node<String, DistanceAttribute> leaf : setLeaves) {
				if(byGene)
					colors.put(leaf.getKey(), dic.getGeneById(leaf.getKey()).getOrganism().getRoot().getColor());
				else
					colors.put(leaf.getKey(), dic.getOrgByKey(leaf.getKey()).getColor());
			}
		}
		
		plotArchaeopteryx(dic, byGene, color, colors, out, saveSeqs, show);
	}
	
	public void plotArchaeopteryx(Dictionary dic, boolean byGene, boolean color, RegistryGroups groups, File out, boolean saveSeqs, boolean show) {
		HashMap<String, Color> colors = null;
		if(color == true) {
			colors = new HashMap<String, Color>();
			for (Node<String, DistanceAttribute> leaf : setLeaves) {
				colors.put(leaf.getKey(), groups.getGroup(dic.getGeneById(leaf.getKey()).getOrganism().getRoot()).getColor());
			}
		}
		
		plotArchaeopteryx(dic, byGene, color, colors, out, saveSeqs, show);
	}
	
	HashMap<Node<String, DistanceAttribute>, HashMap<Node<String, DistanceAttribute>, Integer[]>> phh = null;
	public void plotArchaeopteryx(Dictionary dic, boolean byGene, boolean color, HashMap<String, Color> colors, File out, boolean saveSeqs, boolean show) {
		Configuration config = new Configuration();
		config.setColorizeBranches( true );
		config.putDisplayColors( TreeColorSet.BACKGROUND, new Color( 255, 255, 255 ) );
        config.putDisplayColors( TreeColorSet.BRANCH, new Color( 0, 0, 0 ) );
        config.putDisplayColors( TreeColorSet.OVERVIEW, new Color( 0, 0, 0 ) );
        config.putDisplayColors( TreeColorSet.SEQUENCE, new Color( 0, 0, 0 ) );
		
		HashSet<Node<String, DistanceAttribute>> visited = new HashSet<>();
		Phylogeny phy = new Phylogeny();
		synchronized(sync) {
			phy.setRoot(makePhylogeny(root, visited, dic, byGene, null, null, color, colors, saveSeqs));
		}
		phy.setRooted(rooted);
		
		if(show)
			Archaeopteryx.createApplication(phy, config, "Graph");
		
		if(out != null) {
			try {
				AptxUtil.writePhylogenyToGraphicsFile( phy,
				        out,
				        1000,
				        800,
				        AptxUtil.GraphicsExportType.PNG,
				        config );
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static Integer sync = 0;
	public void xmlArchaeopteryx(Dictionary dic, boolean byGene, RegistryGroups gs[], Alignment align, File out, boolean saveSeqs) throws FileNotFoundException {
		HashSet<Node<String, DistanceAttribute>> visited = new HashSet<>();
		Phylogeny phy = new Phylogeny();
		synchronized(sync) {
			phy.setRoot(makePhylogeny(root, visited, dic, byGene, gs, align, false, null, saveSeqs));
		}
		phy.setRooted(rooted);
		String xml = phy.toPhyloXML(0).replaceAll("  ", "");
		PrintStream stream = new PrintStream(out);
		stream.print(xml);
		stream.close();
	}
	
	public static Integer[] vetSoma(Integer a[], Integer b[]) {
		Integer vet [] = new Integer[a.length];
		for (int i = 0; i < a.length; i++) {
			vet[i] = a[i] + b[i];
		}
		return vet;
	}
	
	public static Integer[] vetSub(Integer a[], Integer b[]) {
		Integer vet [] = new Integer[a.length];
		for (int i = 0; i < vet.length; i++) {
			vet[i] = a[i] - b[i];
		}
		return vet;
	}
	
	public static int unitary(Integer vet[]) {
		int k = -1;
		int cont = 0;
		for (int i = 0; i < vet.length; i++) {
			if(vet[i] != 0) {
				k = i;
				cont++;
			}
		}
		if(cont == 1)
			return k;
		return -1;
	}
	
	public HashMap<Node<String, DistanceAttribute>, HashMap<Node<String, DistanceAttribute>, Integer[]>> edgeDivisionByGroups(RegistryGroups groups, Dictionary dic) {
		HashMap<Node<String, DistanceAttribute>, HashMap<Node<String, DistanceAttribute>, Integer[]>> map = new HashMap<Node<String, DistanceAttribute>, HashMap<Node<String, DistanceAttribute>, Integer[]>>(); 
		edgeDivisionByGroupsForward(map, root, new HashSet<Node<String, DistanceAttribute>>(), groups, dic);
		edgeDivisionByGroupsBackward(map, root, new HashSet<Node<String, DistanceAttribute>>(), groups, dic);
		return map;
	}

	private Integer[] edgeDivisionByGroupsForward(HashMap<Node<String, DistanceAttribute>, HashMap<Node<String, DistanceAttribute>, Integer[]>> map, Node<String, DistanceAttribute> node, HashSet<Node<String, DistanceAttribute>> visited, RegistryGroups groups, Dictionary dic) {
		visited.add(node);
		Integer total[] = new Integer[groups.size()];
		for (int i = 0; i < total.length; i++) {
			total[i] = 0;
		}

		if(setLeaves.contains(node)) {
			int x = groups.getGroup(dic.getGeneByKey(node.getKey()).getOrganism().getRoot()).getId();
			total[x]++;
			return total;
		}
		
		for (EdgeAttribute<DistanceAttribute> edge : node.getEdges()) {
			Node<String, DistanceAttribute> nodeA = edge.diff(node);
			if(!visited.contains(nodeA)) {
				Integer[] vet = edgeDivisionByGroupsForward(map, nodeA, visited, groups, dic);
				total = vetSoma(total, vet);
				if(!map.containsKey(node))
					map.put(node, new HashMap<Node<String, DistanceAttribute>, Integer[]>());
				map.get(node).put(nodeA, vet);
			}
		}
		return total;
	}
	
	private void edgeDivisionByGroupsBackward(HashMap<Node<String, DistanceAttribute>, HashMap<Node<String, DistanceAttribute>, Integer[]>> map, Node<String, DistanceAttribute> node, HashSet<Node<String, DistanceAttribute>> visited, RegistryGroups groups, Dictionary dic) {
		visited.add(node);
		Integer total[] = new Integer[groups.size()];
		for (int i = 0; i < total.length; i++) {
			total[i] = 0;
		}

		for (EdgeAttribute<DistanceAttribute> edge : node.getEdges()) {
			Node<String, DistanceAttribute> dif = edge.diff(node);
			total = vetSoma(total, map.get(node).get(dif));
		}
		
		for (EdgeAttribute<DistanceAttribute> edge : node.getEdges()) {
			Node<String, DistanceAttribute> dif = edge.diff(node);
			Integer[] vet = vetSub(total, map.get(node).get(dif));
			if(!map.containsKey(dif))
				map.put(dif, new HashMap<Node<String, DistanceAttribute>, Integer[]>());
			map.get(dif).put(node, vet);
			
			if(!visited.contains(dif) && !setLeaves.contains(dif))
				edgeDivisionByGroupsBackward(map, dif, visited, groups, dic);
		}
	}

	public LinkedList<LinkedList<String>> getOrthologs(double dc) {
		//double div = 2*al.diversity();
		double div = (0.1 + 2*dc)/(1 + 2*dc);
		//System.out.println(div);
		//System.out.println(d*((0.1 + div)/(1 + div)));
		
		HashSet<EdgeAttribute<DistanceAttribute>> removeEdges = new HashSet<>();
		for (Node<String, DistanceAttribute> node : getNodes()) {
			for (EdgeAttribute<DistanceAttribute> edge : node.getEdges()) {
				//System.out.println(edge.getAttributes()[0].distance);
				//if(edge.getAttributes()[0].distance > dc*((0.1 + div)/(1 + div))) {
				if(edge.getAttributes()[0].distance > div) {
					removeEdges.add(edge);
				}
			}
		}
		for (EdgeAttribute<DistanceAttribute> edge : removeEdges) {
			edge.getA().remove(edge.getB().getKey());
			edge.getB().remove(edge.getA().getKey());
		}
		
		LinkedList<LinkedList<Node<String, DistanceAttribute>>> conns = connComponentList();
		LinkedList<LinkedList<String>> orthos = new LinkedList<>();
		for (LinkedList<Node<String, DistanceAttribute>> conn : conns) {
			LinkedList<String> list = new LinkedList<>();
			for (Node<String, DistanceAttribute> node : conn) {
				if(setLeaves.contains(getNode(node.getKey()))) {
					list.add(node.getKey());
				}
			}
			if(list.size() > 0)
				orthos.add(list);
		}
		//System.out.println();
		return orthos;
	}
	
	private LinkedList<Node<String, DistanceAttribute>> feedFirstDescendant(Node<String, DistanceAttribute> node, HashMap<Node<String, DistanceAttribute>, String> firstDescendant, HashSet<Node<String, DistanceAttribute>> visited, Map<String, String> map) {
		visited.add(node);
		if(setLeaves.contains(node)) {
			firstDescendant.put(node, map.get(node.getKey()));
			LinkedList<Node<String, DistanceAttribute>> list = new LinkedList<>();
			list.add(node);
			return list;
		}
		LinkedList<Node<String, DistanceAttribute>> descendants = new LinkedList<>();
		Node<String, DistanceAttribute> first = null;
		for (EdgeAttribute<DistanceAttribute> edge : node.getEdges()) {
			Node<String, DistanceAttribute> neighbor = edge.diff(node);
			if(!visited.contains(neighbor)) {
				descendants.addAll(feedFirstDescendant(neighbor, firstDescendant, visited, map));
				//System.out.println("!" + first.getKey() + "\t" + neighbor.getKey());
				//System.out.println(firstDescendant.get(first));
				//System.out.println(firstDescendant.get(neighbor));
				if(first == null || firstDescendant.get(first).compareTo(firstDescendant.get(neighbor)) > 0)
					first = neighbor;
			}
		}
		descendants.sort(new Comparator<Node<String, DistanceAttribute>>() {
			@Override
			public int compare(Node<String, DistanceAttribute> arg0, Node<String, DistanceAttribute> arg1) {
				return map.get(arg0.getKey()).compareTo(map.get(arg1.getKey()));
			}			
		});
		
		/*Node<String, DistanceAttribute> first = descendants.iterator().next();
		for (Node<String, DistanceAttribute> desc : descendants) {
			if(first.getKey().compareTo(desc.getKey()) > 0)
				first = desc;
		}*/
		
		String value = "";
		for (Node<String, DistanceAttribute> desc : descendants) {
			value += "," + map.get(desc.getKey());
		}
		value = value.substring(1);
		
		firstDescendant.put(node, value);
		return descendants;
	}
	
	public String print(Node<String, DistanceAttribute> node, HashSet<Node<String, DistanceAttribute>> visited, boolean simplified, HashMap<Node<String, DistanceAttribute>, String> firstDescendant, Map<String, String> map) {
		visited.add(node);
		Collection<? extends EdgeAttribute<DistanceAttribute>> edges = node.getEdges();
		String result = "(";
		LinkedList<Node<String, DistanceAttribute>> neighbors = new LinkedList<>();
		for (EdgeAttribute<DistanceAttribute> edge : edges) {
			neighbors.add(edge.diff(node));
		}
		neighbors.sort(new Comparator<Node<String, DistanceAttribute>>() {
			@Override
			public int compare(Node<String, DistanceAttribute> arg0, Node<String, DistanceAttribute> arg1) {
				return firstDescendant.get(arg0).compareTo(firstDescendant.get(arg1));
			}
		});
		
		for (Node<String, DistanceAttribute> neighbor : neighbors) {
			if(!visited.contains(neighbor)) {
				if(setLeaves.contains(neighbor)) {
					if(simplified)
						result += map.get(neighbor.getKey()) + ",";
				}					
				else {
					if(simplified)
						result += print(neighbor, visited, simplified, firstDescendant, map) + ",";
				}
			}
		}
			
		return result.substring(0, result.length() - 1) + ")";
	}
	
	public String getCanonical() {
		HashMap<String, String> map = new HashMap<>();
		for (Node<String,DistanceAttribute> node : setLeaves) {
			map.put(node.getKey(), node.getKey());
		}
		return getCanonical(map);
	}

	public String getCanonical(Map<String, String> map) {
		Node<String, DistanceAttribute> first = setLeaves.iterator().next();
		for (Node<String, DistanceAttribute> leaf : setLeaves) {
			if(map.get(first.getKey()).compareTo(map.get(leaf.getKey())) > 0)
				first = leaf;
				
		}
		//System.out.println();
		Node<String, DistanceAttribute> root = first.getEdges().iterator().next().diff(first);
		HashMap<Node<String, DistanceAttribute>, String> firstDescendant = new HashMap<>();
		HashSet<Node<String, DistanceAttribute>> visited = new HashSet<>();
		
		feedFirstDescendant(root, firstDescendant, visited, map);
		
		visited.clear();
		return print(root, visited, true, firstDescendant, map);
	}
	
	public LinkedList<BitSet> getAllBipartitions() {
		LinkedList<BitSet> bipartitions = new LinkedList<>();
		HashSet<Node<String,DistanceAttribute>> visited = new HashSet<>();
		
		Node<String, DistanceAttribute> first = setLeaves.iterator().next();
		visited.add(first);
		//BitSet bipart = new BitSet();
		//bipart.set(posArray.get(first));
		//bipartitions.add(bipart);
		getAllBipartitions(first.getEdges().iterator().next().diff(first), bipartitions, visited, posArray.size());
		
		return bipartitions;
	}

	private BitSet getAllBipartitions(Node<String, DistanceAttribute> node, LinkedList<BitSet> bipartitions, HashSet<Node<String, DistanceAttribute>> visited, int size) {
		visited.add(node);
		if(setLeaves.contains(node)) {
			BitSet bipart = new BitSet();
			bipart.set(posArray.get(node));
			return bipart;
		}
		
		BitSet total = new BitSet();
		for (EdgeAttribute<DistanceAttribute> edge : node.getEdges()) {
			Node<String, DistanceAttribute> neighbor = edge.diff(node);
			if(!visited.contains(neighbor)) {
				total.or(getAllBipartitions(neighbor, bipartitions, visited, size));
			}
		}
		bipartitions.add(total);
		BitSet inverted = (BitSet)total.clone();
		inverted.flip(0, size);
		bipartitions.add(inverted);
		return total;
	}

	public boolean isSubTree(LinkedList<String> nodes) {
		BitSet set = new BitSet();
		for (String string : nodes) {
			set.set(posArray.get(getNode(string)));
		}
		
		LinkedList<BitSet> bipartitions = getAllBipartitions();
		for (BitSet bipart : bipartitions) {
			if(bipart.equals(set)) {
				return true;
			}
		}
		return false;
	}
	
	public LinkedList<String> maxSubTree(LinkedList<String> nodes, String obrigatory) {
		BitSet set = new BitSet();
		for (String string : nodes) {
			set.set(posArray.get(getNode(string)));
		}
		Integer obrigatoryIndex = null;
		if(obrigatory != null)
			obrigatoryIndex = posArray.get(getNode(obrigatory));
		
		LinkedList<String> max = new LinkedList<>();  
		LinkedList<BitSet> bipartitions = getAllBipartitions();
		for (BitSet bipart : bipartitions) { 
			if(obrigatoryIndex == null || bipart.get(obrigatoryIndex)) {
				//System.out.println("ok");
				BitSet or = (BitSet)bipart.clone();
				or.or(set);
				if(or.equals(set) && max.size() < bipart.cardinality()) {
					max = new LinkedList<>();
					for (String string : nodes) {
						if(bipart.get(posArray.get(getNode(string))))
							max.add(string);
					}
				}
	
			}
		}
		return max;
	}
	
	public LinkedList<String> eMast(PhylogeneticTree phStandard, Collection<String> mast, Dictionary dic) {
		HashMap<String, String> map = new HashMap<>();
		LinkedList<String> result = new LinkedList<>();
		for (String key : mast) {
			Node<String, DistanceAttribute> node = getNode(key);
			EdgeAttribute<DistanceAttribute> edge = node.getEdges().iterator().next();
			if(edge.getAttributes()[0].distance == 0) {
				Node<String, DistanceAttribute> nodeFather = edge.diff(node);
				LinkedList<String> subtree = new LinkedList<>();
				for (EdgeAttribute<DistanceAttribute> edgeFather : nodeFather.getEdges()) {
					if(edgeFather.getAttributes()[0].distance == 0) {
						subtree.add(dic.getGeneById(edgeFather.diff(nodeFather).getKey()).getOrganism().getAbbrev());
						map.put(dic.getGeneById(edgeFather.diff(nodeFather).getKey()).getOrganism().getAbbrev(), edgeFather.diff(nodeFather).getKey());
					}
				}
				//LinkedList<String> maxSubTree = phStandard.maxSubTree(subtree, dic.getGeneById(key).getOrganism().getAbbrev());
				//System.out.println(subtree.size());
				LinkedList<String> maxSubTree = phStandard.maxSubTree(subtree, null);
				//System.out.println(key + "\t" + maxSubTree.size());
				if(!maxSubTree.isEmpty())
					for (String string : maxSubTree) {
						result.add(map.get(string));						
					}
				else
					result.add(key);
			}
			else {
				result.add(node.getKey());
			}
		}
		return result;
		
	}
}




