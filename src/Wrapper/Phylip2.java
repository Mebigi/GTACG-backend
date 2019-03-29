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
import ToolkitFile.TreeFile;;

public class Phylip2 {
	public enum MetodosArvore {
		Neighbor, UPGMA;
	} 
	
	public enum MetodosConsenso {
		MajorityRule, MajorityRuleE, Ml, Strict; 
	} 
	
	public enum MetodosDistancia {
		Branch, Symmetric;
	}
	
	public static String gerarArvore(DistanceMatrix matriz, MetodosArvore metodo, File saida) throws IOException, InterruptedException {
		return gerarArvore(matriz, metodo, false, null, null, saida);
	}
	
	public static String gerarArvore(DistanceMatrix matriz, MetodosArvore metodo) throws IOException, InterruptedException {
		return gerarArvore(matriz, metodo, false, null, null, null);
	}
	
	public static String gerarArvore(DistanceMatrix matriz, MetodosArvore metodo, boolean ordemRandomica, Integer multSets, Integer outGroup) throws IOException, InterruptedException {
		return gerarArvore(matriz, metodo, ordemRandomica, multSets, outGroup, null);
	}
	
	public static String gerarArvore(DistanceMatrix matriz, MetodosArvore metodo, boolean ordemRandomica, Integer multSets, Integer outGroup, File saida) throws IOException, InterruptedException {
		Runtime rt = Runtime.getRuntime();
		File infile = new File("infile");
		File outfile = new File("outfile");
		File outtree;
		if(saida == null)
			outtree = new File("outtree");
		else
			outtree = saida;
		
		infile.delete();
		outfile.delete();
		outtree.delete();

		PrintStream out = new PrintStream(infile);
		matriz.printMatrixPhylip(out);
		out.close();
		
		String comando = "./phylip/neighbor2 -i " + infile.getAbsolutePath() +
				" -o outfile -t " + outtree.getAbsolutePath() + " -printStart -printProgress -printTree " + 
				(metodo == null || metodo == MetodosArvore.Neighbor?"-aNeighbor ":"-aUpgma ") +
				(ordemRandomica?"-randOrder ":"") + 
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
		if(saida == null)
			outtree.delete();
		return arvore;
	}
	
	public static String gerarArvore(GenesMatrix matriz, File saida) throws IOException, InterruptedException {
		File entrada = new File(Constants.rand());
		matriz.exportPhylip(new PrintListBinary(), new PrintStream(entrada));
		
		Process process = 
				new ProcessBuilder(new String[] {"bash", "-c", "printf '" + entrada.getAbsolutePath() + "\\ny\\n' | phylip pars"})
                    .redirectErrorStream(true)
                    //.directory(new File("/home/caio/Dropbox/workspace/Ferramentas/resultados/"))
                    .start();
		process.waitFor();
		
		File outtree = new File("outtree");
		File outfile = new File("outfile");
		String arvore = "";
		Scanner sc = new Scanner(outtree);
		while(sc.hasNext()) {
			arvore += sc.nextLine() + "\n";
		}
		sc.close();
		if(saida == null)
			outtree.delete();
		else
			outtree.renameTo(saida);
		outfile.delete();
		entrada.delete();
		
		return arvore;
	}
	
	public static String gerarConsensoArvore(Collection<TreeFile> arvores, MetodosConsenso metodo, double coeficienteMl, Integer outGroup, boolean root) throws IOException, InterruptedException {
		return gerarConsensoArvore(arvores, metodo, coeficienteMl, outGroup, root, null);
	}
	
	public static String gerarConsensoArvore(Collection<TreeFile> arvores, MetodosConsenso metodo, double coeficienteMl, Integer outGroup, boolean root, File saida) throws IOException, InterruptedException {
		LinkedList<String> lista = new LinkedList<String>();
		for (TreeFile arv : arvores) {
			Path path = Paths.get(arv.getAbsolutePath());
			lista.add(new String(Files.readAllBytes(path)));
		}
		return gerarConsensoString(lista, metodo, coeficienteMl, outGroup, root, saida);
	}
	
	public static String gerarConsensoString(Collection<String> arvores, MetodosConsenso metodo, double coeficienteMl, Integer outGroup, boolean root) throws IOException, InterruptedException {
		return gerarConsensoString(arvores, metodo, coeficienteMl, outGroup, root, null);
	}
	
	public static String gerarConsensoString(Collection<String> arvores, MetodosConsenso metodo, double coeficienteMl, Integer outGroup, boolean root, File saida) throws IOException, InterruptedException {
		Runtime rt = Runtime.getRuntime();
		Process p;
		File outtree = new File("outtree");
		if(saida != null)
			outtree = saida;
		
		p = rt.exec("rm intree");
		p = rt.exec("rm outfile");
		p = rt.exec("rm " + outtree.getAbsolutePath());
		
		File intree = new File("intree");
		PrintStream out = new PrintStream(intree);
		for (String arvore : arvores) {
			out.print(arvore + "\n");			
		}
		out.close();
		
		String comando = "./phylip/consense2 -i " + intree.getAbsolutePath() +
				" -o outfile -t " + outtree.getAbsolutePath() + " -printStart -printProgress -printTree ";
		if(metodo == MetodosConsenso.MajorityRule)
			comando += "-aMajority ";
		else if(metodo == MetodosConsenso.MajorityRuleE)
			comando += "-aMajorityE ";
		else if(metodo == MetodosConsenso.Ml)
			comando += "-aMl " + coeficienteMl + " ";
		else if(metodo == MetodosConsenso.Strict)
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
		if(saida == null)
			p = rt.exec("rm " + outtree.getAbsolutePath());
		return arvore;
	}
	
	public static String gerarConsenso(File arvores, MetodosConsenso metodo, double coeficienteMl, Integer outGroup, boolean root) throws IOException, InterruptedException {
		Runtime rt = Runtime.getRuntime();
		Process p;
		p = rt.exec("rm intree");
		p = rt.exec("rm outfile");
		p = rt.exec("rm outtree");
		
		String comando = "./phylip/consense2 -i " + arvores.getAbsolutePath() +
				" -o outfile -t outtree -printStart -printProgress -printTree ";
		if(metodo == MetodosConsenso.MajorityRule)
			comando += "-aMajority ";
		else if(metodo == MetodosConsenso.MajorityRuleE)
			comando += "-aMajorityE ";
		else if(metodo == MetodosConsenso.Ml)
			comando += "-aMl " + coeficienteMl + " ";
		else if(metodo == MetodosConsenso.Strict)
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

	
	public static DistanceMatrix distanciaArvore(Collection<String> arvores, Collection<String> label, MetodosDistancia metodo, boolean root) throws IOException, InterruptedException {
		Runtime rt = Runtime.getRuntime();
		Process p;
		p = rt.exec("rm intree");
		p = rt.exec("rm outfile");
		
		DistanceMatrix m = new DistanceMatrix(arvores.size());
		
		File intree = new File("intree");
		PrintStream out = new PrintStream(intree);
		for (String arvore : arvores) {
			out.print(arvore + "\n");
		}
		int i = 0;
		for (String string : label) {
			m.label[i] = string;
			i++;
		}
		out.close();
		
		String comando = "./phylip/treedist2 -i " + intree.getAbsolutePath() +
				" -o outfile -printStart -printProgress -printTree ";
		if(metodo == MetodosDistancia.Branch)
			comando += "-aBranch ";
		else if(metodo == MetodosDistancia.Symmetric)
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



