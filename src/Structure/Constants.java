package Structure;
import java.util.Hashtable;
import java.util.UUID;

public class Constants {
	public static String rand() {
		return ".z" + UUID.randomUUID().toString();
	}
	
	public static String translate(String seq, int lineSize) {
		while(seq.indexOf("\n") != -1)
			seq = seq.replace("\n", "");

		StringBuffer result = new StringBuffer("M");
		int count = 1;
		for (int i = 3; i < seq.length(); i+=3) {
			char letter = 'a';
			try {
				letter = nuclToAcid[nuclToInt[seq.charAt(i)]][nuclToInt[seq.charAt(i+1)]][nuclToInt[seq.charAt(i+2)]];
				
			} catch (Exception e) {
				System.out.println(i + "\t" + seq.charAt(i));
				System.out.println(i+1 + "\t" + seq.charAt(i+1));
				System.out.println(i+2 + "\t" + seq.charAt(i+2));
			}
			if(letter == '*' && i+3 == seq.length())
				return result.toString();
			result.append(letter);
			count++;
			if(count == lineSize) {
				count = 0;
				result.append('\n');
			}
		}
		return result.toString();
	}
	
	public static String translateComplement(String seq, int lineSize) {
		while(seq.indexOf("\n") != -1)
			seq = seq.replace("\n", "");

		StringBuffer result = new StringBuffer("M");
		int count = 1;
		for (int i = seq.length()-4; i >= 0; i-=3) {
			char letter = nuclToAcid[nuclToIntComp[seq.charAt(i)]][nuclToIntComp[seq.charAt(i-1)]][nuclToIntComp[seq.charAt(i-2)]];
			if(letter == '*' && i-3 == -1)
				return result.toString();
			result.append(letter);
			count++;
			if(count == lineSize) {
				count = 0;
				result.append('\n');
			}
		}
		return result.toString();
	}
	
	public static String formatSeq(String seq, int lineSize) {
		StringBuffer result = new StringBuffer();
		int count = 0;
		for (int i = 0; i < seq.length(); i++) {
			if(seq.charAt(i) != '\n') {
				result.append(seq.charAt(i));
				count++;
				if(count == lineSize) {
					count = 0;
					result.append('\n');
				}
			}
		}
		return result.toString();
	}

	public static String formatComplementSeq(String seq, int lineSize) {
		StringBuffer result = new StringBuffer();
		int count = 0;
		for (int i = seq.length()-1; i >= 0; i--) {
			if(seq.charAt(i) != '\n') {
				char ant = seq.charAt(i);
				char c = intToNucl[nuclToIntComp[seq.charAt(i)]];
				result.append(c);
				count++;
				if(count == lineSize) {
					count = 0;
					result.append('\n');
				}
			}
		}
		return result.toString();
	}
	
