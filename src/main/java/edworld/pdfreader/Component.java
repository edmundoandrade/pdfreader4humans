// This open source code is distributed without warranties according to the license published at http://www.apache.org/licenses/LICENSE-2.0
package edworld.pdfreader;

import java.util.ArrayList;
import java.util.List;

public abstract class Component implements Comparable<Component> {
	protected float fromX;
	protected float fromY;
	protected float toX;
	protected float toY;
	private List<Component> children = new ArrayList<Component>();

	public Component(float fromX, float fromY, float toX, float toY) {
		this.fromX = fromX;
		this.fromY = fromY;
		this.toX = toX;
		this.toY = toY;
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

	public List<Component> getChildren() {
		return children;
	}

	public void addChild(Component component) {
		getChildren().add(component);
	}

	public boolean contains(Component other) {
		return fromX <= other.getFromX() && toX >= other.getToX() && fromY <= other.getFromY() && toY >= other.getToY();
	}

	public boolean intersects(Component other) {
		return fromX <= other.getToX() && toX >= other.getFromX() && fromY <= other.getToY() && toY >= other.getFromY();
	}

	public int compareTo(Component other) {
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

	public float getWidth() {
		return getToX() - getFromX();
	}

	public float getHeight() {
		return getToY() - getFromY();
	}

	public float getArea() {
		return getWidth() * getHeight();
	}
}
