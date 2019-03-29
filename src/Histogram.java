

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import Structure.Graph.EdgeAttribute;
import Structure.Graph.GraphM8;
import Structure.Graph.NodeGene;
import Structure.Restriction.M8Attribute;

public class Histogram {
	public interface EdgeField <T> {
		public T getValue(M8Attribute[] attr);
	}
		
	public static <T extends Number> LinkedList<Entry<T, Integer>> edge(GraphM8 graph, EdgeField<T> f) {
		HashMap<T, Integer> map = new HashMap<>();
		
		for (NodeGene<String, M8Attribute> node : graph.getNodes()) {
			for (EdgeAttribute<M8Attribute> edge : node.getEdges()) {
				if(node.getId() < edge.diff(node).getId()) {
					T value = f.getValue(edge.getAttributes());
					if(!map.containsKey(value))
						map.put(value, 1);
					map.put(value, map.get(value) + 1);
				}
			}
		}
		LinkedList<Entry<T, Integer>> list = new LinkedList<>(map.entrySet());
		
		list.sort(new Comparator<Entry<T, Integer>>() {
			@Override
			public int compare(Entry<T, Integer> o1, Entry<T, Integer> o2) {
				T v1 = o1.getKey();
				T v2 = o2.getKey();
				if(v1 instanceof Float && v2 instanceof Float)
					return ((Float)v1.floatValue()).compareTo(v2.floatValue());
				if(v1 instanceof Double && v2 instanceof Double)
					return ((Double)v1.doubleValue()).compareTo(v2.doubleValue());
				if(v1 instanceof Integer && v2 instanceof Integer)
					return ((Integer)v1.intValue()).compareTo(v2.intValue());
				return 0;
			}
		});
		
		return list;
	}
	
	public interface NodeField <T> {
		public T getValue(NodeGene<String, M8Attribute> node);
	}

	public static <T extends Number> LinkedList<Entry<T, Integer>> node(GraphM8 graph, NodeField<T> f) {
		HashMap<T, Integer> map = new HashMap<>();
		
		for (NodeGene<String, M8Attribute> node : graph.getNodes()) {
			T value = f.getValue(node);
			if(!map.containsKey(value))
				map.put(value, 1);
			map.put(value, map.get(value) + 1);
		}
		LinkedList<Entry<T, Integer>> list = new LinkedList<>(map.entrySet());
		
		list.sort(new Comparator<Entry<T, Integer>>() {
			@Override
			public int compare(Entry<T, Integer> o1, Entry<T, Integer> o2) {
				T v1 = o1.getKey();
				T v2 = o2.getKey();
				if(v1 instanceof Float && v2 instanceof Float)
					return ((Float)v1.floatValue()).compareTo(v2.floatValue());
				if(v1 instanceof Double && v2 instanceof Double)
					return ((Double)v1.doubleValue()).compareTo(v2.doubleValue());
				if(v1 instanceof Integer && v2 instanceof Integer)
					return ((Integer)v1.intValue()).compareTo(v2.intValue());
				return 0;
			}
		});
		
		return list;
	}

	
	
	public interface ComponentField <T> {
		public T getValue(GraphM8 component);
	}

