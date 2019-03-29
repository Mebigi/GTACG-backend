package Structure.Graph;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import Structure.Registry.Dictionary;
import Structure.Registry.GeneRegistry;
import Structure.Registry.GeneRegistryRandom;
import Structure.Registry.OrganismRegistry;
import Structure.Registry.Registry;
import Structure.Restriction.DistanceAttribute;

public class GraphMCL extends GraphGenes <String, DistanceAttribute, NodeGene<String, DistanceAttribute>> {
	private GraphMCL() {
		super(false);
	}
	
	@Override
	public GraphMCL newGraph() {
		return new GraphMCL();
	}
	
	public GraphMCL(File inMCL, Dictionary dic) throws FileNotFoundException {
		super(false);
		
		Scanner sc = new Scanner(inMCL);
		while(sc.hasNextLine()) {
			String line = sc.nextLine();
			String split[] = line.split("\t");
			NodeGene<String, DistanceAttribute> nodeA = new NodeGene<String, DistanceAttribute>(split[0], dic.getGeneByKey(split[0]));
			addNode(nodeA);
			for (int i = 1; i < split.length; i++) {
				NodeGene<String, DistanceAttribute> nodeB = new NodeGene<String, DistanceAttribute>(split[i], dic.getGeneByKey(split[i]));
				addNode(nodeB);
				addEdge(nodeA, nodeB);
			}
		}
		sc.close();
		
		for (OrganismRegistry org : dic.getOrganisms()) {
			for (GeneRegistry gene : org.aminos) {
				if(getNode(gene.getKey()) == null)
				addNode(new NodeGene<String, DistanceAttribute>(gene.getKey()));
			}
		}

	}
}
