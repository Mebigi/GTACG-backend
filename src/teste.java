import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.Scanner;

import org.forester.application.gene_tree_preprocess;

import Structure.Constants;
import Structure.Graph.GraphM8;
import Structure.Graph.NodeGene;
import Structure.Registry.Dictionary;
import Structure.Registry.DictionaryGbf;
import Structure.Registry.GeneRegistry;
import Structure.Registry.GeneRegistryFna;
import Structure.Restriction.M8Attribute;
import Structure.Restriction.M8Restriction;
import Structure.Restriction.Restriction;
import ToolkitFile.AlignmentFile;
import ToolkitFile.TreeFile;
import ToolkitFile.ToolkitBaseFile.FileType;
import Wrapper.Phyml;

public class teste {

	public static void main(String[] args) throws IOException, InterruptedException {
		AlignmentFile in = new AlignmentFile("/media/caio/08CE4A891258E142/dados/xantho15/prokka/genes.align", FileType.fasta);
		TreeFile out = new TreeFile("teste.tree");
		Phyml.makeTree(in, out);
		
		/*Constants.inicializacao();
		Dictionary dic = new Dictionary(new File("/media/caio/08CE4A891258E142/dados/xantho15/gb/xantho15.dic"));
		
		M8Restriction rest = new M8Restriction((float)0.96, (float)0.96, 60, 10, (short)5, Math.pow(10, -10), 100);
		GraphM8 graph = new GraphM8(new File("/media/caio/08CE4A891258E142/dados/xantho15/gb/todos.faa.m8"), dic, rest);
		
		graph.export(new File("xantho15.graph"));
		LinkedList<NodeGene<String, M8Attribute>> heads = graph.connComponentHeads();
		graph.saveNodeList(new File("xantho15.heads"), heads);*/		
	}
}
