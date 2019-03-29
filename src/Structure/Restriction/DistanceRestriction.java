package Structure.Restriction;

import java.util.Collection;

public class DistanceRestriction implements Restriction<DistanceAttribute> {
	double distance;
	
	public DistanceRestriction(double distance) {
		this.distance = distance;
	}
	
	public boolean check(DistanceAttribute attr) {
		if(distance < attr.distance - delta)
			return true;
		return false;
	}
	
	public boolean check(Collection<DistanceAttribute> atr) {
		if(atr == null)
			return false;
		for (DistanceAttribute linha : atr) {
			if(check(linha))
				return true;
		}
		return false;
	}
	
	public boolean check(DistanceAttribute[] atr) {
		if(atr == null)
			return false;
		for (DistanceAttribute linha : atr) {
			if(check(linha)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int sumChecks(DistanceAttribute[] atr) {
		int total = 0;
		if(atr == null)
			return total;
		for (DistanceAttribute linha : atr) {
			if(check(linha)) {
				total++;
			}
		}
		return total;
	}


	public String toString() {
		return "" + distance;
	}
}
