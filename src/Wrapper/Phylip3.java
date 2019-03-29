package Wrapper;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Scanner;

import Structure.Constants;
import Structure.Matrix.DistanceMatrix;
import Structure.Matrix.GenesMatrix;
import Structure.Matrix.GenesMatrix.PrintListBinary;
import ToolkitFile.ToolkitBaseFile;
import ToolkitFile.TreeFile;;

public class Phylip3 {
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
		return makeTree(matrix, method, false, null, null, out);
	}
	
	public static String makeTree(DistanceMatrix matrix, TreeMethod method) throws IOException, InterruptedException {
		return makeTree(matrix, method, false, null, null, null);
	}
	
	public static String makeTree(DistanceMatrix matrix, TreeMethod method, boolean randomOrder, Integer multSets, Integer outGroup) throws IOException, InterruptedException {
		return makeTree(matrix, method, randomOrder, multSets, outGroup, null);
	}
	
	public static String makeTree(DistanceMatrix matrix, TreeMethod method, boolean randomOrder, Integer multSets, Integer outGroup, File out) throws IOException, InterruptedException {
		Runtime rt = Runtime.getRuntime();
		File infile = new File("infile");
		File outfile = new File("outfile");
		File outtree;
		if(out == null)
			outtree = new File("outtree");
		else
			outtree = out;
		
		infile.delete();
		outfile.delete();
		outtree.delete();

		PrintStream print = new PrintStream(infile);
		matrix.printMatrixPhylip(print);
		print.close();
		
		String comando = "./phylip/neighbor2 -i " + infile.getAbsolutePath() +
				" -o outfile -t " + outtree.getAbsolutePath() + " -printStart -printProgress -printTree " + 
				(method == null || method == TreeMethod.Neighbor?"-aNeighbor ":"-aUpgma ") +
				(randomOrder?"-randOrder ":"") + 
				(multSets != null?"-mutSet " + multSets:"")	+
				(outGroup != null?"-outgroup " + outGroup:"");
		
		Process p = rt.exec(comando, new String[0], null);
		p.waitFor();

		String arvore = "";
		
		Scanner sc = new Scanner(outtree);
		while(sc.hasNext()) {
			arvore += sc.nextLine() + "\n";
		}
		sc.close();
		
		infile.delete();
		outfile.delete();
		if(out == null)
			outtree.delete();
		return arvore;
	}
	
	public static String makeTree(GenesMatrix matrix, File out) throws IOException, InterruptedException {
		ToolkitBaseFile entrada = new ToolkitBaseFile(Constants.rand());
		matrix.exportPhylip(new PrintListBinary(), new PrintStream(entrada));
		
		Process process = new ProcessBuilder(new String[] {"bash", "-c", "printf '" + entrada.getAbsolutePath() + "\\ny\\n' | phylip pars"})
                    .redirectErrorStream(true)
                    .start();
		process.waitFor();
		
		File outtree = new File("outtree");
		File outfile = new File("outfile");
		String arvore = entrada.load();
		if(out == null)
			outtree.delete();
		else
			outtree.renameTo(out);
		outfile.delete();
		entrada.delete();
		
		return arvore;
	}
	
	public static String makeConsenseTree(Collection<TreeFile> trees, ConsenseTreeMethod method, double coefficientMl, Integer outGroup, boolean root) throws IOException, InterruptedException {
		return makeConsenseTree(trees, method, coefficientMl, outGroup, root, null);
	}
	
	public static String makeConsenseTree(Collection<TreeFile> trees, ConsenseTreeMethod method, double coefficientMl, Integer outGroup, boolean root, File out) throws IOException, InterruptedException {
		LinkedList<String> list = new LinkedList<String>();
		for (TreeFile arv : trees) {
			Path path = Paths.get(arv.getAbsolutePath());
			list.add(new String(Files.readAllBytes(path)));
		}
		return gerarConsensoString(list, method, coefficientMl, outGroup, root, out);
	}
	
	public static String gerarConsensoString(Collection<String> trees, ConsenseTreeMethod method, double coefficientMl, Integer outGroup, boolean root) throws IOException, InterruptedException {
		return gerarConsensoString(trees, method, coefficientMl, outGroup, root, null);
	}
	
	public static String gerarConsensoString(Collection<String> trees, ConsenseTreeMethod method, double coefficientMl, Integer outGroup, boolean root, File out) throws IOException, InterruptedException {
		Runtime rt = Runtime.getRuntime();
		Process p;
		File outtree = new File("outtree");
		if(out != null)
			outtree = out;
		
		p = rt.exec("rm intree");
		p = rt.exec("rm outfile");
		p = rt.exec("rm " + outtree.getAbsolutePath());
		
		File intree = new File("intree");
		PrintStream print = new PrintStream(intree);
		for (String arvore : trees) {
			print.print(arvore + "\n");			
		}
		print.close();
		
		String comando = "./phylip/consense2 -i " + intree.getAbsolutePath() +
				" -o outfile -t " + outtree.getAbsolutePath() + " -printStart -printProgress -printTree ";
		if(method == ConsenseTreeMethod.MajorityRule)
			comando += "-aMajority ";
		else if(method == ConsenseTreeMethod.MajorityRuleE)
			comando += "-aMajorityE ";
		else if(method == ConsenseTreeMethod.Ml)
			comando += "-aMl " + coefficientMl + " ";
		else if(method == ConsenseTreeMethod.Strict)
			comando += "-aStrict ";
		if(root)
			comando += "-root ";
		else
			comando += "-noroot ";
		if(outGroup != null)
			comando += "-outgroup " + outGroup + " ";
		
		p = rt.exec(comando, new String[0], null);
		p.waitFor();
		
		String arvore = "";
		
		Scanner sc = new Scanner(outtree);
		while(sc.hasNext()) {
			arvore += sc.nextLine() + "\n";
		}
		sc.close();
		
		p = rt.exec("rm intree");
		p = rt.exec("rm outfile");
		if(out == null)
			p = rt.exec("rm " + outtree.getAbsolutePath());
		return arvore;
	}
	
	public static String makeConsenseTree(File trees, ConsenseTreeMethod method, double coefficientMl, Integer outGroup, boolean root) throws IOException, InterruptedException {
		Runtime rt = Runtime.getRuntime();
		Process p;
		p = rt.exec("rm intree");
		p = rt.exec("rm outfile");
		p = rt.exec("rm outtree");
		
		String comando = "./phylip/consense2 -i " + trees.getAbsolutePath() +
				" -o outfile -t outtree -printStart -printProgress -printTree ";
		if(method == ConsenseTreeMethod.MajorityRule)
			comando += "-aMajority ";
		else if(method == ConsenseTreeMethod.MajorityRuleE)
			comando += "-aMajorityE ";
		else if(method == ConsenseTreeMethod.Ml)
			comando += "-aMl " + coefficientMl + " ";
		else if(method == ConsenseTreeMethod.Strict)
			comando += "-aStrict ";
		if(root)
			comando += "-root ";
		else
			comando += "-noroot ";
		if(outGroup != null)
			comando += "-outgroup " + outGroup + " ";
		
		p = rt.exec(comando, new String[0], null);
		p.waitFor();
		
		String arvore = "";
		
		Scanner sc = new Scanner(new File("outtree"));
		while(sc.hasNext()) {
			arvore += sc.nextLine() + "\n";
		}
		sc.close();
		
		p = rt.exec("rm intree");
		p = rt.exec("rm outfile");
		p = rt.exec("rm outtree");
		return arvore;
	}

	
	public static DistanceMatrix getDistanceTree(Collection<String> trees, Collection<String> labels, DistanceTreeMethod method, boolean root) throws IOException, InterruptedException {
		Runtime rt = Runtime.getRuntime();
		Process p;
		p = rt.exec("rm intree");
		p = rt.exec("rm outfile");
		
		DistanceMatrix m = new DistanceMatrix(trees.size());
		
		File intree = new File("intree");
		PrintStream out = new PrintStream(intree);
		for (String arvore : trees) {
			out.print(arvore + "\n");
		}
		int i = 0;
		for (String string : labels) {
			m.label[i] = string;
			i++;
		}
		out.close();
		
		String comando = "./phylip/treedist2 -i " + intree.getAbsolutePath() +
				" -o outfile -printStart -printProgress -printTree ";
		if(method == DistanceTreeMethod.Branch)
			comando += "-aBranch ";
		else if(method == DistanceTreeMethod.Symmetric)
			comando += "-aSymmetric ";
		if(root)
			comando += "-root ";
		else
			comando += "-noroot ";
		
		p = rt.exec(comando, new String[0], null);
		p.waitFor();
		
		Scanner sc = new Scanner(new File("outfile"));
		while(sc.hasNext()) {
			m.dist[sc.nextInt()-1][sc.nextInt()-1] = Double.parseDouble(sc.next());
		}
		sc.close();
		
		p = rt.exec("rm intree");
		p = rt.exec("rm outfile");
		return m;
	}
}



