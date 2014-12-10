// This open source code is distributed without warranties according to the license published at http://www.apache.org/licenses/LICENSE-2.0
package edworld.pdfreader.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edworld.pdfreader.Component;
import edworld.pdfreader.GridComponent;
import edworld.pdfreader.MarginComponent;
import edworld.pdfreader.MarginDetector;
import edworld.pdfreader.TextComponent;

public class MarginDetectorImpl implements MarginDetector {
	@Override
	public List<MarginComponent> detectMargins(List<? extends Component> components) {
		List<Component> sortedComponents = new ArrayList<Component>(components);
		Collections.sort(sortedComponents);
		List<MarginComponent> margins = new ArrayList<MarginComponent>();
		float tolerance = 3;
		for (Component region : detectRegions(sortedComponents)) {
			float frequencyLeft = 0;
			float frequencyRight = 0;
			float top = Float.POSITIVE_INFINITY;
			float bottom = Float.NEGATIVE_INFINITY;
			Map<Float, Integer> leftMap = new HashMap<Float, Integer>();
			Map<Float, Integer> rightMap = new HashMap<Float, Integer>();
			for (Component component : sortedComponents)
				if (region.contains(component) && component instanceof TextComponent) {
					top = Math.min(component.getFromY(), top);
					bottom = Math.max(component.getToY(), bottom);
					frequencyLeft = Math.max(updateLeftMap(component, tolerance, leftMap), frequencyLeft);
					frequencyRight = Math.max(updateRightMap(component, tolerance, rightMap), frequencyRight);
				}
			if (frequencyLeft > 4 && frequencyRight > 4) {
				List<Float> leftMargins = marginCandidates(leftMap);
				List<Float> rightMargins = marginCandidates(rightMap);
				for (int i = 0; i < Math.min(leftMargins.size(), rightMargins.size()); i++) {
					while (i + 1 < rightMargins.size() && rightMap.get(rightMargins.get(i)) < frequencyRight / 2)
						rightMargins.remove(i);
					while (i + 1 < leftMargins.size() && leftMap.get(leftMargins.get(i)) < frequencyLeft / 2)
						leftMargins.remove(i);
					if (i + 1 == leftMargins.size())
						while (i + 1 < rightMargins.size())
							rightMargins.remove(i);
					margins.add(new MarginComponent(leftMargins.get(i), top, rightMargins.get(i), bottom));
					while (i + 1 < leftMargins.size() && leftMargins.get(i + 1) <= rightMargins.get(i))
						leftMargins.remove(i + 1);
				}
			}
		}
		return margins;
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
		return component.getWidth() >= contentWidth / 3 && component instanceof GridComponent;
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

	private List<Float> marginCandidates(Map<Float, Integer> map) {
		List<Float> margins = new ArrayList<Float>();
		for (float x : map.keySet())
			margins.add(x);
		Collections.sort(margins);
		return margins;
	}

	private int updateLeftMap(Component component, float tolerance, Map<Float, Integer> leftMap) {
		for (float left : leftMap.keySet())
			if (Math.abs(left - component.getFromX()) <= tolerance)
				return updateLeftMap(left, component.getFromX(), leftMap);
		int frequency = 1;
		leftMap.put(component.getFromX(), frequency);
		return frequency;
	}

	private int updateLeftMap(float left, float fromX, Map<Float, Integer> leftMap) {
		int frequency = leftMap.get(left) + 1;
		if (left > fromX) {
			leftMap.put(fromX, frequency);
			leftMap.remove(left);
		} else
			leftMap.put(left, frequency);
		return frequency;
	}

	private int updateRightMap(Component component, float tolerance, Map<Float, Integer> rightMap) {
		for (float right : rightMap.keySet())
			if (Math.abs(right - component.getToX()) <= tolerance)
				return updateRightMap(right, component.getToX(), rightMap);
		int frequency = 1;
		rightMap.put(component.getToX(), frequency);
		return frequency;
	}

	private int updateRightMap(float right, float toX, Map<Float, Integer> rightMap) {
		int frequency = rightMap.get(right) + 1;
		if (right < toX) {
			rightMap.put(toX, frequency);
			rightMap.remove(right);
		} else
			rightMap.put(right, frequency);
		return frequency;
	}
}
