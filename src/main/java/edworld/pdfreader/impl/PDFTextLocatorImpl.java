// This open source code is distributed without warranties according to the license published at http://www.apache.org/licenses/LICENSE-2.0
package edworld.pdfreader.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.TextPosition;

import edworld.pdfreader.PDFTextLocator;
import edworld.pdfreader.TextComponent;

public class PDFTextLocatorImpl implements PDFTextLocator {
	public TextComponent[] locateTextComponents(final PDPage page) throws IOException {
		return new PDFTextStripper() {
			private ArrayList<TextComponent> list = new ArrayList<TextComponent>();

			public TextComponent[] locateTextComponents() throws IOException {
				PDStream contents = page.getContents();
				setStartPage(getCurrentPageNo());
				setEndPage(getCurrentPageNo());
				setSortByPosition(true);
				if (contents != null) {
					output = new StringWriter();
					processPage(page, contents.getStream());
				}
				return list.toArray(new TextComponent[0]);
			}

			@Override
			protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
				float fromX = Float.MAX_VALUE, toX = Float.MIN_VALUE, fromY = Float.MAX_VALUE, toY = Float.MIN_VALUE, fontSize = -1;
				String fontName = "";
				for (TextPosition textPosition : textPositions) {
					float x1 = textPosition.getX();
					float y1 = textPosition.getY();
					if (x1 <= fromX) {
						fromX = x1;
						fromY = y1;
						fontName = textPosition.getFont().getBaseFont();
						fontSize = textPosition.getFontSizeInPt();
					}
					toX = Math.max(x1 + textPosition.getWidth(), toX);
					toY = Math.max(y1 + textPosition.getHeight(), toY);
				}
				list.add(new TextComponent(text, fromX, fromY, toX, toY, fontName, fontSize));
			}
		}.locateTextComponents();
	}
}
