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
import org.junit.Ignore;
import org.junit.Test;

import edworld.pdfreader4humans.impl.MainBoxDetector;
import edworld.pdfreader4humans.impl.MainMarginDetector;
import edworld.pdfreader4humans.impl.MainPDFComponentLocator;

public class PDFReaderTest {
	private static final String UTF_8 = "UTF-8";
	private PDFReader reader;

	@Before
	public void setUp() throws IOException {
		reader = new PDFReader(getClass().getResource("/testcase1/input.pdf"), new MainPDFComponentLocator(), new MainBoxDetector(), new MainMarginDetector());
	}

	@Test
	public void getFirstLevelComponents() throws IOException {
		assertEquals(31, reader.getFirstLevelComponents(1).size());
	}

	@Test
	public void toXML() throws IOException {
		assertEquals(IOUtils.toString(getClass().getResource("/testcase1/output.xml"), UTF_8), reader.toXML());
	}

	@Test
	@Ignore
	public void toTextLines() throws IOException {
		InputStream input = getClass().getResourceAsStream("/testcase1/output.txt");
		try {
			assertEquals(text(IOUtils.readLines(input, UTF_8)), text(reader.toTextLines()));
		} finally {
			input.close();
		}
	}

	@Test
	public void createPageImageWithStructure() throws IOException {
		File outputFile = new File("target/outputWithStructure.png");
		ImageIO.write(reader.createPageImage(1, 3, Color.WHITE, Color.BLACK, true), "png", outputFile);
		assertImagesAreSimilar(getClass().getResourceAsStream("/testcase1/outputWithStructure.png"), ImageIO.read(outputFile));
	}

	@Test
	public void createPageImageWithoutStructure() throws IOException {
		File outputFile = new File("target/outputWithoutStructure.png");
		ImageIO.write(reader.createPageImage(1, 3, Color.BLACK, Color.WHITE, false), "png", outputFile);
		assertImagesAreSimilar(getClass().getResourceAsStream("/testcase1/outputWithoutStructure.png"), ImageIO.read(outputFile));
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
			for (int k = 0; k < Math.min(outputImage.getWidth(), outputImage.getHeight()); k++) {
				int expectedColor = expectedOutputImage.getRGB(k, k);
				int actualColor = outputImage.getRGB(k, k);
				if ((expectedColor ^ 0xFFFFFF) == actualColor)
					expectedColor ^= 0xFFFFFF;
				assertEquals("Color should be the same at (" + k + "," + k + ").", expectedColor, actualColor);
			}
		} finally {
			expectedOutputStream.close();
		}
	}
}
