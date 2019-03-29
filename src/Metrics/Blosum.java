package Metrics;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

public class Blosum {
	public HashMap<Character, HashMap<Character, Integer>> matrix = new HashMap<>();
	public Blosum(File f) throws FileNotFoundException {
		Scanner sc = new Scanner(f);
		
		sc.nextLine();
		sc.nextLine();
		sc.nextLine();
		sc.nextLine();
		sc.nextLine();
		sc.nextLine();
		
		HashMap<Integer, Character> index = new HashMap<>();
		String header = sc.nextLine().replace("  ", " ").replace(" ", " ");
		String[] sheader = header.split(" ");
		int size = 0;
		for (int i = 2; i < sheader.length; i++) {
			index.put(i-2, sheader[i].charAt(0));
			size++;
		}
		
		int line = 0;
		while(sc.hasNext()) {
			sc.next();
			matrix.put(index.get(line), new HashMap<>());
			for (int i = 0; i < size; i++) {
				matrix.get(index.get(line)).put(index.get(i), sc.nextInt());
			}
			line++;
			
		}
		sc.close();
	}
	
	public int get(char a, char b) {
		return matrix.get(a).get(b);
	}
}