	public static <T extends Number> LinkedList<Entry<T, Integer>> component(GraphM8 graph, LinkedList<NodeGene<String, M8Attribute>> heads, int threads, ComponentField<T> f) {
		HashMap<T, Integer> map = new HashMap<>();
		ConcurrentLinkedDeque<LinkedList<NodeGene<String, M8Attribute>>> listHeads = new ConcurrentLinkedDeque<>(GraphM8.makeBatchs(threads*3, 0, heads));
		ExecutorService executor = Executors.newFixedThreadPool(threads);
		for (int i = 0; i < threads; i++) {
			executor.execute(() -> {
				HashMap<T, Integer> localMap = new HashMap<>();
				while(!listHeads.isEmpty()) {
					for (NodeGene<String, M8Attribute> head : listHeads.poll()) {
						T value = f.getValue(graph.connComponentGraph(head));
						if(!localMap.containsKey(value))
							localMap.put(value, 1);
						localMap.put(value, localMap.get(value) + 1);
					}
				}
				synchronized (map) {
					for (Entry<T, Integer> ent : localMap.entrySet()) {
						if(map.containsKey(ent.getKey()))
							map.put(ent.getKey(), map.get(ent.getKey()) + ent.getValue());
						else
							map.put(ent.getKey(), ent.getValue());
					}
				}
			});
		}
		executor.shutdown();
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		
		LinkedList<Entry<T, Integer>> list = new LinkedList<>(map.entrySet());
		list.sort(new Comparator<Entry<T, Integer>>() {
			@Override
			public int compare(Entry<T, Integer> o1, Entry<T, Integer> o2) {
				T v1 = o1.getKey();
				T v2 = o2.getKey();
				if(v1 instanceof Float && v2 instanceof Float)
					return ((Float)v1.floatValue()).compareTo(v2.floatValue());
				if(v1 instanceof Double && v2 instanceof Double)
					return ((Double)v1.doubleValue()).compareTo(v2.doubleValue());
				if(v1 instanceof Integer && v2 instanceof Integer)
					return ((Integer)v1.intValue()).compareTo(v2.intValue());
				return 0;
			}
		});
		return list;
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	
	
	
	
	public static int[] histIdent(GraphM8 graph, int start, int end, int step) {
		int vet[] = new int[(end-start)/step + 1];
		
		for (NodeGene<String, M8Attribute> node : graph.getNodes()) {
			for (EdgeAttribute<M8Attribute> edge : node.getEdges()) {
				if(node.getId() < edge.diff(node).getId()) {
					for (M8Attribute attr : edge.getAttributes()) {
						vet[(int)(attr.getPercIdentity() - start)/step]++;
					}
				}
			}
		}
		return vet;
	}
	
	public static LinkedList<Entry<Float, Integer>> histIdent2(GraphM8 graph) {
		HashMap<Float, Integer> map = new HashMap<>();
		
		for (NodeGene<String, M8Attribute> node : graph.getNodes()) {
			for (EdgeAttribute<M8Attribute> edge : node.getEdges()) {
				if(node.getId() < edge.diff(node).getId()) {
					for (M8Attribute attr : edge.getAttributes()) {
						if(!map.containsKey(attr.getPercIdentity()))
							map.put(attr.getPercIdentity(), 1);
						map.put(attr.getPercIdentity(), map.get(attr.getPercIdentity()) + 1);
					}
				}
			}
		}
		LinkedList<Entry<Float, Integer>> list = new LinkedList<>(map.entrySet());
		
		list.sort(new Comparator<Entry<Float, Integer>>() {
			@Override
			public int compare(Entry<Float, Integer> o1, Entry<Float, Integer> o2) {
				float diff = o1.getKey() - o2.getKey();
				if(diff < 0)
					return -1;
				if(diff > 0)
					return 1;
				return 0;
			}
		});
		
		return list;
	}
	
	public static int[] histMis(GraphM8 graph, int start, int end, int step) {
		int vet[] = new int[(end-start)/step + 1];
		
		for (NodeGene<String, M8Attribute> node : graph.getNodes()) {
			for (EdgeAttribute<M8Attribute> edge : node.getEdges()) {
				if(node.getId() < edge.diff(node).getId()) {
					for (M8Attribute attr : edge.getAttributes()) {
						vet[(int)(attr.getPercMistmatches() - start)/step]++;
					}
				}
			}
		}
		return vet;
	}
	
	public static int[] histAlin(GraphM8 graph, int start, int end, int step) {
		int vet[] = new int[(end-start)/step + 1];
		
		for (NodeGene<String, M8Attribute> node : graph.getNodes()) {
			for (EdgeAttribute<M8Attribute> edge : node.getEdges()) {
				if(node.getId() < edge.diff(node).getId()) {
					for (M8Attribute attr : edge.getAttributes()) {
						vet[(int)(attr.getPercLengthAlign() - start)/step]++;
					}
				}
			}
		}
		return vet;
	}

	public static int[] histEvalue(GraphM8 graph, int start, int end, int step) {
		int vet[] = new int[(end-start)/step + 1];
		
		for (NodeGene<String, M8Attribute> node : graph.getNodes()) {
			for (EdgeAttribute<M8Attribute> edge : node.getEdges()) {
				if(node.getId() < edge.diff(node).getId()) {
					for (M8Attribute atr : edge.getAttributes()) {
						int a = 200;
						if(atr.getEValue() != 0) {
							double x = -Math.log10(atr.getEValue());
							a = (int)x;
							if(x%10 > 0)
								a++;
						}
						vet[(int)(a - start)/step]++;
					}
				}
			}
		}
		return vet;
	}
}
