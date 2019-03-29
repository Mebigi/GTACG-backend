package Metrics;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import Structure.Matrix.DistanceMatrix;
import Structure.Registry.OrganismRegistry;


public class GCContent {
	public static long getGCContent(OrganismRegistry org) throws FileNotFoundException {
		return getGCContent(org.getFile());
	}
	
	public static long getGCContent(File fasta) throws FileNotFoundException{
		Scanner sc = new Scanner(fasta);
		long gc = 0;
		while(!sc.nextLine().contains(">"));
		while(sc.hasNext()) {
			String str = sc.nextLine();
			if(!"".equals(str)) {
				for (int i = 0; i < str.length(); i++) {
					char c = str.charAt(i);
					if(c == 'G' || c == 'C')
						gc++;
				}
			}
		}
		
		sc.close();
		return gc;
	}
	
	public static DistanceMatrix getDistanceMatrix(OrganismRegistry orgs[]) throws FileNotFoundException {
		String label[] = new String[orgs.length];
		long v[] = new long[orgs.length];
		
		for (int i = 0; i < orgs.length; i++) {
			label[i] = orgs[i].getAbbrev();
			v[i] = getGCContent(orgs[i]);
		}
		
		return getDistanceMatrix(label, v);
	}
	
	public static DistanceMatrix getDistanceMatrix(String label[], long v[]) {
		DistanceMatrix m = new DistanceMatrix(label.length);
		for (int i = 0; i < v.length; i++) {
			m.label[i] = label[i];
			for (int j = 0; j < v.length; j++) {
				m.dist[i][j] = Math.abs(v[i]-v[j]);
			}
		}
		return m;
	}
}
