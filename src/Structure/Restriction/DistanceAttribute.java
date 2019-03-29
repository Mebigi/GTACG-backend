package Structure.Restriction;

public class DistanceAttribute implements Attribute<DistanceAttribute> {
	public double distance;
	public DistanceAttribute(double distance) {
		this.distance = distance;
	}
	@Override
	public boolean better(DistanceAttribute attr) {
		return distance >= attr.distance;
	}
	@Override
	public boolean equals(DistanceAttribute attr) {
		return Math.abs(distance - attr.distance) <= delta;
	}
	@Override
	public DistanceAttribute[] newArray() {
		return new DistanceAttribute[1];
	}
	@Override
	public DistanceAttribute[] newArray(int len) {
		return new DistanceAttribute[len];
	}
}
