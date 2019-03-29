package Structure;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;

import Structure.Registry.RegistryGroup;
import Structure.Registry.RegistryGroups;
import Structure.Restriction.Attribute;
import Structure.Graph.GraphGenes;
import Structure.Graph.Node;
import Structure.Graph.NodeGene;
import Structure.Registry.OrganismRegistry;
import gnu.trove.map.hash.THashMap;

public class SetOperation {
	public static <KEY, ATTRIBUTE extends Attribute<ATTRIBUTE>, NODE extends Node<KEY, ATTRIBUTE>> LinkedList<NODE> intersection(Collection<NODE> list1, Collection<NODE> list2) {
		HashSet<NODE> set1 = new HashSet<>(list1);
		HashSet<NODE> set2 = new HashSet<>(list2);
		LinkedList<NODE> result = new LinkedList<>();		
		for (NODE no : set1) {
			if(set2.contains(no))
				result.add(no);
		}
		return result;
	}
	
	public static <KEY, ATTRIBUTE extends Attribute<ATTRIBUTE>, NODE extends Node<KEY, ATTRIBUTE>> LinkedList<NODE> union(Collection<NODE> list1, Collection<NODE> list2) {
		HashSet<NODE> set1 = new HashSet<>();
		set1.addAll(list1);
		set1.addAll(list2);
		return new LinkedList<>(set1);
	}
	
	public static <KEY, ATTRIBUTE extends Attribute<ATTRIBUTE>, NODE extends Node<KEY, ATTRIBUTE>> LinkedList<NODE> not(Collection<NODE> list1, Collection<NODE> list2) {
		LinkedList<NODE> result = new LinkedList<>();
		HashSet<NODE> set2 = new HashSet<>(list2);
		
		for (NODE no : list1) {
			if(!set2.contains(no))
				result.add(no);
		}		
		return result;
	}
	
	public static <KEY, ATTRIBUTE extends Attribute<ATTRIBUTE>,	NODE extends NodeGene<KEY, ATTRIBUTE>, GRAPH extends GraphGenes<KEY, ATTRIBUTE, NODE>> LinkedList<NODE> listRelatedGenes(GRAPH graph, Collection<NODE> heads, OrganismRegistry org) {
		HashSet<NODE> result = new HashSet<>();
		for (NODE head : heads) {
			LinkedList<NODE> componente = graph.connComponentList(head);
			boolean added = false;
			for (NODE node : componente) {
				if(!added && node.getGene().getOrganism().getRoot() == org) {
					result.add(node);
					added = true;
				}
			}
		}
		return new LinkedList<>(result);
	}
	
	public static <KEY, ATTRIBUTE extends Attribute<ATTRIBUTE>,	NODE extends NodeGene<KEY, ATTRIBUTE>, GRAPH extends GraphGenes<KEY, ATTRIBUTE, NODE>> LinkedList<NODE> listRelatedGenes(GRAPH graph, Collection<NODE> heads, RegistryGroup group) {
		LinkedList<NODE> result = new LinkedList<>();
		for (NODE head : heads) {
			LinkedList<NODE> componente = graph.connComponentList(head);
			for (NODE no : componente) {
				if(group.contain(no.getGene().getOrganism().getRoot())) {
					result.add(no);
					break;
				}
			}
		}
		return result;
	}

	public static <KEY, ATTRIBUTE extends Attribute<ATTRIBUTE>,	NODE extends NodeGene<KEY, ATTRIBUTE>, GRAPH extends GraphGenes<KEY, ATTRIBUTE, NODE>> 
	LinkedList<NODE> listExclusiveGenes(GRAPH graph, Collection<NODE> heads, OrganismRegistry org, boolean considerSameOrg) {
		LinkedList<NODE> resultado = new LinkedList<>();
		for (NODE head : heads) {
			LinkedList<NODE> componente = graph.connComponentList(head);
			if(!considerSameOrg) {
				if(componente.size() == 1 && componente.getFirst().getGene().getOrganism().getRoot() == org)
					resultado.add(head);
			}
			else {
				boolean ok = true;
				for (NODE no : componente) {
					if(org != no.getGene().getOrganism().getRoot())
						ok = false;
				}
				if(ok)
					resultado.add(head);
			}
		}
		return resultado;
	}

