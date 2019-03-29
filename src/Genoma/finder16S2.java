package Genoma;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Scanner;

import Structure.Constants;
import Structure.Registry.OrganismRegistry;

public class finder16S2 {
	public static String inverso(String s) {
		String r = "";
		for (int i = 0; i < s.length(); i++) {
			if(s.charAt(i) == 'A')
				r = r + 'T';
			if(s.charAt(i) == 'T')
				r = r + 'A';
			if(s.charAt(i) == 'C')
				r = r + 'G';
			if(s.charAt(i) == 'G')
				r = r + 'C';
		}
		return r;
	}
	
	public static String inverso2(String s) {
		String r = "";
		for (int i = s.length() - 1; i >= 0; i--) {
			if(s.charAt(i) == 'A')
				r = r + 'T';
			if(s.charAt(i) == 'T')
				r = r + 'A';
			if(s.charAt(i) == 'C')
				r = r + 'G';
			if(s.charAt(i) == 'G')
				r = r + 'C';
		}
		return r;
	}
	
	public static void finder(Collection<OrganismRegistry> regs) throws FileNotFoundException {
		Hashtable<String, String> hash = new Hashtable<String, String>();

		File saida = new File("saida");
		File saidaNormal = new File("saidaNormal");
		File saidaInverso = new File("saidaInverso");
		PrintStream out = new PrintStream(saida);
		PrintStream outNormal = new PrintStream(saidaNormal);
		PrintStream outInverso = new PrintStream(saidaInverso);

		Hashtable<String, Integer> marcadores = new Hashtable<String, Integer>();
		marcadores.put("GGTGGAAGACCTGGTCAAGA", 1);
		marcadores.put("GGTGGAAGACCTGGTCAAGA", 1);
		marcadores.put("GGTGGAAGACCTGGTCAAGA", 1);
		marcadores.put("GGTGGAAGACCTGGTCAAGA", 1);
		
		marcadores.put("TCCTTGACYTCGGTGAACTC", 2);
		
		Hashtable<String, Integer> inversoL = new Hashtable<String, Integer>();
		/*for (String string : marcadores.keySet()) {
			outNormal.println("### " + string);
			outInverso.println("### " + string);
		}*/
		for (String string : marcadores.keySet()) {
			inversoL.put(inverso2(string), marcadores.get(string));
			/*outNormal.println("!!! " + inverso2(string));
			outInverso.println("!!! " + inverso2(string));*/
		}
		
		
		for (OrganismRegistry reg : regs) {
			File arquivo = reg.getFile();
			System.out.println(arquivo.getAbsolutePath());
			outNormal.println(arquivo.getAbsolutePath());
			outInverso.println(arquivo.getAbsolutePath());
			
			int inicioNormal = -1;
			int inicioInverso = -1;
			
			Scanner sc = new Scanner(arquivo);
			int numLinha = 0;
			sc.nextLine();
			String linhaAnt = sc.nextLine();
			while(sc.hasNextLine()) {
				String linhaAtual = sc.nextLine();
				String linha = linhaAnt + linhaAtual;
				for (String marcador : marcadores.keySet()) {
					int lastof = linha.lastIndexOf(marcador);
					if(lastof >= 0 && lastof < 70) {
						outNormal.println("1 -- " + marcadores.get(marcador) + "\t" + numLinha + "\t" + lastof + "\t" + (numLinha*70+linha.lastIndexOf(marcador)) + "\t" + marcador);
						if(marcadores.get(marcador) == 1)
							inicioNormal = (numLinha*70+linha.lastIndexOf(marcador));
						else if(marcadores.get(marcador) == 12) {
							out.println(">gi|16S_Invertido|" + arquivo.getName() + "|" + + inicioNormal + ".." + (numLinha*70+linha.lastIndexOf(marcador)));
							//out.println(Ferramentas.fasta(filtarSeq(arquivo, inicioNormal-7, (numLinha*70+linha.lastIndexOf(marcador))+30)));
							inicioNormal = -1;
						}
					}
				}
				for (String marcador : inversoL.keySet()) {
					int lastof = linha.lastIndexOf(marcador);
					if(lastof >= 0 && lastof < 70) {
						outInverso.println("2 -- " + inversoL.get(marcador) + "\t" + numLinha + "\t" + lastof + "\t" + (numLinha*70+linha.lastIndexOf(marcador)) + "\t" + marcador);
						if(inversoL.get(marcador) == 12)
							inicioInverso = (numLinha*70+linha.lastIndexOf(marcador));
						else if(inversoL.get(marcador) == 1) {
							out.println(">gi|16S_Invertido|" + arquivo.getName() + "|" + + inicioInverso + ".." + (numLinha*70+linha.lastIndexOf(marcador)));
							//out.println(Ferramentas.fasta(inverso2(filtarSeq(arquivo, inicioInverso-13, (numLinha*70+linha.lastIndexOf(marcador)) + 27))));
							inicioInverso = -1;
						}
					}
				}
	
				numLinha++;
				linhaAnt = linhaAtual;
				if(numLinha%1000 == 0)
					System.out.println("### " + numLinha);
			}
			sc.close();
		}
		out.close();
		outNormal.close();
		outInverso.close();
	}
	
