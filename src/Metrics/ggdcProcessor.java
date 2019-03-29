package Metrics;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Scanner;

import Structure.Matrix.DistanceMatrix;

public class ggdcProcessor {
	public enum Coluna {
		queryGenome,
		referenceGenome,
		formula1DDH,
		formula1ModelCI,
		formula1Distance,
		formula1ProbDDH,
		formula2DDH,
		formula2ModelCI,
		formula2Distance,
		formula2ProbDDH,
		formula3DDH,
		formula3ModelCI,
		formula3Distance,
		formula3ProbDDH,
		gcDefference
	}
	
	public static void juntarArquivos(File dir, File saida) throws FileNotFoundException {
		PrintStream out = new PrintStream(saida);
		out.println(",,Formula 1,,,,Formula 2,,,,Formula 3,,,,G+C difference");
		out.println("Query genome,Reference genome,DDH,Model C.I.,Distance,Prob. DDH >= 70%,DDH,Model C.I.,Distance,Prob. DDH >= 70%,DDH,Model C.I.,Distance,Prob. DDH >= 70%,");
		for (File f : dir.listFiles()) {
			Scanner sc = new Scanner(f);
			sc.nextLine();
			sc.nextLine();
			while(sc.hasNextLine()) {
				out.println(sc.nextLine());
			}
			sc.close();
		}
		out.close();
	}
		
	public static DistanceMatrix ggdcFileToMatriz(File ggdcFile, Coluna coluna) throws IOException, InterruptedException {
		Hashtable<String, Double> hash = new Hashtable<String, Double>();
		Hashtable<String, Integer> elementos = new Hashtable<String, Integer>();
		
		int cont = 0;
		Scanner sc = new Scanner(ggdcFile);
		sc.nextLine();
		sc.nextLine();
		while (sc.hasNextLine()) {
			String [] dados = sc.nextLine().split(",");
			String a, b;
			if(dados[Coluna.queryGenome.ordinal()].compareTo(dados[Coluna.referenceGenome.ordinal()]) > 0) {
				a = dados[Coluna.queryGenome.ordinal()];
				b = dados[Coluna.referenceGenome.ordinal()];
			}
			else {
				a = dados[Coluna.referenceGenome.ordinal()];
				b = dados[Coluna.queryGenome.ordinal()];
			}
			String valorS = "0";
			if(coluna.ordinal() < dados.length)
				valorS = dados[coluna.ordinal()];
			if("".equals(valorS))
				valorS = "0";
			double valor = Double.parseDouble(valorS);
			hash.put(a + "@" + b, valor);
			if(!elementos.containsKey(a))
				elementos.put(a, cont++);
			if(!elementos.containsKey(b))
				elementos.put(b, cont++);
		}
		sc.close();
		
		DistanceMatrix m = new DistanceMatrix(cont);
		for (String key : elementos.keySet()) {
			m.label[elementos.get(key)] = key;
		}
		
		for (String key : hash.keySet()) {
			String [] valores = key.split("@"); 
			m.dist[elementos.get(valores[0])][elementos.get(valores[1])] = hash.get(key);
			m.dist[elementos.get(valores[1])][elementos.get(valores[0])] = hash.get(key);
		}
		return m;
	}
	
	public static String gerarDendograma(LinkedList<LinkedList<String>> [] hist, int k, LinkedList<String> grupo, int tamanho) {
		if(k >= hist.length) {
			String result = "";
			for (Object object : grupo) {
				result = result + object + ":" + tamanho + ",";
			}
			result = result.substring(0, result.length()-1);
			return result + ",";
		}
		
        HashSet<LinkedList<String>> aceitos = new HashSet<LinkedList<String>>();
		for (Object obj : grupo) {
			for (LinkedList<String> grupoVerif : hist[k]) {
				for (String objVerif : grupoVerif) {
					if((obj).equals(objVerif)) {
						aceitos.add(grupoVerif);
					}
				}
			}
		}
		
		if(aceitos.size() == 1)
			return gerarDendograma(hist, k + 1, grupo, tamanho + 1);
		String result = "(";
		for (LinkedList<String> linkedList : aceitos) {
			String seg = gerarDendograma(hist, k + 1, linkedList, 1);
			result = result + seg.substring(0, seg.length() - 1) + ",";
		}
		result = result.substring(0, result.length()-1) + ")a" + (k-tamanho) + ".." + k + ":" + tamanho;
		return result + ";";
	}
}