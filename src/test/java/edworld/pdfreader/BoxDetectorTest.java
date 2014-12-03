package edworld.pdfreader;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import edworld.pdfreader.impl.BoxDetectorImpl;

public class BoxDetectorTest {
	private BoxDetector detector;
	private List<GridComponent> gridComponents;

	@Before
	public void setUp() {
		detector = new BoxDetectorImpl();
		gridComponents = new ArrayList<GridComponent>();
	}

	@Test
	public void detectNoBoxes() {
		gridComponents.add(new GridComponent("rect", 10, 11, 80, 12, 1));
		gridComponents.add(new GridComponent("rect", 40, 12, 41, 22, 1));
		Assert.assertTrue(detector.detectBoxes(gridComponents).isEmpty());
	}

	@Test
	public void detectColumnBoxes() {
		gridComponents.add(new GridComponent("rect", 10, 11, 80, 12, 1));
		gridComponents.add(new GridComponent("rect", 40, 12, 41, 22, 1));
		gridComponents.add(new GridComponent("rect", 10, 21, 80, 22, 1));
		List<GridComponent> detected = detector.detectBoxes(gridComponents);
		Assert.assertEquals(2, detected.size());
		Assert.assertEquals("box (10.0, 11.0, 41.0, 22.0, 1.0pt)", detected.get(0).toString());
		Assert.assertEquals("box (40.0, 11.0, 80.0, 22.0, 1.0pt)", detected.get(1).toString());
	}

	@Test
	public void detectRowBoxes() {
		gridComponents.add(new GridComponent("rect", 10, 11, 80, 12, 1));
		gridComponents.add(new GridComponent("rect", 10, 21, 80, 22, 1));
		gridComponents.add(new GridComponent("rect", 10, 45, 80, 46, 1));
		List<GridComponent> detected = detector.detectBoxes(gridComponents);
		Assert.assertEquals(2, detected.size());
		Assert.assertEquals("box (10.0, 11.0, 80.0, 22.0, 1.0pt)", detected.get(0).toString());
		Assert.assertEquals("box (10.0, 21.0, 80.0, 46.0, 1.0pt)", detected.get(1).toString());
	}

	@Test
	public void detectMixedBoxes() {
		gridComponents.add(new GridComponent("rect", 10, 11, 11, 45, 1));
		gridComponents.add(new GridComponent("rect", 10, 11, 80, 12, 1));
		gridComponents.add(new GridComponent("rect", 40, 12, 41, 45, 1));
		gridComponents.add(new GridComponent("rect", 40, 21, 80, 22, 1));
		gridComponents.add(new GridComponent("rect", 10, 45, 80, 46, 1));
		gridComponents.add(new GridComponent("rect", 79, 11, 80, 45, 1));
		List<GridComponent> detected = detector.detectBoxes(gridComponents);
		// Assert.assertEquals(3, detected.size());
		Assert.assertEquals("box (10.0, 11.0, 41.0, 46.0, 1.0pt)", detected.get(0).toString());
		Assert.assertEquals("box (40.0, 11.0, 80.0, 22.0, 1.0pt)", detected.get(1).toString());
		Assert.assertEquals("box (40.0, 21.0, 80.0, 46.0, 1.0pt)", detected.get(2).toString());
	}
}
