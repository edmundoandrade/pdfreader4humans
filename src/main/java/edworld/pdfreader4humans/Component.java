// This open source code is distributed without warranties according to the license published at http://www.apache.org/licenses/LICENSE-2.0
package edworld.pdfreader4humans;

import static edworld.pdfreader4humans.TextComponent.UNDERLINE_TOLERANCE;
import static java.util.Collections.sort;
import static java.util.regex.Matcher.quoteReplacement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Component implements Comparable<Component> {
	protected String type;
	protected float fromX;
	protected float fromY;
	protected float toX;
	protected float toY;
	private List<Component> children = new ArrayList<Component>();

	public Component(String type, float fromX, float fromY, float toX, float toY) {
		this.type = type;
		this.fromX = fromX;
		this.fromY = fromY;
		this.toX = toX;
		this.toY = toY;
	}

	public String getType() {
		return type;
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
		if (other instanceof TextComponent)
			return this != other && fromX <= other.getFromX() && toX >= other.getToX() && fromY < other.getToY()
					&& toY >= other.getToY();
		return this != other && fromX <= other.getFromX() && toX >= other.getToX() && fromY <= other.getFromY()
				&& toY >= other.getToY();
	}

	public boolean intersects(Component other) {
		return intersectsHorizontally(other) && intersectsVertically(other);
	}

	public boolean intersectsHorizontally(Component other) {
		return this != other && fromX <= other.getToX() && toX >= other.getFromX();
	}

	public boolean intersectsVertically(Component other) {
		return this != other && fromY <= other.getToY() && toY >= other.getFromY();
	}

	public boolean verticalExtension(Component other) {
		return getFromX() == other.getFromX() && getToX() == other.getToX() && intersects(other);
	}

	public boolean verticallyBefore(Component other) {
		return getToY() - getHeight() / 20 < other.getFromY() + other.getHeight() / 20;
	}

	public boolean verticallyAfter(Component other) {
		return other.verticallyBefore(this);
	}

	public int compareTo(Component other) {
		int result = 0;
		if (verticallyBefore(other))
			result = -1;
		else if (verticallyAfter(other))
			result = 1;
		else if (getFromX() < other.getFromX())
			result = -1;
		else if (getFromX() > other.getFromX())
			result = 1;
		else if (getFromY() < other.getFromY())
			result = -1;
		else if (getFromY() > other.getFromY())
			result = 1;
		return result;
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

	public String output(String template) {
		String output = fillTemplate(template, "type", getType());
		output = fillTemplate(output, "fromX", getFromX());
		output = fillTemplate(output, "fromY", getFromY());
		output = fillTemplate(output, "toX", getToX());
		output = fillTemplate(output, "toY", getToY());
		return output;
	}

	protected String fillTemplate(String template, String fieldName, Object fieldValue) {
		return template.replaceAll("\\$\\{" + fieldName + "\\}", quoteReplacement(fieldValue.toString()));
	}

	@Override
	public String toString() {
		return type + " :: " + fromX + ", " + fromY + ", " + toX + ", " + toY;
	}

	public static List<Component> horizontal(List<? extends Component> components) {
		List<Component> horizontalComponents = new ArrayList<Component>();
		for (Component component : components)
			if (component.getWidth() > component.getHeight())
				horizontalComponents.add(component);
		sort(horizontalComponents, orderByYX());
		return horizontalComponents;
	}

	public static List<Component> vertical(List<? extends Component> components) {
		List<Component> verticalComponents = new ArrayList<Component>();
		for (Component component : components)
			if (component.getHeight() > component.getWidth())
				verticalComponents.add(component);
		sort(verticalComponents, orderByXY());
		return verticalComponents;
	}

	public static <T extends Component> void smartSort(List<T> list) {
		Map<T, List<T>> mapRemovedAfter = new HashMap<T, List<T>>();
		Map<T, T> mapRemovedBefore = new HashMap<T, T>();
		for (int i = 0; i < list.size(); i++) {
			T component1 = list.get(i);
			for (int j = i + 1; j < list.size(); j++) {
				T component2 = list.get(j);
				if (!component1.verticallyBefore(component2) && !component1.verticallyAfter(component2)) {
					T afterComponent;
					T beforeComponent;
					if (component1.compareTo(component2) < 0) {
						afterComponent = component2;
						beforeComponent = component1;
					} else {
						afterComponent = component1;
						beforeComponent = component2;
					}
					if (!mapRemovedBefore.containsKey(beforeComponent)) {
						while (mapRemovedBefore.containsKey(afterComponent))
							afterComponent = mapRemovedBefore.get(afterComponent);
						if (!mapRemovedAfter.containsKey(afterComponent))
							mapRemovedAfter.put(afterComponent, new ArrayList<T>());
						if (mapRemovedAfter.containsKey(beforeComponent)) {
							mapRemovedAfter.get(afterComponent).addAll(mapRemovedAfter.get(beforeComponent));
							mapRemovedAfter.remove(beforeComponent);
						}
						mapRemovedAfter.get(afterComponent).add(beforeComponent);
						mapRemovedBefore.put(beforeComponent, afterComponent);
					}
				}
			}
		}
		for (T componentAfter : mapRemovedAfter.keySet())
			list.removeAll(mapRemovedAfter.get(componentAfter));
		Collections.sort(list);
		for (T componentAfter : mapRemovedAfter.keySet()) {
			List<T> beforeList = mapRemovedAfter.get(componentAfter);
			smartSort(beforeList);
			list.addAll(list.indexOf(componentAfter), beforeList);
		}
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

	public boolean underlineOf(Component component) {
		return getToY() > component.getFromY() && getToY() - component.getToY() <= UNDERLINE_TOLERANCE
				&& getFromX() <= component.getToX() && getToX() >= component.getFromX();
	}
}