	public static <KEY, ATTRIBUTE extends Attribute<ATTRIBUTE>,	NODE extends NodeGene<KEY, ATTRIBUTE>, GRAPH extends GraphGenes<KEY, ATTRIBUTE, NODE>> LinkedList<NODE> listExclusiveComponent(GRAPH graph, Collection<NODE> heads, RegistryGroup group, boolean core) {
		LinkedList<NODE> result = new LinkedList<>();
		
		for (NODE head : heads) {
			LinkedList<NODE> component = graph.connComponentList(head);
			boolean ok = true;
			HashSet<OrganismRegistry> set = null;
			if(core)
				set = new HashSet<OrganismRegistry>();
			for (NODE node : component) {
				if(!group.contain(node.getGene().getOrganism().getRoot())) {
					ok = false;
				}
				else if(core) { 
						set.add(node.getGene().getOrganism().getRoot());
				}
			}
			if(ok && (!core || set.size() == group.size()))
				result.add(head);
		}
		
		return result;
	}
	
	public static <KEY, ATTRIBUTE extends Attribute<ATTRIBUTE>,	NODE extends NodeGene<KEY, ATTRIBUTE>, GRAPH extends GraphGenes<KEY, ATTRIBUTE, NODE>> LinkedList<NODE> listExclusiveGenes(GRAPH graph, Collection<NODE> list, boolean considerSameOrg) {
		LinkedList<NODE> result = new LinkedList<>();
		for (NODE head : list) {
			LinkedList<NODE> component = graph.connComponentList(head);
			if(!considerSameOrg) {
				if(component.size() == 1)
					result.add(head);
			}
			else {
				OrganismRegistry org = component.getFirst().getGene().getOrganism().getRoot();
				boolean ok = true;
				for (NODE no : component) {
					if(org != no.getGene().getOrganism().getRoot())
						ok = false;
				}
				if(ok)
					result.add(head);
			}
		}
		return result;
	}
	
	public static <KEY, ATTRIBUTE extends Attribute<ATTRIBUTE>,	NODE extends NodeGene<KEY, ATTRIBUTE>, GRAPH extends GraphGenes<KEY, ATTRIBUTE, NODE>> LinkedList<NODE> listExclusiveGenes(GRAPH graph, Collection<NODE> list, RegistryGroup grupo, boolean considerarMesmoOrg) {
		LinkedList<NODE> resultado = new LinkedList<>();
		
		for (NODE head : list) {
			LinkedList<NODE> componente = graph.connComponentList(head);
			if(!considerarMesmoOrg) {
				if(componente.size() == 1 && grupo.contain(componente.getFirst().getGene().getOrganism().getRoot()))
					resultado.add(head);
			}
			else {	
				boolean ok = true;
				for (NODE no : componente) {
					if(!grupo.contain(no.getGene().getOrganism().getRoot())) {
						ok = false;
					}
				}
				if(ok)
					resultado.add(head);
			}
		}
		
		return resultado;
	}

	public static <KEY, ATTRIBUTE extends Attribute<ATTRIBUTE>,	NODE extends NodeGene<KEY, ATTRIBUTE>, GRAPH extends GraphGenes<KEY, ATTRIBUTE, NODE>> HashMap<String, LinkedList<NODE>> mapFamilies(GRAPH graph, Collection<NODE> heads, OrganismRegistry orgs[]) {
		HashMap<String, LinkedList<NODE>> map = new HashMap<>();
		for (NODE head : heads) {
			LinkedList<NODE> component = graph.connComponentList(head);
			GeneFamily family = new GeneFamily(component);
			
			String array = GeneFamily.vet(family.getArrayGenesBoolean(orgs));
			LinkedList<NODE> x = map.get(array);
			if(x == null) {
				x = new LinkedList<>();
				x.add(head);
				map.put(array, x);
			}
			else
				x.add(head);
		}
		return map;
	}
	
	public static <KEY, ATTRIBUTE extends Attribute<ATTRIBUTE>,	NODE extends NodeGene<KEY, ATTRIBUTE>, GRAPH extends GraphGenes<KEY, ATTRIBUTE, NODE>> THashMap<String, LinkedList<NODE>> mapFamilies(GRAPH graph, Collection<NODE> heads, RegistryGroups groups) {
		THashMap<String, LinkedList<NODE>> map = new THashMap<>();
		for (NODE head : heads) {
			LinkedList<NODE> componente = graph.connComponentList(head);
			//FamiliaGenes<KEY_STATIC> familia = new FamiliaGenes<>(componente);
			
			String array = GeneFamily.vet(GeneFamily.getArrayGenesBoolean(componente, groups));
			LinkedList<NODE> x = map.get(array);
			if(x == null) {
				x = new LinkedList<>();
				x.add(head);
				map.put(array, x);
			}
			else
				x.add(head);
		}
		return map;
	}
	
