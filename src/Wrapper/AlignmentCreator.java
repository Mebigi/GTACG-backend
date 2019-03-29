package Wrapper;

import java.io.IOException;

import ToolkitFile.AlignmentFile;
import ToolkitFile.FastaFile;

public interface AlignmentCreator {
	public void makeAligment(FastaFile in, AlignmentFile out, Integer numThreads) throws IOException, InterruptedException;
}
