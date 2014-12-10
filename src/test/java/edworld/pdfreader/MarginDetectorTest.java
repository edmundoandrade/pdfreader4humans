// This open source code is distributed without warranties according to the license published at http://www.apache.org/licenses/LICENSE-2.0
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
	private static final String LINE = "line";
	private static final String TEXT = "text";
	private MarginDetector detector;
	private List<Component> testComponents;
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
		testComponents = new ArrayList<Component>();
		resetGridData();
		marginIndex = 0;
		resetMarginData();
	}

	@Test
	public void detecSingleColumntMargins() {
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

	@Test
	public void detecMultipleColumnstMargins() {
		grd("    ─────      ───      ");
		grd(" ─────        ───────── ");
		grd("      ─────   ──────    ");
		grd(" ───────────    ──────  ");
		grd(" ───────────  ───────── ");
		grd(" ─────        ────────  ");
		grd("      ─────   ───────── ");
		grd(" ──────────   ────────  ");
		grd("   ─────────  ──────    ");
		grd("   ─────────    ─────   ");
		detected = detector.detectMargins(gridComponents());
		Assert.assertEquals(2, detected.size());
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
		mrg("              ┌───────┐ ");
		mrg("              │       │ ");
		mrg("              │       │ ");
		mrg("              │       │ ");
		mrg("              │       │ ");
		mrg("              │       │ ");
		mrg("              │       │ ");
		mrg("              │       │ ");
		mrg("              │       │ ");
		mrg("              └───────┘ ");
		assertMargin(detected);
	}

	@Test
	public void detecMarginsInsideRegions() {
		grd(" ────────────────────── ", LINE);
		grd("    ─────      ───      ");
		grd(" ───────────  ────────  ");
		grd(" ───────────  ───────── ");
		grd(" ───────────  ───────── ");
		grd(" ───────────  ───────── ");
		grd(" ───────      ───────── ");
		grd(" ───────────  ───────── ");
		grd(" ────────────────────── ", LINE);
		detected = detector.detectMargins(gridComponents());
		Assert.assertEquals(2, detected.size());
		mrg("             ");
		mrg(" ┌─────────┐ ");
		mrg(" │         │ ");
		mrg(" │         │ ");
		mrg(" │         │ ");
		mrg(" │         │ ");
		mrg(" │         │ ");
		mrg(" └─────────┘ ");
		mrg("             ");
		assertMargin(detected);
		mrg("                       ");
		mrg("              ┌───────┐ ");
		mrg("              │       │ ");
		mrg("              │       │ ");
		mrg("              │       │ ");
		mrg("              │       │ ");
		mrg("              │       │ ");
		mrg("              └───────┘ ");
		mrg("                        ");
		assertMargin(detected);
	}

	private List<Component> gridComponents() {
		for (Integer column : fromY.keySet())
			addVertical(column, TEXT);
		resetGridData();
		return testComponents;
	}

	private void assertMargin(List<MarginComponent> detected) {
		Assert.assertEquals(new MarginComponent(boxFromX, boxFromY, boxToX, boxToY).toString(), detected.get(marginIndex).toString());
		marginIndex++;
		resetMarginData();
	}

	private void grd(String line) {
		grd(line, TEXT);
	}

	private void grd(String line, String type) {
		int column = 0;
		for (Character character : line.toCharArray()) {
			if (isHorizontal(character)) {
				if (fromX == null)
					fromX = coordFromX(column);
				toX = coordToX(column);
			} else
				addHorizontal(type);
			if (isVertical(character)) {
				if (fromY.get(column) == null)
					fromY.put(column, coordFromY(row));
				toY.put(column, coordToY(row));
			} else
				addVertical(column, type);
			column++;
		}
		addHorizontal(type);
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

	private void addHorizontal(String type) {
		if (fromX != null) {
			if (type.equals(TEXT))
				testComponents.add(new TextComponent("", fromX, coordFromY(row), toX, coordToY(row), "", 1));
			else
				testComponents.add(new GridComponent(type, fromX, coordFromY(row), toX, coordToY(row), 1));
			fromX = null;
			toX = null;
		}
	}

	private void addVertical(int column, String type) {
		if (fromY.get(column) != null) {
			if (type.equals(TEXT))
				testComponents.add(new TextComponent("", coordFromX(column), fromY.get(column), coordToX(column), toY.get(column), "", 1));
			else
				testComponents.add(new GridComponent(type, coordFromX(column), fromY.get(column), coordToX(column), toY.get(column), 1));
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
