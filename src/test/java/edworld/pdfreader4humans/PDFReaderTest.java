// This open source code is distributed without warranties according to the license published at http://www.apache.org/licenses/LICENSE-2.0
package edworld.pdfreader4humans;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import edworld.pdfreader4humans.impl.MainBoxDetector;
import edworld.pdfreader4humans.impl.MainMarginDetector;
import edworld.pdfreader4humans.impl.MainPDFComponentLocator;

public class PDFReaderTest {
	private PDFReader reader;

	@Before
	public void setUp() throws IOException {
		reader = new PDFReader(getClass().getResource("/testcase1/input.pdf"), new MainPDFComponentLocator(), new MainBoxDetector(), new MainMarginDetector());
	}

	@Test
	public void getFirstLevelComponents() throws IOException {
		Assert.assertEquals(31, reader.getFirstLevelComponents(1).size());
	}

	@Test
	@Ignore
	public void toXML() throws IOException {
		Assert.assertEquals(IOUtils.toString(getClass().getResource("/testcase1/output.xml")), reader.toXML());
	}

	@Test
	public void createPageImageWithStructure() throws IOException {
		File outputFile = new File("target/outputWithStructure.png");
		ImageIO.write(reader.createPageImage(1, 3, Color.WHITE, Color.BLACK, true), "png", outputFile);
		assertImageEquals(getClass().getResourceAsStream("/testcase1/outputWithStructure.png"), new FileInputStream(outputFile));
	}

	@Test
	public void createPageImageWithoutStructure() throws IOException {
		File outputFile = new File("target/outputWithoutStructure.png");
		ImageIO.write(reader.createPageImage(1, 3, Color.BLACK, Color.WHITE, false), "png", outputFile);
		assertImageEquals(getClass().getResourceAsStream("/testcase1/outputWithoutStructure.png"), new FileInputStream(outputFile));
	}

	private void assertImageEquals(InputStream expectedOutput, InputStream output) throws IOException {
		try {
			Assert.assertArrayEquals(IOUtils.toByteArray(expectedOutput), IOUtils.toByteArray(output));
		} finally {
			expectedOutput.close();
			output.close();
		}
	}
}
