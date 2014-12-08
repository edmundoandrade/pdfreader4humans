package edworld.pdfreader.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edworld.pdfreader.Component;
import edworld.pdfreader.MarginComponent;
import edworld.pdfreader.MarginDetector;

public class MarginDetectorImpl implements MarginDetector {
	@Override
	public List<MarginComponent> detectMargins(List<? extends Component> components) {
		float tolerance = 3;
		float frequencyLeft = 0;
		float frequencyRight = 0;
		float top = Float.POSITIVE_INFINITY;
		float bottom = Float.NEGATIVE_INFINITY;
		Map<Float, Integer> leftMap = new HashMap<Float, Integer>();
		Map<Float, Integer> rightMap = new HashMap<Float, Integer>();
		for (Component component : components) {
			top = Math.min(component.getFromY(), top);
			bottom = Math.max(component.getToY(), bottom);
			frequencyLeft = Math.max(updateLeftMap(component, tolerance, leftMap), frequencyLeft);
			frequencyRight = Math.max(updateRightMap(component, tolerance, rightMap), frequencyRight);
		}
		List<Float> leftMargins = collectMargins(frequencyLeft, leftMap);
		List<Float> rightMargins = collectMargins(frequencyRight, rightMap);
		List<MarginComponent> margins = new ArrayList<MarginComponent>();
		for (int i = 0; i < Math.min(leftMargins.size(), rightMargins.size()); i++)
			margins.add(new MarginComponent(leftMargins.get(i), top, rightMargins.get(i), bottom));
		return margins;
	}

	private List<Float> collectMargins(float frequency, Map<Float, Integer> map) {
		List<Float> margins = new ArrayList<Float>();
		for (float x : map.keySet())
			if (map.get(x) == frequency)
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
