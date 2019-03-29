package Structure;

public class Pair {
	public int a;
	public int b;
	public Pair(int a, int b) {
		this.a = a;
		this.b = b;
	}
	synchronized void soma(Pair pair) {
		this.a += pair.a;
		this.b += pair.b;
	}
}
