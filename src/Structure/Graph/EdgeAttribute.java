package Structure.Graph;

import java.util.Collection;

import Structure.Restriction.Attribute;

public class EdgeAttribute<ATTRIBUTE extends Attribute<ATTRIBUTE>>{
	private Node<?, ATTRIBUTE> a;
	private Node<?, ATTRIBUTE> b;

    private ATTRIBUTE[] attributes = null;
    
    public EdgeAttribute(Node<?, ATTRIBUTE> a, Node<?, ATTRIBUTE> b) {
		this.a = a;
		this.b = b;
	}
    
	@SuppressWarnings("unchecked")
	public <NODE extends Node<?, ATTRIBUTE>> NODE diff(NODE node) {
		Node<?, ATTRIBUTE> dif = a;
		if(node == a)
			dif = b;
		return (NODE)dif;
	}
	
	@SuppressWarnings("unchecked")
	public <KEY> Node<KEY, ATTRIBUTE> getA() {
		return (Node<KEY, ATTRIBUTE>)a;
	}
	
	@SuppressWarnings("unchecked")
	public <KEY> Node<KEY, ATTRIBUTE> getB() {
		return (Node<KEY, ATTRIBUTE>)b;
	}

    static int count = 0;
	public void addAttribute(ATTRIBUTE newAttr) {
        if(attributes == null) {
            attributes = newAttr.newArray();
            attributes[0] = newAttr;
        }
        else {
        	boolean betterA = false;
        	int betterB = 0;
        	for (int i = 0; i < attributes.length; i++) {
				if(!betterA && (attributes[i].better(newAttr) || attributes[i].equals(newAttr))) {
					betterA = true;
					break;
				}
				if(!newAttr.better(attributes[i])) {
					betterB++;
				}
			}
        	
        	if(!betterA) {
        		ATTRIBUTE[] tmp = newAttr.newArray(betterB + 1);
        		int j = 1;
        		tmp[0] = newAttr;
        		for (int i = 0; i < attributes.length; i++) {
        			if(!newAttr.better(attributes[i])) {
        				tmp[j] = attributes[i];
        				j++;
        			}
        		}
        		if(attributes.length-betterB != 0) {
        			count+=attributes.length-betterB;
        			if(count%10000 == 0)
            			System.out.println("@" +count);
        		}
        	}
        	else {
        		count++;
        		if(count%100000 == 0)
        			System.out.println("@" +count);
        	}
        	
        	
        	
        	
	        	/*ATTRIBUTE[] tmp = newAttr.newArray(attributes.length + 1);
	        	for (int i = 0; i < attributes.length; i++) {
					tmp[i] = attributes[i];		
				}
	        	tmp[attributes.length] = newAttr;
	        	attributes = tmp;*/
        	/*}
        	else {
        		count++;
        		if(count%10000 == 0)
        			System.out.println("@" +count);
        	}*/
        	
        	/*boolean replaced = false;
        	for (int i = 0; i < attributes.length; i++) {
        		if(!replaced && attributes[i].better(newAttr)) {
        			attributes[i] = newAttr;
        			replaced = true;
        		}
        		else if(newAttr.better(attributes[i]))
        			replaced = true;
			}
        	if(!replaced) {
	        	ATTRIBUTE[] tmp = newAttr.newArray(attributes.length + 1);
	        	for (int i = 0; i < attributes.length; i++) {
					tmp[i] = attributes[i];
						
				}
	        	tmp[attributes.length] = newAttr;
	        	attributes = tmp;
        	}*/
        }
    }
     
    public ATTRIBUTE[] getAttributes() {
        return attributes;
    }
     
    public void addAttributes(Collection<ATTRIBUTE> newAttr) {
        attributes = newAttr.toArray(newAttr.iterator().next().newArray());
    }
     
    public void addAttributes(ATTRIBUTE [] newAttr) {
        attributes = newAttr;
    }
     
    public void clearAttributes() {
        attributes = null;
    }
 
    public void copyValues(EdgeAttribute<ATTRIBUTE>  edge) {
    	attributes = edge.attributes;
    }
}