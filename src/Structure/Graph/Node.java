package Structure.Graph;

import java.util.Collection;
import java.util.Set;

import Structure.Restriction.Attribute;
import gnu.trove.map.hash.THashMap;

public class Node<KEY, ATTRIBUTE extends Attribute<ATTRIBUTE>> {
	private static int ids = 0;
	private int id;
	private KEY key;
	private String label;
	
	protected THashMap<KEY, EdgeAttribute<ATTRIBUTE>> edges = new THashMap<KEY, EdgeAttribute<ATTRIBUTE>>(4, (float)0.8);
	
	public Node(KEY key) {
		this.key = key;
		id = ids++;
	}
	
	public Node(KEY key, String label) {
		this.key = key;
		id = ids++;
		this.label = label;
	}
	
	public int getId() {
		return id;
	}
	
	public KEY getKey() {
		return key;
	}
	
	public String getLabel() {
		return label;
	}
	
	@Override
	public Node<KEY, ATTRIBUTE> clone() {
		return new Node<KEY, ATTRIBUTE>(key, label);
	}
	
	/*public EdgeAttribute<ATTRIBUTE> newEdge() {
		return new EdgeAttribute<ATTRIBUTE>();
	}*/
		
	public EdgeAttribute<ATTRIBUTE> getEdge(KEY key) {
		return edges.get(key);
	}
	
	public void setEdge(KEY key, EdgeAttribute<ATTRIBUTE> edge) {
		edges.put(key, edge);
	}
	
	public Set<KEY> getNeighbors() {
		return edges.keySet();
	}
	
	public Collection<? extends EdgeAttribute<ATTRIBUTE>> getEdges() {
		return edges.values();
	}
	
	@SuppressWarnings("unchecked")
	public EdgeAttribute<ATTRIBUTE>[] getEdgesArray() {
		Collection<? extends EdgeAttribute<ATTRIBUTE>> edgesSet = getEdges();
		EdgeAttribute<ATTRIBUTE>[] result = new EdgeAttribute[edgesSet.size()];
		int i = 0;
		for (EdgeAttribute<ATTRIBUTE> edge : edgesSet) {
			result[i] = edge;
			i++;
		}
		
		return result;
	}
	
	public void remove(KEY k) {
		edges.remove(k);
	}
	
	public String toString() {
		return key.toString();
	}
}