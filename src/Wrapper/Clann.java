package Wrapper;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import Structure.Constants;
import ToolkitFile.TreeFile;

public class Clann {
	static int id = 0;
	public enum Criterion {
		dfit, sfit, qfit, mrp, avcon;
	}
	
	public enum Start {
		nj, random;
	}
	
	public enum Swap {
		nni, spr, tbr;
	}
	
	public enum Weight {
		equal, comparisons, taxa, quartets;
	}
	
	public static class Hs {
		private int sample = 100;
		private int nreps = 10;
		private Swap swap = null;
		private int nsteps = 1;
		private Start start = null;
		private int maxswaps = 1000;
		private File savetrees = null;
		private Weight weight = null;
		private Integer nbins = null;
		
		@Override
		public String toString() {
			return "hs sample=" + sample + 
					" nreps=" + nreps +
					(swap!=null?" swap=" + swap:"") + 
					" nsteps=" + nsteps +
					(start!=null?" start=" + start:"") +
					" maxswaps=" + maxswaps +
					" savetrees=" + savetrees.getName() + 
					(weight!=null?" weight=" + weight:"") + 
					(nbins!=null?" nbins=" + nbins:"") + "\\n";
		}
		
		public Hs() {
		}
		
		public Hs(int sample, int nreps) {
			this.sample = sample;
			this.nreps = nreps;
		}
		
		public Hs(int sample, int nreps, int maxswaps, Integer nbins) {
			this.sample = sample;
			this.nreps = nreps;
			this.maxswaps = maxswaps;
			this.nbins = nbins;
		}
		
		public Hs(int sample, int nreps, int maxswaps, Integer nbins, Swap swap, Start start) {
			this.sample = sample;
			this.nreps = nreps;
			this.maxswaps = maxswaps;
			this.nbins = nbins;
			this.swap = swap;
			this.start = start;
		}
	}
	
	public static void gerarArvore(TreeFile in, TreeFile out, Criterion crit, Integer seed, Hs hs, String args) throws IOException, InterruptedException {
		String command = "printf 'set criterion=" + crit + "\\n";
		if(seed != null)
			command = "set seed=" + seed + "\\n";
		hs.savetrees = new File((id++) + ".st");
		command = command + hs + "quit\\n' | clann " + in.getAbsolutePath();
		
		System.out.println(command);

		Process process = new ProcessBuilder(new String[] {"bash", "-c", command}).redirectErrorStream(true).start();
		while(process.isAlive()) {
			if(process.getInputStream().available() > 0) {
				byte read [] = new byte[process.getInputStream().available()];
				process.getInputStream().read(read);
				System.out.print(new String(read));
			}
			Thread.currentThread().sleep(1000);
		}
		process.waitFor();
		byte read [] = new byte[process.getInputStream().available()];
		process.getInputStream().read(read);
		System.out.print(new String(read));
		hs.savetrees.renameTo(out.getAbsoluteFile());
		File ps = new File("supertree.ps");
		if(ps.exists())
			ps.delete();
	}
}
