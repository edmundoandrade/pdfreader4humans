// This open source code is distributed without warranties according to the license published at http://www.apache.org/licenses/LICENSE-2.0
package edworld.pdfreader4humans.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edworld.pdfreader4humans.Component;
import edworld.pdfreader4humans.GridComponent;
import edworld.pdfreader4humans.GroupComponent;
import edworld.pdfreader4humans.MarginComponent;
import edworld.pdfreader4humans.MarginDetector;
import edworld.pdfreader4humans.TextComponent;

public class MainMarginDetector implements MarginDetector {
	@Override
	public List<MarginComponent> detectMargins(List<? extends Component> components) {
		List<Component> sortedComponents = new ArrayList<Component>(components);
		Collections.sort(sortedComponents);
		List<MarginComponent> margins = new ArrayList<MarginComponent>();
		for (Component region : detectRegions(sortedComponents)) {
			List<MarginComponent> regionMargins = new ArrayList<MarginComponent>();
			for (Component component : sortedComponents)
				if (region.contains(component) && component instanceof TextComponent)
					updateMargins(component, regionMargins);
			margins.addAll(regionMargins);
		}
		return margins;
	}

	private void updateMargins(Component component, List<MarginComponent> margins) {
		List<MarginComponent> marginsToBeMerged = new ArrayList<MarginComponent>();
		for (MarginComponent margin : margins)
			if (margin.contains(component))
				return;
			else if (margin.intersectsHorizontally(component))
				marginsToBeMerged.add(margin);
		if (marginsToBeMerged.isEmpty()) {
			margins.add(new MarginComponent(component.getFromX(), component.getFromY(), component.getToX(), component.getToY()));
			return;
		}
		float fromX = component.getFromX();
		float fromY = component.getFromY();
		float toX = component.getToX();
		float toY = component.getToY();
		for (MarginComponent margin : marginsToBeMerged) {
			fromX = Math.min(margin.getFromX(), fromX);
			fromY = Math.min(margin.getFromY(), fromY);
			toX = Math.max(margin.getToX(), toX);
			toY = Math.max(margin.getToY(), toY);
			margins.remove(margin);
		}
		margins.add(new MarginComponent(fromX, fromY, toX, toY));
	}

	private List<Component> detectRegions(List<? extends Component> components) {
		List<Component> regions = new ArrayList<Component>();
		float contentWidth = calculateContentWidth(components);
		float fromY = 0;
		for (Component component : components)
			if (regionSeparator(component, contentWidth)) {
				regions.add(new MarginComponent(Float.NEGATIVE_INFINITY, fromY, Float.POSITIVE_INFINITY, component.getFromY()));
				fromY = component.getToY();
			}
		regions.add(new MarginComponent(Float.NEGATIVE_INFINITY, fromY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY));
		return regions;
	}

	private boolean regionSeparator(Component component, float contentWidth) {
		return component.getWidth() > contentWidth / 2 && (component instanceof GridComponent || component instanceof GroupComponent);
	}

	private float calculateContentWidth(List<? extends Component> components) {
		float left = Float.POSITIVE_INFINITY;
		float right = Float.NEGATIVE_INFINITY;
		for (Component component : components) {
			left = Math.min(component.getFromX(), left);
			right = Math.max(component.getToX(), right);
		}
		return right - left;
	}
}
