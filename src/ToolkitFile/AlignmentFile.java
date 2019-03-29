package ToolkitFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

import Wrapper.FigTree;

public class AlignmentFile extends ToolkitBaseFile {
	private static final long serialVersionUID = 1L;
	public String tree;
	
	public AlignmentFile(String pathname, FileType fileType) {
		super(pathname);
		this.fileType = fileType;
	}
	
	public AlignmentFile(String pathname, FileType fileType, SequenceType seqType) {
		super(pathname);
		this.fileType = fileType;
		this.seqType = seqType;
	}
	
	public AlignmentFile clone(String pathname) {
		return new AlignmentFile(pathname, fileType, seqType);
	}
	
	public AlignmentFile(FileType fileType, String tree) {
		super(null);
		this.tree = tree;
		this.fileType = fileType;
	}
	
	public AlignmentFile(FileType fileType, SequenceType seqType, String tree) {
		this(tree, fileType);
		this.seqType = seqType;
	}
	
	public boolean loadFile() throws FileNotFoundException {
		if(!exists())
			return false;
		Scanner sc = new Scanner(this);
		
		tree = "";
		while(sc.hasNextLine()) {
			tree = tree + sc.nextLine();
		}
		
		sc.close();
		return true;
	}
	
	public boolean saveFile(File f) throws FileNotFoundException {
		if(f == null) 
			return false;
		PrintStream out = new PrintStream(f);
		out.print(tree);
		out.close();
		return true;
	}
	
	public boolean saveFile() throws FileNotFoundException {
		return saveFile(this);
	}
	
	public void printFromFile(boolean showFile, File out) throws IOException, InterruptedException {
		FigTree.printTree(this, showFile, out);
	}
	
	public void printFromString(boolean showFile, File out) throws IOException, InterruptedException {
		FigTree.printTree(tree, showFile, out);
	}	
}