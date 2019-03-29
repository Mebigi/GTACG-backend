package Structure;

public class PairLong {
	public long a;
	public long b;
	public PairLong(long a, long b) {
		this.a = a;
		this.b = b;
	}
	
	public synchronized void sum(PairLong pair) {
		this.a += pair.a;
		this.b += pair.b;
	}
	
	public String toString() {
		return "(" + a + ", " + b + ")";
	}
	
	public static PairLong[] makeArray(int num, PairLong base) {
		PairLong[] vet = new PairLong[num];
		for (int i = 0; i < vet.length; i++) {
			vet[i] = new PairLong(base.a, base.b);
		}
		return vet;
	}
}
