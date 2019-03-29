package Metrics;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import Structure.Constants;
import Structure.Matrix.DistanceMatrix;
import Structure.Registry.OrganismRegistry;

public class KMer {
	private long vet[];
	
	public KMer(int janela, File fasta) throws FileNotFoundException {
		vet = new long[(int) Math.pow(4, janela)];
		
		Scanner sc = new Scanner(fasta);
		String ant = "";
		
		while(!sc.nextLine().contains(">"));
		while(sc.hasNext()) {
			String str = sc.nextLine();
			if(!"".equals(str)) {
				str = ant + str;
				for (int i = 0; i < str.length()-janela; i++) {
					int pos = 0;
					for (int j = 0; j < janela; j++) {
						pos += Constants.nuclToInt[str.charAt(i+j)]*Math.pow(4, j);
					}
					vet[pos]++;
				}
				ant = str.substring(str.length()-janela);
			}
		}
		
		sc.close();
	}
	
	public double distanciaEuclidiana(KMer b) {
		long dist = 0;
		for (int i = 0; i < this.vet.length; i++) {
			long a = this.vet[i] - b.vet[i];
			dist += a*a;
		}
		return Math.sqrt((float)dist);
	}
	
	public static DistanceMatrix matrizDistanciaEuclidiana(OrganismRegistry orgs[], int janela) throws FileNotFoundException {
		KMer vetor[] = new KMer[orgs.length];
		String label[] = new String[orgs.length];
		for (int i = 0; i < orgs.length; i++) {
			label[i] = orgs[i].getAbbrev();
			vetor[i] = new KMer(janela, orgs[i].getFile()); 
		}
		return matrizDistanciaEuclidiana(label, vetor);
	}
	
	public static DistanceMatrix matrizDistanciaEuclidiana(String label[], KMer vetor[]) {
		DistanceMatrix matriz = new DistanceMatrix(label.length);
		for (int i = 0; i < label.length; i++) {
			matriz.label[i] = label[i];
			for (int j = 0; j < label.length; j++) {
				matriz.dist[i][j] = vetor[i].distanciaEuclidiana(vetor[j]);
			}
		}
		return matriz;
	}
	
	public double distanciaManhattan(KMer b) {
		long dist = 0;
		for (int i = 0; i < this.vet.length; i++) {
			System.out.print("*** " + i);
			long a = this.vet[i] - b.vet[i];
			if(a >= 0)
				dist += a;
			else
				dist += -a;
		}
		return Math.sqrt((float)dist);
	}
	
	public static DistanceMatrix matrizDistanciaManhattan(String label[], KMer vetor[]) {
		DistanceMatrix matriz = new DistanceMatrix(label.length);
		for (int i = 0; i < label.length; i++) {
			//System.out.print("*** " + i);
			matriz.label[i] = label[i];
			for (int j = 0; j < label.length; j++) {
				matriz.dist[i][j] = vetor[i].distanciaManhattan(vetor[j]);
			}
		}
		return matriz;
	}
}