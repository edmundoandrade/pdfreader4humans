package edworld.pdfreader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import edworld.pdfreader.impl.MarginDetectorImpl;

public class MarginDetectorTest {
	private MarginDetector detector;
	private List<GridComponent> testComponents;
	private List<MarginComponent> detected;
	int row;
	Float fromX, toX;
	Map<Integer, Float> fromY, toY;
	private int marginIndex, boxRow;
	private Float boxFromX, boxToX;
	private Float boxFromY, boxToY;

	@Before
	public void setUp() {
		detector = new MarginDetectorImpl();
		testComponents = new ArrayList<GridComponent>();
		resetGridData();
		marginIndex = 0;
		resetMarginData();
	}

	@Test
	public void detectMargins() {
		grd("    ─────    ");
		grd(" ─────       ");
		grd("      ─────  ");
		grd(" ─────────── ");
		grd(" ─────────── ");
		grd(" ─────       ");
		grd("      ─────  ");
		grd(" ────────── ");
		grd("   ───────── ");
		grd("   ───────── ");
		detected = detector.detectMargins(gridComponents());
		Assert.assertEquals(1, detected.size());
		mrg(" ┌─────────┐ ");
		mrg(" │         │ ");
		mrg(" │         │ ");
		mrg(" │         │ ");
		mrg(" │         │ ");
		mrg(" │         │ ");
		mrg(" │         │ ");
		mrg(" │         │ ");
		mrg(" │         │ ");
		mrg(" └─────────┘ ");
		assertMargin(detected);
	}

	private List<GridComponent> gridComponents() {
		for (Integer column : fromY.keySet())
			addVertical(column);
		resetGridData();
		return testComponents;
	}

	private void assertMargin(List<MarginComponent> detected) {
		Assert.assertEquals(new MarginComponent(boxFromX, boxFromY, boxToX, boxToY).toString(), detected.get(marginIndex).toString());
		marginIndex++;
		resetMarginData();
	}

	private void grd(String line) {
		int column = 0;
		for (Character character : line.toCharArray()) {
			if (isHorizontal(character)) {
				if (fromX == null)
					fromX = coordFromX(column);
				toX = coordToX(column);
			} else
				addHorizontal();
			if (isVertical(character)) {
				if (fromY.get(column) == null)
					fromY.put(column, coordFromY(row));
				toY.put(column, coordToY(row));
			} else
				addVertical(column);
			column++;
		}
		addHorizontal();
		row++;
	}

	private void mrg(String line) {
		int column = 0;
		for (Character character : line.toCharArray()) {
			if (isHorizontal(character)) {
				if (boxFromX == null)
					boxFromX = coordFromX(column);
				boxToX = coordToX(column);
			}
			if (isVertical(character)) {
				if (boxFromY == null)
					boxFromY = coordFromY(boxRow);
				boxToY = coordToY(boxRow);
			}
			column++;
		}
		boxRow++;
	}

	private void addHorizontal() {
		if (fromX != null) {
			testComponents.add(new GridComponent("rect", fromX, coordFromY(row), toX, coordToY(row), 1));
			fromX = null;
			toX = null;
		}
	}

	private void addVertical(int column) {
		if (fromY.get(column) != null) {
			testComponents.add(new GridComponent("rect", coordFromX(column), fromY.get(column), coordToX(column), toY.get(column), 1));
			fromY.put(column, null);
			toY.put(column, null);
		}
	}

	private boolean isHorizontal(Character character) {
		return "├┬┼┤└┴┌┬┐┘┴─".contains(character.toString());
	}

	private boolean isVertical(Character character) {
		return "├┬┼┤└┴┌┬┐┘┴│".contains(character.toString());
	}

	private Float coordFromX(int column) {
		return (float) (column * 2);
	}

	private Float coordToX(int column) {
		return coordFromX(column) + 1;
	}

	private float coordFromY(int row) {
		return (float) (row * 2);
	}

	private float coordToY(int row) {
		return coordFromY(row) + 1;
	}

	private void resetGridData() {
		row = 0;
		fromX = null;
		toX = null;
		fromY = new HashMap<Integer, Float>();
		toY = new HashMap<Integer, Float>();
	}

	private void resetMarginData() {
		boxRow = 0;
		boxFromX = null;
		boxToX = null;
		boxFromY = null;
		boxToY = null;
	}
}
