package edworld.pdfreader4humans;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import edworld.pdfreader4humans.impl.MainBoxDetector;

public class BoxDetectorTest {
	private static final String BOTTOM = "bottom";
	private static final String RIGHT = "right";
	private static final String TOP = "top";
	private static final String LEFT = "left";
	private BoxDetector detector;
	private List<GridComponent> testComponents;
	private List<BoxComponent> detected;
	int row;
	Float fromX, toX;
	Map<Integer, Float> fromY, toY;
	private int boxIndex, boxRow;
	private Float boxFromX, boxToX;
	private Float boxFromY, boxToY;

	@Before
	public void setUp() {
		detector = new MainBoxDetector();
		testComponents = new ArrayList<GridComponent>();
		resetGridData();
		boxIndex = 0;
		resetBoxData();
	}

	@Test
	public void detectColumnBoxes() {
		grd(" ─────┬───── ");
		grd("      │      ");
		grd(" ─────┘      ");
		detected = detector.detectBoxes(gridComponents());
		Assert.assertEquals(2, detected.size());
		box(" ┌────┐      ");
		box(" │    │      ");
		box(" └────┘      ");
		assertBox(detected, TOP, RIGHT, BOTTOM);
		box("      ┌────┐ ");
		box("      │    │ ");
		box("      └────┘ ");
		assertBox(detected, LEFT, TOP);
	}

	@Test
	public void detectRowBoxes() {
		grd(" ┌────────── ");
		grd(" │           ");
		grd(" └─────────┐ ");
		grd("           │ ");
		grd(" ──────────┘ ");
		detected = detector.detectBoxes(gridComponents());
		Assert.assertEquals(2, detected.size());
		box(" ┌─────────┐ ");
		box(" │         │ ");
		box(" └─────────┘ ");
		box("             ");
		box("             ");
		assertBox(detected, LEFT, TOP, BOTTOM);
		box("             ");
		box("             ");
		box(" ┌─────────┐ ");
		box(" │         │ ");
		box(" └─────────┘ ");
		assertBox(detected, TOP, RIGHT, BOTTOM);
	}

	@Test
	public void detectBoxesWithRowSpan() {
		grd(" ┌────┬────┐ ");
		grd(" │    │    │ ");
		grd(" │    ├────┤ ");
		grd(" │    │    │ ");
		grd(" └────┴────┘ ");
		detected = detector.detectBoxes(gridComponents());
		assertEquals(3, detected.size());
		box(" ┌────┐      ");
		box(" │    │      ");
		box(" │    │      ");
		box(" │    │      ");
		box(" └────┘      ");
		assertBox(detected);
		box("      ┌────┐ ");
		box("      │    │ ");
		box("      └────┘ ");
		box("             ");
		box("             ");
		assertBox(detected);
		box("             ");
		box("             ");
		box("      ┌────┐ ");
		box("      │    │ ");
		box("      └────┘ ");
		assertBox(detected);
	}

	@Test
	public void detectBoxesWithColSpan() {
		grd(" ┌─────────┐ ");
		grd(" ├────┬────┤ ");
		grd(" │    │    │ ");
		grd(" └────┴────┘ ");
		detected = detector.detectBoxes(gridComponents());
		Assert.assertEquals(3, detected.size());
		box(" ┌─────────┐ ");
		box(" └─────────┘ ");
		box("             ");
		box("             ");
		assertBox(detected);
		box("             ");
		box(" ┌────┐      ");
		box(" │    │      ");
		box(" └────┘      ");
		assertBox(detected);
		box("             ");
		box("      ┌────┐ ");
		box("      │    │ ");
		box("      └────┘ ");
		assertBox(detected);
	}

	@Test
	public void detectUncoveredBoxes() {
		grd(" │         │ ");
		grd(" ├─────────┤ ");
		grd(" ├─────────┤ ");
		grd(" │         │ ");
		detected = detector.detectBoxes(gridComponents());
		Assert.assertEquals(3, detected.size());
		box(" ┌─────────┐ ");
		box(" └─────────┘ ");
		box("             ");
		box("             ");
		assertBox(detected, LEFT, RIGHT, BOTTOM);
		box("             ");
		box(" ┌─────────┐ ");
		box(" └─────────┘ ");
		box("             ");
		assertBox(detected);
		box("             ");
		box("             ");
		box(" ┌─────────┐ ");
		box(" └─────────┘ ");
		assertBox(detected, LEFT, TOP, RIGHT);
	}

	@Test
	public void ignoreUnconnectedComponents() {
		grd(" ─────────── ");
		grd(" ─────────── ");
		grd(" ┌─────────┐ ");
		grd(" │    ──   │ ");
		grd(" └─────────┘ ");
		detected = detector.detectBoxes(gridComponents());
		Assert.assertEquals(1, detected.size());
		box("             ");
		box("             ");
		box(" ┌─────────┐ ");
		box(" │         │ ");
		box(" └─────────┘ ");
		assertBox(detected);
	}

	private List<GridComponent> gridComponents() {
		for (Integer column : fromY.keySet())
			addVertical(column);
		resetGridData();
		return testComponents;
	}

	private void assertBox(List<BoxComponent> detected) {
		assertBox(detected, LEFT, TOP, RIGHT, BOTTOM);
	}

	private void assertBox(List<BoxComponent> detected, String... borders) {
		List<String> list = Arrays.asList(borders);
		boolean borderLeft = list.contains(LEFT);
		boolean borderTop = list.contains(TOP);
		boolean borderRight = list.contains(RIGHT);
		boolean borderBottom = list.contains(BOTTOM);
		Assert.assertEquals(new BoxComponent(boxFromX, boxFromY, boxToX, boxToY, 1, borderLeft, borderTop, borderRight, borderBottom).toString(), detected.get(boxIndex).toString());
		boxIndex++;
		resetBoxData();
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

	private void box(String line) {
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

	private void resetBoxData() {
		boxRow = 0;
		boxFromX = null;
		boxToX = null;
		boxFromY = null;
		boxToY = null;
	}
}
