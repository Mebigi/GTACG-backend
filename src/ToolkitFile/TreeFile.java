package ToolkitFile;

import java.io.File;
import java.io.IOException;

import Wrapper.FigTree;

public class TreeFile extends ToolkitBaseFile {
	private static final long serialVersionUID = 1L;

	public TreeFile(String s) {
		super(s);
		this.seqType = ToolkitBaseFile.SequenceType.Generic;
		this.fileType = ToolkitBaseFile.FileType.newick;
	}
	
	public void print(boolean openFile, File saidaPDF) throws IOException, InterruptedException {
		FigTree.printTree(this, openFile, saidaPDF);
	}
}
