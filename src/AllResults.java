

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.TreeSet;

//import org.rosuda.JRI.Rengine;

import HeaderExtractor.HeaderExtractorPatric;
import Structure.GeneFamily;
import Structure.Graph.GraphGenes;
import Structure.Graph.NodeGene;
import Structure.Restriction.Attribute;

public class AllResults {
	/*public static <KEY, ATTRIBUTE extends Attribute<ATTRIBUTE>, NODE extends NodeGene<KEY, ATTRIBUTE>, GRAPH extends GraphGenes<KEY, ATTRIBUTE, NODE>> void make(GRAPH graph, Dictionary dic, HeaderExtractorPatric ext, File folder, Rengine re) throws IOException, InterruptedException {
		OrganismRegistry[] orgs = dic.getOrganisms();
		LinkedList<NODE> heads = graph.connComponentHeads();
		
		System.out.println("AAA " + Calendar.getInstance().getTime());
		gerarDifLabels(graph, heads, ext, new File(folder.getAbsoluteFile() + File.separator + "difLabels"));
		
		System.out.println("BBB " + Calendar.getInstance().getTime());
		DistanceMatrix md = new DistanceMatrix(graph, heads, orgs, Distance.Euclidean, true);
		if(re != null) md.multiScalling(new PrintStream(new File(folder.getAbsoluteFile() + File.separator + "distMatrizEuclidianaBinariaMultiScalling.r")), re, orgs, null, false);
		md.printMatrix(new PrintStream(new File(folder.getAbsoluteFile() + File.separator + "distMatrizEuclidianaBinaria")));
		Phylip.makeTree(md, TreeMethod.Neighbor, new File(folder.getAbsoluteFile() + File.separator + "distMatrizEuclidianaBinariaNeighbor.tre"));
		Phylip.makeTree(md, TreeMethod.UPGMA, new File(folder.getAbsoluteFile() + File.separator + "distMatrizEuclidianaBinariaUPGMA.tre"));
		FigTree.printTree(new File(folder.getAbsoluteFile() + File.separator + "distMatrizEuclidianaBinariaNeighbor.tre"), false, new File(folder.getAbsoluteFile() + File.separator + "distMatrizEuclidianaBinariaNeighbor.pdf"));
		FigTree.printTree(new File(folder.getAbsoluteFile() + File.separator + "distMatrizEuclidianaBinariaUPGMA.tre"), false, new File(folder.getAbsoluteFile() + File.separator + "distMatrizEuclidianaBinariaUPGMA.pdf"));
		
		System.out.println("CCC " + Calendar.getInstance().getTime());
		md = new DistanceMatrix(graph, heads, orgs, Distance.Euclidean, false);
		if(re != null) md.multiScalling(new PrintStream(new File(folder.getAbsoluteFile() + File.separator + "distMatrizEuclidianaNaoBinariaMultiScalling.r")), re, orgs, null, false);
		md.printMatrix(new PrintStream(new File(folder.getAbsoluteFile() + File.separator + "distMatrizEuclidianaNaoBinaria")));
		Phylip.makeTree(md, TreeMethod.Neighbor, new File(folder.getAbsoluteFile() + File.separator + "distMatrizEuclidianaNaoBinariaNeighbor.tre"));
		Phylip.makeTree(md, TreeMethod.UPGMA, new File(folder.getAbsoluteFile() + File.separator + "distMatrizEuclidianaNaoBinariaUPGMA.tre"));
		FigTree.printTree(new File(folder.getAbsoluteFile() + File.separator + "distMatrizEuclidianaNaoBinariaNeighbor.tre"), false, new File(folder.getAbsoluteFile() + File.separator + "distMatrizEuclidianaNaoBinariaNeighbor.pdf"));
		FigTree.printTree(new File(folder.getAbsoluteFile() + File.separator + "distMatrizEuclidianaNaoBinariaUPGMA.tre"), false, new File(folder.getAbsoluteFile() + File.separator + "distMatrizEuclidianaNaoBinariaUPGMA.pdf"));
		
		System.out.println("DDD " + Calendar.getInstance().getTime());
		md = new DistanceMatrix(graph, heads, orgs, Distance.Manhattan, true);
		if(re != null) md.multiScalling(new PrintStream(new File(folder.getAbsoluteFile() + File.separator + "distMatrizManhattanBinariaMultiScalling.r")), re, orgs, null, false);
		md.printMatrix(new PrintStream(new File(folder.getAbsoluteFile() + File.separator + "distMatrizManhattanBinaria")));
		Phylip.makeTree(md, TreeMethod.Neighbor, new File(folder.getAbsoluteFile() + File.separator + "distMatrizManhattanBinariaNeighbor.tre"));
		Phylip.makeTree(md, TreeMethod.UPGMA, new File(folder.getAbsoluteFile() + File.separator + "distMatrizManhattanBinariaUPGMA.tre"));
		FigTree.printTree(new File(folder.getAbsoluteFile() + File.separator + "distMatrizManhattanBinariaNeighbor.tre"), false, new File(folder.getAbsoluteFile() + File.separator + "distMatrizManhattanBinariaNeighbor.pdf"));
		FigTree.printTree(new File(folder.getAbsoluteFile() + File.separator + "distMatrizManhattanBinariaUPGMA.tre"), false, new File(folder.getAbsoluteFile() + File.separator + "distMatrizManhattanBinariaUPGMA.pdf"));
		
		System.out.println("EEE " + Calendar.getInstance().getTime());
		md = new DistanceMatrix(graph, heads, orgs, Distance.Manhattan, false);
		if(re != null) md.multiScalling(new PrintStream(new File(folder.getAbsoluteFile() + File.separator + "distMatrizManhattanNaoBinariaMultiScalling.r")), re, orgs, null, false);
		md.printMatrix(new PrintStream(new File(folder.getAbsoluteFile() + File.separator + "distMatrizManhattanNaoBinaria")));
		Phylip.makeTree(md, TreeMethod.Neighbor, new File(folder.getAbsoluteFile() + File.separator + "distMatrizManhattanNaoBinariaNeighbor.tre"));
		Phylip.makeTree(md, TreeMethod.UPGMA, new File(folder.getAbsoluteFile() + File.separator + "distMatrizManhattanNaoBinariaUPGMA.tre"));
		FigTree.printTree(new File(folder.getAbsoluteFile() + File.separator + "distMatrizManhattanNaoBinariaNeighbor.tre"), false, new File(folder.getAbsoluteFile() + File.separator + "distMatrizManhattanNaoBinariaNeighbor.pdf"));
		FigTree.printTree(new File(folder.getAbsoluteFile() + File.separator + "distMatrizManhattanNaoBinariaUPGMA.tre"), false, new File(folder.getAbsoluteFile() + File.separator + "distMatrizManhattanNaoBinariaUPGMA.pdf"));
		
		System.out.println("FFF " + Calendar.getInstance().getTime());
		GenesMatrix mg = new GenesMatrix(graph, heads, orgs, ext);
		mg.export(new PrintListBinary(), new PrintStream(new File(folder.getAbsoluteFile() + File.separator + "geneMatriz")));
		mg.exportPhylip(new PrintListBinary(), new PrintStream(new File(folder.getAbsoluteFile() + File.separator + "geneMatrizPhylip")));
		System.out.println("GGG " + Calendar.getInstance().getTime());
		if(re != null) {
			PCAMatrix pca = mg.getPCA(null, re);
			pca.plot(new PrintStream(new File(folder.getAbsoluteFile() + File.separator + "geneMatrizPCA.r")), re, null, false);
		}
		Phylip.makeTree(mg, new File(folder.getAbsoluteFile() + File.separator + "geneMatrizPars.tre"));
		FigTree.printTree(new File(folder.getAbsoluteFile() + File.separator + "geneMatrizPars.tre"), false, new File(folder.getAbsoluteFile() + File.separator + "geneMatrizPars.pdf"));
		
		System.out.println("HHH " + Calendar.getInstance().getTime());
		md = GCContent.getDistanceMatrix(orgs);
		Phylip.makeTree(md, TreeMethod.UPGMA, new File(folder.getAbsoluteFile() + File.separator + "distMatrizGC.tre"));
		FigTree.printTree(new File(folder.getAbsoluteFile() + File.separator + "distMatrizGC.tre"), false, new File(folder.getAbsoluteFile() + File.separator + "distMatrizGC.pdf"));

		System.out.println("III " + Calendar.getInstance().getTime());
		md = KMer.matrizDistanciaEuclidiana(orgs, 6);
		Phylip.makeTree(md, TreeMethod.UPGMA, new File(folder.getAbsoluteFile() + File.separator + "distMatrizKmer6.tre"));
		FigTree.printTree(new File(folder.getAbsoluteFile() + File.separator + "distMatrizKmer6.tre"), false, new File(folder.getAbsoluteFile() + File.separator + "distMatrizKmer6.pdf"));
		
		System.out.println("JJJ " + Calendar.getInstance().getTime());
		statistics(graph, heads, orgs.length, new File(folder.getAbsoluteFile() + File.separator + "statistics"));
	}*/
	
