package edworld.pdfreader.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edworld.pdfreader.BoxDetector;
import edworld.pdfreader.Component;
import edworld.pdfreader.GridComponent;

public class BoxDetectorImpl implements BoxDetector {
	private static final String GRID_BOX = "box";
	private static final String GRID_SEGMENT = "segment";

	@Override
	public List<GridComponent> detectBoxes(List<GridComponent> components) {
		List<GridComponent> boxes = new ArrayList<GridComponent>();
		List<GridComponent> verticalComponents = vertical(components);
		List<GridComponent> horizontalConnectedSegments = horizontalConnectedSegments(horizontal(components), verticalComponents);
		float borderWidth = maxBorderWidth(verticalComponents);
		for (GridComponent component1 : horizontalConnectedSegments)
			for (GridComponent component2 : horizontalConnectedSegments)
				if (nearBelow(component1, component2, borderWidth)) {
					addBox(component1, component2, boxes);
					break;
				}
		Collections.sort(boxes);
		return boxes;
	}

	private boolean nearBelow(Component component1, Component component2, float borderWidth) {
		return component1.getFromY() < component2.getFromY() && component1.getFromX() + borderWidth < component2.getToX()
				&& component2.getFromX() + borderWidth < component1.getToX();
	}

	private void addBox(GridComponent component1, Component component2, List<GridComponent> boxes) {
		if (component1.getFromX() == component2.getFromX() && component1.getToX() == component2.getToX())
			boxes.add(new GridComponent(GRID_BOX, component1.getFromX(), component1.getFromY(), component2.getToX(), component2.getToY(), component1.getLineWidth()));
		else if (component2.toString().startsWith(GRID_SEGMENT) && component2.getChildren().size() == 1)
			addBox(component1, component2.getChildren().get(0), boxes);
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

	private List<GridComponent> horizontalConnectedSegments(List<GridComponent> horizontalComponents, List<GridComponent> verticalComponents) {
		List<GridComponent> segments = new ArrayList<GridComponent>();
		for (GridComponent horizontalComponent : horizontalComponents)
			segments.addAll(horizontalConnectedSegments(horizontalComponent, verticalComponents));
		addCoverSegments(horizontalComponents, verticalComponents, segments);
		return segments;
	}

	private List<GridComponent> horizontalConnectedSegments(GridComponent horizontalComponent, List<GridComponent> verticalComponents) {
		List<GridComponent> segments = new ArrayList<GridComponent>();
		boolean connected = false;
		float fromX = horizontalComponent.getFromX();
		for (GridComponent verticalComponent : verticalComponents)
			if (horizontalComponent.intersects(verticalComponent)) {
				connected = true;
				if (fromX != verticalComponent.getFromX()) {
					addSegment(fromX, horizontalComponent.getFromY(), verticalComponent.getToX(), horizontalComponent.getToY(), horizontalComponent.getLineWidth(),
							horizontalComponent, segments);
					fromX = verticalComponent.getFromX();
				}
			}
		if (connected) {
			if (segments.size() == 0)
				segments.add(horizontalComponent);
			else if (fromX < horizontalComponent.getToX())
				addSegment(fromX, horizontalComponent.getFromY(), horizontalComponent.getToX(), horizontalComponent.getToY(), horizontalComponent.getLineWidth(),
						horizontalComponent, segments);
		}
		return segments;
	}

	private void addCoverSegments(List<GridComponent> horizontalComponents, List<GridComponent> verticalComponents, List<GridComponent> segments) {
		float borderHeight = maxBorderHeight(horizontalComponents);
		for (GridComponent component1 : verticalComponents)
			for (GridComponent component2 : verticalComponents)
				if (component1.getFromX() < component2.getFromX() && component1.getFromY() + borderHeight < component2.getToY()
						&& component2.getFromY() + borderHeight < component1.getToY()) {
					if (component1.getFromY() == component2.getFromY())
						addSegment(component1.getFromX(), component1.getFromY(), component2.getToX(), component1.getFromY(), component1.getLineWidth(), null, segments);
					if (component1.getToY() == component2.getToY())
						addSegment(component1.getFromX(), component1.getToY(), component2.getToX(), component1.getToY(), component1.getLineWidth(), null, segments);
					break;
				}
	}

	private void addSegment(float fromX, float fromY, float toX, float toY, double lineWidth, GridComponent sourceComponent, List<GridComponent> segments) {
		GridComponent newSegment = new GridComponent(GRID_SEGMENT, fromX, fromY, toX, toY, lineWidth);
		if (sourceComponent != null)
			newSegment.addChild(sourceComponent);
		for (GridComponent oldSegment : segments)
			if (oldSegment.contains(newSegment))
				return;
		segments.add(newSegment);
	}

	private float maxBorderWidth(List<GridComponent> verticalComponents) {
		float borderWidth = 0;
		for (GridComponent component : verticalComponents)
			borderWidth = Math.max(component.getToX() - component.getFromX(), borderWidth);
		return borderWidth;
	}

	private float maxBorderHeight(List<GridComponent> horizontalComponents) {
		float borderHeight = 0;
		for (GridComponent component : horizontalComponents)
			borderHeight = Math.max(component.getToY() - component.getFromY(), borderHeight);
		return borderHeight;
	}
}