	public static <KEY, ATTRIBUTE extends Attribute<ATTRIBUTE>,	NODE extends NodeGene<KEY, ATTRIBUTE>, GRAPH extends GraphGenes<KEY, ATTRIBUTE, NODE>> void pizzaDuasCamadas(GRAPH grafo, RegistryGroups grupos, OrganismRegistry orgs[]) {
		LinkedList<NODE> heads = grafo.connComponentHeads();
		LinkedList<NODE> grupo1  = listExclusiveComponent(grafo, heads, grupos.getGroups()[0], false);
		LinkedList<NODE> grupo2  = listExclusiveComponent(grafo, heads, grupos.getGroups()[1], false);
		LinkedList<NODE> grupo12 = union(grupo1, grupo2);
		LinkedList<NODE> shared  = not(heads, grupo12);
		
		LinkedList<Entry<String, LinkedList<NODE>>> ent1 = new LinkedList<>(mapFamilies(grafo, grupo1, orgs).entrySet());
		LinkedList<Entry<String, LinkedList<NODE>>> ent2 = new LinkedList<>(mapFamilies(grafo, grupo2, orgs).entrySet());
		LinkedList<Entry<String, LinkedList<NODE>>> entShared = new LinkedList<>(mapFamilies(grafo, shared, orgs).entrySet());
		
		Comparator<Entry<String, LinkedList<NODE>>> cp = new Comparator<Entry<String, LinkedList<NODE>>>() {
			@Override
			public int compare(Entry<String, LinkedList<NODE>> o1, Entry<String, LinkedList<NODE>> o2) {
				return o2.getValue().size() - o1.getValue().size();
			}
		};
		
		Collections.sort(ent1, cp);
		Collections.sort(ent2, cp);
		Collections.sort(entShared, cp);
		
		LinkedList<String> total = new LinkedList<>();
		LinkedList<String> tipo = new LinkedList<>();
		LinkedList<String> nome = new LinkedList<>();
		LinkedList<String> cor1 = new LinkedList<>();
		LinkedList<String> cor2 = new LinkedList<>();
		LinkedList<String> totalGrupoMax = new LinkedList<>();
		LinkedList<String> totalGrupoMin = new LinkedList<>();

		double start = 0;
		start = vaa(grafo, "Sharing", "gray", entShared, total, tipo, nome, cor1, cor2, totalGrupoMax, totalGrupoMin, start, orgs);
		start = vaa(grafo, "Pathogenic", "red", ent1, total, tipo, nome, cor1, cor2, totalGrupoMax, totalGrupoMin, start, orgs);
		start = vaa(grafo, "Nonpathogenic", "blue", ent2, total, tipo, nome, cor1, cor2, totalGrupoMax, totalGrupoMin, start, orgs);
		
		String s = "";
		for (String string : total) {
			s += "," + string;
		}
		System.out.println("total=c(" + s.substring(1) + ")");
		
		s = "";
		for (String string : tipo) {
			s += ",\"" + string + "\"";
		}
		System.out.println("tipo=c(" + s.substring(1) + ")");
		
		s = "";
		for (String string : nome) {
			s += ",\"" + string + "\"";
		}
		System.out.println("nome=c(" + s.substring(1) + ")");
				
		s = "";
		for (String string : cor1) {
			s += ",\"" + string + "\"";
		}
		System.out.println("cor1=c(" + s.substring(1) + ")");
		
		s = "";
		for (String string : cor2) {
			s += ",\"" + string + "\"";
		}
		System.out.println("cor2=c(" + s.substring(1) + ")");
		
		s = "";
		for (String string : totalGrupoMax) {
			s += "," + string + "";
		}
		System.out.println("totalGrupoMax=c(" + s.substring(1) + ")");
		
		s = "";
		for (String string : totalGrupoMin) {
			s += "," + string + "";
		}
		System.out.println("totalGrupoMin=c(" + s.substring(1) + ")");
		
		System.out.println("library(ggplot2)");
		System.out.println("library(ggrepel)");
		System.out.println("xMax=cumsum(total)");
		System.out.println("xMin=c(0,xMax[1:length(xMax)-1])");
		System.out.println("xAvg=xMin+(xMax-xMin)/2");
		System.out.println("totalGrupoTotal = totalGrupoMax-totalGrupoMin");
		System.out.println("totalGrupoPos = totalGrupoMin+(totalGrupoMax-totalGrupoMin)/2");
		System.out.println("m = data.frame(total,tipo,nome,cor1,cor2,xMin,xMax,xAvg,totalGrupoTotal,totalGrupoPos)");
		System.out.println("ggplot(m) + ");
		System.out.println("geom_rect(aes(ymax=xMax, ymin=xMin), fill=cor1, xmax=4, xmin=0) +");
		System.out.println("geom_rect(aes(ymax=xMax, ymin=xMin), fill=cor2, colour='black', xmax=5, xmin=4) +");
		System.out.println("geom_label_repel(aes(y=xAvg, label = m$nome), x=4.9, nudge_x=5, fill = 'blue', fontface = 'bold', color = 'white', box.padding = unit(0.35, 'lines'), point.padding = unit(0.5, 'lines'),  segment.color = 'grey50') +");
		System.out.println("geom_text(aes(y=xAvg, label = total, angle=90-((xAvg/(max(xMax)/360))%%180)), x=4.5) +");
		System.out.println("geom_text(aes(y=totalGrupoPos, label = totalGrupoTotal, group=totalGrupoTotal), x=2) +");
		System.out.println("coord_polar(theta='y') +");
		System.out.println("xlim(c(0, 4.5)) + ");
		System.out.println("theme(aspect.ratio=1) +");
		System.out.println("theme_minimal()+");
		System.out.println("theme(");
		System.out.println("axis.title.x = element_blank(),");
		System.out.println("axis.title.y = element_blank(),");
		System.out.println("axis.text.x = element_blank(),");
		System.out.println("axis.text.y = element_blank(),");
		System.out.println("panel.border = element_blank(),");
		System.out.println("panel.grid=element_blank(),");
		System.out.println("axis.ticks = element_blank(),");
		System.out.println("legend.position='left',");
		System.out.println("plot.title=element_text(size=14, face='bold')");
		System.out.println(")");

	}
	
