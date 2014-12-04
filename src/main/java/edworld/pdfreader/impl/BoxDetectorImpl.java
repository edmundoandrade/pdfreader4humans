package edworld.pdfreader.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edworld.pdfreader.BoxDetector;
import edworld.pdfreader.GridComponent;

public class BoxDetectorImpl implements BoxDetector {
	@Override
	public List<GridComponent> detectBoxes(List<GridComponent> components) {
		List<GridComponent> sortedComponents = new ArrayList<GridComponent>(components);
		Collections.sort(sortedComponents);
		List<GridComponent> boxes = new ArrayList<GridComponent>();
		List<GridComponent> verticalComponents = vertical(sortedComponents);
		List<GridComponent> horizontalSegments = segments(horizontal(sortedComponents), verticalComponents);
		float borderWidth = maxBorderWidth(verticalComponents);
		for (GridComponent component1 : horizontalSegments)
			for (GridComponent component2 : horizontalSegments)
				if (component1.getFromY() < component2.getFromY() && component1.getFromX() + borderWidth < component2.getToX()
						&& component2.getFromX() + borderWidth < component1.getToX()) {
					addBox(component1, component2, boxes);
					break;
				}
		Collections.sort(boxes);
		return boxes;
	}

	private void addBox(GridComponent component1, GridComponent component2, List<GridComponent> boxes) {
		if (component1.getFromX() == component2.getFromX() && component1.getToX() == component2.getToX())
			boxes.add(new GridComponent("box", component1.getFromX(), component1.getFromY(), component2.getToX(), component2.getToY(), component1.getLineWidth()));
	}

	private List<GridComponent> horizontal(List<GridComponent> components) {
		List<GridComponent> horizontalComponents = new ArrayList<GridComponent>();
		for (GridComponent component : components)
			if (component.getWidth() > component.getHeight())
				horizontalComponents.add(component);
		return horizontalComponents;
	}

	private List<GridComponent> vertical(List<GridComponent> components) {
		List<GridComponent> verticalComponents = new ArrayList<GridComponent>();
		for (GridComponent component : components)
			if (component.getHeight() > component.getWidth())
				verticalComponents.add(component);
		return verticalComponents;
	}

	private List<GridComponent> segments(List<GridComponent> horizontalComponents, List<GridComponent> verticalComponents) {
		List<GridComponent> segments = new ArrayList<GridComponent>();
		for (GridComponent horizontalComponent : horizontalComponents)
			segments.addAll(segments(horizontalComponent, verticalComponents));
		addCoverSegments(horizontalComponents, verticalComponents, segments);
		return segments;
	}

	private List<GridComponent> segments(GridComponent horizontalComponent, List<GridComponent> verticalComponents) {
		List<GridComponent> segments = new ArrayList<GridComponent>();
		float fromX = horizontalComponent.getFromX();
		for (GridComponent verticalComponent : verticalComponents)
			if (horizontalComponent.intersects(verticalComponent)) {
				if (fromX != verticalComponent.getFromX()) {
					segments.add(new GridComponent("segment", fromX, horizontalComponent.getFromY(), verticalComponent.getToX(), horizontalComponent.getToY(), horizontalComponent
							.getLineWidth()));
					fromX = verticalComponent.getFromX();
				}
			}
		if (segments.size() == 0)
			segments.add(horizontalComponent);
		else if (fromX < horizontalComponent.getToX())
			addSegment(fromX, horizontalComponent.getFromY(), horizontalComponent.getToX(), horizontalComponent.getToY(), horizontalComponent.getLineWidth(), segments);
		return segments;
	}

	private void addCoverSegments(List<GridComponent> horizontalComponents, List<GridComponent> verticalComponents, List<GridComponent> segments) {
		float borderHeight = maxBorderHeight(horizontalComponents);
		for (GridComponent component1 : verticalComponents)
			for (GridComponent component2 : verticalComponents)
				if (component1.getFromX() < component2.getFromX() && component1.getFromY() + borderHeight < component2.getToY()
						&& component2.getFromY() + borderHeight < component1.getToY()) {
					if (component1.getFromY() == component2.getFromY())
						addSegment(component1.getFromX(), component1.getFromY(), component2.getToX(), component1.getFromY(), component1.getLineWidth(), segments);
					if (component1.getToY() == component2.getToY())
						addSegment(component1.getFromX(), component1.getToY(), component2.getToX(), component1.getToY(), component1.getLineWidth(), segments);
					break;
				}
	}

	private void addSegment(float fromX, float fromY, float toX, float toY, double lineWidth, List<GridComponent> segments) {
		GridComponent newSegment = new GridComponent("segment", fromX, fromY, toX, toY, lineWidth);
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
