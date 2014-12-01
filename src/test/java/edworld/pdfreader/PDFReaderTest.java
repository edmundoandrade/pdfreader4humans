// This open source code is distributed without warranties according to the license published at http://www.apache.org/licenses/LICENSE-2.0
package edworld.pdfreader;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import edworld.pdfreader.impl.PDFGridLocatorImpl;
import edworld.pdfreader.impl.PDFTextLocatorImpl;

public class PDFReaderTest {
	private PDFReader reader;

	@Before
	public void setUp() throws IOException {
		reader = new PDFReader(getClass().getResource("/testcase1/input.pdf"), new PDFTextLocatorImpl(), new PDFGridLocatorImpl());
	}

	@Test
	public void getFirstLevelComponents() {
		List<Component> firstLevel = reader.getFirstLevelComponents();
		//
		try {
			PDDocument doc = PDDocument.load(getClass().getResource("/testcase1/input.pdf"));
			try {
				int scaling = 4;
				PDPage pageToDraw = (PDPage) doc.getDocumentCatalog().getAllPages().get(0);
				PDRectangle cropBox = pageToDraw.findCropBox();
				BufferedImage image = new BufferedImage((int) cropBox.getWidth() * scaling, (int) cropBox.getHeight() * scaling, BufferedImage.TYPE_INT_ARGB);
				Graphics2D graphics = image.createGraphics();
				graphics.clearRect(0, 0, image.getWidth(), image.getHeight());
				graphics.scale(scaling, scaling);
				for (Component component : firstLevel) {
					if (component.toString().startsWith("line")) {
						graphics.drawLine((int) component.getFromX(), (int) component.getFromY(), (int) component.getToX(), (int) component.getToY());
					} else if (component.toString().startsWith("rect")) {
						graphics.drawRect((int) component.getFromX(), (int) component.getFromY(), (int) (component.getToX() - component.getFromX()),
								(int) (component.getToY() - component.getFromY()));
					}
				}
				for (Component component : firstLevel) {
					if (component.toString().startsWith("group")) {
						graphics.setColor(Color.YELLOW);
						graphics.drawRect((int) component.getFromX(), (int) component.getFromY(), (int) (component.getToX() - component.getFromX()),
								(int) (component.getToY() - component.getFromY()));
						graphics.setColor(Color.WHITE);
					}
				}
				ImageIO.write(image, "png", new File("/user/saida.png"));
				graphics.dispose();
			} finally {
				doc.close();
			}
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
		//
		Assert.assertEquals(610, firstLevel.size());
	}
}
