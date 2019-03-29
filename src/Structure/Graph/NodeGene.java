package Structure.Graph;

import Structure.Registry.GeneRegistry;
import Structure.Restriction.Attribute;

public class NodeGene<KEY, ATTRIBUTE extends Attribute<ATTRIBUTE>> extends Node<KEY, ATTRIBUTE>{
	private GeneRegistry gene = null;
	
	public NodeGene(KEY key) {
		super(key);
	}
	
	public NodeGene(KEY key, String label) {
		super(key, label);
	}
	
	public NodeGene(KEY key, GeneRegistry gene) {
		super(key);
		this.gene = gene;
	}
	
	public NodeGene(KEY key, String label, GeneRegistry gene) {
		super(key, label);
		this.gene = gene;
	}
	
	public GeneRegistry getGene() {
		return gene;
	}

	@Override
	public NodeGene<KEY, ATTRIBUTE> clone() {
		return new NodeGene<KEY, ATTRIBUTE>(getKey(), getLabel(), getGene());
	}
}