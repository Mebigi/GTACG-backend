

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;


import Structure.Graph.GraphM8;
import Structure.Graph.NodeGene;
import Structure.Registry.Dictionary;
import Structure.Restriction.M8Attribute;

public class ExportHeads {

	public static void main(String[] args) throws IOException {
		File graphFile = null;
		File dicFile = null;
		File outFile = null;		
		for (int i = 0; i < args.length; i++) {
			if("-graph".equals(args[i])) {
				if(args.length > i + 1)
				graphFile = new File(args[i+1]);
				if(!graphFile.exists()) {
					System.out.println("Graph file not found");
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
			else if("-out".equals(args[i])) {
				if(args.length > i + 1)
				outFile = new File(args[i+1]);
				if(!outFile.exists()) {
					System.out.println("Output file not found");
					return;
				}
			}
		}
		
		if(dicFile == null) {
			System.out.println("Dictionary file not specified");
			return;
		}
		if(graphFile == null) {
			System.out.println("Graph file not specified");
			return;
		}
		if(outFile == null) {
			System.out.println("Output file not specified");
			return;
		}
		Dictionary dic = new Dictionary(dicFile);
		GraphM8 graph = new GraphM8(graphFile, dic);
		LinkedList<NodeGene<String, M8Attribute>> heads = graph.connComponentHeads();
		graph.saveNodeList(outFile, heads);
	}
}