	public static String filtarSeq(File arquivo, int inicio, int fim) throws FileNotFoundException {
		String resultado = "";
		Scanner sc = new Scanner(arquivo);
		sc.nextLine();
		int cont = 0;
		while(cont < fim && sc.hasNext()) {
			String linha = sc.nextLine();
			int l = cont + linha.length() - inicio;
			String trecho = "";
			if(l > 0) {
				trecho = linha;
				if(inicio > cont)
				trecho = trecho.substring(inicio-cont);
			}
			if(cont + trecho.length() > fim) {
				trecho = trecho.substring(0, fim - cont);
			}
			resultado = resultado + trecho;
			cont += linha.length();
				
		}
		sc.close();
		return resultado;
	}
	
	public static void testarRepeticao(File arquivo) throws FileNotFoundException {
		File saida = new File("saidaadfadf");
		PrintStream out = new PrintStream(saida);
		
		Hashtable<String, String> hash = new Hashtable<String, String>();
		Scanner sc = new Scanner(arquivo);
		while(sc.hasNext()) {
			String a = sc.nextLine().replace("|", "asf");
			String linha = sc.nextLine();
			//String split[] = a.split(regex)split("|");
			System.out.println(a.split("asf")[2]);
			
			if(hash.containsKey(a.split("asf")[2])) {
				String linha2 = hash.get(a.split("asf")[2]);
				if(linha.length() != linha2.length()) {
					System.out.println("!" + a.split("asf")[2]);
				}
				else for (int i = 0; i < linha.length(); i++) {
					if(linha.charAt(i) != linha2.charAt(i))
						System.out.println(a.split("asf")[2] + "\t" + i + "\t" + linha.charAt(i) + linha2.charAt(i));
				}
			}
			else {
				hash.put(a.split("asf")[2], linha);
				out.println(a.replace("asf", "|").replace("_Invertido", ""));
				//out.println(Ferramentas.fasta(linha));
			}
		}
		out.close();
		sc.close();
	}
	
	public static String fileTo(File f) throws FileNotFoundException {
		Scanner sc = new Scanner(f);
		sc.nextLine();
		StringBuilder seq = new StringBuilder();
		while(sc.hasNextLine()) {
			seq.append(sc.nextLine());
		}
		sc.close();
		System.out.println(seq.length());
		return seq.substring(1981104,1982084);
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		Constants.inicializacao();
		//System.out.println("TCCTTGACYTCGGTGAACTC");
		System.out.println(Constants.formatComplementSeq("CATTCYAGGTTGGTCTGRTT", 600));
		
		
		
		
		
		
		
		//testarRepeticao(new File("/home/caio/Dropbox/workspace/Ferramentas/16S_xantho15_invertido"));
		
		/*LinkedList<Ferramentas.Bacteria> lista = Ferramentas.ler(new File("/home/caio/Dropbox/xantho15/"), ".fna");
		LinkedList<File> genomas = new LinkedList<File>();
		for (Ferramentas.Bacteria bacteria : lista) {
			genomas.add(bacteria.genoma);
		}
		finder(genomas);*/
	}

}
