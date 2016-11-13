// This open source code is distributed without warranties according to the license published at http://www.apache.org/licenses/LICENSE-2.0
package edworld.pdfreader4humans;

import static java.lang.Math.max;
import static java.lang.System.getProperty;
import static org.apache.commons.io.IOUtils.readLines;
import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import edworld.pdfreader4humans.impl.MainBoxDetector;
import edworld.pdfreader4humans.impl.MainMarginDetector;
import edworld.pdfreader4humans.impl.MainPDFComponentLocator;

public class PDFReaderTest {
	private static final String UTF_8 = "UTF-8";
	private static PDFReader reader1, reader2, reader3, reader4, reader5, reader6, reader7, reader8;

	@BeforeClass
	public static void setUp() throws IOException {
		PDFComponentLocator locator = new MainPDFComponentLocator();
		BoxDetector boxDetector = new MainBoxDetector();
		MarginDetector marginDetector = new MainMarginDetector();
		reader1 = new PDFReader(PDFReaderTest.class.getResource("/testcase1/input.pdf"), locator, boxDetector,
				marginDetector);
		reader2 = new PDFReader(PDFReaderTest.class.getResource("/testcase2/input.pdf"), locator, boxDetector,
				marginDetector);
		reader3 = new PDFReader(PDFReaderTest.class.getResource("/testcase3/input.pdf"), locator, boxDetector,
				marginDetector);
		reader4 = new PDFReader(PDFReaderTest.class.getResource("/testcase4/input.pdf"), locator, boxDetector,
				marginDetector);
		reader5 = new PDFReader(PDFReaderTest.class.getResource("/testcase5/input.pdf"), locator, boxDetector,
				marginDetector);
		reader6 = new PDFReader(PDFReaderTest.class.getResource("/testcase6/input.pdf"), locator, boxDetector,
				marginDetector);
		reader7 = new PDFReader(PDFReaderTest.class.getResource("/testcase7/input.pdf"), locator, boxDetector,
				marginDetector);
		reader8 = new PDFReader(PDFReaderTest.class.getResource("/testcase8/input.pdf"), locator, boxDetector,
				marginDetector, 0.5F);
	}

	@Test
	public void getFirstLevelComponents() throws IOException {
		assertEquals(17, reader1.getFirstLevelComponents(1).size());
		assertEquals(8, reader2.getFirstLevelComponents(1).size());
		assertEquals(13, reader3.getFirstLevelComponents(1).size());
	}

	@Test
	public void toXML() throws IOException {
		assertEquals(text(readLinesFromResource("/testcase1/output.xml")), reader1.toXML());
		assertEquals(text(readLinesFromResource("/testcase4/output.xml")), reader4.toXML());
		assertEquals(text(readLinesFromResource("/testcase5/output.xml")), reader5.toXML());
		assertEquals(text(readLinesFromResource("/testcase6/output.xml")), reader6.toXML());
	}

	@Test
	public void toTextLines() throws IOException {
//		for (Component component : reader3.getFirstLevelComponents(1)) {
//			System.out.println(component.toString());
//			for (Component subComponent : component.getChildren()) {
//				System.out.println("\t" + subComponent);
//				for (Component nextSubComponent : subComponent.getChildren()) {
//					System.out.println("\t\t" + nextSubComponent);
//					for (Component next2SubComponent : nextSubComponent.getChildren())
//						System.out.println("\t\t\t" + next2SubComponent);
//				}
//			}
//		}
		
//margin :: 531.4939, 74.23602, 758.28345, 527.708
//		text :: 559.7438, 523.2607, 676.53735, 527.708, Times-Roman 7.0 :: "X - habitação de interesse social."

//rect :: 559.744, 544.96704, 612.36304, 545.46497, 0.51pt
//		text :: 559.744, 540.3726, 612.3625, 544.9674, Times-Bold 7.0 :: Razões do veto

//margin :: 531.494, 76.22803, 758.2808, 868.8181
//		group :: 531.494, 76.22803, 758.265, 96.071045
//			rect :: 531.494, 76.22803, 758.265, 96.071045, 0.51pt
//				text :: 578.133, 80.860725, 711.62476, 88.18329, JCLDHF+OttawaV 11.0 :: Presidência da República
//				rect :: 531.494, 93.16498, 758.265, 94.85895, 0.51pt
//		text :: 559.7918, 557.77936, 758.2725, 562.2267, Times-Roman 7.0 :: "Da forma como previsto, tal acréscimo de finalidade po-
		
		assertEquals(text(readLinesFromResource("/testcase1/output.txt")), text(reader1.toTextLines()));
		assertEquals(text(readLinesFromResource("/testcase2/output.txt")), text(reader2.toTextLines()));
		assertEquals(text(readLinesFromResource("/testcase8/output.txt")), text(reader8.toTextLines()));
	}

