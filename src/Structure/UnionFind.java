package Structure;

public class UnionFind {
	private int parents[];
	public UnionFind(int n) {
		parents = new int[n];
		for (int i = 0; i < parents.length; i++) {
			parents[i] = i;
		}
	}
	
	public int find(int i) {
		if(i == parents[i])
			return i;
		parents[i] = find(parents[i]);
		return parents[i];
	}
	
	public void union(int i, int j) {
		parents[i] = find(j);
	}
}