	public static <KEY, ATTRIBUTE extends Attribute<ATTRIBUTE>, NODE extends NodeGene<KEY, ATTRIBUTE>, GRAPH extends GraphGenes<KEY, ATTRIBUTE, NODE>> void gerarDifLabels(GRAPH graph, LinkedList<NODE> heads, HeaderExtractorPatric ext, File out) throws FileNotFoundException {
		double totalCoefSame = 0;
		double totalCoefDiff = 0;
		
		int totalSameFam = 0;
		int totalDiffFam = 0;
		int totalSameNodes = 0;
		int totalDiffNodes = 0;
		
		StringBuilder same = new StringBuilder();
		StringBuilder diff = new StringBuilder();
		for (NODE head : heads) {
			LinkedList<NODE> comp = graph.connComponentList(head);
			TreeSet<String> setLabels = new TreeSet<>();
			for (NODE no : comp) {
				String string = ext.getDescription(no.getGene()).toUpperCase();
				if(string.indexOf("FIG") == 0 && string.contains(":"))
					string = string.substring(string.indexOf(":") + 2);
				if(string.indexOf("(") > 0)
					string = string.substring(0, string.indexOf("(") - 1);
				setLabels.add(string);
			}
			if(setLabels.size() == 1 || (setLabels.contains("HYPOTHETICAL PROTEIN") && setLabels.size() == 2)) {
				if(comp.size() > 1) {
					totalSameFam++;
					totalSameNodes += comp.size();
					
					double coef = 0;
					for (NODE node : comp) {
						if(node.getEdges().size() > 1)
							coef += graph.clusterCoef(node);
					}
					
					totalCoefSame += coef/comp.size();
					same.append(comp.size() + "\t" + head.getKey() + "\t" + graph.connComponentGraph(head).avgClusterCoef() + "\n");
					setLabels.remove("HYPOTHETICAL PROTEIN");
					if(setLabels.isEmpty())
						same.append("\tHYPOTHETICAL PROTEIN\n");
					else
						same.append("\t" + setLabels.first() + "\n");
				}
			}
			else {
				totalDiffFam++;
				totalDiffNodes += comp.size();
				totalCoefDiff += graph.connComponentGraph(head).avgClusterCoef();
				diff.append(comp.size() + "\t" + head.getKey() + "\t" + graph.connComponentGraph(head).avgClusterCoef() + "\n");
				for (String string : setLabels) {
					diff.append("\t" + string + "\n");
				}
			}
		}
		PrintStream stream = new PrintStream(out);
		stream.println("# NUMERO DE FAMILIAS\t" + heads.size());
		stream.println("# NUMERO DE IGUAIS\t" + totalSameFam + "\t" + totalSameNodes + "\t" + (totalCoefSame/totalSameFam));
		stream.println("# NUMERO DE DIFERENCAS\t" + totalDiffFam + "\t" + totalDiffNodes + "\t" + (totalCoefDiff/totalDiffFam));
		stream.println();
		stream.println(same.toString());
		/*for (String string : same) {
			stream.println(string);
		}*/
		stream.println("-----------------------------------------------------------");
		stream.println(diff.toString());
		/*for (String string : diff) {
			stream.println(string);
		}*/
		
		stream.close();
	}
	