	public static Hashtable<String, String> nomes = new Hashtable<String, String>();
	public static Hashtable<String, String> nomesAbrev = new Hashtable<String, String>();
	public static int nuclToInt[] = new int[256];
	public static int nuclToIntComp[] = new int[256];
	public static char intToNucl[] = new char[4];
	public static char nuclToAcid[][][] = new char[4][4][4];
	public static int acidToInt[] = new int[256];
	public static void inicializacao() {
		nuclToInt['A'] = 0;
		nuclToInt['C'] = 1;
		nuclToInt['G'] = 2;
		nuclToInt['T'] = 3;
		nuclToInt['U'] = 3;
		
		intToNucl[0] = 'A';
		intToNucl[1] = 'C';
		intToNucl[2] = 'G';
		intToNucl[3] = 'T';
		
		nuclToIntComp['A'] = 3;
		nuclToIntComp['C'] = 2;
		nuclToIntComp['G'] = 1;
		nuclToIntComp['T'] = 0;
		nuclToIntComp['U'] = 0;
		
		acidToInt['-'] = -1;
		acidToInt['A'] = 0;
		acidToInt['B'] = 1;
		acidToInt['C'] = 2;
		acidToInt['D'] = 3;
		acidToInt['E'] = 4;
		acidToInt['F'] = 5;
		acidToInt['G'] = 6;
		acidToInt['H'] = 7;
		acidToInt['I'] = 8;
		acidToInt['J'] = 9;
		acidToInt['K'] = 10;
		acidToInt['L'] = 11;
		acidToInt['M'] = 12;
		acidToInt['N'] = 13;
		acidToInt['O'] = 14;
		acidToInt['P'] = 15;
		acidToInt['Q'] = 16;
		acidToInt['R'] = 17;
		acidToInt['S'] = 18;
		acidToInt['T'] = 19;
		acidToInt['U'] = 20;
		acidToInt['V'] = 21;
		acidToInt['W'] = 22;
		acidToInt['Y'] = 23;
		
		nuclToAcid[nuclToInt['T']][nuclToInt['T']][nuclToInt['T']] = 'F';
		nuclToAcid[nuclToInt['T']][nuclToInt['T']][nuclToInt['C']] = 'F';
		nuclToAcid[nuclToInt['T']][nuclToInt['T']][nuclToInt['A']] = 'L';
		nuclToAcid[nuclToInt['T']][nuclToInt['T']][nuclToInt['G']] = 'L';
		nuclToAcid[nuclToInt['T']][nuclToInt['C']][nuclToInt['T']] = 'S';
		nuclToAcid[nuclToInt['T']][nuclToInt['C']][nuclToInt['C']] = 'S';
		nuclToAcid[nuclToInt['T']][nuclToInt['C']][nuclToInt['A']] = 'S';
		nuclToAcid[nuclToInt['T']][nuclToInt['C']][nuclToInt['G']] = 'S';
		nuclToAcid[nuclToInt['T']][nuclToInt['A']][nuclToInt['T']] = 'Y';
		nuclToAcid[nuclToInt['T']][nuclToInt['A']][nuclToInt['C']] = 'Y';
		nuclToAcid[nuclToInt['T']][nuclToInt['A']][nuclToInt['A']] = '*';
		nuclToAcid[nuclToInt['T']][nuclToInt['A']][nuclToInt['G']] = '*';
		nuclToAcid[nuclToInt['T']][nuclToInt['G']][nuclToInt['T']] = 'C';
		nuclToAcid[nuclToInt['T']][nuclToInt['G']][nuclToInt['C']] = 'C';
		nuclToAcid[nuclToInt['T']][nuclToInt['G']][nuclToInt['A']] = '*';
		nuclToAcid[nuclToInt['T']][nuclToInt['G']][nuclToInt['G']] = 'W';
		
		nuclToAcid[nuclToInt['C']][nuclToInt['T']][nuclToInt['T']] = 'L';
		nuclToAcid[nuclToInt['C']][nuclToInt['T']][nuclToInt['C']] = 'L';
		nuclToAcid[nuclToInt['C']][nuclToInt['T']][nuclToInt['A']] = 'L';
		nuclToAcid[nuclToInt['C']][nuclToInt['T']][nuclToInt['G']] = 'L';
		nuclToAcid[nuclToInt['C']][nuclToInt['C']][nuclToInt['T']] = 'P';
		nuclToAcid[nuclToInt['C']][nuclToInt['C']][nuclToInt['C']] = 'P';
		nuclToAcid[nuclToInt['C']][nuclToInt['C']][nuclToInt['A']] = 'P';
		nuclToAcid[nuclToInt['C']][nuclToInt['C']][nuclToInt['G']] = 'P';
		nuclToAcid[nuclToInt['C']][nuclToInt['A']][nuclToInt['T']] = 'H';
		nuclToAcid[nuclToInt['C']][nuclToInt['A']][nuclToInt['C']] = 'H';
		nuclToAcid[nuclToInt['C']][nuclToInt['A']][nuclToInt['A']] = 'Q';
		nuclToAcid[nuclToInt['C']][nuclToInt['A']][nuclToInt['G']] = 'Q';
		nuclToAcid[nuclToInt['C']][nuclToInt['G']][nuclToInt['T']] = 'R';
		nuclToAcid[nuclToInt['C']][nuclToInt['G']][nuclToInt['C']] = 'R';
		nuclToAcid[nuclToInt['C']][nuclToInt['G']][nuclToInt['A']] = 'R';
		nuclToAcid[nuclToInt['C']][nuclToInt['G']][nuclToInt['G']] = 'R';
		
		nuclToAcid[nuclToInt['A']][nuclToInt['T']][nuclToInt['T']] = 'I';
		nuclToAcid[nuclToInt['A']][nuclToInt['T']][nuclToInt['C']] = 'I';
		nuclToAcid[nuclToInt['A']][nuclToInt['T']][nuclToInt['A']] = 'I';
		nuclToAcid[nuclToInt['A']][nuclToInt['T']][nuclToInt['G']] = 'M';
		nuclToAcid[nuclToInt['A']][nuclToInt['C']][nuclToInt['T']] = 'T';
		nuclToAcid[nuclToInt['A']][nuclToInt['C']][nuclToInt['C']] = 'T';
		nuclToAcid[nuclToInt['A']][nuclToInt['C']][nuclToInt['A']] = 'T';
		nuclToAcid[nuclToInt['A']][nuclToInt['C']][nuclToInt['G']] = 'T';
		nuclToAcid[nuclToInt['A']][nuclToInt['A']][nuclToInt['T']] = 'N';
		nuclToAcid[nuclToInt['A']][nuclToInt['A']][nuclToInt['C']] = 'N';
		nuclToAcid[nuclToInt['A']][nuclToInt['A']][nuclToInt['A']] = 'K';
		nuclToAcid[nuclToInt['A']][nuclToInt['A']][nuclToInt['G']] = 'K';
		nuclToAcid[nuclToInt['A']][nuclToInt['G']][nuclToInt['T']] = 'S';
		nuclToAcid[nuclToInt['A']][nuclToInt['G']][nuclToInt['C']] = 'S';
		nuclToAcid[nuclToInt['A']][nuclToInt['G']][nuclToInt['A']] = 'R';
		nuclToAcid[nuclToInt['A']][nuclToInt['G']][nuclToInt['G']] = 'R';
		
		nuclToAcid[nuclToInt['G']][nuclToInt['T']][nuclToInt['T']] = 'V';
		nuclToAcid[nuclToInt['G']][nuclToInt['T']][nuclToInt['C']] = 'V';
		nuclToAcid[nuclToInt['G']][nuclToInt['T']][nuclToInt['A']] = 'V';
		nuclToAcid[nuclToInt['G']][nuclToInt['T']][nuclToInt['G']] = 'V';
		nuclToAcid[nuclToInt['G']][nuclToInt['C']][nuclToInt['T']] = 'A';
		nuclToAcid[nuclToInt['G']][nuclToInt['C']][nuclToInt['C']] = 'A';
		nuclToAcid[nuclToInt['G']][nuclToInt['C']][nuclToInt['A']] = 'A';
		nuclToAcid[nuclToInt['G']][nuclToInt['C']][nuclToInt['G']] = 'A';
		nuclToAcid[nuclToInt['G']][nuclToInt['A']][nuclToInt['T']] = 'D';
		nuclToAcid[nuclToInt['G']][nuclToInt['A']][nuclToInt['C']] = 'D';
		nuclToAcid[nuclToInt['G']][nuclToInt['A']][nuclToInt['A']] = 'E';
		nuclToAcid[nuclToInt['G']][nuclToInt['A']][nuclToInt['G']] = 'E';
		nuclToAcid[nuclToInt['G']][nuclToInt['G']][nuclToInt['T']] = 'G';
		nuclToAcid[nuclToInt['G']][nuclToInt['G']][nuclToInt['C']] = 'G';
		nuclToAcid[nuclToInt['G']][nuclToInt['G']][nuclToInt['A']] = 'G';
		nuclToAcid[nuclToInt['G']][nuclToInt['G']][nuclToInt['G']] = 'G';
		
		nomes.put("NC_003902", "Xanthomonas campestris pv. campestris str. ATCC 33913 chromosome, complete genome");
		nomes.put("NC_003919", "Xanthomonas axonopodis pv. citri str. 306 chromosome, complete genome");
		nomes.put("NC_006834", "Xanthomonas oryzae pv. oryzae KACC 10331 chromosome, complete genome");
		nomes.put("NC_007086", "Xanthomonas campestris pv. campestris str. 8004 chromosome, complete genome");
		nomes.put("NC_007508", "Xanthomonas campestris pv. vesicatoria str. 85-10 chromosome, complete genome");
		nomes.put("NC_007705", "Xanthomonas oryzae pv. oryzae MAFF 311018 chromosome, complete genome");
		nomes.put("NC_010688", "Xanthomonas campestris pv. campestris str. B100 chromosome, complete genome");
		nomes.put("NC_010717", "Xanthomonas oryzae pv. oryzae PXO99A chromosome, complete genome");
		nomes.put("NC_013722", "Xanthomonas albilineans GPE PC73 chromosome, complete genome");
		nomes.put("NC_016010", "Xanthomonas axonopodis pv. citrumelo F1 chromosome, complete genome");
		nomes.put("NC_017267", "Xanthomonas oryzae pv. oryzicola BLS256 chromosome, complete genome");
		nomes.put("NC_017271", "Xanthomonas campestris pv. raphani 756C chromosome, complete genome");
		nomes.put("NC_020800", "Xanthomonas axonopodis Xac29-1, complete genome");
		nomes.put("NC_020815", "Xanthomonas citri subsp. citri Aw12879, complete genome");
		nomes.put("NC_022541", "Xanthomonas fuscans subsp. fuscans str. 4834-R, chromosome, complete genome");
		
		nomes.put("CP003093.2", "Pseudoxanthomonas spadix BD-a59");
		nomes.put("CP002446.1", "Pseudoxanthomonas suwonensis 11-1");
		nomes.put("CP011144.1", "Pseudoxanthomonas suwonensis str. J1");
		nomes.put("CP012900.1", "Stenotrophomonas acidaminiphila str. ZAC14D2_NAIMI4_2");
		nomes.put("HE798556.1", "Stenotrophomonas maltophilia D457");
		nomes.put("CP002986.1", "Stenotrophomonas maltophilia JV3");
		nomes.put("AM743169.1", "Stenotrophomonas maltophilia K279a");
		nomes.put("CP001111.1", "Stenotrophomonas maltophilia R551-3");
		nomes.put("CP011305.1", "Stenotrophomonas maltophilia str. ISMMS2");
		nomes.put("CP011306.1", "Stenotrophomonas maltophilia str. ISMMS2R");
		nomes.put("CP011010.1", "Stenotrophomonas maltophilia str. ISMMS3");
		nomes.put("FP340277.1", "Xanthomonas albilineans str. GPE PC73, plasmid");
		nomes.put("FP340278.1", "Xanthomonas albilineans str. GPE PC73, plasmid");
		nomes.put("FP340279.1", "Xanthomonas albilineans str. GPE PC73, plasmid");
		nomes.put("FP565176.1", "Xanthomonas albilineans GPE PC73");
		nomes.put("AE008923.1", "Xanthomonas axonopodis pv. citri str. 306");
		nomes.put("AE008924.1", "axonopodis pv. citri str. 306 plasmid pXAC33");
		nomes.put("AE008925.1", "Xanthomonas axonopodis pv. citri str. 306 plasmid pXAC64");
		nomes.put("CP002914.1", "Xanthomonas axonopodis pv. citrumelo F1");
		nomes.put("CP004399.1", "Xanthomonas axonopodis Xac29-1");
		nomes.put("CP004400.1", "Xanthomonas axonopodis Xac29-1 plasmid pXAC64");
		nomes.put("CP004401.1", "Xanthomonas axonopodis Xac29-1 plasmid pXAC47");
		nomes.put("CP004402.1", "Xanthomonas axonopodis Xac29-1 plasmid pXAC33");
		nomes.put("CP000050.1", "Xanthomonas campestris pv. campestris str. 8004");
		nomes.put("AM920689.1", "Xanthomonas campestris pv. campestris str. B100");
		nomes.put("CP012145.1", "Xanthomonas campestris pv. campestris str. ICMP 21080");
		nomes.put("CP012146.1", "Xanthomonas campestris pv. campestris str. ICMP 4013");
		nomes.put("AE008922.1", "Xanthomonas campestris pv. campestris str. ATCC 33913");
		nomes.put("CP002789.1", "Xanthomonas campestris pv. raphani 756C");
		nomes.put("AM039948.1", "Xanthomonas campestris pv. vesicatoria plasmid pXCV2");
		nomes.put("AM039949.1", "Xanthomonas campestris pv. vesicatoria plasmid pXCV19");
		nomes.put("AM039950.1", "Xanthomonas campestris pv. vesicatoria plasmid pXCV38");
		nomes.put("AM039951.1", "Xanthomonas campestris pv. vesicatoria plasmid pXCV183");
		nomes.put("AM039952.1", "Xanthomonas campestris pv. vesicatoria");
		nomes.put("CP011256.1", "Xanthomonas campestris str. 17");
		nomes.put("CP011827.1", "Xanthomonas citri pv. citri str. jx-6");
		nomes.put("CP013664.1", "Xanthomonas citri pv. citri str. jx-6 plasmid pXAC64");
		nomes.put("CP013665.1", "Xanthomonas citri pv. citri str. jx-6 plasmid pXAC33");
		nomes.put("CP006855.1", "Xanthomonas citri subsp. citri A306 plasmid pXAC33");
		nomes.put("CP006856.1", "Xanthomonas citri subsp. citri A306 plasmid pXAC64");
		nomes.put("CP006857.1", "Xanthomonas citri subsp. citri A306");
		nomes.put("CP003778.1", "Xanthomonas citri subsp. citri Aw12879");
		nomes.put("CP003779.1", "Xanthomonas citri subsp. citri Aw12879 plasmid pXcaw19");
		nomes.put("CP003780.1", "Xanthomonas citri subsp. citri Aw12879 plasmid pXcaw58");
		nomes.put("CP009026.1", "Xanthomonas citri subsp. citri str. 5208 plasmid pXAC33");
		nomes.put("CP009027.1", "Xanthomonas citri subsp. citri str. 5208 plasmid pXAC64");
		nomes.put("CP009028.1", "Xanthomonas citri subsp. citri str. 5208");
		nomes.put("CP009029.1", "Xanthomonas citri subsp. citri str. AW13 plasmid pXCAW19");
		nomes.put("CP009030.1", "Xanthomonas citri subsp. citri str. AW13 plasmid pXCAW58");
		nomes.put("CP009031.1", "Xanthomonas citri subsp. citri str. AW13");
		nomes.put("CP009032.1", "Xanthomonas citri subsp. citri str. AW14 plasmid pXCAW19");
		nomes.put("CP009033.1", "Xanthomonas citri subsp. citri str. AW14 plasmid pXCAW58");
		nomes.put("CP009034.1", "Xanthomonas citri subsp. citri str. AW14");
		nomes.put("CP009035.1", "Xanthomonas citri subsp. citri str. AW15 plasmid pXCAW19");
		nomes.put("CP009036.1", "Xanthomonas citri subsp. citri str. AW15 plasmid pXCAW58");
		nomes.put("CP009037.1", "Xanthomonas citri subsp. citri str. AW15");
		nomes.put("CP009038.1", "Xanthomonas citri subsp. citri str. AW16 plasmid pXCAW19");
		nomes.put("CP009039.1", "Xanthomonas citri subsp. citri str. AW16 plasmid pXCAW58");
		nomes.put("CP009040.1", "Xanthomonas citri subsp. citri str. AW16");
		nomes.put("CP009023.1", "Xanthomonas citri subsp. citri str. BL18 plasmid pXAC33");
		nomes.put("CP009024.1", "Xanthomonas citri subsp. citri str. BL18 plasmid pXAC64");
		nomes.put("CP009025.1", "Xanthomonas citri subsp. citri str. BL18");
		nomes.put("CP009020.1", "Xanthomonas citri subsp. citri str. FB19 plasmid pXAC33");
		nomes.put("CP009021.1", "Xanthomonas citri subsp. citri str. FB19 plasmid pXAC64");
		nomes.put("CP009022.1", "Xanthomonas citri subsp. citri str. FB19");
		nomes.put("CP009017.1", "Xanthomonas citri subsp. citri str. gd2 plasmid pXAC33");
		nomes.put("CP009018.1", "Xanthomonas citri subsp. citri str. gd2 plasmid pXAC64");
		nomes.put("CP009019.1", "Xanthomonas citri subsp. citri str. gd2");
		nomes.put("CP009014.1", "Xanthomonas citri subsp. citri str. gd3 plasmid pXAC33");
		nomes.put("CP009015.1", "Xanthomonas citri subsp. citri str. gd3 plasmid pXAC64");
		nomes.put("CP009016.1", "Xanthomonas citri subsp. citri str. gd3");
		nomes.put("CP009011.1", "Xanthomonas citri subsp. citri str. jx4 plasmid pXAC33");
		nomes.put("CP009012.1", "Xanthomonas citri subsp. citri str. jx4 plasmid pXAC64");
		nomes.put("CP009013.1", "Xanthomonas citri subsp. citri str. jx4");
		nomes.put("CP009008.1", "Xanthomonas citri subsp. citri str. jx5 plasmid pXAC33");
		nomes.put("CP009009.1", "Xanthomonas citri subsp. citri str. jx5 plasmid pXAC64");
		nomes.put("CP009010.1", "Xanthomonas citri subsp. citri str. jx5");
		nomes.put("CP009005.1", "Xanthomonas citri subsp. citri str. mf20 plasmid pXAC33");
		nomes.put("CP009006.1", "Xanthomonas citri subsp. citri str. mf20 plasmid pXAC64");
		nomes.put("CP009007.1", "Xanthomonas citri subsp. citri str. mf20");
		nomes.put("CP009002.1", "Xanthomonas citri subsp. citri str. MN10 plasmid pXAC33");
		nomes.put("CP009003.1", "Xanthomonas citri subsp. citri str. MN10 plasmid pXAC64");
		nomes.put("CP009004.1", "Xanthomonas citri subsp. citri str. MN10");
		nomes.put("CP008999.1", "Xanthomonas citri subsp. citri str. MN11 plasmid pXAC33");
		nomes.put("CP009000.1", "Xanthomonas citri subsp. citri str. MN11 plasmid pXAC64");
		nomes.put("CP009001.1", "Xanthomonas citri subsp. citri str. MN11");
		nomes.put("CP008996.1", "Xanthomonas citri subsp. citri str. MN12 plasmid pXAC33");
		nomes.put("CP008997.1", "Xanthomonas citri subsp. citri str. MN12 plasmid pXAC64");
		nomes.put("CP008998.1", "Xanthomonas citri subsp. citri str. MN12");
		nomes.put("CP008993.1", "Xanthomonas citri subsp. citri str. NT17 plasmid pXAC33");
		nomes.put("CP008994.1", "Xanthomonas citri subsp. citri str. NT17 plasmid pXAC64");
		nomes.put("CP008995.1", "Xanthomonas citri subsp. citri str. NT17");
		nomes.put("CP008987.1", "Xanthomonas citri subsp. citri str. UI7 plasmid pXAC33");
		nomes.put("CP008988.1", "Xanthomonas citri subsp. citri str. UI7 plasmid pXAC64");
		nomes.put("CP008989.1", "Xanthomonas citri subsp. citri str. UI7");
		nomes.put("CP008990.1", "Xanthomonas citri subsp. citri UI6 plasmid pXAC33");
		nomes.put("CP008991.1", "Xanthomonas citri subsp. citri UI6 plasmid pXAC64");
		nomes.put("CP008992.1", "Xanthomonas citri subsp. citri UI6");
		nomes.put("FO681494.1", "Xanthomonas fuscans subsp. fuscans str. 4834-R");
		nomes.put("FO681495.1", "Xanthomonas fuscans subsp. fuscans str. 4834-R, plasmid pla");
		nomes.put("FO681496.1", "Xanthomonas fuscans subsp. fuscans str. 4834-R, plasmid plb");
		nomes.put("FO681497.1", "Xanthomonas fuscans subsp. fuscans str. 4834-R, plasmid plc");
		nomes.put("AE013598.1", "Xanthomonas oryzae pv. oryzae KACC 10331");
		nomes.put("AP008229.1", "Xanthomonas oryzae pv. oryzae MAFF 311018");
		nomes.put("CP007166.1", "Xanthomonas oryzae pv. oryzae PXO86");
		nomes.put("CP000967.2", "Xanthomonas oryzae pv. oryzae PXO99A");
		nomes.put("CP003057.2", "Xanthomonas oryzae pv. oryzicola BLS256");
		nomes.put("CP011955.1", "Xanthomonas oryzae pv. oryzicola str. B8-12");
		nomes.put("CP011956.1", "Xanthomonas oryzae pv. oryzicola str. BLS279");
		nomes.put("CP011957.1", "Xanthomonas oryzae pv. oryzicola str. BXOR1");
		nomes.put("CP011962.1", "Xanthomonas oryzae pv. oryzicola str. CFBP2286");
		nomes.put("CP011963.1", "Xanthomonas oryzae pv. oryzicola str. CFBP2286 plasmid");
		nomes.put("CP011958.1", "Xanthomonas oryzae pv. oryzicola str. CFBP7331");
		nomes.put("CP011959.1", "Xanthomonas oryzae pv. oryzicola str. CFBP7341");
		nomes.put("CP007221.1", "Xanthomonas oryzae pv. oryzicola str. CFBP7342");
		nomes.put("CP011960.1", "Xanthomonas oryzae pv. oryzicola str. L8");
		nomes.put("CP011961.1", "Xanthomonas oryzae pv. oryzicola str. RS105");
		nomes.put("CP007810.1", "Xanthomonas oryzae pv. oryzicola str. YM15");
		nomes.put("CP010409.1", "Xanthomonas sacchari str. R1");
		nomes.put("CP010410.1", "Xanthomonas sacchari str. R1 plasmid");
		nomes.put("CP008714.1", "Xanthomonas translucens pv. undulosa str. Xtu 4699");
		nomes.put("AE003849.1", "Xylella fastidiosa 9a5c");
		nomes.put("AE003850.3", "Xylella fastidiosa 9a5c plasmid pXF1.3");
		nomes.put("AE003851.1", "Xylella fastidiosa 9a5c plasmid pXF51");
		nomes.put("CP000941.1", "Xylella fastidiosa M12");
		nomes.put("CP001011.1", "Xylella fastidiosa M23");
		nomes.put("CP001012.1", "Xylella fastidiosa M23 plasmid pXFAS01");
		nomes.put("CP006739.1", "Xylella fastidiosa MUL0034 plasmid unnamed2");
		nomes.put("CP006740.1", "Xylella fastidiosa MUL0034");
		nomes.put("CP002165.1", "Xylella fastidiosa subsp. fastidiosa GB514");
		nomes.put("CP002166.1", "Xylella fastidiosa subsp. fastidiosa GB514 plasmid");
		nomes.put("CP006696.1", "Xylella fastidiosa subsp. sandyi Ann-1");
		nomes.put("CP006697.1", "Xylella fastidiosa subsp. sandyi Ann-1 plasmid unnamed1");
		nomes.put("AE009442.1", "Xylella fastidiosa Temecula1");
		nomes.put("AE009443.1", "Xylella fastidiosa Temecula1 plasmid pXFPD1.3");
		
		nomes.put("CP007241.1", "Streptococcus pyogenes 1E1");
		nomes.put("CP008776.1", "Streptococcus pyogenes 5448");
		nomes.put("CP003901.1", "Streptococcus pyogenes A20");
		nomes.put("NC_017596.1", "Streptococcus pyogenes Alab49");
		nomes.put("CP007537.1", "Streptococcus pyogenes AP1");
		nomes.put("NZ_CP013672.1", "Streptococcus pyogenes AP53");
		nomes.put("NZ_CP008926.1", "Streptococcus pyogenes ATCC 19615");
		nomes.put("NZ_CP011415.1", "Streptococcus pyogenes D471");
		nomes.put("NZ_HG316453.1", "Streptococcus pyogenes H293");
		nomes.put("AFRY01000001.1", "Streptococcus pyogenes HKU16");
		nomes.put("CP009612.1", "Streptococcus pyogenes HKU360");
		nomes.put("CP012045.1", "Streptococcus pyogenes HKU488");
		nomes.put("CP006366.1", "Streptococcus pyogenes HSC5");
		nomes.put("AP017629.1", "Streptococcus pyogenes JMUB1235");
		nomes.put("CP011414.1", "Streptococcus pyogenes JRS4");
		nomes.put("AP012491.2", "Streptococcus pyogenes M1-476");
		nomes.put("CP008695.1", "Streptococcus pyogenes M23ND");
		nomes.put("CP011535.2", "Streptococcus pyogenes M28PF1");
		nomes.put("AP014596.1", "Streptococcus pyogenes M3-b");
		nomes.put("NC_009332.1", "Streptococcus pyogenes Manfredo");
		nomes.put("CP014139.1", "Streptococcus pyogenes MEW123");
		nomes.put("CP014138.1", "Streptococcus pyogenes MEW427");
		nomes.put("NC_008022.1", "Streptococcus pyogenes MGAS10270");
		nomes.put("CP000003.1", "Streptococcus pyogenes MGAS10394");
		nomes.put("NC_008024.1", "Streptococcus pyogenes MGAS10750");
		nomes.put("NZ_CP013838.1", "Streptococcus pyogenes MGAS11027");
		nomes.put("NC_017040.1", "Streptococcus pyogenes MGAS15252");
		nomes.put("CP003121.1", "Streptococcus pyogenes MGAS1882");
		nomes.put("NC_008023.1", "Streptococcus pyogenes MGAS2096");
		nomes.put("CP013839.1", "Streptococcus pyogenes MGAS23530");
		nomes.put("NZ_CP013840.1", "Streptococcus pyogenes MGAS27061");
		nomes.put("NC_004070.1", "Streptococcus pyogenes MGAS315");
		nomes.put("NC_007297.2", "Streptococcus pyogenes MGAS5005");
		nomes.put("NC_007296", "Streptococcus pyogenes MGAS6180");
		nomes.put("NC_003485.1", "Streptococcus pyogenes MGAS8232");
		nomes.put("CP000259.1", "Streptococcus pyogenes MGAS9429");
		nomes.put("AP014572.1", "Streptococcus pyogenes MTB313");
		nomes.put("AP014585.1", "Streptococcus pyogenes MTB314");
		nomes.put("LN831034.1", "Streptococcus pyogenes NCTC8198");
		nomes.put("CP010449.1", "Streptococcus pyogenes NGAS322");
		nomes.put("CP007562.1", "Streptococcus pyogenes NGAS327");
		nomes.put("CP007561.1", "Streptococcus pyogenes NGAS596");
		nomes.put("CP010450.1", "Streptococcus pyogenes NGAS638");
		nomes.put("CP007560.1", "Streptococcus pyogenes NGAS743");
		nomes.put("CP015238.1", "Streptococcus pyogenes NS53");
		nomes.put("CP000829.1", "Streptococcus pyogenes NZ131");
		nomes.put("AE004092.2", "Streptococcus pyogenes SF370");
		nomes.put("BA000034.2", "Streptococcus pyogenes SSI-1");
		nomes.put("CP011069.1", "Streptococcus pyogenes STAB09014");
		nomes.put("CP011068.1", "Streptococcus pyogenes STAB10015");
		nomes.put("NZ_CP007240.1", "Streptococcus pyogenes STAB1101");
		nomes.put("CP007023.1", "Streptococcus pyogenes STAB1102");
		nomes.put("CP014278.2", "Streptococcus pyogenes STAB13021");
		nomes.put("CP007024.1", "Streptococcus pyogenes STAB901");
		nomes.put("NZ_CP007041.1", "Streptococcus pyogenes STAB902");

		
		

		nomesAbrev.put("NC_003902", "XccATCC33913");
		nomesAbrev.put("NC_003919", "Xacitri");
		nomesAbrev.put("NC_006834", "XooKACC10331");
		nomesAbrev.put("NC_007086", "Xcc8004");
		nomesAbrev.put("NC_007508", "Xvesicatoria");
		nomesAbrev.put("NC_007705", "XooMAFF311018");
		nomesAbrev.put("NC_010688", "XccB100");
		nomesAbrev.put("NC_010717", "XooPXO99A");
		nomesAbrev.put("NC_013722", "Xalbilineans");
		nomesAbrev.put("NC_016010", "Xacitrumelo");
		nomesAbrev.put("NC_017267", "Xooryzicola");
		nomesAbrev.put("NC_017271", "Xraphani");
		nomesAbrev.put("NC_020800", "Xac29-1");
		nomesAbrev.put("NC_020815", "Xccitri");
		nomesAbrev.put("NC_022541", "Xff");
		
		nomesAbrev.put("CP003093.2", "PspadixBD-a59");
		nomesAbrev.put("CP002446.1", "Psuwonensis11-1");
		nomesAbrev.put("CP011144.1", "PsuwonensisJ1");
		nomesAbrev.put("CP012900.1", "SacidaminiphilaZAC14D2");
		nomesAbrev.put("HE798556.1", "SmaD457");
		nomesAbrev.put("CP002986.1", "SmaJV3");
		nomesAbrev.put("AM743169.1", "SmaK279a");
		nomesAbrev.put("CP001111.1", "SmaR551-3");
		nomesAbrev.put("CP011305.1", "SmaISMMS2");
		nomesAbrev.put("CP011306.1", "SmaISMMS2R");
		nomesAbrev.put("CP011010.1", "SmaISMMS3");
		nomesAbrev.put("FP340277.1", "Xalbilineansp");
		nomesAbrev.put("FP340278.1", "Xalbilineansp");
		nomesAbrev.put("FP340279.1", "Xalbilineansp");
		nomesAbrev.put("FP565176.1", "Xalbilineans");
		nomesAbrev.put("AE008923.1", "Xac306");
		nomesAbrev.put("AE008924.1", "Xac306ppXAC33");
		nomesAbrev.put("AE008925.1", "Xac306ppXAC64");
		nomesAbrev.put("CP002914.1", "XacitrumeloF1");
		nomesAbrev.put("CP004399.1", "Xac29-1");
		nomesAbrev.put("CP004400.1", "Xac29-1ppXAC64");
		nomesAbrev.put("CP004401.1", "Xac29-1ppXAC47");
		nomesAbrev.put("CP004402.1", "Xac29-1ppXAC33");
		nomesAbrev.put("CP000050.1", "Xcc8004");
		nomesAbrev.put("AM920689.1", "XccB100");
		nomesAbrev.put("CP012145.1", "XccICMP21080");
		nomesAbrev.put("CP012146.1", "XccICMP4013");
		nomesAbrev.put("AE008922.1", "XccATCC33913");
		nomesAbrev.put("CP002789.1", "Xraphani756C");
		nomesAbrev.put("AM039948.1", "XvesicatoriappXCV2");
		nomesAbrev.put("AM039949.1", "XvesicatoriappXCV19");
		nomesAbrev.put("AM039950.1", "XvesicatoriappXCV38");
		nomesAbrev.put("AM039951.1", "XvesicatoriappXCV183");
		nomesAbrev.put("AM039952.1", "Xvesicatoria");
		nomesAbrev.put("CP011256.1", "Xc17");
		nomesAbrev.put("CP011827.1", "XccJx-6");
		nomesAbrev.put("CP013664.1", "XccJx-6ppXAC64");
		nomesAbrev.put("CP013665.1", "XccJx-6ppXAC33");
		nomesAbrev.put("CP006855.1", "XccA306ppXAC33");
		nomesAbrev.put("CP006856.1", "XccA306ppXAC64");
		nomesAbrev.put("CP006857.1", "XccA306");
		nomesAbrev.put("CP003778.1", "XccAw12879");
		nomesAbrev.put("CP003779.1", "XccAw12879ppXcaw19");
		nomesAbrev.put("CP003780.1", "XccAw12879ppXcaw58");
		nomesAbrev.put("CP009026.1", "Xcc5208ppXAC33");
		nomesAbrev.put("CP009027.1", "Xcc5208ppXAC64");
		nomesAbrev.put("CP009028.1", "Xcc5208");
		nomesAbrev.put("CP009029.1", "XccAW13ppXCAW19");
		nomesAbrev.put("CP009030.1", "XccAW13ppXCAW58");
		nomesAbrev.put("CP009031.1", "XccAW13");
		nomesAbrev.put("CP009032.1", "XccAW14ppXCAW19");
		nomesAbrev.put("CP009033.1", "XccAW14ppXCAW58");
		nomesAbrev.put("CP009034.1", "XccAW14");
		nomesAbrev.put("CP009035.1", "XccAW15ppXCAW19");
		nomesAbrev.put("CP009036.1", "XccAW15ppXCAW58");
		nomesAbrev.put("CP009037.1", "XccAW15");
		nomesAbrev.put("CP009038.1", "XccAW16ppXCAW19");
		nomesAbrev.put("CP009039.1", "XccAW16ppXCAW58");
		nomesAbrev.put("CP009040.1", "XccAW16");
		nomesAbrev.put("CP009023.1", "XccBL18ppXAC33");
		nomesAbrev.put("CP009024.1", "XccBL18ppXAC64");
		nomesAbrev.put("CP009025.1", "XccBL18");
		nomesAbrev.put("CP009020.1", "XccFB19ppXAC33");
		nomesAbrev.put("CP009021.1", "XccFB19ppXAC64");
		nomesAbrev.put("CP009022.1", "XccFB19");
		nomesAbrev.put("CP009017.1", "Xccgd2ppXAC33");
		nomesAbrev.put("CP009018.1", "Xccgd2ppXAC64");
		nomesAbrev.put("CP009019.1", "Xccgd2");
		nomesAbrev.put("CP009014.1", "Xccgd3ppXAC33");
		nomesAbrev.put("CP009015.1", "Xccgd3ppXAC64");
		nomesAbrev.put("CP009016.1", "Xccgd3");
		nomesAbrev.put("CP009011.1", "Xccjx4ppXAC33");
		nomesAbrev.put("CP009012.1", "Xccjx4ppXAC64");
		nomesAbrev.put("CP009013.1", "Xccjx4");
		nomesAbrev.put("CP009008.1", "Xccjx5ppXAC33");
		nomesAbrev.put("CP009009.1", "Xccjx5ppXAC64");
		nomesAbrev.put("CP009010.1", "Xccjx5");
		nomesAbrev.put("CP009005.1", "Xccmf20ppXAC33");
		nomesAbrev.put("CP009006.1", "Xccmf20ppXAC64");
		nomesAbrev.put("CP009007.1", "Xccmf20");
		nomesAbrev.put("CP009002.1", "XccMN10ppXAC33");
		nomesAbrev.put("CP009003.1", "XccMN10ppXAC64");
		nomesAbrev.put("CP009004.1", "XccMN10");
		nomesAbrev.put("CP008999.1", "XccMN11ppXAC33");
		nomesAbrev.put("CP009000.1", "XccMN11ppXAC64");
		nomesAbrev.put("CP009001.1", "XccMN11");
		nomesAbrev.put("CP008996.1", "XccMN12ppXAC33");
		nomesAbrev.put("CP008997.1", "XccMN12ppXAC64");
		nomesAbrev.put("CP008998.1", "XccMN12");
		nomesAbrev.put("CP008993.1", "XccNT17ppXAC33");
		nomesAbrev.put("CP008994.1", "XccNT17ppXAC64");
		nomesAbrev.put("CP008995.1", "XccNT17");
		nomesAbrev.put("CP008987.1", "XccUI7ppXAC33");
		nomesAbrev.put("CP008988.1", "XccUI7ppXAC64");
		nomesAbrev.put("CP008989.1", "XccUI7");
		nomesAbrev.put("CP008990.1", "XccUI6ppXAC33");
		nomesAbrev.put("CP008991.1", "XccUI6ppXAC64");
		nomesAbrev.put("CP008992.1", "XccUI6");
		nomesAbrev.put("FO681494.1", "Xff4834-R");
		nomesAbrev.put("FO681495.1", "Xff4834-Rppla");
		nomesAbrev.put("FO681496.1", "Xff4834-Rpplb");
		nomesAbrev.put("FO681497.1", "Xff4834-Rpplc");
		nomesAbrev.put("AE013598.1", "XooKACC10331");
		nomesAbrev.put("AP008229.1", "XooMAFF311018");
		nomesAbrev.put("CP007166.1", "XooPXO86");
		nomesAbrev.put("CP000967.2", "XooPXO99A");
		nomesAbrev.put("CP003057.2", "XoryBLS256");
		nomesAbrev.put("CP011955.1", "XoryB8-12");
		nomesAbrev.put("CP011956.1", "XoryBLS279");
		nomesAbrev.put("CP011957.1", "XoryBXOR1");
		nomesAbrev.put("CP011962.1", "XoryC2286");
		nomesAbrev.put("CP011963.1", "XoryC2286p");
		nomesAbrev.put("CP011958.1", "XoryC7331");
		nomesAbrev.put("CP011959.1", "XoryC7341");
		nomesAbrev.put("CP007221.1", "XoryC7342");
		nomesAbrev.put("CP011960.1", "XoryL8");
		nomesAbrev.put("CP011961.1", "XoryRS105");
		nomesAbrev.put("CP007810.1", "XoryYM15");
		nomesAbrev.put("CP010409.1", "XsacchariR1");
		nomesAbrev.put("CP010410.1", "XsacchariR1p");
		nomesAbrev.put("CP008714.1", "Xtu4699");
		nomesAbrev.put("AE003849.1", "Xyf9a5c");
		nomesAbrev.put("AE003850.3", "Xyf9a5cppXF1.3");
		nomesAbrev.put("AE003851.1", "Xyf9a5cppXF51");
		nomesAbrev.put("CP000941.1", "XyfM12");
		nomesAbrev.put("CP001011.1", "XyfM23");
		nomesAbrev.put("CP001012.1", "XyfM23ppXFAS01");
		nomesAbrev.put("CP006739.1", "XyfMUL0034punnamed2");
		nomesAbrev.put("CP006740.1", "XyfMUL0034");
		nomesAbrev.put("CP002165.1", "XyffGB514");
		nomesAbrev.put("CP002166.1", "XyffGB514p");
		nomesAbrev.put("CP006696.1", "XyfSandyiAnn-1");
		nomesAbrev.put("CP006697.1", "XyfSandyiAnn-1punnamed1");
		nomesAbrev.put("AE009442.1", "XyfTemecula1");
		nomesAbrev.put("AE009443.1", "XyfTemecula1ppXFPD1.3");
		
		nomesAbrev.put("Streptococcus_pyogenes_1E1", "Sp1E1");
		nomesAbrev.put("Streptococcus_pyogenes_5448", "Sp5448");
		nomesAbrev.put("Streptococcus_pyogenes_A20", "SpA20");
		nomesAbrev.put("Streptococcus_pyogenes_Alab49", "SpAlab49");
		nomesAbrev.put("Streptococcus_pyogenes_AP1", "SpAP1");
		nomesAbrev.put("Streptococcus_pyogenes_AP53", "SpAP53");
		nomesAbrev.put("Streptococcus_pyogenes_ATCC_19615", "SpATCC19615");
		nomesAbrev.put("Streptococcus_pyogenes_D471", "SpD471");
		nomesAbrev.put("Streptococcus_pyogenes_H293", "SpH293");
		nomesAbrev.put("Streptococcus_pyogenes_HKU16", "SpHKU16");
		nomesAbrev.put("Streptococcus_pyogenes_HKU360", "SpHKU360");
		nomesAbrev.put("Streptococcus_pyogenes_HKU488", "SpHKU488");
		nomesAbrev.put("Streptococcus_pyogenes_HSC5", "SpHSC5");
		nomesAbrev.put("Streptococcus_pyogenes_JMUB1235", "SpJMUB1235");
		nomesAbrev.put("Streptococcus_pyogenes_JRS4", "SpJRS4");
		nomesAbrev.put("Streptococcus_pyogenes_M1-476", "SpM1-476");
		nomesAbrev.put("Streptococcus_pyogenes_M23ND", "SpM23ND");
		nomesAbrev.put("Streptococcus_pyogenes_M28PF1", "SpM28PF1");
		nomesAbrev.put("Streptococcus_pyogenes_M3-b", "SpM3-b");
		nomesAbrev.put("Streptococcus_pyogenes_Manfredo", "SpManfredo");
		nomesAbrev.put("Streptococcus_pyogenes_MEW123", "SpMEW123");
		nomesAbrev.put("Streptococcus_pyogenes_MEW427", "SpMEW427");
		nomesAbrev.put("Streptococcus_pyogenes_MGAS10270", "SpMGAS10270");
		nomesAbrev.put("Streptococcus_pyogenes_MGAS10394", "SpMGAS10394");
		nomesAbrev.put("Streptococcus_pyogenes_MGAS10750", "SpMGAS10750");
		nomesAbrev.put("Streptococcus_pyogenes_MGAS11027", "SpMGAS11027");
		nomesAbrev.put("Streptococcus_pyogenes_MGAS15252", "SpMGAS15252");
		nomesAbrev.put("Streptococcus_pyogenes_MGAS1882", "SpMGAS1882");
		nomesAbrev.put("Streptococcus_pyogenes_MGAS2096", "SpMGAS2096");
		nomesAbrev.put("Streptococcus_pyogenes_MGAS23530", "SpMGAS23530");
		nomesAbrev.put("Streptococcus_pyogenes_MGAS27061", "SpMGAS27061");
		nomesAbrev.put("Streptococcus_pyogenes_MGAS315", "SpMGAS315");
		nomesAbrev.put("Streptococcus_pyogenes_MGAS5005", "SpMGAS5005");
		nomesAbrev.put("Streptococcus_pyogenes_MGAS6180", "SpMGAS6180");
		nomesAbrev.put("Streptococcus_pyogenes_MGAS8232", "SpMGAS8232");
		nomesAbrev.put("Streptococcus_pyogenes_MGAS9429", "SpMGAS9429");
		nomesAbrev.put("Streptococcus_pyogenes_MTB313", "SpMTB313");
		nomesAbrev.put("Streptococcus_pyogenes_MTB314", "SpMTB314");
		nomesAbrev.put("Streptococcus_pyogenes_NCTC8198", "SpNCTC8198");
		nomesAbrev.put("Streptococcus_pyogenes_NGAS322", "SpNGAS322");
		nomesAbrev.put("Streptococcus_pyogenes_NGAS327", "SpNGAS327");
		nomesAbrev.put("Streptococcus_pyogenes_NGAS596", "SpNGAS596");
		nomesAbrev.put("Streptococcus_pyogenes_NGAS638", "SpNGAS638");
		nomesAbrev.put("Streptococcus_pyogenes_NGAS743", "SpNGAS743");
		nomesAbrev.put("Streptococcus_pyogenes_NS53", "SpNS53");
		nomesAbrev.put("Streptococcus_pyogenes_NZ131", "SpNZ131");
		nomesAbrev.put("Streptococcus_pyogenes_SF370", "SpSF370");
		nomesAbrev.put("Streptococcus_pyogenes_SSI-1", "SpSSI-1");
		nomesAbrev.put("Streptococcus_pyogenes_STAB09014", "SpSTAB09014");
		nomesAbrev.put("Streptococcus_pyogenes_STAB10015", "SpSTAB10015");
		nomesAbrev.put("Streptococcus_pyogenes_STAB1101", "SpSTAB1101");
		nomesAbrev.put("Streptococcus_pyogenes_STAB1102", "SpSTAB1102");
		nomesAbrev.put("Streptococcus_pyogenes_STAB13021", "SpSTAB13021");
		nomesAbrev.put("Streptococcus_pyogenes_STAB901", "SpSTAB901");
		nomesAbrev.put("Streptococcus_pyogenes_STAB902", "SpSTAB902");
		
		
		

	}
}
