

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.GZIPOutputStream;

import HeaderExtractor.HeaderExtractor;
import HeaderExtractor.HeaderExtractorPatric;
import Metrics.KMer;
import Structure.Alignment;
import Structure.Constants;
import Structure.GeneFamily;
import Structure.Graph.EdgeAttribute;
import Structure.Graph.GraphM8;
import Structure.Graph.Node;
import Structure.Graph.NodeGene;
import Structure.Graph.PhylogeneticTree;
import Structure.Graph.Graphics.Point;
import Structure.Matrix.DistanceMatrix;
import Structure.Matrix.GenesMatrix;
import Structure.Registry.Dictionary;
import Structure.Registry.DictionaryGbf;
import Structure.Registry.DictionaryGbf.Format;
import Structure.Registry.GeneRegistry;
import Structure.Registry.GeneRegistryFna;
import Structure.Registry.OrganismRegistry;
import Structure.Registry.RegistryGroup;
import Structure.Registry.RegistryGroups;
import Structure.Restriction.DistanceAttribute;
import Structure.Restriction.M8Attribute;
import ToolkitFile.AlignmentFile;
import ToolkitFile.FastaFile;
import ToolkitFile.ToolkitBaseFile;
import ToolkitFile.ToolkitBaseFile.FileType;
import ToolkitFile.ToolkitBaseFile.SequenceType;
import ToolkitFile.TreeFile;
import Wrapper.Clann;
import Wrapper.Clann.Criterion;
import Wrapper.Clann.Hs;
import Wrapper.Clann.Swap;
import Wrapper.ClustalO;
import Wrapper.Phylip;
import Wrapper.R;
import gnu.trove.map.hash.THashMap;
import Wrapper.Phylip.ConsenseTreeMethod;
import Wrapper.Phylip.TreeMethod;

public class ExportReport {
	public static String standardNewick = null;
	public static <T> String printHistogram(String funcName, LinkedList<Entry<T, Integer>> hist, PrintStream js) {
		js.print(funcName + "=[['Value', 'Frequence'],");
		for (Entry<T, Integer> entry : hist) {
			js.print("[" + entry.getKey() + "," + entry.getValue() + "],");
		}
		js.println("];");
		return "";
	}
	
