package Wrapper;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;

import Structure.Constants;
import Structure.Graph.PhylogeneticTree;
import Structure.Matrix.DistanceMatrix;
import Structure.Matrix.GenesMatrix;
import Structure.Matrix.GenesMatrix.PrintListBinary;
import Structure.Registry.OrganismRegistry;
import ToolkitFile.ToolkitBaseFile;
import ToolkitFile.TreeFile;;

public class Phylip {
	public enum TreeMethod {
		Neighbor, UPGMA;
	} 
	
	public enum ConsenseTreeMethod {
		MajorityRule, MajorityRuleE, Ml, Strict; 
	} 
	
	public enum DistanceTreeMethod {
		Branch, Symmetric;
	}
	
	public static String makeTree(DistanceMatrix matrix, TreeMethod method, File out) throws IOException, InterruptedException {
		return makeTree(matrix, method, null, out);
	}
	
	public static String makeTree(DistanceMatrix matrix, TreeMethod method, Integer randomSeed, File out) throws IOException, InterruptedException {
		ToolkitBaseFile infile = new ToolkitBaseFile("."+Constants.rand());
		ToolkitBaseFile outtree = new ToolkitBaseFile("outtree");
		ToolkitBaseFile outfile = new ToolkitBaseFile("outfile");
		infile.delete();
		outtree.delete();
		outfile.delete();
		
		DistanceMatrix mClone = new DistanceMatrix(matrix.label.length);
		for (int i = 0; i < matrix.dist.length; i++) {
			mClone.label[i] = "a" + i + "a";
			for (int j = 0; j < matrix.dist[i].length; j++) {
				mClone.dist[i][j] = matrix.dist[i][j];
			}
		}
		
		mClone.printMatrixPhylip(new PrintStream(infile));

		String command = "printf '" + infile.getAbsolutePath() + "\\n";
		if(TreeMethod.UPGMA == method)
			command += "N\\n";
		if(randomSeed != null) 
			command += "J\\n" + randomSeed + "\\n";
		command += "Y\\n' | phylip neighbor";

			
		Process process = new ProcessBuilder(new String[] {"bash", "-c", command}).redirectErrorStream(true).start();
		process.waitFor();

		String arvore = outtree.load();
		for (int i = 0; i < matrix.label.length; i++) {
			arvore = arvore.replace(mClone.label[i], matrix.label[i]);
		}
		if(out == null)
			outtree.delete();
		else
			outtree.renameTo(out);
		outfile.delete();
		infile.delete();

		return arvore;
	}
	
	public static String makeTree(GenesMatrix matrix, File out) throws IOException, InterruptedException {
		ToolkitBaseFile infile = new ToolkitBaseFile(Constants.rand());
		ToolkitBaseFile outtree = new ToolkitBaseFile("outtree");
		ToolkitBaseFile outfile = new ToolkitBaseFile("outfile");
		infile.delete();
		outtree.delete();
		outfile.delete();
		
		HashMap<OrganismRegistry, String> map = new HashMap<>();
		OrganismRegistry[] orgs = matrix.getOrgs();
		for (int i = 0; i < orgs.length; i++) {
			map.put(orgs[i], "a" + i + "a");
		}
		
		matrix.exportPhylip(new PrintListBinary(), new PrintStream(infile), map);
		
		String command = "printf '" + infile.getAbsolutePath() + "\\nV\\n1\\ny\\n' | phylip pars";
		Process process = new ProcessBuilder(new String[] {"bash", "-c", command}).redirectErrorStream(true).start();
		process.waitFor();
		
		
		ToolkitBaseFile outReplace = new ToolkitBaseFile(outtree.getAbsolutePath());
		for (int i = 0; i < orgs.length; i++) {
			outReplace.replace(map.get(orgs[i]), orgs[i].getAbbrev());
		}
		String arvore = outtree.load();
		
		if(out == null)
			outtree.delete();
		else {
			outtree.renameTo(out);
		}
		outfile.delete();
		infile.delete();
		
		return arvore;
	}
	
