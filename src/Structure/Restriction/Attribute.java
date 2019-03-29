package Structure.Restriction;

public interface Attribute<E extends Attribute<E>> {
	public static double delta = Double.valueOf("1E-10");
	
	public abstract boolean better(E atr);
	
	public abstract boolean equals(E atr);
	
	public abstract E[] newArray();
	
	public abstract E[] newArray(int len);
}