	public static <KEY, ATTRIBUTE extends Attribute<ATTRIBUTE>, NODE extends NodeGene<KEY, ATTRIBUTE>, GRAPH extends GraphGenes<KEY, ATTRIBUTE, NODE>> void statistics(GRAPH graph, LinkedList<NODE> heads, int numOrgs, File out) throws InterruptedException, IOException {
		LinkedList<Integer> compSize = new LinkedList<>();
		LinkedList<Double> coefDist = new LinkedList<>();
		int coreA = 0;
		int coreB = 0;
		int totalFamWithPar = 0;
		int totalFamWithoutPar = 0;
		long totalEdges = 0;
		for (NODE no : graph.getNodes()) {
			totalEdges += no.getEdges().size();
		}
		
		
		
		int compMax = 0;
		int numExcl = 0;
		double totalCoef = 0;
		double maxCoef = 0;
		String maxCoefNo = "";
		
		for (NODE head : heads) {
			LinkedList<NODE> comp = graph.connComponentList(head);
			int size = comp.size();
			
			compSize.add(size);
			
			if(size > compMax)
				compMax = size;
			if(size == 1)
				numExcl++;
			
			if("CP011491.1_00009".equals(head.getKey()))
				System.out.println("sss");
			
			if(size > 2) {
				double coef = 0;
				for (NODE node : comp) {
					if(node.getEdges().size() > 1)
						coef += graph.clusterCoef(node);
				}
				coef /= comp.size();
				
				coefDist.add(coef);
				totalCoef += coef;
				if(maxCoef < coef) {
					maxCoef = coef;
					maxCoefNo = "" + head.getKey();
				}
			}
			
			try {
				new GeneFamily(comp).getMap().size();
			} catch (Exception e) {
				System.err.println(head.getKey());
				e.printStackTrace();
			}
			int famSize = new GeneFamily(comp).getMap().size();
			if(famSize == numOrgs) {
				coreA++;
				if(size == numOrgs)
					coreB++;
			}
			
			if(size == famSize)
				totalFamWithoutPar++;
			else
				totalFamWithPar++;
		}
		
		Collections.sort(coefDist);
		double ant = coefDist.getFirst();
		int total = 0;
		String sCoefDist = "";
		for (Double coef : coefDist) {
			if(coef == ant)
				total++;
			else {
				if(total == 1)
					sCoefDist += ant + ",";
				else
					sCoefDist += "rep(" + ant + ", " + total + "),";
				ant = coef;
				total = 1;
			}
		}
		sCoefDist += "rep(" + ant + ", " + total + ")";
		
		Collections.sort(compSize);
		int ant2 = compSize.getFirst();
		total = 0;
		String sCompSize = "";
		for (Integer comp : compSize) {
			if(comp == ant2)
				total++;
			else {
				if(total == 1)
					sCompSize += ant2 + ",";
				else
					sCompSize += "rep(" + ant2 + ", " + total + "),";
				ant2 = comp;
				total = 1;
			}
		}
		sCompSize += "rep(" + ant2 + ", " + total + ")";

		PrintStream stream = new PrintStream(out);
		stream.println("Numero de nos\t" + graph.getNodes().size());
		stream.println("Numero de arestas\t" + totalEdges);
		stream.println();
		stream.println("Numero de familias\t" + heads.size());
		stream.println("Tamanho maximo\t" + compMax);
		stream.println("Tamanho medio\t" + ((double)graph.getNodes().size()/heads.size()));
		stream.println("Numero de exclusivos\t" + numExcl);
		
		stream.println("Total core A\t" + coreA);
		stream.println("Total core B\t" + coreB);
		stream.println("Total fams com paralogos\t" + totalFamWithPar);
		stream.println("Total fams sem paralogos\t" + totalFamWithoutPar);
		
		
		stream.println("distCompSize = c(" + sCompSize + ")");
		stream.println();
		//stream.println("Coeficiente Cluster\t" + graph.clusterCoef());
		//stream.println("Coeficiente Cluster\t" + graph.clusterCoef(, Runtime.getRuntime().availableProcessors()));
		stream.println("Coeficiente Medio\t" + (totalCoef/heads.size()));
		stream.println("Coeficiente maximo\t" + maxCoef + " (" + maxCoefNo + ")");
		stream.println("distCoef = c(" + sCoefDist + ")");
		stream.close();
	}
}
