package Structure.Graph;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import Structure.Registry.Dictionary;
import Structure.Registry.GeneRegistry;
import Structure.Registry.GeneRegistryRandom;
import Structure.Restriction.DistanceAttribute;

public class GrafoCDHit extends GraphGenes<String, DistanceAttribute, NodeGene<String, DistanceAttribute>> {
	private GrafoCDHit() {
		super(false);
	}
	
	public GrafoCDHit(File entrada) throws FileNotFoundException {
		this(entrada, null);
	}
	
	@Override
	public GrafoCDHit newGraph() {
		return new GrafoCDHit();
	}
	
	public GrafoCDHit(File entrada, Dictionary dic) throws FileNotFoundException {
		super(false);
		
		Scanner sc = new Scanner(entrada);
		NodeGene<String, DistanceAttribute> no0 = null;
		while(sc.hasNextLine()) {
			String linha = sc.nextLine();
			if (linha.charAt(0) != '>') {
				if(linha.charAt(0) == '0') {
					String key = linha.substring(linha.indexOf('>') + 1, linha.indexOf("... "));
					GeneRegistry gene = null;
					if(dic != null) {
						gene = dic.getGeneByKey(key);
					}
					no0 = new NodeGene<String, DistanceAttribute>(key, gene);
					addNode(no0);
				}
				else {
					String key = linha.substring(linha.indexOf('>') + 1, linha.indexOf("... "));
					GeneRegistry gene = null;
					if(dic != null) {
						gene = dic.getGeneByKey(key);
					}
					NodeGene<String, DistanceAttribute> novo = new NodeGene<String, DistanceAttribute>(key, gene);
					addNode(novo);
					
					addEdge(no0, novo);
				}
			}
		}
		sc.close();
	}
}
