package Structure.Restriction;

import java.util.Comparator;

import Structure.Registry.Dictionary;
import Structure.Registry.GeneRegistry;

public class M8Attribute implements Attribute<M8Attribute> {
	private GeneRegistry geneA;
	//public GeneRegistry geneB;
	private float percLengthAlign;
	private float percIdentity;
	private short lengthAlign;
	private float percMistmatches;
	private short gapOpenings;
	private double eValue;
	private float bitScore;
	
	public GeneRegistry getGeneA() {
		return geneA;
	}
	
	public float getPercLengthAlign() {
		return percLengthAlign;
	}
	
	public float getPercIdentity() {
		return percIdentity;
	}
	
	public short getLengthAlign() {
		return lengthAlign;
	}
	
	public float getPercMistmatches() {
		return percMistmatches;
	}
	
	public short getGapOpenings() { 
		return gapOpenings;
	}
	
	public double getEValue() {
		return eValue;
	}
	
	public float getBitScore() {
		return bitScore;
	}

	
	/*public M8Attribute(boolean directed, String line, Map<String, Integer> geneLen) {
		String linhaSplit[] = line.split("\t");
		String query = linhaSplit[0].trim();
		String subject = linhaSplit[1].trim();
		percIdentity = Float.parseFloat(linhaSplit[2]);
		lengthAlin = (short)Integer.parseInt(linhaSplit[3]);
		float mistmatches = Integer.parseInt(linhaSplit[4]);
		gapOpenings = (short)Integer.parseInt(linhaSplit[5]);
		eValue = Double.parseDouble(linhaSplit[10]);
		bitScore = Float.parseFloat(linhaSplit[11].trim());
		
		Integer queryLen = geneLen.get(query);
		Integer subjectLen = geneLen.get(subject);
		float a = (float)100*Integer.parseInt(linhaSplit[3]);
		float b = Math.min(queryLen, subjectLen);
		percLengthAlin = a/b;
		percMistmatches = mistmatches/lengthAlin;
	}*/
	
	public M8Attribute(String line, Dictionary dic) {
		String linhaSplit[] = line.split("\t");
		String query = linhaSplit[0].trim();
		String subject = linhaSplit[1].trim();
		percIdentity = Float.parseFloat(linhaSplit[2]);
		lengthAlign = (short)Integer.parseInt(linhaSplit[3]);
		float mistmatches = Integer.parseInt(linhaSplit[4]);
		gapOpenings = (short)Integer.parseInt(linhaSplit[5]);
		eValue = Double.parseDouble(linhaSplit[10]);
		bitScore = Float.parseFloat(linhaSplit[11].trim());
		
		geneA = dic.getGeneByKey(query);
		GeneRegistry geneB = dic.getGeneByKey(subject);
		
		int queryLen = (int)geneA.getLengthByteSeq();
		int subjectLen = (int)geneB.getLengthByteSeq();
		float a = (float)100*Integer.parseInt(linhaSplit[3]);
		
		float b = Math.max(queryLen, subjectLen);
		percLengthAlign = a/b;
		int x = Math.max(((int)b-lengthAlign), 0);
		percMistmatches = (100*(mistmatches+x))/b;
		//OLD
		/*float b = Math.min(queryLen, subjectLen);
		percLengthAlin = a/b;
		percMistmatches = mistmatches/lengthAlin;*/
	}
	
	public M8Attribute(GeneRegistry geneA, GeneRegistry geneB, float percLengthAlin, float percIdentity, int lengthAlin, float percMistmatches, int gapOpenings, double eValue, float bitScore) {
		this.geneA = geneA;
		//this.geneB = geneB;
		this.percLengthAlign = (float)percLengthAlin;
		this.percIdentity = (float)percIdentity;
		this.lengthAlign = (short)lengthAlin;
		this.percMistmatches = (float)percMistmatches;
		this.gapOpenings = (short)gapOpenings;
		this.eValue = eValue;
		this.bitScore = (float)bitScore;
	}
	
