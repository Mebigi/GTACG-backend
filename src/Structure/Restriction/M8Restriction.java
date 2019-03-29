package Structure.Restriction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Scanner;

public class M8Restriction implements Restriction<M8Attribute> {
	float minPercLengthAlin;
	float minPercIdentity;
	short minLengthAlin;
	float maxPercMistmatches;
	short maxGapOpenings;
	double maxEValue;
	float minBitScore;
	
	public M8Restriction(float minPercLengthAlin, float minPercIdentity, int minLengthAlin, float maxPercMistmatches, short maxGapOpenings, double maxEValue, float minBitScore) {
		this.minPercLengthAlin = minPercLengthAlin; 
		this.minPercIdentity = minPercIdentity; 
		this.minLengthAlin = (short)minLengthAlin;
		this.maxPercMistmatches = maxPercMistmatches; 
		this.maxGapOpenings = (short)maxGapOpenings;
		this.maxEValue	 = maxEValue;
		this.minBitScore = minBitScore;
	}
	
	public boolean check(M8Attribute atr) {
		if((atr.getPercLengthAlign() >= minPercLengthAlin) && 
		(atr.getPercIdentity() >= minPercIdentity) && 
		(atr.getLengthAlign() >= minLengthAlin) &&
		(atr.getPercMistmatches() <= maxPercMistmatches) && 
		(atr.getGapOpenings() <= maxGapOpenings) && 
		(atr.getEValue() <= maxEValue) &&
		(atr.getBitScore() >= minBitScore))
			return true;
		return false;
	}
	
	public boolean check(Collection<M8Attribute> atr) {
		if(atr == null)
			return false;
		for (M8Attribute linha : atr) {
			if(check(linha))
				return true;
		}
		return false;
	}
	
	public boolean check(M8Attribute[] atr) {
		if(atr == null)
			return false;
		for (M8Attribute linha : atr) {
			if(check(linha)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int sumChecks(M8Attribute[] atr) {
		int total = 0;
		if(atr == null)
			return total;
		for (M8Attribute linha : atr) {
			if(check(linha)) {
				total++;
			}
		}
		return total;
	}

	public static double[] makeRangeDouble(double start, double end, double step) {
		LinkedList<Double> list = new LinkedList<>();
		for (double i = start; i <= end; i+=step) {
			list.add(i);
		}
		double[] result = new double[list.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = list.get(i);
		}
		return result;
	}
	
	public static float[] makeRangeFloat(float ini, float end, float step) {
		LinkedList<Float> lista = new LinkedList<>();
		for (float i = ini; i <= end; i+=step) {
			lista.add(i);
		}
		float[] result = new float[lista.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = lista.get(i);
		}
		return result;
	}
	
	public static LinkedList<M8Restriction> makeList(float minPercLengthAlin[], float minPercIdentity[], int minLengthAlin[], float maxPercMistmatches[], short maxGapOpenings[], double maxEValue[], float minBitScore[]) {
		LinkedList<M8Restriction> lista = new LinkedList<>();
			for (int i1 = 0; i1 < minPercLengthAlin.length; i1++) 
				for (int i2 = 0; i2 < minPercIdentity.length; i2++)
					for (int i3 = 0; i3 < minLengthAlin.length; i3++)
						for (int i4 = 0; i4 < maxPercMistmatches.length; i4++)
							for (short i5 = 0; i5 < maxGapOpenings.length; i5++)
								for (int i6 = 0; i6 < maxEValue.length; i6++)
									for (int i7 = 0; i7 < minBitScore.length; i7++)
										lista.add(new M8Restriction(minPercLengthAlin[i1], 
												minPercIdentity[i2], 
												minLengthAlin[i3], 
												maxPercMistmatches[i4], 
												maxGapOpenings[i5], 
												maxEValue[i6], 
												minBitScore[i7]));
		return lista;
	}
	
	public static LinkedList<M8Restriction> loadFile(File in) throws FileNotFoundException {
		Scanner sc = new Scanner(in);
		LinkedList<M8Restriction> rests = new LinkedList<>();
		while(sc.hasNext()) {
			String linha[] = sc.nextLine().split("\t");
			float minPercLengthAlin = Float.parseFloat(linha[0]);
			float minPercIdentity = Float.parseFloat(linha[1]);
			short minLengthAlin = Short.parseShort(linha[2]);
			float maxPercMistmatches = Float.parseFloat(linha[3]);
			short maxGapOpenings = Short.parseShort(linha[4]);
			double maxEValue = Double.parseDouble(linha[5]);
			float minBitScore = Float.parseFloat(linha[6]);
			
			rests.add(new M8Restriction(minPercLengthAlin, minPercIdentity, minLengthAlin, maxPercMistmatches, maxGapOpenings, maxEValue, minBitScore));
		}
		sc.close();
		return rests;
	}
	
	public static void saveFile(File out, LinkedList<M8Restriction> rests) throws FileNotFoundException {
		PrintStream stream = new PrintStream(out);
		for (M8Restriction rest : rests) {
			stream.println(rest.toString());
		}
		stream.close();
	}
	
	public String toString() {
		return minPercLengthAlin + "\t" +
				minPercIdentity + "\t" +
				minLengthAlin + "\t" +
				maxPercMistmatches + "\t" +
				maxGapOpenings + "\t" +
				maxEValue + "\t" +
				minBitScore;
	}
	
	public M8Restriction clone() {
		return new M8Restriction(minPercLengthAlin, minPercIdentity, minLengthAlin, maxPercMistmatches, maxGapOpenings, maxEValue, minBitScore);
	}
	
	public float getMinPercLengthAlin() {
		return minPercLengthAlin;
	}
	
	public float getMinPercIdentity() { 
		return minPercIdentity;
	}
	
	public short getMinLengthAlin() {
		return minLengthAlin;
	}
	
	public float getMaxPercMistmatches() {
		return maxPercMistmatches;
	}
	
	public short getMaxGapOpenings() {
		return maxGapOpenings;
	}
	
	public double getMaxEValue() {
		return maxEValue;
	}
	
	public float getMinBitScore() {
		return minBitScore;
	}
}