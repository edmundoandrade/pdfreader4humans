// This open source code is distributed without warranties according to the license published at http://www.apache.org/licenses/LICENSE-2.0
package edworld.pdfreader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
		return this != other && fromX <= other.getFromX() && toX >= other.getToX() && fromY <= other.getFromY() && toY >= other.getToY();
	}

	public boolean intersects(Component other) {
		return this != other && fromX <= other.getToX() && toX >= other.getFromX() && fromY <= other.getToY() && toY >= other.getFromY();
	}

	public boolean verticalExtension(Component other) {
		return getFromX() == other.getFromX() && getToX() == other.getToX() && intersects(other);
	}

	public int compareTo(Component other) {
		if (getToY() < other.getFromY())
			return -1;
		else if (getFromY() > other.getToY())
			return 1;
		if (getFromX() < other.getFromX())
			return -1;
		else if (getFromX() > other.getFromX())
			return 1;
		if (getFromY() < other.getFromY())
			return -1;
		else if (getFromY() > other.getFromY())
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

	public Component nextUpperHorizontalComponent(float maxLeft, float minRight, List<Component> horizontalComponents) {
		Component found = null;
		for (Component candidate : horizontalComponents)
			if (candidate.getToY() >= getFromY())
				break;
			else if (candidate.getFromX() <= maxLeft && candidate.getToX() >= minRight)
				found = candidate;
		return found;
	}

	public Component nextLowerHorizontalComponent(float maxLeft, float minRight, List<Component> horizontalComponents) {
		for (Component candidate : horizontalComponents)
			if (candidate.getFromY() > getToY() && candidate.getFromX() <= maxLeft && candidate.getToX() >= minRight)
				return candidate;
		return null;
	}

	public static List<Component> horizontal(List<? extends Component> components) {
		List<Component> horizontalComponents = new ArrayList<Component>();
		for (Component component : components)
			if (component.getWidth() > component.getHeight())
				horizontalComponents.add(component);
		Collections.sort(horizontalComponents, orderByYX());
		return horizontalComponents;
	}

	public static List<Component> vertical(List<? extends Component> components) {
		List<Component> verticalComponents = new ArrayList<Component>();
		for (Component component : components)
			if (component.getHeight() > component.getWidth())
				verticalComponents.add(component);
		Collections.sort(verticalComponents, orderByXY());
		return verticalComponents;
	}

	public static Comparator<Component> orderByYX() {
		return new Comparator<Component>() {
			@Override
			public int compare(Component component1, Component component2) {
				int compare = Float.compare(component1.getFromY(), component2.getFromY());
				if (compare == 0)
					compare = Float.compare(component1.getFromX(), component2.getFromX());
				return compare;
			}
		};
	}

	public static Comparator<Component> orderByXY() {
		return new Comparator<Component>() {
			@Override
			public int compare(Component component1, Component component2) {
				int compare = Float.compare(component1.getFromX(), component2.getFromX());
				if (compare == 0)
					compare = Float.compare(component1.getFromY(), component2.getFromY());
				return compare;
			}
		};
	}
}
