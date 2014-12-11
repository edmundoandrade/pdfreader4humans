// This open source code is distributed without warranties according to the license published at http://www.apache.org/licenses/LICENSE-2.0
package edworld.pdfreader4humans;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import edworld.pdfreader4humans.impl.MainBoxDetector;
import edworld.pdfreader4humans.impl.MainMarginDetector;
import edworld.pdfreader4humans.impl.MainPDFGridLocator;
import edworld.pdfreader4humans.impl.MainPDFTextLocator;

public class PDFReaderTest {
	private PDFReader reader;
	private Map<String, Font> fonts = new HashMap<String, Font>();

	@Before
	public void setUp() throws IOException {
		reader = new PDFReader(getClass().getResource("/testcase1/input.pdf"), new MainPDFTextLocator(), new MainPDFGridLocator(), new MainBoxDetector(), new MainMarginDetector());
	}

	@Test
	public void getFirstLevelComponents() {
		List<Component> firstLevel = reader.getFirstLevelComponents();
		//
		try {
			PDDocument doc = PDDocument.load(getClass().getResource("/testcase1/input.pdf"));
			try {
				int scaling = 3;
				PDPage pageToDraw = (PDPage) doc.getDocumentCatalog().getAllPages().get(0);
				PDRectangle cropBox = pageToDraw.findCropBox();
				BufferedImage image = new BufferedImage(Math.round(cropBox.getWidth() * scaling), Math.round(cropBox.getHeight() * scaling), BufferedImage.TYPE_INT_ARGB);
				Graphics2D graphics = image.createGraphics();
				graphics.clearRect(0, 0, image.getWidth(), image.getHeight());
				graphics.scale(scaling, scaling);
				for (Component component : firstLevel)
					System.out.println(component);
				for (Component component : firstLevel)
					if (component.toString().startsWith("box")) {
						graphics.setColor(new Color(100, 100, 0));
						graphics.fillRect(Math.round(component.getFromX()), Math.round(component.getFromY()), Math.round(component.getWidth()), Math.round(component.getHeight()));
						graphics.setColor(Color.RED);
						graphics.drawRect(Math.round(component.getFromX()), Math.round(component.getFromY()), Math.round(component.getWidth()), Math.round(component.getHeight()));
						graphics.setColor(Color.WHITE);
					} else if (component.toString().startsWith("group")) {
						graphics.setColor(Color.CYAN);
						graphics.drawRect(Math.round(component.getFromX()), Math.round(component.getFromY()), Math.round(component.getWidth()), Math.round(component.getHeight()));
						graphics.setColor(Color.WHITE);
					}
				for (Component component : firstLevel)
					if (component.toString().startsWith("line"))
						graphics.drawLine(Math.round(component.getFromX()), Math.round(component.getFromY()), Math.round(component.getToX()), Math.round(component.getToY()));
					else if (component.toString().startsWith("rect"))
						graphics.drawRect(Math.round(component.getFromX()), Math.round(component.getFromY()), Math.round(component.getWidth()), Math.round(component.getHeight()));
				for (Component component : firstLevel)
					if (component.toString().startsWith("margin")) {
						graphics.setColor(Color.YELLOW);
						graphics.drawRect(Math.round(component.getFromX()), Math.round(component.getFromY()), Math.round(component.getWidth()), Math.round(component.getHeight()));
						graphics.setColor(Color.WHITE);
					}
				for (Component component : firstLevel)
					if (component instanceof TextComponent) {
						graphics.setFont(font((TextComponent) component));
						graphics.drawString(((TextComponent) component).getText(), component.getFromX(), component.getToY());
					}
				ImageIO.write(image, "png", new File("target/output.png"));
				graphics.dispose();
			} finally {
				doc.close();
			}
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
		//
		Assert.assertEquals(35, firstLevel.size());
	}

	private Font font(TextComponent component) {
		String key = component.getFontName() + ":" + component.getFontSize();
		Font font = fonts.get(key);
		if (font == null) {
			String name = component.getFontName().contains("Times") ? "TimesRoman" : "Dialog";
			int style = component.getFontName().contains("Bold") ? Font.BOLD : Font.PLAIN;
			font = new Font(name, style, (int) component.getFontSize());
			fonts.put(key, font);
		}
		return font;
	}
}