	public static String makeConsenseTreeFile(Collection<TreeFile> trees, ConsenseTreeMethod method, double coefficientMl, boolean root, File out) throws IOException, InterruptedException {
		LinkedList<String> list = new LinkedList<String>();
		for (TreeFile tree : trees)
			list.add(tree.load().replaceAll("\n", ""));
		return makeConsenseTreeString(list, method, coefficientMl, root, out);
	}
	
	public static String makeConsensePhylogeneticTree(Collection<PhylogeneticTree> trees, ConsenseTreeMethod method, double coefficientMl, boolean root, File out) throws IOException, InterruptedException {
		LinkedList<String> list = new LinkedList<String>();
		for (PhylogeneticTree tree : trees)
			list.add(tree.print());
		return makeConsenseTreeString(list, method, coefficientMl, root, out);
	}
	
	public static String makeConsenseTreeString(Collection<String> trees, ConsenseTreeMethod method, double coefficientMl, boolean root, File out) throws IOException, InterruptedException {
		ToolkitBaseFile infile = new ToolkitBaseFile(Constants.rand());
		ToolkitBaseFile outtree = new ToolkitBaseFile("outtree");
		ToolkitBaseFile outfile = new ToolkitBaseFile("outfile");
		infile.delete();
		outtree.delete();
		outfile.delete();
		
		PrintStream stream = new PrintStream(infile);
		for (String string : trees) {
			stream.println(string);
		}
		stream.close();
		
		String command = "printf '" + infile.getAbsolutePath() + "\\n";
		if(ConsenseTreeMethod.Strict == method)
			command += "C\\n";
		else if(ConsenseTreeMethod.MajorityRule == method) 
			command += "C\\nC\\n";
		else if(ConsenseTreeMethod.Ml == method) 
			command += "C\\nC\\nC\\n";

		if(root)
			command += "R\\n";
		
		command += "Y\\n";
		if(ConsenseTreeMethod.Ml == method)
			command += coefficientMl + "\\n";
		command += "' | phylip consense";
		Process process = new ProcessBuilder(new String[] {"bash", "-c", command}).redirectErrorStream(true).start();
		process.waitFor();
		
		String arvore = outtree.load();
		if(out == null)
			outtree.delete();
		else
			outtree.renameTo(out);
		outfile.delete();
		infile.delete();
		
		return arvore;
	}
	
	
	public static DistanceMatrix getDistanceTree(String trees[], Collection<String> labels, DistanceTreeMethod method, boolean root) throws IOException, InterruptedException {
		ToolkitBaseFile infile = new ToolkitBaseFile("." + Constants.rand());
		ToolkitBaseFile outfile = new ToolkitBaseFile("outfile");
		infile.delete();
		outfile.delete();
		
		DistanceMatrix m = new DistanceMatrix(trees.length);
		PrintStream stream = new PrintStream(infile);
		
		
		String[] arrayLabels = labels.toArray(new String[1]);
		for (int i = 0; i < trees.length; i++) {
			m.label[i] = arrayLabels[i];
			stream.println(trees[i]);
			
		}
		stream.close();
		
		String command = "printf '" + infile.getAbsolutePath() + "\\n";
		if(DistanceTreeMethod.Symmetric == method)
			command += "D\\n";
		
		if(root)
			command += "R\\n";
		command += "2\\nP\\nS\\nY\\n";
		command += "' | phylip treedist";
		Process process = new ProcessBuilder(new String[] {"bash", "-c", command}).redirectErrorStream(true).start();
		process.waitFor();
		
		Scanner sc = new Scanner(outfile);
		while(sc.hasNext()) {
			int x = sc.nextInt()-1;
			int y = sc.nextInt()-1;
			double v = Double.parseDouble(sc.next());
			m.dist[x][y] = v;
			m.dist[y][x] = v;
		}
		sc.close();
		infile.delete();
		outfile.delete();
		return m;
	}
}