// This open source code is distributed without warranties according to the license published at http://www.apache.org/licenses/LICENSE-2.0
package edworld.pdfreader4humans.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edworld.pdfreader4humans.Component;
import edworld.pdfreader4humans.MarginComponent;
import edworld.pdfreader4humans.MarginDetector;
import edworld.pdfreader4humans.TextComponent;

public class MainMarginDetector implements MarginDetector {
	@Override
	public List<MarginComponent> detectMargins(List<? extends Component> components) {
		List<Component> sortedComponents = new ArrayList<Component>(components);
		Collections.sort(sortedComponents);
		List<MarginComponent> margins = new ArrayList<MarginComponent>();
		Map<Component, MarginComponent> map = new HashMap<Component, MarginComponent>();
		for (Component component : sortedComponents)
			if (map.get(component) == null && component instanceof TextComponent)
				map.put(component, provideMarginForComponent((TextComponent) component, sortedComponents, margins, map));
		joinConnectedMargins(margins, map);
		for (Component component : map.keySet())
			if (map.get(component).getArea() == component.getArea())
				margins.remove(map.get(component));
		Collections.sort(margins);
		return margins;
	}

	private MarginComponent provideMarginForComponent(TextComponent component, List<Component> components, List<MarginComponent> margins, Map<Component, MarginComponent> map) {
		for (MarginComponent margin : margins) {
			Component nextLowerComponent = margin.nextLowerHorizontalComponent(margin.getToX(), margin.getFromX(), components);
			if (nextLowerComponent.underlineOf(margin))
				nextLowerComponent = nextLowerComponent.nextLowerHorizontalComponent(margin.getToX(), margin.getFromX(), components);
			if (component == nextLowerComponent) {
				MarginComponent extendedMargin = margin.extended(component);
				margins.remove(margin);
				margins.add(extendedMargin);
				updateMap(map, margin, extendedMargin);
				return extendedMargin;
			}
		}
		MarginComponent margin = new MarginComponent(component.getFromX(), component.getFromY(), component.getToX(), component.getToY());
		margins.add(margin);
		return margin;
	}

	private void joinConnectedMargins(List<MarginComponent> margins, Map<Component, MarginComponent> map) {
		for (MarginComponent marginA : margins)
			for (MarginComponent marginB : margins)
				if (marginA.intersects(marginB)) {
					MarginComponent extendedMargin = marginA.extended(marginB);
					margins.remove(marginA);
					margins.remove(marginB);
					margins.add(extendedMargin);
					updateMap(map, marginA, extendedMargin);
					updateMap(map, marginB, extendedMargin);
					joinConnectedMargins(margins, map);
					return;
				}
	}

	private void updateMap(Map<Component, MarginComponent> map, MarginComponent fromMargin, MarginComponent toMargin) {
		for (Component component : map.keySet())
			if (map.get(component) == fromMargin)
				map.put(component, toMargin);
	}
}
