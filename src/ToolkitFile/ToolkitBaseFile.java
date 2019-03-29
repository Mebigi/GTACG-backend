package ToolkitFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Scanner;

public class ToolkitBaseFile extends File {
	public ToolkitBaseFile(String pathname) {
		super(pathname);
	}

	public static enum SequenceType {
		Nucleotides, AminoAcids, Generic;
	}
	
	public static enum FileType {
		fasta, clustal, msf, phylip, selex, stockholm, vienna, newick, nexus;
	}

	private static final long serialVersionUID = 1L;
	public SequenceType seqType;
	public FileType fileType;
	
	public void append(File newFile) throws IOException {
		if(exists())
			Files.write(Paths.get(getAbsolutePath()), Files.readAllBytes(Paths.get(newFile.getAbsolutePath())), StandardOpenOption.APPEND);
		else {
			PrintStream stream = new PrintStream(this);
			Scanner sc = new Scanner(newFile);
			while(sc.hasNextLine())
				stream.println(sc.nextLine());
			sc.close();
			stream.close();
		}
			
	}
	
	public void append(ToolkitBaseFile newFile) throws IOException {
		if(newFile.exists())
			append(newFile);
	}
	
	public void replace(String a, String b) throws IOException {
		Path path = Paths.get(getPath());
		Charset charset = StandardCharsets.UTF_8;

		/*InputStream input = new FileInputStream(this);
		int size = input.available();
		byte buffer [] = new byte[size];
		input.read(buffer);
		String content = new String(buffer);*/
		String content = new String(Files.readAllBytes(path), charset);
		content = content.replaceAll(a, b);
		Files.write(path, content.getBytes(charset));

	}
	
	public void replace(Map<String, String> map) throws IOException {
		Path path = Paths.get(getPath());
		Charset charset = StandardCharsets.UTF_8;

		String content = new String(Files.readAllBytes(path), charset);
		for (String string : map.keySet()) {
			content = content.replaceAll(string.replace("|", "\\|"), map.get(string));
		}
		Files.write(path, content.getBytes(charset));
	}
	
	public String load() throws IOException {
		Path path = Paths.get(getAbsolutePath());
		return new String(Files.readAllBytes(path));
	}
	
	public void join(File f) throws IOException {
		join(f, true);
	}
	
	public void join(File f, boolean lineSeparator) throws IOException {
		FileOutputStream output = new FileOutputStream(this, true);
		output.write(new byte['\n']);
		
        byte[] buf = new byte[8192];
        int len;
        InputStream input = new FileInputStream(f);
        while ((len = input.read(buf)) > 0){
            output.write(buf, 0, len);
        }
        input.close();
        output.close();
	}	
}