package Wrapper;

import java.io.IOException;

import ToolkitFile.AlignmentFile;
import ToolkitFile.ToolkitBaseFile;
import ToolkitFile.FastaFile;

public class ClustalO {
	public enum SequenceType {
		Protein, RNA, DNA;
	}
	
	private static String translateSequenceType(ToolkitBaseFile.SequenceType type) {
		if(type == ToolkitBaseFile.SequenceType.Nucleotides)
			 return "DNA";
		else if(type == ToolkitBaseFile.SequenceType.AminoAcids)
			return "Protein";
		else if(type == ToolkitBaseFile.SequenceType.Generic)
			return "auto";
		return "auto";
	}
	
	
	private static String translateFileType(ToolkitBaseFile.FileType type) {
		if(type == ToolkitBaseFile.FileType.fasta)
			 return "fa";
		else if (type == ToolkitBaseFile.FileType.clustal)
			return "clu";
		else if (type == ToolkitBaseFile.FileType.msf)
			return "msf";
		else if (type == ToolkitBaseFile.FileType.phylip)
			return "phy";
		else if (type == ToolkitBaseFile.FileType.selex)
			return "selex";
		else if (type == ToolkitBaseFile.FileType.stockholm)
			return "st";
		else if (type == ToolkitBaseFile.FileType.vienna)
			return "vie";
		return "auto";
	}
	
	public static void makeAlignment(FastaFile in, AlignmentFile out, Integer numThreads) throws IOException, InterruptedException {
		if(numThreads != null)
			makeAlignment(in, out, " --threads=" + numThreads + " ");
		else
			makeAlignment(in, out, " ");
	}
	
	public static void makeAlignment(FastaFile in, AlignmentFile out, String args) throws IOException, InterruptedException {
		if(in.exists()) {
			/*System.out.println("clustalo" + 
					" -i " + in.getAbsolutePath() +
					" -t " + translateSequenceType(in.seqType) +
					" -o " + out.getAbsolutePath() + 
					" --infmt " + translateFileType(in.fileType) +  
					" --outfmt " + translateFileType(out.fileType) +
					" " + args);*/
			Runtime rt = Runtime.getRuntime();
			Process p = rt.exec("clustalo" + 
					" -i " + in.getAbsolutePath() +
					" -t " + translateSequenceType(in.seqType) +
					" -o " + out.getAbsolutePath() + 
					" --infmt " + translateFileType(in.fileType) +  
					" --outfmt " + translateFileType(out.fileType) +
					" " + args);
			p.waitFor();
			
			out.seqType = in.seqType;
		}
	}
}