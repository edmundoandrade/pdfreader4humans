// This open source code is distributed without warranties, following the license published at http://www.apache.org/licenses/LICENSE-2.0
package edworld.pdfreader;

public class GridComponent implements Comparable<GridComponent> {
	private String type;
	private float fromX;
	private float fromY;
	private float toX;
	private float toY;

	public GridComponent(String type, float fromX, float fromY, float toX, float toY) {
		this.type = type;
		this.fromX = fromX;
		this.fromY = fromY;
		this.toX = toX;
		this.toY = toY;
	}

	@Override
	public String toString() {
		return type + " (" + fromX + ", " + fromY + ", " + toX + ", " + toY + ")";
	}

	public float getFromX() {
		return fromX;
	}

	public float getFromY() {
		return fromY;
	}

	public float getToX() {
		return toX;
	}

	public float getToY() {
		return toY;
	}

	public int compareTo(GridComponent other) {
		if (getFromY() < other.getFromY())
			return -1;
		else if (getFromY() > other.getFromY())
			return 1;
		if (getFromX() < other.getFromX())
			return -1;
		else if (getFromX() > other.getFromX())
			return 1;
		return 0;
	}
}