	@Test
	@Ignore
	public void toTextLinesInProgress() throws IOException {
		assertEquals(text(readLinesFromResource("/testcase3/output.txt")), text(reader3.toTextLines()));
		assertEquals(text(readLinesFromResource("/testcase7/output.txt")), text(reader7.toTextLines()));
	}

	@Test
	public void createPageImageWithStructure() throws IOException {
		File outputFile = new File("target/outputWithStructure1.png");
		ImageIO.write(reader1.createPageImage(1, 3, Color.WHITE, Color.BLACK, true), "png", outputFile);
		assertImagesAreSimilar(getClass().getResourceAsStream("/testcase1/outputWithStructure.png"),
				ImageIO.read(outputFile));
		outputFile = new File("target/outputWithStructure2.png");
		ImageIO.write(reader2.createPageImage(1, 3, Color.WHITE, Color.BLACK, true), "png", outputFile);
		assertImagesAreSimilar(getClass().getResourceAsStream("/testcase2/outputWithStructure.png"),
				ImageIO.read(outputFile));
		outputFile = new File("target/outputWithStructure3.png");
		ImageIO.write(reader3.createPageImage(1, 3, Color.WHITE, Color.BLACK, true), "png", outputFile);
		assertImagesAreSimilar(getClass().getResourceAsStream("/testcase3/outputWithStructure.png"),
				ImageIO.read(outputFile));
	}

	@Test
	public void createPageImageWithoutStructure() throws IOException {
		File outputFile = new File("target/outputWithoutStructure1.png");
		ImageIO.write(reader1.createPageImage(1, 3, Color.BLACK, Color.WHITE, false), "png", outputFile);
		assertImagesAreSimilar(getClass().getResourceAsStream("/testcase1/outputWithoutStructure.png"),
				ImageIO.read(outputFile));
		outputFile = new File("target/outputWithoutStructure2.png");
		ImageIO.write(reader2.createPageImage(1, 3, Color.BLACK, Color.WHITE, false), "png", outputFile);
		assertImagesAreSimilar(getClass().getResourceAsStream("/testcase2/outputWithoutStructure.png"),
				ImageIO.read(outputFile));
	}

	private String text(List<String> lines) {
		String text = "";
		for (String line : lines)
			text += line + getProperty("line.separator");
		return text;
	}

	private void assertImagesAreSimilar(InputStream expectedOutputStream, BufferedImage outputImage)
			throws IOException {
		try {
			BufferedImage expectedOutputImage = ImageIO.read(expectedOutputStream);
			assertEquals(expectedOutputImage.getType(), outputImage.getType());
			assertEquals(expectedOutputImage.getWidth(), outputImage.getWidth());
			assertEquals(expectedOutputImage.getHeight(), outputImage.getHeight());
			assertEquals(expectedOutputImage.getTransparency(), outputImage.getTransparency());
			for (int k = 0; k < max(outputImage.getWidth(), outputImage.getHeight()); k++) {
				int kX = k % outputImage.getWidth();
				int kY = k % outputImage.getHeight();
				int expectedColor = expectedOutputImage.getRGB(kX, kY);
				int actualColor = outputImage.getRGB(kX, kY);
				if ((expectedColor ^ 0xFFFFFF) == actualColor)
					expectedColor ^= 0xFFFFFF;
				assertEquals("Color should be the same at (" + k + "," + k + ").", expectedColor, actualColor);
			}
		} finally {
			expectedOutputStream.close();
		}
	}

	private List<String> readLinesFromResource(String resourceName) throws IOException {
		InputStream input = getClass().getResourceAsStream(resourceName);
		try {
			return readLines(input, UTF_8);
		} finally {
			input.close();
		}
	}
}
