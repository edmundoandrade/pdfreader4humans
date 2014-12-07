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

import edworld.pdfreader.impl.BoxDetectorImpl;
import edworld.pdfreader.impl.PDFGridLocatorImpl;
import edworld.pdfreader.impl.PDFTextLocatorImpl;

public class PDFReaderTest {
	private PDFReader reader;

	@Before
	public void setUp() throws IOException {
		reader = new PDFReader(getClass().getResource("/testcase1/input.pdf"), new PDFTextLocatorImpl(), new PDFGridLocatorImpl(), new BoxDetectorImpl());
	}

	@Test
	public void getFirstLevelComponents() {
		List<Component> firstLevel = reader.getFirstLevelComponents();
		//
		try {
			PDDocument doc = PDDocument.load(getClass().getResource("/testcase1/input.pdf"));
			try {
				int scaling = 1;
				PDPage pageToDraw = (PDPage) doc.getDocumentCatalog().getAllPages().get(0);
				PDRectangle cropBox = pageToDraw.findCropBox();
				BufferedImage image = new BufferedImage(Math.round(cropBox.getWidth() * scaling), Math.round(cropBox.getHeight() * scaling), BufferedImage.TYPE_INT_ARGB);
				Graphics2D graphics = image.createGraphics();
				graphics.clearRect(0, 0, image.getWidth(), image.getHeight());
				graphics.scale(scaling, scaling);
				for (Component component : firstLevel)
					if (component.toString().startsWith("box")) {
						graphics.setColor(new Color(100, 100, 0));
						graphics.fillRect(Math.round(component.getFromX()), Math.round(component.getFromY()), Math.round(component.getWidth()), Math.round(component.getHeight()));
						graphics.setColor(Color.RED);
						graphics.drawRect(Math.round(component.getFromX()), Math.round(component.getFromY()), Math.round(component.getWidth()), Math.round(component.getHeight()));
						graphics.setColor(Color.WHITE);
					}
				for (Component component : firstLevel)
					if (component.toString().startsWith("line"))
						graphics.drawLine(Math.round(component.getFromX()), Math.round(component.getFromY()), Math.round(component.getToX()), Math.round(component.getToY()));
					else if (component.toString().startsWith("rect"))
						graphics.drawRect(Math.round(component.getFromX()), Math.round(component.getFromY()), Math.round(component.getWidth()), Math.round(component.getHeight()));
				ImageIO.write(image, "png", new File("target/saida.png"));
				graphics.dispose();
			} finally {
				doc.close();
			}
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
		//
		Assert.assertEquals(411, firstLevel.size());
	}
}
