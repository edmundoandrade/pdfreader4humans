// This open source code is distributed without warranties according to the license published at http://www.apache.org/licenses/LICENSE-2.0
package edworld.pdfreader4humans.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edworld.pdfreader4humans.BoxComponent;
import edworld.pdfreader4humans.BoxDetector;
import edworld.pdfreader4humans.Component;
import edworld.pdfreader4humans.GridComponent;

public class MainBoxDetector implements BoxDetector {
	@Override
	public List<BoxComponent> detectBoxes(List<GridComponent> components) {
		return detectBoxes(Component.horizontal(components), Component.vertical(components));
	}

	private List<BoxComponent> detectBoxes(List<Component> horizontalComponents, List<Component> verticalComponents) {
		List<BoxComponent> boxes = new ArrayList<BoxComponent>();
		for (Component horizontalComponent : horizontalComponents)
			detectBoxes(horizontalComponent, verticalComponents, horizontalComponents, boxes);
		Collections.sort(boxes);
		return boxes;
	}

	private void detectBoxes(Component horizontalComponent, List<Component> verticalComponents, List<Component> horizontalComponents, List<BoxComponent> boxes) {
		GridComponent previousVerticalAbove = null;
		GridComponent previousVerticalBelow = null;
		for (GridComponent verticalBound : verticalBounds(horizontalComponent, verticalComponents)) {
			if (verticalBound.getFromY() < horizontalComponent.getFromY()) {
				createBoxAbove(horizontalComponent, previousVerticalAbove, verticalBound, verticalComponents, horizontalComponents, boxes);
				previousVerticalAbove = verticalBound;
			}
			if (verticalBound.getToY() > horizontalComponent.getToY()) {
				createUnboundedBoxBelow(horizontalComponent, previousVerticalBelow, verticalBound, verticalComponents, horizontalComponents, boxes);
				previousVerticalBelow = verticalBound;
			}
		}
		if (previousVerticalAbove != null && previousVerticalAbove.getToX() < horizontalComponent.getToX() - previousVerticalAbove.getWidth())
			createBoxAbove(horizontalComponent, previousVerticalAbove, null, verticalComponents, horizontalComponents, boxes);
		if (previousVerticalBelow != null && previousVerticalBelow.getToX() < horizontalComponent.getToX() - previousVerticalBelow.getWidth())
			createUnboundedBoxBelow(horizontalComponent, previousVerticalBelow, null, verticalComponents, horizontalComponents, boxes);
	}

	private void createBoxAbove(Component horizontalComponent, GridComponent verticalBound1, GridComponent verticalBound2, List<Component> verticalComponents,
			List<Component> horizontalComponents, List<BoxComponent> boxes) {
		boolean borderLeft = (verticalBound1 != null);
		boolean borderRight = (verticalBound2 != null);
		if (!borderLeft)
			verticalBound1 = new GridComponent(verticalBound2.getType(), horizontalComponent.getFromX(), verticalBound2.getFromY(), horizontalComponent.getFromX()
					+ verticalBound2.getWidth(), horizontalComponent.getFromY(), verticalBound2.getLineWidth());
		if (!borderRight)
			verticalBound2 = new GridComponent(verticalBound1.getType(), horizontalComponent.getToX() - verticalBound1.getWidth(), verticalBound1.getFromY(),
					horizontalComponent.getToX(), horizontalComponent.getFromY(), verticalBound1.getLineWidth());
		if (verticalBound1.getToX() >= verticalBound2.getFromX())
			return;
		Component upperBound = horizontalComponent.nextUpperHorizontalComponent(verticalBound1.getToX(), verticalBound2.getFromX(), horizontalComponents);
		float minTop = (upperBound == null ? Float.NEGATIVE_INFINITY : upperBound.getFromY());
		float fromY1 = Math.max(transitiveTop(verticalBound1, verticalComponents), minTop);
		float fromY2 = Math.max(transitiveTop(verticalBound2, verticalComponents), minTop);
		if (Math.abs(fromY1 - fromY2) < horizontalComponent.getHeight()) {
			float fromY = Math.max(fromY1, fromY2);
			boolean borderTop = (upperBound != null && upperBound.getToY() >= fromY);
			boxes.add(new BoxComponent(verticalBound1.getFromX(), fromY, verticalBound2.getToX(), horizontalComponent.getToY(), ((GridComponent) horizontalComponent)
					.getLineWidth(), borderLeft, borderTop, borderRight, true));
		}
	}

