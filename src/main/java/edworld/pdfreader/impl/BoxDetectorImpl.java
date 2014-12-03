package edworld.pdfreader.impl;

import java.util.ArrayList;
import java.util.List;

import edworld.pdfreader.BoxDetector;
import edworld.pdfreader.GridComponent;

public class BoxDetectorImpl implements BoxDetector {
	@Override
	public List<GridComponent> detectBoxes(List<GridComponent> components) {
		List<GridComponent> boxes = new ArrayList<GridComponent>();
		List<GridComponent> verticalComponents = vertical(components);
		List<GridComponent> horizontalSegments = segments(horizontal(components), verticalComponents);
		float borderWidth = maxBorderWidth(verticalComponents);
		for (GridComponent component1 : horizontalSegments)
			for (GridComponent component2 : horizontalSegments)
				if (component1.getFromY() < component2.getFromY() && component1.getFromX() + borderWidth < component2.getToX()
						&& component2.getFromX() + borderWidth < component1.getToX()) {
					addBox(component1, component2, boxes);
					break;
				}
		return boxes;
	}

	private float maxBorderWidth(List<GridComponent> verticalComponents) {
		float borderWidth = 0;
		for (GridComponent component : verticalComponents)
			borderWidth = Math.max(component.getToX() - component.getFromX(), borderWidth);
		return borderWidth;
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

	private List<GridComponent> segments(List<GridComponent> components, List<GridComponent> transversalComponents) {
		List<GridComponent> segments = new ArrayList<GridComponent>();
		for (GridComponent component : components)
			segments.addAll(segments(component, transversalComponents));
		return segments;
	}

	private List<GridComponent> segments(GridComponent component, List<GridComponent> transversalComponents) {
		List<GridComponent> segments = new ArrayList<GridComponent>();
		float fromX = component.getFromX();
		for (GridComponent transversalComponent : transversalComponents)
			if (component.intersects(transversalComponent)) {
				if (fromX != transversalComponent.getFromX()) {
					GridComponent segment = new GridComponent("segment", fromX, component.getFromY(), transversalComponent.getToX(), component.getToY(), component.getLineWidth());
					segments.add(segment);
					fromX = transversalComponent.getFromX();
				}
			}
		if (segments.size() == 0)
			segments.add(component);
		else if (fromX < component.getToX())
			segments.add(new GridComponent("segment", fromX, component.getFromY(), component.getToX(), component.getToY(), component.getLineWidth()));
		return segments;
	}
}
