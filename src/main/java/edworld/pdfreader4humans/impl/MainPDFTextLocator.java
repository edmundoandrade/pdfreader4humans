// This open source code is distributed without warranties according to the license published at http://www.apache.org/licenses/LICENSE-2.0
package edworld.pdfreader4humans.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.TextPosition;

import edworld.pdfreader4humans.PDFTextLocator;
import edworld.pdfreader4humans.TextComponent;

public class MainPDFTextLocator implements PDFTextLocator {
	public List<TextComponent> locateTextComponents(final PDPage page) throws IOException {
		return new PDFTextStripper() {
			private ArrayList<TextComponent> list;

			public List<TextComponent> locateTextComponents() throws IOException {
				list = new ArrayList<TextComponent>();
				PDStream contents = page.getContents();
				setStartPage(getCurrentPageNo());
				setEndPage(getCurrentPageNo());
				setSortByPosition(false);
				if (contents != null) {
					output = new StringWriter();
					processPage(page, contents.getStream());
				}
				Collections.sort(list);
				return list;
			}

			@Override
			protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
				float fromX = Float.POSITIVE_INFINITY;
				float fromY = Float.POSITIVE_INFINITY;
				float toX = Float.NEGATIVE_INFINITY;
				float toY = Float.NEGATIVE_INFINITY;
				float fontSize = -1;
				String fontName = "";
				List<TextPosition> partialList = new ArrayList<TextPosition>();
				String partialText = "";
				float lastX = Float.NEGATIVE_INFINITY;
				for (TextPosition textPosition : textPositions) {
					float x1 = textPosition.getX();
					float y1 = textPosition.getY();
					if (x1 < lastX) {
						list.add(new TextComponent(partialText, fromX, fromY, toX, toY, fontName, fontSize));
						writeString(text.substring(partialText.length()), textPositions.subList(partialList.size(), textPositions.size()));
						return;
					} else if (x1 <= fromX) {
						fromX = x1;
						fromY = y1 - textPosition.getHeight();
						fontName = textPosition.getFont().getBaseFont();
						fontSize = textPosition.getFontSizeInPt();
					}
					toX = Math.max(x1 + textPosition.getWidth(), toX);
					toY = Math.max(y1, toY);
					partialList.add(textPosition);
					partialText += textPosition.getCharacter();
					lastX = x1;
				}
				list.add(new TextComponent(text, fromX, fromY, toX, toY, fontName, fontSize));
			}
		}.locateTextComponents();
	}
}