	/*public boolean better(M8Attribute atr) {
		if(
		(atr.getPercLengthAlign() >= getPercLengthAlign() - delta) &&
		//(atr.getPercIdentity() >= getPercIdentity() - delta) &&
		//(atr.getLengthAlign() >= getLengthAlign()) &&
		//(atr.getPercMistmatches() <= getPercMistmatches() - delta) &&
		//(atr.getGapOpenings() <= getGapOpenings()) &&
		(atr.getEValue() <= getEValue()))// &&
		//(atr.getBitScore() >= getBitScore() - delta))
			return true;
		return false;
	}*/
	
	public boolean better(M8Attribute atr) {
		if(
		(getPercLengthAlign() >= atr.getPercLengthAlign() - delta) &&
		//(atr.getPercIdentity() >= getPercIdentity() - delta) &&
		//(atr.getLengthAlign() >= getLengthAlign()) &&
		//(atr.getPercMistmatches() <= getPercMistmatches() - delta) &&
		//(atr.getGapOpenings() <= getGapOpenings()) &&
		(getEValue() <= atr.getEValue()))// &&
		//(atr.getBitScore() >= getBitScore() - delta))
			return true;
		return false;
	}
	
	public boolean equals(M8Attribute linha) {
		if((Math.abs(linha.getPercLengthAlign() - getPercLengthAlign()) <= delta) &&
		//(Math.abs(linha.getPercIdentity() - getPercIdentity()) <= delta) &&
		//(linha.getLengthAlign() == getLengthAlign()) &&
		//(linha.getPercMistmatches() == getPercMistmatches()) &&
		//(linha.getGapOpenings() == getGapOpenings()) &&
		(Math.abs(linha.getEValue() - getEValue()) <= delta))// &&
		//(Math.abs(linha.getBitScore() - getBitScore()) <= delta))
			return true;
		return false;
	}

	@Override
	public String toString() {
		return getPercLengthAlign() + "\t" + 
		getPercIdentity() + "\t" +
		getLengthAlign() + "\t" +
		getPercMistmatches() + "\t" +
		getGapOpenings() + "\t" +
		getEValue() + "\t" +
		getBitScore(); 
	}
	
	public String toStringCompact() {
		return "(" + getPercLengthAlign() + "," + 
		getPercIdentity() + "," +
		getLengthAlign() + "," +
		getPercMistmatches() + "," +
		getGapOpenings() + "," +
		getEValue() + "," +
		getBitScore() + ")";
	}
	
	@Override
	public M8Attribute[] newArray() {
		return new M8Attribute[1];
	}

	@Override
	public M8Attribute[] newArray(int len) {
		return new M8Attribute[len];
	}
	
	public M8Restriction revert() {
		return new M8Restriction(getPercLengthAlign(), getPercIdentity(), getLengthAlign(), getPercMistmatches(), getGapOpenings(), getEValue(), getBitScore());
	}
	
	public static Comparator<M8Attribute> comparator = new Comparator<M8Attribute>() {
		public int compare(M8Attribute o1, M8Attribute o2) {
			//double delta = Double.valueOf("1E-2");
			if(o1.getPercLengthAlign() < o2.getPercLengthAlign())
				return -1;
			if(o1.getPercLengthAlign() > o2.getPercLengthAlign())
				return 1;
					
			if(o1.getPercIdentity() < o2.getPercIdentity())
				return -1;
			if(o1.getPercIdentity() > o2.getPercIdentity())
				return 1;
						
			if(o1.getLengthAlign() < o2.getLengthAlign())
				return -1;
			if(o1.getLengthAlign() > o2.getLengthAlign())
				return 1;
			
			if(o1.getPercMistmatches() < o2.getPercMistmatches())
				return -1;
			if(o1.getPercMistmatches() > o2.getPercMistmatches())
				return 1;
			
			if(o1.getGapOpenings() < o2.getGapOpenings())
				return -1;
			if(o1.getGapOpenings() > o2.getGapOpenings())
				return 1;
					
			if(o1.getEValue() < o2.getEValue() - delta)
				return -1;
			if(o1.getEValue() > o2.getEValue() + delta)
				return 1;
			
			if(o1.getBitScore() < o2.getBitScore())
				return -1;
			if(o1.getBitScore() > o2.getBitScore())
				return -1;
			return 0;
		}
	};
}
