import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import javax.annotation.Generated;

import Structure.Constants;
import Structure.Registry.Dictionary;
import Structure.Registry.DictionaryGbf;
import Structure.Registry.OrganismRegistry;
import Structure.Registry.DictionaryGbf.Format;
import Structure.Registry.GeneRegistry;
import ToolkitFile.TreeFile;

public class ExportAllSequences {

	public static void main(String[] args) throws IOException {
		File dicFile = null;
		String dicFormat = null;
		File outFile = null;
		boolean aminoacid = true;
		
		/*args = new String[] {
			"-dic", "/media/caio/08CE4A891258E142/dados/xantho161/xantho161.dic2",
			"-dicFormat", "gff",
			"-outFile", "/media/caio/08CE4A891258E142/dados/xantho161/todos.faa"
		};*/

		for (int i = 0; i < args.length; i++) {
			if("-help".equals(args[i])) {
				System.out.println("Input Files");
				System.out.println("\t-dic");
				System.out.println("\t-dicFormat");
				System.out.println("\t-out");
				return;
			}
			else if("-dic".equals(args[i])) {
				if(args.length > i + 1)
				dicFile = new File(args[i+1]);
				if(!dicFile.exists()) {
					System.out.println("Dictionary file not found");
					return;
				}
			}
			else if("-dicFormat".equals(args[i])) {
				if(args.length > i + 1)
					dicFormat = args[i+1];
			}
			else if("-outFile".equals(args[i])) {
				if(args.length > i + 1)
					outFile = new File(args[i+1]);
			}
			else if("-nucl".equals(args[i])) {
				aminoacid = false;
				i--;
			}
			i++;
		}

		System.setOut(new PrintStream("out"));
		System.setErr(new PrintStream("err"));
		Constants.inicializacao();
		Dictionary dic = Dictionary.getDictionary(dicFile, dicFormat);
		OrganismRegistry[] orgs = dic.getOrganisms();
		
		PrintStream stream = new PrintStream(outFile);
		for (OrganismRegistry org : orgs) {
			try {
				for (GeneRegistry gene : org.getAllGenes()) {
					if(aminoacid)
						stream.println(gene.getAminoacids());
					else
						stream.println(gene.getNucleotides());
				}
				for (OrganismRegistry plas : org.plasmideos) {
					try {
						for (GeneRegistry gene : plas.getAllGenes()) {
							if(aminoacid)
								stream.println(gene.getAminoacids());
							else
								stream.println(gene.getNucleotides());
						}
					} catch (Exception e) {
						System.err.println(plas.getFile());
					}
				}
				
			} catch (Exception e) {
				System.err.println(org.getFile());
			}
		}
		stream.close();
		System.err.println("ok");


	}

}