	private float transitiveTop(Component component, List<Component> verticalComponents) {
		float top = component.getFromY();
		Component next = nextUpperExtension(component, verticalComponents);
		while (next != null) {
			top = next.getFromY();
			next = nextUpperExtension(next, verticalComponents);
		}
		return top;
	}

	private Component nextUpperExtension(Component verticalComponent, List<Component> verticalComponents) {
		for (Component candidate : verticalComponents)
			if (verticalComponent.verticalExtension(candidate) && candidate.getFromY() < verticalComponent.getFromY())
				return candidate;
		return null;
	}

	private void createUnboundedBoxBelow(Component horizontalComponent, GridComponent verticalBound1, GridComponent verticalBound2, List<Component> verticalComponents,
			List<Component> horizontalComponents, List<BoxComponent> boxes) {
		boolean borderLeft = (verticalBound1 != null);
		boolean borderRight = (verticalBound2 != null);
		if (!borderLeft)
			verticalBound1 = new GridComponent(verticalBound2.getType(), horizontalComponent.getFromX(), horizontalComponent.getToY(), horizontalComponent.getFromX()
					+ verticalBound2.getWidth(), verticalBound2.getToY(), verticalBound2.getLineWidth());
		if (!borderRight)
			verticalBound2 = new GridComponent(verticalBound1.getType(), horizontalComponent.getToX() - verticalBound1.getWidth(), horizontalComponent.getToY(),
					horizontalComponent.getToX(), verticalBound1.getToY(), verticalBound1.getLineWidth());
		if (verticalBound1.getToX() >= verticalBound2.getFromX())
			return;
		Component lowerBound = horizontalComponent.nextLowerHorizontalComponent(verticalBound1.getToX(), verticalBound2.getFromX(), horizontalComponents);
		float maxBottom = (lowerBound == null ? Float.POSITIVE_INFINITY : lowerBound.getToY());
		float toY1 = Math.min(transitiveBottom(verticalBound1, verticalComponents), maxBottom);
		float toY2 = Math.min(transitiveBottom(verticalBound2, verticalComponents), maxBottom);
		float toY = Math.min(toY1, toY2);
		boolean borderBottom = (lowerBound != null && lowerBound.getFromY() <= toY);
		if (!borderBottom && Math.abs(toY1 - toY2) < horizontalComponent.getHeight()) {
			boxes.add(new BoxComponent(verticalBound1.getFromX(), horizontalComponent.getFromY(), verticalBound2.getToX(), toY, ((GridComponent) horizontalComponent)
					.getLineWidth(), borderLeft, true, borderRight, borderBottom));
		}
	}

	private float transitiveBottom(Component component, List<Component> verticalComponents) {
		float bottom = component.getToY();
		Component next = nextLowerExtension(component, verticalComponents);
		while (next != null) {
			bottom = next.getToY();
			next = nextLowerExtension(next, verticalComponents);
		}
		return bottom;
	}

	private Component nextLowerExtension(Component verticalComponent, List<Component> verticalComponents) {
		for (Component candidate : verticalComponents)
			if (verticalComponent.verticalExtension(candidate) && candidate.getFromY() > verticalComponent.getFromY())
				return candidate;
		return null;
	}

	private List<GridComponent> verticalBounds(Component horizontalComponent, List<Component> verticalComponents) {
		List<GridComponent> verticalBounds = new ArrayList<GridComponent>();
		for (Component verticalComponent : verticalComponents)
			if (horizontalComponent.intersects(verticalComponent))
				verticalBounds.add((GridComponent) verticalComponent);
		return verticalBounds;
	}
}
