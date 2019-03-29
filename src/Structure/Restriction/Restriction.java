package Structure.Restriction;

import java.util.Collection;

public interface Restriction <E extends Attribute<E>> {
	public static double delta = Double.valueOf("1E-10");
	
	public boolean check(E atr);

	public boolean check(Collection<E> atr);
	
	public boolean check(E[] atr);
	
	public int sumChecks(E[] atr);
}
