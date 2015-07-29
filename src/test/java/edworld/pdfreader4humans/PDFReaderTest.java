// This open source code is distributed without warranties according to the license published at http://www.apache.org/licenses/LICENSE-2.0
package edworld.pdfreader4humans;

import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import edworld.pdfreader4humans.impl.MainBoxDetector;
import edworld.pdfreader4humans.impl.MainMarginDetector;
import edworld.pdfreader4humans.impl.MainPDFComponentLocator;

public class PDFReaderTest {
	private static final String UTF_8 = "UTF-8";
	private PDFReader reader1, reader2;

	@Before
	public void setUp() throws IOException {
		reader1 = new PDFReader(getClass().getResource("/testcase1/input.pdf"), new MainPDFComponentLocator(), new MainBoxDetector(), new MainMarginDetector());
		reader2 = new PDFReader(getClass().getResource("/testcase2/input.pdf"), new MainPDFComponentLocator(), new MainBoxDetector(), new MainMarginDetector());
	}

	@Test
	public void getFirstLevelComponents() throws IOException {
		assertEquals(17, reader1.getFirstLevelComponents(1).size());
		assertEquals(13, reader2.getFirstLevelComponents(1).size());
	}

	@Test
	public void toXML() throws IOException {
		assertEquals(IOUtils.toString(getClass().getResource("/testcase1/output.xml"), UTF_8), reader1.toXML());
	}

	@Test
	public void toTextLines() throws IOException {
		InputStream input = getClass().getResourceAsStream("/testcase1/output.txt");
		try {
			assertEquals(text(IOUtils.readLines(input, UTF_8)), text(reader1.toTextLines()));
		} finally {
			input.close();
		}
		input = getClass().getResourceAsStream("/testcase2/output.txt");
		try {
			assertEquals(text(IOUtils.readLines(input, UTF_8)), text(reader2.toTextLines()));
		} finally {
			input.close();
		}
	}

	@Test
	public void createPageImageWithStructure() throws IOException {
		File outputFile = new File("target/outputWithStructure1.png");
		ImageIO.write(reader1.createPageImage(1, 3, Color.WHITE, Color.BLACK, true), "png", outputFile);
		assertImagesAreSimilar(getClass().getResourceAsStream("/testcase1/outputWithStructure.png"), ImageIO.read(outputFile));
		outputFile = new File("target/outputWithStructure2.png");
		ImageIO.write(reader2.createPageImage(1, 3, Color.WHITE, Color.BLACK, true), "png", outputFile);
		assertImagesAreSimilar(getClass().getResourceAsStream("/testcase2/outputWithStructure.png"), ImageIO.read(outputFile));
	}

	@Test
	public void createPageImageWithoutStructure() throws IOException {
		File outputFile = new File("target/outputWithoutStructure1.png");
		ImageIO.write(reader1.createPageImage(1, 3, Color.BLACK, Color.WHITE, false), "png", outputFile);
		assertImagesAreSimilar(getClass().getResourceAsStream("/testcase1/outputWithoutStructure.png"), ImageIO.read(outputFile));
		outputFile = new File("target/outputWithoutStructure2.png");
		ImageIO.write(reader2.createPageImage(1, 3, Color.BLACK, Color.WHITE, false), "png", outputFile);
		assertImagesAreSimilar(getClass().getResourceAsStream("/testcase2/outputWithoutStructure.png"), ImageIO.read(outputFile));
	}

	private String text(List<String> lines) {
		String text = "";
		for (String line : lines)
			text += line + System.getProperty("line.separator");
		return text;
	}

	private void assertImagesAreSimilar(InputStream expectedOutputStream, BufferedImage outputImage) throws IOException {
		try {
			BufferedImage expectedOutputImage = ImageIO.read(expectedOutputStream);
			assertEquals(expectedOutputImage.getType(), outputImage.getType());
			assertEquals(expectedOutputImage.getWidth(), outputImage.getWidth());
			assertEquals(expectedOutputImage.getHeight(), outputImage.getHeight());
			assertEquals(expectedOutputImage.getTransparency(), outputImage.getTransparency());
			for (int k = 0; k < Math.max(outputImage.getWidth(), outputImage.getHeight()); k++) {
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
}
