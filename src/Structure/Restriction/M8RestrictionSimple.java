package Structure.Restriction;

import java.util.Collection;
import java.util.LinkedList;

public class M8RestrictionSimple implements Restriction<M8Attribute> {
	float minPercLengthAlin;
	float minPercIdentity;
	int minLengthAlin;
	float maxPercMistmatches;
	int maxGapOpenings;
	double maxEValue;
	float minBitScore;
	
	public M8RestrictionSimple(float minPercLengthAlin, float minPercIdentity, int minLengthAlin, float maxPercMistmatches, int maxGapOpenings, double maxEValue, float minBitScore) {
		this.minPercLengthAlin = minPercLengthAlin; 
		this.minPercIdentity = minPercIdentity; 
		this.minLengthAlin = minLengthAlin;
		this.maxPercMistmatches = maxPercMistmatches; 
		this.maxGapOpenings = maxGapOpenings;
		this.maxEValue	 = maxEValue;
		this.minBitScore = minBitScore;
	}
	
	public boolean check(M8Attribute atr) {
		if((atr.getPercLengthAlign() >= minPercLengthAlin) && 
		(atr.getPercIdentity() >= minPercIdentity) && 
		(atr.getPercMistmatches() <= maxPercMistmatches))
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
	
	public static float[] makeRangeFloat(float start, float end, float step) {
		LinkedList<Float> list = new LinkedList<>();
		for (float i = start; i <= end; i+=step) {
			list.add(i);
		}
		float[] result = new float[list.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = list.get(i);
		}
		return result;
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

	
	public static LinkedList<M8RestrictionSimple> makeListSimple(float minPercLengthAlin[], float minPercIdentity[], int minLengthAlin[], float maxPercMistmatches[], int maxGapOpenings[], double maxEValue[], float minBitScore[]) {
		LinkedList<M8RestrictionSimple> lista = new LinkedList<>();
			for (int i1 = 0; i1 < minPercLengthAlin.length; i1++) 
				for (int i2 = 0; i2 < minPercIdentity.length; i2++)
					for (int i3 = 0; i3 < minLengthAlin.length; i3++)
						for (int i4 = 0; i4 < maxPercMistmatches.length; i4++)
							for (int i5 = 0; i5 < maxGapOpenings.length; i5++)
								for (int i6 = 0; i6 < maxEValue.length; i6++)
									for (int i7 = 0; i7 < minBitScore.length; i7++)
										lista.add(new M8RestrictionSimple(minPercLengthAlin[i1], 
												minPercIdentity[i2], 
												minLengthAlin[i3], 
												maxPercMistmatches[i4], 
												maxGapOpenings[i5], 
												maxEValue[i6], 
												minBitScore[i7]));
		return lista;
	}
}
