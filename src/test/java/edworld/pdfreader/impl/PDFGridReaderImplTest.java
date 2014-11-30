// This open source code is distributed without warranties, following the license published at http://www.apache.org/licenses/LICENSE-2.0
package edworld.pdfreader.impl;

import static org.junit.Assert.assertEquals;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edworld.pdfreader.GridComponent;
import edworld.pdfreader.PDFGridReader;

public class PDFGridReaderImplTest {
	private PDDocument doc;
	private PDFGridReader reader;

	@Before
	public void setUp() throws Exception {
		doc = PDDocument.load(getClass().getResource("/testcase1/input.pdf"));
		PDPage page1 = (PDPage) doc.getDocumentCatalog().getAllPages().get(0);
		reader = new PDFGridReaderImpl(page1);
	}

	@Test
	public void locateGridComponents() throws IOException {
		GridComponent[] components = reader.locateGridComponents();
		assertEquals(173, components.length);
		assertEquals("rect (50.406, 69.08, 758.273, 34.147, 0.0pt)", components[0].toString());
		assertEquals("rect (49.518, 75.116, 49.717, 83.987, 0.51pt)", components[1].toString());
		assertEquals("rect (101.254, 75.116, 101.453, 83.987, 0.51pt)", components[2].toString());
		assertEquals("rect (273.328, 75.116, 273.527, 83.987, 0.51pt)", components[3].toString());
		assertEquals("line (49.607, 218.596, 758.265, 218.596, 1.08pt)", components[46].toString());
		assertEquals("line (49.607, 585.267, 517.321, 585.267, 1.08pt)", components[47].toString());
		assertEquals("line (49.607, 873.311, 758.273, 873.311, 0.51pt)", components[172].toString());
		//
		int scaling = 4;
		PDPage pageToDraw = (PDPage) doc.getDocumentCatalog().getAllPages().get(0);
		PDRectangle cropBox = pageToDraw.findCropBox();
		BufferedImage image = new BufferedImage((int) cropBox.getWidth() * scaling, (int) cropBox.getHeight() * scaling, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		graphics.clearRect(0, 0, image.getWidth(), image.getHeight());
		graphics.scale(scaling, scaling);
		for (GridComponent component : components) {
			if (component.toString().startsWith("line")) {
				graphics.drawLine((int) component.getFromX(), (int) component.getFromY(), (int) component.getToX(), (int) component.getToY());
			} else if (component.toString().startsWith("rect")) {
				graphics.drawRect((int) Math.min(component.getFromX(), component.getToX()), (int) Math.min(component.getFromY(), component.getToY()),
						(int) Math.abs(component.getToX() - component.getFromX()), (int) Math.abs(component.getToY() - component.getFromY()));
			}
		}
		ImageIO.write(image, "png", new File("/user/saida.png"));
		graphics.dispose();
	}

	@After
	public void tearDown() throws IOException {
		doc.close();
	}
}
