package ToolkitFile;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;

public class FastaFile extends ToolkitBaseFile {
	private static final long serialVersionUID = 1L;

	public FastaFile(String pathname, SequenceType seqType) {
		super(pathname);
		this.seqType = seqType;
		this.fileType = ToolkitBaseFile.FileType.fasta;
	}
	
	public FastaFile clone(String pathname) {
		return new FastaFile(pathname, seqType);
	}
	
	public String filter(String gene) throws FileNotFoundException {
		return filter(gene, null);
	}
	
	public String filter(String gene, String header) throws FileNotFoundException {
		boolean print = false;
		String result = "";

		Scanner sc = new Scanner(this);
		while(sc.hasNext()) {
			String line = sc.nextLine();
			if(line.charAt(0) == '>') {
				print = false;
				if(line.contains(gene)) {
					print = true;
					if(header == null)
						result = result + line + "\n";
					else
						result = result + header + "\n";
				}
			}
			else if(print)
				result = result + line + "\n";
		}
		sc.close();
		return result;
	}
	
	public String filter(Collection<String> gene) throws FileNotFoundException {
		boolean print = false;
		String result = "";

		Scanner sc = new Scanner(this);
		while(sc.hasNext()) {
			String line = sc.nextLine();
			if(line.charAt(0) == '>') {
				print = false;
				for (String string : gene) {
					if (line.contains(string)) {
						print = true;
						break;
					}
				}
			}
			
			if(print)
				result = result + line + "\n";
		}
		sc.close();
		return result;		
	}
	
	public String filter(Map<String, String> gene) throws FileNotFoundException {
		boolean print = false;
		String result = "";

		Scanner sc = new Scanner(this);
		while(sc.hasNext()) {
			String line = sc.nextLine();
			if(line.charAt(0) == '>') {
				print = false;
				for (String string : gene.keySet()) {
					if (line.contains(string)) {
						print = true;
						result = result + gene.get(string) + "\n";
						break;
					}
				}
			}
			else if(print)
				result = result + line + "\n";
		}
		sc.close();
		return result;		
	}
	
	public LinkedList<String> getIndex() throws FileNotFoundException {
		LinkedList<String> list = new LinkedList<String>();
		
		Scanner sc = new Scanner(this);
		String header = sc.nextLine();
		while(sc.hasNextLine()) {
			String line = sc.nextLine();
			if(line.charAt(0) == '>') {
				list.add(header);
				header = line;
			}
		}
		
		sc.close();
		return list;
	}
	
	public LinkedList<String> getAllLocusTag() throws FileNotFoundException {
		Scanner sc = new Scanner(this);
		LinkedList<String> list = new LinkedList<>();
		while(sc.hasNext()) {
			String header = sc.nextLine();
			if(header.length() > 0 && header.charAt(0) == '>') {
				if(header.indexOf(' ') > 0) {
					header = header.substring(1, header.indexOf(' '));
					list.add(header);
				}
			}
		}
		sc.close();
		return list;
		
	}
}