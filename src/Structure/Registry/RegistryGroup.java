package Structure.Registry;

import java.awt.Color;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class RegistryGroup {
	private String name;
	private Color color;
	private int id;
	private HashSet<Registry> group = new HashSet<Registry>();
	
	public RegistryGroup(String name, int id) {
		this.name = name;
		this.id = id;
	}
	
	public RegistryGroup(String name, String color, int id) {
		this.name = name;
		if(color == null || "".equals(color))
			color = null;
		else
			this.color = Color.decode(color);
		this.id = id;
	}
	
	public RegistryGroup(String name, Collection<Registry> group, int id) {
		this.name = name;
		this.group.addAll(group);
		this.id = id;
	}
	
	public RegistryGroup(String name, String color, Collection<Registry> group, int id) {
		this.name = name;
		this.color = Color.decode(color);
		this.group.addAll(group);
		this.id = id;
	}
	
	public void add(Registry reg) {
		group.add(reg);
	}
	
	public boolean contain(Registry reg) {
		return group.contains(reg);
	}
	
	public Set<Registry> getSet() {
		return group;
	}
	
	public int size() {
		return group.size();
	}
	
	public String getName() {
		return name;
	}
	
	public Color getColor() {
		return color;
	}
	
	public int getId() {
		return id;
	}
	
	public LinkedList<Registry> getMembers() {
		return new LinkedList<>(group);
	}
}
