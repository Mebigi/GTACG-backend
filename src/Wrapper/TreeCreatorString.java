package Wrapper;

import java.io.IOException;

import ToolkitFile.AlignmentFile;

public interface TreeCreatorString {
	public String makeTree(AlignmentFile in) throws IOException, InterruptedException;
}
