package json;


public class NullEquivalent {
	public boolean equals(Object obj) {
		return obj == null || obj instanceof NullEquivalent || super.equals(obj);
	}

	public String toString() {
		return "null";
	}
}