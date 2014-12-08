package edworld.pdfreader.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edworld.pdfreader.BoxComponent;
import edworld.pdfreader.BoxDetector;
import edworld.pdfreader.Component;
import edworld.pdfreader.GridComponent;

public class BoxDetectorImpl implements BoxDetector {
	@Override
	public List<BoxComponent> detectBoxes(List<GridComponent> components) {
		return detectBoxes(horizontal(components), vertical(components));
	}

	private List<BoxComponent> detectBoxes(List<GridComponent> horizontalComponents, List<GridComponent> verticalComponents) {
		List<BoxComponent> boxes = new ArrayList<BoxComponent>();
		for (GridComponent horizontalComponent : horizontalComponents)
			detectBoxes(horizontalComponent, verticalComponents, horizontalComponents, boxes);
		Collections.sort(boxes);
		return boxes;
	}

	private void detectBoxes(GridComponent horizontalComponent, List<GridComponent> verticalComponents, List<GridComponent> horizontalComponents, List<BoxComponent> boxes) {
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

	private void createBoxAbove(GridComponent horizontalComponent, GridComponent verticalBound1, GridComponent verticalBound2, List<GridComponent> verticalComponents,
			List<GridComponent> horizontalComponents, List<BoxComponent> boxes) {
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
		GridComponent upperBound = nextUpperHorizontalComponent(horizontalComponent.getFromY(), verticalBound1.getToX(), verticalBound2.getFromX(), horizontalComponents);
		float minTop = (upperBound == null ? Float.NEGATIVE_INFINITY : upperBound.getFromY());
		float fromY1 = Math.max(transitiveTop(verticalBound1, verticalComponents), minTop);
		float fromY2 = Math.max(transitiveTop(verticalBound2, verticalComponents), minTop);
		if (Math.abs(fromY1 - fromY2) < horizontalComponent.getHeight()) {
			float fromY = Math.max(fromY1, fromY2);
			boolean borderTop = (upperBound != null && upperBound.getToY() >= fromY);
			boxes.add(new BoxComponent(verticalBound1.getFromX(), fromY, verticalBound2.getToX(), horizontalComponent.getToY(), horizontalComponent.getLineWidth(), borderLeft,
					borderTop, borderRight, true));
		}
	}

	private GridComponent nextUpperHorizontalComponent(float maxBottom, float maxLeft, float minRight, List<GridComponent> horizontalComponents) {
		GridComponent found = null;
		for (GridComponent candidate : horizontalComponents)
			if (candidate.getToY() >= maxBottom)
				break;
			else if (candidate.getFromX() <= maxLeft && candidate.getToX() >= minRight)
				found = candidate;
		return found;
	}

	private float transitiveTop(GridComponent component, List<GridComponent> verticalComponents) {
		float top = component.getFromY();
		GridComponent next = nextUpperExtension(component, verticalComponents);
		while (next != null) {
			top = next.getFromY();
			next = nextUpperExtension(next, verticalComponents);
		}
		return top;
	}

	private GridComponent nextUpperExtension(GridComponent verticalComponent, List<GridComponent> verticalComponents) {
		for (GridComponent candidate : verticalComponents)
			if (verticalExtension(verticalComponent, candidate) && candidate.getFromY() < verticalComponent.getFromY())
				return candidate;
		return null;
	}

	private void createUnboundedBoxBelow(GridComponent horizontalComponent, GridComponent verticalBound1, GridComponent verticalBound2, List<GridComponent> verticalComponents,
			List<GridComponent> horizontalComponents, List<BoxComponent> boxes) {
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
		GridComponent lowerBound = nextLowerHorizontalComponent(horizontalComponent.getToY(), verticalBound1.getToX(), verticalBound2.getFromX(), horizontalComponents);
		float maxBottom = (lowerBound == null ? Float.POSITIVE_INFINITY : lowerBound.getToY());
		float toY1 = Math.min(transitiveBottom(verticalBound1, verticalComponents), maxBottom);
		float toY2 = Math.min(transitiveBottom(verticalBound2, verticalComponents), maxBottom);
		float toY = Math.min(toY1, toY2);
		boolean borderBottom = (lowerBound != null && lowerBound.getFromY() <= toY);
		if (!borderBottom && Math.abs(toY1 - toY2) < horizontalComponent.getHeight()) {
			boxes.add(new BoxComponent(verticalBound1.getFromX(), horizontalComponent.getFromY(), verticalBound2.getToX(), toY, horizontalComponent.getLineWidth(), borderLeft,
					true, borderRight, borderBottom));
		}
	}

	private GridComponent nextLowerHorizontalComponent(float minTop, float maxLeft, float minRight, List<GridComponent> horizontalComponents) {
		for (GridComponent candidate : horizontalComponents)
			if (candidate.getFromY() > minTop && candidate.getFromX() <= maxLeft && candidate.getToX() >= minRight)
				return candidate;
		return null;
	}

	private float transitiveBottom(GridComponent component, List<GridComponent> verticalComponents) {
		float bottom = component.getToY();
		GridComponent next = nextLowerExtension(component, verticalComponents);
		while (next != null) {
			bottom = next.getToY();
			next = nextLowerExtension(next, verticalComponents);
		}
		return bottom;
	}

	private GridComponent nextLowerExtension(GridComponent verticalComponent, List<GridComponent> verticalComponents) {
		for (GridComponent candidate : verticalComponents)
			if (verticalExtension(verticalComponent, candidate) && candidate.getFromY() > verticalComponent.getFromY())
				return candidate;
		return null;
	}

	private boolean verticalExtension(GridComponent component1, GridComponent component2) {
		return component1.getFromX() == component2.getFromX() && component1.getToX() == component2.getToX() && component1.intersects(component2);
	}

	private List<GridComponent> verticalBounds(GridComponent horizontalComponent, List<GridComponent> verticalComponents) {
		List<GridComponent> verticalBounds = new ArrayList<GridComponent>();
		for (GridComponent verticalComponent : verticalComponents)
			if (horizontalComponent.intersects(verticalComponent))
				verticalBounds.add(verticalComponent);
		return verticalBounds;
	}

	private List<GridComponent> horizontal(List<GridComponent> components) {
		List<GridComponent> horizontalComponents = new ArrayList<GridComponent>();
		for (GridComponent component : components)
			if (component.getWidth() > component.getHeight())
				horizontalComponents.add(component);
		Collections.sort(horizontalComponents, orderByYX());
		return horizontalComponents;
	}

	private List<GridComponent> vertical(List<GridComponent> components) {
		List<GridComponent> verticalComponents = new ArrayList<GridComponent>();
		for (GridComponent component : components)
			if (component.getHeight() > component.getWidth())
				verticalComponents.add(component);
		Collections.sort(verticalComponents, orderByXY());
		return verticalComponents;
	}

	private Comparator<Component> orderByYX() {
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

	private Comparator<Component> orderByXY() {
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
