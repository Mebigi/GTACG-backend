package Genoma;

public class GenomasIncompletos {
	public static double numIsland(double N, double c, double sigma) {
		return N*Math.exp(-c*sigma);
	}
	
	public static double numIslandJ(double N, double c, double sigma, int j) {
		return N*Math.exp(-2*c*sigma)*Math.pow(1 - Math.exp(-c*sigma), j-1);
	}
	
	public static double numIsland2(double N, double c, double sigma) {
		return N*Math.exp(-c*sigma) - N*Math.exp(-2*c*sigma);
	}
	
	public static double numCloneInAIsland(double c, double sigma) {
		return Math.exp(c*sigma);
	}
	
	public static double islandLength(double L, double c, double sigma) {
		return L*(((Math.exp(c*sigma)-1)/c) + 1-sigma);
	}
	
	public static void main(String args[]) {
		double G = 5000000;
		double L = 2000;
		double N = 6000;
		//double alpha = (double)N/G;
		double T = 100;
		double teta = (double)T/L;
		double sigma = 1 - teta;
		double c = (double)L*N/G;
		System.out.println(numIsland(N, c, sigma));
		System.out.println(numIslandJ(N, c, sigma, 2));
		System.out.println(numIsland2(N, c, sigma));
		System.out.println(numCloneInAIsland(c, sigma));
		System.out.println(islandLength(L, c, sigma));
		
		double x = numIsland(N, c, sigma)*islandLength(L, c, sigma);
		System.out.println("### " + x);
		System.out.println("### " + x/G);

	}
//	G = haploid genome length in bp;
//	L = length of clone insert in bp;
//	N = number of clones fingerprint;
//	a = N/G = probabiliti per base of starting a new clone;
//	T = amount of overlap in base pairs needed to detect overlap;
//	O = T/L;
//	o = 1 - O;
//	c = redundanci of coverage = LN/G
//
//	1.i	The expected number of apparent islands
//		Ne^{c o}
//	1.ii	The expected number of apparent islands consisting of j clones
//		Ne^{2c o}(1 -e^{c o})^j-1, for j>=1
//	1.ii'	The expected number of apparent islands consisting of at least two clones
//		Ne{-c o} - Ne{-2 c o}
//	1.iii	The expected number clones in a apparent island
//		e^{c o}
//	1.iv	The expected length in base pairs of an apparent island
//		L[((e^{c o}-1)/c) + (1 - o)]
//	1.v
//	1.vi
//
//	2.
}