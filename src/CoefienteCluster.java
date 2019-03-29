

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Scanner;

import Structure.Restriction.M8Restriction;

public class CoefienteCluster {
	public static class Reg {
		public M8Restriction rest;
		public double coef;
	}
	
	public LinkedList<Reg> list = new LinkedList<>();
	
	public CoefienteCluster(File in) throws FileNotFoundException {
		Scanner sc = new Scanner(in);
		sc.nextLine();
		sc.nextLine();
		while(sc.hasNextLine()) {
			String line = sc.nextLine();
			if(line.charAt(0) != '#') {
				String[] split = line.split("\t");
				M8Restriction rest = new M8Restriction(
						Float.parseFloat(split[0]), 
						Float.parseFloat(split[1]), 
						Integer.parseInt(split[2]), 
						Float.parseFloat(split[3]), 
						Short.parseShort(split[4]), 
						Float.parseFloat(split[5]), 
						Float.parseFloat(split[6]));
				double coef = Double.parseDouble(split[7]);
				Reg reg = new Reg();
				reg.rest = rest;
				reg.coef = coef;
				list.add(reg);
			}
		}
		sc.close();
		
		Collections.sort(list, new Comparator<Reg>() {
			@Override
			public int compare(Reg o1, Reg o2) {
				double x = o2.coef - o1.coef;
				if(x==0)
					return 0;
				if(x < 0)
					return -1;
				return 1;
			}
		});

	}
	
	public M8Restriction getMin(double maxErro, double minErro) {
		LinkedList<Reg> tmp = new LinkedList<>();
		for (Reg reg : list) {
			if(reg.coef > 1 - maxErro && reg.coef < 1 - minErro)
				tmp.add(reg);
		}
		
		Collections.sort(tmp, new Comparator<Reg>() {
			@Override
			public int compare(Reg o1, Reg o2) {
				double x = o2.rest.getMinPercIdentity() - o1.rest.getMinPercIdentity();
				if(x < 0)
					return -1;
				else if (x > 0)
					return 1;
				
				x = o2.rest.getMinPercLengthAlin() - o1.rest.getMinPercLengthAlin();
				if(x < 0)
					return -1;
				else if(x > 0) 
					return 1;
				return 0;
			}
		});
		System.out.println(tmp.getLast().rest + "\t" + tmp.getLast().coef);
		return tmp.getLast().rest;
	}
}