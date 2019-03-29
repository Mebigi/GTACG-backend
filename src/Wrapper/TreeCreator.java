package Wrapper;

import java.io.IOException;

import ToolkitFile.AlignmentFile;
import ToolkitFile.TreeFile;

public interface TreeCreator {
	public void makeTree(AlignmentFile in, TreeFile out, String args) throws IOException, InterruptedException;
}