	private static <KEY, ATTRIBUTE extends Attribute<ATTRIBUTE>,	NODE extends NodeGene<KEY, ATTRIBUTE>, GRAPH extends GraphGenes<KEY, ATTRIBUTE, NODE>> double vaa(
			GRAPH grafo,
			String stipo,
			String cor,
			LinkedList<Entry<String, LinkedList<NODE>>> ent, 
			LinkedList<String> total,
			LinkedList<String> tipo,
			LinkedList<String> nome,
			LinkedList<String> cor1,
			LinkedList<String> cor2,
			LinkedList<String> totalGrupoMax,
			LinkedList<String> totalGrupoMin,
			double start,
			OrganismRegistry [] orgs) {
		int tLocal = 0;
		int tGrupo = 0;
		int tExclusivos = 0;
		int i = 0;
		for (Entry<String, LinkedList<NODE>> entry : ent) {
			tGrupo += entry.getValue().size();
			int numOrgs = entry.getKey().replaceAll("0", "").length();
			if(entry.getValue().size() > 100 &&  numOrgs > 1) {
				total.add("" + entry.getValue().size());
				tipo.add(stipo);
				cor1.add(cor);
				if(numOrgs == orgs.length) {
					nome.add("Core genome");
					cor2.add("yellow");
				}
				else {
					nome.add("X" + i++);
					cor2.add("white");
				}
				totalGrupoMin.add("" + start);
				tLocal += entry.getValue().size();
			}
			else if(numOrgs == 1) {
				tExclusivos += entry.getValue().size();
			}
		}
		
		if(tExclusivos > 0) {
			total.add("" + tExclusivos);
			tipo.add(stipo);
			cor1.add(cor);
			cor2.add("green");
			nome.add("Exclusive genes");
			totalGrupoMin.add("" + start);
		}
		
		if((tGrupo - tLocal - tExclusivos) > 0) {
			total.add("" + (tGrupo - tLocal - tExclusivos));
			tipo.add(stipo);
			cor1.add(cor);
			cor2.add("gray");
			nome.add("Others");
			totalGrupoMin.add("" + start);
		}
		while(totalGrupoMin.size() > totalGrupoMax.size())
			totalGrupoMax.add("" + (start + tGrupo));
		return start + tGrupo;
	}
}