	public static void printFamilyGenomes(File out, OrganismRegistry orgs [], RegistryGroups gs[]) throws FileNotFoundException {
		PrintStream jsFamily = new PrintStream(out);
		jsFamily.println("var gs = [");
		for (int i = 0; i < gs.length; i++) {
			jsFamily.println("  {name:'" + gs[i].getName() + "', childs:[");
			for (int j = 0; j < gs[i].getGroups().length; j++) {
				Color color = gs[i].getGroups()[j].getColor();
				jsFamily.println("    {name:'" + gs[i].getGroups()[j].getName() + "', color:'" + String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue()) + "'},");
			}
			jsFamily.println("  ]},");
		}
		jsFamily.println("];");
		jsFamily.println("var genomes = [");
		for (int i = 0; i < orgs.length; i++) {
			jsFamily.print("  {name:'" + orgs[i].getName() + "',abbrev:'" + orgs[i].getAbbrev() + "', rg:[");
			for (int j = 0; j < gs.length; j++) {
				jsFamily.print(gs[j].getGroup(orgs[i]).getId() + ",");
			}
			jsFamily.println("]},");
		}
		jsFamily.println("];");		
		jsFamily.close();
	}
	
	public static void printOutputWeka(File out, GraphM8 graph, LinkedList<NodeGene<String, M8Attribute>> heads, OrganismRegistry orgs [], RegistryGroups gs[], HashMap<NodeGene<String, M8Attribute>, Struct> treeMap) throws FileNotFoundException {
		PrintStream stream = new PrintStream(out);
		stream.println("@RELATION GENOMES");
		stream.println();
		stream.println("@ATTRIBUTE GENOME STRING");
		StringBuilder strings[] = new StringBuilder[orgs.length];
		for (int i = 0; i < orgs.length; i++) {
			strings[i] = new StringBuilder();
			strings[i].append(orgs[i].getAbbrev());
		}
		for (NodeGene<String, M8Attribute> head : heads) {
			stream.println("@ATTRIBUTE " + head.getKey() + " NUMERIC");
			GeneFamily fam = treeMap.get(head).fam;
			int[] vet = fam.getArrayGenesCount(orgs);
			for (int i = 0; i < vet.length; i++) {
				//strings[i].append((vet[i]>0?"1":"0") + ",");
				strings[i].append("," + (vet[i]>0?"1":"0"));
			}
		}
		for (int i = 0; i < gs.length; i++) {
			stream.print("@ATTRIBUTE " + gs[i].getName() + " {");
			for (int j = 0; j < gs[i].getGroups().length; j++) {
				stream.print(gs[i].getGroups()[j].getName().replace(" ", "_") + (j < gs[i].getGroups().length -1?",":""));
			}
			stream.println("}");
		}
		
		stream.println();
		stream.println("@DATA");
		for (int i = 0; i < orgs.length; i++) {
			for (int j = 0; j < gs.length; j++) {
				try {
					strings[i].append("," + gs[j].getGroup(orgs[i]).getName().replace(" ", "_"));
					
				} catch (Exception e) {
					System.err.println(i);
					System.err.println(j);
					System.err.println(orgs[i].getFile());
					System.err.println(gs[j].getGroup(orgs[i]));
					System.err.println(gs[j].getGroup(orgs[i]).getName());
				}
			}
			stream.println(strings[i].toString());
			
		}
		stream.close();
	}
	
	public static void printFamilyWeka(File file, GeneFamily fam, AlignmentFile alignFile, RegistryGroups gs[]) throws FileNotFoundException {
		if(!alignFile.exists())
			return;
		
		Alignment align = new Alignment(alignFile);
		
		PrintStream stream = new PrintStream(file);
		stream.println("@RELATION FAMILY");
		stream.println();
		stream.println("@ATTRIBUTE FAMILY STRING");
		for (int i = 0; i < align.size(); i++) {
			stream.println("@ATTRIBUTE c" + i + " STRING");
		}
		for (int i = 0; i < gs.length; i++) {
			stream.println("@ATTRIBUTE " + gs[i].getName().replace(" ", "_") + " STRING");
		}
		stream.println();
		stream.println("@DATA");
		for (Entry<OrganismRegistry, LinkedList<GeneRegistry>> ent : fam.getMap().entrySet()) {
			OrganismRegistry genome = ent.getKey();
			for (GeneRegistry gene : ent.getValue()) {
				stream.print(gene.getKey());
				String seq = align.getSequence(gene.getKey()).toString();
				for (int i = 0; i < seq.length(); i++) {
					stream.print("," + Constants.acidToInt[seq.charAt(i)]);
				}
				for (int i = 0; i < gs.length; i++) {
					stream.print("," + gs[i].getGroup(genome).getName().replace(" ", "_"));
				}
				stream.println();
			}
		}
		
		
		stream.close();
		
	}
	
	public static void printStatistics(File out, GraphM8 graph, OrganismRegistry orgs [], RegistryGroups gs[], LinkedList<NodeGene<String, M8Attribute>> heads, HashMap<NodeGene<String, M8Attribute>, Struct> treeMap, int threads) throws FileNotFoundException {
		PrintStream jsStats = new PrintStream(out);
		printHistogram("statIdent", Histogram.edge(graph, (attrs)-> {Double max = 0.0; for (M8Attribute attr : attrs) max = Math.max(attr.getPercIdentity(), max); return max;}), jsStats);
		printHistogram("statBitscore", Histogram.edge(graph, (attrs)-> {Double max = 0.0; for (M8Attribute attr : attrs) max = Math.max(attr.getBitScore(), max); return Math.round((double)max/100)*100;}), jsStats);
		printHistogram("statEValue", Histogram.edge(graph, (attrs)-> {Integer max = 0; for (M8Attribute attr : attrs) max = Math.max((int)-Math.log10(attr.getEValue()), max); return Math.min(max, 180);}), jsStats);
		printHistogram("statGapOpening", Histogram.edge(graph, (attrs)-> {Short max = (short)0; for (M8Attribute attr : attrs) max = (short) Math.max(attr.getGapOpenings(), max); return max;}), jsStats);
		printHistogram("statLenAlign", Histogram.edge(graph, (attrs)-> {Double max = 0.0; for (M8Attribute attr : attrs) max = Math.max(attr.getPercLengthAlign()*100, max); return max;}), jsStats);
		printHistogram("statMismatch", Histogram.edge(graph, (attrs)-> {Double max = 0.0; for (M8Attribute attr : attrs) max = Math.max(attr.getPercMistmatches()*100, max); return max;}), jsStats);
		
		printHistogram("statClusterCoeff", Histogram.node(graph, (node)-> {return (double)Math.round(100*(double)graph.clusterCoef(node))/100;}), jsStats);
		printHistogram("statNumEdge", Histogram.node(graph, (node)-> {return (double)Math.round(node.getEdges().size()/10)*10;}), jsStats);
		printHistogram("statHSP", Histogram.node(graph, (node)-> {int size = 0; for (EdgeAttribute<M8Attribute> edge : node.getEdges()) size += edge.getAttributes().length; return Math.round(size/10)*10;}), jsStats);
		printHistogram("statGeneLen", Histogram.node(graph, (node)-> {return node.getGene().getLengthByteSeq();}), jsStats);
		
		printHistogram("statFamClusterCoeff", Histogram.component(graph, heads, threads, (component)-> {return component.avgClusterCoef();}), jsStats);
		printHistogram("statFamNumSeqs", Histogram.component(graph, heads, threads, (component)-> {return component.getNodes().size();}), jsStats);
		printHistogram("statFamNumGenomes", Histogram.component(graph, heads, threads, (component)-> {return new GeneFamily(component.getNodes()).numGenomes();}), jsStats);

		HashMap<String, Integer> genomeHomo = new HashMap<>();
		HashMap<String, Integer> genomeDoms = new HashMap<>();
		HashMap<String, Integer> genomeDomsAll = new HashMap<>();
		HashMap<String, Integer> genomeOrthos = new HashMap<>();
		HashMap<String, Integer> genomeOrthosAll = new HashMap<>();
		LinkedList<HashMap<String, Integer>> configHomo = new LinkedList<>();
		LinkedList<HashMap<String, Integer>> configDoms = new LinkedList<>();
		LinkedList<HashMap<String, Integer>> configDomsAll = new LinkedList<>();
		LinkedList<HashMap<String, Integer>> configOrthos = new LinkedList<>();
		LinkedList<HashMap<String, Integer>> configOrthosAll = new LinkedList<>();
		for (int i = 0; i < gs.length; i++) {
			configHomo.add(new HashMap<>());
			configDoms.add(new HashMap<>());
			configDomsAll.add(new HashMap<>());
			configOrthos.add(new HashMap<>());
			configOrthosAll.add(new HashMap<>());
		}
		
		for (Struct struct : treeMap.values()) {
			for (int i = 0; i < gs.length; i++) {
				String sArray = "";
				int[] array = struct.fam.getArrayGenesCount(gs[i], true);
				for (int j = 0; j < array.length; j++) {
					if(array[j] == gs[i].getGroups()[j].size())
						sArray += "C";
					else if(array[j] > 0)
						sArray += "1";
					else
						sArray += "0";
				}
				//System.out.println(sArray);
				Integer value = configHomo.get(i).get(sArray);
				if(value == null)
					value = 1;
				else
					value += 1;
				configHomo.get(i).put(sArray, value);
				
				if(struct.orthologs.isEmpty()) {
					value = configOrthosAll.get(i).get(sArray);
					if(value == null)
						value = 1;
					else
						value += 1;
					configOrthosAll.get(i).put(sArray, value);
				}
				
				if(struct.domains.isEmpty()) {
					value = configDomsAll.get(i).get(sArray);
					if(value == null)
						value = 1;
					else
						value += 1;
					configDomsAll.get(i).put(sArray, value);
				}
				
			}
			String cod = GeneFamily.vet(struct.fam.getArrayGenesBoolean(orgs));
			if(genomeHomo.containsKey(cod))
				genomeHomo.put(cod, genomeHomo.get(cod) + 1);
			else
				genomeHomo.put(cod, 1);
			
			if(struct.orthologs.isEmpty()) {
				if(genomeOrthosAll.containsKey(cod))
					genomeOrthosAll.put(cod, genomeOrthosAll.get(cod) + 1);
				else
					genomeOrthosAll.put(cod, 1);
			}
			
			if(struct.domains.isEmpty()) {
				if(genomeDomsAll.containsKey(cod))
					genomeDomsAll.put(cod, genomeDomsAll.get(cod) + 1);
				else
					genomeDomsAll.put(cod, 1);
			}
		}
		
		for (Struct struct : treeMap.values()) {
			for (Struct dom : struct.domains) {
				GeneFamily fam = dom.fam;
				for (int i = 0; i < gs.length; i++) {
					String sArray = "";
					int[] array = fam.getArrayGenesCount(gs[i], true);
					for (int j = 0; j < array.length; j++) {
						if(array[j] == gs[i].getGroups()[j].size())
							sArray += "C";
						else if(array[j] > 0)
							sArray += "1";
						else
							sArray += "0";
					}
					Integer value = configDoms.get(i).get(sArray);
					if(value == null)
						value = 1;
					else
						value += 1;
					configDoms.get(i).put(sArray, value);
					
				}
				String cod = GeneFamily.vet(fam.getArrayGenesBoolean(orgs));
				if(genomeDoms.containsKey(cod))
					genomeDoms.put(cod, genomeDoms.get(cod) + 1);
				else
					genomeDoms.put(cod, 1);
			}
		}
		
		for (Struct struct : treeMap.values()) {
			for (Struct ortho : struct.orthologs) {
				GeneFamily fam = ortho.fam;
				for (int i = 0; i < gs.length; i++) {
					String sArray = "";
					int[] array = fam.getArrayGenesCount(gs[i], true);
					for (int j = 0; j < array.length; j++) {
						if(array[j] == gs[i].getGroups()[j].size())
							sArray += "C";
						else if(array[j] > 0)
							sArray += "1";
						else
							sArray += "0";
					}
					//System.out.println(sArray);
					Integer value = configOrthos.get(i).get(sArray);
					if(value == null)
						value = 1;
					else
						value += 1;
					configOrthos.get(i).put(sArray, value);
				}
				String cod = GeneFamily.vet(fam.getArrayGenesBoolean(orgs));
				if(genomeOrthos.containsKey(cod))
					genomeOrthos.put(cod, genomeOrthos.get(cod) + 1);
				else
					genomeOrthos.put(cod, 1);
			}
		}
		
		jsStats.println("statGenome={homo:[");
		for (Entry<String, Integer> ent : genomeHomo.entrySet()) {
			jsStats.println("  {cod:'" + ent.getKey() + "',total:" + ent.getValue() + ",all:'f'},");
		}
		jsStats.println("], doms:[");
		for (Entry<String, Integer> ent : genomeDoms.entrySet()) {
			jsStats.println("  {cod:'" + ent.getKey() + "',total:" + ent.getValue() + ",all:'f'},");
		}
		for (Entry<String, Integer> ent : genomeDomsAll.entrySet()) {
			jsStats.println("  {cod:'" + ent.getKey() + "',total:" + ent.getValue() + ",all:'t'},");
		}
		jsStats.println("], orthos:[");
		for (Entry<String, Integer> ent : genomeOrthos.entrySet()) {
			jsStats.println("  {cod:'" + ent.getKey() + "',total:" + ent.getValue() + ",all:'f'},");
		}
		for (Entry<String, Integer> ent : genomeOrthosAll.entrySet()) {
			jsStats.println("  {cod:'" + ent.getKey() + "',total:" + ent.getValue() + ",all:'t'},");
		}
		jsStats.println("]};");

		jsStats.println("statRg={homo:[");
		for (int i = 0; i < gs.length; i++) {
			jsStats.println("[");
			for (Entry<String, Integer> ent : configHomo.get(i).entrySet()) {
				jsStats.print("  {");
				for (int j = 0; j < ent.getKey().length(); j++) {
					jsStats.print("c" + j + ":'");
					if(ent.getKey().charAt(j) == 'C')
						jsStats.print("Yes(Core)");
					else if(ent.getKey().charAt(j) == '1')
						jsStats.print("Yes");
					else if(ent.getKey().charAt(j) == '0')
						jsStats.print("No");
					else
							jsStats.print(ent.getKey().charAt(j));
					jsStats.print("',");
				}
				jsStats.println("all:'f',total: " + ent.getValue() + "},");
			}
			jsStats.println("],");
		}
		jsStats.println("], doms:[");
		for (int i = 0; i < gs.length; i++) {
			jsStats.println("[");
			for (Entry<String, Integer> ent : configDomsAll.get(i).entrySet()) {
				jsStats.print("  {");
				for (int j = 0; j < ent.getKey().length(); j++) {
					jsStats.print("c" + j + ":'");
					if(ent.getKey().charAt(j) == 'C')
						jsStats.print("Yes(Core)");
					else if(ent.getKey().charAt(j) == '1')
						jsStats.print("Yes");
					else if(ent.getKey().charAt(j) == '0')
						jsStats.print("No");
					else
							jsStats.print(ent.getKey().charAt(j));
					jsStats.print("',");
				}
				jsStats.println("all:'t', total: " + ent.getValue() + "},");
			}
			
			for (Entry<String, Integer> ent : configDoms.get(i).entrySet()) {
				jsStats.print("  {");
				for (int j = 0; j < ent.getKey().length(); j++) {
					jsStats.print("c" + j + ":'");
					if(ent.getKey().charAt(j) == 'C')
						jsStats.print("Yes(Core)");
					else if(ent.getKey().charAt(j) == '1')
						jsStats.print("Yes");
					else if(ent.getKey().charAt(j) == '0')
						jsStats.print("No");
					else
							jsStats.print(ent.getKey().charAt(j));
					jsStats.print("',");
				}
				jsStats.println("all:'f', total: " + ent.getValue() + "},");
			}
			jsStats.println("],");
		}
		jsStats.println("], orthos:[");
		for (int i = 0; i < gs.length; i++) {
			jsStats.println("[");
			for (Entry<String, Integer> ent : configOrthosAll.get(i).entrySet()) {
				jsStats.print("  {");
				for (int j = 0; j < ent.getKey().length(); j++) {
					jsStats.print("c" + j + ":'");
					if(ent.getKey().charAt(j) == 'C')
						jsStats.print("Yes(Core)");
					else if(ent.getKey().charAt(j) == '1')
						jsStats.print("Yes");
					else if(ent.getKey().charAt(j) == '0')
						jsStats.print("No");
					else
							jsStats.print(ent.getKey().charAt(j));
					jsStats.print("',");
				}
				jsStats.println("all:'t',total: " + ent.getValue() + "},");
			}
			
			for (Entry<String, Integer> ent : configOrthos.get(i).entrySet()) {
				jsStats.print("  {");
				for (int j = 0; j < ent.getKey().length(); j++) {
					jsStats.print("c" + j + ":'");
					if(ent.getKey().charAt(j) == 'C')
						jsStats.print("Yes(Core)");
					else if(ent.getKey().charAt(j) == '1')
						jsStats.print("Yes");
					else if(ent.getKey().charAt(j) == '0')
						jsStats.print("No");
					else
							jsStats.print(ent.getKey().charAt(j));
					jsStats.print("',");
				}
				jsStats.println("all:'f',total: " + ent.getValue() + "},");
			}
			jsStats.println("],");
		}
		jsStats.println("]};");
		jsStats.println();
		
		BitSet core = new BitSet(orgs.length);
		core.set(0, orgs.length-1);
		
		HashMap<String, Integer> genomeAll = new HashMap<>(genomeHomo);
		for (Entry<String, Integer> ent : genomeDoms.entrySet()) {
			if(!genomeAll.containsKey(ent.getKey()))
				genomeAll.put(ent.getKey(), ent.getValue());
		}
		for (Entry<String, Integer> ent : genomeOrthos.entrySet()) {
			if(!genomeAll.containsKey(ent.getKey()))
				genomeAll.put(ent.getKey(), ent.getValue());
		}
		
		

		
		String mask = "0";
		for (int j = 0; j < orgs.length; j++) {
			mask += "0";
		}
		
		jsStats.println("bestSoluctions = [");
		Random rand = new Random();
		for (int i = 0; i < gs.length; i++) {
			jsStats.println("  [");
			for (int j = 0; j < gs[i].getGroups().length; j++) {
				BitSet base = new BitSet(orgs.length);
				for (int k = 0; k < orgs.length; k++) {
					if(gs[i].getGroups()[j].contain(orgs[k]))
						base.set(k);
				}
				
				LinkedList<Obj> objs = new LinkedList<>();
				for (Entry<String, Integer> ent : genomeHomo.entrySet()) {
					String cod = ent.getKey();
					Obj obj = new Obj();
					for (int k = 0; k < cod.length(); k++) {
						if(cod.charAt(k) == '1')
							obj.set.set(k);
					}
					BitSet tmp = (BitSet)obj.set.clone();
					tmp.and(base);
					if(tmp.equals(base)) {
						obj.total = ent.getKey().replace("0", "").length();
						obj.freq = ent.getValue();
						objs.add(obj);
					}
				}
				
				objs.sort(new Comparator<Obj>() {
					@Override
					public int compare(Obj arg0, Obj arg1) {
						return arg0.total - arg1.total;
					}
				});
				
				Obj[] array = objs.toArray(new Obj[1]);

				
				
				
				
				
				
				
				
				
				
				jsStats.println("    [");
				bestSolutionForGs(core, orgs, gs[i].getGroups()[j], genomeHomo, array, base, new TreeSet<>());
				LinkedList<LinkedList<Integer>> bestSolution = new LinkedList<>(); 
				TreeSet<Integer> elements = new TreeSet<>();
				for (int k = 0; k < 128; k++) {
					TreeSet<Integer> ignorados = new TreeSet<>();
					for (Integer integer : elements) {
						if(rand.nextInt(2) == 0)
							ignorados.add(integer);
					}
					
					bestSolution.addAll(bestSolutionForGs(core, orgs, gs[i].getGroups()[j], genomeHomo, array, base, ignorados));
					for (LinkedList<Integer> list : bestSolution) {
						for (Integer integer : list) {
							elements.add(integer);
						}
					}
				}
				
				for (LinkedList<Integer> list : bestSolution) {
					list.sort(new Comparator<Integer>() {
						@Override
						public int compare(Integer o1, Integer o2) {
							return o1-o2;
						}
					});
				}
				
				bestSolution.sort(new Comparator<LinkedList<Integer>>() {
					@Override
					public int compare(LinkedList<Integer> o1, LinkedList<Integer> o2) {
						Iterator<Integer> it1 = o1.iterator();
						Iterator<Integer> it2 = o2.iterator();
						while (it1.hasNext() && it2.hasNext()) {
							Integer i1 = (Integer)it1.next();
							Integer i2 = (Integer)it2.next();
							if(i1-i2 != 0)
								return i1-i2;
						}
						if(it2.hasNext())
							return -1;
						if(it1.hasNext())
							return 1;
						return 0;
					}
				});
				
				LinkedList<LinkedList<Integer>> semRep = new LinkedList<>();
				LinkedList<Integer> ant = null;
				for (LinkedList<Integer> list : bestSolution) {
					if(ant != null) {
						if(!sameList(ant, list))
							semRep.add(list);
					}
					else
						semRep.add(list);
					ant = list;
				}
				
				bestSolution = semRep;
				
				bestSolution.sort(new Comparator<LinkedList<Integer>>() {
					@Override
					public int compare(LinkedList<Integer> o1, LinkedList<Integer> o2) {
						return o1.size()-o2.size();
					}
				});
				
				LinkedList<LinkedList<LinkedList<Integer>>> finalSolution = new LinkedList<>();
				for (LinkedList<Integer> list : bestSolution) {
					LinkedList<LinkedList<Integer>> newList = new LinkedList<>();
					for (Integer integer : list) {
						LinkedList<Integer> tmp = new LinkedList<>(); 
						tmp.add(integer);
						newList.add(tmp);
					}
					finalSolution.add(newList);
				}
				
				boolean altered = true;
				LinkedList<LinkedList<LinkedList<Integer>>> finalSolution2 = new LinkedList<>();
				while(altered) {
					altered = false;
					for (LinkedList<LinkedList<Integer>> list : finalSolution) {
						boolean added = false;
						for (LinkedList<LinkedList<Integer>> finalList : finalSolution2) {
							LinkedList<LinkedList<Integer>> lcs = lcsList(finalList, list);
							if(lcs.size() == finalList.size()-1) {
								LinkedList<Integer> diff1 = null;
								Iterator<LinkedList<Integer>> it1 = finalList.iterator();
								Iterator<LinkedList<Integer>> it2 = lcs.iterator();
								while (it1.hasNext() && it2.hasNext()) {
									LinkedList<Integer> i1 = (LinkedList<Integer>)it1.next();
									LinkedList<Integer> i2 = (LinkedList<Integer>)it2.next();
									if(i1 != i2)
										diff1 = i1;
								}
								if(it1.hasNext())
									diff1 = it1.next();
								
								LinkedList<Integer> diff2 = null;
								it1 = list.iterator();
								it2 = lcs.iterator();
								while (it1.hasNext() && it2.hasNext()) {
									LinkedList<Integer> i1 = (LinkedList<Integer>)it1.next();
									LinkedList<Integer> i2 = (LinkedList<Integer>)it2.next();
									if(!sameList(i1, i2))
										diff2 = i1;
								}
								if(it1.hasNext())
									diff2 = it1.next();
								
								diff1.addAll(diff2);
								TreeSet<Integer> set = new TreeSet<>();
								set.addAll(diff1);
								diff1.clear();
								diff1.addAll(set);
								added = true;
								altered = true;
								break;
							}
						}
						if(!added)
							finalSolution2.add(list);
					}
					finalSolution = finalSolution2;
					finalSolution2 = new LinkedList<>();
				}
				
				finalSolution.sort(new Comparator<LinkedList<LinkedList<Integer>>>() {
					@Override
					public int compare(LinkedList<LinkedList<Integer>> o1, LinkedList<LinkedList<Integer>> o2) {
						return o1.size()-o2.size();
					}
				});
				
				int freq = 0;
				HashMap<Integer, Integer> fGlobal = new HashMap<>();
				HashMap<LinkedList<LinkedList<Integer>>, HashMap<Integer, Integer>> fLocal = new HashMap<>();
				for (LinkedList<LinkedList<Integer>> list : finalSolution) {
					fLocal.put(list, new HashMap<>());
					int freqLocal = 1;
					for (LinkedList<Integer> list2 : list) {
						freqLocal *= list2.size();
					}
					freq += freqLocal;
				}				
				for (LinkedList<LinkedList<Integer>> list : finalSolution) {
					int freqLocal = 1;
					for (LinkedList<Integer> list2 : list) {
						freqLocal *= list2.size();
					}
					for (LinkedList<Integer> list2 : list) {
						for (Integer integer : list2) {
							Integer x = fGlobal.get(integer);
							if(x == null)
								x = 0;
							x += freqLocal / list2.size();
							fLocal.get(list).put(integer, freqLocal / list2.size());
							fGlobal.put(integer, x);
						}
					}
				}
				
				
				for (LinkedList<LinkedList<Integer>> list1 : finalSolution) {
					jsStats.print("      [");
					for (LinkedList<Integer> list2 : list1) {
						jsStats.print("[");
						for (Integer integer : list2) {
							//jsStats.print("'" + (printBitset(array[integer].set)+mask).substring(0,orgs.length) + "',");
							jsStats.print("{cod:'" + (printBitset(array[integer].set)+mask).substring(0,orgs.length) + "',pl:" + fLocal.get(list1).get(integer) + ",pg:" + (fGlobal.get(integer)/freq) + "},");
							//jsStats.print("{cod:'" + integer + "',pl:" + fLocal.get(list1).get(integer) + ",pg:" + fGlobal.get(integer) + "},");
						}
						jsStats.print("],");
					}
					jsStats.println("],");
				}
				jsStats.println("    ],");
			}
			jsStats.println("  ],");
		}
		jsStats.println("];");
		jsStats.close();
	}
	
	public static LinkedList<LinkedList<Integer>> lcsList(LinkedList<LinkedList<Integer>> l1, LinkedList<LinkedList<Integer>> l2) {
		int m = l1.size();
		int n = l2.size();
		int [][] matrix = new int[m+1][n+1];
		char [][] path = new char[m+1][n+1]; 
		for (int i = 0; i <= m; i++) {
			for (int j = 0; j <= n; j++) {
				if(i==0 || j==0) {
					matrix[i][j] = 0;
					path[i][j] = '0';
				}
				else if(sameList(l1.get(i-1), l2.get(j-1))) {
					matrix[i][j] = 1 + matrix[i-1][j-1];
					path[i][j] = 'D';
				}
				else if(matrix[i-1][j] > matrix[i][j-1]) {
					matrix[i][j] = matrix[i-1][j];
					path[i][j] = 'x';
				}
				else {
					matrix[i][j] = matrix[i][j-1];
					path[i][j] = 'y';
				}
			}
		}
		
		LinkedList<LinkedList<Integer>> result = new LinkedList<>();
		int posX = m;
		int posY = n;
		while(posX >= 1 && posY >= 1) {
			if(path[posX][posY] == 'D') {
				result.addFirst(l1.get(posX-1));
				posX--;
				posY--;
			}
			else if(path[posX][posY] == 'x') {
				posX = posX -1;
			}
			else if(path[posX][posY] == 'y') {
				posY = posY -1;
			}
			
		}
		return result;
	}
	
	
	public static int diffList(LinkedList<LinkedList<Integer>> l1, LinkedList<LinkedList<Integer>> l2) {
		int diffs = 0;
		Iterator<LinkedList<Integer>> it1 = l1.iterator();
		Iterator<LinkedList<Integer>> it2 = l2.iterator();
		while (it1.hasNext() && it2.hasNext()) {
			LinkedList<Integer> i1 = (LinkedList<Integer>)it1.next();
			LinkedList<Integer> i2 = (LinkedList<Integer>)it2.next();
			if(i1.size()-i2.size() != 0)
				diffs++;
			else if(!sameList(i1, i2))
				diffs++;
		}
		while (it1.hasNext()) {
			it1.next();
			diffs++;
		}
		while (it2.hasNext()) {
			it2.next();
			diffs++;
		}
		return diffs;
	}
	
	public static boolean sameList(LinkedList<Integer> l1, LinkedList<Integer> l2) {
		if(l1.size() != l2.size())
			return false;
		
		Iterator<Integer> it1 = l1.iterator();
		Iterator<Integer> it2 = l2.iterator();
		while (it1.hasNext() && it2.hasNext()) {
			Integer i1 = (Integer)it1.next();
			Integer i2 = (Integer)it2.next();
			if(i1-i2 != 0)
				return false;
		}
		return true;
	}
	
	static class Obj2 {
		public int id;
	}
	
	static class Obj {
		public BitSet set = new BitSet();
		public int total = 0;
		public int freq = 0;
	}
	
	public static LinkedList<LinkedList<Integer>> bestSolutionForGs(BitSet core, OrganismRegistry[] orgs, RegistryGroup group, HashMap<String, Integer> genomes, Obj[] array, BitSet base, TreeSet<Integer> ignorados) {
		
		//TreeSet<Integer> ignorados = new TreeSet<>();
		LinkedList<LinkedList<Integer>> results = new LinkedList<>();
		LinkedList<Integer> next = new LinkedList<>();
		while(true) {
			while(!next.isEmpty() && !ignorados.add(next.pollLast()));
			
			BitSet[] accumF = new BitSet[array.length];
			BitSet[] accumB = new BitSet[array.length];
			
			if(ignorados.contains(0))
				accumF[0] = (BitSet)core.clone();
			else
				accumF[0] = (BitSet)array[0].set.clone();
			
			int pos = 1;
			LinkedList<Integer> valid = new LinkedList<>();
			if(!accumF[0].equals(base)) {
				boolean equal = false;
				while(!equal && pos < array.length) {
					if(ignorados.contains(pos))
						accumF[pos] = accumF[pos-1];
					else {
						accumF[pos] = (BitSet)array[pos].set.clone();
						accumF[pos].and(accumF[pos-1]);
						equal = accumF[pos].equals(base);
					}
					pos++;
				}
				if(pos == array.length) {
					//System.out.println(printBitset(accumF[pos-1]));
					return results;
				}
				
				valid.add(pos-1);
				accumB[pos-1] = (BitSet)array[pos-1].set.clone();
				for (int i = pos-2; i > 0; i--) {
					accumB[i] = (BitSet)accumB[i+1].clone();
					
					if(!ignorados.contains(i)) {
						BitSet tmp = (BitSet)accumF[i-1].clone();
						tmp.and(accumB[i+1]);
						if(!tmp.equals(base)) {
							valid.add(i);
							accumB[i].and(array[i].set);
						}
					}
				}
				if(!accumB[1].equals(base))
					valid.add(0);
			}
			else
				valid.add(0);
			
			
			
			next.addAll(valid);
			next.sort(new Comparator<Integer>() {
				@Override
				public int compare(Integer arg0, Integer arg1) {
					return arg0 - arg1;
				}
			});

			LinkedList<Integer> result = new LinkedList<>();
			while(!valid.isEmpty()) {
				int i = valid.pollLast();
				//result.add((printBitset(array[i].set) + mask).substring(0,orgs.length));
				result.add(i);
			}
			results.add(result);
		}
	}
	
	public static String printBitset(BitSet set) {
		if(set == null)
			return "NA";
		String s = "";
		for (int i = 0; i < set.length(); i++) {
			s += set.get(i)?"1":"0";
		}
		return s;
	}
	
	public static int id = 0;
	public static String printFamily(File familiesFolder, GraphM8 graph, Dictionary dic, OrganismRegistry orgs[], RegistryGroups gs[], NodeGene<String, M8Attribute> head, Struct struct, HeaderExtractor ext, String type, Integer typeSuperId, Integer typeId) throws IOException, InterruptedException {
		String fileName = head.getKey();
		if(typeId != null) {
			if(type.equals("domain"))
				fileName = head.getKey() + ".d" + typeId;
			else if(typeSuperId == null)
				fileName = head.getKey() + ".o" + typeId;
			else
				fileName = head.getKey() + ".o" + typeSuperId + ".d" + typeId;
		}		
		
		Alignment align = null;
		if(struct.align.exists()) {
			align = new Alignment(struct.align);
		}
			
		PhylogeneticTree ph = null;
		String canonical = null;

		double treeDist = -1;
		int tnodes = -1;
		TreeSet<String> mast = null;
		TreeSet<String> emast = null;
		
		if(struct.tree.exists()) {
			String newick = struct.tree.load();
			if(newick != null && newick.length() > 1) {
				ph = new PhylogeneticTree(newick, false);
				ph.simplify(-1);
				
				tnodes = ph.getNodes().size() - ph.getLeaves().size();
				
				HashMap<OrganismRegistry, Integer> mapOrgs = new HashMap<>();
				for (int i = 0; i < orgs.length; i++) {
					mapOrgs.put(orgs[i], i);
				}

				HashMap<String, String> mapMast = new HashMap<>();
				HashMap<String, String> mapCanonical = new HashMap<>();
				for (Entry<OrganismRegistry, LinkedList<GeneRegistry>> ent : struct.fam.getMap().entrySet()) {
					for (GeneRegistry gene : ent.getValue()) {
						mapMast.put(gene.getKey(), ""+gene.getOrganism().getAbbrev());
						mapCanonical.put(gene.getKey(), ""+mapOrgs.get(gene.getOrganism().getRoot()));
					}  
				}
				
				PhylogeneticTree phCanonical = new PhylogeneticTree(ph.print(false), false);
				try {
					canonical = phCanonical.getCanonical(mapCanonical);
					
				} catch (Exception e) {
					System.out.println("erro");
					canonical = phCanonical.getCanonical(mapCanonical);
				}
				
				if(struct.fam.numGenomes() == struct.fam.numGenes() && (standardNewick != null && standardNewick.length() >0)) {
					String newickMast = ph.print(mapMast, false); 
					treeDist = R.treeDist(standardNewick, newickMast, false);
					
					PhylogeneticTree phStandard = new PhylogeneticTree(standardNewick, false);
					
					String mastTree = R.mast(newickMast, standardNewick, false);
					if(!"".equals(newickMast)) {
						mast = new TreeSet<>();
						if(mastTree.length() > 0) {
							PhylogeneticTree phMast = new PhylogeneticTree(mastTree, false);
							THashMap<OrganismRegistry, LinkedList<GeneRegistry>> famMast = struct.fam.getMap();
							for (Node<String, DistanceAttribute> node : phMast.setLeaves) {
								GeneRegistry gene = famMast.get(dic.getOrgByAbbrev(node.getKey())).getFirst();
								mast.add(gene.getKey());
							}
							
							emast = new TreeSet<>(ph.eMast(phStandard, mast, dic));
						}
						else
							emast = new TreeSet<>();
					}
				}
			}
		}
		
		String diversity = "";
		if(align != null)
			diversity = "divers:" + align.diversity() + ",";
		String coef = "";
		LinkedList<NodeGene<String, M8Attribute>> nodes = new LinkedList<>();
		for (GeneRegistry gene : struct.fam.getGenes()) {
			nodes.add(graph.getNode(gene.getKey()));
		}
		GraphM8 sub = graph.subGraph(nodes);
		if(sub != null)
			coef = "coef:" + sub.avgClusterCoef() + ",";
		
		int numGenes = struct.fam.numGenes();
		int numGenomes = struct.fam.numGenomes();
		int numParalogs = numGenes - numGenomes;
		String func = struct.fam.getAllDescriptions(ext);
		
		String result ="";
		try {
			result = 
					"{id:" + id++ + "," + 
							"key:\"" + head.getKey() + "\"," +
							"func:\"" + func.replace("\n", "<br>") + "\"," +
							"numFunc:" + (func.length() - func.replace("\n", "").length() + 1) + "," +
							coef +
							"seqs:" + numGenes + "," + 
							"gens:" + numGenomes + "," + 
							"paras:" + numParalogs + "," +
							(tnodes!=-1?"tnodes:" + tnodes + ",":"") +
							(treeDist!=-1?"tdist:" + treeDist + ",":"") +
							(mast!=null?"mast:" + mast.size() + ",":"") +
							(emast!=null?"emast:" + emast.size() + ",":"") +
							diversity;			
		} catch (Exception e) {
			System.out.println(fileName);
			for (Entry<OrganismRegistry, LinkedList<GeneRegistry>> ent : struct.fam.getMap().entrySet()) {
				for (GeneRegistry gene : ent.getValue()) {
					System.out.println(gene.get());					
				}
				
			}
			e.printStackTrace();
		}
		LinkedList<GeneRegistry> genes = new LinkedList<>();
		for (LinkedList<GeneRegistry> genesList : struct.fam.getMap().values()) {
			genes.addAll(genesList);
		}
		genes.sort(new Comparator<GeneRegistry>() {
			@Override
			public int compare(GeneRegistry o1, GeneRegistry o2) {
				return (int)(o2.getLengthByteSeq() - o1.getLengthByteSeq());
			}
		});
		result += "lens:[";
		for (GeneRegistry gene : genes) {
			result += gene.getLengthByteSeq() + ",";
		}
		result += "],";
		if(struct.fam.numGenomes() == 1)
			result += "excl:\"" + struct.fam.getMap().keySet().iterator().next().getRoot().getAbbrev() + "\",";
		if(struct.domains.size() > 1) {
			if(typeId == null)
				result += makeLinks(head, struct.domains, "doms", "d", "");
			else
				result += makeLinks(head, struct.domains, "doms", "d", ".o" + typeId);
		}
		if(struct.orthologs.size() > 1)
			result += makeLinks(head, struct.orthologs, "orthos", "o", "");
		if(typeId != null) {
			if("domain".equals(type))
				result += "doms:\"<a href=\\\"family.htm?file=" + head.getKey() + ".d" + typeId + "\\\">" + typeId + "</a>\",";
			else
				result += "orthos:\"<a href=\\\"family.htm?file=" + head.getKey() + ".o" + typeId + "\\\">" + typeId + "</a>\",";
		}
		for (int i = 0; i < gs.length; i++)
			result += "rg" + i + ":{" + printGroup(struct.fam, gs[i], struct.align, ph, dic) + "},";
		result += "cod:'";
		boolean[] array = struct.fam.getArrayGenesBoolean(orgs);
		for (int i = 0; i < array.length; i++) {
			result += array[i]?'1':'0';
		}
		result += "'";
		result += ",fam:[";
		for (LinkedList<GeneRegistry> list : struct.fam.getArrayGenes(orgs)) {
			result += "[";
			if(list != null)
				for (GeneRegistry gene : list) {
					result += "'" + gene.getKey() + "',";
				}
			result += "],";
		}
		result += "]";
		if(canonical != null)
			result += ",phylo:'" + canonical + "'";
		result += "},";
		
		
		PrintStream jsFamily = new PrintStream(familiesFolder.getAbsolutePath() + "/" + fileName + ".js");
		jsFamily.println("function getNodes() { return [");
		for (Entry<OrganismRegistry, LinkedList<GeneRegistry>> ent : struct.fam.getMap().entrySet()) {
			for (GeneRegistry gene : ent.getValue()) {
				String geneFnaString = "";
				if(gene instanceof GeneRegistryFna) {
					GeneRegistryFna geneFna = (GeneRegistryFna) gene;
					geneFnaString = 
							"s:" + geneFna.getStart() + "," +
							"e:" + geneFna.getEnd() + "," + 
							"d:" + geneFna.isComplement() + "," +
							"c:\"" + geneFna.getContig() + "\",";
				}
				jsFamily.print(
						"{id:'" + Integer.toString(gene.getId(),32) + "'," + 
						"data:{key:'" + gene.getKey() + "'," + 
						"org:'" + gene.getOrganism().getRoot().getAbbrev() + "'," + 
						"func:'" + ext.getDescription(gene) + "'," + 
						"len:" + gene.getLengthByteSeq() + "," +
						geneFnaString +
						//(gene.getOrganism() != gene.getOrganism().getRoot()?"plasm:' " + gene.getOrganism().getAbbrev() + "',":"")+ 
						"coef:" + sub.clusterCoef(sub.getNode(gene.getKey())) + "," +
						"seq:\"" + (align!= null?align.getSequence(gene.getKey()):gene.get().replace(gene.getHeader(), "").replace("\n", "")) + "\"");
				jsFamily.print(",domains:[");
				if(struct.domains != null && !struct.domains.isEmpty()) {
					int i = 1;
					for (Struct dom : struct.domains) {
						if(dom.fam.getMap().get(gene.getOrganism().getRoot()) != null && dom.fam.getMap().get(gene.getOrganism().getRoot()).contains(gene))
							jsFamily.print(i + ",");
						i++;
					}
				}
				jsFamily.print("]");
				jsFamily.print(",orthologs:[");
				if(struct.orthologs != null && !struct.orthologs.isEmpty()) {
					int i = 1;
					for (Struct ortho : struct.orthologs) {
						if(ortho.fam.getMap().get(gene.getOrganism().getRoot()) != null && ortho.fam.getMap().get(gene.getOrganism().getRoot()).contains(gene))
							jsFamily.print(i + ",");
						i++;
					}
				}
				jsFamily.print("]");
				for (int i = 0; i < gs.length; i++) {
					Color color = gs[i].getGroup(gene.getOrganism().getRoot()).getColor();
					jsFamily.print(",rg" + i + ":{id: " + gs[i].getGroup(gene.getOrganism().getRoot()).getId() + ", name:'" + gs[i].getGroup(gene.getOrganism().getRoot()).getName() + "',color: '#" + String.format("%02x%02x%02x", + color.getRed(), color.getGreen(), color.getBlue()) + "'}");
				}
				jsFamily.println("}},");
			}
		}
		jsFamily.println("];}");
		jsFamily.println();
		
		if(sub != null) {
			jsFamily.println("function getEdges() { return [");
			for (NodeGene<String, M8Attribute> nodeA : sub.getNodes()) {
				for (EdgeAttribute<M8Attribute> edge : nodeA.getEdges()) {
					NodeGene<String, M8Attribute> nodeB = edge.diff(nodeA);
					if(nodeA.getId() < nodeB.getId()) {
						jsFamily.println(
								"{s:'" + Integer.toString(nodeA.getGene().getId(),32) + "'," + 
								"t:'" + Integer.toString(nodeB.getGene().getId(),32) + "'," + 
								"data:" + minEvalue(edge) + "},");
					}
				}
			}
			jsFamily.println("];}"); 
		}
		
		jsFamily.println("function getDataRg() { return [");
		for (int i = 0; i < gs.length; i++) {
			jsFamily.println("[");
			HashMap<RegistryGroup, resultG> hash = getGroup(struct.fam, gs[i], null, ph, dic);
			for (int j = 0; j < gs[i].getGroups().length; j++) {
				Color color = gs[i].getGroups()[j].getColor();
				resultG resultg = hash.get(gs[i].getGroups()[j]);
				if(resultg.numSeqs > 0) {
					jsFamily.print(
							"  {g:\"" + gs[i].getGroups()[j].getName() + "\"," +
							"c1:" + resultg.maxNiche + "," +
							"c2:" + resultg.percNiche + "," +
							"c3:" + resultg.numSeqs + "," +
							"c4:" + resultg.percNumSeqs + "," +
							"c5:" + resultg.numGenomes + "," +
							"c6:" + resultg.percNumGenomes + "," +
							"c7:" + resultg.percNumGenomes2 + "," +
							"c8:" + resultg.dissimilarity + "," +
							"color:" + "\""+String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue()) + "\"},");
				}
			}
			jsFamily.println("],");
		}
		jsFamily.println("];}");
		jsFamily.println("function getDataFields() {return {coeff: '" + sub.clusterCoef() + "'};}");
		jsFamily.println("function getNumDomains() {return " + struct.domains.size() + ";}");
		jsFamily.println("function getNumOrthologs() {return " + struct.orthologs.size() + ";}");
		jsFamily.print("function getMast() {return [");
		if(mast != null)
			for (String key : mast) {
				jsFamily.print("'" + key + "',");
			}
		jsFamily.println("]};");
		jsFamily.print("function getEMast() {return [");
		if(emast != null)
			for (String key : emast) {
				jsFamily.print("'" + key + "',");
			}
		jsFamily.println("]};");
		if(ph != null) {
			jsFamily.print("function getTree() {return '" + ph.print(false) + "';}");
		}
		
		jsFamily.close();
		return result;
	}
	
	private static String makeLinks(NodeGene<String, M8Attribute> head, LinkedList<Struct> sets, String field, String abbrev, String prefix) {
		String s = field + ":\"";
		int i = 1;
		for (Struct set : sets) {
			if(i > 1 && i < sets.size())
				s += ", ";
			else if(i > 1)
				s += " and ";
			s += "<a title=\\\"" + set.fam.numGenes() + "\\\" href=\\\"family.htm?file=" + head.getKey() + prefix + "." + abbrev + "" + (i-1) + "\\\">" + i + "</a>";
			i++;
		}
		s += "\",";
		return s;
	}
	
	public static class resultG {
		public int maxNiche;
		public double percNiche;
		public int numGenomes;
		public double percNumGenomes;
		public double percNumGenomes2;
		public int numSeqs;
		public double percNumSeqs;
		public double dissimilarity;
		
		public resultG(int maxNiche, int numGenomes, int numSeqs, double dissimilarity, int totalSeqs, int totalGenomes, int totalGenomes2) {
			this.maxNiche = maxNiche;
			this.numGenomes = numGenomes;
			this.numSeqs = numSeqs;
			this.dissimilarity = dissimilarity;
			
			percNiche = ((double)100*maxNiche)/numSeqs;
			percNumSeqs = ((double)100*numSeqs)/totalSeqs;
			percNumGenomes = ((double)100*numGenomes)/totalGenomes;
			percNumGenomes2 = ((double)100*numGenomes)/totalGenomes2;
		}
	}
	
	public static String printGroup(GeneFamily fam, RegistryGroups g, AlignmentFile align, PhylogeneticTree ph, Dictionary dic) throws InterruptedException, IOException {
		HashMap<RegistryGroup, resultG> hash = getGroup(fam, g, align, ph, dic);
		DecimalFormat df = new DecimalFormat("0.00");
		
		String result = "";
		for (int i = 0; i < g.getGroups().length; i++) {
			resultG resultg = hash.get(g.getGroups()[i]);
			if(resultg.numSeqs > 0) {
				result = result + "g" + i + ":{" +
						"c1:" + resultg.maxNiche + "," +
						"c2:" + df.format(resultg.percNiche).replace(",", ".") + "," +
						"c3:" + resultg.numSeqs + "," +
						"c4:" + df.format(resultg.percNumSeqs).replace(",", ".") + "," +
						"c5:" + resultg.numGenomes + "," +
						"c6:" + df.format(resultg.percNumGenomes).replace(",", ".") + "," +
						"c7:" + df.format(resultg.percNumGenomes2).replace(",", ".") + "," +
						"c8:" + resultg.dissimilarity + "},";
			}
			else
				result = result + "g" + i + ":{c1:0}," ;
		}
		return result;
	}
	
	public static HashMap<RegistryGroup, resultG> getGroup(GeneFamily fam, RegistryGroups g, AlignmentFile align, PhylogeneticTree ph, Dictionary dic) throws InterruptedException, IOException {
		int max[] = new int[g.size()];
		if(ph != null) {
			HashMap<Node<String, DistanceAttribute>, HashMap<Node<String, DistanceAttribute>, Integer[]>> division = ph.edgeDivisionByGroups(g, dic);
			for (Node<String, DistanceAttribute> k1 : division.keySet()) {
				for (Node<String, DistanceAttribute> k2 : division.get(k1).keySet()) {
					Integer[] vet1 = division.get(k1).get(k2);
					Integer[] vet2 = division.get(k2).get(k1);
					int x1 = PhylogeneticTree.unitary(vet1);
					int x2 = PhylogeneticTree.unitary(vet2);
					
					if(x1 != -1 && vet1[x1] > max[x1])
						max[x1] = vet1[x1];
					
					if(x2 != -1 && vet1[x2] > max[x2])
						max[x2] = vet2[x2];
					
					if(x1 != -1 && x2 != -1 && x1 == x2)
						max[x1] = vet1[x1] + vet2[x2];
				}
			}
		}
		
		HashMap<RegistryGroup, resultG> result = new HashMap<>();
		
		int[] array = fam.getArrayGenesCount(g);
		for (int i = 0; i < array.length; i++) {
			int numGenomes = 0;
			int numSeqs = 0;
			for (OrganismRegistry reg : fam.getMap().keySet()) {
				if(g.getGroups()[i].contain(reg.getRoot())) {
					numGenomes++;
				}
				numSeqs += fam.getMap().get(reg).size();
			}
			
			double diss = 0;
			if(align != null && align.exists()) {
				diss = new Alignment(align).scoreDiffSimple(g.getGroups()[i], dic);
			}
			result.put(g.getGroups()[i], new resultG(max[i], numGenomes, array[i], diss, numSeqs, fam.getMap().size(), g.getGroups()[i].size()));
		}
		return result;
	}
	
	private static String minEvalue(EdgeAttribute<M8Attribute> edge) {
		M8Attribute min = edge.getAttributes()[0];
		for (M8Attribute attr : edge.getAttributes()) {
			if(attr.getEValue() < min.getEValue())
				min = attr;
		}
		return ("{i:" + min.getPercIdentity() + ",e:" + min.getEValue() +",l:" + min.getLengthAlign() + ",lp:" + min.getPercLengthAlign() + ",m:" + min.getPercMistmatches() + ",g:" + min.getGapOpenings() + ",b:" + min.getBitScore() + "}").replace(".0,", ",").replace(".0}", "}");
	}
	
	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
		Constants.inicializacao();
		File graphFile = null;
		File headsFile = null;
		File dicFile = null;
		String dicFormat = null;
		File treesFolder = null;
		File outFolder = null;
		File gsFiles[] = new File[0];
		int threads = 1;
		TreeFile ggdcTree = null;
		TreeFile standardTree = null;
		HeaderExtractorPatric ext = new HeaderExtractorPatric();
		
		boolean printAll = true;
		boolean printPhylogeny = false;
		boolean printWekaArff = false;
		boolean printStatistics = false;
		boolean printFamilies = false;
		boolean printPlot = false;
		boolean printGenomes = false;
		boolean printCoreAlignment = false;
		
		/*args = new String[] {
				"-graph", "/media/caio/08CE4A891258E142/dados/streptococcus/patric/graph41.strep.gz", 
				"-heads", "/media/caio/08CE4A891258E142/dados/streptococcus/patric/heads41.strep",
				"-dic", "/media/caio/08CE4A891258E142/dados/streptococcus/patric/streptococcus2.dic",
				"-dicFormat", "gbf",
				"-trees", "/media/caio/08CE4A891258E142/dados/streptococcus/patric/trees41new2",
				"-threads", "4",
				"-ggdc", "/home/caio/Dropbox/DoutoradoCaioSantiago/resultados/streptococcus/ggdc/arvoreFormula3DDH.tree",
				//"-standardTree", "/home/caio/Dropbox/DoutoradoCaioSantiago/resultados/streptococcus/mProtein/emm_gene.tree",
				"-standardTree", "/home/caio/Dropbox/DoutoradoCaioSantiago/resultados/streptococcus/patric/super.tree",
				"-groups", "4", 
					"/media/caio/08CE4A891258E142/dados/streptococcus/patric/doenca.grupo",
			 		"/media/caio/08CE4A891258E142/dados/streptococcus/patric/genotimoM.grupo",
			 		"/media/caio/08CE4A891258E142/dados/streptococcus/patric/invasividade.grupo",
			 		"/media/caio/08CE4A891258E142/dados/streptococcus/patric/pattern.grupo",
			 	"-printPhylogeny",
			 	//"-printCoreAlignment",
		 		"-outFolder", "reportStrep"
		};
		/**/
		
		/*args = new String[] {
				"-graph", "/media/caio/08CE4A891258E142/dados/xantho15/gb/xantho15.graph.gz", 
				"-heads", "/media/caio/08CE4A891258E142/dados/xantho15/gb/xantho15.heads",
				"-dic", "/media/caio/08CE4A891258E142/dados/xantho15/gb/xantho15.dic", 
				"-trees", "/media/caio/08CE4A891258E142/dados/xantho15/gb/trees",
				"-threads", "4",
		 		"-outFolder", "reportXantho15"
		};*/
		
		/*args = new String[] {
				"-graph", "/home/caio/dados/xyllela/xf.graph50.gz", 
				"-heads", "/home/caio/dados/xyllela/xf.heads50",
				"-dic", "/home/caio/dados/xyllela/xf.dic", 
				"-trees", "/home/caio/dados/xyllela/trees50a",
				"-threads", "1",
				"-groups", "0",
		 		"-outFolder", "reportXf"
		};*/
		
		/*args = new String[] {
				"-graph", "/home/caio/dados/myco/prokka/myco.graph40.gz", 
				"-heads", "/home/caio/dados/myco/prokka/myco.heads40",
				//"-heads", "heads",
				"-dic", "/home/caio/dados/myco/prokka/myco.dic", 
				"-trees", "/home/caio/dados/myco/prokka/trees40",
				"-threads", "4",
				"-groups", "3", 
					"/home/caio/dados/myco/prokka/g1.grupo",
			 		"/home/caio/dados/myco/prokka/g2.grupo",
			 		"/home/caio/dados/myco/prokka/g3.grupo",
		 		//"-printPhylogeny",
			 	"-standardTree", "/home/caio/dados/myco/prokka/standard.tree",
				"-outFolder", "reportMyco"
		};*/
		
		/*args = new String[] {
				"-graph", "/home/caio/dados/xyllela/xy.graph76.gz", 
				"-heads", "/home/caio/dados/xyllela/xy.heads76",
				//"-heads", "heads",
				"-dic", "/home/caio/dados/xyllela/all_genom.dic", 
				"-trees", "/home/caio/dados/xyllela/trees76",
				"-threads", "4",
				"-groups", "3", 
					"/home/caio/dados/xyllela/hospedeiro.grupo",
			 		"/home/caio/dados/xyllela/mlst.grupo",
			 		"/home/caio/dados/xyllela/origem.grupo",
		 		"-printStatistics",
		 		"-printFamilies",
		 		"-printPlot",
				"-outFolder", "reportXy"
		};*/
		
		/*args = new String[] {
				"-graph", "/media/caio/08CE4A891258E142/dados/myco/prokka/myco.graph45.gz", 
				"-heads", "/media/caio/08CE4A891258E142/dados/myco/prokka/myco.heads45",
				//"-heads", "heads",
				"-dic", "/media/caio/08CE4A891258E142/dados/myco/prokka/myco2.dic",
				"-dicFormat", "gbf",
				"-trees", "/media/caio/08CE4A891258E142/dados/myco/prokka/trees45.2",
				"-threads", "4",
				"-groups", "1", 
					"/media/caio/08CE4A891258E142/dados/myco/prokka/degradation.grupo",
		 		//"-printFamilies",
		 		"-printPhylogeny",
		 		"-outFolder", "reportMyco"
		};*/
		
		/*args = new String[] {
				"-graph", "/media/caio/08CE4A891258E142/dados/xyllela/Xfastidiosa.graph45.gz", 
				"-heads", "/media/caio/08CE4A891258E142/dados/xyllela/tmp.heads45",
				//"-heads", "heads",
				"-dic", "/media/caio/08CE4A891258E142/dados/xyllela/genomes.dic",
				//"-dicFormat", "gbf",
				"-trees", "/media/caio/08CE4A891258E142/dados/xyllela/trees45",
				"-threads", "4",
				"-groups", "3", 
					"/media/caio/08CE4A891258E142/dados/xyllela/hospedeiro.grupo",
					"/media/caio/08CE4A891258E142/dados/xyllela/mlst.grupo",
					"/media/caio/08CE4A891258E142/dados/xyllela/origem.grupo",
		 		//"-printFamilies",
		 		"-printCoreAlignment",
		 		"-outFolder", "reportXy"
		};/**/
		
		for (int i = 0; i < args.length; i++) {
			if("-help".equals(args[i])) {
				System.out.println("Input Files");
				System.out.println("\t-graph");
				System.out.println("\t-heads");
				System.out.println("\t-dic");
				System.out.println("\t-groups");
				System.out.println("\t-trees");
				System.out.println("\t[-ggdc]");
				System.out.println("\t[-standardTree]");
				System.out.println();
				System.out.println("Output Files");
				System.out.println("\t-outFolder");
				System.out.println();
				System.out.println("Parameters");
				System.out.println("\t[-threads");
				System.out.println("\t[-printAll]");
				System.out.println("\t[-printPhylogeny]");
				System.out.println("\t[-printWeka]");
				System.out.println("\t[-printStatistics]");
				System.out.println("\t[-printPlot]");
				System.out.println("\t[-printGenomes]");
				System.out.println("\t[-printCoreAlignment]");
				System.out.println("\t[-printFamilies]");
				return;
			}
			if("-graph".equals(args[i])) {
				if(args.length > i + 1)
				graphFile = new File(args[i+1]);
				if(!graphFile.exists()) {
					System.out.println("Graph file not found");
					return;
				}
			}
			if("-heads".equals(args[i])) {
				if(args.length > i + 1)
				headsFile = new File(args[i+1]);
				if(!headsFile.exists()) {
					System.out.println("Heads file not found");
					return;
				}
			}
			else if("-dic".equals(args[i])) {
				if(args.length > i + 1)
				dicFile = new File(args[i+1]);
				if(!dicFile.exists()) {
					System.out.println("Dictionary file not found");
					return;
				}
			}
			else if("-dicFormat".equals(args[i])) {
					if(args.length > i + 1)
						dicFormat = args[i+1];
				}
			else if("-trees".equals(args[i])) {
				if(args.length > i + 1)
				treesFolder = new File(args[i+1]);
				if(!treesFolder.exists()) {
					System.out.println("Trees folder not found");
					return;
				}
			}
			else if("-ggdc".equals(args[i])) {
				if(args.length > i + 1)
				ggdcTree = new TreeFile(args[i+1]);
				if(!ggdcTree.exists()) {
					System.out.println("GGDC tree not found");
					return;
				}
			}
			else if("-standardTree".equals(args[i])) {
				if(args.length > i + 1)
				standardTree = new TreeFile(args[i+1]);
				if(!standardTree.exists()) {
					System.out.println("Standard tree not found");
					return;
				}
			}
			else if("-outFolder".equals(args[i])) {
				if(args.length > i + 1)
				outFolder = new File(args[i+1]);
			}
			else if("-groups".equals(args[i])) {
				int numGroups = 0;
				if(args.length > i + 1)
					numGroups = Integer.parseInt(args[i+1]);
				if(args.length <= i + 1 + numGroups) {
					System.out.println("error");
					return;
				}
				gsFiles = new File[numGroups];
				for (int j = 0; j < numGroups; j++) {
					gsFiles[j] = new File(args[i+2+j]);
				}
				i+= numGroups;
			}
			else if("-threads".equals(args[i])) {
				threads = Integer.parseInt(args[i+1]);
			}
			else if("-printAll".equals(args[i])) {
				printAll = true;
				i--;
			}
			else if("-printPhylogeny".equals(args[i])) {
				printAll = false;
				printPhylogeny = true;
				i--;
			}
			else if("-printWeka".equals(args[i])) {
				printAll = false;
				printWekaArff = true;
				i--;
			}
			else if("-printGenomes".equals(args[i])) {
				printAll = false;
				printGenomes = true;
				i--;
			}
			else if("-printStatistics".equals(args[i])) {
				printAll = false;
				printStatistics = true;
				i--;
			}
			else if("-printFamilies".equals(args[i])) {
				printAll = false;
				printFamilies = true;
				i--;
			}
			else if("-printPlot".equals(args[i])) {
				printAll = false;
				printPlot = true;
				i--;
			}
			else if("-printCoreAlignment".equals(args[i])) { 
				printAll = false; 
				printCoreAlignment = true; 
				i--; 
			}
			i++;
		}
		
		System.setOut(new PrintStream("out"));
		System.setErr(new PrintStream("err"));
		if(standardTree != null && standardTree.exists())
			standardNewick = standardTree.load().replace("\n", "");
		
		Dictionary dicTmp = null;
		if(dicFormat == null || dicFormat.equals("faa"))
			dicTmp = new Dictionary(dicFile);
		else if(dicFormat.equals("gbf"))
			dicTmp = new DictionaryGbf(dicFile, Format.GBF);
		else if (dicFormat.equals("gff"))
			dicTmp = new DictionaryGbf(dicFile, Format.GFF);
		Dictionary dic = dicTmp;
		//System.out.println(dic.getGeneById("fig|64187.249.peg.952"));
		OrganismRegistry[] orgs = dic.getOrganisms();
		RegistryGroups[] gs = new RegistryGroups[gsFiles.length];
		for (int i = 0; i < gs.length; i++) {
			gs[i] = new RegistryGroups(gsFiles[i], dic);
		}

		GraphM8 graph = null;
		if(graphFile.getName().endsWith(".gz"))
			graph = new GraphM8(graphFile, "gz", dic);
		else
			graph = new GraphM8(graphFile, "", dic);
		LinkedList<NodeGene<String, M8Attribute>> heads = graph.loadNodeList(headsFile);

		///graph.plotGraphviz(null, heads, null);
		/*BufferedImage buf = graph.gerarImagem(graph.loadGraphviz(new File("/home/caio/Dropbox/workspace2/framework/saida2.txt")));
		ImageIO.write(buf,"PNG",new File("img.png"));
		if(1==1)
			return;*/

		
		
		
		
		
		
		
		
		HashMap<NodeGene<String, M8Attribute>, Struct> treeMap = ExportTreeFiles.load(treesFolder, graph);
		
		if(!outFolder.exists())
			outFolder.mkdir();
		
		if(printCoreAlignment) printCoreAlignment(new File(outFolder.getAbsolutePath() + "/core_amino.align"), new File(outFolder.getAbsolutePath() + "/core_nucl.align"), orgs, treeMap, threads);
		if(printAll || printPlot) printPlot(new File(outFolder.getAbsolutePath() + "/plot.js"), graph, orgs, treeMap);
		if(printAll || printWekaArff) printOutputWeka(new File(outFolder.getAbsolutePath() + "/weka.arff"), graph, heads, orgs, gs, treeMap);
		if(printAll || printPhylogeny) printPhylogeny(new File(outFolder.getAbsolutePath() + "/phylo.js"), graph, dic, orgs, gs, heads, treeMap, ggdcTree, standardTree);
        if(printAll || printStatistics) printStatistics(new File(outFolder.getAbsolutePath() + "/statistics.js"), graph, orgs, gs, heads, treeMap, threads);
        if(printAll || printFamilies) printSequenceList(new File(outFolder.getAbsolutePath() + "/sequenceList.js"), orgs, graph, heads, treeMap, ext);
        if(printAll || printFamilies) printFamilyGenomes(new File(outFolder.getAbsolutePath() + "/annotation.js"), orgs, gs);
        if(printAll || printGenomes) {
        	File genomesFolder = new File(outFolder.getAbsolutePath() + "/genomes/");
        	if(!genomesFolder.exists())
        		genomesFolder.mkdirs();
        	prepareGenomeFiles(dic, orgs, genomesFolder);        	
        }
        //printEdgeList(graph);
        //printGenome(graph, dic, treeMap, orgs, threads);
        
        //if(1==1)
        //	return;
        
        ExecutorService executor = Executors.newFixedThreadPool(threads);
    	/*Thread mainThread = Thread.currentThread();
		Runnable runnable = () -> {
			Scanner sc = new Scanner(System.in);
			while(true) {
				if(sc.hasNextLine()) {
					sc.nextLine();
					for (Entry<Thread, StackTraceElement[]> ent : Thread.getAllStackTraces().entrySet()) {
						System.out.println("---------------- " + ent.getKey().getId());
						for (StackTraceElement el : ent.getValue()) {
							System.out.println(el.toString());
						}
					}
				}
				try {
					Thread.currentThread().sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if(mainThread.isAlive() && executor.isTerminated()) {
					sc.close();
					break;
				}
			}
		};
		Thread thread = new Thread(runnable);
		thread.start();*/
        
        if(printAll || printFamilies) {
    		File familiesFolder = new File(outFolder.getAbsolutePath() + "/families");
    		if(!familiesFolder.exists())
    			familiesFolder.mkdir();
    		
    		File wekaFolder = new File(outFolder.getAbsolutePath() + "/weka");
    		if(!wekaFolder.exists())
    			wekaFolder.mkdir();
        	
			PrintStream jsHomo = new PrintStream(outFolder.getAbsolutePath() + "/data.homo.js");
			PrintStream jsDoms = new PrintStream(outFolder.getAbsolutePath() + "/data.doms.js");
			PrintStream jsOrthos = new PrintStream(outFolder.getAbsolutePath() + "/data.orthos.js");				
			jsHomo.println("dataHomo = [");
			jsDoms.println("dataDoms = [");
			jsOrthos.println("dataOrthos = [");
			
			ConcurrentLinkedDeque<Future<String>> listHomo = new ConcurrentLinkedDeque<>();
			ConcurrentLinkedDeque<Future<String>> listDoms = new ConcurrentLinkedDeque<>();
			ConcurrentLinkedDeque<Future<String>> listOrthos = new ConcurrentLinkedDeque<>();
			for (NodeGene<String, M8Attribute> head : heads) {
				//GraphM8 sub = graph.connComponentGraph(head);
				GraphM8 localGraph = graph;
				
				Struct struct = treeMap.get(head);
				if(struct != null) {
					while(!listHomo.isEmpty() && listHomo.peek().isDone()) {
						jsHomo.println(listHomo.poll().get());
					}
					listHomo.addLast(executor.submit(() -> {
						printFamilyWeka(new File(wekaFolder.getAbsolutePath() + "/" + head.getKey() + ".arff"), struct.fam, struct.align, gs);
						return printFamily(familiesFolder, localGraph, dic, orgs, gs, head, struct, ext, null, null, null);
					}));
					
					while(!listDoms.isEmpty() && listDoms.peek().isDone()) {
						jsDoms.println(listDoms.poll().get());
					}
					int domainId = 0;
					for (Struct domain : struct.domains) {
						final int localDomainId = domainId;
						listDoms.addLast(executor.submit(() -> {
							return printFamily(familiesFolder, localGraph, dic, orgs, gs, head, domain, ext, "domain", null, localDomainId);
						}));
						domainId++;
					}
					
					while(!listOrthos.isEmpty() && listOrthos.peek().isDone()) {
						jsOrthos.println(listOrthos.poll().get());
					}
					int orthoId = 0;
					for (Struct ortho : struct.orthologs) {
						final int localOrthoId = orthoId;
						listOrthos.addLast(executor.submit(() -> {
							int orthoDomainId = 0;
							for (Struct domain : ortho.domains) {
								//System.out.println(domain.fam.numGenes());
								//System.out.println(domain.fasta);
								printFamily(familiesFolder, localGraph, dic, orgs, gs, head, domain, ext, "ortholog", localOrthoId, orthoDomainId++);
								
							}
							return printFamily(familiesFolder, localGraph, dic, orgs, gs, head, ortho, ext, "ortholog", null, localOrthoId);
						}));
						orthoId++;
					}
				}
			}
			executor.shutdown();
			while(!listHomo.isEmpty()) {
				jsHomo.println(listHomo.poll().get());
			}
			
			while(!listDoms.isEmpty()) {
				jsDoms.println(listDoms.poll().get());
			}
			
			while(!listOrthos.isEmpty()) {
				jsOrthos.println(listOrthos.poll().get());
			}
			
			jsHomo.println("];");
			jsDoms.println("];");
			jsOrthos.println("];");
			jsHomo.close();
			jsDoms.close();
			jsOrthos.close();
        }
	}

	private static void printCoreAlignment(File fileAmino, File fileNucl, OrganismRegistry[] orgs, HashMap<NodeGene<String, M8Attribute>, Struct> treeMap, int threads) throws IOException, InterruptedException {
		PrintStream positionAmin = new PrintStream("core.position.amin");
		PrintStream positionNucl = new PrintStream("core.position.nucl");
		
		int sumAmin = 0;
		int sumNucl = 0;
		
		/*ArrayList<StringBuffer> coreAlignAmin = new ArrayList<>(orgs.length);
		ArrayList<StringBuffer> coreAlignNucl = new ArrayList<>(orgs.length);
		for (int i = 0; i < orgs.length; i++) {
			coreAlignAmin.add(new StringBuffer());
			coreAlignNucl.add(new StringBuffer());
		}*/
		ArrayList<PrintStream> coreAlignAmin = new ArrayList<>(orgs.length);
		ArrayList<PrintStream> coreAlignNucl = new ArrayList<>(orgs.length);
		ArrayList<File> fileAlignAmin = new ArrayList<>(orgs.length);
		ArrayList<File> fileAlignNucl = new ArrayList<>(orgs.length);
		for (int i = 0; i < orgs.length; i++) {
			File amin = new File(Constants.rand());
			coreAlignAmin.add(new PrintStream(amin));
			fileAlignAmin.add(amin);
			
			File nucl = new File(Constants.rand());
			coreAlignNucl.add(new PrintStream(nucl));
			fileAlignNucl.add(nucl);
		}
	
		
		for (Entry<NodeGene<String, M8Attribute>, Struct> ent : treeMap.entrySet()) {
			NodeGene<String, M8Attribute> key = ent.getKey();
			Struct struct = ent.getValue();
			
			//key = treeMap.entrySet().iterator().next().getValue().graph.getNode("Xf_11399_01380");
			//struct = treeMap.get(key);
			
 			if(struct.fam.numGenomes() == orgs.length) {
				positionAmin.print(key.getKey() + "\t" + sumAmin);
				positionNucl.print(key.getKey() + "\t" + sumNucl);
				
				THashMap<OrganismRegistry, LinkedList<GeneRegistry>> map = struct.fam.getMap();

				FastaFile fastaFileNucl = new FastaFile("." + Constants.rand(), SequenceType.Nucleotides);
				AlignmentFile alignFileNucl = new AlignmentFile("." + Constants.rand(), FileType.fasta);
				
				PrintStream stream = new PrintStream(fastaFileNucl);
				int k = 0;
				for (LinkedList<GeneRegistry> list : struct.fam.getArrayGenes(orgs))
				{
					stream.println(list.getFirst().getNucleotides());
					//System.out.println(orgs[k].getAbbrev() + "\t" + k);
					//System.out.println(list.getFirst().getNucleotides());
					k++;
				}
				LinkedList<GeneRegistry>[] temp = struct.fam.getArrayGenes(orgs);
				/*System.out.println(temp[12].getFirst().getAminoacids());
				System.out.println();
				System.out.println(temp[12].getFirst().getNucleotides());*/
				
				stream.close();
				ClustalO.makeAlignment(fastaFileNucl, alignFileNucl, " --threads=" + threads + " ");
				Alignment align = new Alignment(struct.align);
				try {
					Alignment alignNucl = new Alignment(alignFileNucl);
					
					
					for (int i = 0; i < orgs.length; i++) {
						coreAlignAmin.get(i).println(align.getSequence(map.get(orgs[i]).getFirst().getKey()));
						coreAlignNucl.get(i).println(alignNucl.getSequence(map.get(orgs[i]).getFirst().getKey()));
						if(alignNucl.getSequence(map.get(orgs[i]).getFirst().getKey()) == null) {
							System.out.println(key + "\t" + orgs[i].getAbbrev());
						}
					}
					
					sumAmin += align.getSequence(map.get(orgs[0]).getFirst().getKey()).length();
					sumNucl += alignNucl.getSequence(map.get(orgs[0]).getFirst().getKey()).length();
					positionAmin.println("\t" + sumAmin);
					positionNucl.println("\t" + sumNucl);					
					fastaFileNucl.delete();
					alignFileNucl.delete();
				}
				catch(Exception e) {
					System.out.println("erro: " + key);
					System.out.println(fastaFileNucl.getAbsolutePath());
					System.out.println(alignFileNucl.getAbsolutePath());
					e.printStackTrace();
				}
				
			}
		}
		for (int i = 0; i < orgs.length; i++) {
			coreAlignAmin.get(i).close();
			coreAlignNucl.get(i).close();
		}
	
		PrintStream streamAnimo = new PrintStream(fileAmino);
		PrintStream streamNucl = new PrintStream(fileNucl);
		for (int i = 0; i < orgs.length; i++) {
			streamAnimo.println(">" + orgs[i].getAbbrev());
			streamNucl.println(">" + orgs[i].getAbbrev());
			Scanner sc = new Scanner(fileAlignAmin.get(i));
			String rest = "";
			while(sc.hasNextLine()) {
				rest += sc.nextLine();
				for (String string: rest.split("(?<=\\G.{60})")) {
					if(string.length() < 60)
						rest = string;
					else
						streamAnimo.println(string);
				}
			}
			streamAnimo.println(rest);
			sc.close();
			
			sc = new Scanner(fileAlignNucl.get(i));
			rest = "";
			while(sc.hasNextLine()) {
				rest += sc.nextLine();
				for (String string: rest.split("(?<=\\G.{60})")) {
					if(string.length() < 60)
						rest = string;
					else
						streamNucl.println(string);
				}
			}
			streamNucl.println(rest);
			sc.close();
		}
		streamAnimo.close();
		streamNucl.close();
		
		for (int i = 0; i < orgs.length; i++) {
			fileAlignAmin.get(i).delete();
			fileAlignNucl.get(i).delete();
		}
	}

	private static void printPlot(File file, GraphM8 graph, OrganismRegistry[] orgs, HashMap<NodeGene<String, M8Attribute>, Struct> treeMap) throws FileNotFoundException {
		DistanceMatrix m = new DistanceMatrix(orgs.length);
		for (int i = 0; i < orgs.length; i++) {
			m.label[i] = orgs[i].getAbbrev();
			
		}
		for (Struct struct : treeMap.values()) {
			THashMap<OrganismRegistry, LinkedList<GeneRegistry>> map = struct.fam.getMap();
			for (int i = 0; i < orgs.length; i++) {
				for (int j = i+1; j < orgs.length; j++) {
					LinkedList<GeneRegistry> a = map.get(orgs[i]);
					LinkedList<GeneRegistry> b = map.get(orgs[j]);
					if(!((a  == null || a.size() == 0) && (b == null || b.size() == 0) &&
						(a != null && a.size() > 0 && b != null && b.size() > 0))) {
						m.dist[i][j]++;
						m.dist[j][i]++;
					}
				}
			}
		}
		for (int i = 0; i < orgs.length; i++) {
			for (int j = 0; j < orgs.length; j++) {
				m.dist[i][j] = Math.sqrt(m.dist[i][j]);
			}
		}
		
		Point[] points = m.multiScalling();
		//Point[] points = new DistanceMatrix(graph, orgs).multiScalling();
		
		PrintStream stream = new PrintStream(file);
		stream.println("var points = [");
		for (int i = 0; i < orgs.length; i++) {
			stream.println("[" + points[i].x + ", " + points[i].y + "],");
		}
		stream.println("];");
		stream.close();
	}

	private static void printSequenceList(File file, OrganismRegistry[] orgs, GraphM8 graph, LinkedList<NodeGene<String, M8Attribute>> heads, HashMap<NodeGene<String, M8Attribute>, Struct> treeMap, HeaderExtractorPatric ext) throws FileNotFoundException {
		PrintStream stream = new PrintStream(file);
		stream.println("sequenceList = [");
		LinkedList<String> index = new LinkedList<>();
		index.add("indexSequenceList = [");
		
		int pos = 0;
		for (int i = 0; i < orgs.length; i++) {
			HashMap<GeneRegistryFna, NodeGene<String, M8Attribute>> list = new HashMap<>();
			OrganismRegistry org = orgs[i];
			for (Entry<NodeGene<String, M8Attribute>, Struct> ent : treeMap.entrySet()) {
				Struct struct = ent.getValue();
				NodeGene<String, M8Attribute> head = ent.getKey();
				LinkedList<GeneRegistry> genes = struct.fam.getMap().get(org);
				
				if(genes != null)
					for (GeneRegistry gene : genes) {
						if(gene instanceof GeneRegistryFna) {
							list.put((GeneRegistryFna) gene, head);
						}
						else
							stream.println("{seq:\"" + gene.getKey() + "\", head:\"" + head.getKey() + "\", func:\"" + ext.getDescription(gene) + "\", gen:\"" + gene.getOrganism().getRoot().getAbbrev() + "\"},");
					}
			}
			
			LinkedList<Entry<GeneRegistryFna, NodeGene<String, M8Attribute>>> ents = new LinkedList<>(list.entrySet());
			ents.sort(new Comparator<Entry<GeneRegistryFna, NodeGene<String, M8Attribute>>>() {
				@Override
				public int compare(Entry<GeneRegistryFna, NodeGene<String, M8Attribute>> o1,
						Entry<GeneRegistryFna, NodeGene<String, M8Attribute>> o2) {
					return (int)(o1.getKey().getStart() - o2.getKey().getStart());
				}
			});
			
			//index.add("[");
			int start = pos;
			for (Entry<GeneRegistryFna, NodeGene<String, M8Attribute>> ent : ents) {
				NodeGene<String, M8Attribute> head = ent.getValue();
				GeneRegistryFna gene = ent.getKey();
				//stream.println("{seq:\"" + gene.getKey() + "\", head:\"" + head.getKey() + "\", func:\"" + ext.getDescription(gene) + "\", gen:\"" + gene.getOrganism().getRoot().getAbbrev() + "\"},");
				
				stream.print("{seq:'" + gene.getKey() + "',head:'" + head.getKey() + "',func:\"" + ext.getDescription(gene) + "\",gen:" + i + ",s:" + gene.getStart() + ",e:" + gene.getEnd() + ",d:'" + (gene.isComplement()?'-':'+') + "',contig:'" + gene.getContig() + "'");
				if(gene.getGeneName() != null)
					stream.print(",gene:'" + gene.getGeneName()+ "'");
				stream.println("},");
				pos++;
			}
			index.add(" {start:" + start + ",end:" + (pos-1) + "},");
			//index.add("],");
		}
		stream.println("];");
		//stream.println("}");
		
		
		for (String string : index) {
			stream.println(string);
		}
		stream.println("];");
		/*for (NodeGene<String, M8Attribute> head : heads) {
			LinkedList<NodeGene<String, M8Attribute>> nodes = graph.connComponentList(head);
			for (NodeGene<String, M8Attribute> node : nodes) {
				//System.out.println(node.getGene().getHeader());
				stream.print("{seq:\"" + node.getKey() + "\", head:\"" + head.getKey() + "\", func:\"" + ext.getDescription(node.getGene()) + "\", gen:\"" + node.getGene().getOrganism().getRoot().getAbbrev() + "\"");
				if(node.getGene() instanceof GeneRegistryFna) {
					GeneRegistryFna gene = (GeneRegistryFna) node.getGene();
					stream.print(",s:" + gene.getStart() + ",e:" + gene.getEnd() + ",d:" + gene.isComplement());
					if(gene.getGeneName() != null)
						stream.print(",gene:" + gene.getGeneName());
				}
				stream.println("},");
			}
					
		}*/
		/*stream.println("];");
		stream.println("}");*/
		stream.close();
		
		
		
	}

	private static void printPhylogeny(File file, GraphM8 graph, Dictionary dic, OrganismRegistry orgs[],  RegistryGroups[] gs, LinkedList<NodeGene<String, M8Attribute>> heads, HashMap<NodeGene<String, M8Attribute>, Struct> treeMap, TreeFile ggdcTree, TreeFile standardTree) throws IOException, InterruptedException {
		PrintStream stream = new PrintStream(file);
		
		LinkedList<String> consense = new LinkedList<>();
		LinkedList<String> supertree = new LinkedList<>();
		System.out.println("A " + Calendar.getInstance().getTime());
		for (NodeGene<String, M8Attribute> head : heads) {
			Struct struct = treeMap.get(head);
			if(struct.fam.numGenes() >= 3 && struct.fam.numGenes() == struct.fam.numGenomes()) {
				String tree = struct.tree.load();
				for (Entry<OrganismRegistry, LinkedList<GeneRegistry>> ent : struct.fam.getMap().entrySet()) {
					tree = tree.replace(ent.getValue().getFirst().getKey(), ent.getKey().getRoot().getAbbrev());
				}
				
				supertree.add(tree);
				if(struct.fam.numGenomes() == orgs.length)
					consense.add(tree);
			}
		}
		
		//System.out.println("B " + Calendar.getInstance().getTime());
		HashMap<String, Integer> mostFrequente = new HashMap<>();
		for (String string : consense) {
			PhylogeneticTree tree = new PhylogeneticTree(string, false);
			boolean adicionado = false;
			for (String string2 : mostFrequente.keySet()) {
				if(!adicionado) {
					PhylogeneticTree tree2 = new PhylogeneticTree(string2, false);
					if(tree.isomorph(tree2)) {
						mostFrequente.put(string2, mostFrequente.get(string2) + 1);
						adicionado = true;
					}
				}
			}
			
			if(!adicionado)
				mostFrequente.put(string, 1);
		}
		
		//System.out.println("C " + Calendar.getInstance().getTime());
		Entry<String, Integer> max = null;
		if(!mostFrequente.entrySet().isEmpty()) {
			max = mostFrequente.entrySet().iterator().next();
			for (Entry<String, Integer> ent : mostFrequente.entrySet()) {
				if(max.getValue() < ent.getValue())
					max = ent;
			}
		}
		
		
		System.out.println("D " + Calendar.getInstance().getTime());
		String distanceNeighbor = Phylip.makeTree(new DistanceMatrix(graph, orgs), TreeMethod.Neighbor, new File("teste.tree")).replace("\n", "");
		System.out.println("E " + Calendar.getInstance().getTime());
		String genePhylip = Phylip.makeTree(new GenesMatrix(graph, orgs, new HeaderExtractorPatric()), null).replace("\n", "");
		System.out.println("F " + Calendar.getInstance().getTime());
		String kmerNeighbor = Phylip.makeTree(KMer.matrizDistanciaEuclidiana(orgs, 6), TreeMethod.Neighbor, null).replace("\n", "");
		System.out.println("G " + Calendar.getInstance().getTime());
		String consenseTreeMajorityExtended = "";
		String consenseTreeStrict = "";
		String consenseTreeMajority = "";
		if(!consense.isEmpty()) {
			consenseTreeMajorityExtended = Phylip.makeConsenseTreeString(consense, ConsenseTreeMethod.MajorityRuleE, 0, false, null).replace("\n", "");
			consenseTreeStrict = Phylip.makeConsenseTreeString(consense, ConsenseTreeMethod.Strict, 0, false, null).replace("\n", "");
			consenseTreeMajority = Phylip.makeConsenseTreeString(consense, ConsenseTreeMethod.MajorityRule, 0, false, null).replace("\n", "");
		}
		//System.out.println("H " + Calendar.getInstance().getTime());
		String mostFrequentTree = "";
		if(max != null)
			mostFrequentTree = max.getKey().replace("\n", "");
		String ggdc = "";
		String standard = "";
		if(ggdcTree != null && ggdcTree.exists())
			ggdc = ggdcTree.load().replace("\n", "");
		if(standardTree != null && standardTree.exists())
			standard = standardTree.load().replace("\n", "");
		
		System.out.println("I " + Calendar.getInstance().getTime());
		String cgenePhylip = new PhylogeneticTree(genePhylip, false).getCanonical();
		String cdistanceNeighbor = new PhylogeneticTree(distanceNeighbor, false).getCanonical();
		String ckmerNeighbor = new PhylogeneticTree(kmerNeighbor, false).getCanonical();
		String cconsenseTreeMajorityExtended = "";
		String cconsenseTreeStrict = "";
		String cconsenseTreeMajority = "";
		if(!consense.isEmpty()) {
			cconsenseTreeMajorityExtended = new PhylogeneticTree(consenseTreeMajorityExtended, false).getCanonical();
			cconsenseTreeStrict = new PhylogeneticTree(consenseTreeStrict, false).getCanonical();
			cconsenseTreeMajority = new PhylogeneticTree(consenseTreeMajority, false).getCanonical();
		}
		String cmostFrequentTree = "";
		if(max != null)
			cmostFrequentTree = new PhylogeneticTree(mostFrequentTree, false).getCanonical();
		String cggdc = "";
		if(ggdc != null && ggdc.length() > 0)
			cggdc = new PhylogeneticTree(ggdc, false).getCanonical();
		String cstandard = "";
		if(standard != null && standard.length() > 0)
			new PhylogeneticTree(standard, false).getCanonical();
		System.out.println("J " + Calendar.getInstance().getTime());
		
		
		for (int i = 0; i < orgs.length; i++) {
			cgenePhylip = cgenePhylip.replace(orgs[i].getAbbrev(), "" + i);
			cdistanceNeighbor = cdistanceNeighbor.replace(orgs[i].getAbbrev(), "" + i);
			ckmerNeighbor = ckmerNeighbor.replace(orgs[i].getAbbrev(), "" + i);
			cconsenseTreeMajorityExtended = cconsenseTreeMajorityExtended.replace(orgs[i].getAbbrev(), "" + i);
			cconsenseTreeStrict = cconsenseTreeStrict.replace(orgs[i].getAbbrev(), "" + i);
			cconsenseTreeMajority = cconsenseTreeMajority.replace(orgs[i].getAbbrev(), "" + i);
			cmostFrequentTree = cmostFrequentTree.replace(orgs[i].getAbbrev(), "" + i);
			cggdc = cggdc.replace(orgs[i].getAbbrev(), "" + i);
			cstandard = cstandard.replace(orgs[i].getAbbrev(), "" + i);
		}
		
		//System.out.println("K " + Calendar.getInstance().getTime());
		TreeFile superTree = new TreeFile("supertree.tmp");
		TreeFile treeFile = new TreeFile("trees.tmp");
		PrintStream streamTree = new PrintStream(treeFile);
		for (String string : supertree) {
			streamTree.print(string);
		}
		streamTree.close();
		Clann.gerarArvore(treeFile, superTree, Criterion.qfit, null, new Hs(1, 1, 1, null, Swap.nni, null), " ");
		String superTree2 = new PhylogeneticTree(superTree.load().substring(0, superTree.load().indexOf(";")+1), false).print().replaceAll("0.0", "1");//replaceAll(",", ":1,").replaceAll("\\)", ":1)").replaceAll(":1:1", ":1");
		//String superTree2 = "";
		superTree.delete();
		treeFile.delete();
		
				
		//System.out.println("L " + Calendar.getInstance().getTime());
		stream.println("var trees = [");
		if(standard != null && !"".equals(standard)) {
			stream.println("{name: 'Standard Tree', childs: [");
			stream.println("  {name: 'Standard', tree: '" + standard + "', canonical: '" + cstandard + "'},");
			stream.println("]},");
		}
		stream.println("{name: 'Discrete characters', childs: [");
		stream.println("  {name: 'Presence/Absence', tree: '" + genePhylip + "', canonical: '" + cgenePhylip + "'},");
		stream.println("]},");
		stream.println("{name: 'Distance matrix', childs: [");
		stream.println("  {name: 'Presence/Absence', tree: '" + distanceNeighbor + "', canonical: '" + cdistanceNeighbor + "'},");
		stream.println("  {name: 'k-Mer', tree: '" + kmerNeighbor + "', canonical: '" + ckmerNeighbor + "'},");
		if(ggdc != null && !"".equals(ggdc))
			stream.println("  {name: 'GGDC DDH', tree: '" + ggdc + "', canonical: '" + cggdc + "'},");
		stream.println("]},");
		stream.println("{name: 'Families philogeny', childs: [");
		if(!consense.isEmpty()) {
			stream.println("  {name: 'Consense (Majority Rule Extended)', tree: '" + consenseTreeMajorityExtended + "', canonical: '" + cconsenseTreeMajorityExtended + "'},");
			stream.println("  {name: 'Consense (Strict)', tree: '" + consenseTreeStrict + "', canonical: '" + cconsenseTreeStrict + "'},");
			stream.println("  {name: 'Consense (Majority Rule)', tree: '" + consenseTreeMajority + "', canonical: '" + cconsenseTreeMajority + "'},");
		}
		if(mostFrequentTree != null && !"".equals(mostFrequentTree))
		stream.println("  {name: 'Most Frequent', tree: '" + mostFrequentTree + "', canonical: '" + cmostFrequentTree + "'},");
		if(superTree2 != null && !"".equals(superTree2))
			stream.println("  {name: 'Supertree', tree: '" + superTree2 + "', canonical: '" + new PhylogeneticTree(superTree2, false).getCanonical() + "'},");
		stream.println("]}");
		stream.println("];");
		stream.close();
		//System.out.println("M " + Calendar.getInstance().getTime());
		
	}
	
	private static void printEdgeList(GraphM8 graph) throws IOException, InterruptedException {
		PrintStream streamGenes = new PrintStream(new File("genes.txt"));
		for (NodeGene<String, M8Attribute> node : graph.getNodes()) {
			streamGenes.println(node.getKey() + "\t" + node.getGene().getOrganism().getRoot().getAbbrev());
		}
		streamGenes.close();
		
		HeaderExtractorPatric ext = new HeaderExtractorPatric();
		PrintStream streamAnnotations = new PrintStream(new File("annotations.txt"));
		for (NodeGene<String, M8Attribute> node : graph.getNodes()) {
			streamAnnotations.println(node.getKey() + "\t" + ext.getDescription(node.getGene()));
		}
		streamAnnotations.close();
		
		PrintStream streamEdges = new PrintStream(new File("edges.txt"));
		for (NodeGene<String, M8Attribute> nodeA : graph.getNodes()) {
			for (String nodeB : nodeA.getNeighbors()) {
				streamEdges.println(nodeA.getKey() + "\t" + nodeB);
				
			}
		}
		
		

	}
	
	private static void printGenome(GraphM8 graph, Dictionary dic, HashMap<NodeGene<String, M8Attribute>, Struct> treeMap, OrganismRegistry orgs[], int threads) throws IOException, InterruptedException {
		HashMap<Struct, Long> hash = new HashMap<>();
		LinkedList<Struct> structs = new LinkedList<>();
		for (Struct structDom : treeMap.values()) {
			if(structDom.orthologs == null || structDom.orthologs.isEmpty()) {
				structs.add(structDom);
				long total = 0;
				for (GeneRegistry gene : structDom.fam.getGenes())
					total += ((GeneRegistryFna)gene).getStart();
				hash.put(structDom, total/structDom.fam.getGenes().size());
			}
			else {
				for (Struct structOrthos : structDom.orthologs) {
					structs.add(structOrthos);
					long total = 0;
					for (GeneRegistry gene : structOrthos.fam.getGenes())
						total += ((GeneRegistryFna)gene).getStart();
					hash.put(structOrthos, total/structOrthos.fam.getGenes().size());
				}
			}
		}
		structs.sort(new Comparator<Struct>() {
			@Override
			public int compare(Struct o1, Struct o2) {
				return (int) (hash.get(o1) - hash.get(o2));
			}
		});
		
		PrintStream extra[] = new PrintStream[orgs.length];
		for (int i = 0; i < extra.length; i++) {
			extra[i] = new PrintStream(new File("extra_" + orgs[i].getAbbrev() + ".fasta"));
		}
		
		StringBuilder pangenomeUnique = new StringBuilder();
		PrintStream pangenomeMulti = new PrintStream("pangenomeMulti.fasta");
		PrintStream pangenomeExtra = new PrintStream("pangenomeExtra.fasta");
		int id = 0;
		for (Struct struct : structs) {
			String consense = "";
			String consenseFormated = "";
			if(struct.fam.numGenes() == 1)
				consense = struct.fam.getGenes().getFirst().getNucleotidesRegistry().getSequence();
			else {
				FastaFile fastaFileNucl = new FastaFile(Constants.rand() + "_" + (id++), SequenceType.Nucleotides);
				AlignmentFile alignFileNucl = new AlignmentFile(Constants.rand() + "_" + (id++), FileType.fasta);
				
				try {
					PrintStream stream = new PrintStream(fastaFileNucl);
					for (GeneRegistry gene: struct.fam.getGenes())
						stream.println(gene.getNucleotides());
					stream.close();
					
					if(struct.fam.getGenes().getFirst().getKey().equals("fig|1314.374.peg.1102"))
						alignFileNucl = new AlignmentFile("/home/caio/Dropbox/workspace2/framework/.z130b8f68-87d4-49c1-8079-efe0449cdbd7_3323", FileType.fasta);
					else
						ClustalO.makeAlignment(fastaFileNucl, alignFileNucl, " --threads=" + threads + " ");
					Alignment align = new Alignment(alignFileNucl);
					consense = align.consense(true);
					consense = consense.replaceAll("-", "");
					alignFileNucl.delete();
					fastaFileNucl.delete();
				} catch (Exception e) {
					consense = "";
					System.err.println(struct.fam.getGenes().getFirst().getKey());
					System.err.println(fastaFileNucl);
					System.err.println(alignFileNucl);
					e.printStackTrace();
				}
			}
			
			consenseFormated = ">" + struct.fam.getGenes().getFirst().getKey() + "\n" + Constants.formatSeq(consense, 60);
			pangenomeUnique.append(consense);
			pangenomeMulti.println(consenseFormated);
			if(struct.fam.numGenomes() != orgs.length) {
				pangenomeExtra.println(consenseFormated);
				boolean[] array = struct.fam.getArrayGenesBoolean(orgs);
				for (int i = 0; i < orgs.length; i++) {
					if(array[i])
						extra[i].println(consenseFormated);
				}
			}
		}
		pangenomeMulti.close();
		pangenomeExtra.close();
		for (int i = 0; i < extra.length; i++) {
			extra[i].close();
		}
		
		PrintStream pangenomeFile = new PrintStream(new File("pangenome.fasta"));
		pangenomeFile.println(">pangenome\n" + Constants.formatSeq(pangenomeUnique.toString(), 60));
		pangenomeFile.close();
	}
	
	private static String gff(OrganismRegistry org) {
		HeaderExtractorPatric ext = new HeaderExtractorPatric();
		StringBuilder result = new StringBuilder();
		for (GeneRegistry geneOrig : org.getGenes()) {
			if(geneOrig instanceof GeneRegistryFna) {
				GeneRegistryFna gene = (GeneRegistryFna) geneOrig;
				result.append(
						gene.getKey() + "\t" +
						"DIIDFDIFJ\t" +
						"CDS\t" +
						gene.getStart() + "\t" +
						gene.getEnd() + "\t" +
						".\t" +
						(gene.isComplement()?"-":"+") + "\t" +
						"1\t" +
						"ID=" + gene.getKey() + ";FUNC=" + ext.getDescription(gene) + "\n"
				);
				
			}
			else {
				result.append(
						geneOrig.getKey() + "\t" +
						"DIIDFDIFJ\t" +
						"CDS\t" +
						"0\t" +
						"0\t" +
						".\t" +
						"+\t" +
						"1\t" +
						"ID=" + geneOrig.getKey() + ";FUNC=" + ext.getDescription(geneOrig) + "\n"
				);
			}
		}
		return result.toString();
	}
	
	private static String bed(OrganismRegistry org) {
		StringBuilder result = new StringBuilder();
		for (GeneRegistry geneOrig : org.getGenes()) {
			if(geneOrig instanceof GeneRegistryFna) {
				GeneRegistryFna gene = (GeneRegistryFna) geneOrig;
				result.append(
						gene.getContig() + "\t" +
								gene.getStart() + "\t" +
								gene.getEnd() + "\t" +
								gene.getKey() + "\t" +
								".\t" +
								(gene.isComplement()?"-":"+") + "\t" +
								"1\t" +
								"1\n"
						);
			}
			else {
				result.append(
						"CONTIG\t" +
						"0\t" +
						"0\t" +
						geneOrig.getKey() + "\t" +
						".\t" +
						"+\t" +
						"1\t" +
						"1\n"
				);
			}
		}
		return result.toString();
	}
	
	private static void prepareGenomeFiles(Dictionary dic, OrganismRegistry orgs[], File dest) throws IOException, InterruptedException {
		for (OrganismRegistry org : dic.getAllOrganisms()) {
			ToolkitBaseFile orgDest = new ToolkitBaseFile(dest.getAbsolutePath() + File.separator + org.getAbbrev() + ".fna");
			File fai = new File(orgDest.getAbsoluteFile() + ".fai");
			if(!orgDest.exists()) {
				Files.copy(org.getFile().toPath(), orgDest.toPath());
				for (OrganismRegistry plasm : org.plasmideos) {
					orgDest.join(plasm.getFile(), true);
				}
			}
			if(!fai.exists()) {
				String command = "samtools faidx " + orgDest.getAbsolutePath() + "\n";
				Process process = new ProcessBuilder(new String[] {"bash", "-c", command}).redirectErrorStream(true).start();
				process.waitFor();
			}
			
			PrintStream streamGff = new PrintStream(new File(orgDest.getPath().replace(".fna", ".gff")));
			streamGff.print(gff(org));
			streamGff.close();
			
			PrintStream streamBed = new PrintStream(new File(orgDest.getPath().replace(".fna", ".bed")));
			streamBed.print(bed(org));
			streamBed.close();
			
			FileOutputStream fileBedGz = new FileOutputStream(new File(orgDest.getPath().replace(".fna", ".bed.gz")));
			GZIPOutputStream streamBedGz = new GZIPOutputStream(fileBedGz);
			streamBedGz.write(bed(org).getBytes());
			streamBedGz.close();
			
			//Files.copy(fai.toPath(), new File(dest.getAbsolutePath() + File.separator + fai.getName()).toPath());
		}
	}
}
