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
		List<GridComponent> extendedComponents = extendConnectedComponents(components);
		List<GridComponent> boxes = new ArrayList<GridComponent>();
		List<GridComponent> verticalComponents = vertical(extendedComponents);
		List<GridComponent> horizontalConnectedSegments = horizontalConnectedSegments(horizontal(extendedComponents), verticalComponents);
		float borderWidth = maxBorderWidth(verticalComponents);
		for (GridComponent component1 : horizontalConnectedSegments)
			for (GridComponent component2 : horizontalConnectedSegments)
				if (nearestOnY(component1, component2, borderWidth)) {
					addBox(component1, component2, boxes);
					break;
				}
		Collections.sort(boxes);
		return boxes;
	}

	private boolean nearestOnY(Component component1, Component component2, float borderWidth) {
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
		Collections.sort(segments, orderByYX());
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
				if (nearestOnX(borderHeight, component1, component2)) {
					if (component1.getFromY() == component2.getFromY())
						addSegment(component1.getFromX(), component1.getFromY(), component2.getToX(), component1.getFromY(), component1.getLineWidth(), null, segments);
					if (component1.getToY() == component2.getToY())
						addSegment(component1.getFromX(), component1.getToY(), component2.getToX(), component1.getToY(), component1.getLineWidth(), null, segments);
					break;
				}
	}

	private boolean nearestOnX(float borderHeight, GridComponent component1, GridComponent component2) {
		return component1.getFromX() < component2.getFromX() && component1.getFromY() + borderHeight < component2.getToY()
				&& component2.getFromY() + borderHeight < component1.getToY();
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

	private List<GridComponent> extendConnectedComponents(List<GridComponent> gridComponents) {
		List<GridComponent> listNotExtended = new ArrayList<GridComponent>(gridComponents);
		List<GridComponent> list = new ArrayList<GridComponent>();
		for (GridComponent component1 : gridComponents) {
			GridComponent next = component1;
			List<GridComponent> horizontalExtension = new ArrayList<GridComponent>();
			for (GridComponent component2 : gridComponents)
				if (component1 != component2 && horizontallyExtendable(next, component2, gridComponents)) {
					horizontalExtension.add(component2);
					next = component2;
				}
			addExtendedComponent(component1, horizontalExtension, list, listNotExtended);
			next = component1;
			List<GridComponent> verticalExtension = new ArrayList<GridComponent>();
			for (GridComponent component2 : gridComponents)
				if (component1 != component2 && verticallyExtendable(next, component2, gridComponents)) {
					verticalExtension.add(component2);
					next = component2;
				}
			addExtendedComponent(component1, verticalExtension, list, listNotExtended);
		}
		list.addAll(listNotExtended);
		return list;
	}

	private void addExtendedComponent(GridComponent component1, List<GridComponent> extension, List<GridComponent> list, List<GridComponent> listNotExtended) {
		if (extension.size() == 0 || !listNotExtended.contains(component1))
			return;
		float fromX = component1.getFromX();
		float fromY = component1.getFromY();
		float toX = component1.getToX();
		float toY = component1.getToY();
		for (GridComponent component2 : extension) {
			if (!listNotExtended.contains(component2))
				return;
			fromX = Math.min(component2.getFromX(), fromX);
			fromY = Math.min(component2.getFromY(), fromY);
			toX = Math.max(component2.getToX(), toX);
			toY = Math.max(component2.getToY(), toY);
		}
		GridComponent extendedComponent = new GridComponent("extension", fromX, fromY, toX, toY, component1.getLineWidth());
		extendedComponent.addChild(component1);
		listNotExtended.remove(component1);
		for (GridComponent component2 : extension) {
			extendedComponent.addChild(component2);
			listNotExtended.remove(component2);
		}
		list.add(extendedComponent);
	}

	private boolean horizontallyExtendable(GridComponent component1, GridComponent component2, List<GridComponent> gridComponents) {
		if (component1.getFromY() == component2.getFromY() && component1.getToY() == component2.getToY() && !isVertical(component1) && !isVertical(component2)) {
			return component1.intersects(component2) || intersectsTransitively(component1, component2, gridComponents);
		}
		return false;
	}

	private boolean verticallyExtendable(GridComponent component1, GridComponent component2, List<GridComponent> gridComponents) {
		if (component1.getFromX() == component2.getFromX() && component1.getToX() == component2.getToX() && isVertical(component1) && isVertical(component2)) {
			return component1.intersects(component2) || intersectsTransitively(component1, component2, gridComponents);
		}
		return false;
	}

	private boolean isVertical(GridComponent component) {
		return component.getHeight() > component.getWidth();
	}

	private boolean intersectsTransitively(GridComponent component1, GridComponent component2, List<GridComponent> gridComponents) {
		for (GridComponent component : gridComponents)
			if (component != component1 && component != component2 && isVertical(component) == !isVertical(component1) && component1.intersects(component)
					&& component2.intersects(component)) {
				System.out.println(component1.toString());
				System.out.println(component.toString());
				System.out.println(component2.toString());
				System.out.println();
				return true;
			}
		return false;
	}
}
